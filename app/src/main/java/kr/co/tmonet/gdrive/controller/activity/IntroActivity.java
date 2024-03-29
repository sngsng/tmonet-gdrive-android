package kr.co.tmonet.gdrive.controller.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import java.util.Locale;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.databinding.ActivityIntroBinding;
import kr.co.tmonet.gdrive.manager.ModelManager;
import kr.co.tmonet.gdrive.manager.SettingManager;
import kr.co.tmonet.gdrive.model.CarInfo;
import kr.co.tmonet.gdrive.model.ChargeStation;
import kr.co.tmonet.gdrive.model.GlobalInfo;
import kr.co.tmonet.gdrive.model.UserInfo;
import kr.co.tmonet.gdrive.network.AppService;
import kr.co.tmonet.gdrive.utils.ModelUtils;

public class IntroActivity extends TMapBaseActivity {

    private static final String LOG_TAG = IntroActivity.class.getSimpleName();

    private ActivityIntroBinding mBinding;
    private String tempFullText = "AT@CARINFO=2,4.4,22,0,0,0,0,0,0,0";
//    private String tempFullText = "AT@CARINFO=2,4.4,22,90,0,0,0,0,0,0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_intro);
        setFinishTransitionStyle(TransitionStyle.None);

        double runnableDistance = ModelUtils.getRunnableDistance(44, 28, 20);

        AppService appService = new AppService(IntroActivity.this);
        appService.checkResponseCommand(tempFullText);


        checkPermissions(IntroActivity.this, SettingManager.PermissionType.Init, new CheckPermissionListener() {
            @Override
            public void onReady() {
                loadChargeStationList();
                setUpViews();
                setUpActions();
            }
        });

    }

    @Override
    public void onBackPressed() {
        checkToCloseApp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateFooterUsrInfo();
        updateFooterCarInfo();
    }

    private void loadChargeStationList() {
        ModelManager modelManager = ModelManager.getInstance();
        modelManager.setChargeStationList(ChargeStation.createList());
    }

    private void setUpViews() {

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
        mBinding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(IntroActivity.this, MainActivity.class);
        startActivity(intent);
        setStartTransitionStyle(TransitionStyle.PushPop);
        finish();
    }
}
