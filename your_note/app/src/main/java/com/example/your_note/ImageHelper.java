package com.example.your_note;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

public class ImageHelper {

    public static final int REQUEST_PICK_IMAGE = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 2;

    public static void loadRecentImages(Activity activity, LinearLayout previewRow, ImageView imageOverlay, Consumer<Uri> onImageSelected) {
        previewRow.removeAllViews();
        String[] projection = {MediaStore.Images.Media._ID};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
        Cursor cursor = activity.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        );
        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int count = 0;
            while (cursor.moveToNext() && count < 5) {
                long id = cursor.getLong(idColumn);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                FrameLayout frame = new FrameLayout(activity);
                LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(200, 200, 1f);
                frameParams.setMargins(8, 0, 8, 0);
                frame.setLayoutParams(frameParams);
                frame.setBackground(ContextCompat.getDrawable(activity, R.drawable.rounded_outline));
                frame.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
                frame.setClipToOutline(true);

                ImageView imageView = new ImageView(activity);
                FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );
                imageView.setLayoutParams(imageParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(activity).load(contentUri).into(imageView);

                imageView.setOnClickListener(v -> {
                    imageOverlay.setImageURI(contentUri);
                    imageOverlay.setVisibility(View.VISIBLE);
                    onImageSelected.accept(contentUri);
                });

                frame.addView(imageView);
                previewRow.addView(frame);
                count++;
            }
            cursor.close();
        }
    }

    public static void openGallery(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    public static Uri openCamera(Activity activity) {
        ContentValues values = new ContentValues();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + timeStamp + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/YourNote");

        Uri photoUri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (photoUri != null) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(activity, "Не удалось создать файл", Toast.LENGTH_SHORT).show();
        }

        return photoUri;
    }
}
