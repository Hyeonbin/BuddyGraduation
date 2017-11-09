package com.example.seo.buddy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by Seo on 2016-12-26.
 */
public class LogDBHelper extends SQLiteOpenHelper{ // 로그 다이어리에 관련된 테이블들을 다루기 위한 LogDBHelper

    // 데이터베이스 및 버젼 정의
    private static final String DATABASE_NAME = "Logdata.db";
    private static final int DATABASE_VERSION = 1;

    public LogDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 데이터베이스에 관련된 모든 테이블들 생성
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE logtable (_id INTEGER PRIMARY KEY AUTOINCREMENT," + " year INTEGER, month INTEGER," +
                " day INTEGER, hour INTEGER, min INTEGER, ampm TEXT, location TEXT, photo TEXT, meal INTEGER,  glucose TEXT, dairy TEXT);");
        db.execSQL("CREATE TABLE bbtable (_id INTEGER PRIMARY KEY AUTOINCREMENT," + " year INTEGER, month INTEGER," +
                " day INTEGER, hour INTEGER, min INTEGER, ampm TEXT, location TEXT, photo TEXT, meal INTEGER,  glucose TEXT, dairy TEXT);");
        db.execSQL("CREATE TABLE batable (_id INTEGER PRIMARY KEY AUTOINCREMENT," + " year INTEGER, month INTEGER," +
                " day INTEGER, hour INTEGER, min INTEGER, ampm TEXT, location TEXT, photo TEXT, meal INTEGER,  glucose TEXT, dairy TEXT);");
        db.execSQL("CREATE TABLE lbtable (_id INTEGER PRIMARY KEY AUTOINCREMENT," + " year INTEGER, month INTEGER," +
                " day INTEGER, hour INTEGER, min INTEGER, ampm TEXT, location TEXT, photo TEXT, meal INTEGER,  glucose TEXT, dairy TEXT);");
        db.execSQL("CREATE TABLE latable (_id INTEGER PRIMARY KEY AUTOINCREMENT," + " year INTEGER, month INTEGER," +
                " day INTEGER, hour INTEGER, min INTEGER, ampm TEXT, location TEXT, photo TEXT, meal INTEGER,  glucose TEXT, dairy TEXT);");
        db.execSQL("CREATE TABLE dbtable (_id INTEGER PRIMARY KEY AUTOINCREMENT," + " year INTEGER, month INTEGER," +
                " day INTEGER, hour INTEGER, min INTEGER, ampm TEXT, location TEXT, photo TEXT, meal INTEGER,  glucose TEXT, dairy TEXT);");
        db.execSQL("CREATE TABLE datable (_id INTEGER PRIMARY KEY AUTOINCREMENT," + " year INTEGER, month INTEGER," +
                " day INTEGER, hour INTEGER, min INTEGER, ampm TEXT, location TEXT, photo TEXT, meal INTEGER,  glucose TEXT, dairy TEXT);");
        db.execSQL("CREATE TABLE sleeptable (_id INTEGER PRIMARY KEY AUTOINCREMENT," + " year INTEGER, month INTEGER," +
                " day INTEGER, hour INTEGER, min INTEGER, ampm TEXT, location TEXT, photo TEXT, meal INTEGER,  glucose TEXT, dairy TEXT);");
    }

    // 데이터베이스 업그레이드
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS logtable");
        db.execSQL("DROP TABLE IF EXISTS bbtable");
        db.execSQL("DROP TABLE IF EXISTS batable");
        db.execSQL("DROP TABLE IF EXISTS lbtable");
        db.execSQL("DROP TABLE IF EXISTS latable");
        db.execSQL("DROP TABLE IF EXISTS dbtable");
        db.execSQL("DROP TABLE IF EXISTS datable");
        db.execSQL("DROP TABLE IF EXISTS sleeptable");

        onCreate(db);
    }

    // 그래프를 위한 테이블들에 대한 업데이트를 위한 메소드
    public void Updatetableforgraph(String table, int y, int m, int d, int h, int mi, String a, String l, String p, int me, String g, String da){
        // 해당 테이블과 전체 로그 다이어리 테이블인 logtable에 대해서 셋팅
        SQLiteDatabase db = getWritableDatabase();
        SQLiteDatabase ddb = getWritableDatabase();
        String sql = "SELECT * FROM " + table + " WHERE year = " + y + " AND month = " + m + " AND day = " + d + ";";
        String dbsql = "SELECT * FROM logtable WHERE year = " + y + " AND month = " + m + " AND day = " + d + " AND meal = " + me + ";";
        Cursor cursor = db.rawQuery(sql, null);
        Cursor dbcursor = ddb.rawQuery(dbsql, null);

        cursor.moveToFirst();
        dbcursor.moveToFirst();

        // 인덱스 정보 얻어오기
        int index = cursor.getInt(0);
        int dbindex = dbcursor.getInt(0);

        // 업데이트 하기 위한 정보들 셋팅
        ContentValues args = new ContentValues();
        args.put("hour", h);
        args.put("min", mi);
        args.put("ampm", a);
        args.put("location", l);
        args.put("photo", p);
        args.put("meal", me);
        args.put("glucose", g);
        args.put("dairy", da);

        // 해당 테이블들에서 인덱스로 업데이트할 부분 찾은 후 정보들 업데이트
        db.update(table, args, "_id = " + index, null);
        ddb.update("logtable", args, "_id = " + dbindex, null);
        db.close();
        ddb.close();
        cursor.close();
        dbcursor.close();
    }

    public void Updatedatafornull(int n, String l, String p, int me, String g, String da){
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM logtable WHERE _id = " + n + ";";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();

        ContentValues args = new ContentValues();
        args.put("location", l);
        args.put("photo", p);
        args.put("meal", me);
        args.put("glucose", g);
        args.put("dairy", da);

        db.update("logtable", args, "_id = " + n, null);
        db.close();
        cursor.close();
    }

    // 년, 월, 일로 데이터 유무를 알기 위한 메소드
    public int Searchbydate(String table, int y, int m, int d) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + table + " WHERE year = " + y + " AND month = " + m + " AND day = " + d + ";";
        Cursor cursor = db.rawQuery(sql, null);

        // 데이터가 있는 경우
        if(cursor.moveToFirst()) {
            if(Integer.parseInt(cursor.getString(10)) == 0) // 데이터가 있는데 임시 데이터(혈당 정보 0)인 경우
                return 2;
            else // 정식 데이터인 경우
                return 0;
        }

        cursor.close();
        db.close();
        return 1; // 해당 데이터가 없는 경우
    }

    // 다이어리 내용으로 데이터를 찾기 위한 메소드
    public int Searchbydiary(ArrayList<SearchData> datas, EditText edittext) {
        // 입력 받은 다이어리 내용으로 찾기 위한 준비 작업
        SQLiteDatabase db = getReadableDatabase();
        String text = edittext.getText().toString();
        String sql = "SELECT * FROM logtable WHERE diary LIKE %" + text + "%;";
        Cursor cursor = db.rawQuery(sql, null);

        cursor.moveToLast(); // 커서 위치 셋팅

        datas = new ArrayList<SearchData>();

        if(cursor.getCount() != 0) { // 테이블에 데이터가 있을 경우 동작 진행
            while (true) {
                datas.add(new SearchData(cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getString(6), cursor.getString(7),
                        cursor.getString(8), cursor.getInt(9), cursor.getString(10), cursor.getString(11))); // 배열에 데이터를 입력해준다

                if (cursor.getPosition() == 0) { // 동작이 끝나면 1을 반환하며 종료
                    cursor.close();
                    db.close();
                    return 1;
                }

                cursor.moveToPrevious();
            }
        }

        cursor.close();
        db.close();
        return 0; // 해당 데이터가 없는 경우 0 반환
    }

    // 해당하는 데이터를 삭제하기 위한 메소드
    public void Delete(int index, int meal, int year, int mon, int day) {
        // logtable과 해당 table에 대해서 삭제 동작이 진행된다
        SQLiteDatabase db = getWritableDatabase();
        SQLiteDatabase ldb = getWritableDatabase();

        db.execSQL("DELETE FROM logtable WHERE _id = " + index + ";");

        if(meal == 1) {
            ldb.execSQL("DELETE FROM bbtable WHERE year = " + year + " AND month = " + mon + " AND day = " + day + ";");
        } else if(meal == 2) {
            ldb.execSQL("DELETE FROM batable WHERE year = " + year + " AND month = " + mon + " AND day = " + day + ";");
        } else if(meal == 3) {
            ldb.execSQL("DELETE FROM lbtable WHERE year = " + year + " AND month = " + mon + " AND day = " + day + ";");
        } else if(meal == 4) {
            ldb.execSQL("DELETE FROM latable WHERE year = " + year + " AND month = " + mon + " AND day = " + day + ";");
        } else if(meal == 5) {
            ldb.execSQL("DELETE FROM dbtable WHERE year = " + year + " AND month = " + mon + " AND day = " + day + ";");
        } else if(meal == 6) {
            ldb.execSQL("DELETE FROM datable WHERE year = " + year + " AND month = " + mon + " AND day = " + day + ";");
        } else if(meal == 7) {
            ldb.execSQL("DELETE FROM sleeptable WHERE year = " + year + " AND month = " + mon + " AND day = " + day + ";");
        }

        db.close();
        ldb.close();
    }

    public int getIndex(int y, int m, int d, int h, int mi, String a, String l, String p, int me, String g, String da){
        SQLiteDatabase db = getWritableDatabase();
        String dbsql = "SELECT * FROM logtable WHERE year = " + y + " AND month = " + m + " AND day = " + d + " AND meal = " + me + " AND glucose = 0;";
        Cursor dbcursor = db.rawQuery(dbsql, null);

        dbcursor.moveToFirst();

        int dbindex = dbcursor.getInt(0);

        return dbindex;
    }

    // 데이터를 삽입하기 위한 메소드
    public void Insert(int y, int m, int d, int h, int mi, String a, String l, String p, int me, String g, String da) {
        // logtable과 해당 table에 대해서 삽입 동작이 진행된다
        SQLiteDatabase db = getWritableDatabase();
        SQLiteDatabase ldb = getWritableDatabase();

        db.execSQL("INSERT INTO logtable VALUES(null, '" + y + "', '" + m + "', '" + d + "', '" + h + "', '" + mi + "', '" + a + "', '" + l + "', '" +
                p + "', '" + me + "', '" + g + "', '" + da + "');");

        if(me == 1) {
            ldb.execSQL("INSERT INTO bbtable VALUES(null, '" + y + "', '" + m + "', '" + d + "', '" + h + "', '" + mi + "', '" + a + "', '" + l + "', '" +
                    p + "', '" + me + "', '" + g + "', '" + da + "');");
        } else if(me == 2) {
            ldb.execSQL("INSERT INTO batable VALUES(null, '" + y + "', '" + m + "', '" + d + "', '" + h + "', '" + mi + "', '" + a + "', '" + l + "', '" +
                    p + "', '" + me + "', '" + g + "', '" + da + "');");
        } else if(me == 3) {
            ldb.execSQL("INSERT INTO lbtable VALUES(null, '" + y + "', '" + m + "', '" + d + "', '" + h + "', '" + mi + "', '" + a + "', '" + l + "', '" +
                    p + "', '" + me + "', '" + g + "', '" + da + "');");
        } else if(me == 4) {
            ldb.execSQL("INSERT INTO latable VALUES(null, '" + y + "', '" + m + "', '" + d + "', '" + h + "', '" + mi + "', '" + a + "', '" + l + "', '" +
                    p + "', '" + me + "', '" + g + "', '" + da + "');");
        } else if(me == 5) {
            ldb.execSQL("INSERT INTO dbtable VALUES(null, '" + y + "', '" + m + "', '" + d + "', '" + h + "', '" + mi + "', '" + a + "', '" + l + "', '" +
                    p + "', '" + me + "', '" + g + "', '" + da + "');");
        } else if(me == 6) {
            ldb.execSQL("INSERT INTO datable VALUES(null, '" + y + "', '" + m + "', '" + d + "', '" + h + "', '" + mi + "', '" + a + "', '" + l + "', '" +
                    p + "', '" + me + "', '" + g + "', '" + da + "');");
        } else if(me == 7) {
            ldb.execSQL("INSERT INTO sleeptable VALUES(null, '" + y + "', '" + m + "', '" + d + "', '" + h + "', '" + mi + "', '" + a + "', '" + l + "', '" +
                    p + "', '" + me + "', '" + g + "', '" + da + "');");
        }
        db.close();
        ldb.close();
    }
}
