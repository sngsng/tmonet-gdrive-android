package kr.co.tmonet.gdrive.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jessehj on 13/06/2017.
 */

public class UserInfo implements Parcelable {

    private static final String LOG_TAG = UserInfo.class.getSimpleName();

    private long mUseNum;       // 이용번호 20YYMMDDhhmmss (20170321000002)
    private long mStartAt;      // 시작시간 YYMMDDhhmmss (170402120000)
    private long mEndAt;        // 종료시간 YYMMDDhhmmss (170403120000)
    private double mReturnLat;  // 반납거점 위도 (mm.ssssssss)
    private double mReturnLng;  // 반납거점 경도 (mmm.ssssssss)

    public UserInfo() {
    }

    public long getUseNum() {
        return mUseNum;
    }

    public void setUseNum(long useNum) {
        mUseNum = useNum;
    }

    public long getStartAt() {
        return mStartAt;
    }

    public void setStartAt(long startAt) {
        mStartAt = startAt;
    }

    public long getEndAt() {
        return mEndAt;
    }

    public void setEndAt(long endAt) {
        mEndAt = endAt;
    }

    public double getReturnLat() {
        return mReturnLat;
    }

    public void setReturnLat(double returnLat) {
        mReturnLat = returnLat;
    }

    public double getReturnLng() {
        return mReturnLng;
    }

    public void setReturnLng(double returnLng) {
        mReturnLng = returnLng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mUseNum);
        dest.writeLong(this.mStartAt);
        dest.writeLong(this.mEndAt);
        dest.writeDouble(this.mReturnLat);
        dest.writeDouble(this.mReturnLng);
    }

    protected UserInfo(Parcel in) {
        this.mUseNum = in.readLong();
        this.mStartAt = in.readLong();
        this.mEndAt = in.readLong();
        this.mReturnLat = in.readDouble();
        this.mReturnLng = in.readDouble();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    @Override
    public String toString() {
        return "UserInfo{" +
                "mUseNum=" + mUseNum +
                ", mStartAt=" + mStartAt +
                ", mEndAt=" + mEndAt +
                ", mReturnLat=" + mReturnLat +
                ", mReturnLng=" + mReturnLng +
                '}';
    }
}
