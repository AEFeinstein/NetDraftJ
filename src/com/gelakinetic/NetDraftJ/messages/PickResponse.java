package com.gelakinetic.NetDraftJ.messages;

public class PickResponse {
    private long mUuid;
    private int mPick;

    public PickResponse() {

    }

    public PickResponse(int multiverseId, long uuid) {
        mUuid = uuid;
        mPick = multiverseId;
    }

    public long getUuid() {
        return mUuid;
    }

    public int getPick() {
        return mPick;
    }
}
