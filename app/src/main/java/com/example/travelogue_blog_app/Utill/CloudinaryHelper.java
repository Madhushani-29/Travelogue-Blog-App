package com.example.travelogue_blog_app.Utill;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

public class CloudinaryHelper {
    private static Cloudinary cloudinary;

    // Initialize Cloudinary
    public static void initialize(String cloudName, String apiKey, String apiSecret) {
        Log.d("CloudinaryHelper", "Initializing Cloudinary...");
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    // Upload image to Cloudinary
    public static String uploadImage(String imagePath, String fileName) throws Exception {
        if (cloudinary == null) {
            throw new IllegalStateException("Cloudinary not initialized. Call initialize() first.");
        }

        Map uploadResult = cloudinary.uploader().upload(imagePath, ObjectUtils.asMap(
                "public_id", fileName,
                "folder", "blog_images"
        ));
        return (String) uploadResult.get("secure_url");
    }

    // Helper to get real file path from content URI
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null; // Return null if path could not be found
    }

    // Method to handle image upload, considering both Content URI and File Path
    public static String handleImageUpload(Context context, Uri imageUri, String fileName) {
        try {
            String realImagePath;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android Q and above, we need to use MediaStore to access the image
                ContentResolver resolver = context.getContentResolver();
                InputStream imageStream = resolver.openInputStream(imageUri);
                File tempFile = new File(context.getCacheDir(), "image.jpg");
                FileOutputStream fos = new FileOutputStream(tempFile);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = imageStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                imageStream.close();

                // Use the temp file path for upload
                realImagePath = tempFile.getAbsolutePath();
            } else {
                // For older versions, directly get the file path from URI
                realImagePath = getRealPathFromURI(context, imageUri);
            }

            // Upload the image to Cloudinary and return the URL
            if (realImagePath != null) {
                return uploadImage(realImagePath, fileName);
            } else {
                Log.e("CloudinaryHelper", "Failed to get real file path.");
                return null;
            }
        } catch (Exception e) {
            Log.e("CloudinaryHelper", "Image upload failed: " + e.getMessage());
            return null;
        }
    }
}
