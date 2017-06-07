package kr.co.tmonet.gdrive;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class SettingManager {

    private static final String LOG_TAG = SettingManager.class.getSimpleName();
    private static SettingManager sSettingManager = new SettingManager();

    public static SettingManager getInstance() {
        return sSettingManager;
    }

    private Activity mCurrentActivity;
    private Double mCurrentLatitude = APIConstants.GeoCoding.DEF_LAT;
    private Double mCurrentLongitude = APIConstants.GeoCoding.DEF_LNG;

    public enum PermissionType {
        Init,
        Location
    }

    public String[] getRequiredPermissions(Context context, PermissionType type) {
        ArrayList<String> requiredPermissions = new ArrayList<>();
        switch (type) {
            case Init:
                if (!AppUtils.isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
                }
                if (!AppUtils.isPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                }
                break;
            case Location:
                if (!AppUtils.isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
                }
                if (!AppUtils.isPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                }
                break;
            default:
                break;

        }

        Object[] objArray = requiredPermissions.toArray();
        return Arrays.copyOf(objArray, objArray.length, String[].class);
    }

    public boolean isGPSOn(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.i(LOG_TAG, "GPS ON");
            return true;
        } else {
            Log.i(LOG_TAG, "GPS OFF");
            return false;
        }
    }

    public boolean isLocationPermissionGranted(Context context) {
        return AppUtils.isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
                AppUtils.isPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        mCurrentActivity = currentActivity;
    }

    public Double getCurrentLatitude() {
        return mCurrentLatitude;
    }

    public void setCurrentLatitude(Double currentLatitude) {
        mCurrentLatitude = currentLatitude;
    }
}
