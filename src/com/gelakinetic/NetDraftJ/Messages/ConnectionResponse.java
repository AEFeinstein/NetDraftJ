package com.gelakinetic.NetDraftJ.Messages;

public class ConnectionResponse {

    private final boolean mConnectionStatus;
    private final String mMessage;

    @SuppressWarnings("unused")
    public ConnectionResponse() {
        mConnectionStatus = true;
        mMessage = "";
    }

    public ConnectionResponse(boolean status, String message) {
        mConnectionStatus = status;
        mMessage = message;
    }

    public boolean getConnectionStatus() {
        return mConnectionStatus;
    }

    public String getMessage() {
        return mMessage;
    }
}
