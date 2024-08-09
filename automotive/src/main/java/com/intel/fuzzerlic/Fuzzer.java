package com.intel.fuzzerlic;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Random;

public class Fuzzer extends AppCompatActivity {

    public static final String version = "V1";
    public static final String TAG = "Fuzzer";
    Activity activity;
    FuzzTcpHelper tcpHelper;

    public static final int COMMAND_LIST_IMAGES = 1;
    public static final int COMMAND_SET_DNS = 2;
    public static final int COMMAND_CANCEL_BUILD = 3;
    public static final int COMMAND_START_DOCKERD = 4;
    public static final int COMMAND_DOCKERD_STATUS = 5;
    public static final int COMMAND_GET_GAMECORE_STATUS = 6;
    public static final int COMMAND_GET_AICORE_STATUS = 7;
    public static final int COMMAND_CHECK_GAMECORE_UPGRADE = 8;
    public static final int COMMAND_CHECK_AICORE_UPGRADE = 9;
    public static final int COMMAND_START_GAMECORE = 10;
    public static final int COMMAND_START_AICORE = 11;
    public static final int COMMAND_STOP_GAMECORE = 12;
    public static final int COMMAND_STOP_AICORE = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        tcpHelper = new FuzzTcpHelper();
        try {
            fuzzDockerManager(activity);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void fuzzDockerManager(Context context) throws InterruptedException {
        ArrayList<Integer> randomCommandList = randomCommandList();
        for (int i = 0; i < randomCommandList.size(); i++) {
            tcpHelper.performTCP(i);
        }
    }

    private ArrayList<Integer> randomCommandList() {
        ArrayList<Integer> randomCmdList = new ArrayList<Integer>();
        int randomTestTimes = getRandomNumberFromRange(20, 70); // test 20 to 70 rounds
        for (int i = 0; i < randomTestTimes; i ++) {
            int randomCommands = getRandomNumberFromRange(1, 13);
            randomCmdList.add(randomCommands);
        }
        return randomCmdList;
    }

    private int getRandomNumberFromRange(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }



}
