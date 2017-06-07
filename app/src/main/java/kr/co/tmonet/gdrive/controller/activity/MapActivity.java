package kr.co.tmonet.gdrive.controller.activity;

import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.controller.fragment.ChargeListDialogFragment;
import kr.co.tmonet.gdrive.databinding.ActivityMapBinding;
import kr.co.tmonet.gdrive.model.ChargeStation;

public class MapActivity extends TMapBaseActivity implements ChargeListDialogFragment.OnFragmentInteractionListener {

    private static final String LOG_TAG = MapActivity.class.getSimpleName();

    private ActivityMapBinding mBinding;
    private View mDecorView;
    private int mUiOption;

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

    private void setUpViews() {

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

    private void showSearchAddressDialog() {

    }

    private void foundCurrentLocation() {

    }
}
