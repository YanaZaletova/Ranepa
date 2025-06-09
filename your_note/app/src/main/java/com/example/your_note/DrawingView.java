// DrawingView.java
package com.example.your_note;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private Paint currentPaint;
    private Path currentPath;
    private int currentColor = Color.BLACK;
    private int eraserColor = Color.WHITE;

    public void setEraserBackgroundColor(int color) {
        this.eraserColor = color;
    }
    private Bitmap loadedBitmap;

    private float strokeWidth = 5f;
    private boolean eraserMode = false;

    private static class Stroke {
        Path path;
        Paint paint;

        Stroke(Path p, Paint paint) {
            this.path = p;
            this.paint = paint;
        }
    }

    public boolean hasDrawing() {
        return !strokes.isEmpty() || loadedBitmap != null;
    }

    private final List<Stroke> strokes = new ArrayList<>();

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupPaint();
        setZ(10);
        setBackgroundColor(Color.TRANSPARENT);
    }

    private void setupPaint() {
        currentPaint = new Paint();
        currentPaint.setAntiAlias(true);
        currentPaint.setColor(currentColor);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setStrokeWidth(strokeWidth);
    }

    public void setColor(int color) {
        currentColor = color;
        if (!eraserMode) {
            currentPaint.setColor(currentColor);
        }
    }

    public void setStrokeWidth(float width) {
        strokeWidth = width;
        currentPaint.setStrokeWidth(width);
    }

    public void setEraserMode(boolean enabled) {
        eraserMode = enabled;
        if (enabled) {
            currentPaint.setXfermode(null);
            currentPaint.setColor(eraserColor);
            currentPaint.setAlpha(0xFF);
        } else {
            currentPaint.setColor(currentColor);
            currentPaint.setAlpha(0xFF);
        }
    }

    public void clearCanvas() {
        strokes.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (loadedBitmap != null) {
            canvas.drawBitmap(loadedBitmap, 0, 0, null);
        }

        for (Stroke stroke : strokes) {
            canvas.drawPath(stroke.path, stroke.paint);
        }
        if (currentPath != null) {
            canvas.drawPath(currentPath, currentPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getVisibility() != VISIBLE) return false;
        float x = event.getX(), y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath = new Path();
                currentPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                Path drawnPath = new Path(currentPath);
                Paint p = new Paint();
                p.set(currentPaint);
                strokes.add(new Stroke(drawnPath, p));
                currentPath = null;
                invalidate();
                break;
        }
        return true;
    }

    public Bitmap getBitmap() {
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return null;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.loadedBitmap = bitmap;
        invalidate();
    }

}
