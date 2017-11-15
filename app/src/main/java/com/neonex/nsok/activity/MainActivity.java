package com.neonex.nsok.activity;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.neonex.nsok.R;
import com.neonex.nsok.common.Nsok;
import com.neonex.nsok.common.ReceiverEvent;
import com.neonex.nsok.javascript.NsokBridge;
import com.neonex.nsok.service.PersistentService;
import com.neonex.nsok.util.AppExitPreventUtil;
import com.neonex.nsok.util.NsokPreferences;
import com.neonex.nsok.util.RealPathUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static WebView mNsokWebView = null;                                 // NSOK WebView
    private SwipeRefreshLayout mSwipeRefreshLayout = null;                      // 아래로 당겨서 새로고침 레이아웃
    private String mAddress = Nsok.connWebUrl;

    private NsokBridge mNsokBridge = null;
    private String mPushData = null;

    private static final String TYPE_IMAGE = "image/*";
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;

    private BroadcastReceiver mBroadcastReceiver = null;
    private Intent mIntentService = null;

    private AppExitPreventUtil mAppExitPreventHandler = null;                   // 뒤로가기 한 번에 앱 죽이는 것 방지 핸들러

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            Set<String> keyset = bundle.keySet();
            for (Iterator<String> it = keyset.iterator(); it.hasNext();) {
                String targetUrl = it.next();
                if (targetUrl.equals("targetUrl")) {
                    mAddress = bundle.get("targetUrl").toString();
                }
            }
        }

//        if (getIntent() != null) {
//            if (getIntent().getExtras().get("targetUrl") != null) {
//                Log.e("onCreate", "제발 ㅠㅠ : " + getIntent().getExtras().get("targetUrl").toString());
//                mAddress = getIntent().getExtras().get("targetUrl").toString();
//            }
//        }

        // Register Immortal Service
//        mIntentService = new Intent(this, PersistentService.class);
//
//        try {
//            IntentFilter filter = new IntentFilter("com.nsok.push");
//            registerReceiver(mBroadcastReceiver, filter);
//            startService(mIntentService);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // Broadcast 로 fcm token 받기
        IntentFilter tokenIntentFilter = new IntentFilter();
        tokenIntentFilter.addAction("com.nsok.fcm.FCM_TOKEN");

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NsokPreferences.setFcmToken(MainActivity.this, intent.getStringExtra("token"));
            }
        }, tokenIntentFilter);

        mNsokBridge = new NsokBridge(this);
        mNsokBridge.setOnFcmTokenSendListener(new NsokBridge.OnFcmTokenSendListener() {
            @Override
            public void sendFcmToken() {
                mNsokWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("push key", "push key : " + NsokPreferences.getFcmToken(MainActivity.this));
                        mNsokWebView.loadUrl("javascript:getFcmToken('" + NsokPreferences.getFcmToken(MainActivity.this) + "');");
                    }
                });
            }
        });

        initializeWebView();

        mAppExitPreventHandler = new AppExitPreventUtil(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
//        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            Set<String> keyset = bundle.keySet();
            for (Iterator<String> it = keyset.iterator(); it.hasNext();) {
                String targetUrl = it.next();
                if (targetUrl.equals("targetUrl")) {
                    mAddress = bundle.get("targetUrl").toString();
                }
            }
        }

        mNsokWebView.post(new Runnable() {
            @Override
            public void run() {
                mNsokWebView.loadUrl(mAddress);
            }
        });
    }

    private void initializeWebView() {
        mNsokWebView = (WebView) findViewById(R.id.nsok_webView);
        mNsokWebView.setWebChromeClient(new NsokWebChromeClient());
        mNsokWebView.setWebViewClient(new NsokWebViewClient());
        mNsokWebView.setScrollContainer(true);
        mNsokWebView.addJavascriptInterface(mNsokBridge, "nsokBridge");
        mNsokWebView.clearView();
        mNsokWebView.clearHistory();
        mNsokWebView.requestFocus();
        mNsokWebView.requestFocusFromTouch();
        mNsokWebView.getSettings().setJavaScriptEnabled(true);
        mNsokWebView.loadUrl(mAddress);


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

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessage(ReceiverEvent event) {
        Log.e("EventBus", "EventBus data : " + event.message);
        mAddress = event.message;
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

        // For Android Version < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            //System.out.println("WebViewActivity OS Version : " + Build.VERSION.SDK_INT + "\t openFC(VCU), n=1");
            mUploadMessage = uploadMsg;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(TYPE_IMAGE);
            startActivityForResult(intent, INPUT_FILE_REQUEST_CODE);
        }

        // For 3.0 <= Android Version < 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            //System.out.println("WebViewActivity 3<A<4.1, OS Version : " + Build.VERSION.SDK_INT + "\t openFC(VCU,aT), n=2");
            openFileChooser(uploadMsg, acceptType, "");
        }

        // For 4.1 <= Android Version < 5.0
        public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
            Log.d(getClass().getName(), "openFileChooser : " + acceptType + "/" + capture);
            mUploadMessage = uploadFile;
            imageChooser();
        }

        // For Android Version 5.0+
        // Ref: https://github.com/GoogleChrome/chromium-webview-samples/blob/master/input-file-example/app/src/main/java/inputfilesample/android/chrome/google/com/inputfilesample/MainFragment.java
        public boolean onShowFileChooser(WebView webView,
                                         ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            System.out.println("WebViewActivity A>5, OS Version : " + Build.VERSION.SDK_INT + "\t onSFC(WV,VCUB,FCP), n=3");
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePathCallback;
            imageChooser();
            return true;
        }

        private void imageChooser() {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.e(getClass().getName(), "Unable to create Image File", ex);
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType(TYPE_IMAGE);

            Intent[] intentArray;
            if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INPUT_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mFilePathCallback == null) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }
                Uri[] results = new Uri[]{getResultUri(data)};

                mFilePathCallback.onReceiveValue(results);
                mFilePathCallback = null;
            } else {
                if (mUploadMessage == null) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }
                Uri result = getResultUri(data);
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        } else {
            if (mFilePathCallback != null) mFilePathCallback.onReceiveValue(null);
            if (mUploadMessage != null) mUploadMessage.onReceiveValue(null);
            mFilePathCallback = null;
            mUploadMessage = null;
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Uri getResultUri(Intent data) {
        Uri result = null;
        if (data == null || TextUtils.isEmpty(data.getDataString())) {
            // If there is not data, then we may have taken a photo
            if (mCameraPhotoPath != null) {
                result = Uri.parse(mCameraPhotoPath);
            }
        } else {
            String filePath = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                filePath = data.getDataString();
            } else {
                filePath = "file:" + RealPathUtil.getRealPath(this, data.getData());
            }
            result = Uri.parse(filePath);
        }

        return result;
    }

    final class NsokWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            mNsokWebView.invalidate();
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }


        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    @Override
    public void onBackPressed() {

        if (mNsokWebView.canGoBack() && !mNsokWebView.getUrl().contains("/mobile/main.do")
                && !mNsokWebView.getUrl().contains("/login/loginForm.do")) {
            mNsokWebView.goBack();
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                mAppExitPreventHandler.onBackPressed();
            } else {
                super.onBackPressed();
            }
        }
    }
}
