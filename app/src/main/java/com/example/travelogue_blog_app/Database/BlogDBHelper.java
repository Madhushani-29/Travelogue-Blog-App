package com.example.travelogue_blog_app.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.example.travelogue_blog_app.Database.Constants;
import com.example.travelogue_blog_app.Model.BlogModel;
import java.util.ArrayList;

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

    // update data
    public void updateData(String id, String title, String content, String location, String image){
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

        // update row
        // return record id of saved blog
        db.update(Constants.TABLE_NAME, values, Constants.C_ID +" =?", new String[] {id});

        // close db connection
        db.close();
    }

    // get all data from a table
    public ArrayList<BlogModel> getAllBlogs(String orderBy){
        ArrayList<BlogModel> blogList=new ArrayList<>();
        String selectQuery="SELECT * FROM " +
                Constants.TABLE_NAME +
                " ORDER BY " +
                orderBy;
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor=db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do {
                // Safely retrieve the column values
                String id = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_TITLE));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_CONTENT));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_LOCATION));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_IMAGE));

                // Create a new BlogModel and add it to the list
                BlogModel blogModel = new BlogModel(id, title, content, location, image);
                blogList.add(blogModel);
            } while (cursor.moveToNext());
        }
        // close connection
        db.close();
        return blogList;
    }

    // search blogs from a table
    public ArrayList<BlogModel> searchBlogs(String query){
        ArrayList<BlogModel> blogList=new ArrayList<>();
        String selectQuery = "SELECT * FROM " +
                Constants.TABLE_NAME +
                " WHERE " +
                Constants.C_TITLE +
                " LIKE '%" +
                query +
                "%'";
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor=db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do {
                // Safely retrieve the column values
                String id = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_TITLE));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_CONTENT));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_LOCATION));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_IMAGE));

                // Create a new BlogModel and add it to the list
                BlogModel blogModel = new BlogModel(id, title, content, location, image);
                blogList.add(blogModel);
            } while (cursor.moveToNext());
        }
        // close connection
        db.close();
        return blogList;
    }

    // get total blog count
    public int getBlogCount(){
        String countQuery="SELECT * FROM "+Constants.TABLE_NAME;
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.rawQuery(countQuery, null);
        int count= cursor.getCount();
        db.close();
        return count;
    }

    // delete single blog
    public void deleteBlogById(String id){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Constants.TABLE_NAME, Constants.C_ID+ " =?", new String [] {id});
        db.close();
    }

    // delete all blogs
    public void deleteAllBlogs(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + Constants.TABLE_NAME);
        db.close();
    }

    // delete multiple blogs once
    public void deleteMultipleBlogsByIds(ArrayList<String> ids) {
        SQLiteDatabase db = getWritableDatabase();
        for (String id : ids) {
            db.delete(Constants.TABLE_NAME, Constants.C_ID + " =?", new String[]{id});
        }
        db.close();
    }
}
