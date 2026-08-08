package com.cloudpos.pinpad.newui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageLineView extends View {

    public interface OnLineConfirmedListener {
        void onLineConfirmed(int distance);

        void onKeyArraysGenerated(int[][] keyLocs, int[][] functionKeyLocs);
    }

    private static final int DRAG_NONE = 0;
    private static final int DRAG_LINE = 1;
    private static final int DRAG_IMAGE = 2;
    private static final int LINE_TOUCH_RADIUS = 60;

    private static final float MAGNIFIER_ZOOM = 3f;
    private static final float MAGNIFIER_SIZE = 200f;

    private static final int HORIZONTAL_MAX = 4;
    private static final int VERTICAL_MAX = 3;

    private static final int MODE_HORIZONTAL = 0;
    private static final int MODE_VERTICAL = 1;
    private static final int MODE_DONE = 2;

    private Bitmap mBitmap;
    private float imgLeft = 0f;
    private float imgTop = 0f;
    private PinLine activeLine;
    private int lineMode = MODE_HORIZONTAL;
    private int dragMode = DRAG_NONE;
    private float lastX = 0f;
    private float lastY = 0f;
    private boolean showMagnifier = false;
    private float magCenterX = 0f;
    private float magCenterY = 0f;
    private int horizontalConfirmed = 0;
    private int verticalConfirmed = 0;

    private Paint linePaint;
    private Paint textPaint;
    private Paint btnPaint;
    private Paint checkPaint;
    private Paint gridPaint;
    private Paint bgPaint;
    private Paint borderPaint;
    private RectF confirmBtn = new RectF();
    private OnLineConfirmedListener dListener;

    private int sizeWidth = 0;
    private int sizeHeight = 0;
    private List<PinLine> lockedLines = new ArrayList<>();
    private List<int[]> rowPairs = new ArrayList<>();
    private List<int[]> colPairs = new ArrayList<>();
    private int[][] generatedKeyLocs = null;
    private int[][] generatedFunctionKeyLocs = null;

    public ImageLineView(Context context) {
        this(context, null);
    }

    public ImageLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activeLine = new PinLine(80f, PinLine.ORIENTATION_HORIZONTAL);
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
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.GRAY);
        gridPaint.setStrokeWidth(1f);
        bgPaint = new Paint();
        bgPaint.setColor(Color.WHITE);
        borderPaint = new Paint();
        borderPaint.setColor(Color.YELLOW);
        borderPaint.setStrokeWidth(2f);
        borderPaint.setStyle(Paint.Style.STROKE);
    }

    public void setImageResource(int resId) {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), resId);
        setImageBitmap(bmp);
    }

    public void setImageBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
        centerImage();
        invalidate();
    }

    private void centerImage() {
        if (mBitmap != null) {
            imgLeft = (getWidth() - mBitmap.getWidth()) / 2f;
            imgTop = (getHeight() - mBitmap.getHeight()) / 2f;
        }
    }

    public void setOnLineConfirmedListener(OnLineConfirmedListener l) {
        this.dListener = l;
    }

    public void setInputSize(int width, int height) {
        this.sizeWidth = width;
        this.sizeHeight = height;
    }

    public int getLineY() {
        if (activeLine == null) {
            return -1;
        }
        return (int) activeLine.getPosition();
    }

    public List<PinLine> getLockedLines() {
        return lockedLines;
    }

    public int getLineMode() {
        return lineMode;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mBitmap != null) {
            imgLeft = (w - mBitmap.getWidth()) / 2f;
            imgTop = (h - mBitmap.getHeight()) / 2f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmap == null) {
            return;
        }
        canvas.drawBitmap(mBitmap, imgLeft, imgTop, null);

        for (PinLine line : lockedLines) {
            drawLine(canvas, line);
        }

        if (activeLine != null) {
            drawLine(canvas, activeLine);
            if (!activeLine.isLocked()) {
                drawConfirmButton(canvas, activeLine);
            }
        }

        if (showMagnifier && activeLine != null) {
            drawMagnifier(canvas);
        }
    }

    private void drawLine(Canvas canvas, PinLine line) {
        float bmpW = mBitmap.getWidth();
        float bmpH = mBitmap.getHeight();
        if (line.getOrientation() == PinLine.ORIENTATION_HORIZONTAL) {
            float ly = imgTop + Math.min(Math.max(line.getPosition(), 0), bmpH);
            canvas.drawLine(imgLeft, ly, imgLeft + bmpW, ly, linePaint);
            int topY = (int) (line.getPosition() - linePaint.getStrokeWidth() / 2f);
            if (topY < 0) topY = 0;
            canvas.drawText(String.valueOf(topY), imgLeft + 10, ly - 12, textPaint);
        } else {
            float lx = imgLeft + Math.min(Math.max(line.getPosition(), 0), bmpW);
            canvas.drawLine(lx, imgTop, lx, imgTop + bmpH, linePaint);
            int leftX = (int) (line.getPosition() - linePaint.getStrokeWidth() / 2f);
            if (leftX < 0) leftX = 0;
            canvas.drawText(String.valueOf(leftX), lx + 6, imgTop + 20, textPaint);
        }
    }

    private void drawConfirmButton(Canvas canvas, PinLine line) {
        float bmpW = mBitmap.getWidth();
        float bmpH = mBitmap.getHeight();
        float cx;
        float cy;
        if (line.getOrientation() == PinLine.ORIENTATION_HORIZONTAL) {
            float ly = imgTop + Math.min(Math.max(line.getPosition(), 0), bmpH);
            cx = imgLeft + bmpW - 35;
            cy = ly;
        } else {
            float lx = imgLeft + Math.min(Math.max(line.getPosition(), 0), bmpW);
            cx = lx;
            cy = imgTop + bmpH - 35;
        }
        confirmBtn.set(cx - 25, cy - 25, cx + 25, cy + 25);
        canvas.drawOval(confirmBtn, btnPaint);

        Path path = new Path();
        path.moveTo(cx - 7, cy);
        path.lineTo(cx - 1, cy + 7);
        path.lineTo(cx + 8, cy - 7);
        canvas.drawPath(path, checkPaint);
    }

    private void drawMagnifier(Canvas canvas) {
        float zoom = MAGNIFIER_ZOOM;
        float magSize = MAGNIFIER_SIZE;

        float centerX;
        float centerY;
        if (activeLine.getOrientation() == PinLine.ORIENTATION_HORIZONTAL) {
            centerX = Math.min(Math.max(magCenterX, imgLeft), imgLeft + mBitmap.getWidth());
            centerY = Math.min(Math.max(imgTop + activeLine.getPosition(), imgTop), imgTop + mBitmap.getHeight());
        } else {
            centerX = Math.min(Math.max(imgLeft + activeLine.getPosition(), imgLeft), imgLeft + mBitmap.getWidth());
            centerY = Math.min(Math.max(magCenterY, imgTop), imgTop + mBitmap.getHeight());
        }

        float magLeft = centerX + 30;
        float magTop = centerY - magSize / 2f;
        if (magLeft + magSize > getWidth()) {
            magLeft = centerX - 30 - magSize;
        }
        if (magTop < 0) {
            magTop = 0;
        }
        if (magTop + magSize > getHeight()) {
            magTop = getHeight() - magSize;
        }
        RectF magRect = new RectF(magLeft, magTop, magLeft + magSize, magTop + magSize);

        float dx = magSize / (2f * zoom) - (centerX - imgLeft);
        float dy = magSize / (2f * zoom) - (centerY - imgTop);

        canvas.save();
        canvas.clipRect(magRect);
        canvas.drawRect(magRect, bgPaint);
        canvas.translate(magRect.left, magRect.top);
        canvas.scale(zoom, zoom);
        canvas.drawBitmap(mBitmap, dx, dy, null);
        canvas.restore();

        canvas.drawRect(magRect, borderPaint);

        float magStep = 10f * zoom;
        for (float gx = magRect.left; gx <= magRect.right; gx += magStep) {
            canvas.drawLine(gx, magRect.top, gx, magRect.bottom, gridPaint);
        }
        for (float gy = magRect.top; gy <= magRect.bottom; gy += magStep) {
            canvas.drawLine(magRect.left, gy, magRect.right, gy, gridPaint);
        }

        if (activeLine.getOrientation() == PinLine.ORIENTATION_HORIZONTAL) {
            float refY = magRect.top + magSize / 2f + zoom * (imgTop + activeLine.getPosition() - centerY);
            canvas.drawLine(magRect.left, refY, magRect.right, refY, linePaint);
        } else {
            float refX = magRect.left + magSize / 2f + zoom * (imgLeft + activeLine.getPosition() - centerX);
            canvas.drawLine(refX, magRect.top, refX, magRect.bottom, linePaint);
        }

        canvas.drawText(String.valueOf((int) activeLine.getPosition()), magRect.left + 4, magRect.top + 14, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mBitmap == null || activeLine == null) {
            return super.onTouchEvent(event);
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                if (isNearActiveLine(x, y)) {
                    dragMode = DRAG_LINE;
                    magCenterX = x;
                    magCenterY = y;
                    showMagnifier = true;
                    invalidate();
                    if (!activeLine.isLocked() && confirmBtn.contains(x, y)) {
                        confirmActiveLine();
                    }
                } else {
                    dragMode = DRAG_IMAGE;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (dragMode == DRAG_LINE && !activeLine.isLocked()) {
                    moveActiveLine(x, y);
                    magCenterX = x;
                    magCenterY = y;
                    invalidate();
                } else if (dragMode == DRAG_IMAGE) {
                    float dx = x - lastX;
                    float dy = y - lastY;
                    imgLeft += dx;
                    imgTop += dy;
                    lastX = x;
                    lastY = y;
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragMode = DRAG_NONE;
                showMagnifier = false;
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private boolean isNearActiveLine(float x, float y) {
        float pos;
        if (activeLine.getOrientation() == PinLine.ORIENTATION_HORIZONTAL) {
            pos = imgTop + Math.min(Math.max(activeLine.getPosition(), 0), mBitmap.getHeight());
            return Math.abs(y - pos) <= LINE_TOUCH_RADIUS;
        } else {
            pos = imgLeft + Math.min(Math.max(activeLine.getPosition(), 0), mBitmap.getWidth());
            return Math.abs(x - pos) <= LINE_TOUCH_RADIUS;
        }
    }

    private void moveActiveLine(float x, float y) {
        if (activeLine.getOrientation() == PinLine.ORIENTATION_HORIZONTAL) {
            float dy = y - lastY;
            activeLine.setPosition(Math.min(Math.max(activeLine.getPosition() + dy, 0), mBitmap.getHeight()));
            lastY = y;
        } else {
            float dx = x - lastX;
            activeLine.setPosition(Math.min(Math.max(activeLine.getPosition() + dx, 0), mBitmap.getWidth()));
            lastX = x;
        }
    }

    private void confirmActiveLine() {
        showMagnifier = false;
        if (activeLine.getOrientation() == PinLine.ORIENTATION_HORIZONTAL) {
            horizontalConfirmed++;
            float curY = activeLine.getPosition();
            PinLine first = new PinLine(curY, PinLine.ORIENTATION_HORIZONTAL);
            first.setLocked(true);
            lockedLines.add(first);
            PinLine second = new PinLine(Math.min(curY + sizeHeight, mBitmap.getHeight()),
                    PinLine.ORIENTATION_HORIZONTAL);
            second.setLocked(true);
            lockedLines.add(second);
            rowPairs.add(new int[]{(int) curY, (int) Math.min(curY + sizeHeight, mBitmap.getHeight())});
            if (horizontalConfirmed >= HORIZONTAL_MAX) {
                lineMode = MODE_VERTICAL;
                activeLine = new PinLine(80f, PinLine.ORIENTATION_VERTICAL);
            } else {
                activeLine = new PinLine(Math.min(curY + sizeHeight * 2, mBitmap.getHeight()),
                        PinLine.ORIENTATION_HORIZONTAL);
            }
        } else {
            verticalConfirmed++;
            float curPos = activeLine.getPosition();
            PinLine first = new PinLine(curPos, PinLine.ORIENTATION_VERTICAL);
            first.setLocked(true);
            lockedLines.add(first);
            PinLine second = new PinLine(Math.min(curPos + sizeWidth, mBitmap.getWidth()),
                    PinLine.ORIENTATION_VERTICAL);
            second.setLocked(true);
            lockedLines.add(second);
            colPairs.add(new int[]{(int) curPos, (int) Math.min(curPos + sizeWidth, mBitmap.getWidth())});
            if (verticalConfirmed >= VERTICAL_MAX) {
                lineMode = MODE_DONE;
                activeLine = null;
                generateKeyArrays();
            } else {
                activeLine = new PinLine(Math.min(curPos + sizeWidth * 2, mBitmap.getWidth()),
                        PinLine.ORIENTATION_VERTICAL);
            }
        }
        if (dListener != null) {
            float val = activeLine != null ? activeLine.getPosition() : -1f;
            dListener.onLineConfirmed((int) val);
        }
        invalidate();
    }

    private void generateKeyArrays() {
        if (rowPairs.size() < 4 || colPairs.size() < 3) {
            return;
        }
        int[] row0 = rowPairs.get(0);
        int[] row1 = rowPairs.get(1);
        int[] row2 = rowPairs.get(2);
        int[] row3 = rowPairs.get(3);
        int[] col0 = colPairs.get(0);
        int[] col1 = colPairs.get(1);
        int[] col2 = colPairs.get(2);

        generatedKeyLocs = new int[10][4];
        generatedKeyLocs[0] = new int[]{col1[0], row3[0], col1[1], row3[1]};
        for (int i = 1; i <= 9; i++) {
            int r = (i - 1) / 3;
            int c = (i - 1) % 3;
            int[] col = colPairs.get(c);
            int[] row = rowPairs.get(r);
            generatedKeyLocs[i] = new int[]{col[0], row[0], col[1], row[1]};
        }

        generatedFunctionKeyLocs = new int[3][];
        generatedFunctionKeyLocs[0] = new int[]{col0[0], row3[0], col0[1], row3[1]};
        generatedFunctionKeyLocs[1] = new int[]{col2[0], row3[0], col2[1], row3[1]};
        generatedFunctionKeyLocs[2] = null;

        Log.d("keyLocs", Arrays.deepToString(generatedKeyLocs));
        Log.d("functionKeyLocs", Arrays.deepToString(generatedFunctionKeyLocs));
        if (dListener != null) {
            dListener.onKeyArraysGenerated(generatedKeyLocs, generatedFunctionKeyLocs);
        }
    }

    public int[][] getGeneratedKeyLocs() {
        return generatedKeyLocs;
    }

    public int[][] getGeneratedFunctionKeyLocs() {
        return generatedFunctionKeyLocs;
    }
}