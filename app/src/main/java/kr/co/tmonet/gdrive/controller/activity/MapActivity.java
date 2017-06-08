package kr.co.tmonet.gdrive.controller.activity;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapMarkerItem2;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.controller.fragment.ChargeListDialogFragment;
import kr.co.tmonet.gdrive.databinding.ActivityMapBinding;
import kr.co.tmonet.gdrive.manager.SettingManager;
import kr.co.tmonet.gdrive.model.ChargeStation;
import kr.co.tmonet.gdrive.network.APIConstants;
import kr.co.tmonet.gdrive.network.RequestHelper;
import kr.co.tmonet.gdrive.network.RestClient;
import kr.co.tmonet.gdrive.utils.ModelUtils;

public class MapActivity extends TMapBaseActivity implements ChargeListDialogFragment.OnFragmentInteractionListener {

    private static final String LOG_TAG = MapActivity.class.getSimpleName();

    private ActivityMapBinding mBinding;
    private View mDecorView;
    private int mUiOption;

    private ArrayList<String> mMarkerIds = new ArrayList<>();
    private TMapView mTMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_map);

        mDecorView = getWindow().getDecorView();
        mUiOption = getWindow().getDecorView().getSystemUiVisibility();
        mUiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mUiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            mUiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        mDecorView.setSystemUiVisibility(mUiOption);

        setUpViews();
        setUpActions();
    }

    @Override
    public void onStationItemClick(int position) {
        final ChargeStation station = mChargeStations.get(position);

        checkEnableUseLocation(MapActivity.this, new CheckPermissionListener() {
            @Override
            public void onReady() {
                linkToTMap(station);
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            mDecorView.setSystemUiVisibility(mUiOption);
        }
    }

    @Override
    protected void updateLocation(Location location) {
        super.updateLocation(location);
    }

    private void setUpViews() {
        mTMapView = new TMapView(MapActivity.this);

        double curLat = SettingManager.getInstance().getCurrentLatitude();
        double curLng = SettingManager.getInstance().getCurrentLongitude();

        mTMapView.setSKPMapApiKey(getString(R.string.t_map_api_key));
        mTMapView.setCenterPoint(curLng, curLat, true);
        mTMapView.setLocationPoint(curLng, curLat);
        mTMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        mTMapView.setIconVisibility(true);
        mTMapView.setZoomLevel(10);
        mTMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        mTMapView.setCompassMode(false);
        mTMapView.setTrackingMode(true);

        mBinding.mapLayout.addView(mTMapView);

        setChargeStationMarkerPoint();
        mTMapView.setOnMarkerClickEvent(new TMapView.OnCalloutMarker2ClickCallback() {
            @Override
            public void onCalloutMarker2ClickEvent(String s, TMapMarkerItem2 tMapMarkerItem2) {
                Log.i(LOG_TAG, "Click marker? " + tMapMarkerItem2.getTMapPoint());
                findPath(tMapMarkerItem2);
            }
        });
    }

    private void setUpActions() {
        mBinding.search.beforeSearchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showSearchAddressDialog();
                mBinding.search.beforeSearchLayout.setVisibility(View.GONE);
                mBinding.search.afterSearchLayout.setVisibility(View.VISIBLE);
            }
        });
        mBinding.search.searchClearImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.search.beforeSearchLayout.setVisibility(View.VISIBLE);
                mBinding.search.afterSearchLayout.setVisibility(View.GONE);
            }
        });
        mBinding.chargeStationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChargeStationListDialog();
            }
        });
        mBinding.currentLocateImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foundCurrentLocation();
            }
        });
    }

    private void setChargeStationMarkerPoint() {

        for (ChargeStation station : mChargeStations) {
            TMapPoint point = new TMapPoint(station.getLatitude(), station.getLongitude());
            TMapMarkerItem item = new TMapMarkerItem();

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_charge_station_marker);

            item.setTMapPoint(point);
            item.setIcon(bitmap);
            item.setName(station.getName());
            item.setVisible(TMapMarkerItem.VISIBLE);
            mTMapView.addMarkerItem(station.getId().toString(), item);
            mMarkerIds.add(station.getId().toString());
        }
    }

    private void showSearchAddressDialog() {

    }

    private void foundCurrentLocation() {
        mTMapView.setCenterPoint(SettingManager.getInstance().getCurrentLongitude(), SettingManager.getInstance().getCurrentLatitude(), true);
    }

    private void findPath(TMapMarkerItem2 markerItem) {
        TMapPoint curPoint = new TMapPoint(SettingManager.getInstance().getCurrentLatitude(), SettingManager.getInstance().getCurrentLongitude());
        double destLat = markerItem.getTMapPoint().getLatitude();
        double destLng = markerItem.getTMapPoint().getLongitude();

        mTMapView.setTMapPathIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_path_start), BitmapFactory.decodeResource(getResources(), R.drawable.ic_path_end));

        TMapData tMapData = new TMapData();
        tMapData.findPathData(curPoint, markerItem.getTMapPoint(), new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(final TMapPolyLine tMapPolyLine) {
                if (tMapPolyLine != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTMapView.addTMapPath(tMapPolyLine);
                                }
                            });
                        }
                    }).start();
                }
            }
        });

        RequestHelper requestHelper = new RequestHelper();
        requestHelper.requestRouteTimeWithDistance(MapActivity.this, curPoint.getLatitude(), curPoint.getLongitude(), destLat, destLng, new RestClient.RestListener() {
            @Override
            public void onBefore() {

            }

            @Override
            public void onSuccess(Object response) {
                if (response instanceof JSONObject) {
                    JSONObject properties = (JSONObject) response;
                    try {
                        String totalDistance = properties.getString(APIConstants.TMap.TOTAL_DISTANCE);
                        double totalDistanceDoubleInKm = Double.parseDouble(totalDistance) / 1000;

                        String totalTime = properties.getString(APIConstants.TMap.TOTAL_TIME);
                        String totalTimeSimpleFormat = ModelUtils.getExpectedTimeStringFromSeconds(Integer.parseInt(totalTime));
                        String distanceInKm = String.format("%.2f", totalDistanceDoubleInKm);
                        showSnackbar(String.format(Locale.KOREA, "약 %1$s/ 약 %2$skm", totalTimeSimpleFormat, distanceInKm));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFail(Error error) {

            }

            @Override
            public void onError(Error error) {

            }
        });
    }
}
