package kr.co.tmonet.gdrive.controller.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.skp.Tmap.TMapTapi;

import java.util.HashMap;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.controller.fragment.ChargeListDialogFragment;
import kr.co.tmonet.gdrive.manager.SettingManager;
import kr.co.tmonet.gdrive.model.Charger;
import kr.co.tmonet.gdrive.model.SearchAddress;
import kr.co.tmonet.gdrive.network.APIConstants;
import kr.co.tmonet.gdrive.utils.DialogUtils;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class TMapBaseActivity extends ConnectBaseActivity {

    private static final String LOG_TAG = TMapBaseActivity.class.getSimpleName();
    public static final int REQ_LOCATION_PERMISSION = 0;

    public ChargeListDialogFragment mChargeListDialogFragment;
    public LocationChangedListener mLocationChangedListener;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private boolean mRequestingLocationUpdate = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpLocationUpdater();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdate) {
            startLocationUpdates();
        }
    }

    public void setLocationChangedListener(LocationChangedListener locationChangedListener) {
        mLocationChangedListener = locationChangedListener;
    }

    protected void updateLocation(Location location) {
        mCurrentLocation = location;
        SettingManager settingManager = SettingManager.getInstance();
        settingManager.setCurrentLatitude(location.getLatitude());
        settingManager.setCurrentLongitude(location.getLongitude());
        Log.i(LOG_TAG, "updateLocation: curLat: " + location.getLatitude() + " / curLng: " + location.getLongitude());

        if (mLocationChangedListener != null) {
            mLocationChangedListener.onLocationChanged();
        }
    }

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

    public void showChargeStationListDialog(boolean isWayPoint) {

        if (mChargeListDialogFragment == null) {
            if (isWayPoint) {
                mChargeListDialogFragment = ChargeListDialogFragment.newInstance(true);
            } else {
                mChargeListDialogFragment = ChargeListDialogFragment.newInstance(false);
            }
        }
        mChargeListDialogFragment.show(getSupportFragmentManager(), ChargeListDialogFragment.class.getSimpleName());
    }

    public void updateChargeStationList() {
        if (mChargeListDialogFragment != null && mChargeListDialogFragment.isAdded()) {
            mChargeListDialogFragment.updateList();
        }
    }

    public void linkToTMap(Charger station, SearchAddress address) {
        TMapTapi tMapTapi = new TMapTapi(this);
        tMapTapi.setSKPMapAuthentication(getString(R.string.t_map_api_key));

        HashMap<String, String> pathInfo = new HashMap<>();

        if (station != null && address != null) {
            // 목적지 + 충전소 (경유지) 탐색
            Log.i(LOG_TAG, "목적지 + 충전소 (경유지) 탐색");
            pathInfo.put(APIConstants.TMap.R_GO_NAME, address.getName());
            pathInfo.put(APIConstants.TMap.R_GO_Y, String.valueOf(address.getLatitude()));
            pathInfo.put(APIConstants.TMap.R_GO_X, String.valueOf(address.getLongitude()));

            pathInfo.put(APIConstants.TMap.R_V1_NAME, station.getName());
            pathInfo.put(APIConstants.TMap.R_V1_Y, String.valueOf(station.getLat()));
            pathInfo.put(APIConstants.TMap.R_V1_X, String.valueOf(station.getLng()));

        } else {
            if (station == null) {
                // 목적지 탐색
                Log.i(LOG_TAG, "목적지 탐색");
                pathInfo.put(APIConstants.TMap.R_GO_NAME, address.getName());
                pathInfo.put(APIConstants.TMap.R_GO_Y, String.valueOf(address.getLatitude()));
                pathInfo.put(APIConstants.TMap.R_GO_X, String.valueOf(address.getLongitude()));
            } else {
                // 충전소 탐색
                Log.i(LOG_TAG, "충전소 탐색");
                pathInfo.put(APIConstants.TMap.R_GO_NAME, station.getName());
                pathInfo.put(APIConstants.TMap.R_GO_Y, String.valueOf(station.getLat()));
                pathInfo.put(APIConstants.TMap.R_GO_X, String.valueOf(station.getLng()));
            }
        }

        if (!tMapTapi.invokeRoute(pathInfo)) {
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

    private void startLocationUpdates() {
        // M이상 런타임 퍼미션 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check Permission
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i(LOG_TAG, "Location Service : Permission denied");
                return;
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLocation(location);
            }
        });
    }

    private void stopLocationUpdates() {
        if (mRequestingLocationUpdate) {
            mRequestingLocationUpdate = false;
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.i(LOG_TAG, "Location Service : onLocationChanged");
                    updateLocation(location);
                }
            });
        }
    }

    private void setUpLocationUpdater() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.i(LOG_TAG, "Location Service : onConnected");

                        // M이상 런타임 퍼미션 체크
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            // Check Permissions
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                        }
                        if (mCurrentLocation == null) {
                            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        }

                        if (!mRequestingLocationUpdate) {
                            mRequestingLocationUpdate = true;
                            startLocationUpdates();
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.i(LOG_TAG, "Location Service : onConnectionSuspended");
                    }
                }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.i(LOG_TAG, "Location Service : onConnectionFailed");
                    }
                }).addApi(LocationServices.API)
                .build();
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

    public interface LocationChangedListener {
        void onLocationChanged();
    }


}
