package com.example.seo.buddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Seo on 2017-01-29.
 */
public class FabActivity extends AppCompatActivity { // LogbookActivity의 floating action button을 위한 FabActivity

    LogbookActivity logbookActivity = (LogbookActivity)LogbookActivity.currentfragment; // LogbookActivity에 접근하기 위한 logbookActivity 변수

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fab);

        // 레이아웃 선언
        RelativeLayout fab_layout = (RelativeLayout) findViewById(R.id.Buddy_Fab_Layout);

        TextView fab_adddiary = (TextView) findViewById(R.id.Buddy_Fab_Adddiary); // 다이어리 추가를 위한 버튼
        TextView fab_deldiary = (TextView) findViewById(R.id.Buddy_Fab_Deldiary); // 다이어리 삭제를 위한 버튼

        fab_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        fab_adddiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 추가 버튼에 대한 클릭 리스너
                Intent intent = new Intent(FabActivity.this, AddlogActivity.class); // AddlogActivity로 넘어간다
                startActivity(intent);
                finish();
            }
        });

        fab_deldiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 삭제 버튼에 대한 클릭 리스너
                Intent intent = new Intent(FabActivity.this, DeleteActivity.class); // DeleteActivity로 넘어간다
                startActivity(intent);
                finish();
            }
        });
    }
}
