package kr.co.tmonet.gdrive.controller.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import java.util.Locale;
import java.util.Set;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.databinding.ActivityIntroBinding;
import kr.co.tmonet.gdrive.manager.ModelManager;
import kr.co.tmonet.gdrive.manager.SettingManager;
import kr.co.tmonet.gdrive.model.CarInfo;
import kr.co.tmonet.gdrive.model.ChargeStation;
import kr.co.tmonet.gdrive.model.UserInfo;
import kr.co.tmonet.gdrive.network.BluetoothService;
import kr.co.tmonet.gdrive.network.UsbSerialService;
import kr.co.tmonet.gdrive.utils.DialogUtils;
import kr.co.tmonet.gdrive.utils.ModelUtils;

public class IntroActivity extends TMapBaseActivity {

    private static final String LOG_TAG = IntroActivity.class.getSimpleName();
    public static final int REQ_CONNECT_DEVICE = 1;
    public static final int REQ_ENABLE_BLUETOOTH = 2;

    private ActivityIntroBinding mBinding;
    private BluetoothService mBtService;
    private UsbSerialService mUsbService;
    private Handler mHandler;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbSerialService.ACTION_USB_PERMISSION_GRANTED:
                    // USB PERMISSION GRANTED
                    showToast("USB READY");
                    break;
                case UsbSerialService.ACTION_USB_PERMISSION_NOT_GRANTED:
                    // USB PERMISSION NOT GRANTED
                    showToast("USB PERMISSION NOT GRANTED");
                    break;
                case UsbSerialService.ACTION_USB_NONE:
                    showToast("NO USB CONNECTED");
                    break;
                case UsbSerialService.ACTION_USB_DISCONNECTED:
                    showToast("USB DISCONNECTED");
                    break;
                case UsbSerialService.ACTION_USB_NOT_SUPPORTED:
                    showToast("USB_DEVICE_NOT_SUPPORTED");
                    break;
            }
        }
    };

    private final ServiceConnection mUsbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mUsbService = ((UsbSerialService.UsbBinder) service).getService();
            mUsbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mUsbService = null;
        }
    };


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

//        setUpUsbSerialService();
//        setUpBluetoothService();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setFilters();   // Start listening notifications from UsbSerialService;
        startUsbService(UsbSerialService.class, mUsbConnection, null);     // Start UsbSerialService(if it was not started before) and Bind it
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(mUsbConnection);
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

    private void setUpUsbSerialService() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UsbSerialService.MESSAGE_FROM_SERIAL_PORT:
                        String data = (String) msg.obj;
                        showToast("HANDLE MSG : " + (String) msg.obj);
                        break;
                }
            }
        };
    }

    private void startUsbService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbSerialService.SERVICE_CONNECTED) {
            Intent startUsbService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startUsbService.putExtra(key, extra);
                }
            }
            startService(startUsbService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbSerialService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbSerialService.ACTION_USB_NONE);
        filter.addAction(UsbSerialService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbSerialService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbSerialService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
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
        updateFooterInfo();
    }

    private void updateFooterInfo() {
        UserInfo userInfo = ModelManager.getInstance().getGlobalInfo().getUserInfo();
        CarInfo carInfo = ModelManager.getInstance().getGlobalInfo().getCarInfo();

        if (userInfo != null && carInfo != null) {

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

            mBinding.footer.coTextView.setText(carInfo.getCO());
            mBinding.footer.co2TextView.setText(carInfo.getCO2());
            mBinding.footer.alcoholTextView.setText(carInfo.getVolatility());
            mBinding.footer.tempHumiTextView.setText(String.format(Locale.KOREA, getString(R.string.title_temp_humi_format), String.valueOf(carInfo.getTemperature()), String.valueOf(carInfo.getHumidity())));
            mBinding.footer.distanceTextView.setText(ModelUtils.getRunnableDistance(carInfo.getFuelEfficiency(), carInfo.getCarBettery(), carInfo.getRemainBettery()));

        }

//        (연결 1, 충전중 2, 오류 3, 미충전 0)
        switch (carInfo.getChargeState()) {
            case 0:     // 미충전
                mBinding.footer.chargeStateTextView.setText("배터리");
                if (carInfo.getRemainBettery() > 80) {
                    mBinding.footer.chargeStateImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_battery_100));
                } else if (carInfo.getRemainBettery() > 60) {
                    mBinding.footer.chargeStateImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_battery_80));
                } else if (carInfo.getRemainBettery() > 40) {
                    mBinding.footer.chargeStateImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_battery_80));
                } else if (carInfo.getRemainBettery() > 20) {
                    mBinding.footer.chargeStateImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_battery_40));
                } else if (carInfo.getRemainBettery() < 20) {
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
