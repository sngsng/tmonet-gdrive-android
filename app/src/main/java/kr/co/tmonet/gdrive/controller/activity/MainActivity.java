package kr.co.tmonet.gdrive.controller.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.controller.fragment.ChargeListDialogFragment;
import kr.co.tmonet.gdrive.databinding.ActivityMainBinding;
import kr.co.tmonet.gdrive.model.ChargeStation;

public class MainActivity extends TMapBaseActivity implements ChargeListDialogFragment.OnFragmentInteractionListener {

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setUpViews();
        setUpActions();
    }

    @Override
    public void onBackPressed() {
        checkToCloseApp();
    }

    @Override
    public void onStationItemClick(int position, boolean isWayPoint) {
        final ChargeStation station = mChargeStations.get(position);

        checkEnableUseLocation(MainActivity.this, new CheckPermissionListener() {
            @Override
            public void onReady() {
                linkToTMap(station);
            }
        });
    }

    private void setUpViews() {
        // TODO Check isShareing state
        // TODO if isShareing -> Fill data
    }

    private void setUpActions() {
        mBinding.chargeStationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChargeStationListDialog(false);
            }
        });
        mBinding.mapLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMapActivity();
            }
        });
    }

    private void startMapActivity() {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        startActivity(intent);
        setStartTransitionStyle(TransitionStyle.PushPop);
    }
}
