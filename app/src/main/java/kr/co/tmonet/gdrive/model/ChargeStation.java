package kr.co.tmonet.gdrive.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class ChargeStation implements Parcelable {

    private Integer mId;
    private String mName;
    private String mAddress;
    private String mDistance;
    private double mLatitude;
    private double mLongitude;
    private boolean mIsChargeable;

    public ChargeStation() {
    }

    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getDistance() {
        return mDistance;
    }

    public void setDistance(String distance) {
        mDistance = distance;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public boolean isChargeable() {
        return mIsChargeable;
    }

    public void setChargeable(boolean chargeable) {
        mIsChargeable = chargeable;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.mId);
        dest.writeString(this.mName);
        dest.writeString(this.mAddress);
        dest.writeDouble(this.mLatitude);
        dest.writeDouble(this.mLongitude);
        dest.writeByte(this.mIsChargeable ? (byte) 1 : (byte) 0);
    }

    protected ChargeStation(Parcel in) {
        this.mId = (Integer) in.readValue(Integer.class.getClassLoader());
        this.mName = in.readString();
        this.mAddress = in.readString();
        this.mLatitude = in.readDouble();
        this.mLongitude = in.readDouble();
        this.mIsChargeable = in.readByte() != 0;
    }

    public static final Creator<ChargeStation> CREATOR = new Creator<ChargeStation>() {
        @Override
        public ChargeStation createFromParcel(Parcel source) {
            return new ChargeStation(source);
        }

        @Override
        public ChargeStation[] newArray(int size) {
            return new ChargeStation[size];
        }
    };

    public static ArrayList<ChargeStation> createList() {
        ArrayList<ChargeStation> stations = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            ChargeStation chargeItem = new ChargeStation();
            chargeItem.setDistance(i + "Km");
            chargeItem.setChargeable(i % 2 == 0);

            switch (i) {
                case 0:
                    chargeItem.setName("공덕역");
                    chargeItem.setAddress("서울특별시 마포구 공덕동 439-8");
                    chargeItem.setLatitude(37.543459);
                    chargeItem.setLongitude(126.951321);
                    break;
                case 1:
                    chargeItem.setName("극동아파트");
                    chargeItem.setAddress("인천광역시 계양구 하느재로19번길 6");
                    chargeItem.setLatitude(37.546431);
                    chargeItem.setLongitude(126.724139);
                    break;
                case 2:
                    chargeItem.setName("구암중학교");
                    chargeItem.setAddress("서울특별시 관악구 봉천동 산101-360");
                    chargeItem.setLatitude(37.492522);
                    chargeItem.setLongitude(126.948019);
                    break;
                case 3:
                    chargeItem.setName("동원장여관");
                    chargeItem.setAddress("서울특별시 영등포구 신길동 4300-25");
                    chargeItem.setLatitude(37.498752);
                    chargeItem.setLongitude(126.918055);
                    break;
                case 4:
                    chargeItem.setName("영문초등학교");
                    chargeItem.setAddress("서울특별시 영등포구 문래로 56");
                    chargeItem.setLatitude(37.519682);
                    chargeItem.setLongitude(126.887679);
                    break;
                case 5:
                    chargeItem.setName("염창우체국");
                    chargeItem.setAddress("염창동우체국 서울특별시 강서구 염창동 282-25");
                    chargeItem.setLatitude(37.547176);
                    chargeItem.setLongitude(126.873997);
                    break;
                case 6:
                    chargeItem.setName("광성중학교");
                    chargeItem.setAddress("서울특별시 마포구 신수동 89-94");
                    chargeItem.setLatitude(37.548793);
                    chargeItem.setLongitude(126.936991);
                    break;

                case 7:
                    chargeItem.setName("예일디자인고등학교");
                    chargeItem.setAddress("서울특별시 은평구 구산동 8-3");
                    chargeItem.setLatitude(37.609611);
                    chargeItem.setLongitude(126.915808);
                    break;
                case 8:
                    chargeItem.setName("철산초등학교");
                    chargeItem.setAddress("경기도 광명시 철산3동 하안로 392");
                    chargeItem.setLatitude(37.469097);
                    chargeItem.setLongitude(126.874848);
                    break;
                case 9:
                    chargeItem.setName("청라휴먼시아");
                    chargeItem.setAddress("인천광역시 경서동 959-1");
                    chargeItem.setLatitude(37.528539);
                    chargeItem.setLongitude(126.646894);
                    break;

                default:
                    chargeItem.setName("이화여자대학교");
                    chargeItem.setAddress("서울특별시 서대문구 대현동 이화여대길 52");
                    chargeItem.setLatitude(37.561860);
                    chargeItem.setLongitude(126.946841);
                    break;
            }

            stations.add(chargeItem);
        }

        return stations;
    }

}
