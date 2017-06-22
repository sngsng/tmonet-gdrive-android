package kr.co.tmonet.gdrive.network;

import android.app.Activity;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kr.co.tmonet.gdrive.manager.ModelManager;
import kr.co.tmonet.gdrive.manager.SettingManager;
import kr.co.tmonet.gdrive.model.CarInfo;
import kr.co.tmonet.gdrive.model.ChargeStation;
import kr.co.tmonet.gdrive.model.UserInfo;
import kr.co.tmonet.gdrive.network.APIConstants.Command;

import static kr.co.tmonet.gdrive.utils.DataConvertUtils.calcCheckSum;
import static kr.co.tmonet.gdrive.utils.DataConvertUtils.convertAsciiToBytes;
import static kr.co.tmonet.gdrive.utils.DataConvertUtils.veryfyCheckSum;

/**
 * Created by Jessehj on 20/06/2017.
 */

public class AppService {

    private static final String LOG_TAG = AppService.class.getSimpleName();

    private Activity mActivity;
    private ResponseCallback mCallback;

    public AppService() {
    }

    public AppService(Activity activity) {
        mActivity = activity;
    }

    public void setCallback(ResponseCallback callback) {
        mCallback = callback;
    }

    public void checkResponseCommand(String cmd) {

        if (veryfyCheckSum(cmd)) {
            if (cmd.contains(Command.SIGN_READ)) {
                // READ ?
                checkReadResponse(cmd);

            } else if (cmd.contains(Command.SIGN_WRITE)) {
                // WRITE =
                checkWriteCommand(cmd);
            }
        } else {
            // TODO CHECKSUM ERROR
        }
    }

    public void requestEventCommand(int eventCode, JSONObject params) {
        String requestCmd = "";
        switch (eventCode) {
            case 1:     // 이용정보 조회
                requestCmd = Command.EVENT1 + Command.CR;

                if (mCallback != null) {
                    mCallback.onRequestNewCommand(requestCmd);
                }
                break;
            case 2:     // 충전소 조회
                double lat = SettingManager.getInstance().getCurrentLatitude();
                double lng = SettingManager.getInstance().getCurrentLongitude();
                requestCmd = Command.EVENT2 + "," + lat + "," + lng + Command.CR;

                if (mCallback != null) {
                    mCallback.onRequestNewCommand(requestCmd);
                }

                break;
            case 3:     // 현재시간 요청
                requestCmd = Command.EVENT3 + Command.CR;
                if (mCallback != null) {
                    mCallback.onRequestNewCommand(requestCmd);
                }

                break;
            case 4:     // GPS 좌표 조회
                requestCmd = Command.EVENT4 + Command.CR;

                if (mCallback != null) {
                    mCallback.onRequestNewCommand(requestCmd);
                }
                break;
            case 5:     // 차량정보 요청
                requestCmd = Command.EVENT5 + Command.CR;
                if (mCallback != null) {
                    mCallback.onRequestNewCommand(requestCmd);
                }
                break;
            default:
                break;
        }
    }

    public void requestNotify(int notiCode) {
        String requestCmd = "";

        switch (notiCode) {
            case 0:
                requestCmd = Command.NOTI0 + Command.CR;
                break;
            case 1:
                requestCmd = Command.NOTI1 + Command.CR;
                break;
            default:
                break;
        }

        if (mCallback != null) {
            mCallback.onRequestNewCommand(requestCmd);
        }
    }

    private void checkReadResponse(String responseCmd) {       // ?
        String requestCmd = "";
        if (responseCmd.contains(Command.USRINFO)) {
            UserInfo userInfo = ModelManager.getInstance().getGlobalInfo().getUserInfo();

            if (userInfo != null) {
                requestCmd = Command.USRINFO_READ + userInfo.getUseNum() + "," + userInfo.getStartAt() + "," + userInfo.getEndAt();
            } else {
                requestCmd = Command.USRINFO_READ + "0,0,0";
            }

            requestCmd += Command.SIGN_CHECKSUM_START + calcCheckSum(requestCmd) + Command.CR;

            if (mCallback != null) {
                mCallback.onRequestNewCommand(requestCmd);
            }

        } else if (responseCmd.contains(Command.PWOFF)) {

            if (mCallback != null) {
                mCallback.onPowerOff();
            }

        } else if (responseCmd.contains(Command.CHARGER)) {

        } else if (responseCmd.contains(Command.TIME)) {
            SimpleDateFormat formatter = new SimpleDateFormat("YYYYMMDDhhmmss", Locale.KOREA);
            Date curTime = new Date();
            String formattedTime = formatter.format(curTime);

            requestCmd = Command.TIME_READ + formattedTime;
            requestCmd += calcCheckSum(requestCmd) + Command.CR;


            if (mCallback != null) {
                mCallback.onRequestNewCommand(requestCmd);
            }

        } else if (responseCmd.contains(Command.GPS)) {
            double lat = SettingManager.getInstance().getCurrentLatitude();
            double lng = SettingManager.getInstance().getCurrentLongitude();

            requestCmd = Command.GPS_READ + lat + "," + lng;
            requestCmd += calcCheckSum(requestCmd) + Command.CR;

            if (mCallback != null) {
                mCallback.onRequestNewCommand(requestCmd);
            }

        } else if (responseCmd.contains(Command.CARINFO)) {

        }
    }

