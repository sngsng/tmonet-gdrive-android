package kr.co.tmonet.gdrive.controller.activity;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.skp.Tmap.TMapCircle;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.controller.fragment.ChargeListDialogFragment;
import kr.co.tmonet.gdrive.controller.fragment.SearchAddressDialogFragment;
import kr.co.tmonet.gdrive.databinding.ActivityMapBinding;
import kr.co.tmonet.gdrive.manager.SettingManager;
import kr.co.tmonet.gdrive.model.ChargeStation;
import kr.co.tmonet.gdrive.model.SearchAddress;
import kr.co.tmonet.gdrive.network.APIConstants;
import kr.co.tmonet.gdrive.network.RestClient;
import kr.co.tmonet.gdrive.utils.DialogUtils;
import kr.co.tmonet.gdrive.utils.ModelUtils;
import kr.co.tmonet.gdrive.view.helper.MapActivityHelper;

public class MapActivity extends TMapBaseActivity implements ChargeListDialogFragment.OnFragmentInteractionListener, SearchAddressDialogFragment.OnFragmentInteractionListener {

    private static final String LOG_TAG = MapActivity.class.getSimpleName();
    private static final String KEY_SEARCH_ADDRESS = "keySearchAddress";

    private ActivityMapBinding mBinding;
    private MapActivityHelper mActivityHelper;
    private SearchAddressDialogFragment mSearchAddressDialogFragment;
    private View mDecorView;
    private int mUiOption;

