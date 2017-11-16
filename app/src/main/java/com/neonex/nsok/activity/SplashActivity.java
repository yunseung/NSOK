package com.neonex.nsok.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.neonex.nsok.R;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by yun on 2017-11-16.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            Set<String> keyset = bundle.keySet();
            for (Iterator<String> it = keyset.iterator(); it.hasNext(); ) {
                String targetUrl = it.next();
                if (targetUrl.equals("targetUrl")) {
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    i.putExtra("targetUrl", bundle.get("targetUrl").toString());
                    startActivity(i);
                    finish();
                    return;
                }
            }
        }

        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 1500);

    }

    @Override
    public void onBackPressed() {
        return;
    }
}
