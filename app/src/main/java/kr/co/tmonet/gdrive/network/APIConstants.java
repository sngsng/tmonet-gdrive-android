package kr.co.tmonet.gdrive.network;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class APIConstants {

    public static class GeoCoding {
        public static final double DEF_LAT = 37.566626;
        public static final double DEF_LNG = 126.977938;

        public static final String URL = "https://maps.googleapis.com/maps/api/geocode/json";
        public static final String ADDRESS = "address";
        public static final String KEY = "key";
        public static final String RESULTS = "results";
        public static final String GEOMETRY = "geometry";
        public static final String LOCATION = "location";
        public static final String LAT = "lat";
        public static final String LNG = "lng";
    }

    public static class TMap {
        public static final String API_ROOT_AUTHORITY = "apis.skplanetx.com";
        public static final String TMAP = "tmap";
        public static final String ROUTES = "routes";
        public static final String VERSION = "version";
        public static final String START_Y = "startY";
        public static final String START_X = "startX";
        public static final String END_Y = "endY";
        public static final String END_X = "endX";
        public static final String COORD_TYPE = "reqCoordType";
        public static final String COORD_TYPE_VALUE = "WGS84GEO";
        public static final String FEATURES = "features";
        public static final String PROPERTIES = "properties";
        public static final String TOTAL_DISTANCE = "totalDistance";
        public static final String TOTAL_TIME = "totalTime";


    }
}
