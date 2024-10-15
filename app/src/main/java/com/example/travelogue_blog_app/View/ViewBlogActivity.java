package com.example.travelogue_blog_app.View;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.travelogue_blog_app.Database.BlogDBHelper;
import com.example.travelogue_blog_app.Database.Constants;
import com.example.travelogue_blog_app.Model.BlogModel;
import com.example.travelogue_blog_app.R;

public class ViewBlogActivity extends AppCompatActivity {
    // views
    private ImageView image;
    private TextView title, content, location;

    // action bar
    private ActionBar actionBar;

    private String blogId;

    private BlogDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_blog);

        actionBar=getSupportActionBar();
        actionBar.setTitle("View Blog");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // get record id
        Intent intent=getIntent();
        blogId=intent.getStringExtra("BLOG_ID");

        // init db helper
        dbHelper=new BlogDBHelper(this);

        // init views
        image=findViewById(R.id.blogImage);
        title=findViewById(R.id.titleText);
        content=findViewById(R.id.contentText);
        location=findViewById(R.id.locationText);

        displayBlogDetails();
    }

    private void displayBlogDetails() {
        // get records
        // based on this query get data with id
        String selectQuery="SELECT * FROM "
                + Constants.TABLE_NAME
                + " WHERE "
                + Constants.C_ID
                + " =\""
                + blogId
                + "\"";
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        Cursor cursor=db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do {
                // Safely retrieve the column values
                String blogId = ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_ID));
                String blogTitle = ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_TITLE));
                String blogContent = ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_CONTENT));
                String blogLocation = ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_LOCATION));
                String blogImage = ""+cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_IMAGE));

                // set data
                title.setText(blogTitle);
                content.setText(blogTitle);
                location.setText(blogTitle);
                image.setImageURI(Uri.parse(blogImage));
            } while (cursor.moveToNext());
        }

        db.close();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}