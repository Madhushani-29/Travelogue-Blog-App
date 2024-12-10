package com.example.travelogue_blog_app.Model;

public class FirebaseUserModel {
    private String userId;
    private String email;

    public FirebaseUserModel() {
    }

    public FirebaseUserModel(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
