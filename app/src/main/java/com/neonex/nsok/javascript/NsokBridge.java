package com.neonex.nsok.javascript;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by yun on 2017-09-29.
 */

public class NsokBridge {
    private Context mContext = null;

    public NsokBridge(Context ctx) {
        mContext = ctx;
    }

    @JavascriptInterface
    private void nsokBridge() {
        Toast.makeText(mContext, "TEST", Toast.LENGTH_LONG).show();
    }
}
