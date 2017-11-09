package com.example.seo.buddy;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seo on 2016-12-19.
 */
public class GraphActivity extends Fragment { // 혈당 그래프를 나타내기 위한 GraphActivity

    // LogDBHelper 데이터베이스를 사용하기 위한 변수들
    private Cursor dbcursor = null;
    private Cursor graphcursor = null;
    private LogDBHelper dbhelper;
    private SQLiteDatabase db;
    private String dbsql;
    private String graphsql;

    // GraphDBHelper 데이터베이스를 사용하기 위한 변수들
    private GraphDBHelper typeDBHelper;
    private SQLiteDatabase typedb;
    private Cursor typecursor = null;
    private String typesql;

    private int count = 0; // 아이템 갯수를 저장하기 위한 변수

    // 아침, 점심, 저녁, 취침 x축들을 위한 배열
    ArrayList<String> line_bxVals = new ArrayList<String>();
    ArrayList<String> line_lxVals = new ArrayList<String>();
    ArrayList<String> line_dxVals = new ArrayList<String>();
    ArrayList<String> line_sxVals = new ArrayList<String>();

    // 각 식사 정보별로 데이터를 저장하기 위한 배열
    ArrayList<Entry> breakfast_before = new ArrayList<Entry>();
    ArrayList<Entry> breakfast_after = new ArrayList<Entry>();
    ArrayList<Entry> lunch_before = new ArrayList<Entry>();
    ArrayList<Entry> lunch_after = new ArrayList<Entry>();
    ArrayList<Entry> dinner_before = new ArrayList<Entry>();
    ArrayList<Entry> dinner_after = new ArrayList<Entry>();
    ArrayList<Entry> sleep = new ArrayList<Entry>();

    GraphlistAdapter bAdapter;
    GraphlistAdapter lAdapter;
    GraphlistAdapter dAdapter;
    GraphlistAdapter sAdapter;

    float bbave=0, baave=0, lbave=0, laave=0, dbave=0, daave=0, slave=0; // 각 식사 정보별 평균값
    int bvisible = 0, lvisible = 0, dvisible = 0, svisible = 0; // 각 그래프의 보일지 말지에 대한 시그널

    public static Fragment currentfragment; // 현재 프래그먼트를 나타내기 위한 변수

