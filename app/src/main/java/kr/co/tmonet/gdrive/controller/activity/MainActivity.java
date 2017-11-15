package kr.co.tmonet.gdrive.controller.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Locale;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.controller.fragment.ChargeListDialogFragment;
import kr.co.tmonet.gdrive.databinding.ActivityMainBinding;
import kr.co.tmonet.gdrive.manager.ModelManager;
import kr.co.tmonet.gdrive.model.CarInfo;
import kr.co.tmonet.gdrive.model.Charger;
import kr.co.tmonet.gdrive.model.GlobalInfo;
import kr.co.tmonet.gdrive.model.UserInfo;
import kr.co.tmonet.gdrive.network.AppService;
import kr.co.tmonet.gdrive.utils.ModelUtils;

public class MainActivity extends TMapBaseActivity implements ChargeListDialogFragment.OnFragmentInteractionListener {

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        AppService appService = new AppService(MainActivity.this);
//        String tempChargerText = "AT@CHARGER=3,0,0,이름1,37.543459,126.951321,0,0,이름2,37.492522,126.948019,0,0,이름3,37.519682,126.887679,0,0\\r";[
//        appService.checkResponseCommand(tempChargerText);

        setUpViews();
        setUpActions();
    }

    @Override
    public void onBackPressed() {
        checkToCloseApp();
    }

    @Override
    public void onStationItemClick(int position, boolean isWayPoint) {
        ArrayList<Charger> chargers = ModelManager.getInstance().getChargers();
        final Charger station = chargers.get(position);

        checkEnableUseLocation(MainActivity.this, new CheckPermissionListener() {
            @Override
            public void onReady() {
                linkToTMap(station, null);
            }
        });
    }

    @Override
    public void onStationDialogCancelClick(boolean isWayPoint) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        updateFooterUsrInfo();
        updateFooterCarInfo();


    }

    private void setUpViews() {
        // TODO Check isShareing state
        // TODO if isShareing -> Fill data

        updateFooterCarInfo();
        updateFooterUsrInfo();


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
                        case Charger:
                            updateChargeStationList();
                            break;
                    }
                }
            }
        });
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

            mBinding.footer.distanceTextView.setText(String.format(Locale.KOREA, getString(R.string.title_distance_format), String.format(Locale.KOREA, "%.0f", runnableDistance)));

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
                    } else {
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

    private void setUpActions() {
        mBinding.chargeStationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEvent(2);
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
