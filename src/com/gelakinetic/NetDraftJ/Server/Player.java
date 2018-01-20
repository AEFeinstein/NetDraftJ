package com.gelakinetic.NetDraftJ.Server;

import java.util.ArrayList;

import com.esotericsoftware.kryonet.Connection;
import com.gelakinetic.NetDraftJ.messages.ConnectionRequest;
import com.gelakinetic.NetDraftJ.messages.DraftOverNotification;
import com.gelakinetic.NetDraftJ.messages.PickRequest;

public class Player {
    private Connection mConnection;
    private String mName;
    private long mUuid;
    private ArrayList<Integer> mPack;
    private ArrayList<Integer> mPicks;
    private boolean mPicked;

    public Player(Connection connection, ConnectionRequest request, int seatingOrder) {
        this.mConnection = connection;
        this.mName = request.getName();
        this.mUuid = request.getUuid();
        mPack = new ArrayList<>();
        mPicks = new ArrayList<>();
        mPicked = false;
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

    public void sendPickRequest() {
        mPicked = false;
        mConnection.sendTCP(new PickRequest(mPack));
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
}
