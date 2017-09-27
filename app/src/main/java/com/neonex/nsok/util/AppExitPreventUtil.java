package com.neonex.nsok.util;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.neonex.nsok.R;

/**
 * Created by yun on 2017-09-25.
 */

public class AppExitPreventUtil {

    int delayTime = 2500;
    private long backKeyPressedTime = 0;
    private Toast toast;

    private Context mContext ;

    public AppExitPreventUtil(Context _context) {
        mContext = _context;
    }


    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + delayTime) { //백버튼 한번 눌렀을때 가이드 보여주기
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + delayTime) { // 두번 눌렀을때 종료 처리

            CommonUtils.killAppProcess(mContext);
            ((AppCompatActivity) mContext).finish();

            toast.cancel();
        }
    }

    private void showGuide() {
        toast = Toast.makeText(mContext, mContext.getResources().getString(R.string.app_exit_message) , Toast.LENGTH_LONG);
        toast.show();
    }
}
