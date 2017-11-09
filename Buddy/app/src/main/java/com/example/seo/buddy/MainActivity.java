package com.example.seo.buddy;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Calendar;

public class MainActivity extends FragmentActivity { // 전체 프래그먼트를 담당하는 MainActivity

    int max = 3; // 전체 프래그먼트 갯수
    Fragment cur_fragment = new Fragment(); // 현재 보여지는 프래그먼트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpager_main);

        // 뷰페이져 셋팅
        final ViewPager viewPager = (ViewPager)findViewById(R.id.Viewpager_Main);
        viewPager.setAdapter(new adapter(getSupportFragmentManager()));

        // 상단 바 버튼들 선언
        ImageView graph_profile = (ImageView)findViewById(R.id.Buddy_Topbar_Profile);
        ImageView graph_setting = (ImageView)findViewById(R.id.Buddy_Topbar_Setting);

        // 상단 탭 레이아웃 및 버튼 선언
        //TabLayout tabLayout = (TabLayout)findViewById(R.id.Viewpager_Tab);
        //tabLayout.setupWithViewPager(viewPager);
        LinearLayout tab_graph = (LinearLayout)findViewById(R.id.Buddy_Tab_Graph);
        LinearLayout tab_diary = (LinearLayout)findViewById(R.id.Buddy_Tab_Diary);
        LinearLayout tab_search = (LinearLayout)findViewById(R.id.Buddy_Tab_Search);
        final ImageView tab_graphimage = (ImageView)findViewById(R.id.Buddy_Tab_Graphimage);
        final ImageView tab_diaryimage = (ImageView)findViewById(R.id.Buddy_Tab_Diaryimage);
        final ImageView tab_searchimage = (ImageView)findViewById(R.id.Buddy_Tab_Searchimage);

        String str = getIntent().getStringExtra("particularFragment");

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 0) {
                    tab_graphimage.setImageResource(R.drawable.tab_graph2);
                    tab_diaryimage.setImageResource(R.drawable.tab_diary2_emp);
                    tab_searchimage.setImageResource(R.drawable.tab_search3_emp);
                } else if(position == 1) {
                    tab_graphimage.setImageResource(R.drawable.tab_graph2_emp);
                    tab_diaryimage.setImageResource(R.drawable.tab_diary2);
                    tab_searchimage.setImageResource(R.drawable.tab_search3_emp);
                } else if(position == 2) {
                    tab_graphimage.setImageResource(R.drawable.tab_graph2_emp);
                    tab_diaryimage.setImageResource(R.drawable.tab_diary2_emp);
                    tab_searchimage.setImageResource(R.drawable.tab_search3);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        graph_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 프로필 버튼에 대한 클릭 리스너
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class); // ProfileActivity로 이동
                startActivity(intent);
                overridePendingTransition(R.anim.leftin_activity, R.anim.rightout_activity);
            }
        });

        graph_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 설정 버튼에 대한 클릭 리스너
                Intent intent = new Intent(MainActivity.this, SettingActivity.class); // SettingActivity로 이동
                startActivity(intent);
                overridePendingTransition(R.anim.rightin_activity, R.anim.leftout_activity);
            }
        });

        // 탭 버튼들에 대한 클릭 리스너
        // 그래프 버튼
        tab_graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(0);
            }
        });

        // 다이어리 버튼
        tab_diary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(1);
            }
        });

        // 검색 버튼
        tab_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(2);
            }
        });
    }

    // 프래그먼트 어뎁터
    private class adapter extends FragmentStatePagerAdapter {
        public adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position < 0 || max<=position)
                return null;
            switch (position) { // 각 페이지별로 액티비티 정의
                case 0:
                    cur_fragment = new GraphActivity();
                    break;
                case 1:
                    cur_fragment = new LogbookActivity();
                    break;
                case 2:
                    cur_fragment = new SearchActivity();
                    break;
            }

            return cur_fragment;
        }

        @Override
        public int getCount() {
            return max;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { // 스마트폰의 버튼 입력에 대한 메소드

        if(keyCode == KeyEvent.KEYCODE_BACK) { // 종료 버튼
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogTheme); // 다이얼로그 Build
            builder.setTitle("Buddy를 종료하시겠습니까?          ");
            builder.setCancelable(false);

            builder.setPositiveButton("종료", new DialogInterface.OnClickListener() { // 종료 버튼
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) { // 취소 버튼
                    dialogInterface.dismiss();
                }
            });

            // 다이얼로그 출력
            AlertDialog alert = builder.create();
            alert.show();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}
