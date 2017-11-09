package com.example.seo.buddy;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.w3c.dom.Text;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seo on 2016-12-19.
 */
public class LogbookActivity extends Fragment { // 로그 다이어리를 보여주는 LogbookActivity

    // LogDBHelper에 접근하기 위한 변수들
    private Cursor logcursor = null;
    private LogDBHelper logdbhelper;
    private SQLiteDatabase logdb;
    private String logsql;

    // 지난 년, 월, 일을 나타내기 위한 변수들
    private int preyear = 0;
    private int premonth = 0;
    private int preday = 0;

    // 기타 시그널들
    private int startcnt = 0;
    private int nodata = 0;

    public static Fragment currentfragment; // 현재 프래그먼트를 나타내기 위한 변수

    LogbookListAdapter adapter; // 로그 다이어리 리스트들에 대한 어뎁터

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentfragment = this; // 현재 프래그먼트 셋팅
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.floating_logbook, container, false);

        FloatingActionButton fab = (FloatingActionButton)rootView.findViewById(R.id.Buddy_Floatingbtn); // floating action button 선언

        ExpandableListView logbook_listview = (ExpandableListView) rootView.findViewById(R.id.Buddy_Logbook_Listview); // 로그 다이어리에 대한 expandablelistview

        TextView logbook_nodata = (TextView) rootView.findViewById(R.id.Buddy_Logbook_Nodata); // 데이터가 없음을 나타내는 textview (데이터가 없습니다)

        // LogDBHelper에 접근하기 위한 변수들 셋팅
        logdbhelper = new LogDBHelper(getActivity());
        logdb = logdbhelper.getReadableDatabase();
        logsql = "SELECT * FROM logtable";
        logcursor = logdb.rawQuery(logsql, null);
        logcursor.moveToLast();

        // expandablelistview의 group, child를 나타내기 위한 배열
        ArrayList<LogbookGroup> group = new ArrayList<LogbookGroup>();
        ArrayList<LogbookChild> child = new ArrayList<LogbookChild>();

        if(logcursor.getCount() != 0) { // logtable에 데이터가 존재할 경우 동작 진행
            nodata = 1; // 데이터 유무 시그널 on
            while (true) {
                // logtable의 데이터를 받아온다
                int curnum = logcursor.getInt(0);
                int curyear = logcursor.getInt(1);
                int curmonth = logcursor.getInt(2);
                int curday = logcursor.getInt(3);
                int curhour = logcursor.getInt(4);
                int curmin = logcursor.getInt(5);
                String curampm = logcursor.getString(6);
                String curlocation = logcursor.getString(7);
                String curphoto = logcursor.getString(8);
                int curmeal = logcursor.getInt(9);
                String curglucose = logcursor.getString(10);
                String curdiary = logcursor.getString(11);

                if(Integer.parseInt(curglucose) != 0) { // 임시데이터가 아닌 경우 (혈당 정보가 0이 아닌 경우)
                    if (preyear != curyear || premonth != curmonth || preday != curday) { // 해당 날짜에 대한 첫 insert인 경우
                        if (startcnt != 0) { // preyear, premonth, preday로 group을 만들기 위한 if문
                            group.add(new LogbookGroup(preyear, premonth, preday, child));
                        }
                        child = new ArrayList<LogbookChild>(); // child 초기화
                        child.add(new LogbookChild(curnum, curhour, curmin, curampm, curlocation, curphoto, curmeal, curglucose, curdiary)); // child로 insert
                        // pre값에 cur값을 입력
                        preyear = curyear;
                        premonth = curmonth;
                        preday = curday;
                        startcnt = 1; // start 시그널 변경
                    } else // 해당 날짜에 대한 첫 insert가 아닌 경우
                        child.add(new LogbookChild(curnum, curhour, curmin, curampm, curlocation, curphoto, curmeal, curglucose, curdiary)); // 그냥 child에 insert
                }

                if (logcursor.getPosition() == 0) // 마지막 데이터인 경우
                    break; // 동작 종료

                logcursor.moveToPrevious(); // 뒤로 1칸 이동

            }
        }

        if(nodata == 1) { // nodata 시그널에 따라 listview를 보여줄지 말지에 대한 알고리즘
            logbook_listview.setVisibility(View.VISIBLE);
            logbook_nodata.setVisibility(View.GONE);
        } else {
            logbook_listview.setVisibility(View.GONE);
            logbook_nodata.setVisibility(View.VISIBLE);
        }

        group.add(new LogbookGroup(preyear, premonth, preday, child)); // 마지막 group insert 동작은 while문 밖에서 진행

        // 데이터베이스 종료
        logcursor.close();
        logdb.close();
        logdbhelper.close();

        adapter = new LogbookListAdapter(getActivity(), group); // 어뎁터 정의

        final String[] category = {"다이어리 수정"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        logbook_listview.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, final int i, final int i1, long l) {
                builder.setItems(category, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if(which == 0) { // 다이어리 검색
                            LogbookGroup group = (LogbookGroup)adapter.getGroup(i);
                            LogbookChild child = (LogbookChild)adapter.getChild(i, i1);

                            Bundle extras = new Bundle();
                            extras.putInt("Num", child.getNum());
                            extras.putInt("Year", group.getYear());
                            extras.putInt("Mon", group.getMon());
                            extras.putInt("Day", group.getDay());
                            extras.putInt("Hour", child.getHour());
                            extras.putInt("Min", child.getMin());
                            extras.putString("Ampm", child.getAmpm());
                            extras.putString("Location", child.getLocation());
                            extras.putString("Photo", child.getPhoto());
                            extras.putInt("Meal", child.getMeal());
                            extras.putString("Glucose", child.getGlocose());
                            extras.putString("Diary", child.getDiary());

                            Intent intent = new Intent(getActivity(), ModifyActivity.class);
                            intent.putExtras(extras);
                            startActivity(intent);
                        }
                    }
                });
                builder.create();
                builder.show();

                return true;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // floating action button에 대한 클릭 리스너
                Intent intent = new Intent(getActivity(), FabActivity.class); // FabActivity로 이동
                startActivity(intent);
            }
        });

        if(preyear != 0) { // preyear가 존재할 경우, 즉, 데이터가 있을 때만 listview에 대한 전체적인 셋팅이 이루어진다
            logbook_listview.setAdapter(adapter);

            for (int i = 0; i < adapter.getGroupCount(); i++)
                logbook_listview.expandGroup(i);
        }

        adapter.notifyDataSetChanged(); // 데이터 변경이 있을 때마다 listview 새로고침
        return rootView;
    }

    @Override
    public void onDestroyView() {
        // 메모리 재활용을 위한 메소드
        if(adapter != null)
            adapter.recycle();
        RecycleUtils.recursiveRecycle(getActivity().getWindow().getDecorView());
        System.gc();

        super.onDestroyView();
    }
}

