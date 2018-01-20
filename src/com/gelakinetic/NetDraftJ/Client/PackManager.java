package com.gelakinetic.NetDraftJ.Client;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import com.gelakinetic.NetDraftJ.Database.MtgCard;
import com.gelakinetic.NetDraftJ.Database.NetDraftJDatabase;

public class PackManager {

    static final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    /**
     * TODO doc
     * 
     * @param packGridLayout
     * @param textArea
     * @param pack
     */
    public static void loadPack(NetDraftJClient netDraftJClient, int[] pack) {

        // First clear out the grid
        netDraftJClient.getUi().removeAllCards();

        for (int i = 0; i < pack.length; i++) {
            final MtgCard card = new MtgCard(pack[i]);

            ImageLabel lblCard = new ImageLabel();
            lblCard.setHorizontalAlignment(SwingConstants.CENTER);
            lblCard.setVerticalAlignment(SwingConstants.CENTER);

            lblCard.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent arg0) {

                    // Custom button text
                    Object[] options = { "Yes, please", "No, thanks" };
                    int choice = JOptionPane.showOptionDialog(netDraftJClient.getUi().getFrame(),
                            "Sure you want to draft " + card.mCardName + "?", "Double Checking",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

                    switch (choice) {
                        case JOptionPane.YES_OPTION: {
                            netDraftJClient.getUi().removeCard(lblCard);
                            netDraftJClient.getUi().removeCardListeners();
                            netDraftJClient.getUi().redrawCards();
                            netDraftJClient.getUi().appendText("1 " + card.mCardName + '\n');
                            netDraftJClient.pickCard(card);
                            break;
                        }
                        default:
                        case JOptionPane.NO_OPTION: {
                            break;
                        }
                    }
                }
            });

            netDraftJClient.getUi().addCard(lblCard);

