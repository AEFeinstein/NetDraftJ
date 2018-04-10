package com.gelakinetic.NetDraftJ.Messages;

import com.esotericsoftware.kryo.Kryo;

public class MessageUtils {

    public static void registerAll(Kryo kryo) {
        kryo.register(ConnectionRequest.class);
        kryo.register(ConnectionResponse.class);
        kryo.register(StartDraftInfo.class);
        kryo.register(PickRequest.class);
        kryo.register(PickResponse.class);
        kryo.register(DraftOverNotification.class);
        kryo.register(PreviousPicksInfo.class);

        kryo.register(String[].class);
        kryo.register(int[].class);
    }

}
