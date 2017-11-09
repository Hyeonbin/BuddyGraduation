package com.example.seo.buddy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Seo on 2017-02-03.
 */
public class AlarmReceiver extends BroadcastReceiver { // 푸시알람을 받기 위한 Alarmreceiver

    long bb, ba, lb, la, db, da, sleep;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent mainintent = new Intent(context, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainintent, PendingIntent.FLAG_UPDATE_CURRENT); // 앱 외부에서 실행하기 위한 pendingintent

        Notification.Builder builder = new Notification.Builder(context); // 푸시알람을 위한 notification 변수 할당

        Calendar current = Calendar.getInstance();
        //current.setTimeInMillis(System.currentTimeMillis());

        // 푸시알람 아이템들 셋팅
        builder.setSmallIcon(R.drawable.heart_white);
        builder.setTicker("Buddy");
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle("Buddy");
        // 현재 시간에 따라 해당하는 푸시알람 출력
        if(current.get(Calendar.HOUR_OF_DAY) == 7){
            builder.setContentText("아침 식사 전에 혈당체크 잊지마세요!");
        } else if(current.get(Calendar.HOUR_OF_DAY) == 9){
            builder.setContentText("아침 식사 잘 하셨나요? 식사 후 혈당체크 잊지마세요!");
        } else if(current.get(Calendar.HOUR_OF_DAY) == 12){
            builder.setContentText("점심 식사 전에 혈당체크 잊지마세요!");
        } else if(current.get(Calendar.HOUR_OF_DAY) == 14){
            builder.setContentText("점심 식사 잘 하셨나요? 식사 후 혈당체크 잊지마세요!");
        } else if(current.get(Calendar.HOUR_OF_DAY) == 18){
            builder.setContentText("저녁 식사 전에 혈당체크 잊지마세요!");
        } else if(current.get(Calendar.HOUR_OF_DAY) == 20){
            builder.setContentText("저녁 식사 잘 하셨나요? 식사 후 혈당체크 잊지마세요!");
        } else if(current.get(Calendar.HOUR_OF_DAY) == 22){
            builder.setContentText("취침 전에 혈당체크 잊지마세요!");
        }
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE); // 소리와 진동 가능하도록 셋팅
        builder.setContentIntent(pendingIntent); // 푸시알람을 눌렀을 경우 MainActivity로 이동
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_MAX); // 우선순위 가장 높게

        // Manager를 통해 푸시알람 build
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(current.get(Calendar.HOUR_OF_DAY) == 7 || current.get(Calendar.HOUR_OF_DAY) == 9 || current.get(Calendar.HOUR_OF_DAY) == 12 ||
                current.get(Calendar.HOUR_OF_DAY) == 14 || current.get(Calendar.HOUR_OF_DAY) == 18 || current.get(Calendar.HOUR_OF_DAY) == 20 ||
                current.get(Calendar.HOUR_OF_DAY) == 22) {
            nm.notify(12345, builder.build());
        }
    }


}
