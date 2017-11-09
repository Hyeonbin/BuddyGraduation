package com.example.seo.buddy;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.data.LineRadarDataSet;

/**
 * Created by Seo on 2017-02-12.
 */
public class GraphFabActivity extends AppCompatActivity { // GraphActivity의 floating action button을 위한 GraphFabActivity

    // GraphDBHelper에 접근하기 위한 변수들
    private GraphDBHelper graphDBHelper;
    private SQLiteDatabase graphdb;
    private Cursor graphcursor = null;
    private String graphsql;

    GraphActivity graphActivity = (GraphActivity)GraphActivity.currentfragment; // GraphActivity에 접근하기 위한 변수

    // handler와 runnable 선언
    private Runnable runnable;
    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_deletefab);

        final Intent intent = new Intent();

        RelativeLayout graphfab_layout = (RelativeLayout) findViewById(R.id.Buddy_Graphfab_Layout);

        // 아침, 점심, 저녁, 취침 그래프 버튼
        TextView graphfab_bar = (TextView)findViewById(R.id.Buddy_Graphfab_Bar);
        TextView graphfab_morning = (TextView)findViewById(R.id.Buddy_Graphfab_Morning);
        TextView graphfab_noon = (TextView)findViewById(R.id.Buddy_Graphfab_Noon);
        TextView graphfab_night = (TextView)findViewById(R.id.Buddy_Graphfab_Night);
        TextView graphfab_sleep = (TextView)findViewById(R.id.Buddy_Graphfab_Sleep);

        runnable = new Runnable() { // GraphActivity를 종료하기 위한 메소드
            @Override
            public void run() {
                graphActivity.getActivity().finish();
            }
        };

        handler = new Handler(); // handler 할당

        // GraphDBHelper에 접근하기 위한 모든 변수들 셋팅
        graphDBHelper = new GraphDBHelper(this);
        graphdb = graphDBHelper.getReadableDatabase();
        graphsql = "SELECT * FROM graphtable";
        graphcursor = graphdb.rawQuery(graphsql, null);
        graphcursor.moveToFirst();

        graphfab_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        graphfab_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 바 그래프 버튼에 대한 클릭 리스너
                graphDBHelper.delete(); // 테이블 삭제 후 재 삽입
                graphDBHelper.Insert(0);
                graphdb.close();
                graphcursor.close();
                graphDBHelper.close();
                Intent intent = new Intent(GraphFabActivity.this, MainActivity.class); // MainActivity로 이동
                startActivity(intent);
                handler.postDelayed(runnable, 1000); // 1초 후 현재 액티비티 종료(자연스러운 종료를 위해)
                finish();
            }
        });

        graphfab_morning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 아침 그래프 버튼에 대한 클릭 리스너
                graphDBHelper.delete(); // 테이블 삭제 후 재 삽입
                graphDBHelper.Insert(1);
                graphdb.close();
                graphcursor.close();
                graphDBHelper.close();
                Intent intent = new Intent(GraphFabActivity.this, MainActivity.class); // MainActivity로 이동
                startActivity(intent);
                handler.postDelayed(runnable, 1000); // 1초 후 현재 액티비티 종료(자연스러운 종료를 위해)
                finish();
            }
        });

        graphfab_noon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 점심 그래프 버튼에 대한 클릭 리스너
                graphDBHelper.delete();
                graphDBHelper.Insert(2);
                graphdb.close();
                graphcursor.close();
                graphDBHelper.close();
                Intent intent = new Intent(GraphFabActivity.this, MainActivity.class);
                startActivity(intent);
                handler.postDelayed(runnable, 1000);
                finish();
            }
        });

        graphfab_night.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 저녁 그래프 버튼에 대한 클릭 리스너
                graphDBHelper.delete();
                graphDBHelper.Insert(3);
                graphdb.close();
                graphcursor.close();
                graphDBHelper.close();
                Intent intent = new Intent(GraphFabActivity.this, MainActivity.class);
                startActivity(intent);
                handler.postDelayed(runnable, 1000);
                finish();
            }
        });

        graphfab_sleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 취침 그래프 버튼에 대한 클릭 리스너
                graphDBHelper.delete();
                graphDBHelper.Insert(4);
                graphdb.close();
                graphcursor.close();
                graphDBHelper.close();
                Intent intent = new Intent(GraphFabActivity.this, MainActivity.class);
                startActivity(intent);
                handler.postDelayed(runnable, 1000);
                finish();
            }
        });
    }
}
