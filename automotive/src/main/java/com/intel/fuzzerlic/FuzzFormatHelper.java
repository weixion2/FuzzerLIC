package com.intel.fuzzerlic;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Random;

public class FuzzFormatHelper {

    public static final String VERSION = "V1";
    private static final String TAG = "FuzzFormatHelper";

    public FuzzFormatHelper() {
    }

    public static byte[] fuzzJsonByteArray(byte[] validByteArray) {
        Random random = new Random();
        int arrayLength = validByteArray.length;

        // Iterate over each byte in the array
        for (int i = 0; i < arrayLength; i++) {
            // Generate a random byte and assign it to the current position
            byte randomByte = (byte) random.nextInt(256);
            validByteArray[i] = randomByte;
        }
        return validByteArray;
    }

    public byte[] convertJsonByteArray(JSONObject json) {
        String requestBodyStr = json.toString();
        short length = (short) requestBodyStr.length();
        String str = VERSION + "11" + requestBodyStr;
        byte[] array = str.getBytes();
        array[2] = (byte) ((length >> 8) & 0xFF);
        array[3] = (byte) (length & 0xFF);
        return array;
    }

    public JSONObject createJSONByInput(int command) throws JSONException {
        JSONObject json = new JSONObject();
        switch (command) {
            case Fuzzer.COMMAND_LIST_IMAGES:
                json.put("operation_type", "list_images");
                break;
            case Fuzzer.COMMAND_SET_DNS: // set network
                json.put("operation_type", "set_network");
                JSONObject msgJson4 = new JSONObject();
                msgJson4.put("dns", "8.8.8.8");
                msgJson4.put("proxy", "http://child-prc.intel.com:913");
                String msgJson4Str = msgJson4.toString();
                json.put("msg", msgJson4Str);
                break;
            case Fuzzer.COMMAND_CANCEL_BUILD:
                json.put("operation_type", "cancel_build");
                break;
            case Fuzzer.COMMAND_START_AICORE: // start aicore container
                json.put("operation_type", "start_container");
                json.put("msg", "aicore");
                break;
            case Fuzzer.COMMAND_STOP_AICORE: // stop aicore container
                json.put("operation_type", "stop_container");
                json.put("msg", "aicore");
                break;
            case Fuzzer.COMMAND_START_DOCKERD:
                json.put("operation_type", "start_dockerd");
                break;
            case Fuzzer.COMMAND_DOCKERD_STATUS:
                json.put("operation_type", "dockerd_status");
                break;
            case Fuzzer.COMMAND_GET_GAMECORE_STATUS:
                json.put("operation_type", "is_gamecore_ready");
                break;
            case Fuzzer.COMMAND_GET_AICORE_STATUS:
                json.put("operation_type", "is_container_ready");
                json.put("msg", "aicore");
                break;
            case Fuzzer.COMMAND_CHECK_GAMECORE_UPGRADE: // check if gamecore needs upgrade
                json.put("operation_type", "get_container");
                json.put("msg", "gamecore");
                break;
            case Fuzzer.COMMAND_CHECK_AICORE_UPGRADE: // check if aicore needs upgrade
                json.put("operation_type", "get_container");
                json.put("msg", "aicore");
                break;
            case Fuzzer.COMMAND_START_GAMECORE: // start gamecore
                json.put("operation_type", "start_container");
                json.put("msg", "gamecore");
                break;
            case Fuzzer.COMMAND_STOP_GAMECORE: // stop gamecore
                json.put("operation_type", "stop_container");
                json.put("msg", "gamecore");
                break;
        }
        return json;
    }

    public String readVersionStr(BufferedInputStream inputBufferedStream) throws IOException {
        byte[] versionArray = new byte[2];
        //read next two bytes (which is the first two bytes returned by server)
        int byteCount = inputBufferedStream.read(versionArray, 0, 2);
        if (byteCount < 0) {
            Log.e(TAG, "readVersionStr end-of-file");
            return null;
        }
        return new String(versionArray);
    }

    public int readResponseLength(BufferedInputStream inputBufferedStream) throws IOException {
        byte[] lenArray = new byte[2];
        int lenCount = inputBufferedStream.read(lenArray, 0, 2);
        if (lenCount < 0) {
            Log.e(TAG, "readResponseLength end-of-file");
            return 0;
        }
        return (lenArray[0] & 0xFF) << 8 | (lenArray[1] & 0xFF);
    }

    public String readResponseBody(int lenVal, BufferedInputStream inputBufferedStream) throws IOException {
        byte[] responseArray = new byte[lenVal];
        int bodyCount = inputBufferedStream.read(responseArray, 0, lenVal);
        if (bodyCount < 0) {
            Log.e(TAG, "readResponseBody end-of-file");
            return null;
        }
        return new String(responseArray);
    }

    public static String toHexString(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            // Convert the byte to an int with masking to avoid sign extension issues
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                // Ensure the hex string has two characters, prepend a zero if needed
                hexString.append('0');
            }
            hexString.append(hex).append(" "); // Append a space for readability
        }
        return hexString.toString().toUpperCase(); // Convert to upper case for standard hex format
    }

}
