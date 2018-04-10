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

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.gelakinetic.NetDraftJ.Database.MtgCard;
import com.gelakinetic.NetDraftJ.Database.NetDraftJDatabase;
import com.gelakinetic.NetDraftJ.Messages.ConnectionRequest;
import com.gelakinetic.NetDraftJ.Messages.ConnectionResponse;
import com.gelakinetic.NetDraftJ.Messages.MessageUtils;
import com.gelakinetic.NetDraftJ.Messages.PickResponse;
import com.gelakinetic.NetDraftJ.Messages.StartDraftInfo;

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
    boolean draftStarted = false;

    private Thread disconnectCheckThread;

    /**
     * TODO doc
     * 
     * @param netDraftJServer_ui
     */
    public NetDraftJServer(NetDraftJServer_ui netDraftJServer_ui) {
        mUi = netDraftJServer_ui;
        draftStarted = false;
    }

    /**
     * TODO doc
     */
    private void dealPacks() {

        synchronized (players) {
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
    }

    /**
     * TODO doc
     */
    protected void sendPlayersPacks() {
        synchronized (players) {
            for (Player player : players) {
                player.sendPickRequest(false);
            }
        }
    }

    /**
     * TODO doc
     */
    @Override
    public void received(Connection connection, Object object) {
        synchronized (players) {

            if (object instanceof ConnectionRequest) {
                ConnectionRequest request = (ConnectionRequest) object;
                if (!draftStarted) {
                    // Draft hasn't started, so let the player join

                    // Logging
                    mUi.appendText(request.getUuid() + ": " + request.getName() + " joined the draft");

                    // Let the player know they've joined
                    connection.sendTCP(new ConnectionResponse(true));

                    // Save the player's information
                    Player player = new Player(connection, request);
                    players.add(player);

                }
                else {
                    boolean reconnected = false;
                    for (Player player : players) {
                        if (player.getUuid() == request.getUuid() && player.isDetached()) {
                            reconnected = true;

                            player.initalize(connection, request);

                            // Let the player know they've joined
                            connection.sendTCP(new ConnectionResponse(true));

                            // Send the reconnected player their seating info
                            player.sendSeatingOrder(players);

                            // Send picked list
                            player.sendPreviousPicks();

                            // Send the reconnected player their pick request
                            player.sendPickRequest(true);

                            // Logging
                            mUi.appendText("Reconnected " + request.getUuid() + ": " + request.getName());

                            break;
                        }
                    }

                    if (!reconnected) {
                        // Logging
                        mUi.appendText("Rejected " + request.getUuid() + ": " + request.getName());

                        // Let the player know they've been rejected
                        connection.sendTCP(new ConnectionResponse(false));
                    }
                }
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
    }

    /**
     * TODO doc
     * 
     * @param dir
     */
    void shiftPacks(Direction dir) {
        synchronized (players) {

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
    }

    /**
     * TODO doc
     * 
     * @param ipAddress
     */
    public void startServer(String ipAddress) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                mUi.setButtonEnabled(false);
                File cubeFile = mUi.pickCubeFile();
                if (null == cubeFile) {
                    mUi.appendText("User didn't pick a file");
                }
                else {
                    mUi.appendText("Loading " + cubeFile.getName());
                    if (false == loadCubeList(cubeFile)) {
                        mUi.appendText("Failed to open cube file");
                        return;
                    }
                    mUi.appendText(cubeFile.getName() + " loaded");
                }

                // Start the server!
                server = new Server();
                server.start();
                try {
                    server.bind(PORT);
                } catch (IOException e) {
                    mUi.appendText(e.getMessage());
                    e.printStackTrace();
                    return;
                }
                MessageUtils.registerAll(server.getKryo());
                server.addListener(NetDraftJServer.this);

                mUi.appendText("Server started on " + ipAddress + ":" + PORT);
                mUi.setButtonEnabled(true);
                mUi.setHostMenuItemEnabled(false);
                draftStarted = false;

                disconnectCheckThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while (true) {
                            synchronized (players) {
                                for (int i = 0; i < players.size(); i++) {
                                    if (!players.get(i).isConnected()) {
                                        if (!draftStarted) {
                                            // If they disconnect before the draft starts, drop em
                                            mUi.appendText(players.get(i).getName() + " left the draft");
                                            players.remove(i);
                                            i--;
                                        }
                                        else if (!players.get(i).isDetached()) {
                                            // If they disconnect after the draft starts, wait for a reconnect
                                            players.get(i).setDetached(true);
                                            mUi.appendText(
                                                    players.get(i).getName() + " disconnected. Waiting for reconnect");
                                        }
                                    }
                                }
                            }
                            try {
                                Thread.sleep(1000 * 8);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                });
                disconnectCheckThread.run();
                return;
            }
        }).start();
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
                if (database.loadCard(card)) {
                    cubeList.add(card.getMultiverseId());
                }
            }

            // Clean up
            fileReader.close();
            database.closeConnection();

            if (cubeList.isEmpty()) {
                return false;
            }

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
        if (null != server) {
            server.stop();
        }
        server = null;
        mUi.setHostMenuItemEnabled(true);
        if (null != disconnectCheckThread) {
            disconnectCheckThread.stop();
        }
    }

    /**
     * TODO doc
     * 
     * @param e
     */
    public void clickStartGameButton(ActionEvent e) {

        synchronized (players) {
            // Mark the draft as started
            draftStarted = true;

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
}
