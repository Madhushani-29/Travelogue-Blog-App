package com.example.travelogue_blog_app.Database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.travelogue_blog_app.Utill.SyncCompleteListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.travelogue_blog_app.Model.BlogModel;
import com.example.travelogue_blog_app.Utill.NetworkUtils;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    public long insertData(String title, String content, String location, String image, String creator, Context context) {
        // insert data into SQLite database
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.C_TITLE, title);
        values.put(Constants.C_CONTENT, content);
        values.put(Constants.C_LOCATION, location);
        values.put(Constants.C_IMAGE, image);
        values.put(Constants.C_CREATOR, creator);
        values.put(Constants.C_IS_SYNCED, 0);

        long id = db.insert(Constants.TABLE_NAME, null, values);

        // close the database connection
        db.close();

        // check if internet is available
        if (NetworkUtils.isInternetAvailable(context)) {
            // Internet is available, save data to Firebase
            saveDataToFirebase(title, content, location, image, String.valueOf(id), creator);
        }
        return id;
    }

    // update data
    public void updateData(String id, String title, String content, String location, String image, String creator, Context context){
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
        values.put(Constants.C_CREATOR, creator);
        values.put(Constants.C_IS_SYNCED, 0);

        // update row
        // return record id of saved blog
        db.update(Constants.TABLE_NAME, values, Constants.C_ID +" =?", new String[] {id});

        // close db connection
        db.close();

        if (NetworkUtils.isInternetAvailable(context)) {
            // Internet is available, update data in Firebase as well
            updateDataInFirebase(id, title, content, location, image, creator);
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
                String creator = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_CREATOR));

                // Create a new BlogModel and add it to the list
                BlogModel blogModel = new BlogModel(id, title, content, location, image, creator);
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
                String creator = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_CREATOR));

                // Create a new BlogModel and add it to the list
                BlogModel blogModel = new BlogModel(id, title, content, location, image, creator);
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
        } else {
            // if no internet, store the ID for later syncing
            storeIdForLaterSync(id, context);
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
    public void deleteMultipleBlogsByIds(ArrayList<String> ids, Context context) {
        SQLiteDatabase db = getWritableDatabase();
        for (String id : ids) {
            db.delete(Constants.TABLE_NAME, Constants.C_ID + " =?", new String[]{id});
        }
        db.close();

        if (NetworkUtils.isInternetAvailable(context)) {
            for (String id : ids) {
                deleteBlogFromFirebase(id);
            }
        } else {
            storeIdsForLaterSync(ids, context);
        }
    }

    // data save to firebase
    private void saveDataToFirebase(String title, String content, String location, String imagePath, String id, String creator) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        new Thread(() -> {
            try {
                BlogModel blogPost = new BlogModel(id, title, content, location, imagePath, creator);

                firestore.collection("blogs").document(id)
                        .set(blogPost)
                        .addOnSuccessListener(
                                aVoid ->{
                                    updateSyncStatus(id);
                                    Log.d("Firebase", "Blog post saved successfully.");
                                })
                        .addOnFailureListener(e -> Log.e("Firebase", "Failed to save blog post: " + e.getMessage()));
            } catch (Exception e) {
            }
        }).start();
    }

    // data update firebase
    private void updateDataInFirebase(String id, String title, String content, String location, String image, String creator) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        new Thread(() -> {
            try {
                BlogModel blogPost = new BlogModel(id, title, content, location, image, creator);

                firestore.collection("blogs").document(id)
                        .set(blogPost)  // Overwrites the existing data in Firebase
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Firebase", "Blog post updated successfully.");
                            updateSyncStatus(id);
                        })
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

    // upodate the sync status that use to identify the synced status
    private void updateSyncStatus(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // mark as synced
        values.put(Constants.C_IS_SYNCED, 1);
        db.update(Constants.TABLE_NAME, values, Constants.C_ID + " =?", new String[]{id});
        db.close();
    }

    // upload to fitebase when have connection
    public void syncWithFirebase(Context context) {
        if (NetworkUtils.isInternetAvailable(context)) {
            ArrayList<BlogModel> unsyncedBlogs = getUnsyncedBlogs();
            for (BlogModel blog : unsyncedBlogs) {
                saveDataToFirebase(blog.getTitle(), blog.getContent(), blog.getLocation(),
                        blog.getImage(), blog.getId(), blog.getCreator());
            }
        }
    }

    // get firebase blogs to sqlite database
    public void syncToSqliteDB(Context context, SyncCompleteListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper();
        FirebaseUser currentUser = firebaseAuthHelper.getCurrentUser();

        if (currentUser == null) {
            Log.e("FirebaseSync", "No logged-in user found.");
            listener.onSyncComplete(false);
            return;
        }

        String currentUserId = currentUser.getUid();

        db.collection("blogs")
                .whereEqualTo("creator", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            String content = document.getString("content");
                            String location = document.getString("location");
                            String image = document.getString("image");
                            String id = document.getString("id");
                            String creator = document.getString("creator");

                            BlogModel blog = new BlogModel(id, title, content, location, image, creator);
                            saveBlogToSQLite(context, blog);
                        }
                        // Notify that sync is complete
                        listener.onSyncComplete(true);
                    } else {
                        Log.e("FirebaseSync", "Error fetching blogs from Firebase", task.getException());
                        listener.onSyncComplete(false);
                    }
                });
    }

    // save firebase blogs to sqlite database
    private void saveBlogToSQLite(Context context, BlogModel blog) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.C_TITLE, blog.getTitle());
        values.put(Constants.C_CONTENT, blog.getContent());
        values.put(Constants.C_LOCATION, blog.getLocation());
        values.put(Constants.C_IMAGE, blog.getImage());
        values.put(Constants.C_ID, Integer.parseInt(blog.getId()));
        values.put(Constants.C_CREATOR, blog.getCreator());
        values.put(Constants.C_IS_SYNCED, 1);

        db.insert(Constants.TABLE_NAME, null, values);
        db.close();
    }

    //  get a list of unsynced blogs
    private ArrayList<BlogModel> getUnsyncedBlogs() {
        ArrayList<BlogModel> blogList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + Constants.TABLE_NAME + " WHERE " + Constants.C_IS_SYNCED + " = 0";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_TITLE));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_CONTENT));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_LOCATION));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_IMAGE));
                String creator = cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_CREATOR));

                BlogModel blogModel = new BlogModel(id, title, content, location, image, creator);
                blogList.add(blogModel);
            } while (cursor.moveToNext());
        }
        db.close();
        return blogList;
    }

    // clear full table to logout
    public void clearBlogsToLogout(Context context) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + Constants.TABLE_NAME); // Deletes all rows from the table
        db.close();
    }

    // add for the pending delete list
    private void storeIdForLaterSync(String id, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sync_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> pendingIds = sharedPreferences.getStringSet("pending_blog_deletions", new HashSet<>());
        pendingIds.add(id);
        editor.putStringSet("pending_blog_deletions", pendingIds);
        editor.apply();
        Log.d("Sync", "Blog ID stored for later sync: " + id);
    }

    private void storeIdsForLaterSync(List<String> ids, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sync_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> pendingIds = new HashSet<>(sharedPreferences.getStringSet("pending_blog_deletions", new HashSet<>()));
        pendingIds.addAll(ids);

        editor.putStringSet("pending_blog_deletions", pendingIds);
        editor.apply();
        Log.d("Sync", "Stored blog IDs for later sync: " + pendingIds);
    }

    // delete pending blogs
    public void deleteFirebasePendings(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sync_preferences", Context.MODE_PRIVATE);
        Set<String> pendingIds = new HashSet<>(sharedPreferences.getStringSet("pending_blog_deletions", new HashSet<>()));

        if (pendingIds.isEmpty()) {
            Log.d("Sync", "No pending deletions to process.");
            return;
        }

        if (NetworkUtils.isInternetAvailable(context)) {
            SQLiteDatabase db = getWritableDatabase();
            Iterator<String> iterator = pendingIds.iterator();

            while (iterator.hasNext()) {
                String id = iterator.next();

                deleteBlogFromFirebase(id);
                db.delete(Constants.TABLE_NAME, Constants.C_ID + " =?", new String[]{id});
                iterator.remove();
            }

            db.close();

            // Update SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet("pending_blog_deletions", pendingIds);
            editor.apply();
            Log.d("Sync", "Processed and cleared all pending blog deletions.");
        } else {
            Log.e("Sync", "No internet available. Can't process pending deletions.");
        }
    }

}
