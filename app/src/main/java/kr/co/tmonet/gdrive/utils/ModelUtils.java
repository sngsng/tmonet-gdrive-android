package kr.co.tmonet.gdrive.utils;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

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

    public static String getExpectedDistanceInKmFromMeter(String totalDistance) {
        double totalDistanceDoubleInKm = Double.parseDouble(totalDistance) / 1000;

        return String.format("%.2f", totalDistanceDoubleInKm);
    }

    public static String getExpectedTimeStringFromSeconds(String totalTime) {
        int seconds = Integer.parseInt(totalTime);
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

    public static String getRemainServiceTime(Date endAt) {
        Date nowDate = new Date();

        long remain = endAt.getTime() - nowDate.getTime();

        if (remain > 0) {
            Date remainDate = new Date(remain);

            SimpleDateFormat remainFormat = new SimpleDateFormat("hhh:mm", Locale.KOREA);
            return remainFormat.format(remainDate);

        } else {
            return null;
        }
    }

    public static String getDateFormat(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM-DD", Locale.KOREA);
        return formatter.format(date);
    }

    public static String getTimeFormat(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm", Locale.KOREA);
        return formatter.format(date);
    }

    public static String getRunnableDistance(double fuelEfficiency, double carBettery, double remainPercent) {

        double remainBetterySize = carBettery * remainPercent * (1 / 100);
        double runnableDistance = fuelEfficiency * remainBetterySize;

        return String.valueOf(runnableDistance);
    }
}
