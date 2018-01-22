package com.gelakinetic.NetDraftJ.Messages;

import java.util.ArrayList;

public class PickRequest {
    private int mPack[];

    public PickRequest() {

    }

    public PickRequest(ArrayList<Integer> packToSend) {
        mPack = new int[packToSend.size()];
        for (int i = 0; i < packToSend.size(); i++) {
            mPack[i] = packToSend.get(i);
        }
    }

    public int[] getPack() {
        return mPack;
    }
}
