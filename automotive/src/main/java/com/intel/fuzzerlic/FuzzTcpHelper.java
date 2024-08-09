package com.intel.fuzzerlic;

import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FuzzTcpHelper {

    private static final String TAG = "FuzzTcpHelper";
    Context context;
    private static final int TIMEOUT = 15000;
    private static final int LONG_TIMEOUT = 2147483646;
    FuzzFormatHelper formatHelper = new FuzzFormatHelper();


    public byte[] createByteArrayByCommand(int command) {
        byte[] msgByteArray;
        {
            try {
                //msgByteArray = formatHelper.convertJsonByteArray(formatHelper.createJSONByInput(command));
                msgByteArray = FuzzFormatHelper.fuzzJsonByteArray(formatHelper.convertJsonByteArray(formatHelper.createJSONByInput(command)));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return msgByteArray;
    }

    public void performTCP(int command) {
        Thread newThread = new Thread(() -> {
            try {
                connectSocket(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        newThread.start();
    }

    public boolean connectSocket(int command) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(TIMEOUT);
        String port = "12555";

        byte[] msgToSend = createByteArrayByCommand(command);

        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), Integer.parseInt(port)), TIMEOUT);

        try {
            if (interactServer(socket, msgToSend) == 1) {
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception occurs: " + e);
        }

        socket.close();
        Log.i(TAG, "TCP Connection Ends");
        return true; //status_code is 0, interaction succeed
    }

    public int interactServer(Socket socket, byte[] msgToSend) throws Exception {
        OutputStream outputStream = null;
        InputStream inputStream = null;
        BufferedInputStream inputBufferedStream = null;
        try {
            //get output stream that our client writes to the server
            outputStream = socket.getOutputStream();
            //get input stream that our client reads from the server
            inputStream = socket.getInputStream();
            inputBufferedStream = new BufferedInputStream(inputStream);

            //write the message to socket, sending to server
            outputStream.write(msgToSend);

            Log.i(TAG, "[Fuzzing] " + FuzzFormatHelper.toHexString(msgToSend));

            //read response message from socket, sent by server

            //read version response
            String versionStr = formatHelper.readVersionStr(inputBufferedStream);
            Log.i(TAG, "    Response Version: " + versionStr);
            //if version does not match the version client sent, log error and quit
            if (null != versionStr && !versionStr.equals(FuzzFormatHelper.VERSION)) {
                Log.e(TAG, "    Response version mismatch");
                throw new Exception("Response version number does not match the version number of client");
            }

            //read second two bytes, length of request body
            int lenVal = formatHelper.readResponseLength(inputBufferedStream);
            Log.i(TAG, "    Response Body Length: " + lenVal);

            //read response body from server, including status_code and data
            //e.g. {"status_code":0,"data":"Start dockerd successfully"}
            String bodyStr = formatHelper.readResponseBody(lenVal, inputBufferedStream);
            Log.i(TAG, "    Response Body: " + bodyStr);

            //If status code is 1, re-try one loop of interaction, 200ms delay
            String[] bodyStrArray = bodyStr.split(",");
            Map<String, String> responseBodyMap = new HashMap<>();
            for (String s : bodyStrArray) {
                String[] ms = s.split(":");
                responseBodyMap.put(ms[0], ms[1]);
            }
            if (Objects.requireNonNull(responseBodyMap.get("{\"status_code\"")).equals("1")) {
                Thread.sleep(200);
                return 1;
            }

            //auto-quit for now
            Log.i(TAG, "-----------------------------------");
            Log.i(TAG, "Last revision made: 08/07/2023");
            return 0;
        } finally {
            //free up memory
            Objects.requireNonNull(outputStream).close();
            Objects.requireNonNull(inputStream).close();
            Objects.requireNonNull(inputBufferedStream).close();
        }
    }

}
