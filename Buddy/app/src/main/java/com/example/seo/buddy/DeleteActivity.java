package com.example.seo.buddy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seo on 2017-01-30.
 */
public class DeleteActivity extends AppCompatActivity { // 로그 다이어리를 삭제하기 위한 DeleteActivity

    // 데이터베이스에 접근하기 위한 커서, 헬퍼, db, sql문장 변수들
    private Cursor logcursor = null;
    private LogDBHelper logdbhelper;
    private SQLiteDatabase logdb;
    private String logsql;

    private int nodata = 0; // 데이터가 없는 경우에 대한 시그널

    private String table; // 테이블 이름을 저장하기 위한 변수

    LogbookActivity logbookActivity = (LogbookActivity)LogbookActivity.currentfragment; // logbookactivity에 접근하기 위한 변수
    GraphActivity graphActivity = (GraphActivity)GraphActivity.currentfragment; // graphactivity에 접근하기 위한 변수

    DeleteDataAdapter deleteDataAdapter; // deletedata에 대한 어댑터

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dltlog);

        logdbhelper = new LogDBHelper(this); // logdbhelper 할당
        logdb = logdbhelper.getReadableDatabase(); // logdb를 읽기 위해 할당

        logsql = "SELECT * FROM logtable"; // logtable을 읽기 위한 sql문장
        logcursor = logdb.rawQuery(logsql, null); // logcursor 셋팅
        logcursor.moveToLast(); // 가장 최근 데이터부터 읽기 위해 logcursor 맨 뒤로 이동

        // 로그 다이어리를 저장하기 위한 배열 변수들 선언
        final ArrayList<DeleteData> child = new ArrayList<>();
        final ArrayList<DeleteData> zerodata = new ArrayList<>();

        ListView dltlog_listview = (ListView)findViewById(R.id.Buddy_Dltlog_Listview); // 전체 로그 다이어리를 나타내기 위한 listview
        // 모두 선택, 선택 취소, 삭제 버튼
        LinearLayout dltlog_allselect = (LinearLayout)findViewById(R.id.Buddy_Dltlog_Allselect);
        LinearLayout dltlog_unselect = (LinearLayout)findViewById(R.id.Buddy_Dltlog_Unselect);
        LinearLayout dltlog_delete = (LinearLayout)findViewById(R.id.Buddy_Dltlog_Delete);

        TextView dltlog_nodata = (TextView)findViewById(R.id.Buddy_Dltlog_Nodata); // 데이터가 없다는 것을 나타내는 textview

        ImageView dltlog_backbtn = (ImageView)findViewById(R.id.Buddy_Dltlog_Backbtn); // Back 버튼

        if(logcursor.getCount() != 0) { // 데이터베이스를 읽기 위한 알고리즘, 데이터가 없을 경우 동작하지 않는다
            nodata = 1; // nodata 시그널 on
            while (true) {
                // 데이터베이스 순서대로 읽는다
                int curindex = logcursor.getInt(0);
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

                if(Integer.parseInt(curglucose) != 0) // 혈당 정보가 0이 아닌 경우
                    child.add(new DeleteData(curindex, curyear, curmonth, curday, curhour, curmin, curampm, curlocation, curphoto, curmeal, curglucose, curdiary));
                else // 혈당 정보가 0인 경우(그래프를 위한 임시 데이터인 경우)
                    zerodata.add(new DeleteData(curindex, curyear, curmonth, curday, curhour, curmin, curampm, curlocation, curphoto, curmeal, curglucose, curdiary));

                if (logcursor.getPosition() == 0) // 커서 위치가 0인 경우 알고리즘 종료
                    break;

                logcursor.moveToPrevious(); // 커서 뒤로 이동

            }
        }

        if(nodata == 1) { // nodata 시그널이 on인 경우 listview 보이도록
            dltlog_listview.setVisibility(View.VISIBLE);
            dltlog_nodata.setVisibility(View.GONE);
        } else { // 그렇지 않다면, listview 안보이도록
            dltlog_listview.setVisibility(View.GONE);
            dltlog_nodata.setVisibility(View.VISIBLE);
        }

        // 어댑터 listview에 셋팅
        deleteDataAdapter = new DeleteDataAdapter(this, R.layout.listview_dltlog_child, child);
        dltlog_listview.setAdapter(deleteDataAdapter);

        dltlog_allselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 모두 선택 버튼에 대한 클릭 리스너
                for(int i=0; i<child.size(); i++) {
                    child.get(i).setSig(1); // 모든 선택 시그널을 바꿔준다
                }

                deleteDataAdapter.notifyDataSetChanged();
            }
        });

        dltlog_backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // back 버튼에 대한 클릭 리스너
                AlertDialog.Builder builder = new AlertDialog.Builder(DeleteActivity.this, R.style.MyAlertDialogTheme); // 다이얼로그 build
                builder.setTitle("다이어리 삭제를 취소하시겠습니까?");
                builder.setCancelable(false);

                builder.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { // 취소
                        finish();
                    }
                });

                builder.setNegativeButton("계속", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { // 동작 게속 진행
                        dialogInterface.dismiss();
                    }
                });

                // 다이얼로그 출력
                AlertDialog alert = builder.create();
                alert.show();

            }
        });

        dltlog_unselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 모든 선택 취소 버튼에 대한 클릭 리스너
                for(int i=0; i<child.size(); i++) {
                    child.get(i).setSig(0); // 모든 선택 시그널을 바꿔준다
                }
                deleteDataAdapter.notifyDataSetChanged();
            }
        });

        dltlog_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) { // listview의 아이템들에 대한 클릭 리스너
                DeleteData deleteData = child.get(i); // 선택한 다이어리를 찾는다

                // 선택에 따라 시그널 변경
                if(deleteData.getSig() == 0)
                    deleteData.setSig(1);
                else
                    deleteData.setSig(0);

                deleteDataAdapter.notifyDataSetChanged();
            }
        });

        dltlog_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 삭제 버튼에 대한 클릭 리스너
                if(KnowSelect(child)) { // 선택된 아이템이 있는 경우
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DeleteActivity.this, R.style.MyAlertDialogTheme); // 삭제 다이얼로그 생성

                    alertDialogBuilder.setTitle("선택한 다이어리를 삭제하면 복구할 수 없습니다. 삭제 하시겠습니까?");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("삭제", // 삭제 버튼
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int id) {
                                    for (int i = 0; i < child.size(); i++) {
                                        if (child.get(i).getSig() == 1) { // 전체 아이템들 중 선택된 아이템에 대해서만 동작 진행
                                            if(child.get(i).getMeal() !=0 && child.get(i).getMeal() != 7) { // 선택한 아이템이 식사정보가 없거나 취침 전이 아닌 경우
                                                int zerosig = 0; // 혈당 정보가 0인 임시 데이터가 존재하는 경우에 대한 시그널
                                                for (int j = 0; j < zerodata.size(); j++) { // 혈당 정보가 0인 임시 데이터가 존재하는 경우를 찾기 위한 반복문
                                                    if (child.get(i).getMeal() == 1 || child.get(i).getMeal() == 3 || child.get(i).getMeal() == 5) {
                                                        if (zerodata.get(j).getMeal() == child.get(i).getMeal() + 1 && zerodata.get(j).getYear() == child.get(i).getYear()
                                                                && zerodata.get(j).getMon() == child.get(i).getMon() && zerodata.get(j).getDay() == child.get(i).getDay()) {
                                                            zerosig = 1;
                                                            logdbhelper.Delete(child.get(i).getIndex(), child.get(i).getMeal(), child.get(i).getYear(), child.get(i).getMon(), child.get(i).getDay());
                                                            logdbhelper.Delete(zerodata.get(j).getIndex(), zerodata.get(j).getMeal(), zerodata.get(j).getYear(), zerodata.get(j).getMon(), zerodata.get(j).getDay());
                                                        }
                                                    } else if (child.get(i).getMeal() == 2 || child.get(i).getMeal() == 4 || child.get(i).getMeal() == 6) {
                                                        if (zerodata.get(j).getMeal() == child.get(i).getMeal() - 1 && zerodata.get(j).getYear() == child.get(i).getYear()
                                                                && zerodata.get(j).getMon() == child.get(i).getMon() && zerodata.get(j).getDay() == child.get(i).getDay()) {
                                                            zerosig = 1;
                                                            logdbhelper.Delete(child.get(i).getIndex(), child.get(i).getMeal(), child.get(i).getYear(), child.get(i).getMon(), child.get(i).getDay());
                                                            logdbhelper.Delete(zerodata.get(j).getIndex(), zerodata.get(j).getMeal(), zerodata.get(j).getYear(), zerodata.get(j).getMon(), zerodata.get(j).getDay());
                                                        }
                                                    }
                                                } // 존재한다면 zerosig를 on으로 바꾸고 해당 데이터와 임시 데이터 모두 삭제
                                                if(zerosig == 0) { // 해당 데이터에 대한 상대 데이터가 임시 데이터가 아닌 경우
                                                    // 식사 정보에 따라 테이블 셋팅
                                                    if(child.get(i).getMeal() == 1)
                                                        table = "bbtable";
                                                    else if(child.get(i).getMeal() == 2)
                                                        table = "batable";
                                                    else if(child.get(i).getMeal() == 3)
                                                        table = "lbtable";
                                                    else if(child.get(i).getMeal() == 4)
                                                        table = "latable";
                                                    else if(child.get(i).getMeal() == 5)
                                                        table = "dbtable";
                                                    else if(child.get(i).getMeal() == 6)
                                                        table = "datable";

                                                    // 해당 데이터에 대한 혈당 정보를 0으로 만든다(상대 데이터의 혈당 정보는 0이 아니므로)
                                                    // 0으로 만든 후 zerodata 배열에 해당 데이터를 추가한다
                                                    logdbhelper.Updatetableforgraph(table, child.get(i).getYear(), child.get(i).getMon(), child.get(i).getDay(), 0, 0, null,
                                                            null, null, child.get(i).getMeal(), "0", null);
                                                    zerodata.add(new DeleteData(child.get(i).getIndex(), child.get(i).getYear(), child.get(i).getMon(), child.get(i).getDay(),
                                                            child.get(i).getHour(), child.get(i).getMin(), child.get(i).getAmpm(), child.get(i).getLocation(), child.get(i).getPhoto(),
                                                            child.get(i).getMeal(), "0", child.get(i).getDiary()));
                                                }
                                            } else // 선택한 아이템이 식사정보가 없거나 취침 전인 경우, 그냥 삭제해도 된다
                                                logdbhelper.Delete(child.get(i).getIndex(), child.get(i).getMeal(), child.get(i).getYear(), child.get(i).getMon(), child.get(i).getDay());
                                        }
                                    }
                                    logcursor.close();
                                    logbookActivity.getActivity().finish(); // 상위 activity 종료
                                    Intent intent = new Intent(DeleteActivity.this, MainActivity.class); // 삭제 후 MainActivity로 이동
                                    startActivity(intent);
                                    Toast toast = Toast.makeText(getApplicationContext(), " 삭제 되었습니다 ", Toast.LENGTH_LONG); // 토스트메시지 출력
                                    toast.show();
                                    finish();
                                }
                            });
                    alertDialogBuilder.setNegativeButton("취소", // 취소 버튼
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });

                    // 다이얼로그 출력
                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();

                } else { // 삭제할 데이터가 없는 경우, 다이얼로그 build 및 출력
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DeleteActivity.this, R.style.MyAlertDialogTheme);

                    alertDialogBuilder.setTitle("삭제할 데이터가 없습니다");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });

                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();
                }

            }
        });

        deleteDataAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() { // Destroy state에서 각 리스트들의 메모리를 해제
        if(deleteDataAdapter != null)
            deleteDataAdapter.recycle();
        RecycleUtils.recursiveRecycle(getWindow().getDecorView());
        System.gc();

        super.onDestroy();
    }

    public boolean KnowSelect(ArrayList<DeleteData> deleteDatas){ // 아이템 선택 여부를 확인하는 메소드

        for(int i=0; i<deleteDatas.size(); i++) {
            if(deleteDatas.get(i).getSig() == 1)
                return true;
        }

        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { // 스마트폰 버튼에 대한 메소드

        if(keyCode == KeyEvent.KEYCODE_BACK) { // 종료 버튼
            AlertDialog.Builder builder = new AlertDialog.Builder(DeleteActivity.this, R.style.MyAlertDialogTheme); // 종료 다이얼로그 호출
            builder.setTitle("다이어리 삭제를 취소하시겠습니까?");
            builder.setCancelable(false);

            builder.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            builder.setNegativeButton("계속", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}

class DeleteDataAdapter extends BaseAdapter { // deletedata class에 대한 어댑터

    Context context;
    int layout;
    ArrayList<DeleteData> datas;
    LayoutInflater inflater;
    private List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>(); // 메모리를 재활용하기 위한 배열변수
    public int selectedIndex = -1;

    public DeleteDataAdapter(Context context, int layout, ArrayList<DeleteData> datas) {
        this.context = context;
        this.layout = layout;
        this.datas = datas;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void recycle() { // 메모리 재활용 메소드
        for (WeakReference<View> ref : mRecycleList) {
            RecycleUtils.recursiveRecycle(ref.get());
        }
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public DeleteData getItem(int i) {
        return datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) { // listview를 출력하기 위한 메소드
        ViewHolder viewHolder; // 메모리를 줄이기 위한 viewholder class

        // 각 항목에 맞게 데이터 셋팅
        if(view == null) {
            view = inflater.inflate(R.layout.listview_dltlog_child, null);

            viewHolder = new ViewHolder();
            viewHolder.year = (TextView)view.findViewById(R.id.Buddy_Childdltlistview_Year);
            viewHolder.mon = (TextView)view.findViewById(R.id.Buddy_Childdltlistview_Month);
            viewHolder.day = (TextView)view.findViewById(R.id.Buddy_Childdltlistview_Day);
            viewHolder.hour = (TextView)view.findViewById(R.id.Buddy_Childdltlistview_Hour);
            viewHolder.min = (TextView)view.findViewById(R.id.Buddy_Childdltlistview_Min);
            viewHolder.ampm = (TextView)view.findViewById(R.id.Buddy_Childdltlistview_Ampm);
            viewHolder.location = (TextView)view.findViewById(R.id.Buddy_Childdltlistview_Location);
            viewHolder.photo = (ImageView)view.findViewById(R.id.Buddy_Childdltlistview_Photo);
            viewHolder.glucose = (TextView)view.findViewById(R.id.Buddy_Childdltlistview_Glucose);
            viewHolder.glucoseunit = (TextView)view.findViewById(R.id.Buddy_Childdltlistview_Glucoseunit);
            viewHolder.diary = (TextView)view.findViewById(R.id.Buddy_Childdltlistview_Diary);
            viewHolder.meal = (TextView)view.findViewById(R.id.Buddy_Childdltlistview_Meal);
            viewHolder.glucoselayout = (LinearLayout)view.findViewById(R.id.Buddy_Childdltlistview_Glucoselayout);
            viewHolder.glucosedetaillayout = (LinearLayout)view.findViewById(R.id.Buddy_Childdltlistview_Glucosedetaillayout);
            viewHolder.mainlayout = (LinearLayout)view.findViewById(R.id.Buddy_Childdltlistview_Mainlayout);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if(datas.get(i).getMeal() == 0) // 식사 정보가 없는 경우
            viewHolder.glucoselayout.setVisibility(View.GONE); // 혈당 정보 레이아웃 숨김
        else
            viewHolder.glucoselayout.setVisibility(View.VISIBLE);
            //child_glucoselayout.setVisibility(View.GONE);

        // 각 항목별로 데이터 셋팅
        viewHolder.year.setText(""+datas.get(i).getYear());
        viewHolder.mon.setText(""+datas.get(i).getMon());
        viewHolder.day.setText(""+datas.get(i).getDay());
        if(datas.get(i).getHour() == 0)
            viewHolder.hour.setText("12");
        else
            viewHolder.hour.setText(""+datas.get(i).getHour());

        // 분의 글자 길이에 따라 0을 붙일지 여부를 선택
        if(String.valueOf(datas.get(i).getMin()).length() == 1)
            viewHolder.min.setText("0"+datas.get(i).getMin());
        else
            viewHolder.min.setText(""+datas.get(i).getMin());

        viewHolder.ampm.setText(datas.get(i).getAmpm());
        viewHolder.location.setText(datas.get(i).getLocation());
        if(viewHolder.photo != null) { // 사진은 메모리를 줄이기 위해 glide 라이브러리로 출력
            try {
                Glide.with(context).load(datas.get(i).getPhoto()).thumbnail(0.1f).override(600, 700).centerCrop().into(viewHolder.photo);
            } catch (Exception e) {
                Log.e("Photo", "Error: " + e);
            }
        } else
            viewHolder.photo.setVisibility(View.GONE); // 편법이지만 일단 주석처리*/

        // 식사 정보 시그널에 따라 textview 다르게 출력
        if(datas.get(i).getMeal() == 1)
            viewHolder.meal.setText("아침 식사 전");
        else if(datas.get(i).getMeal() == 2)
            viewHolder.meal.setText("아침 식사 후");
        else if(datas.get(i).getMeal() == 3)
            viewHolder.meal.setText("점심 식사 전");
        else if(datas.get(i).getMeal() == 4)
            viewHolder.meal.setText("점심 식사 후");
        else if(datas.get(i).getMeal() == 5)
            viewHolder.meal.setText("저녁 식사 전");
        else if(datas.get(i).getMeal() == 6)
            viewHolder.meal.setText("저녁 식사 후");
        else if(datas.get(i).getMeal() == 7)
            viewHolder.meal.setText("취침 전");
        viewHolder.glucose.setText(datas.get(i).getGlocose());
        int gvalue = Integer.parseInt(datas.get(i).getGlocose()); // 숫자로 혈당 저장

        // 혈당 수치에 따라 혈당 배경 색 다르게 표현
        if(gvalue < 70) {
            viewHolder.glucose.setTextColor(Color.parseColor("#CD1039"));
            viewHolder.glucoseunit.setTextColor(Color.parseColor("#CD1039"));
        }
        else {
            if (datas.get(i).getMeal() == 1 || datas.get(i).getMeal() == 3 || datas.get(i).getMeal() == 5) {
                if(gvalue < 100) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#51FFA6"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#51FFA6"));
                }
                else if(gvalue >= 100 && gvalue < 126) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#FFDC3C"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#FFDC3C"));
                }
                else if(gvalue >= 126) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#CD1039"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#CD1039"));
                }
            } else if (datas.get(i).getMeal() == 2 || datas.get(i).getMeal() == 4 || datas.get(i).getMeal() == 6) {
                if(gvalue < 140) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#51FFA6"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#51FFA6"));
                }
                else if(gvalue >= 140 && gvalue < 200) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#FFDC3C"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#FFDC3C"));
                }
                else if(gvalue >= 200) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#CD1039"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#CD1039"));
                }
            } else if (datas.get(i).getMeal() == 7) {
                if(gvalue < 120) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#51FFA6"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#51FFA6"));
                }
                else if(gvalue >= 100 && gvalue < 160) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#FFDC3C"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#FFDC3C"));
                }
                else if(gvalue >= 160) {
                    viewHolder.glucose.setTextColor(Color.parseColor("#CD1039"));
                    viewHolder.glucoseunit.setTextColor(Color.parseColor("#CD1039"));
                }
            }
        }

        viewHolder.diary.setText(datas.get(i).getDiary());

        // 선택하여 시그널이 변하는 경우 해당 아이템의 배경색이 바뀜
        if(datas.get(i).getSig() == 1)
            viewHolder.mainlayout.setBackgroundColor(Color.parseColor("#1A000000"));
        else
            viewHolder.mainlayout.setBackgroundColor(Color.parseColor("#00000000"));

        mRecycleList.add(new WeakReference<View>(viewHolder.photo)); // 메모리 재활용을 위해 해당 배열에 저장
        return view;
    }
}

