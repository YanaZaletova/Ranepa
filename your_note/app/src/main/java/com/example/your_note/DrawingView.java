package com.example.your_note;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private class Stroke {
        Path path;
        Paint paint;
        boolean isEraser;

        Stroke(int color, float strokeWidth, boolean isEraser) {
            path = new Path();
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(strokeWidth);
            this.isEraser = isEraser;

            if (isEraser) {
                paint.setColor(Color.WHITE);
            } else {
                paint.setColor(color);
            }
        }
    }

    private List<Stroke> strokes = new ArrayList<>();
    private Stroke currentStroke;

    private int currentColor = Color.BLACK;
    private float currentStrokeWidth = 8f;
    private boolean isEraser = false;

    public DrawingView(Context context) {
        super(context);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public float getCurrentStrokeWidth() {
        return currentStrokeWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Stroke stroke : strokes) {
            canvas.drawPath(stroke.path, stroke.paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentStroke = new Stroke(currentColor, currentStrokeWidth, isEraser);
                currentStroke.path.moveTo(x, y);
                strokes.add(currentStroke);
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (currentStroke != null) {
                    currentStroke.path.lineTo(x, y);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                currentStroke = null;
                break;
        }
        return true;
    }

    public void setPaintColor(int color) {
        currentColor = color;
        isEraser = false;
    }

    public void setStrokeWidth(float width) {
        currentStrokeWidth = width;
    }

    public void enableEraser(boolean enable) {
        isEraser = enable;
    }

    public void clear() {
        strokes.clear();
        invalidate();
    }
}
