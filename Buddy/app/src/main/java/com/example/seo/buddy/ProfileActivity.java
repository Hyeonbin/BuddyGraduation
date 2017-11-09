package com.example.seo.buddy;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Seo on 2016-12-22.
 */
public class ProfileActivity extends AppCompatActivity { // Profile을 나타내기 위한 ProfileActivity

    // Profile 데이터베이스에 접근하기 위한 변수들
    private Cursor profilecursor = null;
    private ProfileDBHelper profiledbhelper;
    private SQLiteDatabase profiledb;
    private String profilesql;

    public static Activity CurrentActivity; // 현재 액티비티를 나타내기 위한 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        CurrentActivity = this; // 현재 액티비티로 정의

        ImageView profile_backbtn = (ImageView)findViewById(R.id.Buddy_Profile_Backbtn); // 백버튼 선언
        CircleImageView profile_photo = (CircleImageView) findViewById(R.id.Buddy_Profile_Photo); // 프로필 사진 Imageview

        // 이름과 생년월일 textview
        TextView profile_name = (TextView)findViewById(R.id.Buddy_Profile_Name);
        TextView profile_old = (TextView)findViewById(R.id.Buddy_Profile_Old);

        // 성별 textview
        TextView profile_gender = (TextView)findViewById(R.id.Buddy_Profile_Genderbtn);

        // 프로필 입력 버튼
        Button profile_inputbtn = (Button)findViewById(R.id.Buddy_Profile_Inputbtn);

        // Profile 데이터베이스에 접근하기 위한 변수들 셋팅
        profiledbhelper = new ProfileDBHelper(this);
        profiledb = profiledbhelper.getReadableDatabase();
        profilesql = "SELECT * FROM profiletable;";
        profilecursor = profiledb.rawQuery(profilesql, null);

        if(profilecursor.getCount() > 0) { // 프로필 데이터베이스에 데이터가 존재할 경우 동작 진행
            profilecursor.moveToFirst();

            // 데이터들 변수들에 저장
            String curphoto = profilecursor.getString(1);
            String curname = profilecursor.getString(2);
            String curold = profilecursor.getString(3);
            String curgender = profilecursor.getString(4);

            if(curphoto != null) { // 사진 데이터가 있는 경우
                try {
                    File file = new File(curphoto);
                    Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath()); // 비트맵으로 변환하여 나타낸다
                    profile_photo.setImageBitmap(bm);
                } catch (Exception e) {
                    Log.e("Photo", "Error: " + e);
                }
            } else { // 없는 경우 -> 기본 프로필 그림 보여준다
                profile_photo.setImageDrawable(getResources().getDrawable(R.drawable.icon_profile, null));
            }

            // 이름과 생년월일 입력
            profile_name.setText(curname);
            profile_old.setText(curold);

            // 성별에 따라 색과 글자 변경
            if (curgender.equals("Man")) {
                profile_gender.setText("남");
                profile_gender.setBackgroundColor(Color.parseColor("#93DAFF"));
            } else {
                profile_gender.setText("여");
                profile_gender.setBackgroundColor(Color.parseColor("#FF98A3"));
            }
        }

        profilecursor.close();
        profiledbhelper.close();

        // 백버튼에 대한 클릭 리스너
        profile_backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.rightin_activity, R.anim.leftout_activity);
            }
        });

        // 입력 버튼에 대한 클릭 리스너
        profile_inputbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, NewProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { // 스마트폰의 버튼 입력에 대한 메소드

        if(keyCode == KeyEvent.KEYCODE_BACK) { // 종료 버튼
            finish();
            overridePendingTransition(R.anim.rightin_activity, R.anim.leftout_activity);

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
