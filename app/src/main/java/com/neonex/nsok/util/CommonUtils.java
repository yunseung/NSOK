package com.neonex.nsok.util;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yun on 2017-05-18.
 */

public class CommonUtils {public static final String TAG = "CommonUtils";
    private static String SMARTHOME_PACKAGE_NAME = "com.skt.sh";


    private static final String mCaptureTempPath = "temp_path";     // 이미지 캡쳐 임시 폴더 명
    private static File mCaptureImageFile = null  ;                 // 캡쳐된 이미지 파일
    private static Uri mCaptureImageUri = null ;                    // 캡쳐된 이미지 파일의 경우

    /**
     * 키보드를 hide 한다.
     * @param context
     * @param view
     */
    public static void hideKeypad(Context context, View view) {
        InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 키보드를 hide -> show, show -> hide 한다.
     * @param context
     */
    public static void toggleKeypad(Context context) {
        InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        im.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

     /**
     * 단문 메시지 발송 App을 런칭한다.(수신자 번호 및 메시지 정보 설정)
     * @param context Context
     * @param phoneNumber Phone number
     * @param message Message
     * @return true 이면 정상 처리된 것이고 그렇지 않으면 오류가 발생된 것임
     */
    public static boolean showSmsClient(Context context, String phoneNumber, String message) {
        if (context == null) {
            return false;
        }

        if (phoneNumber != null && phoneNumber.trim().length() > 0) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(String.format("smsto:%s", phoneNumber)));
            if (message != null && message.length() > 0)
                intent.putExtra("sms_body", message);

            context.startActivity(intent);
            return true;
        }
        return false;
    }

