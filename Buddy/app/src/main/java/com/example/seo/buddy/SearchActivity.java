package com.example.seo.buddy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.Inflater;

/**
 * Created by Seo on 2017-01-19.
 */
public class SearchActivity extends Fragment { // 로그 다이어리 검색을 위한 SearchActivity

    // 로그 데이터베이스에 점근하기 위한 변수들
    private Cursor cursor = null;
    private LogDBHelper logdbhelper;
    private SQLiteDatabase logdb;
    private String logsql;

    // 찾은 데이터들 넣기 위한 배열
    ArrayList<SearchData> originDatas = new ArrayList<SearchData>();

    // 검색의 기준이 되는 카테고리 변수
    private String cat = "dairy";
    private String table = "logtable";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.activity_search, container, false);

        // 검색어 edittext
        final EditText search_edittext = (EditText) rootView.findViewById(R.id.Buddy_Search_Edittext);

        // 카테고리 버튼과 검색 버튼
        final ImageView search_category = (ImageView) rootView.findViewById(R.id.Buddy_Search_Category);
        final ImageView search_button = (ImageView) rootView.findViewById(R.id.Buddy_Search_Button);

        // 찾은 데이터들 나타낼 listview
        final ListView search_listview = (ListView) rootView.findViewById(R.id.Buddy_Search_Listview);

        // 데이터가 없을시 나타낼 textview
        final TextView search_nodata = (TextView) rootView.findViewById(R.id.Buddy_Search_Nodata);

        // 카테고리 버튼 클릭시 나타날 다이얼로그 Build
        String[] category2 = {"다이어리", "장소"};
        final AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
        builder2.setItems(category2, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if(which == 0) { // 다이어리 검색
                    cat = "dairy";
                    search_edittext.setText(null);
                    search_edittext.setHint("다이어리로 검색");
                }
                else if(which == 1) { // 장소 검색
                    cat = "location";
                    search_edittext.setText(null);
                    search_edittext.setHint("장소로 검색");
                }
            }
        });
        builder2.create();

        search_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder2.show();
            }
        }); // 카테고리 버튼을 누르면 다이얼로그가 등장한다

        // listview 아이템에 대한 클릭 리스너
        search_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() { // TODO: 2017-01-27 리스트뷰 아이템 클릭시 이벤트
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SearchData searchData = originDatas.get(i);

                // 데이터를 찾아 extras에 저장 후
                Bundle extras = new Bundle();
                extras.putInt("hour", searchData.getHour());
                extras.putInt("min", searchData.getMin());
                extras.putString("ampm", searchData.getAmpm());
                extras.putString("location", searchData.getLocation());
                extras.putString("photo", searchData.getPhoto());
                extras.putInt("meal", searchData.getMeal());
                extras.putString("glucose", searchData.getGlocose());
                extras.putString("diary", searchData.getDiary());

                // SearchDetailActivity로 보낸다
                Intent intent = new Intent(getActivity(), SearchDetailActivity.class);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });

        search_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) { // 키보드에 검색 기능을 넣기 위한 메소드
                switch(i) {
                    case EditorInfo.IME_ACTION_SEARCH: // 돋보기 모양 버튼(검색), 입력받은 edittext로 logtable에서 검색
                        ArrayList<SearchData> datas = new ArrayList<SearchData>();
                        logdbhelper = new LogDBHelper(getActivity());
                        logdb = logdbhelper.getReadableDatabase();
                        logsql =  "SELECT * FROM " + table + " WHERE " + cat + " LIKE '%" + search_edittext.getText().toString() + "%';";
                        cursor = logdb.rawQuery(logsql, null);
                        cursor.moveToLast();

                        // 검색한 결과 데이터가 있는 경우 -> 데이터 배열에 입력하여 listview에 삽입, 어뎁터 셋팅 및 listview 보이도록 변경
                        if(cursor.getCount() != 0 && search_edittext.getText().toString().getBytes().length > 0) {
                            while (true) {
                                datas.add(new SearchData(cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getString(6), cursor.getString(7),
                                        cursor.getString(8), cursor.getInt(9), cursor.getString(10), cursor.getString(11)));

                                if (cursor.getPosition() == 0) {
                                    break;
                                }

                                cursor.moveToPrevious();
                            }
                            SearchDataAdapter searchDataAdapter = new SearchDataAdapter(getContext(), R.layout.listview_search, datas);
                            search_listview.setAdapter(searchDataAdapter);
                            search_listview.setVisibility(View.VISIBLE);
                            search_nodata.setVisibility(View.GONE);
                        } else { // 데이터가 없다는 textview 보이도록 변경
                            search_listview.setVisibility(View.GONE);
                            search_nodata.setVisibility(View.VISIBLE);
                        }
                        originDatas = datas;
                        cursor.close();
                        logdb.close();
                        logdbhelper.close();
                        break;
                }
                return true;
            }
        });

        // 검색 버튼에 대한 클릭 리스너 -> 위 메소드와 동작 동일하다
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<SearchData> datas = new ArrayList<SearchData>();
                logdbhelper = new LogDBHelper(getActivity());
                logdb = logdbhelper.getReadableDatabase();
                logsql =  "SELECT * FROM " + table + " WHERE " + cat + " LIKE '%" + search_edittext.getText().toString() + "%';";
                cursor = logdb.rawQuery(logsql, null);
                cursor.moveToLast();

                if(cursor.getCount() != 0 && search_edittext.getText().toString().getBytes().length > 0) {
                    while (true) {
                        datas.add(new SearchData(cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getString(6), cursor.getString(7),
                                cursor.getString(8), cursor.getInt(9), cursor.getString(10), cursor.getString(11)));

                        if (cursor.getPosition() == 0) {
                            break;
                        }

                        cursor.moveToPrevious();
                    }
                    SearchDataAdapter searchDataAdapter = new SearchDataAdapter(getContext(), R.layout.listview_search, datas);
                    search_listview.setAdapter(searchDataAdapter);
                    search_listview.setVisibility(View.VISIBLE);
                    search_nodata.setVisibility(View.GONE);
                } else {
                    search_listview.setVisibility(View.GONE);
                    search_nodata.setVisibility(View.VISIBLE);
                }
                originDatas = datas;
                cursor.close();
                logdb.close();
                logdbhelper.close();
            }
        });

        return rootView;
    }
}

