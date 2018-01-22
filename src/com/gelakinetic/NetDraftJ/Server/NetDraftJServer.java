package com.gelakinetic.NetDraftJ.Server;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JFileChooser;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.gelakinetic.NetDraftJ.Database.MtgCard;
import com.gelakinetic.NetDraftJ.Database.NetDraftJDatabase;
import com.gelakinetic.NetDraftJ.messages.ConnectionRequest;
import com.gelakinetic.NetDraftJ.messages.ConnectionResponse;
import com.gelakinetic.NetDraftJ.messages.MessageUtils;
import com.gelakinetic.NetDraftJ.messages.PickResponse;
import com.gelakinetic.NetDraftJ.messages.StartDraftInfo;

public class NetDraftJServer extends Listener {

    enum Direction {
        LEFT, RIGHT
    }

    // UI
    private NetDraftJServer_ui mUi;

    // Server information
    private Server server;
    public static final int PORT = 54777;

    // Draft state information
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Integer> cubeList = new ArrayList<>(720);
    private int cubeIdx;
    private int packSize;
    private int numPacks;
    private Direction currentPackDirection = Direction.RIGHT;

    /**
     * TODO doc
     * 
     * @param netDraftJServer_ui
     */
    public NetDraftJServer(NetDraftJServer_ui netDraftJServer_ui) {
        mUi = netDraftJServer_ui;
    }

    /**
     * TODO doc
     */
    private void dealPacks() {

        // Mark another pack as dealt
        numPacks--;

        // Reverse the pack direction
        if (currentPackDirection == Direction.LEFT) {
            currentPackDirection = Direction.RIGHT;
        }
        else {
            currentPackDirection = Direction.LEFT;
        }

        // Deal out the packs
        for (Player player : players) {
            player.clearPack();
            for (int packIdx = 0; packIdx < packSize; packIdx++) {
                player.addCardToPack(cubeList.get(cubeIdx++));
            }
        }
    }

    /**
     * TODO doc
     */
    protected void sendPlayersPacks() {
        for (Player player : players) {
            player.sendPickRequest();
        }
    }

    /**
     * TODO doc
     */
    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof ConnectionRequest) {
            ConnectionRequest request = (ConnectionRequest) object;

            // Logging
            mUi.appendText(request.getUuid() + ": " + request.getName() + " joined the draft");

            // Let the player know they've joined
            ConnectionResponse response = new ConnectionResponse();
            connection.sendTCP(response);

            // Save the player's information
            Player player = new Player(connection, request, 0);
            players.add(player);
        }
        else if (object instanceof PickResponse) {
            PickResponse response = (PickResponse) object;
            boolean allPicked = true;

            // Find the player reporting a pick
            for (Player player : players) {
                if (response.getUuid() == player.getUuid()) {
                    // Mark that card as picked
                    player.pickCard(response.getPick());
                }
                // Check if any player hasn't made a pick yet
                if (false == player.getPicked()) {
                    allPicked = false;
                }
            }

            // If all players have made their picks
            if (allPicked) {
                // If the packs are empty and there are more to be drafted
                if (players.get(0).getPack().size() == 0) {
                    if (numPacks > 0) {
                        // deal new ones
                        dealPacks();
                        sendPlayersPacks();
                    }
                    else {
                        for (Player player : players) {
                            player.sendDraftOverNotification();
                        }
                    }
                }
                else {
                    // If everyone picked and there are still packs in cards,
                    // shift them
                    shiftPacks(currentPackDirection);
                    // Then send them to the players for picking
                    sendPlayersPacks();
                }
            }
        }
    }

    /**
     * TODO doc
     * 
     * @param dir
     */
    void shiftPacks(Direction dir) {
        switch (dir) {
            case LEFT: {
                ArrayList<Integer> packZero = players.get(0).getPack();
                for (int i = 0; i < players.size() - 1; i++) {
                    players.get(i).setPack(players.get(i + 1).getPack());
                }
                players.get(players.size() - 1).setPack(packZero);
                break;
            }
            case RIGHT: {
                ArrayList<Integer> lastPack = players.get(players.size() - 1).getPack();
                for (int i = players.size() - 1; i > 0; i--) {
                    players.get(i).setPack(players.get(i - 1).getPack());
                }
                players.get(0).setPack(lastPack);

                break;
            }
        }
    }

    /**
     * TODO doc
     * 
     * @param ipAddress
     */
    public boolean startServer(String ipAddress) {

        // Try to load the cube file
        final JFileChooser fc = new JFileChooser("./");
        if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(mUi.getFrame())) {
            mUi.appendText("Loading " + fc.getSelectedFile().getName());
            if (false == loadCubeList(fc.getSelectedFile())) {
                mUi.appendText("Failed to open cube file");
                return false;
            }
        }
        else {
            mUi.appendText("User didn't pick a file");
            return false;
        }
        mUi.appendText(fc.getSelectedFile().getName() + " loaded");

        // Start the server!
        server = new Server();
        server.start();
        try {
            server.bind(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        MessageUtils.registerAll(server.getKryo());
        server.addListener(this);

        mUi.appendText("Server started on " + ipAddress + ":" + PORT);
        return true;
    }

    /**
     * TODO doc
     * 
     * @param cubeFile
     * @return
     */
    private boolean loadCubeList(File cubeFile) {
        try {
            // Open the database and cube file
            NetDraftJDatabase database = new NetDraftJDatabase();
            BufferedReader fileReader = new BufferedReader(new FileReader(cubeFile));
            String line;

            // Read the file and fill in extra info from the database, save the
            // multiverseID
            while ((line = fileReader.readLine()) != null) {
                MtgCard card = new MtgCard(line);
                database.loadCard(card);
                cubeList.add(card.getMultiverseId());
            }

            // Clean up
            fileReader.close();
            database.closeConnection();

            // Shuffle the cube
            Collections.shuffle(cubeList);

            // Return the shuffled multiverseIDs
            return true;
        } catch (IOException | SQLException e) {
            // Something went wrong, print out what and return null
            e.printStackTrace();
            return false;
        }
    }

    /**
     * TODO doc
     * 
     * @return
     * @throws UnknownHostException
     */
    public static String getPublicIp() throws UnknownHostException {
        try {
            URL url_name = new URL("http://bot.whatismyipaddress.com");
            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
            return sc.readLine().trim();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * TODO doc
     */
    public void stopServer() {
        server.stop();
        server = null;
    }

    /**
     * TODO doc
     * 
     * @param e
     */
    public void clickStartGameButton(ActionEvent e) {

        // Randomize seating
        Collections.shuffle(players);

        // Tell everyone the seating order
        StartDraftInfo sdi = new StartDraftInfo();
        sdi.setSeatingOrder(players);
        server.sendToAllTCP(sdi);

        // Figure out pack size and number of packs
        packSize = (players.size() * 2) - 1;
        numPacks = (int) Math.ceil(45 / (float) packSize);

        if (packSize * numPacks > cubeList.size()) {
            int cardsPerPlayer = (int) Math.floor(cubeList.size() / (double) players.size());
            packSize = (int) Math.floor(cardsPerPlayer / 3.0f);
        }
        cubeIdx = 0;

        // Deal out the packs
        dealPacks();

        // Send the packs to the drafters
        sendPlayersPacks();
    }
}
