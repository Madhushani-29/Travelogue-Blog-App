package com.example.travelogue_blog_app.View;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.travelogue_blog_app.R;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Fetch references
        TextView titleTextView = findViewById(R.id.titleTextView);
        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        Button actionButton = findViewById(R.id.actionButton);
        TextView redirectTextView = findViewById(R.id.redirectTextView);

        // Determine the activity type
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
                if (isSignUp) {
                    Toast.makeText(this, "Sign-Up successful", Toast.LENGTH_SHORT).show();
                    // Proceed with Sign-Up logic
                } else {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                    // Proceed with Login logic
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
}
