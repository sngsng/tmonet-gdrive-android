package kr.co.tmonet.gdrive.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jessehj on 13/06/2017.
 */

public class GlobalInfo implements Parcelable {

    private CarInfo mCarInfo;           // 차량 정보
    private UserInfo mUserInfo;         // 사용자 정보

    public GlobalInfo() {
    }

    public CarInfo getCarInfo() {
        return mCarInfo;
    }

    public void setCarInfo(CarInfo carInfo) {
        mCarInfo = carInfo;
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        mUserInfo = userInfo;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mCarInfo, flags);
        dest.writeParcelable(this.mUserInfo, flags);
    }

    protected GlobalInfo(Parcel in) {
        this.mCarInfo = in.readParcelable(CarInfo.class.getClassLoader());
        this.mUserInfo = in.readParcelable(UserInfo.class.getClassLoader());
    }

    public static final Creator<GlobalInfo> CREATOR = new Creator<GlobalInfo>() {
        @Override
        public GlobalInfo createFromParcel(Parcel source) {
            return new GlobalInfo(source);
        }

        @Override
        public GlobalInfo[] newArray(int size) {
            return new GlobalInfo[size];
        }
    };

    @Override
    public String toString() {
        return "GlobalInfo{" +
                "mCarInfo=" + mCarInfo +
                ", mUserInfo=" + mUserInfo +
                '}';
    }
}
