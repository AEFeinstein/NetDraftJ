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
    private boolean isDetached;

    Player(Connection connection, ConnectionRequest request) {
        this.initialize(connection, request);
    }

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
        isDetached = false;
    }

    void clearPack() {
        mPack.clear();
    }

    void addCardToPack(Integer multiverseId) {
        mPack.add(multiverseId);
    }

    ArrayList<Integer> getPack() {
        return mPack;
    }

    void setPack(ArrayList<Integer> lastPack) {
        mPack = lastPack;
    }

    void sendPickRequest(boolean isReconnect) {
        if (!isReconnect) {
            mPicked = false;
        }
        mConnection.sendTCP(new PickRequest(mPack, mPicked));
    }

    void pickCard(Integer multiverseId) {
        mPicked = true;
        mPack.remove(multiverseId);
        mPicks.add(multiverseId);
    }

    void sendDraftOverNotification() {
        mConnection.sendTCP(new DraftOverNotification(mPicks));
    }

    public String getName() {
        return mName;
    }

    boolean getPicked() {
        return mPicked;
    }

    long getUuid() {
        return mUuid;
    }

    boolean isDisconnected() {
        return !mConnection.isConnected();
    }

    void setDetached() {
        isDetached = true;
    }

    boolean isDetached() {
        return isDetached;
    }

    void sendSeatingOrder(ArrayList<Player> players) {
        StartDraftInfo sdi = new StartDraftInfo();
        sdi.setSeatingOrder(players);
        mConnection.sendTCP(sdi);
    }

    void sendPreviousPicks() {
        mConnection.sendTCP(new PreviousPicksInfo(mPicks));
    }

}
