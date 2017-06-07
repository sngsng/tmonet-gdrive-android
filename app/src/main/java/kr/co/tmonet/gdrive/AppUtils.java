package kr.co.tmonet.gdrive;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class AppUtils {
    
    private static final String LOG_TAG = AppUtils.class.getSimpleName();

    public static final String URL_MARKET = "market://details?id=";
    public static final String URL_WEB_MARKET = "https://play.google.com/store/apps/details?id=";

    public static boolean isPermissionGranted(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.i(LOG_TAG, "Permission is denied: " + permission);
                return false;
            } else {
                Log.i(LOG_TAG, "Permission is granted: " + permission);
                return true;
            }
        } else {
            return true;
        }
    }
}
