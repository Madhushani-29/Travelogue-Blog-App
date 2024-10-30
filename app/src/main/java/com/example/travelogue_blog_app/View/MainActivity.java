package com.example.travelogue_blog_app.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelogue_blog_app.Adapter.BlogAdapter;
import com.example.travelogue_blog_app.Database.BlogDBHelper;
import com.example.travelogue_blog_app.Database.Constants;
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
        
        retrieveBlogs();

        // add onclick listener to add button to navigate add blog page
        navigateAddBlogsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // click to start add record activity
                Intent intent=new Intent(MainActivity.this, AddUpdateBlogActivity.class);
                // not a update
                intent.putExtra("isEditMode", false);
                startActivity(intent);
            }
        });
    }

    private void retrieveBlogs() {
        BlogAdapter blogAdapter=new BlogAdapter(MainActivity.this,
                dbHelper.getAllBlogs(Constants.C_ID + " ASC"));
        blogCard.setAdapter(blogAdapter);

        // set title as no of blogs created
        actionBar.setTitle("Total: " + dbHelper.getBlogCount());
    }

    private void searchBlogs(String query) {
        BlogAdapter blogAdapter=new BlogAdapter(MainActivity.this,
                dbHelper.searchBlogs(query));
        blogCard.setAdapter(blogAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        retrieveBlogs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item= menu.findItem(R.id.action_search);
        SearchView searchView=(SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // on search
                searchBlogs(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // on search text changes
                searchBlogs(newText);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}