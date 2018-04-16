package com.gelakinetic.NetDraftJ.Messages;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import com.gelakinetic.NetDraftJ.Client.NetDraftJClient_ui;

public class ConnectionRequest {

    private final String mName;
    private long mUuid;
    private long mBuildTimestamp;

    private static final String UUID_FILE = "uuid";

    @SuppressWarnings("unused")
    public ConnectionRequest() {
        mName = "";
    }

    public ConnectionRequest(String name, NetDraftJClient_ui mUi) {
        this.mName = name;
        this.mBuildTimestamp = NetDraftJClient_ui.getClassBuildTime(this).getTime();
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
                mUi.showErrorDialog(
                        "Cannot write UUID file. Please move NetDraftJ somewhere with write permissions and restart it.");
                e1.printStackTrace();
            }
        }
    }

    public ConnectionRequest(String name, long uuid) {
        this.mName = name;
        this.mUuid = uuid;
        this.mBuildTimestamp = NetDraftJClient_ui.getClassBuildTime(this).getTime();
    }

    public long getUuid() {
        return mUuid;
    }

    public String getName() {
        return mName;
    }

    public long getBuildTimestamp() {
        return mBuildTimestamp;
    }
}
