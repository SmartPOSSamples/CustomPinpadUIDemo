package com.cloudpos.pinpad.newui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DraggableLineView extends View {

    public interface OnLineConfirmedListener {
        void onLineConfirmed(int distance);
    }

    private Paint linePaint;
    private Paint textPaint;
    private Paint btnPaint;
    private Paint checkPaint;
    private float lineY = 80f;
    private boolean locked = false;
    private float lastY = 0f;
    private RectF confirmBtn = new RectF();
    private OnLineConfirmedListener dListener;

    public DraggableLineView(Context context) {
        this(context, null);
    }

    public DraggableLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        linePaint = new Paint();
        linePaint.setColor(Color.YELLOW);
        linePaint.setStrokeWidth(2f);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.YELLOW);
        textPaint.setTextSize(18f);
        btnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnPaint.setColor(Color.GREEN);
        btnPaint.setStyle(Paint.Style.FILL);
        checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        checkPaint.setColor(Color.WHITE);
        checkPaint.setStrokeWidth(3f);
        checkPaint.setStyle(Paint.Style.STROKE);
    }

    public void setOnLineConfirmedListener(OnLineConfirmedListener l) {
        this.dListener = l;
    }

    public int getLineY() {
        return (int) lineY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float height = getHeight();
        float width = getWidth();

        float y = Math.min(Math.max(lineY, 2), height - 2);

        canvas.drawLine(0, y, width, y, linePaint);

        int topY = (int) (y - linePaint.getStrokeWidth() / 2f);
        String text = String.valueOf(topY);
        canvas.drawText(text, 10, y - 12, textPaint);

        confirmBtn.set(width - 60, y - 30, width - 10, y + 30);
        canvas.drawOval(confirmBtn, btnPaint);

        Path path = new Path();
        path.moveTo(confirmBtn.left + 18, y - 2);
        path.lineTo(confirmBtn.left + 28, y + 10);
        path.lineTo(confirmBtn.right - 18, y - 14);
        canvas.drawPath(path, checkPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = y;
                if (!locked && confirmBtn.contains(x, y)) {
                    locked = true;
                    if (dListener != null) {
                        dListener.onLineConfirmed((int) lineY);
                    }
                    invalidate();
                    return true;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!locked) {
                    float dy = y - lastY;
                    lineY = Math.min(Math.max(lineY + dy, 0), getHeight());
                    lastY = y;
                    invalidate();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }
}