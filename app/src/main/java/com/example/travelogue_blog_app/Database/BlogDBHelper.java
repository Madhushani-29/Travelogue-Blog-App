package com.example.travelogue_blog_app.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.travelogue_blog_app.Database.Constants;

import javax.annotation.Nullable;

public class BlogDBHelper extends SQLiteOpenHelper {
    public BlogDBHelper(@Nullable Context context){
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
    }

    // create database on first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Constants.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // when database version need to upgrade
        // drop the existing table
        db.execSQL("DROP TABLE IF EXISTS "+ Constants.TABLE_NAME);
        // create a new table with updated schema
        onCreate(db);
    }

    // insert data into table
    public long insertData(String title, String content, String location, String image){
        // need to write data
        // then get a writable database
        SQLiteDatabase db=this.getWritableDatabase();

        ContentValues values=new ContentValues();

        // id increment auto in query
        // insert other data
        values.put(Constants.C_TITLE, title);
        values.put(Constants.C_CONTENT, content);
        values.put(Constants.C_LOCATION, location);
        values.put(Constants.C_IMAGE, image);

        // insert row
        // return record id of saved blog
        long id =db.insert(Constants.TABLE_NAME, null, values);

        // close db connection
        db.close();

        return id;
    }
}
