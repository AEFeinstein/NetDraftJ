package com.gelakinetic.NetDraftJ.Messages;

import java.util.ArrayList;

public class PickRequest {
    private int mPack[];
    private boolean mPicked;

    public PickRequest() {

    }

    public PickRequest(ArrayList<Integer> packToSend, boolean picked) {
        mPack = new int[packToSend.size()];
        for (int i = 0; i < packToSend.size(); i++) {
            mPack[i] = packToSend.get(i);
        }
        mPicked = picked;
    }

    public int[] getPack() {
        return mPack;
    }

    public boolean getPicked() {
        return mPicked;
    }
}
