package com.example.myapplication.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for image processing operations.
 */
public class ImageUtils {
    
    private static final String TAG = "ImageUtils";
    
    /**
     * Converts an image URI to a Base64 encoded string.
     * 
     * @param context The application context
     * @param imageUri The URI of the image to convert
     * @return Base64 encoded string of the image, or null if conversion fails
     */
    public static String convertImageToBase64(Context context, Uri imageUri) {
        if (context == null || imageUri == null) {
            Log.e(TAG, "Context or imageUri is null");
            return null;
        }
        
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "Could not open input stream for URI: " + imageUri);
                return null;
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            
            byte[] imageBytes = outputStream.toByteArray();
            String base64String = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
            
            inputStream.close();
            outputStream.close();
            
            Log.d(TAG, "Successfully converted image to Base64, length: " + base64String.length());
            return base64String;
            
        } catch (IOException e) {
            Log.e(TAG, "Error converting image to Base64", e);
            return null;
        }
    }
}
