package com.example.travelogue_blog_app.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelogue_blog_app.Adapter.BlogAdapter;
import com.example.travelogue_blog_app.Database.BlogDBHelper;
import com.example.travelogue_blog_app.Database.Constants;
import com.example.travelogue_blog_app.R;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // ui components
    private FloatingActionButton navigateAddBlogsBtn;
    private RecyclerView blogCard;

    // db helper
    private BlogDBHelper dbHelper;

    // action bar
    ActionBar actionBar;

    // sort options
    String orderByTitleAsc=Constants.C_TITLE + " ASC";
    String orderByTitleDesc=Constants.C_TITLE + " DESC";
    String orderByLocationAsc=Constants.C_LOCATION + " ASC";
    String orderByLocationDesc=Constants.C_LOCATION + " DESC";
    String orderByID=Constants.C_ID + " ASC";

    // selected ID list to delete
    private ArrayList<String> selectedIds = new ArrayList<>();

    // for refreshing use the last selected sorting
    String currentOrderByStatus=orderByID;

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

        // by default sort by id
        retrieveBlogs(orderByID);

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

    private void retrieveBlogs(String orderBy) {
        currentOrderByStatus=orderBy;
        BlogAdapter blogAdapter = new BlogAdapter(MainActivity.this, dbHelper.getAllBlogs(orderBy), selectedIds);
        blogCard.setAdapter(blogAdapter);

        // set title as no of blogs created
        actionBar.setTitle("Total: " + dbHelper.getBlogCount());
    }

    private void searchBlogs(String query) {
        BlogAdapter blogAdapter=new BlogAdapter(MainActivity.this,
                dbHelper.searchBlogs(query), selectedIds);
        blogCard.setAdapter(blogAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // by default sort by id
        retrieveBlogs(currentOrderByStatus);
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
        // handle menu items
        int id=item.getItemId();
        if (id==R.id.action_sort){
            // show sort options
            sortOptionDialog();
        } else if (id==R.id.action_delete_all) {
            // check if there are any blogs to delete
            if (dbHelper.getBlogCount() > 0) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete All Blogs")
                        .setMessage("Are you sure you want to delete all blogs?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            dbHelper.deleteAllBlogs();
                            onResume();
                            Toast.makeText(this, "All blogs deleted", Toast.LENGTH_SHORT).show(); // Optional: Show a toast message
                        })
                        .setNegativeButton("No", null)
                        .show();
            } else {
                // show a message there are no blogs
                Toast.makeText(this, "No blogs available to delete", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.action_delete_selected) {
            if (!selectedIds.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Blogs")
                        .setMessage("Are you sure you want to delete selected blogs?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            dbHelper.deleteMultipleBlogsByIds(selectedIds);
                            retrieveBlogs(currentOrderByStatus); // Refresh the blog list
                            selectedIds.clear(); // Clear selection after deletion
                        })
                        .setNegativeButton("No", null)
                        .show();
            } else {
                Toast.makeText(this, "No blogs selected", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void sortOptionDialog() {
        // options to display
        String [] options={
                "Title Ascending",
                "Title Descending",
                "Location Ascending",
                "Location Descending"
        };
        // dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Sort By").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // handle options click
                if (which==0){
                    retrieveBlogs(orderByTitleAsc);
                } else if (which==1) {
                    retrieveBlogs(orderByTitleDesc);
                } else if (which==2) {
                    retrieveBlogs(orderByLocationAsc);
                } else if (which==3) {
                    retrieveBlogs(orderByLocationDesc);
                }
            }
        }).create().show(); // display dialog
    }
}