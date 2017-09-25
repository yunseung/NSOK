package com.neonex.nsok.util;

import android.util.Log;

/**
 * Created by yun on 2017-08-21.
 */

public class NsokLog {
    public static void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    /**
     * 긴 로그는 모두 출력이 되지 않으므로, 잘라서 출력 하는 함수
     *
     * @param logLevel : Log.VERBOSE, Log.DEBUG...
     * @param tag      : TAG
     * @param log      : log
     */
    public static void print(int logLevel, String tag, String log) {
        int outputLength = 3000;
        int length = log.length();

        if (length > outputLength) {
            for (int i = 0; i < length / outputLength + 1; i++) {
                int start = i * outputLength;
                int end = (i + 1) * outputLength;
                if (end > length) {
                    end = length;
                }

                if (logLevel == Log.VERBOSE) {
                    NsokLog.v(tag, log.substring(start, end));
                } else if (logLevel == Log.DEBUG) {
                    NsokLog.d(tag, log.substring(start, end));
                } else if (logLevel == Log.INFO) {
                    NsokLog.i(tag, log.substring(start, end));
                } else if (logLevel == Log.WARN) {
                    NsokLog.w(tag, log.substring(start, end));
                } else if (logLevel == Log.ERROR) {
                    NsokLog.e(tag, log.substring(start, end));
                }
            }
        } else {
            if (logLevel == Log.VERBOSE) {
                NsokLog.v(tag, log);
            } else if (logLevel == Log.DEBUG) {
                NsokLog.d(tag, log);
            } else if (logLevel == Log.INFO) {
                NsokLog.i(tag, log);
            } else if (logLevel == Log.WARN) {
                NsokLog.w(tag, log);
            } else if (logLevel == Log.ERROR) {
                NsokLog.e(tag, log);
            }
        }
    }
}
