package com.gelakinetic.NetDraftJ.Client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.gelakinetic.NetDraftJ.Database.MtgCard;
import com.gelakinetic.NetDraftJ.Database.NetDraftJDatabase;
import com.gelakinetic.NetDraftJ.Messages.ConnectionRequest;
import com.gelakinetic.NetDraftJ.Messages.ConnectionResponse;
import com.gelakinetic.NetDraftJ.Messages.DraftOverNotification;
import com.gelakinetic.NetDraftJ.Messages.MessageUtils;
import com.gelakinetic.NetDraftJ.Messages.PickRequest;
import com.gelakinetic.NetDraftJ.Messages.PickResponse;
import com.gelakinetic.NetDraftJ.Messages.PreviousPicksInfo;
import com.gelakinetic.NetDraftJ.Messages.StartDraftInfo;
import com.gelakinetic.NetDraftJ.Server.NetDraftJServer;

class NetDraftJClient extends Listener {

    // UI parts
    private final NetDraftJClient_ui mUi;

    // User data
    private Client mClient;
    private long mUuid;

    // Picked cards
    private boolean mPickedCard;
    private final ArrayList<String> mAllPicks;

    /**
     * Construct a client to power the given UI
     * 
     * @param ui The UI this client will display everything in
     */
    NetDraftJClient(NetDraftJClient_ui ui) {
        this.mUi = ui;
        this.mPickedCard = false;
        this.mAllPicks = new ArrayList<>();
        if (ui.hasAssignedUuid()) {
            this.mUuid = ui.getAssignedUuid();
        }
    }

    /**
     * This is called whenever a TCP message is received from the server
     *
     * @param connection The connection which received the TCP Message
     * @param object The de-serialized TCP message
     */
    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof ConnectionResponse) {
            ConnectionResponse response = (ConnectionResponse) object;
            if (response.getConnectionStatus()) {
                mUi.appendText("Connected to " + connection.getRemoteAddressTCP().toString());
                mUi.setConnectMenuItemEnabled(false);
                mUi.setHostMenuItemEnabled(false);
            }
            else {
                mUi.appendText("Connection rejected: " + response.getMessage());
                mUi.setConnectMenuItemEnabled(true);
                mUi.setHostMenuItemEnabled(true);
            }
        }
        else if (object instanceof PickRequest) {
            PickRequest response = (PickRequest) object;
            mUi.loadPack(response.getPack());
            mPickedCard = response.getPicked();
        }
        else if (object instanceof StartDraftInfo) {
            StartDraftInfo sdi = (StartDraftInfo) object;
            StringBuilder sb = new StringBuilder();
            sb.append("<html>Seating Order<br>");
            sb.append("-------------<br>");
            for (String player : sdi.getSeatingOrder()) {
                sb.append(player);
                sb.append("<br>");
            }
            sb.append("<br>");
            sb.append("Picks<br>");
            sb.append("-------------<br></html>");
            mUi.appendText(sb.toString());
        }
        else if (object instanceof PreviousPicksInfo) {
            // Load up the previous pick information
            PreviousPicksInfo ppi = (PreviousPicksInfo) object;

            NetDraftJDatabase database = new NetDraftJDatabase();

            for (int multiverseId : ppi.getPicks()) {
                MtgCard card = new MtgCard(multiverseId);
                try {
                    database.loadCard(card);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                mUi.appendText(card.getName(), card.getToolTipText(), card.getColor());
                mAllPicks.add(card.getName());
            }
            database.closeConnection();
        }
        else if (object instanceof DraftOverNotification) {
            // DraftOverNotification don = (DraftOverNotification) object;

            mUi.appendText("<html><br>Draft is complete.<br><br></html>");
            this.saveDraftedCards();
        }
    }

    /**
     * Attempt to connect to the server at the given IP address with the given username
     * 
     * @param name The username to connect to the server with
     * @param serverIp The String representation of the dotted decimal IP address
     */
    void connectToServer(String name, String serverIp) {
        if (null == name || name.isEmpty()) {
            mUi.appendText("Name can't be empty");
            return;
        }
        else if (null == serverIp || serverIp.isEmpty()) {
            mUi.appendText("Server IP address can't be empty");
            return;
        }
        try {
            mClient = new Client();
            mClient.start();
            mClient.connect(5000, serverIp, NetDraftJServer.PORT);
            MessageUtils.registerAll(mClient.getKryo());
            mClient.addListener(this);

            if (mClient.isConnected()) {
                ConnectionRequest request;
                if (mUi.hasAssignedUuid()) {
                    request = new ConnectionRequest(name, mUuid);
                }
                else {
                    request = new ConnectionRequest(name, mUi);
                }
                mClient.sendTCP(request);
                mUuid = request.getUuid();
            }
            else {
                mUi.appendText("Connection failed");
            }
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
            mUi.appendText("EXCEPTION: " + e.getMessage());
        }
    }

    /**
     * Pick a card out of the current pack, tell the server, and add the card to the list of picked cards
     * 
     * @param card The card which was picked
     */
    void pickCard(MtgCard card) {
        if (!mPickedCard) {
            mPickedCard = true;
            mClient.sendTCP(new PickResponse(card.getMultiverseId(), mUuid));
            mAllPicks.add(card.getName());
        }
    }

    /**
     * @return The number of picked cards so far
     */
    int getPickedCardCount() {
        return mAllPicks.size();
    }

    /**
     * Pop open a dialog to ask the user where to save the .dec file after a draft, then save it
     */
    void saveDraftedCards() {
        // Only save cards if some have been drafted
        if (!mAllPicks.isEmpty()) {
            // Prompt the user to save a file
            File saveFile = mUi.getSaveFile();

            // Make sure the user picked a file
            if (null == saveFile) {
                mUi.appendText("Drafted cards not saved, please copy them to a plaintext .dec file");
                return;
            }

            // Make sure the file ends in .dec
            final String EXTENSION = "dec";
            if (!FilenameUtils.getExtension(saveFile.getName()).equalsIgnoreCase(EXTENSION)) {
                saveFile = new File(saveFile.getParentFile(),
                        FilenameUtils.getBaseName(saveFile.getName()) + "." + EXTENSION);
            }

            // Save the file
            mUi.appendText("Saving to " + saveFile.getAbsolutePath());
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
                for (String cardName : mAllPicks) {
                    bw.write("1 " + cardName + '\n');
                }
                bw.close();
            } catch (IOException e) {
                mUi.appendText("Drafted cards not saved, please copy them to a plaintext .dec file");
                e.printStackTrace();
            }
        }
    }
}
