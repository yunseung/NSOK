package com.neonex.nsok.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.neonex.nsok.R;
import com.neonex.nsok.common.Nsok;
import com.neonex.nsok.common.ReceiverEvent;
import com.neonex.nsok.javascript.NsokBridge;
import com.neonex.nsok.util.AppExitPreventUtil;
import com.neonex.nsok.util.NsokPreferences;
import com.neonex.nsok.util.RealPathUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static WebView mNsokWebView = null;                                 // NSOK WebView
    private SwipeRefreshLayout mSwipeRefreshLayout = null;                      // 아래로 당겨서 새로고침 레이아웃
    private String mAddress = Nsok.connWebUrl + "/mobile/index.do";

    private NsokBridge mNsokBridge = null;
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;

    private AppExitPreventUtil mAppExitPreventHandler = null;                   // 뒤로가기 한 번에 앱 죽이는 것 방지 핸들러

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermissionMarshmallow(); // 마시멜로우 권한 확인

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            Set<String> keyset = bundle.keySet();
            for (Iterator<String> it = keyset.iterator(); it.hasNext(); ) {
                String targetUrl = it.next();
                if (targetUrl.equals("targetUrl")) {
                    mAddress = bundle.get("targetUrl").toString();
                }
            }
        }

        mNsokBridge = new NsokBridge(this);
        mNsokBridge.setOnFcmTokenSendListener(new NsokBridge.OnFcmTokenSendListener() {
            @Override
            public void sendFcmToken() {
                mNsokWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("NSOK PUSH KEY", "key is : " + NsokPreferences.getFcmToken(MainActivity.this));
                        mNsokWebView.loadUrl("javascript:getFcmToken('" + NsokPreferences.getFcmToken(MainActivity.this) + "');");
                    }
                });
            }
        });

        initializeWebView();

        mAppExitPreventHandler = new AppExitPreventUtil(this);
    }

    /**
     * 마시멜로우 권한 체크
     *
     */
    private void getPermissionMarshmallow(){


        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                finish();
            }
        };

        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setRationaleMessage(getString(R.string.and_ver_m_permission_message_1))
//                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setGotoSettingButtonText("setting").setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE )
                .check();
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
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            Set<String> keyset = bundle.keySet();
            for (Iterator<String> it = keyset.iterator(); it.hasNext(); ) {
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
        mNsokWebView.setDownloadListener(mDownloadListener);
        mNsokWebView.setScrollContainer(true);
        mNsokWebView.addJavascriptInterface(mNsokBridge, "nsokBridge");
        mNsokWebView.clearView();
        mNsokWebView.clearHistory();
        mNsokWebView.requestFocus();
        mNsokWebView.requestFocusFromTouch();
        mNsokWebView.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mNsokWebView.getSettings().setTextZoom(100);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            mNsokWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
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
        mAddress = event.message;
    }

    private DownloadListener mDownloadListener = new DownloadListener() {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file");
                String fileName = contentDisposition.replace("inline; filename=", "");
                fileName = fileName.replaceAll("\"", "");
                request.setTitle(fileName);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
            } catch (Exception e) {

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Toast.makeText(getBaseContext(), "첨부파일 다운로드를 위해\n동의가 필요합니다.", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                110);
                    } else {
                        Toast.makeText(getBaseContext(), "첨부파일 다운로드를 위해\n동의가 필요합니다.", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                110);
                    }
                }
            }
        }
    };

    final class NsokWebChromeClient extends WebChromeClient {

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            final WebSettings settings = view.getSettings();
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptEnabled(true);
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            view.setWebChromeClient(this);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(view);
            resultMsg.sendToTarget();
            return false;
        }

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
            intent.setType("video/*, image/*");
            startActivityForResult(intent, INPUT_FILE_REQUEST_CODE);
        }

        // For 3.0 <= Android Version < 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooser(uploadMsg, acceptType, "");
        }

        // For 4.1 <= Android Version < 5.0
        public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
            mUploadMessage = uploadFile;
            imageChooser();
        }

        // For Android Version 5.0+
        // Ref: https://github.com/GoogleChrome/chromium-webview-samples/blob/master/input-file-example/app/src/main/java/inputfilesample/android/chrome/google/com/inputfilesample/MainFragment.java
        public boolean onShowFileChooser(WebView webView,
                                         ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
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
            contentSelectionIntent.setType("*/*");

//            Intent[] intentArray;
//            if (takePictureIntent != null) {
//                intentArray = new Intent[]{takePictureIntent};
//            } else {
//                intentArray = new Intent[0];
//            }
//
//            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
//            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
//            chooserIntent.putExtra(Intent.EXTRA_TITLE, "뭐라고 쓰지?");
//            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            startActivityForResult(contentSelectionIntent, INPUT_FILE_REQUEST_CODE);
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
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(R.string.notification_error_ssl_cert_invalid);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton(R.string.nok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }

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