            threadPool.submit(new Runnable() {

                @Override
                public void run() {
                    NetDraftJDatabase database = new NetDraftJDatabase();
                    try {
                        // TODO hover tooltip with oracle text?
                        database.loadCard(card);
                        String filename = downloadImage(card);
                        lblCard.setIcon(new ImageIcon(filename));
                        lblCard.setHorizontalAlignment(SwingConstants.CENTER);
                        lblCard.setVerticalAlignment(SwingConstants.CENTER);
                        netDraftJClient.getUi().redrawCard(lblCard);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    database.closeConnection();

                }
            });

        }
    }

    /**
     * TODO doc
     * 
     * @param card
     * @return
     */
    private static String downloadImage(MtgCard card) {

        String cardLanguage = "en";

        String mImageKey = Integer.toString(card.mMultiverseId) + cardLanguage;

        /* Check disk cache in background thread first */
        if (Files.exists(Paths.get("cache", mImageKey))) {
            return Paths.get("cache", mImageKey).toString();
        }

        /* Download the image */
        boolean bRetry = true;

        boolean triedMtgi = false;
        boolean triedGatherer = false;
        boolean triedScryfall = false;

        while (bRetry) {

            bRetry = false;

            try {
                URL picUrl;
                if (!cardLanguage.equalsIgnoreCase("en")) {
                    /*
                     * Non-English have to come from magiccards.info. Try there
                     * first
                     */
                    picUrl = new URL(getMtgiPicUrl(card, cardLanguage));
                    /* If this fails, try next time with the English version */
                    cardLanguage = "en";
                }
                else if (!triedScryfall) {
                    /* Try downloading the image from Scryfall next */
                    picUrl = new URL(getScryfallImageUri(card));
                    /*
                     * If this fails, try next time with the Magiccards.info
                     * version
                     */
                    triedScryfall = true;
                }
                else if (!triedMtgi) {
                    /* Try downloading the image from magiccards.info next */
                    picUrl = new URL(getMtgiPicUrl(card, cardLanguage));
                    /* If this fails, try next time with the gatherer version */
                    triedMtgi = true;
                }
                else {
                    /* Try downloading the image from gatherer */
                    picUrl = new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid="
                            + card.mMultiverseId + "&type=card");
                    /* If this fails, give up */
                    triedGatherer = true;
                }

                /* Download the bitmap */
                if (!Files.exists(Paths.get("cache"))) {
                    Files.createDirectory(Paths.get("cache"));
                }
                Path output = Paths.get("cache", mImageKey);
                Files.copy(picUrl.openStream(), output, StandardCopyOption.REPLACE_EXISTING);

                /* Gatherer is always tried last. If that fails, give up */
                if (!triedGatherer) {
                    bRetry = true;
                }

                return output.toString();

            } catch (Exception e) {
                /* Something went wrong */
                e.printStackTrace();

                /* Gatherer is always tried last. If that fails, give up */
                if (!triedGatherer) {
                    bRetry = true;
                }
            }
        }
        return null;
    }

    /**
     * Jumps through hoops and returns a correctly formatted URL for
     * magiccards.info's image.
     *
     * @param cardName
     *            The name of the card
     * @param magicCardsInfoSetCode
     *            The set of the card
     * @param cardNumber
     *            The number of the card
     * @param cardLanguage
     *            The language of the card
     * @return a URL to the card's image
     */
    private static String getMtgiPicUrl(MtgCard card, String language) {

        final String mtgiExtras = "http://magiccards.info/extras/";
        String picURL;
        if (card.mCardType.toLowerCase().contains("Ongoing".toLowerCase()) ||
        /* extra space to not confuse with planeswalker */
                card.mCardType.toLowerCase().contains("Plane ".toLowerCase())
                || card.mCardType.toLowerCase().contains("Phenomenon".toLowerCase())
                || card.mCardType.toLowerCase().contains("Scheme".toLowerCase())) {
            switch (card.mSetCode) {
                case "PC2":
                    picURL = mtgiExtras + "plane/planechase-2012-edition/" + card.mCardName + ".jpg";
                    picURL = picURL.replace(" ", "-").replace("?", "").replace(",", "").replace("'", "").replace("!",
                            "");
                    break;
                case "PCH":
                    if (card.mCardName.equalsIgnoreCase("tazeem")) {
                        card.mCardName = "tazeem-release-promo";
                    }
                    else if (card.mCardName.equalsIgnoreCase("celestine reef")) {
                        card.mCardName = "celestine-reef-pre-release-promo";
                    }
                    else if (card.mCardName.equalsIgnoreCase("horizon boughs")) {
                        card.mCardName = "horizon-boughs-gateway-promo";
                    }
                    picURL = mtgiExtras + "plane/planechase/" + card.mCardName + ".jpg";
                    picURL = picURL.replace(" ", "-").replace("?", "").replace(",", "").replace("'", "").replace("!",
                            "");
                    break;
                case "ARC":
                    picURL = mtgiExtras + "scheme/archenemy/" + card.mCardName + ".jpg";
                    picURL = picURL.replace(" ", "-").replace("?", "").replace(",", "").replace("'", "").replace("!",
                            "");
                    break;
                default:
                    picURL = "http://magiccards.info/scans/" + language + "/" + card.mMagicCardsInfoSetCode + "/"
                            + card.mCardNumber + ".jpg";
                    break;
            }
        }
        else {
            picURL = "http://magiccards.info/scans/" + language + "/" + card.mMagicCardsInfoSetCode + "/"
                    + card.mCardNumber + ".jpg";
        }
        return picURL.toLowerCase(Locale.ENGLISH);
    }

    /**
     * Easily gets the uri for the image for a card by multiverseid.
     *
     * @param multiverseId
     *            the multiverse id of the card
     * @return uri of the card image
     */
    private static String getScryfallImageUri(MtgCard card) {
        return "https://api.scryfall.com/cards/multiverse/" + card.mMultiverseId + "?format=image&version=normal";
    }

}