class LogbookListAdapter extends BaseExpandableListAdapter { // expandablelistview 어뎁터
    private Context context;
    private ArrayList<LogbookGroup> group;
    private List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>(); // 메모리 누수를 잡기 위한 배열
    public int selectedIndex = -1;

    public LogbookListAdapter(Context context, ArrayList<LogbookGroup> group) {
        this.context = context;
        this.group = group;
    }

    public void recycle() { // 메모리 재활용을 위한 메소드
        for (WeakReference<View> ref : mRecycleList) {
            RecycleUtils.recursiveRecycle(ref.get());
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<LogbookChild> chList = group.get(groupPosition).getChild();
        return chList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    // child 부분을 보여주기 위한 getview 메소드, 각 항목별로 선언 후 데이터를 불러와 연결 (AddlogActivity 참조)
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LogbookChild child = (LogbookChild)getChild(groupPosition, childPosition);
        ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_logbook_child, null);

            viewHolder = new ViewHolder();
            viewHolder.hour = (TextView)convertView.findViewById(R.id.Buddy_Childlistview_Hour);
            viewHolder.min = (TextView)convertView.findViewById(R.id.Buddy_Childlistview_Min);
            viewHolder.ampm = (TextView)convertView.findViewById(R.id.Buddy_Childlistview_Ampm);
            viewHolder.location = (TextView)convertView.findViewById(R.id.Buddy_Childlistview_Location);
            viewHolder.photo = (ImageView)convertView.findViewById(R.id.Buddy_Childlistview_Photo);
            viewHolder.glucose = (TextView)convertView.findViewById(R.id.Buddy_Childlistview_Glucose);
            viewHolder.glucoseunit = (TextView)convertView.findViewById(R.id.Buddy_Childlistview_Glucoseunit);
            viewHolder.diary = (TextView)convertView.findViewById(R.id.Buddy_Childlistview_Diary);
            viewHolder.meal = (TextView)convertView.findViewById(R.id.Buddy_Childlistview_Meal);
            viewHolder.glucoselayout = (LinearLayout)convertView.findViewById(R.id.Buddy_Childlistview_Glucoselayout);
            viewHolder.glucosedetaillayout = (LinearLayout)convertView.findViewById(R.id.Buddy_Childlistview_Glucosedetaillayout);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(child.getMeal() == 0)
            viewHolder.glucoselayout.setVisibility(View.GONE);
            //child_glucoselayout.setVisibility(View.VISIBLE);
        else
            viewHolder.glucoselayout.setVisibility(View.VISIBLE);
            //child_glucoselayout.setVisibility(View.GONE);

        if(child.getHour() == 0)
            viewHolder.hour.setText("12");
        else
            viewHolder.hour.setText(""+child.getHour());
        //child_hour.setText(""+child.getHour());
        if(String.valueOf(child.getMin()).length() == 1) {
            viewHolder.min.setText("0"+child.getMin());
            //child_min.setText("0"+child.getMin());
        }
            //child_min.setText("0"+child.getMin());
        else
            viewHolder.min.setText(""+child.getMin());
            //child_min.setText(""+child.getMin());
        viewHolder.ampm.setText(child.getAmpm());
        //child_ampm.setText(child.getAmpm());
        viewHolder.location.setText(child.getLocation());
        //child_location.setText(child.getLocation());
        if(viewHolder.photo != null) {
            try {
                //File file = new File(child.getPhoto());
                //BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inSampleSize = 8;
                //Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
                //Bitmap resized = Bitmap.createScaledBitmap(bm, 600, 600, true);
                //child_photo.setImageBitmap(resized);
                Glide.with(context).load(child.getPhoto()).thumbnail(0.1f).override(600, 700).centerCrop().into(viewHolder.photo);
            } catch (Exception e) {
                Log.e("Photo", "Error: " + e);
            }
        } else
            viewHolder.photo.setVisibility(View.GONE); // 편법이지만 일단 주석처리*/

        if(child.getMeal() == 1)
            viewHolder.meal.setText("아침 식사 전");
            //child_meal.setText("아침 식사 전");
        else if(child.getMeal() == 2)
            viewHolder.meal.setText("아침 식사 후");
            //child_meal.setText("아침 식사 후");
        else if(child.getMeal() == 3)
            viewHolder.meal.setText("점심 식사 전");
            //child_meal.setText("점심 식사 전");
        else if(child.getMeal() == 4)
            viewHolder.meal.setText("점심 식사 후");
            //child_meal.setText("점심 식사 후");
        else if(child.getMeal() == 5)
            viewHolder.meal.setText("저녁 식사 전");
            //child_meal.setText("저녁 식사 전");
        else if(child.getMeal() == 6)
            viewHolder.meal.setText("저녁 식사 후");
            //child_meal.setText("저녁 식사 후");
        else if(child.getMeal() == 7)
            viewHolder.meal.setText("취침 전");
            //child_meal.setText("취침 전");

        viewHolder.glucose.setText(child.getGlocose());
        //child_glucose.setText(child.getGlocose());
        int gvalue = Integer.parseInt(child.getGlocose());
        if(gvalue < 70) {
            viewHolder.glucose.setTextColor(Color.parseColor("#CD1039"));
            viewHolder.glucoseunit.setTextColor(Color.parseColor("#CD1039"));
            //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#CD1039"));
        }
        else {
            if (child.getMeal() == 1 || child.getMeal() == 3 || child.getMeal() == 5) {
                if(gvalue < 100) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#51FFA6"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#51FFA6"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#51FFA6"));
                }
                else if(gvalue >= 100 && gvalue < 126) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#FFDC3C"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#FFDC3C"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#FFF064"));
                }
                else if(gvalue >= 126) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#CD1039"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#CD1039"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#CD1039"));
                }
            } else if (child.getMeal() == 2 || child.getMeal() == 4 || child.getMeal() == 6) {
                if(gvalue < 140) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#51FFA6"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#51FFA6"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#51FFA6"));
                }
                else if(gvalue >= 140 && gvalue < 200) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#FFDC3C"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#FFDC3C"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#FFF064"));
                }
                else if(gvalue >= 200) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#CD1039"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#CD1039"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#CD1039"));
                }
            } else if (child.getMeal() == 7) {
                if(gvalue < 120) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#51FFA6"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#51FFA6"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#51FFA6"));
                }
                else if(gvalue >= 100 && gvalue < 160) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#FFDC3C"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#FFDC3C"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#FFF064"));
                }
                else if(gvalue >= 160) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#CD1039"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#CD1039"));
                    //child_glucosedetaillayout.setBackgroundColor(Color.parseColor("#CD1039"));
                }
            }
        }
        viewHolder.diary.setText(child.getDiary());
        //child_diary.setText(child.getDiary());

        mRecycleList.add(new WeakReference<View>(viewHolder.photo));
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<LogbookChild> chList = group.get(groupPosition).getChild();
        return chList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return group.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return group.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    // group 부분을 보여주기 위한 getview 메소드, 년, 월, 일만 보여주면 된다
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LogbookGroup group= (LogbookGroup)getGroup(groupPosition);
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_logbook_group, null);
        }

        TextView group_year = (TextView)convertView.findViewById(R.id.Buddy_Logbook_Listview_Year);
        TextView group_min = (TextView)convertView.findViewById(R.id.Buddy_Logbook_Listview_Month);
        TextView group_day = (TextView)convertView.findViewById(R.id.Buddy_Logbook_Listview_Day);

        group_year.setText(""+group.getYear());
        group_min.setText(""+group.getMon());
        group_day.setText(""+group.getDay());

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}

