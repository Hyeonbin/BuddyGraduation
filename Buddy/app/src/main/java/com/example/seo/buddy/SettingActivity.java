package com.example.seo.buddy;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.channels.AlreadyBoundException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;

/**
 * Created by Seo on 2016-12-21.
 */
public class SettingActivity extends AppCompatActivity { // 설정을 위한 SettingActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 백 버튼
        ImageView setting_backbtn = (ImageView)findViewById(R.id.Buddy_Setting_Backbtn);

        // 푸시알람 스위치
        Switch setting_alarmswitch = (Switch)findViewById(R.id.Buddy_Setting_Pushalarm);

        // 기타 버튼들(케어센스몰, 아이센스 홈페이지, 문의하기)
        LinearLayout setting_caresensemall = (LinearLayout)findViewById(R.id.Buddy_Setting_Caresensemall);
        LinearLayout setting_isens = (LinearLayout)findViewById(R.id.Buddy_Setting_Isens);
        LinearLayout setting_ask = (LinearLayout)findViewById(R.id.Buddy_Setting_Ask);
        LinearLayout setting_version = (LinearLayout)findViewById(R.id.Buddy_Setting_Version);

        // 알람 on/off 상태를 불러오기 위한 변수들 선언
        final SharedPreferences sharedPreferences = getSharedPreferences("alarm", Activity.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit(); // 알람 on/off 상태 수정을 위한 editor
        final int state = sharedPreferences.getInt("state", 0);
        final AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        // 알람 on/off 상태에 따라 스위치 상태 변경
        if(state == 0)
            setting_alarmswitch.setChecked(false);
        else
            setting_alarmswitch.setChecked(true);

        // 알람 스위치에 대한 클릭 리스너
        setting_alarmswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            Toast toast;
            // on인 경우 -> editor를 통해 상태를 1로 수정
            if(b) {
                editor.putInt("state", 1);
                Intent intent = new Intent(SettingActivity.this, AlarmReceiver.class);
                //PendingIntent pender = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT); // AlarmReceiver로 Broadcast
                PendingIntent[] pender = new PendingIntent[7];
                long oneday = 24 * 60 * 60 * 1000;

                for(int i=0; i<7; i++) {
                    pender[i] = PendingIntent.getBroadcast(SettingActivity.this, i, intent, 0);

                    Calendar calendar = Calendar.getInstance();
                    //Calendar calendar = new GregorianCalendar();
                    //calendar.setTimeInMillis(System.currentTimeMillis());
                    long alarmtime = 0;

                    if (i == 0) {
                        calendar.set(Calendar.HOUR_OF_DAY, 7);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                    } else if (i == 1) {
                        calendar.set(Calendar.HOUR_OF_DAY, 9);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                    } else if (i == 2) {
                        calendar.set(Calendar.HOUR_OF_DAY, 12);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                    } else if (i == 3) {
                        calendar.set(Calendar.HOUR_OF_DAY, 14);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                    } else if (i == 4) {
                        calendar.set(Calendar.HOUR_OF_DAY, 18);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                    } else if (i == 5) {
                        calendar.set(Calendar.HOUR_OF_DAY, 20);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                    } else if (i == 6) {
                        calendar.set(Calendar.HOUR_OF_DAY, 22);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                    }

                    alarmtime = calendar.getTimeInMillis();
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmtime, oneday, pender[i]);
                }
                toast = Toast.makeText(getApplicationContext(), "푸시알람이 설정되었습니다.", Toast.LENGTH_LONG);
            }
            else { // off인 경우 -> editor를 통해 상태를 0으로 수정
                editor.putInt("state", 0);
                Intent intent = new Intent(SettingActivity.this, AlarmReceiver.class);
                PendingIntent[] pender = new PendingIntent[7];
                //PendingIntent pender = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

                for(int i=0; i<7; i++) {
                    pender[i] = PendingIntent.getBroadcast(SettingActivity.this, i, intent, 0);

                    if(pender[0] == null)
                        break;

                    alarmManager.cancel(pender[i]);
                }
                toast = Toast.makeText(getApplicationContext(), "푸시알람이 해제되었습니다.", Toast.LENGTH_LONG);
            }

            editor.commit();
            toast.show();
            }
        });

        setting_caresensemall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 케어센스몰 이동
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://caresensmall.kr"));
                startActivity(intent);
            }
        });

        setting_isens.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 아이센스 이동
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.i-sens.com"));
                startActivity(intent);
            }
        });

        setting_version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast = Toast.makeText(getApplicationContext(), "준비중입니다", Toast.LENGTH_LONG);
                toast.show();
            }
        });

        setting_ask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 문의하기 -> 마켓 올린 후 해도 될듯
                Toast toast = Toast.makeText(getApplicationContext(), "준비중입니다", Toast.LENGTH_LONG);
                toast.show();
            }
        });

        setting_backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.leftin_activity, R.anim.rightout_activity);
            }
        }); // 백 버튼 클릭 리스너
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { // 스마트폰의 버튼 입력에 대한 메소드

        if(keyCode == KeyEvent.KEYCODE_BACK) { // 종료 버튼
            finish();
            overridePendingTransition(R.anim.leftin_activity, R.anim.rightout_activity);

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
