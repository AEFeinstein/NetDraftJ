package com.gelakinetic.NetDraftJ.Database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NetDraftJDatabase {

    private Connection mDbConnection = null;

    /**
     * Create a new database object and connect it to the database file
     */
    public NetDraftJDatabase() {
        try {
            this.openConnection();
        }
        catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Try to connect to a database file, either in the res folder or bundled in the
     * JAR
     */
    private void openConnection() throws SQLException, ClassNotFoundException {
        if ((new File("res\\mtg.sqlite")).exists()) {
            /* Open up the database */
            Class.forName("org.sqlite.JDBC");
            mDbConnection = DriverManager.getConnection("jdbc:sqlite:res\\mtg.sqlite");
        }
        else {
            /* Open up the database */
            Class.forName("org.sqlite.JDBC");
            mDbConnection = DriverManager.getConnection("jdbc:sqlite::resource:mtg.sqlite");
        }
    }

    /**
     * Close the database connection
     */
    public void closeConnection() {
        /* Close the database */
        if (mDbConnection != null) {
            try {
                mDbConnection.close();
            }
            catch (SQLException e) {
                /* For exceptions, just print them out */
                e.printStackTrace();
            }
        }
    }

    /**
     * Load a card from the database based on either the card's name or multiverse
     * ID
     * 
     * @param card A card with either a name or multiverse ID to be filled in
     * @return true if the card's data was filled in, false if there was an error
     * @throws SQLException if there's a database exception
     */
    public boolean loadCard(MtgCard card) throws SQLException {

        if (null == mDbConnection) {
            return false;
        }

        /* Perform the query */
        Statement         statement = mDbConnection.createStatement();

        String            sqlStatement;
        PreparedStatement preparedStatement;
        // noinspection SpellCheckingInspection
        sqlStatement = "SELECT "
                // Multiverse ID
                + "cards.multiverseID, "
                // Card number
                + "cards.number, "
                // Card name
                + "cards.suggest_text_1 AS card_name, "
                // Supertype
                + "cards.supertype, "
                // Subtype
                + "cards.subtype, "
                // Mana Cost
                + "cards.manacost, "
                // Power
                + "cards.power, "
                // Toughness
                + "cards.toughness, "
                // Loyalty
                + "cards.loyalty, "
                // Card Text
                + "cards.cardtext, "
                // Card Text
                + "cards.color, "
                // Flavor Text
                + "cards.flavor, "
                // Artist
                + "cards.artist, "
                // Watermark
                + "cards.watermark, "
                // Set code
                + "sets.code AS set_code, "
                // Set code (magicCards.info)
                + "sets.code_mtgi AS set_code_mtgi, "
                // Set code (magicCards.info)
                + "sets.suggest_text_1 AS set_name "
                // Join the tables on set code
                + "FROM (cards JOIN sets ON cards.expansion = sets.code) "
                + "WHERE (set_name NOT LIKE 'Masterpiece%') AND " + "(set_name != 'Zendikar Expeditions') AND "
                + "(set_name != 'From the Vault: Transform') AND " + "(set_name != 'Commander Anthology') AND ";

        String orderLogic = "ORDER BY sets.date DESC";

        if (card.getName() != null && !card.getName().isEmpty()) {
            sqlStatement += "(cards.suggest_text_1 = ?) ";
            sqlStatement += orderLogic;
            preparedStatement = mDbConnection.prepareStatement(sqlStatement);
            preparedStatement.setString(1, card.getName());
        }
        else if (card.getMultiverseId() != 0) {
            sqlStatement += "(cards.multiverseID = " + card.getMultiverseId() + ") ";
            sqlStatement += orderLogic;
            preparedStatement = mDbConnection.prepareStatement(sqlStatement);
        }
        else {
            /* Clean up */
            statement.close();
            return false;
        }

        ResultSet resultSet = preparedStatement.executeQuery();

        boolean   returnVal = card.setDataFromQuery(resultSet);

        /* Clean up */
        resultSet.close();
        statement.close();

        return returnVal;
    }

    /**
     * Query for all cards of a given rarity in a given set and return a list of
     * multiverseIDs
     * 
     * @param set    The set to search for cards from
     * @param rarity The rarity of the cards to find
     * @return A list of multiverseIDs, or null if the query fails
     * @throws SQLException if there's a database exception
     */
    public List<Integer> getListBySetAndRarity(String set, char rarity) throws SQLException {
        if (null == mDbConnection) {
            return null;
        }

        // Perform the query
        String             sqlStatement      = "SELECT multiverseID "
                + "FROM cards JOIN sets ON (cards.expansion = sets.code) " + "WHERE cards.expansion = '" + set
                + "' AND cards.rarity = " + (int) rarity
                + " AND cards.supertype != 'Basic Land' AND cards.number NOT LIKE '%b'";
        PreparedStatement  preparedStatement = mDbConnection.prepareStatement(sqlStatement);
        ResultSet          resultSet         = preparedStatement.executeQuery();

        // Allocate an ArrayList to store the results
        ArrayList<Integer> mIds              = new ArrayList<Integer>();

        // Find the column index, just once
        int                colIdx            = resultSet.findColumn("multiverseID");

        // Copy all multiverse IDs to the ArrayList
        while (!resultSet.isAfterLast()) {
            // Make sure this is unique
            int multiverseId = resultSet.getInt(colIdx);
            if (!mIds.contains(multiverseId)) {
                mIds.add(multiverseId);
            }
            resultSet.next();
        }

        // Clean up
        resultSet.close();

        return mIds;
    }
}
