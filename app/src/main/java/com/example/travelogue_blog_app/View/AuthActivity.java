package com.example.travelogue_blog_app.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.travelogue_blog_app.Database.BlogDBHelper;
import com.example.travelogue_blog_app.Database.FirebaseAuthHelper;
import com.example.travelogue_blog_app.R;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private FirebaseAuthHelper firebaseAuthHelper;

    private static final int STORAGE_PERMISSION_CODE = 100;
    private Runnable pendingPermissionAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // ui elements
        TextView titleTextView = findViewById(R.id.titleTextView);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button actionButton = findViewById(R.id.actionButton);
        TextView redirectTextView = findViewById(R.id.redirectTextView);

        // initialize firebaseAuthHelper
        firebaseAuthHelper = new FirebaseAuthHelper();  // Initialize here

        // login or signup boolean
        boolean isSignUp = getIntent().getBooleanExtra("isSignUp", true);

        if (isSignUp) {
            titleTextView.setText(R.string.sign_up_label);
            actionButton.setText(R.string.sign_up_label);
            redirectTextView.setText(R.string.already_user_text);
            redirectTextView.setOnClickListener(v -> {
                Intent intent = new Intent(this, AuthActivity.class);
                intent.putExtra("isSignUp", false);
                startActivity(intent);
            });
        } else {
            titleTextView.setText(R.string.sign_in_label);
            actionButton.setText(R.string.sign_in_label);
            redirectTextView.setText(R.string.new_user_text);
            redirectTextView.setOnClickListener(v -> {
                Intent intent = new Intent(this, AuthActivity.class);
                intent.putExtra("isSignUp", true);
                startActivity(intent);
            });
        }

        actionButton.setOnClickListener(v -> {
            if (validateInputs(emailEditText, passwordEditText)) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (isSignUp) {
                    onSignupClick(email, password);
                } else {
                    requestStoragePermission(() -> onLoginClick(email, password));
                }
            }
        });
    }

    private boolean validateInputs(EditText emailEditText, EditText passwordEditText) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_email_required));
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.error_invalid_email));
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_password_required));
            return false;
        }

        if (password.length() < 6) {
            passwordEditText.setError(getString(R.string.error_password_too_short));
            return false;
        }

        return true;
    }

    private void navigateToHome() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onLoginClick(String email, String password) {
        firebaseAuthHelper.signInUser(email, password, new FirebaseAuthHelper.AuthListener() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Toast.makeText(AuthActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                BlogDBHelper dbHelper = new BlogDBHelper(AuthActivity.this);

                dbHelper.syncToSqliteDB(AuthActivity.this, isSuccess -> {
                    if (isSuccess) {
                        navigateToHome();
                    } else {
                        Toast.makeText(AuthActivity.this, "Data sync failed!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(AuthActivity.this, "Login failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onSignupClick(String email, String password) {
        firebaseAuthHelper.signUpUser(email, password, new FirebaseAuthHelper.AuthListener() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // redirect to home and display a toast
                Toast.makeText(AuthActivity.this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                navigateToHome();
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(AuthActivity.this, "Sign up failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestStoragePermission(Runnable onPermissionGranted) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            pendingPermissionAction = onPermissionGranted; // Save the action to run later
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        } else {
            onPermissionGranted.run();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted!", Toast.LENGTH_SHORT).show();

                if (pendingPermissionAction != null) {
                    pendingPermissionAction.run();
                    pendingPermissionAction = null; // Clear the pending action
                }
            } else {
                Toast.makeText(this, "Storage permission denied. Some features may not work.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
