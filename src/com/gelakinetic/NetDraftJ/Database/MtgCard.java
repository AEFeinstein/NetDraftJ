package com.gelakinetic.NetDraftJ.Database;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class MtgCard {

    /* Special values for KEY_POWER and KEY_TOUGHNESS */
    private static final int STAR = -1000;
    private static final int ONE_PLUS_STAR = -1001;
    private static final int TWO_PLUS_STAR = -1002;
    private static final int SEVEN_MINUS_STAR = -1003;
    private static final int STAR_SQUARED = -1004;
    private static final int NO_ONE_CARES = -1005;
    private static final int X = -1006;
    private static final float QUESTION_MARK = -1007;
    private static final float INFINITY = 1000000000;

    private int mMultiverseId = 0;
    private String mCardNumber = null;

    private String mName = null;
    private String mManaCost = null;
    private String mSuperType = null;
    private String mSubType = null;
    private String mColor = null;

    private String mText = null;

    private double mToughness = NO_ONE_CARES;
    private double mPower = NO_ONE_CARES;
    private double mLoyalty = NO_ONE_CARES;

    private String mFlavor = null;
    private String mArtist = null;
    private String mWatermark = null;

    private String mSetCode = null;
    private String mMagicCardsInfoSetCode = null;

    /**
     * Create a magic card with the given name
     * 
     * @param name
     *            The name for this object
     */
    public MtgCard(String name) {
        mName = name;
    }

    /**
     * Create a magic card with the given multiverse ID
     * 
     * @param multiverseId
     *            The multiverse ID for this object
     */
    public MtgCard(int multiverseId) {
        mMultiverseId = multiverseId;
    }

    /**
     * Build the HTML tooltip text for this card, which is basically all the information about it
     * 
     * @return The HTML formatted String with all this card's information
     */
    public String getToolTipText() {
        StringBuilder sb = new StringBuilder();

        sb.append("<html>");
        sb.append(mName);
        sb.append("<br>");

        if (!mManaCost.isEmpty()) {
            sb.append(mManaCost);
            sb.append("<br>");
        }

        sb.append(mSuperType);
        if (!mSubType.isEmpty()) {
            sb.append(" - ");
            sb.append(mSubType);
        }
        sb.append("<br>");

        if (!mText.isEmpty()) {
            sb.append(mText);
            sb.append("<br>");
        }

        String ptl = getPTLString();
        if (null != ptl) {
            sb.append(ptl);
            sb.append("<br>");
        }

        if (!mFlavor.isEmpty()) {
            sb.append("<i>");
            sb.append(mFlavor);
            sb.append("</i><br>");
        }

        if (!mArtist.isEmpty()) {
            sb.append("Artist: ");
            sb.append(mArtist);
            sb.append("<br>");
        }

        if (!mWatermark.isEmpty()) {
            sb.append("Watermark: ");
            sb.append(mWatermark);
            sb.append("<br>");
        }
        sb.append("</html>");
        return sb.toString();
    }

    /**
     * Convert a double representation of a power, toughness, or loyalty into the String representation
     * 
     * @param stat
     *            The stat to convert
     * @return The String representation of this stat
     */
    private static String getPrintedPTL(double stat) {
        if (stat == STAR) {
            return "*";
        }
        else if (stat == ONE_PLUS_STAR) {
            return "1+*";
        }
        else if (stat == TWO_PLUS_STAR) {
            return "2+*";
        }
        else if (stat == SEVEN_MINUS_STAR) {
            return "7-*";
        }
        else if (stat == STAR_SQUARED) {
            return "*^2";
        }
        else if (stat == X) {
            return "X";
        }
        else if (stat == QUESTION_MARK) {
            return "?";
        }
        else if (stat == INFINITY) {
            return "∞";
        }
        else if (stat == NO_ONE_CARES) {
            return "";
        }
        else {
            if (stat == (int) stat) {
                return String.format(Locale.US, "%d", (int) stat);
            }
            else {
                return String.format(Locale.US, "%.1f", stat);
            }
        }
    }

    /**
     * Build and return this card's power & toughness or Loyalty String
     * 
     * @return The power & toughness String, or null if the card doesn't have one
     */
    private String getPTLString() {
        if (mPower != NO_ONE_CARES || mToughness != NO_ONE_CARES) {
            return getPrintedPTL(mPower) + " / " + getPrintedPTL(mToughness);
        }
        else if (mLoyalty != NO_ONE_CARES) {
            return getPrintedPTL(mLoyalty);
        }
        else {
            return null;
        }
    }

    /**
     * Jumps through hoops and returns a correctly formatted URL for MagicCards.info's image.
     *
     * @return a URL to the card's image
     */
    private String getMtgiPicUrl(String language) {

        final String mtgiExtras = "http://magiccards.info/extras/";
        String picURL;
        if (mSuperType.toLowerCase().contains("Ongoing".toLowerCase()) ||
        /* extra space to not confuse with planeswalker */
                mSuperType.toLowerCase().contains("Plane ".toLowerCase())
                || mSuperType.toLowerCase().contains("Phenomenon".toLowerCase())
                || mSuperType.toLowerCase().contains("Scheme".toLowerCase())) {
            switch (mSetCode) {
                case "PC2":
                    picURL = mtgiExtras + "plane/planechase-2012-edition/" + mName + ".jpg";
                    picURL = picURL.replace(" ", "-").replace("?", "").replace(",", "").replace("'", "").replace("!",
                            "");
                    break;
                case "PCH":
                    if (mName.equalsIgnoreCase("tazeem")) {
                        mName = "tazeem-release-promo";
                    }
                    else if (mName.equalsIgnoreCase("celestine reef")) {
                        mName = "celestine-reef-pre-release-promo";
                    }
                    else if (mName.equalsIgnoreCase("horizon boughs")) {
                        mName = "horizon-boughs-gateway-promo";
                    }
                    picURL = mtgiExtras + "plane/planechase/" + mName + ".jpg";
                    picURL = picURL.replace(" ", "-").replace("?", "").replace(",", "").replace("'", "").replace("!",
                            "");
                    break;
                case "ARC":
                    picURL = mtgiExtras + "scheme/archenemy/" + mName + ".jpg";
                    picURL = picURL.replace(" ", "-").replace("?", "").replace(",", "").replace("'", "").replace("!",
                            "");
                    break;
                default:
                    picURL = "http://magiccards.info/scans/" + language + "/" + mMagicCardsInfoSetCode + "/"
                            + mCardNumber + ".jpg";
                    break;
            }
        }
        else {
            picURL = "http://magiccards.info/scans/" + language + "/" + mMagicCardsInfoSetCode + "/" + mCardNumber
                    + ".jpg";
        }
        return picURL.toLowerCase(Locale.ENGLISH);
    }

    /**
     * Easily gets the uri for the image for a card by multiverse ID.
     *
     * @return uri of the card image
     */
    private String getScryfallImageUri() {
        return "https://api.scryfall.com/cards/multiverse/" + mMultiverseId + "?format=image&version=normal";
    }

    /**
     * @return The String URL to this card's image on Gatherer
     */
    private String getGathererPicUrl() {
        return "http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + mMultiverseId + "&type=card";
    }

    /**
     * @return The card's name
     */
    public String getName() {
        return mName;
    }

    /**
     * @return THe card's multiverse ID
     */
    public int getMultiverseId() {
        return mMultiverseId;
    }

    /**
     * Fill in this card's data from the result of a SQLite query
     * 
     * @param resultSet
     *            The result of a SQLite query
     * @throws SQLException
     *             if there's a database error
     */
    boolean setDataFromQuery(ResultSet resultSet) throws SQLException {
        if (!resultSet.isClosed()) {
            mMultiverseId = resultSet.getInt(resultSet.findColumn("multiverseID"));
            mCardNumber = resultSet.getString(resultSet.findColumn("number"));

            mName = resultSet.getString(resultSet.findColumn("card_name"));
            mSuperType = resultSet.getString(resultSet.findColumn("supertype"));
            mSubType = resultSet.getString(resultSet.findColumn("subtype"));
            mColor = resultSet.getString(resultSet.findColumn("color"));
            // noinspection SpellCheckingInspection
            mManaCost = resultSet.getString(resultSet.findColumn("manacost"));
            mPower = resultSet.getDouble(resultSet.findColumn("power"));
            mToughness = resultSet.getDouble(resultSet.findColumn("toughness"));
            mLoyalty = resultSet.getDouble(resultSet.findColumn("loyalty"));
            // noinspection SpellCheckingInspection
            mText = resultSet.getString(resultSet.findColumn("cardtext"));

            mFlavor = resultSet.getString(resultSet.findColumn("flavor"));
            mArtist = resultSet.getString(resultSet.findColumn("artist"));
            mWatermark = resultSet.getString(resultSet.findColumn("watermark"));

            mSetCode = resultSet.getString(resultSet.findColumn("set_code"));
            mMagicCardsInfoSetCode = resultSet.getString(resultSet.findColumn("set_code_mtgi"));
            return true;
        }
        return false;
    }

    /**
     * Download and save an image of this card, then return a String path to that image
     * 
     * @return A String path to where this image was downloaded
     */
    public String downloadImage() {
        String cardLanguage = "en";

        String mImageKey = Integer.toString(getMultiverseId()) + cardLanguage;

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
            Path output = Paths.get("cache", mImageKey);

            try {
                URL picUrl;
                if (!triedScryfall) {
                    /* Try downloading the image from Scryfall next */
                    picUrl = new URL(getScryfallImageUri());
                    /*
                     * If this fails, try next time with the MagicCards.info version
                     */
                    triedScryfall = true;
                }
                else if (!triedMtgi) {
                    /* Try downloading the image from MagicCards.info next */
                    picUrl = new URL(getMtgiPicUrl(cardLanguage));
                    /* If this fails, try next time with the gatherer version */
                    triedMtgi = true;
                }
                else {
                    /* Try downloading the image from gatherer */
                    picUrl = new URL(getGathererPicUrl());
                    /* If this fails, give up */
                    triedGatherer = true;
                }

                /* Download the bitmap */
                if (!Files.exists(Paths.get("cache"))) {
                    Files.createDirectory(Paths.get("cache"));
                }
                Files.copy(picUrl.openStream(), output, StandardCopyOption.REPLACE_EXISTING);
                long fileSize = new File(output.toString()).length();
                if (5000 > fileSize) {
                    throw new Exception("Image too small, " + picUrl.toString() + ", " + fileSize);
                }

                /* Gatherer is always tried last. If that fails, give up */
                if (!triedGatherer) {
                    bRetry = true;
                }

                return output.toString();

            } catch (Exception e) {
                /* Something went wrong */
                e.printStackTrace();

                try {
                    Files.delete(output);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                /* Gatherer is always tried last. If that fails, give up */
                if (!triedGatherer) {
                    bRetry = true;
                }
            }
        }
        return null;
    }

    /**
     * @return This card's color as a String with the chars w, u, b, r, g, c, a, and l
     */
    public String getColor() {
        return this.mColor;
    }
}
