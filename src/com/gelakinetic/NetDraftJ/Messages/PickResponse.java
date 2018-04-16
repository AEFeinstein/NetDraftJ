package com.gelakinetic.NetDraftJ.Messages;

public class PickResponse {
    private long mUuid;
    private int mPick;

    @SuppressWarnings("unused")
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
