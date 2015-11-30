package edu.sei.eecs.pku.hermes.configs;

/**
 * Created by bilibili on 15/11/16.
 */
public class Constants {
    public static final int FAILED_REASON_NUM = 5;
    public static final String BASE_URL = "http://10.1.2.139:8888/";
    public static final String SCHEDULE_URL = "http://10.1.2.139:8888/schedule/";
    public static final int DISPATCHING_CENTER = -1;
    public static final int STATUS_UNINFORMED = 0;
    public static final int STATUS_READY = 1;
    public static final int STATUS_COMPLETED = 2;
    public static final int STATUS_FAILED = 3;

    // -1 起始点， 0 未发送短信，1 已发短信，2 已送达，3冲突
}
