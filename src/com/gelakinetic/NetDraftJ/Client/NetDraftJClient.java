package com.gelakinetic.NetDraftJ.Client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.gelakinetic.NetDraftJ.Database.MtgCard;
import com.gelakinetic.NetDraftJ.Messages.ConnectionRequest;
import com.gelakinetic.NetDraftJ.Messages.ConnectionResponse;
import com.gelakinetic.NetDraftJ.Messages.DraftOverNotification;
import com.gelakinetic.NetDraftJ.Messages.MessageUtils;
import com.gelakinetic.NetDraftJ.Messages.PickRequest;
import com.gelakinetic.NetDraftJ.Messages.PickResponse;
import com.gelakinetic.NetDraftJ.Messages.StartDraftInfo;
import com.gelakinetic.NetDraftJ.Server.NetDraftJServer;

public class NetDraftJClient extends Listener {

    // UI parts
    private NetDraftJClient_ui mUi;

    // User data
    private Client client;
    private long mUuid;

    // Picked cards
    private boolean pickedCard;
    private ArrayList<String> mAllPicks;

    /**
     * TODO doc
     * 
     * @param ui
     */
    public NetDraftJClient(NetDraftJClient_ui ui) {
        this.mUi = ui;
        this.pickedCard = false;
        this.mAllPicks = new ArrayList<>();
    }

    /**
     * TODO doc
     * 
     * @param connection
     * @param object
     */
    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof ConnectionResponse) {
            ConnectionResponse request = (ConnectionResponse) object;
            if (request.getConnectionStatus()) {
                mUi.appendText("Connected to " + connection.getRemoteAddressTCP().toString());
            }
            else {
                mUi.appendText("Connection to server failed");
            }
        }
        else if (object instanceof PickRequest) {
            PickRequest response = (PickRequest) object;
            mUi.loadPack(response.getPack());
            pickedCard = false;
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
        else if (object instanceof DraftOverNotification) {
            // DraftOverNotification don = (DraftOverNotification) object;

            mUi.appendText("<html><br>Draft is complete.<br><br></html>");

            File saveFile = mUi.getSaveFile();
            if (null != saveFile) {
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
            else {
                mUi.appendText("Drafted cards not saved, please copy them to a plaintext .dec file");
            }
        }
    }

    /**
     * TODO doc
     * 
     * @param name
     * @param serverIp
     */
    public void connectToServer(String name, String serverIp) {
        if (null == name || name.isEmpty()) {
            mUi.appendText("Name can't be empty");
            return;
        }
        else if (null == serverIp || serverIp.isEmpty()) {
            mUi.appendText("Server IP address can't be empty");
            return;
        }
        try {
            client = new Client();
            client.start();
            client.connect(5000, serverIp, NetDraftJServer.PORT);
            MessageUtils.registerAll(client.getKryo());
            client.addListener(this);

            if (client.isConnected()) {
                ConnectionRequest request = new ConnectionRequest(name);
                client.sendTCP(request);
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
     * TODO doc
     * 
     * @param card
     */
    public void pickCard(MtgCard card) {
        if (false == pickedCard) {
            pickedCard = true;
            client.sendTCP(new PickResponse(card.getMultiverseId(), mUuid));
            mAllPicks.add(card.getName());
        }
    }

    /**
     * TODO doc
     * 
     * @return
     */
    public NetDraftJClient_ui getUi() {
        return mUi;
    }
}
