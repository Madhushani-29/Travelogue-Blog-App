package com.example.travelogue_blog_app.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelogue_blog_app.Database.BlogDBHelper;
import com.example.travelogue_blog_app.R;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    // ui components
    private FloatingActionButton navigateAddBlogsBtn;
    private RecyclerView blogCard;

    // db helper
    private BlogDBHelper dbHelper;

    // action bar
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar=getSupportActionBar();
        actionBar.setTitle("All Blogs");

        //initialize views
        navigateAddBlogsBtn=findViewById(R.id.addBlogButton);
        blogCard=findViewById(R.id.blogCard);

        // init db helper
        dbHelper=new BlogDBHelper(this);

        // add onclick listener to add button to navigate add blog page
        navigateAddBlogsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddUpdateBlogActivity.class));
            }
        });
    }
}