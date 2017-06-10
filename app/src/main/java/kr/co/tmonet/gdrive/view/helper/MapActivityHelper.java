package kr.co.tmonet.gdrive.view.helper;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.skp.Tmap.TMapView;

import java.util.Locale;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.databinding.ActivityMapBinding;
import kr.co.tmonet.gdrive.model.SearchAddress;

/**
 * Created by Jessehj on 09/06/2017.
 */

public class MapActivityHelper extends ViewHelper implements View.OnClickListener {

    private ActivityMapBinding mBinding;
    private EventCallback mEventCallback;

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

    public void fillSearchReslut(SearchAddress address) {
        if (address != null) {
            mBinding.search.afterSearchLayout.setVisibility(View.VISIBLE);
            mBinding.search.beforeSearchLayout.setVisibility(View.GONE);
            mBinding.search.searchResultTextView.setText(address.getRoadAddress());
            mBinding.search.leadTimeTextView.setText(String.format(Locale.KOREA, mActivity.getString(R.string.title_lead_time_format), address.getLeadTime()));
            mBinding.search.distanceTextView.setText(String.format(Locale.KOREA, mActivity.getString(R.string.title_optimum_distance_format), address.getDistance()));
            // TODO setText : consumeTextView, batteryTextView
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
    }
}
