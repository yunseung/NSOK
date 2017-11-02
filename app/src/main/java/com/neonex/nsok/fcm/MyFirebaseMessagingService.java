package com.neonex.nsok.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.neonex.nsok.R;
import com.neonex.nsok.activity.MainActivity;
import com.neonex.nsok.common.ReceiverEvent;
import com.neonex.nsok.util.CommonUtils;
import com.neonex.nsok.util.NsokLog;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Map;


/**
 * Created by n on 2017-04-25.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getNotification() != null) {

            {
                NsokLog.d("dataChat",remoteMessage.getData().toString());
                try
                {
                    Map<String, String> params = remoteMessage.getData();
                    JSONObject object = new JSONObject(params);
                    NsokLog.d("JSON_OBJECT", object.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            EventBus.getDefault().post(new ReceiverEvent(remoteMessage.getData().get("targetUrl").toString()));

            Handler handler = new Handler(getMainLooper(), new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {


                    return false;
                }
            });

            handler.sendEmptyMessage(0);


        }
        sendPushNotification(remoteMessage.getData().get("message"));
    }

    private void sendPushNotification(String message) {

        NsokLog.d(TAG , "received push message : " + message);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.support.v4.app.NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher) )
                .setContentTitle("Push Title ")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri).setLights(000000255,500,2000)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
        wakelock.acquire(3000);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}
