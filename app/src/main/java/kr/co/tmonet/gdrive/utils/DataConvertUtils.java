package kr.co.tmonet.gdrive.utils;

import java.nio.charset.StandardCharsets;

import kr.co.tmonet.gdrive.network.APIConstants.Command;

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

    public static boolean veryfyCheckSum(String command) {

        String cmd = command.substring(0, command.indexOf(Command.SIGN_CHECKSUM_START));
        String checkSum = (command.substring(command.indexOf(Command.SIGN_CHECKSUM_START) + 1, command.indexOf(Command.SIGN_CHECKSUM_END))).toUpperCase();

        return calcCheckSum(cmd).equals(checkSum);
    }

    public static String calcCheckSum(String cmd) {
        String hexString = convertAsciiToHex(cmd);
        byte[] bytes = hexStringToByteArray(hexString);

        int checkSum = 0;
        for (byte b : bytes) {
            checkSum += (0xFF & b);
        }

        return Integer.toHexString(checkSum & 0xFF).toUpperCase();
    }

    public static String convertAsciiToHex(String asciiStr) {
        char[] chars = asciiStr.toCharArray();
        StringBuilder hex = new StringBuilder();

        for (char ch : chars) {
            hex.append(Integer.toHexString((int) ch));
        }

        return hex.toString();
    }

    private static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) +
                    Character.digit(hexString.charAt(i + 1), 16));

        }
        return data;
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