    /**
     * 전화걸기 화면으로 이동
     * @param context Context
     * @param number phone number
     * @return true 이면 정상 처리된 것이고 그렇지 않으면 오류가 발생된 것임
     */
    public static boolean showDialing(Context context, String number) {
        if (context == null) {
            return false;
        }

        if (number != null && number.length() > 0) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(String.format("tel:%s", number)));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        }

        return false;
    }

    /**
     * 단문 메시지 발송 App을 런칭하지 않고 바로 SMS를 보낸다. (수신자 번호 및 메시지 정보 살정)
     * @param context Context
     * @param data 전화번호를 가져올 수 있는 Intent Data (onActivityResult의 결과로 내려온 intent이다.)
     * @param message Message
     * @return true 이면 정상 처리된 것이고 그렇지 않으면 오류가 발생된 것임
     */
    public static boolean sendSmsDirect(Context context, Intent data, String message) {
        String phoneNumber;
        Cursor cursor = context.getContentResolver().query(data.getData(),
                new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER }, null, null, null);
        cursor.moveToFirst();
        phoneNumber = cursor.getString(0);
        cursor.close();

        SmsManager sms = SmsManager.getDefault();
        if (phoneNumber != null && phoneNumber.trim().length() > 0) {
            sms.sendTextMessage(phoneNumber, null, message, null, null);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 단문 메시지 발송 App을 런칭하지 않고 바로 SMS를 보낸다. (수신자 번호 및 메시지 정보 살정)
     * @param context Context
     * @param phoneNumber 전화번호
     * @param message Message
     * @return true 이면 정상 처리된 것이고 그렇지 않으면 오류가 발생된 것임
     */
    public static boolean sendSmsDirect(Context context, String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        if (phoneNumber != null && phoneNumber.trim().length() > 0) {
            sms.sendTextMessage(phoneNumber, null, message, null, null);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 연락처 앱을 호출하여 사용자의 번호를 가져온다. (결과값은 이 함수를 호출한 액티비티의 onActivityResult로 받는다.)
     * @param activity 호출한 액티비티
     */
    public static void getPhoneNumber(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 아이콘 뱃지를 카운트를 올려준다.
     * @param context Context
     * @param className getComponentName().getClassName()
     * @param count badge count
     */
    public static void addBadge(Context context, String className, int count) {
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count_package_name", "com.skt.sh");
        intent.putExtra("badge_count_class_name", className);
        intent.putExtra("badge_count", count);
        context.sendBroadcast(intent);
    }

    /**
     * 아이콘 뱃지를 없앤다. (정책이 나와야 어떤식으로 뱃지를 줄일지..)
     * @param context Context
     * @param className getComponentName().getClassName()
     */
    public static void removeBadge(Context context, String className, int count) {
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count_package_name", "com.skt.sh");
        intent.putExtra("badge_count_class_name", className);
        intent.putExtra("badge_count", 0);
        context.sendBroadcast(intent);
    }

    /**
     * dp를 px로 바꿔줌
     * @param dp
     * @param context
     * @return
     */
    public static int dpToPx(int dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * (metrics.densityDpi / 160f));
    }

    /**
     * 단말의 IMEI를 구해온다.
     * @param context
     * @return
     */
    public static String getDeviceIMEI(Context context) {
        TelephonyManager Info = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        return Info.getDeviceId();
    }

    /**
     * 단말기 전화 번호
     * @param context
     * @return String
     */
    public static String getPhoneNumber(Context context) {
        String phoneNumber = "";

        // MSISDN 추출
        TelephonyManager tpManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        phoneNumber = tpManager.getLine1Number();

        // 국가코드
        if (phoneNumber != null && phoneNumber.length() > 0) {
            phoneNumber = phoneNumber.replace("+82", "0");
        }

        if (phoneNumber == null || phoneNumber.equals("null")) {
            phoneNumber = "";
        }

        return phoneNumber;
    }

    public static boolean isUsimExist(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE); // gets the
        // current
        // TelephonyManager
        if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
            return true;
        }

        return false;
    }

     /**
     * 버전별 해상도
     * @return point
     */
    @SuppressWarnings("deprecation")
    public static Point getScreenSize(Context context) {
        Display dis = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        dis.getMetrics(metrics);

        Point point = new Point();

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            dis.getSize(point);
        } else {
            point.x = dis.getWidth();
            point.y = dis.getHeight();
        }

        return point;
    }

    /**
     * String 날짜 포메터.
     * @param dateStr String 날짜
     * @param curDateFormat 현재 date format
     * @param newDateFormat 바꿀 date format
     * @return String 날짜
     * @throws ParseException
     */
    public static String convertDateFormat(String dateStr, String curDateFormat, String newDateFormat)
            throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(curDateFormat);
        Date date = sdf.parse(dateStr);
        sdf = new SimpleDateFormat(newDateFormat);
        return sdf.format(date);
    }

    // 이메일 마스킹 함수
    public static String idEncrypt(String id) {
        StringBuilder result = new StringBuilder();

        try {
            String[] emailTokens = id.split("@");
            String idOfEmail = emailTokens[0];
            String addressOfEmail = emailTokens[1];

            for (int inx = 0; inx < idOfEmail.toCharArray().length; inx++) {
                if (inx < 3)
                    result.append(idOfEmail.charAt(inx));
                else
                    result.append("*");
            }

            result.append("@");
            result.append(addressOfEmail);
        } catch (Exception e) {
            e.printStackTrace();
            result.append(id);
        }

        return result.toString();
    }

    /**
     * 외부 웹 브라우저 열기
     * @param ctx Context
     * @param url web url
     * @return true 이면 정상 처리된 것이고 그렇지 않으면 오류가 발생된 것임
     */
    public static boolean showBrowser(Context ctx, String url) {
        if (null == ctx)
            return false;

        try {

            if (url != null && url.length() > 0) {
                Intent mWebIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mWebIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(mWebIntent);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * 임의의 븅아이디 생성
     *
     * @return
     */
    public static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    /**
     * 활성화 되어있는 현재 앱프로세스를 완전종료한다.
     *
     * @param context
     *            :앱 자원
     */
    public static void killAppProcess(final Context context) {

        int sdkVersion = Integer.parseInt(Build.VERSION.SDK);

        if (sdkVersion < 8) {
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            am.restartPackage(context.getPackageName());
        } else {
            new Thread(new Runnable() {

                public void run() {
                    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                    String name = context.getApplicationInfo().processName;

                    while (true) {
                        List<ActivityManager.RunningAppProcessInfo> list = am
                                .getRunningAppProcesses();
                        for (ActivityManager.RunningAppProcessInfo i : list) {
                            if (i.processName.equals(name) == true) {
                                if (i.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND)
                                    am.restartPackage(context.getPackageName());
                                else
                                    Thread.yield();
                                break;
                            }
                        }
                    }
                }
            }, "Process Killer").start();
        }
    }

    public static String getDateFirst() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyyMM", Locale.getDefault());
        Date date = new Date();
        return (dateFormat.format(date)).concat("01");
    }

    public static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyyMMddHHmmss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getDateTime2() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getDateTime3() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getDateTime4() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getDateTime5() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "HH:mm", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getDateTime6() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyyMMdd", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getTimeHour() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "HH", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getTimeMinute() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "mm", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }


    public static String getDate(String splintChar) {

        String format = "yyyy" + splintChar + "MM" + splintChar + "dd";

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                format, Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }


    /**
     * 캡쳐된 이미지 파일명 가져오기
     *
     * @return
     */
    public static File getCaptureImageFile(){
        return mCaptureImageFile;
    }

    /**
     * 촬영한 이미지 파일의 임시 저장소
     *
     * @return
     */
    private static void setCaptureImageFile(Context context){
        //folder stuff
        File imagesFolder = new File(Environment.getExternalStorageDirectory(), mCaptureTempPath);
        if (!imagesFolder.exists())
            imagesFolder.mkdirs();

        String timeStamp = CommonUtils.getDate("");

        mCaptureImageFile = new File(imagesFolder, mCaptureTempPath + "_" + timeStamp + ".png");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mCaptureImageUri = FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider",
                    mCaptureImageFile);
        }
        else {
            mCaptureImageUri = Uri.fromFile(mCaptureImageFile);
        }
//        mCaptureImageUri = Uri.fromFile(mCaptureImageFile);




    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /**
     * 토스트 메시지 보이기
     *
     * @param context
     * @param message
     */
    public static void showToast(Context context , String message){
        Toast.makeText(context , message , Toast.LENGTH_SHORT).show();
    }

    public static void hideKeyBoard(AppCompatActivity context) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (context.getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Image SDCard Save (input Bitmap -> saved file JPEG)
     * Writer intruder(Kwangseob Kim)
     * @param bitmap : input bitmap file
     */
    public static File saveBitmapToFileCache(Bitmap bitmap){

        String folder = "temp";
        String name = "file_" + System.currentTimeMillis();
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard
        String foler_name = "/"+folder+"/";
        String file_name = name+".jpg";
        String string_path = ex_storage+foler_name;

        File file_path;
        try{
            file_path = new File(string_path);
            if(!file_path.isDirectory()){
                file_path.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(string_path+file_name);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();

            return new File(string_path+file_name) ;
        }catch(FileNotFoundException exception){
            Log.e("TAG"  , exception.getMessage() , exception);
        }catch(IOException exception){
            Log.e("TAG"  , exception.getMessage() , exception);
        }

        return null ;
    }

    public static void deleteFile(File file){

        if (null != file){
            if (file.isFile()){
                file.delete();
            }
        }
    }

    public static File getByteToApkFile(final byte[] bytes , String fileName) {

        String folder = "temp";

        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        String foler_name = "/" + folder + "/";

        String string_path = ex_storage + foler_name;

        File file_path;
        try {
            file_path = new File(string_path);
            if (!file_path.isDirectory()) {
                file_path.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(string_path + fileName);
            out.write(bytes);
            out.close();

            return new File(string_path + fileName);

        } catch (FileNotFoundException exception) {
            Log.e("TAG", exception.getMessage(), exception);
        } catch (IOException exception) {
            Log.e("TAG", exception.getMessage(), exception);
        }

        return null;
    }

    public static String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }
}
