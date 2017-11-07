package kr.co.tmonet.gdrive.manager;

import java.util.ArrayList;

import kr.co.tmonet.gdrive.model.CarInfo;
import kr.co.tmonet.gdrive.model.ChargeStation;
import kr.co.tmonet.gdrive.model.Charger;
import kr.co.tmonet.gdrive.model.GlobalInfo;
import kr.co.tmonet.gdrive.model.UserInfo;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class ModelManager {

    private static final String LOG_TAG = ModelManager.class.getSimpleName();
    private static ModelManager sModelManager = new ModelManager();

    public static ModelManager getInstance() {
        return sModelManager;
    }

    private GlobalInfo mGlobalInfo = new GlobalInfo();
    private ArrayList<Charger> mChargers = new ArrayList<>();

    private ArrayList<ChargeStation> mChargeStationList = new ArrayList<>();

    public ArrayList<ChargeStation> getChargeStationList() {
        return mChargeStationList;
    }

    public void setChargeStationList(ArrayList<ChargeStation> chargeStationList) {
        mChargeStationList = chargeStationList;
    }

    public GlobalInfo getGlobalInfo() {
        return mGlobalInfo;
    }

    public void setGlobalInfo(GlobalInfo globalInfo) {
        mGlobalInfo = globalInfo;
    }

    public void updateGlobalInfo(Object object) {
        if (object instanceof UserInfo) {
            UserInfo userInfo = (UserInfo) object;
            mGlobalInfo.setUserInfo(userInfo);
        } else if (object instanceof CarInfo) {
            CarInfo carInfo = (CarInfo) object;
            mGlobalInfo.setCarInfo(carInfo);
        }
        setGlobalInfo(mGlobalInfo);
    }

    public ArrayList<Charger> getChargers() {
        return mChargers;
    }

    public void setChargers(ArrayList<Charger> chargers) {
        mChargers = chargers;
    }
}
