package com.neonex.nsok.javascript;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by yun on 2017-09-29.
 */

public class NsokBridge {
    private Context mContext = null;

    private OnFcmTokenSendListener mOnFcmTokenSendListener = null;

    public interface OnFcmTokenSendListener {
        void sendFcmToken();
    }

    public void setOnFcmTokenSendListener(OnFcmTokenSendListener listener) {
        mOnFcmTokenSendListener = listener;
    }

    public NsokBridge(Context ctx) {
        mContext = ctx;
    }

    @JavascriptInterface
    public void sendToken() {
        mOnFcmTokenSendListener.sendFcmToken();
    }
}
