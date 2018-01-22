package com.gelakinetic.NetDraftJ.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class NetDraftJDatabase {

    private Connection mDbConnection = null;

    /**
     * TODO doc
     */
    public NetDraftJDatabase() {
        this.openConnection();
    }

    /**
     * TODO doc
     * 
     * @return
     */
    public boolean openConnection() {
        try {
            /* Open up the database */
            Class.forName("org.sqlite.JDBC");
            mDbConnection = DriverManager.getConnection("jdbc:sqlite::resource:mtg.sqlite");
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            /* For exceptions, just print them out and exit cleanly */
            e.printStackTrace();
        }

        try {
            /* Open up the database */
            Class.forName("org.sqlite.JDBC");
            mDbConnection = DriverManager.getConnection("jdbc:sqlite:res\\mtg.sqlite");
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            /* For exceptions, just print them out and exit cleanly */
            e.printStackTrace();
        }

        return false;
    }

    /**
     * TODO doc
     * 
     * @param mDbConnection
     */
    public void closeConnection() {
        /* Close the database */
        if (mDbConnection != null) {
            try {
                mDbConnection.close();
            } catch (SQLException e) {
                /* For exceptions, just print them out and exit cleanly */
                System.exit(0);
                e.printStackTrace();
            }
        }
    }

    /**
     * TODO doc
     * 
     * @param card
     * @param mDbConnection
     * @return
     * @throws SQLException
     */
    public MtgCard loadCard(MtgCard card) throws SQLException {

        if (null == mDbConnection) {
            return card;
        }

        /* Perform the query */
        Statement statement = mDbConnection.createStatement();

        String sqlStatement = null;
        PreparedStatement pstmt = null;
        sqlStatement = 
                "SELECT "
                    + "cards.multiverseID, "
                    + "cards.number, "
                    // Card data
                    + "cards.suggest_text_1 AS card_name, "
                    + "cards.supertype, "
                    + "cards.subtype, "
                    + "cards.manacost, "
                    + "cards.power, "
                    + "cards.toughness, "
                    + "cards.loyalty, "
                    + "cards.cardtext, "
                    // Fluff
                    + "cards.flavor, "
                    + "cards.artist, "
                    + "cards.watermark, "
                    // Set information
                    + "sets.code AS set_code, "
                    + "sets.code_mtgi AS set_code_mtgi "
                + "FROM (cards JOIN sets ON cards.expansion = sets.code) ";
        String orderLogic = "ORDER BY sets.date DESC";

        if (card.getName() != null && !card.getName().isEmpty()) {
            sqlStatement += "WHERE cards.suggest_text_1 = ? ";
            sqlStatement += orderLogic;
            pstmt = mDbConnection.prepareStatement(sqlStatement);
            pstmt.setString(1, card.getName());
        }
        else if (card.getMultiverseId() != 0) {
            sqlStatement += "WHERE cards.multiverseID = " + card.getMultiverseId() + " ";
            sqlStatement += orderLogic;
            pstmt = mDbConnection.prepareStatement(sqlStatement);
        }
        else {
            /* Clean up */
            statement.close();
            return card;
        }

        ResultSet resultSet = pstmt.executeQuery();

        card.setDataFromQuery(resultSet);

        /* Clean up */
        resultSet.close();
        statement.close();

        return card;
    }
}