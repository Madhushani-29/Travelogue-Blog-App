package com.example.travelogue_blog_app.View;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.travelogue_blog_app.Database.BlogDBHelper;
import com.example.travelogue_blog_app.Database.FirebaseAuthHelper;
import com.example.travelogue_blog_app.R;
import com.google.firebase.auth.FirebaseUser;


public class SplashActivity extends AppCompatActivity {
    private FirebaseAuthHelper firebaseAuthHelper;

    @Override
    protected void onStart() {
        super.onStart();
        BlogDBHelper dbHelper = new BlogDBHelper(this);
        dbHelper.syncWithFirebase(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        firebaseAuthHelper = new FirebaseAuthHelper();

        FirebaseUser currentUser = firebaseAuthHelper.getCurrentUser();

        Handler handler = new Handler();

        // check user is logged in or no
        // if there are then navigate to main scrreen
        if (currentUser != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            }, 6000);
        } else {
            // else display the splash screen
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, AuthActivity.class));
                    finish();
                }
            }, 6000);
        }
    }
}