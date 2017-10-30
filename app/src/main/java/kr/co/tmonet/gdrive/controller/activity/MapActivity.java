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
import java.util.Locale;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.controller.fragment.AlertDialogFragment;
import kr.co.tmonet.gdrive.controller.fragment.ChargeListDialogFragment;
import kr.co.tmonet.gdrive.controller.fragment.SearchAddressDialogFragment;
import kr.co.tmonet.gdrive.databinding.ActivityMapBinding;
import kr.co.tmonet.gdrive.manager.ModelManager;
import kr.co.tmonet.gdrive.manager.SettingManager;
import kr.co.tmonet.gdrive.model.CarInfo;
import kr.co.tmonet.gdrive.model.ChargeStation;
import kr.co.tmonet.gdrive.model.GlobalInfo;
import kr.co.tmonet.gdrive.model.SearchAddress;
import kr.co.tmonet.gdrive.model.TMapViewAttr;
import kr.co.tmonet.gdrive.model.UserInfo;
import kr.co.tmonet.gdrive.network.APIConstants;
import kr.co.tmonet.gdrive.network.AppService;
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
    protected void onStart() {
        super.onStart();
        updateFooterUsrInfo();
        updateFooterCarInfo();


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
        sendEvent(2);

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

    private void updateFooterCarInfo() {
        GlobalInfo globalInfo = ModelManager.getInstance().getGlobalInfo();

        CarInfo carInfo = globalInfo.getCarInfo();

        if (carInfo != null) {

            mBinding.footer.coTextView.setText(String.valueOf(carInfo.getCO()));
            mBinding.footer.co2TextView.setText(carInfo.getCO2() + "");
            mBinding.footer.alcoholTextView.setText(carInfo.getVolatility() + "");
            mBinding.footer.tempHumiTextView.setText(String.format(Locale.KOREA, getString(R.string.title_temp_humi_format), String.valueOf(carInfo.getTemperature()), String.valueOf(carInfo.getHumidity())));

            double runnableDistance = ModelUtils.getRunnableDistance(carInfo.getFuelEfficiency(), carInfo.getCarBettery(), carInfo.getRemainBettery());

            mBinding.footer.distanceTextView.setText(String.format(Locale.KOREA, getString(R.string.title_distance_format), String.format(Locale.KOREA,"%.0f",runnableDistance)));

            //        (연결 1, 충전중 2, 오류 3, 미충전 0)
            switch (carInfo.getChargeState()) {
                case 0:     // 미충전
                    mBinding.footer.chargeStateTextView.setText("배터리");
                    if (carInfo.getRemainBettery() > 80) {
                        mBinding.footer.chargeStateImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_battery_100));
                    } else if (carInfo.getRemainBettery() > 60) {
                        mBinding.footer.chargeStateImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_battery_80));
                    } else if (carInfo.getRemainBettery() > 40) {
                        mBinding.footer.chargeStateImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_battery_60));
                    } else if (carInfo.getRemainBettery() > 20) {
                        mBinding.footer.chargeStateImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_battery_40));
                    } else if (carInfo.getRemainBettery() < 20) {
                        mBinding.footer.chargeStateImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_battery_20));
                    }
                    break;
                case 1:     // 연결
                    mBinding.footer.chargeStateTextView.setText("충전대기");
                    mBinding.footer.chargeStateImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_battery_standby_60_x_19));
                    break;
                case 2:     // 충전중
                    mBinding.footer.chargeStateTextView.setText("충전중");
                    mBinding.footer.chargeStateImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_battery_charging_60_x_19));
                    break;
                case 3:     // 오류
                    mBinding.footer.chargeStateTextView.setText("통신장애");
                    mBinding.footer.chargeStateImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_battery_disruption_60_x_19));
                    break;
                default:
                    break;
            }

        }


    }

    private void updateFooterUsrInfo() {
        GlobalInfo globalInfo = ModelManager.getInstance().getGlobalInfo();

        UserInfo userInfo = globalInfo.getUserInfo();

        if (userInfo != null) {

            String remainStr = ModelUtils.getRemainServiceTime(userInfo.getEndAt());
            if (remainStr != null) {
                mBinding.footer.emptyStateLayout.setVisibility(View.VISIBLE);
                mBinding.footer.shareingStateLayout.setVisibility(View.GONE);

                mBinding.footer.remainTimeTextView.setText(remainStr);
                mBinding.footer.startDateTextView.setText(ModelUtils.getDateFormat(userInfo.getStartAt()));
                mBinding.footer.startTimeTextView.setText(ModelUtils.getTimeFormat(userInfo.getStartAt()));

                mBinding.footer.endDateTextView.setText(ModelUtils.getDateFormat(userInfo.getEndAt()));
                mBinding.footer.endTimeTextView.setText(ModelUtils.getTimeFormat(userInfo.getEndAt()));

            } else {
                mBinding.footer.emptyStateLayout.setVisibility(View.VISIBLE);
                mBinding.footer.shareingStateLayout.setVisibility(View.GONE);
            }
        }
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
                sendEvent(2);

                showChargeStationListDialog(false);
            }

            @Override
            public void onCurrentLocateClick() {
                foundCurrentLocation();
            }

            @Override
            public void onLinkTMapToFindPath() {
                if (mSearchAddress != null) {
                    if (mActivityHelper.isRunnable()) {
                        linkToTMap(null, mSearchAddress);

                    } else {
                        showAddWayPointDialog();
                    }
                }
            }

            @Override
            public void onZoomInButtonClick() {
                mTMapView.MapZoomIn();
            }

            @Override
            public void onZoomOutButtonClick() {
                mTMapView.MapZoomOut();
            }

            @Override
            public void onHomeButtonClick() {
                finish();
            }
        });

        setResultActionListener(new ResultActionListener() {
            @Override
            public void onResultAction(AppService.ActionType actionType) {
                if (ModelManager.getInstance().getGlobalInfo() != null) {
                    GlobalInfo globalInfo = ModelManager.getInstance().getGlobalInfo();

                    switch (actionType) {
                        case CarInfo:
                            updateFooterCarInfo();
                            break;
                        case UsrInfo:
                            updateFooterUsrInfo();
                            break;
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
                return false;
            }

            @Override
            public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                mTMapView.setTrackingMode(false);
                showToast("tracking off");
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
        circleS.setRadiusVisible(true);

        circleM.setCenterPoint(mTMapView.getLocationPoint());
        circleM.setAreaColor(0xffff00);
        circleM.setAreaAlpha(50);
        circleM.setLineColor(0xffff00);
        circleM.setRadiusVisible(true);

        circleL.setCenterPoint(mTMapView.getLocationPoint());
        circleL.setAreaColor(0xff0000);
        circleL.setAreaAlpha(15);
        circleL.setLineColor(0xff0000);
        circleL.setRadiusVisible(true);

        if (ModelManager.getInstance().getGlobalInfo() != null) {
            CarInfo carInfo = ModelManager.getInstance().getGlobalInfo().getCarInfo();
            if (carInfo != null) {
                double runnableDistance = ModelUtils.getRunnableDistance(carInfo.getFuelEfficiency(), carInfo.getCarBettery(), carInfo.getRemainBettery()) * 1000;

                circleS.setRadius(runnableDistance * 0.7);   // 미터
                circleM.setRadius(runnableDistance * 0.9);   // 미터
                circleL.setRadius(runnableDistance);   // 미터
            }
        } else {
            circleS.setRadius(100000 * 0.7);   // 미터
            circleM.setRadius(100000 * 0.9);   // 미터
            circleL.setRadius(100000);   // 미터
        }

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
        if (mAlertDialogFragment == null) {
            mAlertDialogFragment = AlertDialogFragment.newInstance(mSearchAddress.getDistance(), mBinding.search.distanceTextView.getText().toString());
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