class DeleteData { // Deletedata class
    int Index;
    int Year;
    int Mon;
    int Day;
    int Hour;
    int Min;
    String Ampm;
    String Location;
    String Photo;
    int Meal;
    String Glocose;
    String Diary;
    int Sig;

    public DeleteData(int index, int year, int mon, int day, int hour, int min, String ampm, String location, String photo, int meal, String glocose, String diary) {
        Index = index;
        Year = year;
        Mon = mon;
        Day = day;
        Hour = hour;
        Min = min;
        Ampm = ampm;
        Location = location;
        Photo = photo;
        Meal = meal;
        Glocose = glocose;
        Diary = diary;
        Sig = 0;
    }

    public int getIndex() { return Index; }

    public void setIndex(int index) { Index = index; }

    public int getYear() {
        return Year;
    }

    public void setYear(int year) {
        Year = year;
    }

    public int getMon() {
        return Mon;
    }

    public void setMon(int mon) {
        Mon = mon;
    }

    public int getDay() {
        return Day;
    }

    public void setDay(int day) {
        Day = day;
    }

    public int getHour() {
        return Hour;
    }

    public void setHour(int hour) {
        Hour = hour;
    }

    public int getMin() {
        return Min;
    }

    public void setMin(int min) {
        Min = min;
    }

    public String getAmpm() {
        return Ampm;
    }

    public void setAmpm(String ampm) {
        Ampm = ampm;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getPhoto() {
        return Photo;
    }

    public void setPhoto(String photo) {
        Photo = photo;
    }

    public int getMeal() {
        return Meal;
    }

    public void setMeal(int meal) {
        Meal = meal;
    }

    public String getGlocose() {
        return Glocose;
    }

    public void setGlocose(String glocose) {
        Glocose = glocose;
    }

    public String getDiary() {
        return Diary;
    }

    public void setDiary(String diary) {
        Diary = diary;
    }

    public int getSig() { return Sig; }

    public void setSig(int sig) { Sig = sig; }
}
