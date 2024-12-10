package com.example.travelogue_blog_app.Database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseAuthHelper {
    private static final String TAG = "FirebaseAuthHelper";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public FirebaseAuthHelper() {
        mAuth = FirebaseAuth.getInstance();
        // reference to firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    // new user sign up
    public void signUpUser(String email, String password, final AuthListener listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            listener.onSuccess(user);
                        }
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    // log in
    public void signInUser(String email, String password, final AuthListener listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            listener.onSuccess(user);
                        }
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    // sign out
    public void signOutUser() {
        mAuth.signOut();
    }

    // handle authentication results asynchronously with firebase workflow
    public interface AuthListener {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception exception);
    }

    // get current user
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    // Get reference to the database
    public DatabaseReference getDatabaseReference() {
        return mDatabase;  // Returning the initialized DatabaseReference
    }
}
