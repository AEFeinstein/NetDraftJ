package com.gelakinetic.NetDraftJ.Messages;

import java.util.ArrayList;

import com.gelakinetic.NetDraftJ.Server.Player;

public class StartDraftInfo {

    private String mPlayers[];

    public StartDraftInfo() {

    }

    public void setSeatingOrder(ArrayList<Player> players2) {
        mPlayers = new String[players2.size()];
        for (int i = 0; i < players2.size(); i++) {
            mPlayers[i] = players2.get(i).getName();
        }
    }

    public String[] getSeatingOrder() {
        return mPlayers;
    }
}
