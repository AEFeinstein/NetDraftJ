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

    public Player(Connection connection, ConnectionRequest request) {
        this.initalize(connection, request);
    }

    public void initalize(Connection connection, ConnectionRequest request) {
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

    public void clearPack() {
        mPack.clear();
    }

    void addCardToPack(Integer multiverseId) {
        mPack.add(multiverseId);
    }

    public ArrayList<Integer> getPack() {
        return mPack;
    }

    public void setPack(ArrayList<Integer> lastPack) {
        mPack = lastPack;
    }

    public void sendPickRequest(boolean isReconnect) {
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

    public void sendDraftOverNotification() {
        mConnection.sendTCP(new DraftOverNotification(mPicks));
    }

    public String getName() {
        return mName;
    }

    public boolean getPicked() {
        return mPicked;
    }

    public long getUuid() {
        return mUuid;
    }

    public boolean isConnected() {
        return mConnection.isConnected();
    }

    public void setDetached(boolean state) {
        isDetached = state;
    }

    public boolean isDetached() {
        return isDetached;
    }

    public void sendSeatingOrder(ArrayList<Player> players) {
        StartDraftInfo sdi = new StartDraftInfo();
        sdi.setSeatingOrder(players);
        mConnection.sendTCP(sdi);
    }

    public void sendPreviousPicks() {
        mConnection.sendTCP(new PreviousPicksInfo(mPicks));
    }

    public void setConnection(Connection connection) {
        mConnection = connection;
    }

    public void sendPing() {
        mConnection.updateReturnTripTime();
    }
}
