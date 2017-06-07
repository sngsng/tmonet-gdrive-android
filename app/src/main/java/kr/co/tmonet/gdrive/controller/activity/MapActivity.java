package kr.co.tmonet.gdrive.controller.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.controller.fragment.ChargeListDialogFragment;
import kr.co.tmonet.gdrive.databinding.ActivityMapBinding;
import kr.co.tmonet.gdrive.model.ChargeStation;

public class MapActivity extends TMapBaseActivity implements ChargeListDialogFragment.OnFragmentInteractionListener {

    private static final String LOG_TAG = MapActivity.class.getSimpleName();

    private ActivityMapBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_map);

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

    private void setUpViews() {

    }

    private void setUpActions() {
        mBinding.searchTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchAddressDialog();
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
