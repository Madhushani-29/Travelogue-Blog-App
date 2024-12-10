package com.example.travelogue_blog_app.Database;

public class Constants {
    // database name
    public static final String DB_NAME="Blogs_DB";
    //database version
    public static final int DB_VERSION=1;
    //table name
    public static final String TABLE_NAME="Travel_Blogs_Table";

    //table columns
    public static final String C_ID="id";
    public static final String C_TITLE="title";
    public static final String C_CONTENT="content";
    public static final String C_LOCATION="location";
    public static final String C_IMAGE="blog_image";

    //table create sql query
    public static final String CREATE_TABLE=
            "CREATE TABLE " + TABLE_NAME + "("
                    + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + C_TITLE + " TEXT,"
                    + C_CONTENT + " TEXT,"
                    + C_LOCATION + " TEXT,"
                    + C_IMAGE + " TEXT"
            + ")";

    public static final String USERS_COLLECTION = "users";
}
