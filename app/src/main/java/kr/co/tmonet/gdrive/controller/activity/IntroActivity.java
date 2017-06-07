package kr.co.tmonet.gdrive.controller.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.databinding.ActivityIntroBinding;
import kr.co.tmonet.gdrive.manager.ModelManager;
import kr.co.tmonet.gdrive.manager.SettingManager;
import kr.co.tmonet.gdrive.model.ChargeStation;

public class IntroActivity extends TMapBaseActivity {

    private ActivityIntroBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_intro);
        setFinishTransitionStyle(TransitionStyle.None);

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


    private void loadChargeStationList() {
        ModelManager modelManager = ModelManager.getInstance();
        modelManager.setChargeStationList(ChargeStation.createList());
    }

    private void setUpViews() {
        // TODO Check isShareing state
        // TODO if isShareing -> Fill data
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
