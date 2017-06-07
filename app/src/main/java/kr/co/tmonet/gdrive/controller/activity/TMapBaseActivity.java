package kr.co.tmonet.gdrive.controller.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.skp.Tmap.TMapTapi;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.manager.SettingManager;
import kr.co.tmonet.gdrive.model.ChargeStation;
import kr.co.tmonet.gdrive.utils.DialogUtils;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class TMapBaseActivity extends BaseActivity {

    private static final String LOG_TAG = TMapBaseActivity.class.getSimpleName();
    public static final int REQ_LOCATION_PERMISSION = 0;
    public static final String T_MAP_API_KEY = "70a342d1-f27b-3dcc-8b75-840f56e68e5d";

    public void checkPermissions(Context context, SettingManager.PermissionType type, CheckPermissionListener listener) {
        if (Build.VERSION.SDK_INT >= M) {
            String[] requiredPermissions = SettingManager.getInstance().getRequiredPermissions(context, type);

            if (requiredPermissions.length > 0) {
                requestPermissions(requiredPermissions, 0);
            } else {
                listener.onReady();
            }
        } else {
            listener.onReady();
        }
    }

    public void linkToTMap(ChargeStation station) {
        TMapTapi tMapTapi = new TMapTapi(this);
        tMapTapi.setSKPMapAuthentication(T_MAP_API_KEY);

        if (!tMapTapi.invokeRoute(station.getName(), (float) station.getLongitude(), (float) station.getLatitude())) {
            showSnackbar(getString(R.string.error_no_cannot_found_t_map));
            Log.i(LOG_TAG, "can not found t map");
        }
    }

    // Check Location Permission && GPS function
    public void checkEnableUseLocation(Context context, CheckPermissionListener listener) {
        SettingManager settingManager = SettingManager.getInstance();

        if (settingManager.isLocationPermissionGranted(context)) {
            if (settingManager.isGPSOn(context)) {
                if (listener != null) {
                    listener.onReady();
                }
            } else {
                showGPSDialog(context);
            }
        } else {
            if (Build.VERSION.SDK_INT >= M) {
                String[] requiredPermissions = settingManager.getRequiredPermissions(context, SettingManager.PermissionType.Location);
                requestPermissions(requiredPermissions, REQ_LOCATION_PERMISSION);
            }
        }
    }

    private void showGPSDialog(Context context) {
        DialogUtils.showDialog(context, getString(R.string.title_msg_turn_on_gps), getString(R.string.title_submit), false, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
    }

    public interface CheckPermissionListener {
        void onReady();
    }


}
