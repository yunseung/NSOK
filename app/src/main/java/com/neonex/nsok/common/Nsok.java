package com.neonex.nsok.common;

/**
 * Created by yun on 2017-09-15.
 */

public class Nsok {

    /**
     * constructor
     */
    public Nsok() {}

    public static final ServerConnectTarget serverTarget = ServerConnectTarget.TARGET_DEV; // for connect to development server
//    public static final ServerConnectTarget serverTarget = ServerConnectTarget.TARGET_REAL; // for connect to real server

    private enum ServerUrl {
        API_DEV_URL(""),
        WEB_DEV_URL("http://106.248.228.114:28088"), // 네오넥스 오픈서버
//        WEB_DEV_URL("http://192.168.11.199:28088"), // 네오넥스 개발서버
//        WEB_DEV_URL("http://211.173.192.37:28080"), // NSOK 개발서버
//        WEB_DEV_URL("http://211.173.192.37:3306"), // 임시
//        WEB_DEV_URL("http://192.168.11.154:80"), // 개발
//        WEB_DEV_URL("http://192.168.1.152:80"), // 황과장님
//        WEB_DEV_URL("http://192.168.11.62:80"), // mobile web 개발자님

        API_REAL_URL(""),
        WEB_REAL_URL("");

        String url;
        ServerUrl(String _url) {
            url = _url;
        }

        private String getUrl() {
            return url;
        }

    }

    public enum ServerConnectTarget {
        TARGET_DEV("dev"),
        TARGET_REAL("real");

        String value;
        ServerConnectTarget(String _value) {
            value = _value;
        }

        public String getValue() {
            return value;
        }
    }

    public static String connApiUrl = getApiUrl();
    public static String connWebUrl = getWebUrl();

    private static String getApiUrl() {
        if (serverTarget.getValue().equals(ServerConnectTarget.TARGET_DEV.getValue())) {
            return ServerUrl.API_DEV_URL.getUrl();
        } else {
            return ServerUrl.API_REAL_URL.getUrl();
        }
    }

    private static String getWebUrl() {
        if (serverTarget.getValue().equals(ServerConnectTarget.TARGET_DEV.getValue())) {
            return ServerUrl.WEB_DEV_URL.getUrl();
        } else {
            return ServerUrl.WEB_REAL_URL.getUrl();
        }
    }

}
