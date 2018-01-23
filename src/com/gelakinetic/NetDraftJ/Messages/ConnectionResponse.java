package com.gelakinetic.NetDraftJ.Messages;

public class ConnectionResponse {

    private boolean mConnectionStatus;

    public ConnectionResponse() {
        mConnectionStatus = true;
    }

    public ConnectionResponse(boolean status) {
        mConnectionStatus = status;
    }

    public boolean getConnectionStatus() {
        return mConnectionStatus;
    }
}
