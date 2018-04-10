package com.gelakinetic.NetDraftJ.Messages;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class ConnectionRequest {

    private String mName;
    private long mUuid;

    private static final String UUID_FILE = "uuid";

    public ConnectionRequest() {
        mName = "";
    }

    public ConnectionRequest(String name) {
        this.mName = name;
        try {
            // Try reading the UUID
            BufferedReader br = new BufferedReader(new FileReader(UUID_FILE));
            mUuid = Long.parseLong(br.readLine());
            br.close();
        } catch (NumberFormatException | IOException e) {
            // If that fails, generate a new one and save it
            mUuid = new Random(System.currentTimeMillis()).nextLong();
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(UUID_FILE));
                bw.write(Long.toString(mUuid));
                bw.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public ConnectionRequest(String name, long uuid) {
        this.mName = name;
        this.mUuid = uuid;
    }

    public long getUuid() {
        return mUuid;
    }

    public String getName() {
        return mName;
    }
}
