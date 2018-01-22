package com.gelakinetic.NetDraftJ.Database;

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
     * TODO doc
     * 
     * @param name
     */
    public MtgCard(String name) {
        mName = name;
    }

    /**
     * TODO doc
     * 
     * @param multiverseId
     */
    public MtgCard(int multiverseId) {
        mMultiverseId = multiverseId;
    }

    /**
     * TODO doc
     * 
     * @return
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
     * TODO doc
     * 
     * @param stat
     * @param displaySign
     * @return
     */
    private static String getPrintedPTL(double stat, boolean displaySign) {
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
                if (displaySign) {
                    return String.format(Locale.US, "%+d", (int) stat);
                }
                return String.format(Locale.US, "%d", (int) stat);
            }
            else {
                if (displaySign) {
                    return String.format(Locale.US, "%+.1f", stat);
                }
                return String.format(Locale.US, "%.1f", stat);
            }
        }
    }

    /**
     * TODO doc
     * 
     * @return
     */
    private String getPTLString() {
        if (mPower != NO_ONE_CARES || mToughness != NO_ONE_CARES) {
            return getPrintedPTL(mPower, false) + " / " + getPrintedPTL(mToughness, false);
        }
        else if (mLoyalty != NO_ONE_CARES) {
            return getPrintedPTL(mLoyalty, false);
        }
        else {
            return null;
        }
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
    public String getMtgiPicUrl(String language) {

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
     * Easily gets the uri for the image for a card by multiverseid.
     *
     * @param multiverseId
     *            the multiverse id of the card
     * @return uri of the card image
     */
    public String getScryfallImageUri() {
        return "https://api.scryfall.com/cards/multiverse/" + mMultiverseId + "?format=image&version=normal";
    }

    /**
     * TODO doc
     * 
     * @param cardLanguage
     * @return
     */
    public String getGathererPicUrl(String cardLanguage) {
        return "http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + mMultiverseId + "&type=card";
    }

    /**
     * TODO doc
     * 
     * @return
     */
    public String getName() {
        return mName;
    }

    /**
     * TODO doc
     * 
     * @return
     */
    public int getMultiverseId() {
        return mMultiverseId;
    }

    /**
     * TODO doc
     * 
     * @param resultSet
     * @throws SQLException
     */
    public void setDataFromQuery(ResultSet resultSet) throws SQLException {
        mMultiverseId = resultSet.getInt(resultSet.findColumn("multiverseID"));
        mCardNumber = resultSet.getString(resultSet.findColumn("number"));

        mName = resultSet.getString(resultSet.findColumn("card_name"));
        mSuperType = resultSet.getString(resultSet.findColumn("supertype"));
        mSubType = resultSet.getString(resultSet.findColumn("subtype"));
        mManaCost = resultSet.getString(resultSet.findColumn("manacost"));
        mPower = resultSet.getDouble(resultSet.findColumn("power"));
        mToughness = resultSet.getDouble(resultSet.findColumn("toughness"));
        mLoyalty = resultSet.getDouble(resultSet.findColumn("loyalty"));
        mText = resultSet.getString(resultSet.findColumn("cardtext"));

        mFlavor = resultSet.getString(resultSet.findColumn("flavor"));
        mArtist = resultSet.getString(resultSet.findColumn("artist"));
        mWatermark = resultSet.getString(resultSet.findColumn("watermark"));

        mSetCode = resultSet.getString(resultSet.findColumn("set_code"));
        mMagicCardsInfoSetCode = resultSet.getString(resultSet.findColumn("set_code_mtgi"));
    }
}
