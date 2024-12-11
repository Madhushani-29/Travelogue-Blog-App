package com.example.travelogue_blog_app.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.example.travelogue_blog_app.Utill.CloudinaryHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.travelogue_blog_app.Model.BlogModel;
import com.example.travelogue_blog_app.Utill.NetworkUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
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
    public long insertData(String title, String content, String location, String image, Context context) {
        // insert data into SQLite database
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.C_TITLE, title);
        values.put(Constants.C_CONTENT, content);
        values.put(Constants.C_LOCATION, location);
        values.put(Constants.C_IMAGE, image);

        long id = db.insert(Constants.TABLE_NAME, null, values);

        // close the database connection
        db.close();

        // check if internet is available
        if (NetworkUtils.isInternetAvailable(context)) {
            // Internet is available, save data to Firebase
            saveDataToFirebase(title, content, location, image, String.valueOf(id));
        }
        return id;
    }

    // update data
    public void updateData(String id, String title, String content, String location, String image, Context context){
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

        if (NetworkUtils.isInternetAvailable(context)) {
            // Internet is available, update data in Firebase as well
            updateDataInFirebase(id, title, content, location, image);
        }
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
    public void deleteBlogById(String id, Context context){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Constants.TABLE_NAME, Constants.C_ID+ " =?", new String [] {id});
        db.close();

        if (NetworkUtils.isInternetAvailable(context)) {
            deleteBlogFromFirebase(id);
        }
    }

    // delete all blogs
    public void deleteAllBlogs(Context context){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + Constants.TABLE_NAME);
        db.close();

        if (NetworkUtils.isInternetAvailable(context)) {
            deleteAllBlogsFromFirebase();
        }
    }

    // delete multiple blogs once
    public void deleteMultipleBlogsByIds(ArrayList<String> ids) {
        SQLiteDatabase db = getWritableDatabase();
        for (String id : ids) {
            db.delete(Constants.TABLE_NAME, Constants.C_ID + " =?", new String[]{id});
        }
        db.close();

        // iterate over a array to delete a set of blogs
        for (String id : ids) {
            deleteBlogFromFirebase(id);
        }
    }

    // data save to firebase
    private void saveDataToFirebase(String title, String content, String location, String imagePath, String id) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        new Thread(() -> {
            try {
                BlogModel blogPost = new BlogModel(id, title, content, location, imagePath);

                firestore.collection("blogs").document(id)
                        .set(blogPost)
                        .addOnSuccessListener(aVoid -> Log.d("Firebase", "Blog post saved successfully."))
                        .addOnFailureListener(e -> Log.e("Firebase", "Failed to save blog post: " + e.getMessage()));
            } catch (Exception e) {
            }
        }).start();
    }

    // data update firebase
    private void updateDataInFirebase(String id, String title, String content, String location, String image) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        new Thread(() -> {
            try {
                BlogModel blogPost = new BlogModel(id, title, content, location, image);

                firestore.collection("blogs").document(id)
                        .set(blogPost)  // Overwrites the existing data in Firebase
                        .addOnSuccessListener(aVoid -> Log.d("Firebase", "Blog post updated successfully."))
                        .addOnFailureListener(e -> Log.e("Firebase", "Failed to update blog post: " + e.getMessage()));
            } catch (Exception e) {
                Log.e("Firebase", "Error updating blog post in Firebase: " + e.getMessage());
            }
        }).start();
    }

    // delete single blog from Firebase
    private void deleteBlogFromFirebase(String id) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        new Thread(() -> {
            try {
                firestore.collection("blogs").document(id)
                        .delete()
                        .addOnSuccessListener(aVoid -> Log.d("Firebase", "Blog post deleted successfully."))
                        .addOnFailureListener(e -> Log.e("Firebase", "Failed to delete blog post: " + e.getMessage()));
            } catch (Exception e) {
                Log.e("Firebase", "Error deleting blog post from Firebase: " + e.getMessage());
            }
        }).start();
    }

    // delete all blogs from Firebase
    private void deleteAllBlogsFromFirebase() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        new Thread(() -> {
            try {
                firestore.collection("blogs")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            for (DocumentSnapshot document : querySnapshot) {
                                firestore.collection("blogs").document(document.getId())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> Log.d("Firebase", "Blog post deleted successfully."))
                                        .addOnFailureListener(e -> Log.e("Firebase", "Failed to delete blog post: " + e.getMessage()));
                            }
                        })
                        .addOnFailureListener(e -> Log.e("Firebase", "Failed to fetch blogs for deletion: " + e.getMessage()));
            } catch (Exception e) {
                Log.e("Firebase", "Error deleting all blog posts from Firebase: " + e.getMessage());
            }
        }).start();
    }


}
