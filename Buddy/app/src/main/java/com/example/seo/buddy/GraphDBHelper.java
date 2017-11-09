package com.example.seo.buddy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Seo on 2017-02-12.
 */
public class GraphDBHelper extends SQLiteOpenHelper { // 어떠한 그래프를 보여줄지를 선택하기 위한 데이터베이스인 GraphDBHelper

    // 데이터베이스와 버전 정의
    private static final String DATABASE_NAME = "Graph.db";
    private static final int DATABASE_VERSION = 1;

    public GraphDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 데이터베이스 생성
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE graphtable (_id INTEGER PRIMARY KEY AUTOINCREMENT, type INTEGER);");
    }

    // 데이터베이스 업그레이드
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS graphtable");
        onCreate(db);
    }

    // 데이터베이스에 아이템 삽입
    public void Insert(int g) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO graphtable VALUES(null, '" + g + "');");
        db.close();
    }

    // 데이터베이스 테이블 삭제
    public void delete() {
        SQLiteDatabase profiledb = getWritableDatabase();
        String profilesql = "DELETE FROM graphtable;";
        profiledb.execSQL(profilesql);
    }
}
