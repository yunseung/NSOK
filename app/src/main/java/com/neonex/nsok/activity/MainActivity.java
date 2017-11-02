package com.neonex.nsok.activity;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.iid.FirebaseInstanceId;
import com.neonex.nsok.R;
import com.neonex.nsok.common.Nsok;
import com.neonex.nsok.javascript.NsokBridge;
import com.neonex.nsok.util.AppExitPreventUtil;
import com.neonex.nsok.util.NsokPreferences;

public class MainActivity extends AppCompatActivity {

    private static WebView mNsokWebView = null;                                 // NSOK WebView
    private SwipeRefreshLayout mSwipeRefreshLayout = null;                      // 아래로 당겨서 새로고침 레이아웃
    private AppExitPreventUtil mAppExitPreventHandler = null;                   // 뒤로가기 한 번에 앱 죽이는 것 방지 핸들러

    private NsokBridge mNsokBridge = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (NsokPreferences.getFcmToken(MainActivity.this) != null) {
            Log.e("MainActivity.java", "is it token? : " + NsokPreferences.getFcmToken(MainActivity.this));
        } else {
            Log.e("MainActivity.java", "token is empty");
        }

        // Broadcast 로 fcm token 받기
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.nsok.fcm.FCM_TOKEN");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NsokPreferences.setFcmToken(MainActivity.this, intent.getStringExtra("token"));
            }
        }, intentFilter);

        mNsokBridge = new NsokBridge(this);
        mNsokBridge.setOnFcmTokenSendListener(new NsokBridge.OnFcmTokenSendListener() {
            @Override
            public void sendFcmToken() {
                mNsokWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("token already send??", "Token : " + NsokPreferences.getFcmToken(MainActivity.this));
                        mNsokWebView.loadUrl("javascript:getFcmToken('" + NsokPreferences.getFcmToken(MainActivity.this) + "');");
                    }
                });
            }
        });

        initializeWebView();

        mAppExitPreventHandler = new AppExitPreventUtil(this);
    }

    private void initializeWebView() {
        mNsokWebView = (WebView) findViewById(R.id.nsok_webView);
        mNsokWebView.setWebChromeClient(new NsokWebChromeClient());
        mNsokWebView.setWebViewClient(new NsokWebViewClient());
        WebSettings webSettings = mNsokWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mNsokWebView.addJavascriptInterface(mNsokBridge, "nsokBridge");

        mNsokWebView.loadUrl(Nsok.connWebUrl);

        // 아래로 당겨서 새로고침.

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                                     @Override
                                                     public void onRefresh() {
                                                         if (mSwipeRefreshLayout.isRefreshing()) {
                                                             mSwipeRefreshLayout.setRefreshing(false);
                                                         }
                                                         mNsokWebView.reload();
                                                     }
                                                 }
        );
    }

    final class NsokWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
            if (message == null)
                return;
        }
    }

    final class NsokWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }


        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("ShouldOverride", url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
        }
    }

    @Override
    public void onBackPressed() {

        int fragmentStackCnt = getSupportFragmentManager().getBackStackEntryCount();
        if (fragmentStackCnt == 0) {
            mAppExitPreventHandler.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}
