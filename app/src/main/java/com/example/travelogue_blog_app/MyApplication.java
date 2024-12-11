package com.example.travelogue_blog_app;

import android.app.Application;
import android.util.Log;

import com.example.travelogue_blog_app.Utill.CloudinaryHelper;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Cloudinary with your credentials
        CloudinaryHelper.initialize(
                getString(R.string.cloud_name),
                getString(R.string.api_key),
                getString(R.string.api_secret)
        );
        Log.d("MyApplication", "Cloudinary initialized.");
    }
}

