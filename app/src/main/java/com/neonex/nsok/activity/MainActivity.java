package com.neonex.nsok.activity;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.neonex.nsok.R;
import com.neonex.nsok.common.Nsok;

public class MainActivity extends AppCompatActivity {

    private static WebView mNsokWebView = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeWebView();
    }

    private void initializeWebView() {
        mNsokWebView = (WebView) findViewById(R.id.nsok_webView);
        mNsokWebView.setWebChromeClient(new NsokWebChromeClient());
        mNsokWebView.setWebViewClient(new NsokWebViewClient());
        mNsokWebView.getSettings().setJavaScriptEnabled(true);

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
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            result.confirm();
            return true;
        }

        @Override
        public void onConsoleMessage(String message, int lineNumber, String sourceID)
        {
            if(message == null )
                return;
        }


    }

    final class NsokWebViewClient extends WebViewClient {
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
        }
    }
}
