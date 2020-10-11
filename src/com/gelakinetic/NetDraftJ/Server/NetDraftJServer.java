package com.gelakinetic.NetDraftJ.Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.gelakinetic.NetDraftJ.Client.NetDraftJClient_ui;
import com.gelakinetic.NetDraftJ.Database.MtgCard;
import com.gelakinetic.NetDraftJ.Database.NetDraftJDatabase;
import com.gelakinetic.NetDraftJ.Messages.ConnectionRequest;
import com.gelakinetic.NetDraftJ.Messages.ConnectionResponse;
import com.gelakinetic.NetDraftJ.Messages.MessageUtils;
import com.gelakinetic.NetDraftJ.Messages.PickResponse;
import com.gelakinetic.NetDraftJ.Server.LimitedDraftPacks.LimitedPack;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class NetDraftJServer extends Listener {

    enum Direction {
        LEFT, RIGHT
    }

    // UI
    private final NetDraftJServer_ui mUi;

    // Server information
    private Server                   mServer;
    public static final int          PORT                       = 54777;

    // Draft state information
    private final ArrayList<Player>  mPlayers                   = new ArrayList<>();
    private final ArrayList<Integer> mCubeList                  = new ArrayList<>(720);
    private int                      mCubeIdx;
    private int                      mPackSize;
    private int                      mNumPacks;
    private Direction                mCurrentPackDirection      = Direction.RIGHT;
    private boolean                  mDraftStarted;

    private boolean                  mStopDisconnectCheckThread = false;

    private LimitedDraftPacks        mLimitedInfo;

    private Random                   mRand                      = new Random();

    /**
     * Create a new server with the given UI
     * 
     * @param netDraftJServer_ui The UI which this server will display info in
     */
    NetDraftJServer(NetDraftJServer_ui netDraftJServer_ui) {
        mUi = netDraftJServer_ui;
        mDraftStarted = false;
    }

    /**
     * Deal out the packs to all the players. This can't be called if there are no
     * packs left. This doesn't send the TCP messages to any connected players.
     */
    private void dealPacks() {

        synchronized (mPlayers) {
            // Mark another pack as dealt
            mNumPacks--;

            // Logging
            mUi.appendText("Deal out the next pack, " + mNumPacks + " pack(s) left");

            // Reverse the pack direction
            if (mCurrentPackDirection == Direction.LEFT) {
                mCurrentPackDirection = Direction.RIGHT;
            }
            else {
                mCurrentPackDirection = Direction.LEFT;
            }

            if (mCubeList.size() > 0) {
                // Deal out the cube packs
                for (Player player : mPlayers) {
                    player.clearPack();
                    for (int packIdx = 0; packIdx < mPackSize; packIdx++) {
                        player.addCardToPack(mCubeList.get(mCubeIdx++));
                    }
                }
            }
            else {

                int         packIdx = mLimitedInfo.limitedPacks.size() - mNumPacks - 1;
                LimitedPack li      = mLimitedInfo.limitedPacks.get(packIdx);

                try {
                    NetDraftJDatabase                 database = new NetDraftJDatabase();
                    HashMap<Character, List<Integer>> cardPool = new HashMap<Character, List<Integer>>();
                    for (Character rarity : li.distribution.keySet()) {
                        cardPool.put(rarity, database.getListBySetAndRarity(li.setCode, rarity, li.maxCardNumber));
                    }
                    database.closeConnection();

                    // Dealing limited packs. Gotta create these first
                    for (Player player : mPlayers) {
                        player.clearPack();
                        char rarityOrder[] = { 'C', 'U', 'R' };
                        for (char rarity : rarityOrder) {
                            if (cardPool.containsKey(rarity)) {

                                // If this is the rare slot and mythics exist
                                if ('R' == rarity && cardPool.containsKey('M')) {
                                    // There's a one-in-eight chance a mythic replaces a rare
                                    if (mRand.nextInt() % 8 == 0) {
                                        rarity = 'M';
                                    }
                                }
                                Collections.shuffle(cardPool.get(rarity));
                                for (int i = 0; i < li.distribution.get(rarity); i++) {
                                    int mid = cardPool.get(rarity).get(i);
                                    if (!player.getPack().contains(mid)) {
                                        player.addCardToPack(mid);
                                    }
                                    else {
                                        i--;
                                    }
                                }
                            }
                        }
                    }
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Send the dealt packs to all the connected players, asking for which card
     * they're picking
     */
    private void sendPlayersPacks() {
        synchronized (mPlayers) {
            for (Player player : mPlayers) {
                player.sendPickRequest(false);
            }
        }
    }

    /**
     * Callback called whenever any TCP message is received from a player
     *
     * @param connection The connection which received the message
     * @param object     The de-serialized object from the TCP message
     */
    @Override
    public void received(Connection connection, Object object) {
        synchronized (mPlayers) {

            if (object instanceof ConnectionRequest) {
                ConnectionRequest request = (ConnectionRequest) object;
                if (!mDraftStarted) {
                    // Draft hasn't started, so let the player join

                    // First make sure this player's name isn't in use already
                    for (Player p : mPlayers) {
                        if (p.getName().equalsIgnoreCase(request.getName())) {
                            // Logging
                            mUi.appendText(
                                    "Rejected (name already in use)" + request.getUuid() + ": " + request.getName());

                            // Let the player know they've been rejected
                            connection.sendTCP(new ConnectionResponse(false, "Name already in use"));
                            return;
                        }
                    }

                    if (request.getBuildTimestamp().equals(NetDraftJClient_ui.getClassBuildTime(this))) {

                        // Logging
                        mUi.appendText(request.getUuid() + ": " + request.getName() + " joined the draft");

                        // Let the player know they've joined
                        connection.sendTCP(new ConnectionResponse(true, null));

                        // Save the player's information
                        Player player = new Player(connection, request);
                        mPlayers.add(player);
                    }
                    else {
                        // Logging
                        mUi.appendText("Rejected (build mismatch)" + request.getUuid() + ": " + request.getName());

                        // Let the player know they've been rejected
                        connection.sendTCP(new ConnectionResponse(false, "Build Mismatch"));
                    }

                }
                else {
                    boolean reconnected = false;
                    for (Player player : mPlayers) {
                        if (player.getUuid() == request.getUuid() && player.isDetached()) {
                            reconnected = true;

                            player.initialize(connection, request);

                            // Let the player know they've joined
                            connection.sendTCP(new ConnectionResponse(true, null));

                            // Send the reconnected player their seating info
                            player.sendSeatingOrder(mPlayers);

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
                        mUi.appendText("Rejected (draft started)" + request.getUuid() + ": " + request.getName());

                        // Let the player know they've been rejected
                        connection.sendTCP(new ConnectionResponse(false, "Draft Already Started"));
                    }
                }
            }
            else if (object instanceof PickResponse) {
                PickResponse response  = (PickResponse) object;
                boolean      allPicked = true;

                // Find the player reporting a pick
                for (Player player : mPlayers) {
                    if (response.getUuid() == player.getUuid()) {
                        // Mark that card as picked
                        player.pickCard(response.getPick());
                        // Logging
                        mUi.appendText(player.getName() + " picked a card");
                    }
                    // Check if any player hasn't made a pick yet
                    if (!player.getPicked()) {
                        allPicked = false;
                    }
                }

                // If all players have made their picks
                if (allPicked) {
                    // If the packs are empty and there are more to be drafted
                    if (mPlayers.get(0).getPack().size() == 0) {
                        if (mNumPacks > 0) {

                            // deal new ones
                            dealPacks();
                            sendPlayersPacks();
                        }
                        else {
                            // Logging
                            mUi.appendText("Draft over");

                            for (Player player : mPlayers) {
                                player.sendDraftOverNotification();
                            }
                        }
                    }
                    else {
                        // Logging
                        mUi.appendText("Pass the packs, " + mPlayers.get(0).getPack().size() + " cards left");

                        // If everyone picked and there are still packs in cards,
                        // shift them
                        shiftPacks(mCurrentPackDirection);
                        // Then send them to the players for picking
                        sendPlayersPacks();
                    }
                }
            }
        }
    }

    /**
     * Shift the packs either LEFT or RIGHT after all picks have been made
     * 
     * @param dir The direction to shift the packs
     */
    private void shiftPacks(Direction dir) {
        synchronized (mPlayers) {

            switch (dir) {
            case LEFT: {
                ArrayList<Integer> packZero = mPlayers.get(0).getPack();
                for (int i = 0; i < mPlayers.size() - 1; i++) {
                    mPlayers.get(i).setPack(mPlayers.get(i + 1).getPack());
                }
                mPlayers.get(mPlayers.size() - 1).setPack(packZero);
                break;
            }
            case RIGHT: {
                ArrayList<Integer> lastPack = mPlayers.get(mPlayers.size() - 1).getPack();
                for (int i = mPlayers.size() - 1; i > 0; i--) {
                    mPlayers.get(i).setPack(mPlayers.get(i - 1).getPack());
                }
                mPlayers.get(0).setPack(lastPack);

                break;
            }
            }
        }
    }

    /**
     * Start a new Thread which the Server will run in. Ask the user what cube to
     * load first, and load it. Loading a cube can take a while. Start another
     * Thread to periodically check for client disconnects.
     * 
     * @param ipAddress The String representation of the dotted decimal IP address
     */
    void startServer(String ipAddress) {

        new Thread(() -> {
            mUi.setStartButtonEnabled(false);
            File cubeFile = mUi.pickCubeFile();
            if (null == cubeFile) {
                mUi.appendText("User didn't pick a file");
                return;
            }
            else if (cubeFile.getName().toLowerCase().endsWith("json")) {
                // Load the JSON for packs
                mUi.appendText("Loading " + cubeFile.getName());
                if (!loadLimitedPackList(cubeFile)) {
                    mUi.appendText("Failed to open limited pack file");
                    return;
                }
                mUi.appendText(cubeFile.getName() + " loaded");
            }
            else if (cubeFile.getName().toLowerCase().endsWith("txt")) {
                // Load the plain text for cube
                mUi.appendText("Loading " + cubeFile.getName());
                if (!loadCubeList(cubeFile)) {
                    mUi.appendText("Failed to open cube file");
                    return;
                }
                mUi.appendText(cubeFile.getName() + " loaded");
            }
            else {
                mUi.appendText("User didn't pick a .txt or .json file");
                return;
            }

            // Start the server!
            mServer = new Server();
            mServer.start();
            try {
                mServer.bind(PORT);
            }
            catch (IOException e) {
                mUi.appendText(e.getMessage());
                e.printStackTrace();
                return;
            }
            MessageUtils.registerAll(mServer.getKryo());
            mServer.addListener(NetDraftJServer.this);

            mUi.appendText("Server started on " + ipAddress + ":" + PORT);
            mUi.setStartButtonEnabled(true);
            mUi.setHostMenuItemEnabled(false);
            mDraftStarted = false;

            new Thread(() -> {
                while (true) {
                    synchronized (mPlayers) {
                        for (int i = 0; i < mPlayers.size(); i++) {
                            if (mPlayers.get(i).isDisconnected()) {
                                if (!mDraftStarted) {
                                    // If they disconnect before the draft starts, drop em
                                    mUi.appendText(mPlayers.get(i).getName() + " left the draft");
                                    mPlayers.remove(i);
                                    i--;
                                }
                                else if (!mPlayers.get(i).isDetached()) {
                                    // If they disconnect after the draft starts, wait for a reconnect
                                    mPlayers.get(i).setDetached();
                                    mUi.appendText(mPlayers.get(i).getName() + " disconnected. Waiting for reconnect");
                                }
                            }
                        }
                    }
                    try {
                        Thread.sleep(1000 * 8);
                    }
                    catch (InterruptedException e) {
                        return;
                    }
                    if (mStopDisconnectCheckThread) {
                        return;
                    }
                }
            }).run();
        }).start();
    }

    /**
     * 
     * @param limitedPackFile
     * @return
     */
    private boolean loadLimitedPackList(File limitedPackFile) {
        try {
            mLimitedInfo = (new Gson()).fromJson(new FileReader(limitedPackFile), LimitedDraftPacks.class);
            mNumPacks = mLimitedInfo.limitedPacks.size();
            mCubeList.clear();
        }
        catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Load a cube from a file, which is a plaintext list of cards
     * 
     * @param cubeFile The file to read and load cards from
     * @return true if the cube was loaded, false if the cube couldn't be loaded
     */
    private boolean loadCubeList(File cubeFile) {
        try {
            // Open the database and cube file
            NetDraftJDatabase database    = new NetDraftJDatabase();
            BufferedReader    fileReader  = new BufferedReader(new FileReader(cubeFile));
            String            line;

            // Read the file and fill in extra info from the database, save the
            // multiverseID
            int               cardsLoaded = 0;
            while ((line = fileReader.readLine()) != null) {
                MtgCard card = new MtgCard(line.split(" // ")[0]);
                if (database.loadCard(card)) {
                    mCubeList.add(card.getMultiverseId());
                    cardsLoaded++;
                    if (cardsLoaded % 50 == 0) {
                        mUi.appendText("Loaded " + cardsLoaded + " cards");
                    }
                }
                else {
                    mUi.appendText("Failed to load \"" + line + "\"");
                }
            }

            // Clean up
            fileReader.close();
            database.closeConnection();

            if (mCubeList.isEmpty()) {
                return false;
            }

            // Shuffle the cube
            Collections.shuffle(mCubeList);

            // Return the shuffled multiverseIDs
            return true;
        }
        catch (IOException | SQLException e) {
            // Something went wrong, print out what and return null
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get this computer's public IP address using the whatIsMyIpAddress.com service
     * 
     * @return This computer's public IP address, i.e. the router's IP address, or
     *         null if there's an Exception
     */
    public static String getPublicIp() {
        try {
            URL            url_name = new URL("http://bot.whatismyipaddress.com");
            BufferedReader sc       = new BufferedReader(new InputStreamReader(url_name.openStream()));
            return sc.readLine().trim();
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Stop the server, halt the disconnection checking thread, and re-enable the
     * menu "Host" button
     */
    void stopServer() {
        if (null != mServer) {
            mServer.stop();
        }
        mServer = null;
        mUi.setHostMenuItemEnabled(true);
        mStopDisconnectCheckThread = true;
    }

    /**
     * Start the draft. Ensure the UUIDs are unique, shuffle the seating, send the
     * seating orders to all connected players, figure out the pack size and number
     * of packs, deal out the first packs, and send the packs to all players
     * 
     * @return true if the draft started, false otherwise
     */
    boolean clickStartGameButton() {

        synchronized (mPlayers) {

            // Make sure UUIDs are actually unique
            for (Player one : mPlayers) {
                for (Player two : mPlayers) {
                    if (one != two) {
                        if (one.getUuid() == two.getUuid()) {
                            // Logging
                            mUi.appendText("UUIDs aren't unique!");
                            return false;
                        }
                    }
                }
            }

            // Mark the draft as started
            mDraftStarted = true;

            // Randomize seating
            Collections.shuffle(mPlayers);

            // Tell everyone the seating order
            for (Player player : mPlayers) {
                player.sendSeatingOrder(mPlayers);
            }

            // Logging
            mUi.appendText("Send the seating orders");

            // Figure out pack size and number of packs
            if (mCubeList.size() > 0) {
                mPackSize = (mPlayers.size() * 2) - 1;
                mNumPacks = (int) Math.ceil(45 / (float) mPackSize);
                if (mPackSize * mNumPacks > mCubeList.size()) {
                    int cardsPerPlayer = (int) Math.floor(mCubeList.size() / (double) mPlayers.size());
                    mPackSize = (int) Math.floor(cardsPerPlayer / 3.0f);
                }
            }
            else {
                mNumPacks = mLimitedInfo.limitedPacks.size();
                mPackSize = 0; // Doesn't matter
            }

            mCubeIdx = 0;

            // Deal out the packs
            dealPacks();

            // Send the packs to the drafters
            sendPlayersPacks();

            return true;
        }
    }
}