class LogbookGroup { // group을 위한 class
    int Year;
    int Mon;
    int Day;
    ArrayList<LogbookChild> Child;

    public LogbookGroup(int Y, int M, int D, ArrayList<LogbookChild> C){
        this.Year = Y;
        this.Mon = M;
        this.Day = D;
        this.Child = C;
    }

    public void setYear(int Y){
        this.Year = Y;
    }

    public void setMon(int M){
        this.Mon = M;
    }

    public void setDay(int D){
        this.Day = D;
    }

    public void setChild(ArrayList<LogbookChild> C){
        this.Child = C;
    }

    public int getYear(){
        return Year;
    }

    public int getMon(){
        return Mon;
    }

    public int getDay(){
        return Day;
    }

    public ArrayList<LogbookChild> getChild() {
        return Child;
    }
}

class LogbookChild { // child를 위한 class
    int Num;
    int Hour;
    int Min;
    String Ampm;
    String Location;
    String Photo;
    int Meal;
    String Glocose;
    String Diary;

    public LogbookChild(int n, int h, int m, String a, String l, String p, int me, String g, String d){
        this.Num = n;
        this.Hour = h;
        this.Min = m;
        this.Ampm = a;
        this.Location = l;
        this.Photo = p;
        this.Meal = me;
        this.Glocose = g;
        this.Diary = d;
    }

    public void setNum(int n) { this.Num = n; }

    public void setHour(int h){
        this.Hour = h;
    }

    public void setMin(int m){
        this.Min = m;
    }

    public void setAmpm(String a){
        this.Ampm = a;
    }

    public void setLocation(String l){
        this.Location = l;
    }

    public void setPhoto(String p){
        this.Photo = p;
    }

    public void setMeal(int me) { this.Meal = me; }

    public void setGlocose(String g){
        this.Glocose = g;
    }

    public void setDiary(String d){
        this.Diary = d;
    }

    public int getNum() { return Num; }

    public int getHour(){
        return Hour;
    }

    public int getMin(){
        return Min;
    }

    public String getAmpm(){
        return Ampm;
    }

    public String getLocation(){
        return Location;
    }

    public String getPhoto(){
        return Photo;
    }

    public int getMeal() { return Meal; }

    public String getGlocose(){
        return Glocose;
    }

    public String getDiary(){
        return Diary;
    }
}

