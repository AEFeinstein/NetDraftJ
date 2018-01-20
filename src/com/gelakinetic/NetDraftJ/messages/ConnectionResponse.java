package com.gelakinetic.NetDraftJ.messages;

public class ConnectionResponse {

    private boolean mConnectionStatus;

    public ConnectionResponse() {
        mConnectionStatus = true;
    }

    public boolean getConnectionStatus() {
        return mConnectionStatus;
    }
}
