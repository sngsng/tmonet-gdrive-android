package kr.co.tmonet.gdrive.controller.activity;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import java.util.Calendar;
import java.util.Set;

import kr.co.tmonet.gdrive.R;
import kr.co.tmonet.gdrive.network.AppService;
import kr.co.tmonet.gdrive.network.BluetoothService;
import kr.co.tmonet.gdrive.network.UsbSerialService;
import kr.co.tmonet.gdrive.utils.DataConvertUtils;
import kr.co.tmonet.gdrive.utils.DialogUtils;

/**
 * Created by Jessehj on 21/06/2017.
 */

public class ConnectBaseActivity extends BaseActivity {

    private static final String LOG_TAG = ConnectBaseActivity.class.getSimpleName();

    public static final int REQ_CONNECT_DEVICE = 1;
    public static final int REQ_ENABLE_BLUETOOTH = 2;

    private BluetoothService mBtService;
    private UsbSerialService mUsbService;
    private Handler mHandler;
    private AppService mAppService = new AppService(this);

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpUsbSerialService();
        setUpBluetoothService();

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

                        mAppService.setCallback(new AppService.ResponseCallback() {
                            @Override
                            public void onRequestNewCommand(String requestCmd) {
                                byte[] requestData = DataConvertUtils.convertAsciiToBytes(requestCmd);
                                mUsbService.write(requestData);
                                mUsbService.write(mAppService.returnOK());
                            }

                            @Override
                            public void onPowerOff() {
                                ActivityCompat.finishAffinity(ConnectBaseActivity.this);
                                System.runFinalizersOnExit(true);
                                System.exit(0);
                            }

                            @Override
                            public void onSynchronizeTime(Calendar calendar) {
                                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                                am.setTime(calendar.getTimeInMillis());
                            }

                            @Override
                            public void returnOK() {
                                mUsbService.write(mAppService.returnOK());
                            }
                        });

                        mAppService.checkResponseCommand(data);
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
            mBtService = new BluetoothService(this, mHandler);
        }

        if (mBtService.getDeviceState()) {
            DialogUtils.showDialog(this, "블루투스 연결을 하시겠습니까?", getString(R.string.title_submit), true, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBtService.enableBluetooth();
                }
            });
        }

    }
}
