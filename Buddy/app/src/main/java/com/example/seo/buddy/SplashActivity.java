package com.example.seo.buddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by Seo on 2016-12-19.
 */
public class SplashActivity extends Activity { // 맨 처음 스플래쉬 화면을 보여주는 SplashActivity

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() { // 스플래쉬 화면을 보여주는 핸들러, 꺼진 후 MainActivity로 이동
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, 3000); // 3초 동안 진행
    }
}
