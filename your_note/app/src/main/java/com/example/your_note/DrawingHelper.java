package com.example.your_note;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DrawingHelper {

    public static String saveDrawing(Context context, DrawingView drawingView) {
        Bitmap bitmap = drawingView.getBitmap();

        File drawingsDir = new File(context.getExternalFilesDir(null), "drawings");
        if (!drawingsDir.exists()) drawingsDir.mkdirs();

        String fileName = "drawing_" + System.currentTimeMillis() + ".png";
        File file = new File(drawingsDir, fileName);

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

