package com.example.testcdc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.testcdc.entity.User;

import java.util.ArrayList;
import java.util.List;

public class UserDBHelper extends SQLiteOpenHelper {


    private static final String DB_NAME = "user.db";
    private static final String TABLE_NAME = "user_info";

    private static final int DB_VERSION = 2;

    private static UserDBHelper mHelper = null;

    private SQLiteDatabase mRDB = null;

    private SQLiteDatabase mWDB = null;

    public static UserDBHelper getInstance(Context context)
    {
        if(mHelper == null)
        {
            mHelper = new UserDBHelper(context);
        }
        return mHelper;
    }

    private UserDBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name VARCHAR NOT NULL);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 版本更新时候会执行
        String sql = "ALTER TABLE " + TABLE_NAME +" ADD COLUMN phone VARCHAR;";
        db.execSQL(sql);
    }

    public SQLiteDatabase openReadLink()
    {
        if (mRDB == null || !mRDB.isOpen())
        {
            mRDB = mHelper.getReadableDatabase();
        }
        return mRDB;
    }

    public SQLiteDatabase openWriteLink()
    {
        if (mWDB == null || !mWDB.isOpen())
        {
            mWDB = mHelper.getReadableDatabase();
        }
        return mWDB;
    }

    public void closeLink()
    {
        if(mRDB != null && mRDB.isOpen())
        {
            mRDB.close();
            mRDB = null;
        }

        if(mWDB != null && mWDB.isOpen())
        {
            mWDB.close();
            mWDB = null;
        }
    }

    public long insert(User user)
    {
        ContentValues values = new ContentValues();
        values.put("name",user.name);
        long ret = -1;
        mWDB.beginTransaction();
        try {
            ret = mWDB.insert(TABLE_NAME,null,values);
            mWDB.setTransactionSuccessful();
        }finally {
            mWDB.endTransaction();
        }

        return ret;
    }

    public long delete(String name)
    {
        return mWDB.delete(TABLE_NAME,"name=?",new String[]{name});
    }

    public List<User> query()
    {
        List<User> list = new ArrayList<>();

        Cursor cursor = mRDB.query(TABLE_NAME,null,null,null,null,null,null);
        while (cursor.moveToNext())
        {
            User user = new User();
            user.id = cursor.getInt(0);
            user.name = cursor.getString(1);
            list.add(user);
        }
        cursor.close();
        return list;
    }
}
