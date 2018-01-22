package com.gelakinetic.NetDraftJ.Client;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import com.gelakinetic.NetDraftJ.Database.MtgCard;
import com.gelakinetic.NetDraftJ.Database.NetDraftJDatabase;

public class PackManager {

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(1);

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
                            "Sure you want to draft " + card.getName() + "?", "Double Checking",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

                    switch (choice) {
                        case JOptionPane.YES_OPTION: {
                            netDraftJClient.getUi().removeCard(lblCard);
                            netDraftJClient.getUi().removeCardListeners();
                            netDraftJClient.getUi().redrawCards();
                            netDraftJClient.getUi().appendText("1 " + card.getName() + '\n');
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
                        database.loadCard(card);
                        String filename = downloadImage(card);
                        lblCard.setIcon(new ImageIcon(filename));
                        lblCard.setHorizontalAlignment(SwingConstants.CENTER);
                        lblCard.setVerticalAlignment(SwingConstants.CENTER);
                        lblCard.setToolTipText(card.getToolTipText());
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

        String mImageKey = Integer.toString(card.getMultiverseId()) + cardLanguage;

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
                    picUrl = new URL(card.getMtgiPicUrl(cardLanguage));
                    /* If this fails, try next time with the English version */
                    cardLanguage = "en";
                }
                else if (!triedScryfall) {
                    /* Try downloading the image from Scryfall next */
                    picUrl = new URL(card.getScryfallImageUri());
                    /*
                     * If this fails, try next time with the Magiccards.info
                     * version
                     */
                    triedScryfall = true;
                }
                else if (!triedMtgi) {
                    /* Try downloading the image from magiccards.info next */
                    picUrl = new URL(card.getMtgiPicUrl(cardLanguage));
                    /* If this fails, try next time with the gatherer version */
                    triedMtgi = true;
                }
                else {
                    /* Try downloading the image from gatherer */
                    picUrl = new URL(card.getGathererPicUrl(cardLanguage));
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

}
