package kr.co.tmonet.gdrive.view.helper;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.skp.Tmap.TMapView;

import java.util.Locale;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.databinding.ActivityMapBinding;
import kr.co.tmonet.gdrive.manager.ModelManager;
import kr.co.tmonet.gdrive.model.CarInfo;
import kr.co.tmonet.gdrive.model.SearchAddress;
import kr.co.tmonet.gdrive.utils.ModelUtils;

/**
 * Created by Jessehj on 09/06/2017.
 */

public class MapActivityHelper extends ViewHelper implements View.OnClickListener {

    private ActivityMapBinding mBinding;
    private EventCallback mEventCallback;
    private boolean mIsRunnable = true;

    public MapActivityHelper(AppCompatActivity activity, ActivityMapBinding binding) {
        super(activity);
        mBinding = binding;

        setUpActions();
    }

    public void setEventCallback(EventCallback eventCallback) {
        mEventCallback = eventCallback;
    }

    public void setClearSearchLayout() {
        mBinding.search.beforeSearchLayout.setVisibility(View.VISIBLE);
        mBinding.search.afterSearchLayout.setVisibility(View.GONE);
        mBinding.search.searchResultTextView.setText("");
    }

    public boolean isRunnable() {
        return mIsRunnable;
    }

    public void fillSearchReslut(SearchAddress address) {


        if (address != null) {
            mBinding.search.afterSearchLayout.setVisibility(View.VISIBLE);
            mBinding.search.beforeSearchLayout.setVisibility(View.GONE);
            mBinding.search.searchResultTextView.setText(address.getRoadAddress());
            String timeWithFormat = ModelUtils.getExpectedTimeStringFromSeconds(address.getLeadTime());
            mBinding.search.leadTimeTextView.setText(String.format(Locale.KOREA, mActivity.getString(R.string.title_lead_time_format), timeWithFormat));
            mBinding.search.distanceTextView.setText(String.format(Locale.KOREA, mActivity.getString(R.string.title_optimum_distance_format), address.getDistance()));

            if (ModelManager.getInstance().getGlobalInfo() != null) {

                double leadTimeInDouble = Double.parseDouble(address.getLeadTime());
                double consume = leadTimeInDouble / ModelManager.getInstance().getGlobalInfo().getCarInfo().getFuelEfficiency();
                double consumePercent = consume / ModelManager.getInstance().getGlobalInfo().getCarInfo().getCarBettery() * 100;
                double remainBatteryPercent = ModelManager.getInstance().getGlobalInfo().getCarInfo().getRemainBettery() - consumePercent;

                String consumePercentStr = String.format("%.2f", consumePercent);
                String consumeStr = String.format("%.2f", consume);
                mBinding.search.consumeTextView.setText(String.format(Locale.KOREA, mActivity.getString(R.string.title_expect_consume_format), consumePercentStr, consumeStr));
                if (remainBatteryPercent > 0) {
                    String remainBatteryPercentStr = String.format("%.2f", remainBatteryPercent);
                    mBinding.search.batteryTextView.setText(String.format(Locale.KOREA, mActivity.getString(R.string.title_remain_battery_format), remainBatteryPercentStr));
                } else {
                    mBinding.search.batteryTextView.setText("-");
                }

                CarInfo carInfo = ModelManager.getInstance().getGlobalInfo().getCarInfo();
                if (carInfo != null) {
                    double runnableDistance = ModelUtils.getRunnableDistance(carInfo.getFuelEfficiency(), carInfo.getCarBettery(), carInfo.getRemainBettery());

                    if (runnableDistance + 20.0 < Double.parseDouble(address.getDistance())) {
                        mIsRunnable = false;
                    } else {
                        mIsRunnable = true;
                    }
                }
            }
        }
    }

    public void addTMapView(TMapView tMapView) {

        mBinding.mapLayout.addView(tMapView);
    }

    private void setUpActions() {
        mBinding.search.beforeSearchLayout.setOnClickListener(this);
        mBinding.search.searchResultTextView.setOnClickListener(this);
        mBinding.search.searchClearImageView.setOnClickListener(this);
        mBinding.chargeStationImageView.setOnClickListener(this);
        mBinding.currentLocateImageView.setOnClickListener(this);
        mBinding.search.findPathButton.setOnClickListener(this);
        mBinding.zoomInImageView.setOnClickListener(this);
        mBinding.zoomOutImageView.setOnClickListener(this);
        mBinding.homeImageView.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (mEventCallback != null) {
            switch (v.getId()) {
                case R.id.before_search_layout:
                    mEventCallback.onBeforeSearchLayoutClick();
                    break;
                case R.id.search_result_text_view:
                    mEventCallback.onSearchResultClick();
                    break;
                case R.id.search_clear_image_view:
                    mEventCallback.onClearImageClick();
                    break;
                case R.id.charge_station_image_view:
                    mEventCallback.onChargeStationClick();
                    break;
                case R.id.current_locate_image_view:
                    mEventCallback.onCurrentLocateClick();
                    break;
                case R.id.find_path_button:
                    mEventCallback.onLinkTMapToFindPath();
                    break;
                case R.id.zoom_in_image_view:
                    mEventCallback.onZoomInButtonClick();
                    break;
                case R.id.zoom_out_image_view:
                    mEventCallback.onZoomOutButtonClick();
                    break;
                case R.id.home_image_view:
                    mEventCallback.onHomeButtonClick();
                    break;
            }
        }
    }

    public interface EventCallback {
        void onBeforeSearchLayoutClick();

        void onSearchResultClick();

        void onClearImageClick();

        void onChargeStationClick();

        void onCurrentLocateClick();

        void onLinkTMapToFindPath();

        void onZoomInButtonClick();

        void onZoomOutButtonClick();

        void onHomeButtonClick();
    }
}