    // 그래프 어떻게 그릴지 생각한다.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentfragment = this; // 현재 프래그먼트 정의
        restart(); // 모든 변수 초기화
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.floating_graph, container, false);

        // 각 그래프에 대한 id 정의
        LineChart graph_blinechart = (LineChart)rootView.findViewById(R.id.Buddy_Graph_Blinechart);
        LineChart graph_llinechart = (LineChart)rootView.findViewById(R.id.Buddy_Graph_Llinechart);
        LineChart graph_dlinechart = (LineChart)rootView.findViewById(R.id.Buddy_Graph_Dlinechart);
        LineChart graph_slinechart = (LineChart)rootView.findViewById(R.id.Buddy_Graph_Slinechart);
        BarChart graph_barchart = (BarChart)rootView.findViewById(R.id.Buddy_Graph_Barchart);

        // 각 그래프의 데이터가 존재하지 않을 경우에 대한 textview (데이터가 없습니다)
        TextView nobgraph = (TextView)rootView.findViewById(R.id.Buddy_Graph_Noblinechart);
        TextView nolgraph = (TextView)rootView.findViewById(R.id.Buddy_Graph_Nollinechart);
        TextView nodgraph = (TextView)rootView.findViewById(R.id.Buddy_Graph_Nodlinechart);
        TextView nosgraph = (TextView)rootView.findViewById(R.id.Buddy_Graph_Noslinechart);
        TextView nobargraph = (TextView)rootView.findViewById(R.id.Buddy_Graph_Nobarchart);

        // 각 그래프에 대한 layout
        RelativeLayout graph_barlayout = (RelativeLayout)rootView.findViewById(R.id.Buddy_Graph_Barlayout);
        LinearLayout graph_morninglayout = (LinearLayout)rootView.findViewById(R.id.Buddy_Graph_Morninglayout);
        LinearLayout graph_noonlayout = (LinearLayout)rootView.findViewById(R.id.Buddy_Graph_Noonlayout);// ToDo : 해당 파티에 대한 정보 넘겨줘야할듯
        LinearLayout graph_nightlayout = (LinearLayout)rootView.findViewById(R.id.Buddy_Graph_Nightlayout);
        LinearLayout graph_sleeplayout = (LinearLayout)rootView.findViewById(R.id.Buddy_Graph_Sleeplayout);

        ExpandableListView graph_blistview = (ExpandableListView)rootView.findViewById(R.id.Buddy_Graph_Blistview);
        ExpandableListView graph_llistview = (ExpandableListView)rootView.findViewById(R.id.Buddy_Graph_Llistview);
        ExpandableListView graph_dlistview = (ExpandableListView)rootView.findViewById(R.id.Buddy_Graph_Dlistview);
        ExpandableListView graph_slistview = (ExpandableListView)rootView.findViewById(R.id.Buddy_Graph_Slistview);

        ArrayList<Graphlist> graph_bbdata = new ArrayList<>();
        ArrayList<Graphlist> graph_badata = new ArrayList<>();
        ArrayList<Graphlist> graph_lbdata = new ArrayList<>();
        ArrayList<Graphlist> graph_ladata = new ArrayList<>();
        ArrayList<Graphlist> graph_dbdata = new ArrayList<>();
        ArrayList<Graphlist> graph_dadata = new ArrayList<>();
        ArrayList<Graphlist> graph_sleepdata = new ArrayList<>();

        ArrayList<Graphgroup> graph_bgroup = new ArrayList<>();
        ArrayList<Graphgroup> graph_lgroup = new ArrayList<>();
        ArrayList<Graphgroup> graph_dgroup = new ArrayList<>();
        ArrayList<Graphgroup> graph_sgroup = new ArrayList<>();

        // 식전, 식후를 나누어서 저장하기 위한 배열
        ArrayList<BarEntry> before_meal = new ArrayList<>();
        ArrayList<BarEntry> after_meal = new ArrayList<>();

        // 바 그래프의 x축에 대한 배열
        ArrayList<String> bar_xVals = new ArrayList<String>();

        // 바 그래프의 x축 정의
        bar_xVals.add("아침");
        bar_xVals.add("점심");
        bar_xVals.add("저녁");
        bar_xVals.add("취침");

        FloatingActionButton graph_fab = (FloatingActionButton)rootView.findViewById(R.id.Buddy_Graph_Floatingbtn); // floating action button 선언

        graph_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // floating action button에 대한 클릭 리스너
                Intent intent = new Intent(getActivity(), GraphFabActivity.class); // GraphFabActivity로 이동
                startActivity(intent);
            }
        });

        // 로그 다이어리들의 데이터베이스에 접근하기 위한 데이터베이스 셋팅
        dbhelper = new LogDBHelper(getActivity());
        db = dbhelper.getReadableDatabase();
        dbsql = "SELECT * FROM logtable";
        dbcursor = db.rawQuery(dbsql, null);
        count = dbcursor.getCount(); // logtable의 로그 다이어리 갯수를 저장

        if(count !=0) { // 데이터가 존재할 경우 진행
            int bsig = 0, lsig = 0, dsig = 0; // 아침, 점심, 저녁 x축 데이터의 중복 저장을 방지하기 위한 시그널
            for (int i = 0; i < 7; i++) { // 최근 일주일에 대한 그래프를 작성하기 위한 반복문
                if(i==0) { // 아침 식전
                    graphsql = "SELECT * FROM bbtable"; // 테이블 셋팅
                    graphcursor = db.rawQuery(graphsql, null); // 커서 셋팅

                    graphcursor.moveToLast(); // 최근 혈당 정보를 위해 커서 맨 뒤로 이동

                    if(graphcursor.getCount() != 0) { // 데이터 존재할 경우 동작 진행
                        int tablecnt = 0;
                        int zerocnt = 0;

                        // 테이블에 존재하는 데이터가 7개 이상이면 tablecnt를 7로, 그렇지 않으면 데이터의 갯수로 셋팅 (혈당 평균 계산을 위해)
                        if(graphcursor.getCount() > 7)
                            tablecnt = 7;
                        else
                            tablecnt = graphcursor.getCount();

                        for (int j = 0; j < tablecnt-1; j++) // tablecnt만큼 커서 이동
                            graphcursor.moveToPrevious();

                        // 아침 식전 그래프와 평균 계산
                        for(int j=0; j < tablecnt; j++) {
                            breakfast_before.add(new Entry(Float.parseFloat(graphcursor.getString(10)), j));
                            bbave += Float.parseFloat(graphcursor.getString(10));
                            if(Float.parseFloat(graphcursor.getString(10)) == 0) // 임시 데이터에 대한 계산 (혈당 0인 경우 평균에서 빼야하므로)
                                zerocnt += 1;
                            else
                                graph_bbdata.add(new Graphlist(graphcursor.getInt(1), graphcursor.getInt(2), graphcursor.getInt(3),
                                        graphcursor.getString(10), graphcursor.getInt(9)));
                            if(bsig == 0)
                                line_bxVals.add(new String(String.valueOf(graphcursor.getInt(2)) + "/" + graphcursor.getInt(3)));
                            graphcursor.moveToNext();
                        }
                        if(bsig == 0) { // 데이터가 존재하는 경우, 그래프를 보여주고 중복 저장 벙지를 위한 시그널을 바꾼다
                            bvisible = 1;
                            bsig = 1;
                        }
                        bbave /= (tablecnt - zerocnt); // 혈당 평균 계산
                    }
                    graphcursor.close();
                }
                else if(i==1) { // 아침 식후
                    graphsql = "SELECT * FROM batable";
                    graphcursor = db.rawQuery(graphsql, null);

                    graphcursor.moveToLast();

                    if(graphcursor.getCount() != 0) {
                        int tablecnt = 0;
                        int zerocnt = 0;

                        if(graphcursor.getCount() > 7)
                            tablecnt = 7;
                        else
                            tablecnt = graphcursor.getCount();

                        for (int j = 0; j < tablecnt-1; j++)
                            graphcursor.moveToPrevious();

                        for(int j=0; j < tablecnt; j++) {
                            breakfast_after.add(new Entry(Float.parseFloat(graphcursor.getString(10)), j));
                            baave += Float.parseFloat(graphcursor.getString(10));
                            if(Float.parseFloat(graphcursor.getString(10)) == 0)
                                zerocnt += 1;
                            else
                                graph_badata.add(new Graphlist(graphcursor.getInt(1), graphcursor.getInt(2), graphcursor.getInt(3),
                                        graphcursor.getString(10), graphcursor.getInt(9)));
                            if(bsig == 0)
                                line_bxVals.add(new String(String.valueOf(graphcursor.getInt(2)) + "/" + graphcursor.getInt(3)));
                            graphcursor.moveToNext();
                        }
                        if(bsig == 0) {
                            bvisible = 1;
                            bsig = 1;
                        }
                        baave /= (tablecnt - zerocnt);
                    }
                    graphcursor.close();
                }
                else if(i==2) { // 점심 식전
                    graphsql = "SELECT * FROM lbtable";
                    graphcursor = db.rawQuery(graphsql, null);

                    graphcursor.moveToLast();

                    if(graphcursor.getCount() != 0) {
                        int tablecnt = 0;
                        int zerocnt = 0;

                        if(graphcursor.getCount() > 7)
                            tablecnt = 7;
                        else
                            tablecnt = graphcursor.getCount();

                        for (int j = 0; j < tablecnt-1; j++)
                            graphcursor.moveToPrevious();

                        for(int j=0; j < tablecnt; j++) {
                            lunch_before.add(new Entry(Float.parseFloat(graphcursor.getString(10)), j));
                            lbave += Float.parseFloat(graphcursor.getString(10));
                            if(Float.parseFloat(graphcursor.getString(10)) == 0)
                                zerocnt += 1;
                            else
                                graph_lbdata.add(new Graphlist(graphcursor.getInt(1), graphcursor.getInt(2), graphcursor.getInt(3),
                                        graphcursor.getString(10), graphcursor.getInt(9)));
                            if(lsig == 0)
                                line_lxVals.add(new String(String.valueOf(graphcursor.getInt(2)) + "/" + graphcursor.getInt(3)));
                            graphcursor.moveToNext();
                        }
                        if(lsig == 0) {
                            lvisible = 1;
                            lsig = 1;
                        }
                        lbave /= (tablecnt - zerocnt);
                    }
                    graphcursor.close();
                }
                else if(i==3) { // 점심 식후
                    graphsql = "SELECT * FROM latable";
                    graphcursor = db.rawQuery(graphsql, null);

                    graphcursor.moveToLast();

                    if(graphcursor.getCount() != 0) {
                        int tablecnt = 0;
                        int zerocnt = 0;

                        if(graphcursor.getCount() > 7)
                            tablecnt = 7;
                        else
                            tablecnt = graphcursor.getCount();

                        for (int j = 0; j < tablecnt-1; j++)
                            graphcursor.moveToPrevious();

                        for(int j=0; j < tablecnt; j++) {
                            lunch_after.add(new Entry(Float.parseFloat(graphcursor.getString(10)), j));
                            laave += Float.parseFloat(graphcursor.getString(10));
                            if(Float.parseFloat(graphcursor.getString(10)) == 0)
                                zerocnt += 1;
                            else
                                graph_ladata.add(new Graphlist(graphcursor.getInt(1), graphcursor.getInt(2), graphcursor.getInt(3),
                                        graphcursor.getString(10), graphcursor.getInt(9)));
                            if(lsig == 0)
                                line_lxVals.add(new String(String.valueOf(graphcursor.getInt(2)) + "/" + graphcursor.getInt(3)));
                            graphcursor.moveToNext();
                        }
                        if(lsig == 0) {
                            lvisible = 1;
                            lsig = 1;
                        }
                        laave /= (tablecnt - zerocnt);
                    }
                    graphcursor.close();
                }
                else if(i==4) { // 저녁 식전
                    graphsql = "SELECT * FROM dbtable";
                    graphcursor = db.rawQuery(graphsql, null);

                    graphcursor.moveToLast();

                    if(graphcursor.getCount() != 0) {
                        int tablecnt = 0;
                        int zerocnt = 0;

                        if(graphcursor.getCount() > 7)
                            tablecnt = 7;
                        else
                            tablecnt = graphcursor.getCount();

                        for (int j = 0; j < tablecnt-1; j++)
                            graphcursor.moveToPrevious();

                        for(int j=0; j < tablecnt; j++) {
                            dinner_before.add(new Entry(Float.parseFloat(graphcursor.getString(10)), j));
                            dbave += Float.parseFloat(graphcursor.getString(10));
                            if(Float.parseFloat(graphcursor.getString(10)) == 0)
                                zerocnt += 1;
                            else
                                graph_dbdata.add(new Graphlist(graphcursor.getInt(1), graphcursor.getInt(2), graphcursor.getInt(3),
                                        graphcursor.getString(10), graphcursor.getInt(9)));
                            if(dsig == 0)
                                line_dxVals.add(new String(String.valueOf(graphcursor.getInt(2)) + "/" + graphcursor.getInt(3)));
                            graphcursor.moveToNext();
                        }
                        if(dsig == 0) {
                            dvisible = 1;
                            dsig = 1;
                        }
                        dbave /= (tablecnt - zerocnt);
                    }
                    graphcursor.close();
                }
                else if(i==5) { // 저녁 식후
                    graphsql = "SELECT * FROM datable";
                    graphcursor = db.rawQuery(graphsql, null);

                    graphcursor.moveToLast();

                    if(graphcursor.getCount() != 0) {
                        int tablecnt = 0;
                        int zerocnt = 0;

                        if(graphcursor.getCount() > 7)
                            tablecnt = 7;
                        else
                            tablecnt = graphcursor.getCount();

                        for (int j = 0; j < tablecnt-1; j++)
                            graphcursor.moveToPrevious();

                        for(int j=0; j < tablecnt; j++) {
                            dinner_after.add(new Entry(Float.parseFloat(graphcursor.getString(10)), j));
                            daave += Float.parseFloat(graphcursor.getString(10));
                            if(Float.parseFloat(graphcursor.getString(10)) == 0)
                                zerocnt += 1;
                            else
                                graph_dadata.add(new Graphlist(graphcursor.getInt(1), graphcursor.getInt(2), graphcursor.getInt(3),
                                        graphcursor.getString(10), graphcursor.getInt(9)));
                            if(dsig == 0)
                                line_dxVals.add(new String(String.valueOf(graphcursor.getInt(2)) + "/" + graphcursor.getInt(3)));
                            graphcursor.moveToNext();
                        }
                        if(dsig == 0) {
                            dvisible = 1;
                            dsig = 1;
                        }
                        daave /= (tablecnt - zerocnt);
                    }
                    graphcursor.close();
                }
                else if(i==6) { // 취침
                    graphsql = "SELECT * FROM sleeptable";
                    graphcursor = db.rawQuery(graphsql, null);

                    graphcursor.moveToLast();

                    if(graphcursor.getCount() != 0) {
                        int tablecnt = 0;

                        if(graphcursor.getCount() > 7)
                            tablecnt = 7;
                        else
                            tablecnt = graphcursor.getCount();

                        for (int j = 0; j < tablecnt-1; j++)
                            graphcursor.moveToPrevious();

                        for(int j=0; j < tablecnt; j++) {
                            sleep.add(new Entry(Float.parseFloat(graphcursor.getString(10)), j));
                            slave += Float.parseFloat(graphcursor.getString(10));
                            graph_sleepdata.add(new Graphlist(graphcursor.getInt(1), graphcursor.getInt(2), graphcursor.getInt(3),
                                    graphcursor.getString(10), graphcursor.getInt(9)));
                            line_sxVals.add(new String(String.valueOf(graphcursor.getInt(2)) + "/" + graphcursor.getInt(3)));
                            graphcursor.moveToNext();
                        }
                        svisible = 1;
                        slave /= tablecnt;
                    }
                    graphcursor.close();
                }
            }
        }

        graph_bgroup.add(new Graphgroup("식 전", graph_bbdata));
        graph_bgroup.add(new Graphgroup("식 후", graph_badata));
        graph_lgroup.add(new Graphgroup("식 전", graph_lbdata));
        graph_lgroup.add(new Graphgroup("식 후", graph_ladata));
        graph_dgroup.add(new Graphgroup("식 전", graph_dbdata));
        graph_dgroup.add(new Graphgroup("식 후", graph_dadata));
        graph_sgroup.add(new Graphgroup("취침 전", graph_sleepdata));

        bAdapter = new GraphlistAdapter(getActivity(), graph_bgroup);
        lAdapter = new GraphlistAdapter(getActivity(), graph_lgroup);
        dAdapter = new GraphlistAdapter(getActivity(), graph_dgroup);
        sAdapter = new GraphlistAdapter(getActivity(), graph_sgroup);

        // 각 시간대별 라인 그래프 세부 셋팅
        LineDataSet bbdata = new LineDataSet(breakfast_before, "아침 식사 전");
        bbdata.setDrawCircles(true);
        bbdata.setColor(Color.parseColor("#9DE4FF"));
        bbdata.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineDataSet badata = new LineDataSet(breakfast_after, "아침 식사 후");
        badata.setDrawCircles(true);
        badata.setColor(Color.parseColor("#FFC0CB"));
        badata.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineDataSet lbdata = new LineDataSet(lunch_before, "점심 식사 전");
        lbdata.setDrawCircles(true);
        lbdata.setColor(Color.parseColor("#9DE4FF"));
        lbdata.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineDataSet ladata = new LineDataSet(lunch_after, "점심 식사 후");
        ladata.setDrawCircles(true);
        ladata.setColor(Color.parseColor("#FFC0CB"));
        ladata.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineDataSet dbdata = new LineDataSet(dinner_before, "저녁 식사 전");
        dbdata.setDrawCircles(true);
        dbdata.setColor(Color.parseColor("#9DE4FF"));
        dbdata.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineDataSet dadata = new LineDataSet(dinner_after, "저녁 식사 후");
        dadata.setDrawCircles(true);
        dadata.setColor(Color.parseColor("#FFC0CB"));
        dadata.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineDataSet sleepdata = new LineDataSet(sleep, "취침 전");
        sleepdata.setDrawCircles(true);
        sleepdata.setColor(Color.parseColor("#9DE4FF"));
        sleepdata.setAxisDependency(YAxis.AxisDependency.LEFT);

        // 셋팅한 라인 그래프 세부 정보들 시간대별로 다시 묶기
        ArrayList<ILineDataSet> bdataSets = new ArrayList<>();
        bdataSets.add(bbdata);
        bdataSets.add(badata);
        ArrayList<ILineDataSet> ldataSets = new ArrayList<>();
        ldataSets.add(lbdata);
        ldataSets.add(ladata);
        ArrayList<ILineDataSet> ddataSets = new ArrayList<>();
        ddataSets.add(dbdata);
        ddataSets.add(dadata);
        ArrayList<ILineDataSet> sdataSets = new ArrayList<>();
        sdataSets.add(sleepdata);

        // 묶은 정보들 x축 정보들과 함께 각 라인 그래프 데이터로 다시 묶는다
        LineData blineData = new LineData(line_bxVals, bdataSets);
        LineData llineData = new LineData(line_lxVals, ldataSets);
        LineData dlineData = new LineData(line_dxVals, ddataSets);
        LineData slineData = new LineData(line_sxVals, sdataSets);

        // 라인 그래프로 데이터화된 정보들 삽입
        graph_blinechart.setData(blineData);
        graph_llinechart.setData(llineData);
        graph_dlinechart.setData(dlineData);
        graph_slinechart.setData(slineData);

        // 식전 바 그래프 평균 삽입
        before_meal.add(new BarEntry(bbave, 0));
        before_meal.add(new BarEntry(lbave, 1));
        before_meal.add(new BarEntry(dbave, 2));
        before_meal.add(new BarEntry(slave, 3));

        // 식후 바 그래프 평균 삽입
        after_meal.add(new BarEntry(baave, 0));
        after_meal.add(new BarEntry(laave, 1));
        after_meal.add(new BarEntry(daave, 2));

        // 바 그래프 세부 셋팅
        BarDataSet bmdataSet = new BarDataSet(before_meal, "식 전");
        bmdataSet.setColor(Color.parseColor("#9DE4FF"));
        BarDataSet amdataSet = new BarDataSet(after_meal, "식 후");
        amdataSet.setColor(Color.parseColor("#FFC0CB"));

        // 셋팅한 바 그래프 세부 정보들 바 그래프 데이터화
        ArrayList<IBarDataSet> bardataSets = new ArrayList<>();
        bardataSets.add(bmdataSet);
        bardataSets.add(amdataSet);

         // 바 그래프에 x축 데이터와 혈당 평균 정보 삽입
        BarData barData = new BarData(bar_xVals, bardataSets);
        graph_barchart.setData(barData);
        graph_barchart.animateXY(2000, 2000);

        // 로그 데이터베이스 종료
        db.close();
        dbcursor.close();
        dbhelper.close();

        // 각 그래프 변경시 실시간 수정
        graph_blinechart.notifyDataSetChanged();
        graph_llinechart.notifyDataSetChanged();
        graph_dlinechart.notifyDataSetChanged();
        graph_slinechart.notifyDataSetChanged();
        graph_barchart.notifyDataSetChanged();

        graph_blinechart.invalidate();
        graph_llinechart.invalidate();
        graph_dlinechart.invalidate();
        graph_slinechart.invalidate();
        graph_barchart.invalidate();

        if(bvisible == 1) {
            graph_blinechart.setVisibility(View.VISIBLE);
            graph_blistview.setVisibility(View.VISIBLE);
            nobgraph.setVisibility(View.GONE);
        } else {
            graph_blinechart.setVisibility(View.GONE);
            graph_blistview.setVisibility(View.GONE);
            nobgraph.setVisibility(View.VISIBLE);
        }

        // 시그널에 따라 그래프를 보여줄지 말지에 대한 if~else문
        if(lvisible == 1) {
            graph_llinechart.setVisibility(View.VISIBLE);
            graph_llistview.setVisibility(View.VISIBLE);
            nolgraph.setVisibility(View.GONE);
        } else {
            graph_llinechart.setVisibility(View.GONE);
            graph_llistview.setVisibility(View.GONE);
            nolgraph.setVisibility(View.VISIBLE);
        }

        if(dvisible == 1) {
            graph_dlinechart.setVisibility(View.VISIBLE);
            graph_dlistview.setVisibility(View.VISIBLE);
            nodgraph.setVisibility(View.GONE);
        } else {
            graph_dlinechart.setVisibility(View.GONE);
            graph_dlistview.setVisibility(View.GONE);
            nodgraph.setVisibility(View.VISIBLE);
        }

        if(svisible == 1) {
            graph_slinechart.setVisibility(View.VISIBLE);
            graph_slistview.setVisibility(View.VISIBLE);
            nosgraph.setVisibility(View.GONE);
        } else {
            graph_slinechart.setVisibility(View.GONE);
            graph_slistview.setVisibility(View.GONE);
            nosgraph.setVisibility(View.VISIBLE);
        }

        if(bvisible == 1 || lvisible == 1 || dvisible == 1 || svisible == 1) {
            graph_barchart.setVisibility(View.VISIBLE);
            nobargraph.setVisibility(View.GONE);
        }

        // 어떠한 그래프를 선택했는지에 대한 타입 데이터베이스 셋팅
        typeDBHelper = new GraphDBHelper(getActivity());
        typedb = typeDBHelper.getReadableDatabase();
        typesql = "SELECT * FROM graphtable;";
        typecursor = typedb.rawQuery(typesql, null);
        typecursor.moveToFirst();

        // 타입 데이터베이스에 따라 어떠한 그래프를 보여줄지 선택하기 위한 if문
        if(typecursor.getCount() > 0) {
            if(typecursor.getInt(1) == 1) {
                graph_barlayout.setVisibility(View.GONE);
                graph_morninglayout.setVisibility(View.VISIBLE);
                graph_noonlayout.setVisibility(View.GONE);
                graph_nightlayout.setVisibility(View.GONE);
                graph_sleeplayout.setVisibility(View.GONE);
            } else if(typecursor.getInt(1) == 2) {
                graph_barlayout.setVisibility(View.GONE);
                graph_morninglayout.setVisibility(View.GONE);
                graph_noonlayout.setVisibility(View.VISIBLE);
                graph_nightlayout.setVisibility(View.GONE);
                graph_sleeplayout.setVisibility(View.GONE);
            } else if(typecursor.getInt(1) == 3) {
                graph_barlayout.setVisibility(View.GONE);
                graph_morninglayout.setVisibility(View.GONE);
                graph_noonlayout.setVisibility(View.GONE);
                graph_nightlayout.setVisibility(View.VISIBLE);
                graph_sleeplayout.setVisibility(View.GONE);
            } else if(typecursor.getInt(1) == 4) {
                graph_barlayout.setVisibility(View.GONE);
                graph_morninglayout.setVisibility(View.GONE);
                graph_noonlayout.setVisibility(View.GONE);
                graph_nightlayout.setVisibility(View.GONE);
                graph_sleeplayout.setVisibility(View.VISIBLE);
            } else {
                graph_barlayout.setVisibility(View.VISIBLE);
                graph_morninglayout.setVisibility(View.GONE);
                graph_noonlayout.setVisibility(View.GONE);
                graph_nightlayout.setVisibility(View.GONE);
                graph_sleeplayout.setVisibility(View.GONE);
            }
        }

        // 타입 데이터베이스 종료
        typecursor.close();
        typedb.close();
        typeDBHelper.close();

        graph_blistview.setAdapter(bAdapter);
        graph_llistview.setAdapter(lAdapter);
        graph_dlistview.setAdapter(dAdapter);
        graph_slistview.setAdapter(sAdapter);

        for(int i=0; i<bAdapter.getGroupCount(); i++)
            graph_blistview.expandGroup(i);

        for(int i=0; i<lAdapter.getGroupCount(); i++)
            graph_llistview.expandGroup(i);

        for(int i=0; i<dAdapter.getGroupCount(); i++)
            graph_dlistview.expandGroup(i);

        for(int i=0; i<sAdapter.getGroupCount(); i++)
            graph_slistview.expandGroup(i);

        bAdapter.notifyDataSetChanged();
        lAdapter.notifyDataSetChanged();
        dAdapter.notifyDataSetChanged();
        sAdapter.notifyDataSetChanged();

        return rootView;
    }

    public void restart() { // 모든 변수들을 초기화하기 위한 메소드
        count=0;
        bbave=0;
        baave=0;
        lbave=0;
        laave=0;
        dbave=0;
        daave=0;
        slave=0;
        bvisible = 0;
        lvisible = 0;
        dvisible = 0;
        svisible = 0;
        line_bxVals = new ArrayList<>();
        line_lxVals = new ArrayList<>();
        line_dxVals = new ArrayList<>();
        line_sxVals = new ArrayList<>();
        breakfast_before = new ArrayList<>();
        breakfast_after = new ArrayList<>();
        lunch_before = new ArrayList<>();
        lunch_after = new ArrayList<>();
        dinner_before = new ArrayList<>();
        dinner_after = new ArrayList<>();
        sleep = new ArrayList<>();
    }
}

class GraphlistAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<Graphgroup> group;

    public GraphlistAdapter(Context context, ArrayList<Graphgroup> group) {
        this.context = context;
        this.group = group;
    }

    @Override
    public int getGroupCount() {
        return group.size();
    }

    @Override
    public int getChildrenCount(int i) {
        ArrayList<Graphlist> chList = group.get(i).getChild();
        return chList.size();
    }

    @Override
    public Object getGroup(int i) {
        return group.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        ArrayList<Graphlist> chList = group.get(i).getChild();
        return chList.get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        Graphgroup graphgroup = (Graphgroup)getGroup(i);
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_graph_group, null);
        }

        TextView group_meal = (TextView)view.findViewById(R.id.Buddy_Graphlistview_Group);

        group_meal.setText(""+graphgroup.getMeal());

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        Graphlist graphlist = (Graphlist)getChild(i, i1);
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_graph, null);
        }

        TextView child_year = (TextView)view.findViewById(R.id.Buddy_Graphlistview_Year);
        TextView child_mon = (TextView)view.findViewById(R.id.Buddy_Graphlistview_Mon);
        TextView child_day = (TextView)view.findViewById(R.id.Buddy_Graphlistview_Day);
        TextView child_glucose = (TextView)view.findViewById(R.id.Buddy_Graphlistview_Glucose);

        child_year.setText(""+graphlist.getYear());
        child_mon.setText(""+graphlist.getMon());
        child_day.setText(""+graphlist.getDay());
        child_glucose.setText(""+graphlist.getGlucose());

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }
}

class Graphgroup {
    String meal;
    ArrayList<Graphlist> Child;

    public Graphgroup(String meal, ArrayList<Graphlist> child) {
        this.meal = meal;
        Child = child;
    }

    public String getMeal() {
        return meal;
    }

    public void setMeal(String meal) {
        this.meal = meal;
    }

    public ArrayList<Graphlist> getChild() {
        return Child;
    }

    public void setChild(ArrayList<Graphlist> child) {
        Child = child;
    }
}

class Graphlist {
    int Year;
    int Mon;
    int Day;
    String Glucose;
    int Meal;

    public Graphlist(int year, int mon, int day, String glucose, int meal) {
        Year = year;
        Mon = mon;
        Day = day;
        Glucose = glucose;
        Meal = meal;
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

    public String getGlucose() {
        return Glucose;
    }

    public void setGlucose(String glucose) {
        Glucose = glucose;
    }

    public int getMeal() {
        return Meal;
    }

    public void setMeal(int meal) {
        Meal = meal;
    }
}