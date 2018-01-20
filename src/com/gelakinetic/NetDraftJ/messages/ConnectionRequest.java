package com.gelakinetic.NetDraftJ.messages;

import java.util.Random;

public class ConnectionRequest {

    private String mName;
    private long mUuid;

    public ConnectionRequest() {
        mName = "";
    }

    public ConnectionRequest(String name) {
        this.mName = name;
        mUuid = new Random(System.currentTimeMillis()).nextLong();
    }

    public long getUuid() {
        return mUuid;
    }

    public String getName() {
        return mName;
    }
}
