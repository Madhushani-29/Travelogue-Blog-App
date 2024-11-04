package com.example.travelogue_blog_app.View;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.widget.Toast;
import com.example.travelogue_blog_app.R;

import com.example.travelogue_blog_app.Database.BlogDBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddUpdateBlogActivity extends AppCompatActivity {
    // ui components
    private FloatingActionButton createBlogBtn;
    private EditText titleInputField, contentInputField, locationInputField;
    private ImageView imageView;

    // permission constants
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=101;

    // image picker constants
    private static final int IMAGE_PICK_CAMERA_CODE=102;
    private static final int IMAGE_PICK_GALLERY_CODE=103;

    // arrays of permissions
    // camera and storage
    private String[] cameraPermissions;
    // only storage
    private String[] storagePermissions;

    // image uri and other data to save
    private Uri imageUri;
    private String id, title, content, location;

    // use to check update or create operation
    private boolean isUpdateMode;

    //db helper
    private BlogDBHelper dbHelper;

    //action bar
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_update_blog);

        // initialize
        actionBar=getSupportActionBar();

        // add title
        actionBar.setTitle(R.string.addBlogTitle);
        // add back button
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //initialize views
        createBlogBtn=findViewById(R.id.createButton);
        titleInputField=findViewById(R.id.blogTitle);
        contentInputField=findViewById(R.id.blogContent);
        locationInputField=findViewById(R.id.blogLocation);
        imageView=findViewById(R.id.blogImage);

        // get data from intents
        Intent intent =getIntent();
        isUpdateMode = intent.getBooleanExtra("isEditMode", false);

        if (isUpdateMode){
            actionBar.setTitle(R.string.update_blog_title_text);

            id = intent.getStringExtra("id");
            title = intent.getStringExtra("title");
            content = intent.getStringExtra("content");
            location = intent.getStringExtra("location");
            imageUri=Uri.parse(intent.getStringExtra("image"));

            titleInputField.setText(title);
            contentInputField.setText(content);
            locationInputField.setText(location);

            if (imageUri.toString().equals("null")){
                imageView.setImageResource(R.drawable.ic_addphoto);
            }
            else {
                imageView.setImageURI(imageUri);
            }
        }
        else {
            actionBar.setTitle(R.string.addBlogTitle);
        }

        // init db helper
        dbHelper=new BlogDBHelper(this);

        // init permission array
        cameraPermissions=new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //image select
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickDialog();
            }
        });

        // create new blog when create on create button
        createBlogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    saveData();
                }
            }
        });
    }

    private boolean validateInputs() {
        String title = titleInputField.getText().toString().trim();
        String content = contentInputField.getText().toString().trim();
        String location = locationInputField.getText().toString().trim();

        if (title.isEmpty()) {
            titleInputField.setError(getString(R.string.error_title_required));
            return false;
        }

        if (content.isEmpty()) {
            contentInputField.setError(getString(R.string.error_content_required));
            return false;
        }

        if (location.isEmpty()) {
            locationInputField.setError(getString(R.string.error_location_required));
            return false;
        }

        if (imageUri == null) {
            Toast.makeText(this, R.string.image_required_toast_text, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveData() {
        // get data
        title=""+titleInputField.getText().toString().trim();
        content=""+contentInputField.getText().toString().trim();
        location=""+locationInputField.getText().toString().trim();

        if (isUpdateMode){
            dbHelper.updateData(
                    ""+id,
                    ""+title,
                    ""+content,
                    ""+location,
                    ""+imageUri
            );
            Toast.makeText(this, R.string.update_success_toast_text , Toast.LENGTH_SHORT).show();
        }
        else {
            // save to db
            long id=dbHelper.insertData(
                    ""+title,
                    ""+content,
                    ""+location,
                    ""+imageUri
            );
            Toast.makeText(this, R.string.create_success_toast_text, Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(this, MainActivity.class);
        // FLAG_ACTIVITY_CLEAR_TOP clear all activities on MainActivity in the back stack
        // FLAG_ACTIVITY_NEW_TASK clear all activities on MainActivity in the back stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        // closes the current activity
        finish();
    }

    // display image picker dialog
    private void imagePickDialog(){
        // options to display in dialog
        String[] options = {getString(R.string.camera), getString(R.string.gallery)};

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        // title
        builder.setTitle(R.string.pick_image_from_text);
        // set items
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    // if not permission then request
                    if (!checkCameraPermissions()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                } else if (which==1) {
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });

        // show dialog
        builder.create().show();
    }

    private void pickFromGallery() {
        // intent to select image
        Intent galleryIntent =new Intent(Intent.ACTION_PICK);
        // only get images
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        // pick image from camera
        ContentValues values=new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "Image title");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        // image uri
        imageUri =getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        // intent to open camera
        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    // check storage permission is enabled or no
    private boolean checkStoragePermission(){
        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    // request storage permissions
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    // check camera permission is enabled or no
    private boolean checkCameraPermissions(){
        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    // request camera permissions
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public boolean onSupportNavigateUp(){
        // go back when click on back button
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // result of permission denied/ allowed
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    // if allowed return true else return false
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(this, R.string.camera_storage_permission_required_toast_text, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    // if allowed return true else return false
                    boolean storageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(this, R.string.storage_permission_required_toast_text, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // image picked from camera received here
        if (resultCode==RESULT_OK){
            if (requestCode==IMAGE_PICK_GALLERY_CODE){
                Uri selectedImageUri = data != null ? data.getData() : null;
                if (selectedImageUri != null){
                    imageUri = selectedImageUri;
                    // set image
                    imageView.setImageURI(selectedImageUri);
                } else {
                    Toast.makeText(this, R.string.image_selection_failed_toast_text, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode==IMAGE_PICK_CAMERA_CODE) {
                if (imageUri != null){
                    imageView.setImageURI(imageUri);
                } else {
                    Toast.makeText(this, R.string.image_capture_failed_toast_text, Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