    private void checkWriteCommand(String cmd) {      // =
        if (cmd.contains(Command.USRINFO)) {
            UserInfo userInfo = new UserInfo();

            String response = cmd.substring(11, cmd.length() - 4);
            if (response.contains(",")) {
                String[] responseDatas = response.split(",");

                userInfo.setUseNum(Long.parseLong(responseDatas[0]));

                String startAt = responseDatas[1];
                String endAt = responseDatas[2];

                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("YYYYMMDDhhmmss", Locale.KOREA);
                    Date startDate = formatter.parse(startAt);
                    Date endDate = formatter.parse(endAt);

                    userInfo.setStartAt(startDate);
                    userInfo.setEndAt(endDate);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            ModelManager.getInstance().updateGlobalInfo(userInfo);

        } else if (cmd.contains(Command.PWOFF)) {

        } else if (cmd.contains(Command.CHARGER)) {

            // TODO getChargeList( p.8 )
            String response = cmd.substring(11, cmd.length() - 4);
            if (response.contains(",")) {
                String[] responseDatas = response.split(",");
                for (int i = 0; i < responseDatas.length; i++) {
                    ChargeStation chargeStation = new ChargeStation();
                    chargeStation.setName(responseDatas[0]);
                    chargeStation.setLatitude(Double.parseDouble(responseDatas[1]));
                    chargeStation.setLongitude(Double.parseDouble(responseDatas[2]));
                }
            }

        } else if (cmd.contains(Command.TIME)) {
            String response = cmd.substring(8, cmd.length() - 4);
            int year = Integer.parseInt(response.substring(0, 4));
            int month = Integer.parseInt(response.substring(4, 6));
            int date = Integer.parseInt(response.substring(6, 8));
            int hour = Integer.parseInt(response.substring(8, 10));
            int min = Integer.parseInt(response.substring(10, 12));
            int sec = Integer.parseInt(response.substring(12, 14));

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, date, hour, min, sec);

            if (mCallback != null) {
                mCallback.onSynchronizeTime(calendar);
            }

        } else if (cmd.contains(Command.GPS)) {

        } else if (cmd.contains(Command.CARINFO)) {
            CarInfo carInfo = new CarInfo();

            String response = cmd.substring(11, cmd.length() - 4);

            if (response.contains(",")) {
                String[] responseDatas = response.split(",");
                carInfo.setCarType(Integer.parseInt(responseDatas[0]));
                carInfo.setFuelEfficiency(Integer.parseInt(responseDatas[1]));
                carInfo.setCarBettery(Double.parseDouble(responseDatas[2]));
                carInfo.setRemainBettery(Double.parseDouble(responseDatas[3]));
                carInfo.setChargeState(Integer.parseInt(responseDatas[4]));
                carInfo.setCO(Integer.parseInt(responseDatas[5]));
                carInfo.setCO2(Integer.parseInt(responseDatas[6]));
                carInfo.setVolatility(Integer.parseInt(responseDatas[7]));
                carInfo.setTemperature(Double.parseDouble(responseDatas[8]));
                carInfo.setHumidity(Double.parseDouble(responseDatas[9]));
            }

            ModelManager.getInstance().updateGlobalInfo(carInfo);
        }

        if (mCallback != null) {
            mCallback.returnOK();
        }
    }

    private byte[] carriageReturn() {
        return convertAsciiToBytes(Command.CR);
    }

    public byte[] returnOK() {
        return convertAsciiToBytes(Command.OK + Command.CR);
    }

    public interface ResponseCallback {
        void onRequestNewCommand(String requestCmd);

        void onPowerOff();

        void onSynchronizeTime(Calendar calendar);

        void returnOK();
    }
}