    private ArrayList<String> mMarkerIds = new ArrayList<>();
    private SearchAddress mSearchAddress = new SearchAddress();
    private TMapView mTMapView;
    private TMapGpsManager mTMapGpsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_map);

        setScreenSizeFull();
        setUpViews();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(LOG_TAG, "onSaveInstanceState : searchAddress");

        // TODO SAVE mSearchAddress, mTMapView
    }

    @Override
    public void onStationItemClick(int position, final boolean isWayPoint) {
        final ChargeStation station = mChargeStations.get(position);
        checkEnableUseLocation(MapActivity.this, new CheckPermissionListener() {
            @Override
            public void onReady() {
                if (isWayPoint) {
                    final double curLat = SettingManager.getInstance().getCurrentLatitude();
                    final double curLng = SettingManager.getInstance().getCurrentLongitude();
                    final double destLat = mSearchAddress.getLatitude();
                    final double destLng = mSearchAddress.getLongitude();
                    findDestinationPath(curLat, curLng, destLat, destLng, station.getLatitude(), station.getLongitude());
                } else {
                    linkToTMap(station);
                }

            }
        });
    }

    @Override
    public void onSelectAddress(SearchAddress address) {
        mSearchAddress = address;

        requestGoogleGeoCoding(address.getRoadAddress());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            mDecorView.setSystemUiVisibility(mUiOption);
        }
    }

    @Override
    protected void updateLocation(Location location) {
        setLocationChangedListener(new LocationChangedListener() {
            @Override
            public void onLocationChanged() {
                if (mTMapView != null) {
                    Log.i(LOG_TAG, "LOCATION CHANGED !!");
                    mTMapView.setLocationPoint(SettingManager.getInstance().getCurrentLongitude(), SettingManager.getInstance().getCurrentLatitude());
                    mTMapView.setCenterPoint(mTMapView.getLocationPoint().getLongitude(), mTMapView.getLocationPoint().getLatitude());
                }
            }
        });
        super.updateLocation(location);
    }

    private void setScreenSizeFull() {
        mDecorView = getWindow().getDecorView();
        mUiOption = getWindow().getDecorView().getSystemUiVisibility();
        mUiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mUiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            mUiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        mDecorView.setSystemUiVisibility(mUiOption);
    }

    private void setUpViews() {
        mActivityHelper = new MapActivityHelper(this, mBinding);

        initializeTMap();

        mActivityHelper.setEventCallback(new MapActivityHelper.EventCallback() {
            @Override
            public void onBeforeSearchLayoutClick() {
                showSearchAddressDialog();
            }

            @Override
            public void onSearchResultClick() {
                showSearchAddressDialog();
            }

            @Override
            public void onClearImageClick() {
                clearSearchResult();
            }

            @Override
            public void onChargeStationClick() {
                showChargeStationListDialog(false);
            }

            @Override
            public void onCurrentLocateClick() {
                foundCurrentLocation();
            }

            @Override
            public void onLinkTMapToFindPath() {
                if (mSearchAddress != null) {
                    linkToTMap(mSearchAddress);
                }
            }
        });

    }

    private void initializeTMap() {

        mTMapView = new TMapView(MapActivity.this);

        double curLat = SettingManager.getInstance().getCurrentLatitude();
        double curLng = SettingManager.getInstance().getCurrentLongitude();

        mTMapView.setSKPMapApiKey(getString(R.string.t_map_api_key));
        mTMapView.setCenterPoint(curLng, curLat);
        mTMapView.setLocationPoint(curLng, curLat);
        mTMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        mTMapView.setIconVisibility(true);
        mTMapView.setZoomLevel(15);
        mTMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        mTMapView.setCompassMode(false);
        mTMapView.setTrackingMode(true);

//        mTMapGpsManager = new TMapGpsManager(MapActivity.this);
//        mTMapGpsManager.setMinTime(1000);
//        mTMapGpsManager.setMinDistance(5);
//        mTMapGpsManager.setProvider(mTMapGpsManager.GPS_PROVIDER);
//        mTMapGpsManager.OpenGps();

        setRadiusCircle();

        mActivityHelper.addTMapView(mTMapView);

        setChargeStationMarkerPoint();
    }

    private void setRadiusCircle() {
        TMapCircle circleS = new TMapCircle();
        TMapCircle circleM = new TMapCircle();
        TMapCircle circleL = new TMapCircle();

        circleS.setCenterPoint(mTMapView.getLocationPoint());
        circleS.setAreaColor(0x00ff23);
        circleS.setAreaAlpha(38);
        circleS.setLineColor(0x00ff23);
        circleS.setRadius(1000.0);   // 미터
        circleS.setRadiusVisible(true);

        circleM.setCenterPoint(mTMapView.getLocationPoint());
        circleM.setAreaColor(0xffff00);
        circleM.setAreaAlpha(38);
        circleM.setLineColor(0xffff00);
        circleM.setRadius(2000.0);   // 미터
        circleM.setRadiusVisible(true);

        circleL.setCenterPoint(mTMapView.getLocationPoint());
        circleL.setAreaColor(0xff0000);
        circleL.setAreaAlpha(38);
        circleL.setLineColor(0xff0000);
        circleL.setRadius(3000.0);   // 미터
        circleL.setRadiusVisible(true);

        mTMapView.addTMapCircle("cId1", circleS);
        mTMapView.addTMapCircle("cId2", circleM);
        mTMapView.addTMapCircle("cId3", circleL);

    }

    private void setChargeStationMarkerPoint() {

        for (ChargeStation station : mChargeStations) {
            TMapPoint point = new TMapPoint(station.getLatitude(), station.getLongitude());
            TMapMarkerItem item = new TMapMarkerItem();

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_charge_station_marker);

            item.setTMapPoint(point);
            item.setIcon(bitmap);
            item.setName(station.getName());
            item.setPosition((float) 0.5, (float) 1.0);
            item.setVisible(TMapMarkerItem.VISIBLE);
            item.setEnableClustering(true);

            mTMapView.addMarkerItem(station.getId().toString(), item);
            mTMapView.setEnableClustering(true);
            mMarkerIds.add(station.getId().toString());

        }
    }

    private void showSearchAddressDialog() {
        if (mSearchAddressDialogFragment == null) {
            mSearchAddressDialogFragment = SearchAddressDialogFragment.newInstance();
        }
        mSearchAddressDialogFragment.show(getSupportFragmentManager(), ChargeListDialogFragment.class.getSimpleName());
    }

    private void clearSearchResult() {
        mActivityHelper.setClearSearchLayout();
        mSearchAddress = new SearchAddress();

        mTMapView.removeTMapPath();
    }

    private void foundCurrentLocation() {
        mTMapView.setCenterPoint(SettingManager.getInstance().getCurrentLongitude(), SettingManager.getInstance().getCurrentLatitude(), true);
    }

    private void requestGoogleGeoCoding(String address) {
        SearchAddress.getGeoCoding(MapActivity.this, address, new RestClient.RestListener() {
            @Override
            public void onBefore() {
                showProgressDialog();
            }

            @Override
            public void onSuccess(Object response) {
                if (response instanceof LatLng) {
                    LatLng latLng = (LatLng) response;
                    mSearchAddress.setLatitude(latLng.latitude);
                    mSearchAddress.setLongitude(latLng.longitude);
                }

                loadDestinationInfo();
            }

            @Override
            public void onFail(Error error) {
                dismissProgressDialog();
                showSnackbar(error.getLocalizedMessage());
            }

            @Override
            public void onError(Error error) {
                dismissProgressDialog();
                showSnackbar(error.getLocalizedMessage());
            }
        });
    }

    private void loadDestinationInfo() {
        final double curLat = SettingManager.getInstance().getCurrentLatitude();
        final double curLng = SettingManager.getInstance().getCurrentLongitude();
        final double destLat = mSearchAddress.getLatitude();
        final double destLng = mSearchAddress.getLongitude();

        SearchAddress.getRouteTimeWithDistance(MapActivity.this, curLat, curLng, destLat, destLng, new RestClient.RestListener() {
            @Override
            public void onBefore() {

            }

            @Override
            public void onSuccess(Object response) {
                dismissProgressDialog();
                if (response instanceof JSONObject) {
                    JSONObject properties = (JSONObject) response;
                    try {
                        String totalDistance = properties.getString(APIConstants.TMap.TOTAL_DISTANCE);
                        String totalTime = properties.getString(APIConstants.TMap.TOTAL_TIME);

                        String distanceInKm = ModelUtils.getExpectedDistanceInKmFromMeter(totalDistance);
                        String timeWithFormat = ModelUtils.getExpectedTimeStringFromSeconds(totalTime);

                        mSearchAddress.setDistance(distanceInKm);
                        mSearchAddress.setLeadTime(timeWithFormat);

                        // TODO get expect consume, remain battery data to fill in SearchResultLayout
                        mActivityHelper.fillSearchReslut(mSearchAddress);

                        findDestinationPath(curLat, curLng, destLat, destLng, -1, -1);

                        if (true) { // TODO if distance > Enable running distance (battery + footer info)
                            showAddWayPointDialog();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFail(Error error) {
                dismissProgressDialog();
                showSnackbar(error.getLocalizedMessage());
            }

            @Override
            public void onError(Error error) {
                dismissProgressDialog();
                showSnackbar(error.getLocalizedMessage());
            }
        });
    }

    private void showAddWayPointDialog() {
        DialogUtils.showDialog(MapActivity.this, getString(R.string.title_msg_ask_add_way_point), getString(R.string.title_submit), true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChargeStationListDialog(true);
            }
        });
    }

    /**
     * if it has waypoint,
     *
     * @param passLat,passLng absolutely necessary.
     *                        or passLat = -1
     */
    private void findDestinationPath(double curLat, double curLng, double destLat, double destLng, double passLat, double passLng) {
        TMapPoint curPoint = new TMapPoint(curLat, curLng);
        TMapPoint destPoint = new TMapPoint(destLat, destLng);
        mTMapView.setTMapPathIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_path_start)
                , BitmapFactory.decodeResource(getResources(), R.drawable.ic_path_end));

        TMapData tMapData = new TMapData();
        if (passLat == -1) {
            tMapData.findPathData(curPoint, destPoint, new TMapData.FindPathDataListenerCallback() {
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
        } else {
            ArrayList<TMapPoint> wayPoints = new ArrayList<>();
            wayPoints.add(new TMapPoint(passLat, passLng));
            tMapData.findMultiPointPathData(curPoint, destPoint, wayPoints, 0, new TMapData.FindPathDataListenerCallback() {
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
        }
    }


}
