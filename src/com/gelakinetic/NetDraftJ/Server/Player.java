package com.gelakinetic.NetDraftJ.Server;

import java.util.ArrayList;

import com.esotericsoftware.kryonet.Connection;
import com.gelakinetic.NetDraftJ.Messages.ConnectionRequest;
import com.gelakinetic.NetDraftJ.Messages.DraftOverNotification;
import com.gelakinetic.NetDraftJ.Messages.PickRequest;
import com.gelakinetic.NetDraftJ.Messages.PreviousPicksInfo;
import com.gelakinetic.NetDraftJ.Messages.StartDraftInfo;

public class Player {
    private Connection mConnection;
    private String mName;
    private long mUuid;
    private ArrayList<Integer> mPack;
    private ArrayList<Integer> mPicks;
    private boolean mPicked;
    private boolean mIsDetached;

    /**
     * Create a Player object with a Connection and the ConnectionRequest from that player
     * @param connection The TCP connection the player connected on
     * @param request The request with the player's UUID and name
     */
    Player(Connection connection, ConnectionRequest request) {
        this.initialize(connection, request);
    }

    /**
     * Initialize a Player object with a Connection and the ConnectionRequest from that player. This may be
     * called for a reconnected player and it will not disrupt the packs or picks.
     * @param connection The TCP connection the player connected on
     * @param request The request with the player's UUID and name
     */
    void initialize(Connection connection, ConnectionRequest request) {
        this.mConnection = connection;
        this.mName = request.getName();
        this.mUuid = request.getUuid();
        if (mPack == null) {
            mPack = new ArrayList<>();
        }
        if (mPicks == null) {
            mPicks = new ArrayList<>();
            mPicked = false;
        }
        mIsDetached = false;
    }

    /**
     * Clear this player's current pack, called before dealing packs
     */
    void clearPack() {
        mPack.clear();
    }

    /**
     * Add a card to the current pack
     * @param multiverseId The multiverse ID of the card to add to the pack
     */
    void addCardToPack(Integer multiverseId) {
        mPack.add(multiverseId);
    }

    /**
     * @return The current pack, an ArrayList of multiverse IDs
     */
    ArrayList<Integer> getPack() {
        return mPack;
    }

    /**
     * Set the entire pack to the pack argument. This doesn't copy contents. Used when shifting packs.
     * @param lastPack The ArrayList to set the current pack to.
     */
    void setPack(ArrayList<Integer> lastPack) {
        mPack = lastPack;
    }

    /**
     * Send a TCP message to this players asking what card to pick out of the current pack
     * @param isReconnect true if this is a reconnection, false otherwise
     */
    void sendPickRequest(boolean isReconnect) {
        if (!isReconnect) {
            mPicked = false;
        }
        mConnection.sendTCP(new PickRequest(mPack, mPicked));
    }

    /**
     * Pick a card from the current pack, moving it from the pack to the picks
     * @param multiverseId The multiverse ID of the card to move from the pack to the picks
     */
    void pickCard(Integer multiverseId) {
        mPicked = true;
        mPack.remove(multiverseId);
        mPicks.add(multiverseId);
    }

    /**
     * Send the TCP message to the player telling them the draft is finished
     */
    void sendDraftOverNotification() {
        mConnection.sendTCP(new DraftOverNotification());
    }

    /**
     * @return This player's name
     */
    public String getName() {
        return mName;
    }

    /**
     * @return true if this player already picked a card, false otherwise
     */
    boolean getPicked() {
        return mPicked;
    }

    /**
     * @return this player's UUID
     */
    long getUuid() {
        return mUuid;
    }

    /**
     * @return true if this player is disconnected, false if it is still connected
     */
    boolean isDisconnected() {
        return !mConnection.isConnected();
    }

    /**
     * Set this player as detached if it was connected and it disconnected
     */
    void setDetached() {
        mIsDetached = true;
    }

    /**
     * @return true if this player is marked as detached, false if not
     */
    boolean isDetached() {
        return mIsDetached;
    }

    /**
     * Send the seating order to this player
     * @param players An ArrayList of Players which is ordered in the seating order
     */
    void sendSeatingOrder(ArrayList<Player> players) {
        StartDraftInfo sdi = new StartDraftInfo();
        sdi.setSeatingOrder(players);
        mConnection.sendTCP(sdi);
    }

    /**
     * Send all the previous information about picked cards to a reconnected player
     */
    void sendPreviousPicks() {
        mConnection.sendTCP(new PreviousPicksInfo(mPicks));
    }

}
