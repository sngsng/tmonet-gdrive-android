package kr.co.tmonet.gdrive.utils;

import java.nio.charset.StandardCharsets;

/**
 * Created by Jessehj on 19/06/2017.
 */

public class DataConvertUtils {

    private static final String LOG_TAG = DataConvertUtils.class.getSimpleName();

    public static byte[] convertAsciiToBytes(String str) {
        byte[] data = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            data = str.getBytes(StandardCharsets.US_ASCII);
        }
        return data;
    }

    public static String convertBytesToAscii(byte[] data) {

        return new String(data);
    }


    public static String convertAsciiToHex(String asciiStr) {
        char[] chars = asciiStr.toCharArray();
        StringBuilder hex = new StringBuilder();

        for (char ch : chars) {
            hex.append("0x" + Integer.toHexString((int) ch));
        }

        return hex.toString();
    }

    public static String convertHexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }
}
