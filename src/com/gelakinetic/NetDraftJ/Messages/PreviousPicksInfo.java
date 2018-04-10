package com.gelakinetic.NetDraftJ.Messages;

import java.util.ArrayList;

public class PreviousPicksInfo {

    private int[] mPicks;

    public PreviousPicksInfo() {
        mPicks = new int[0];
    }

    public PreviousPicksInfo(ArrayList<Integer> previousPicks) {
        mPicks = new int[previousPicks.size()];
        for (int i = 0; i < previousPicks.size(); i++) {
            mPicks[i] = previousPicks.get(i);
        }
    }

    public int[] getPicks() {
        return mPicks;
    }

}
