package kr.co.tmonet.gdrive.controller.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.databinding.ActivityIntroBinding;
import kr.co.tmonet.gdrive.manager.ModelManager;
import kr.co.tmonet.gdrive.manager.SettingManager;
import kr.co.tmonet.gdrive.model.ChargeStation;
import kr.co.tmonet.gdrive.network.BluetoothService;
import kr.co.tmonet.gdrive.utils.DialogUtils;

public class IntroActivity extends TMapBaseActivity {

    private static final String LOG_TAG = IntroActivity.class.getSimpleName();
    public static final int REQ_CONNECT_DEVICE = 1;
    public static final int REQ_ENABLE_BLUETOOTH = 2;

    private ActivityIntroBinding mBinding;
    private BluetoothService mBtService;
    private Handler mHandler;

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

        setUpBluetoothService();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_ENABLE_BLUETOOTH:
                // When the request to enable Bluetooth returns
                if (resultCode == RESULT_OK) {
                    // OK:
                    mBtService.scanDevice();
                } else {
                    // CANCEL:
                    Log.i(LOG_TAG, "Bluetooth is not enabled");
                }
                break;

            case REQ_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == RESULT_OK) {
                    // Select Device
                    mBtService.getDeviceInfo(data);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setUpBluetoothService() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        if (mBtService == null) {
            mBtService = new BluetoothService(IntroActivity.this, mHandler);
        }

        if (mBtService.getDeviceState()) {
            DialogUtils.showDialog(IntroActivity.this, "블루투스 연결을 하시겠습니까?", getString(R.string.title_submit), true, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBtService.enableBluetooth();
                }
            });
        }

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
