package kr.co.tmonet.gdrive.manager;

import java.util.ArrayList;

import kr.co.tmonet.gdrive.model.ChargeStation;
import kr.co.tmonet.gdrive.model.GlobalInfo;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class ModelManager {

    private static final String LOG_TAG = ModelManager.class.getSimpleName();
    private static ModelManager sModelManager = new ModelManager();

    public static ModelManager getInstance() {
        return sModelManager;
    }

    private GlobalInfo mGlobalInfo;

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
}
