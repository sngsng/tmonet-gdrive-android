package kr.co.tmonet.gdrive.controller.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.controller.fragment.ChargeListDialogFragment;
import kr.co.tmonet.gdrive.databinding.ActivityMainBinding;
import kr.co.tmonet.gdrive.manager.ModelManager;
import kr.co.tmonet.gdrive.model.ChargeStation;

public class MainActivity extends TMapBaseActivity implements ChargeListDialogFragment.OnFragmentInteractionListener {

    private ActivityMainBinding mBinding;
    private ChargeListDialogFragment mChargeListDialogFragment;
    private ArrayList<ChargeStation> mChargeStations = ModelManager.getInstance().getChargeStationList();

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
    public void onStationItemClick(int position) {
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
                showChargeStationListDialog();
            }
        });
        mBinding.mapLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMapActivity();
            }
        });
    }

    private void showChargeStationListDialog() {
        if (mChargeListDialogFragment == null) {
            mChargeListDialogFragment = ChargeListDialogFragment.newInstance(mChargeStations);
        }
        mChargeListDialogFragment.show(getSupportFragmentManager(), ChargeListDialogFragment.class.getSimpleName());
    }

    private void startMapActivity() {

    }


}
