package kr.co.tmonet.gdrive.controller.activity;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.skp.Tmap.TMapCircle;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.controller.fragment.AlertDialogFragment;
import kr.co.tmonet.gdrive.controller.fragment.ChargeListDialogFragment;
import kr.co.tmonet.gdrive.controller.fragment.SearchAddressDialogFragment;
import kr.co.tmonet.gdrive.databinding.ActivityMapBinding;
import kr.co.tmonet.gdrive.manager.SettingManager;
import kr.co.tmonet.gdrive.model.ChargeStation;
import kr.co.tmonet.gdrive.model.SearchAddress;
import kr.co.tmonet.gdrive.model.TMapViewAttr;
import kr.co.tmonet.gdrive.network.APIConstants;
import kr.co.tmonet.gdrive.network.RestClient;
import kr.co.tmonet.gdrive.utils.ModelUtils;
import kr.co.tmonet.gdrive.view.helper.MapActivityHelper;

public class MapActivity extends TMapBaseActivity implements AlertDialogFragment.OnFragmentInteractionListener, ChargeListDialogFragment.OnFragmentInteractionListener, SearchAddressDialogFragment.OnFragmentInteractionListener {

    private static final String LOG_TAG = MapActivity.class.getSimpleName();
    private static final String KEY_SEARCH_ADDRESS = "keySearchAddress";
    private static final String KEY_TMAP_VIEW_ATTR = "keyTMapViewAttr";

    private ActivityMapBinding mBinding;
    private MapActivityHelper mActivityHelper;
    private SearchAddressDialogFragment mSearchAddressDialogFragment;
    private AlertDialogFragment mAlertDialogFragment;
    private View mDecorView;
    private int mUiOption;

    private ArrayList<String> mMarkerIds = new ArrayList<>();
    private SearchAddress mSearchAddress = new SearchAddress();
    private TMapView mTMapView;
    private TMapViewAttr mTMapViewAttr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_map);

        if (savedInstanceState != null) {

            mTMapViewAttr = savedInstanceState.getParcelable(KEY_TMAP_VIEW_ATTR);
            mSearchAddress = savedInstanceState.getParcelable(KEY_SEARCH_ADDRESS);
        }

        setScreenSizeFull();
        setUpViews();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(LOG_TAG, "onSaveInstanceState : searchAddress");

        saveTMapAttribute();

        outState.putParcelable(KEY_SEARCH_ADDRESS, mSearchAddress);
        outState.putParcelable(KEY_TMAP_VIEW_ATTR, mTMapViewAttr);
    }

    @Override
    public void onStationItemClick(int position, final boolean isWayPoint) {
        final ChargeStation station = mChargeStations.get(position);
        checkEnableUseLocation(MapActivity.this, new CheckPermissionListener() {
            @Override
            public void onReady() {
                linkToTMap(station, isWayPoint ? mSearchAddress : null);
            }
        });
    }

    @Override
    public void onStationDialogCancelClick(boolean isWayPoint) {

        if (isWayPoint) {
            linkToTMap(null, mSearchAddress);
        }
    }

    @Override
    public void onAlertSubmitClick() {
        showChargeStationListDialog(true);
    }

    @Override
    public void onAlertCancelClick() {
        linkToTMap(null, mSearchAddress);
    }

    @Override
    public void onSelectAddress(SearchAddress address) {
        mSearchAddress = address;

        requestGeoCoding(address);
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
                    mTMapView.setLocationPoint(SettingManager.getInstance().getCurrentLongitude(), SettingManager.getInstance().getCurrentLatitude());
                    mTMapView.setCenterPoint(SettingManager.getInstance().getCurrentLongitude(), SettingManager.getInstance().getCurrentLatitude());

                }
            }
        });
        super.updateLocation(location);
    }

    private void saveTMapAttribute() {
        Log.i(LOG_TAG, "onSaveInstanceState : searchAddress");
        mTMapViewAttr = new TMapViewAttr();
        mTMapViewAttr.setZoomLevel(mTMapView.getZoomLevel());
        mTMapViewAttr.setLocateLat(mTMapView.getLocationPoint().getLatitude());
        mTMapViewAttr.setLocateLng(mTMapView.getLocationPoint().getLongitude());
        Log.i(LOG_TAG, "mTMapViewATTR: " + mTMapViewAttr.toString());
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

        if (mSearchAddress.getName() != null) {
            loadDestinationInfo();
        }

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
                    if (Double.parseDouble(mSearchAddress.getDistance()) > 100.0) { // TODO if distance > Enable running distance (battery + footer info)
                        showAddWayPointDialog();
                    } else {
                        linkToTMap(null, mSearchAddress);
                    }
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
        mTMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        mTMapView.setIconVisibility(true);
        mTMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        mTMapView.setCompassMode(false);
        mTMapView.setTrackingMode(false);

        if (mTMapViewAttr != null) {
            Log.i(LOG_TAG, "ATTR is not null: load Attr: ");
            mTMapView.setLocationPoint(mTMapViewAttr.getLocateLng(), mTMapViewAttr.getLocateLat());
            mTMapView.setZoomLevel(mTMapViewAttr.getZoomLevel());
        } else {
            mTMapView.setLocationPoint(curLng, curLat);
            mTMapView.setZoomLevel(15);
        }

        setRadiusCircle();

        mActivityHelper.addTMapView(mTMapView);

        setChargeStationMarkerPoint();

        mTMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                mTMapView.setTrackingMode(false);
                showToast("tracking off");
                return false;
            }

            @Override
            public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                return false;
            }
        });


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
        mTMapView.setTrackingMode(true);
        showToast("tracking on");
        mTMapView.setCenterPoint(SettingManager.getInstance().getCurrentLongitude(), SettingManager.getInstance().getCurrentLatitude(), true);
    }

    private void requestGeoCoding(SearchAddress address) {
        SearchAddress.getTMapGeoCoding(MapActivity.this, address, new RestClient.RestListener() {
            @Override
            public void onBefore() {
                showProgressDialog();
            }

            @Override
            public void onSuccess(Object response) {
                dismissProgressDialog();
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
        mTMapView.removeTMapPath();
        final double curLat = SettingManager.getInstance().getCurrentLatitude();
        final double curLng = SettingManager.getInstance().getCurrentLongitude();
        final double destLat = mSearchAddress.getLatitude();
        final double destLng = mSearchAddress.getLongitude();

        SearchAddress.getRouteTimeWithDistance(MapActivity.this, curLat, curLng, destLat, destLng, new RestClient.RestListener() {
            @Override
            public void onBefore() {

                showProgressDialog();

            }

            @Override
            public void onSuccess(Object response) {
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

                        findDestinationPath(curLat, curLng, destLat, destLng);

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
        // TODO 예상 주행 가능 거리 계산???
        if (mAlertDialogFragment == null) {
            mAlertDialogFragment = AlertDialogFragment.newInstance(mSearchAddress.getDistance(), "18");
        }
        mAlertDialogFragment.show(getSupportFragmentManager(), AlertDialogFragment.class.getSimpleName());
    }

    private void findDestinationPath(double curLat, double curLng, double destLat, double destLng) {
        TMapPoint curPoint = new TMapPoint(curLat, curLng);
        TMapPoint destPoint = new TMapPoint(destLat, destLng);
        mTMapView.setTMapPathIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_path_start)
                , BitmapFactory.decodeResource(getResources(), R.drawable.ic_path_end));

        TMapData tMapData = new TMapData();

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
        dismissProgressDialog();
    }
}
