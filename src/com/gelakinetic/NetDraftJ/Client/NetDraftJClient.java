package com.gelakinetic.NetDraftJ.Client;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.gelakinetic.NetDraftJ.Database.MtgCard;
import com.gelakinetic.NetDraftJ.Server.NetDraftJServer;
import com.gelakinetic.NetDraftJ.messages.ConnectionRequest;
import com.gelakinetic.NetDraftJ.messages.ConnectionResponse;
import com.gelakinetic.NetDraftJ.messages.DraftOverNotification;
import com.gelakinetic.NetDraftJ.messages.MessageUtils;
import com.gelakinetic.NetDraftJ.messages.PickRequest;
import com.gelakinetic.NetDraftJ.messages.PickResponse;
import com.gelakinetic.NetDraftJ.messages.StartDraftInfo;

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
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ConnectionResponse request = (ConnectionResponse) object;
                    if (request.getConnectionStatus()) {
                        mUi.appendText("Connected to " + connection.getRemoteAddressTCP().toString() + '\n');
                    }
                    else {
                        mUi.appendText("Connection to server failed");
                    }
                }
            });
        }
        else if (object instanceof PickRequest) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    PickRequest response = (PickRequest) object;
                    PackManager.loadPack(NetDraftJClient.this, response.getPack());
                    pickedCard = false;
                }
            });
        }
        else if (object instanceof StartDraftInfo) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    StartDraftInfo sdi = (StartDraftInfo) object;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Seating Order\n");
                    sb.append("-------------\n");
                    for (String player : sdi.getSeatingOrder()) {
                        sb.append(player);
                        sb.append('\n');
                    }
                    sb.append('\n');
                    sb.append("Picks\n");
                    sb.append("-------------\n");
                    mUi.appendText(sb.toString());
                }
            });
        }
        else if (object instanceof DraftOverNotification) {
            // DraftOverNotification don = (DraftOverNotification) object;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {

                    mUi.appendText("\nDraft is complete.\n\n");

                    JFileChooser fc = new JFileChooser("./");
                    fc.setDialogTitle("Save Drafted Cards");
                    if (JFileChooser.APPROVE_OPTION == fc.showSaveDialog(mUi.getFrame())) {
                        mUi.appendText("Saving to " + fc.getSelectedFile().getAbsolutePath());
                        try {
                            BufferedWriter bw = new BufferedWriter(new FileWriter(fc.getSelectedFile()));
                            for (String cardName : mAllPicks) {
                                bw.write("1 " + cardName + '\n');
                            }
                            bw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        mUi.appendText("Drafted cards not saved, please copy them to a plaintext .dec file");
                    }
                }
            });
        }
    }

    /**
     * TODO doc
     * 
     * @param name
     * @param serverIp
     */
    public void connectToServer(String name, String serverIp) {
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
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
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
