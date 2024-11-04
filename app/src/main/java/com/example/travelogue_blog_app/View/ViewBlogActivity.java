package com.example.travelogue_blog_app.View;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travelogue_blog_app.Database.BlogDBHelper;
import com.example.travelogue_blog_app.Database.Constants;
import com.example.travelogue_blog_app.R;

import java.util.List;

public class ViewBlogActivity extends AppCompatActivity {
    // views
    private ImageView image;
    private TextView title, content, location;
    private Button shareEmailButton, shareFacebookButton;

    // action bar
    private ActionBar actionBar;

    private String blogId;

    // hold blog image path to use with share
    private String blogImage;

    private BlogDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_blog);

        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.view_blog_title);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // get record id
        Intent intent = getIntent();
        blogId = intent.getStringExtra("BLOG_ID");

        // init db helper
        dbHelper = new BlogDBHelper(this);

        // init views
        image = findViewById(R.id.blogImage);
        title = findViewById(R.id.titleText);
        content = findViewById(R.id.contentText);
        location = findViewById(R.id.locationText);
        shareEmailButton = findViewById(R.id.shareEmailButton);
        shareFacebookButton = findViewById(R.id.shareFacebookButton);

        displayBlogDetails();

        shareEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareBlogViaEmail();
            }
        });

        shareFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareBlogViaFacebook();
            }
        });
    }

    private void shareBlogViaEmail() {
        Intent shareIntent = new Intent(Intent.ACTION_SENDTO);
        shareIntent.setData(Uri.parse("mailto:"));

        String emailBody = getString(R.string.email_body, title.getText().toString(), location.getText().toString(), content.getText().toString());

        // set subject and body text
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        Uri imageUri = null;

        // use the content URI directly if it's in the content:// format
        if (blogImage != null && blogImage.startsWith("content://")) {
            imageUri = Uri.parse(blogImage);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant read URI permission for the image
        } else {
            Toast.makeText(this, R.string.invalid_image_uri, Toast.LENGTH_SHORT).show();
            return;
        }

        // show chooser dialog restricted to email clients
        Intent chooser = Intent.createChooser(shareIntent, getString(R.string.share_via_email));

        // grant read permission to all apps receiving the intent
        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        // Start the share intent
        startActivity(chooser);
    }

    private void shareBlogViaFacebook() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");

        String emailBody = getString(R.string.email_body, title.getText().toString(), location.getText().toString(), content.getText().toString());

        // set the subject and body text
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        Uri imageUri = null;

        // use the content URI directly if it's in the content:// format
        if (blogImage != null && blogImage.startsWith("content://")) {
            imageUri = Uri.parse(blogImage);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            // read URI permission for the image
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            Toast.makeText(this, R.string.invalid_image_uri, Toast.LENGTH_SHORT).show();
            return;
        }

        // set the package to limit the chooser to Facebook
        shareIntent.setPackage("com.facebook.katana"); // Facebook app package name

        try {
            startActivity(shareIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.facebook_app_not_installed, Toast.LENGTH_SHORT).show();
        }
    }

    private void displayBlogDetails() {
        // get records
        // based on this query get data with id
        String selectQuery = "SELECT * FROM "
                + Constants.TABLE_NAME
                + " WHERE "
                + Constants.C_ID
                + " =\""
                + blogId
                + "\"";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                // Safely retrieve the column values
                String blogId = "" + cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_ID));
                String blogTitle = "" + cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_TITLE));
                String blogContent = "" + cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_CONTENT));
                String blogLocation = "" + cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_LOCATION));
                blogImage = "" + cursor.getString(cursor.getColumnIndexOrThrow(Constants.C_IMAGE));

                // set data
                title.setText(blogTitle);
                content.setText(blogContent);
                location.setText(blogLocation);
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
