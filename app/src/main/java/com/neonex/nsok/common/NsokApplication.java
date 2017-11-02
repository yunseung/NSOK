package com.neonex.nsok.common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by yun on 2017-09-22.
 */

public class NsokApplication extends Application {

    private static Context mApplicationContext = null; // 전역 컨텍스트
    private static Activity mCurrentActivity = null;
    private static Bundle mBundle = null;

    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.builder()
                .logNoSubscriberMessages(false)
                .sendNoSubscriberEvent(false)
                .throwSubscriberException(false)
                .installDefaultEventBus();
    }
}

