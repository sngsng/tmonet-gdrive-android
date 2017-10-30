package kr.co.tmonet.gdrive.network;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class APIConstants {

    public static class TMap {
        public static final String APP_KEY = "appKey";
        public static final String ACCEPT = "Accept";
        public static final String APPLICATION_JSON = "application/json";
        public static final String ROUTE_URL = "https://apis.skplanetx.com/tmap/routes?";
        public static final String TMAP = "tmap";
        public static final String ROUTES = "routes";
        public static final String VERSION = "version";
        public static final String VERSION_VALUE_1 = "1";
        public static final String START_Y = "startY";
        public static final String START_X = "startX";
        public static final String END_Y = "endY";
        public static final String END_X = "endX";
        public static final String REQ_COORD_TYPE = "reqCoordType";
        public static final String COORD_TYPE_VALUE = "WGS84GEO";
        public static final String FEATURES = "features";
        public static final String PROPERTIES = "properties";
        public static final String TOTAL_DISTANCE = "totalDistance";
        public static final String TOTAL_TIME = "totalTime";

        public static final String R_GO_NAME = "rGoName";
        public static final String R_GO_X = "rGoX";
        public static final String R_GO_Y = "rGoY";
        public static final String R_V1_NAME = "rV1Name";
        public static final String R_V1_X = "rV1X";
        public static final String R_V1_Y = "rV1Y";
    }

    public static class GeoCoding {
        public static final String URL = "https://apis.skplanetx.com/tmap/geo/geocoding";
        public static final String COORD_TYPE = "coordType";
        public static final String CITY_DO = "city_do";
        public static final String GU_GUN = "gu_gun";
        public static final String DONG = "dong";
        public static final String BUNJI = "bunji";
        public static final String DETAIL_ADDRESS = "detailAddress";
        public static final String ADDRESS_FLAG = "addressFlag";
        public static final String ADDR_FLAG_OLD = "F01"; // 구 주소
        public static final String ADDR_FLAG_ROAD = "F02"; // 도로명 주소
        public static final String COORDINATE_INFO = "coordinateInfo";
        public static final String NEW_LAT = "newLat";
        public static final String NEW_LNG = "newLon";
        public static final double DEF_LAT = 37.5554135;
        public static final double DEF_LNG = 126.915578;
    }

    public static class AddressSearch {
        public static final String URL = "http://www.juso.go.kr/addrlink/addrLinkApiJsonp.do";
        public static final String CONFM_KEY = "confmKey";
        public static final String KEY_WORD = "keyword";
        public static final String CURRENT_PAGE = "currentPage";
        public static final String COUNT_PER_PAGE = "countPerPage";
        public static final String RESULT_TYPE = "resultType";
        public static final String RESULT_TYPE_JSON = "json";

        public static final String RESULTS = "results";
        public static final String COMMON = "common";
        public static final String ERROR_CODE = "errorCode";
        public static final String ERROR_MESSAGE = "errorMessage";
        public static final String JUSO = "juso";
        public static final String TOTAL_COUNT = "totalCount";
        public static final String ROAD_ADDR = "roadAddr";
        public static final String ROAD_ADDR_PART1 = "roadAddrPart1";
        public static final String ROAD_ADDR_PART2 = "roadAddrPart2";
        public static final String JIBUN_ADDR = "jibunAddr";
        public static final String ZIP_NO = "zipNo";
        public static final String RN = "rn";
        public static final String BULD_MNNM = "buldMnnm";
        public static final String BULD_SLNO = "buldSlno";
        public static final String SI_NM = "siNm";
        public static final String SGG_NM = "sggNm";
        public static final String EMD_NM = "emdNm";
        public static final String BD_NM = "bdNm";
        public static final String LNBR_MNNM = "lnbrMnnm";
        public static final String LNBR_SLNO = "lnbrSlno";
    }

    public static class Command {
        public static final String CR = "\r";
        public static final String AT = "AT";
        public static final String SIGN_AT = "@";
        public static final String SIGN_READ = "?";
        public static final String SIGN_WRITE = "=";

        public static final String SIGN_CHECKSUM_START = "*";
        public static final String SIGN_CHECKSUM_END = "<";

        public static final String PWOFF = "PWOFF";
        public static final String CHARGER = "CHARGER";

        public static final String TIME = "TIME";
        public static final String TIME_READ = "@TIME=";

        public static final String GPS = "GPS";
        public static final String GPS_READ = "@GPS=";

        public static final String CARINFO = "CARINFO";

        public static final String EVENT = "EVENT";
        public static final String EVENT1 = "EVENT:1";
        public static final String EVENT2 = "EVENT:2";
        public static final String EVENT3 = "EVENT:3";
        public static final String EVENT4 = "EVENT:4";
        public static final String EVENT5 = "EVENT:5";

        public static final String USRINFO = "USRINFO";
        public static final String USRINFO_READ = "@USRINFO=";

        public static final String OK = "OK";

        public static final String NOTI = "NOTI";
        public static final String NOTI0 = "NOTI:0";
        public static final String NOTI1 = "NOTI:1";


        public static final String ERROR = "ERROR";
    }
}