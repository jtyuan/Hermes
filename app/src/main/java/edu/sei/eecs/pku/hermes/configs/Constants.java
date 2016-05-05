package edu.sei.eecs.pku.hermes.configs;

/**
 * Created by bilibili on 15/11/16.
 */
public class Constants {
    public static final int FAILED_REASON_NUM = 3;
    // URL needs to be strictly the same as the route settings in the play server
    public static final String BASE_URL = "http://192.168.1.10:9000";
    public static final String LOC_URL = BASE_URL + "/courier/%s/loc";
    public static final String LOGIN_URL = "http://192.168.1.10:9001/oauth2/access_token/";
    public static final String AUTH_URL = BASE_URL + "/oauth";
    public static final int DISPATCHING_CENTER = -1;

    // -1 起始点， 0 未发送短信，1 已发短信，2 已送达，3冲突
    public static final int STATUS_UNINFORMED = 0;
    public static final int STATUS_READY = 1;
    public static final int STATUS_COMPLETED = 2;
    public static final int STATUS_FAILED = 3;

    public static final String CLIENT_ID = "e5256814efbbe3534859";
    public static final String CLIENT_SECRET = "5a50245e9341ffdcf137c22339432c699e6c5e38";

//    public static final long SCHEDULE_INTERVAL = 43200000L;
    public static final long SCHEDULE_INTERVAL = 43200000000L;
    public static final int LOCATION_INTERVAL_FG = 5000;
    public static final int LOCATION_INTERVAL_BG = 180000;


}
