package kr.co.tmonet.gdrive.utils;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class ModelUtils {

    public static String getQueryAppendedUrlFromJson(String url, JSONObject params) {

        Uri.Builder builder = Uri.parse(url).buildUpon();
        if (params != null) {
            try {
                for (Iterator iterator = params.keys(); iterator.hasNext(); ) {
                    String key = (String) iterator.next();
                    Object value = params.get(key);
                    builder.appendQueryParameter(key, value.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return builder.build().toString();
    }

    public static String getExpectedTimeStringFromSeconds(int seconds) {

        int totalExpectedMinute = seconds / 60;

        if (totalExpectedMinute < 60) {
            return totalExpectedMinute + "분";

        } else {
            int hour = totalExpectedMinute / 60;
            int min = totalExpectedMinute % 60;

            if (min > 0) {
                return hour + "시간" + min + "분";
            } else {
                return hour + "시간";
            }
        }
    }
}
