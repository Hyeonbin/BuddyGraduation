package com.example.seo.buddy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * Created by Seo on 2017-01-29.
 */
public class SearchDetailActivity extends AppCompatActivity { // SearchActivity에서 클릭한 아이템에 대한 정보를 보여주는 SearchDetailActivity

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchdetail);

        // 데이터를 받기 위한 변수들 선언
        int hour = 0, min = 0, meal = 0;
        String ampm, location, photo, glucose, diary;

        // SearchActivity로 부터 데이터를 받는다
        hour = getIntent().getIntExtra("hour", -1);
        min = getIntent().getIntExtra("min", -1);
        meal = getIntent().getIntExtra("meal", -1);
        ampm = getIntent().getStringExtra("ampm");
        location = getIntent().getStringExtra("location");
        photo = getIntent().getStringExtra("photo");
        glucose = getIntent().getStringExtra("glucose");
        diary = getIntent().getStringExtra("diary");

        // 혈당 정보에 대한 layout
        LinearLayout detail_glucoselayout = (LinearLayout)findViewById(R.id.Buddy_Searchdetail_Glucoselayout);
        LinearLayout detail_detaillayout = (LinearLayout)findViewById(R.id.Buddy_Searchdetail_Glucosedetaillayout);

        // 백 버튼
        ImageView detail_backbtn = (ImageView)findViewById(R.id.Buddy_Searchdetail_Backbtn);

        // 시간, 장소 등에 대한 변수들
        TextView detail_hour = (TextView)findViewById(R.id.Buddy_Searchdetail_Hour);
        TextView detail_min = (TextView)findViewById(R.id.Buddy_Searchdetail_Min);
        TextView detail_ampm = (TextView)findViewById(R.id.Buddy_Searchdetail_Ampm);
        TextView detail_location = (TextView)findViewById(R.id.Buddy_Searchdetail_Location);
        ImageView detail_photo = (ImageView)findViewById(R.id.Buddy_Searchdetail_Photo);
        TextView detail_glucose = (TextView)findViewById(R.id.Buddy_Searchdetail_Glucose);
        TextView detail_glucoseunit = (TextView)findViewById(R.id.Buddy_Searchdetail_Glucoseunit);
        TextView detail_diary = (TextView)findViewById(R.id.Buddy_Searchdetail_Diary);

        // 식사 정보 변수
        TextView detail_meal = (TextView)findViewById(R.id.Buddy_Searchdetail_Meal);

        // 백버튼에 대한 클릭 리스너
        detail_backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if(meal == 0) // 식사 정보가 없으면 혈당 layout 보여주지 않는다
            detail_glucoselayout.setVisibility(View.GONE);

        // 각 변수들에 데이터 삽입 -> 다른 Activity들의 데이터 셋팅과 동일
        if(hour == 0)
            detail_hour.setText("12");
        else
            detail_hour.setText(""+hour);
        if(String.valueOf(min).length() == 1)
            detail_min.setText("0"+min);
        else
            detail_min.setText(""+min);
        detail_ampm.setText(ampm);
        detail_location.setText(location);
        if(photo != null) {
            try { // 사진은 Glide 라이브러리를 이용하여
                //File file = new File(photo);
                //Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
                //Bitmap resized = Bitmap.createScaledBitmap(bm, bm.getWidth() * 5, bm.getHeight() * 5, true);
                Glide.with(this).load(photo).thumbnail(0.1f).override(600, 700).centerCrop().into(detail_photo);
                //detail_photo.setImageBitmap(resized);
            } catch (Exception e) {
                Log.e("Photo", "Error: " + e);
            }
        } /*else
            child_photo.setVisibility(View.GONE);*/ // 편법이지만 일단 주석처리
        if(meal == 1) // 식사 정보 textview 셋팅
            detail_meal.setText("아침 식사 전");
        else if(meal == 2)
            detail_meal.setText("아침 식사 후");
        else if(meal == 3)
            detail_meal.setText("점심 식사 전");
        else if(meal == 4)
            detail_meal.setText("점심 식사 후");
        else if(meal == 5)
            detail_meal.setText("저녁 식사 전");
        else if(meal == 6)
            detail_meal.setText("저녁 식사 후");
        else if(meal == 7)
            detail_meal.setText("취침 전");
        detail_glucose.setText(glucose);
        int gvalue = Integer.parseInt(glucose);
        if(gvalue < 70) {
            detail_glucose.setTextColor(Color.parseColor("#CD1039"));
            detail_glucoseunit.setTextColor(Color.parseColor("#CD1039"));
            //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#CD1039"));
        }
        else {
            if (meal == 1 || meal == 3 || meal == 5) {
                if(gvalue < 100) {
                    detail_glucose.setTextColor(Color.parseColor("#51FFA6"));
                    detail_glucoseunit.setTextColor(Color.parseColor("#51FFA6"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#51FFA6"));
                }
                else if(gvalue >= 100 && gvalue < 126) {
                    detail_glucose.setTextColor(Color.parseColor("#FFDC3C"));
                    detail_glucoseunit.setTextColor(Color.parseColor("#FFDC3C"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#FFF064"));
                }
                else if(gvalue >= 126) {
                    detail_glucose.setTextColor(Color.parseColor("#CD1039"));
                    detail_glucoseunit.setTextColor(Color.parseColor("#CD1039"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#CD1039"));
                }
            } else if (meal == 2 || meal == 4 || meal == 6) {
                if(gvalue < 140) {
                    detail_glucose.setTextColor(Color.parseColor("#51FFA6"));
                    detail_glucoseunit.setTextColor(Color.parseColor("#51FFA6"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#51FFA6"));
                }
                else if(gvalue >= 140 && gvalue < 200) {
                    detail_glucose.setTextColor(Color.parseColor("#FFDC3C"));
                    detail_glucoseunit.setTextColor(Color.parseColor("#FFDC3C"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#FFF064"));
                }
                else if(gvalue >= 200) {
                    detail_glucose.setTextColor(Color.parseColor("#CD1039"));
                    detail_glucoseunit.setTextColor(Color.parseColor("#CD1039"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#CD1039"));
                }
            } else if (meal == 7) {
                if(gvalue < 120) {
                    detail_glucose.setTextColor(Color.parseColor("#51FFA6"));
                    detail_glucoseunit.setTextColor(Color.parseColor("#51FFA6"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#51FFA6"));
                }
                else if(gvalue >= 100 && gvalue < 160) {
                    detail_glucose.setTextColor(Color.parseColor("#FFDC3C"));
                    detail_glucoseunit.setTextColor(Color.parseColor("#FFDC3C"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#FFF064"));
                }
                else if(gvalue >= 160) {
                    detail_glucose.setTextColor(Color.parseColor("#CD1039"));
                    detail_glucoseunit.setTextColor(Color.parseColor("#CD1039"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#CD1039"));
                }
            }
        }
        detail_diary.setText(diary);
    }
}