class SearchDataAdapter extends BaseAdapter { // SearchData에 대한 어뎁터

    Context context;
    int layout;
    ArrayList<SearchData> datas;
    LayoutInflater inflater;

    public SearchDataAdapter(Context context, int layout, ArrayList<SearchData> datas) {
        this.context = context;
        this.layout = layout;
        this.datas = datas;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public SearchData getItem(int i) {
        return datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    // listview의 각 아이템들에 데이터를 셋팅하여 화면에 보여주는 getview
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            view = inflater.inflate(R.layout.listview_search, null);
        }
        // listview 아이템들 정의
        TextView search_year = (TextView)view.findViewById(R.id.Buddy_Searchlistview_Year);
        TextView search_mon = (TextView)view.findViewById(R.id.Buddy_Searchlistview_Month);
        TextView search_day = (TextView)view.findViewById(R.id.Buddy_Searchlistview_Day);
        TextView search_hour = (TextView)view.findViewById(R.id.Buddy_Searchlistview_Hour);
        TextView search_min = (TextView)view.findViewById(R.id.Buddy_Searchlistview_Min);
        TextView search_ampm = (TextView)view.findViewById(R.id.Buddy_Searchlistview_Ampm);
        TextView search_location = (TextView)view.findViewById(R.id.Buddy_Searchlistview_Location);
        TextView search_diary = (TextView)view.findViewById(R.id.Buddy_Searchlistview_Diary);

        // 아이템들에 데이터 셋팅
        search_year.setText(""+datas.get(i).getYear());
        search_mon.setText(""+datas.get(i).getMon());
        search_day.setText(""+datas.get(i).getDay());
        if(datas.get(i).getHour() == 0)
            search_hour.setText("12");
        else
            search_hour.setText(""+datas.get(i).getHour());

        if(String.valueOf(datas.get(i).getMin()).length() == 1)
            search_min.setText("0"+datas.get(i).getMin());
        else
            search_min.setText(""+datas.get(i).getMin());

       //search_min.setText(""+datas.get(i).getMin());
        search_ampm.setText(datas.get(i).getAmpm());
        search_location.setText(datas.get(i).getLocation());
        search_diary.setText(datas.get(i).getDiary());

        return view;
    }
}

class SearchData { // SearchData class
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

    public SearchData(int year, int mon, int day, int hour, int min, String ampm, String location, String photo, int meal, String glocose, String diary) {
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
    }

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
}
