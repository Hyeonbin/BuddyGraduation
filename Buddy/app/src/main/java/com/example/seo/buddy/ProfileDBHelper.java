package com.example.seo.buddy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Seo on 2016-12-26.
 */
public class ProfileDBHelper extends SQLiteOpenHelper { // 프로필에 관련된 테이블들을 다루기 위한 ProfileDBHelper

    // 데이터베이스 및 버젼 정의
    private static final String DATABASE_NAME = "Profiledata.db";
    private static final int DATABASE_VERSION = 1;

    public ProfileDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 데이터베이스에 관련된 profiletable 생성
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE profiletable (_id INTEGER PRIMARY KEY AUTOINCREMENT, photo TEXT, name TEXT, old TEXT, gender TEXT);");
    }

    // 데이터베이스 업그레이드
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS profiletable");
        onCreate(db);
    }

    // 데이터를 삽입하기 위한 메소드
    public void Insert(String p, String n, String o, String g) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO profiletable VALUES(null, '" + p + "', '" + n + "', '" + o + "', '" + g + "');");
        db.close();
    }

    // 해당하는 데이터를 삭제하기 위한 메소드
    public void delete() {
        SQLiteDatabase profiledb = getWritableDatabase();
        String profilesql = "DELETE FROM profiletable;";
        profiledb.execSQL(profilesql);
    }
}
