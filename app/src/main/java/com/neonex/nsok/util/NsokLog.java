package com.neonex.nsok.util;

import android.util.Log;

import com.neonex.nsok.common.Nsok;

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
     * develop level 에서만 출력한다.
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

                switch (logLevel) {
                    case Log.VERBOSE:
                    case Log.DEBUG:
                    case Log.INFO:
                    case Log.WARN:
                        if (Nsok.serverTarget == Nsok.ServerConnectTarget.TARGET_DEV) {
                            NsokLog.w(tag, log.substring(start, end));
                        }
                        break;
                    case Log.ERROR:
                        NsokLog.e(tag, log.substring(start, end));
                        break;
                }
            }
        } else {
            switch (logLevel) {
                case Log.VERBOSE:
                case Log.DEBUG:
                case Log.INFO:
                case Log.WARN:
                    if (Nsok.serverTarget == Nsok.ServerConnectTarget.TARGET_DEV) {
                        NsokLog.w(tag, log);
                    }
                    break;
                case Log.ERROR:
                    NsokLog.e(tag, log);
                    break;
            }
        }
    }
}
