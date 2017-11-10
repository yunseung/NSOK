package com.neonex.nsok.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.neonex.nsok.broadcast.RestartService;
import com.neonex.nsok.common.ReceiverEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by yun on 2017-11-10.
 */

public class PersistentService extends Service implements Runnable {

    // 서비스 종료시 재부팅 딜레이 시간, activity의 활성 시간을 벌어야 한다.
    private static final int REBOOT_DELAY_TIMER = 10 * 1000;

    private int mStartId = 0;

    private String mPushData = null;

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {

        Log.e("PersistentService", "onCreate()");
        // 등록된 알람은 제거
        unregisterRestartAlarm();

        EventBus.getDefault().register(this);
        super.onCreate();

    }

    @Override
    public void onDestroy() {

        // 서비스가 죽었을때 알람 등록
        registerRestartAlarm();

        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    /**
     * (non-Javadoc)
     * @see android.app.Service#onStart(android.content.Intent, int)
     *
     * 서비스가 시작되었을때 run()이 실행되기까지 delay를 handler를 통해서 주고 있다.
     */
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

    }

    /**
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     *
     * 서비스가 돌아가고 있을때 실제로 내가 원하는 기능을 구현하는 부분
     */
    @Override
    public void run() {

    }

    @Subscribe
    public void onEventFromReceiver(ReceiverEvent event) {
        mPushData = event.message;
        Log.e("mPushData", "mPushData : " + mPushData);
    }

    /**
     * 서비스가 시스템에 의해서 또는 강제적으로 종료되었을 때 호출되어
     * 알람을 등록해서 10초 후에 서비스가 실행되도록 한다.
     */
    private void registerRestartAlarm() {

        Intent intent = new Intent(PersistentService.this, RestartService.class);
        intent.setAction(RestartService.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this, 0, intent, 0);

        long firstTime = SystemClock.elapsedRealtime();
        firstTime += REBOOT_DELAY_TIMER; // 10초 후에 알람이벤트 발생

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,REBOOT_DELAY_TIMER, sender);
    }


    /**
     * 기존 등록되어있는 알람을 해제한다.
     */
    private void unregisterRestartAlarm() {
        Intent intent = new Intent(PersistentService.this, RestartService.class);
        intent.setAction(RestartService.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this, 0, intent, 0);

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }
}
