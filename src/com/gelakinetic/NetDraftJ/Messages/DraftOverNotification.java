package com.gelakinetic.NetDraftJ.Messages;

import java.util.ArrayList;

public class DraftOverNotification {
    private int mAllPicks[];

    @SuppressWarnings("unused")
    public DraftOverNotification() {

    }

    public DraftOverNotification(ArrayList<Integer> picks) {
        mAllPicks = new int[picks.size()];
        for (int i = 0; i < picks.size(); i++) {
            mAllPicks[i] = picks.get(i);
        }
    }
}
