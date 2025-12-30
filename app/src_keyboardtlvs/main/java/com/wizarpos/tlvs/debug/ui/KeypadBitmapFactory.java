package com.wizarpos.tlvs.debug.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.HashMap;
import java.util.Map;

public class KeypadBitmapFactory {

    public static class KeypadResult {
        public Bitmap bitmap;
        public Rect displayRect;
        public Map<String, Rect> keyRects = new HashMap<>();
    }

    /**
     * 创建一个更紧凑的“输入框 + 数字键盘 + CLEAR/CANCEL/OK”Bitmap，
     * OK 放在右下角。
     *
     * @param width  bitmap 宽度，例如 720
     * @param height bitmap 高度，例如 1280
     */
    public static KeypadResult createKeypadBitmap(int width, int height) {
        final KeypadResult result = new KeypadResult();

        // 1. 创建 Bitmap & Canvas
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        // 清空为完全透明 (可选，通常 createBitmap 就是透明的)
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // 2. 画笔
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.TRANSPARENT);

        final Paint rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(4);
        rectPaint.setColor(Color.WHITE);

        final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(0xFF222222); // 深灰背景

        final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(height * 0.045f); // 字稍微大一点

        // 背景
        canvas.drawRect(0, 0, width, height, bgPaint);

        // 3. 布局参数（比之前更紧凑 / 更大）
        int paddingSide = width * 30 / width;   // 左右内边距 ~30
        int paddingTop = height * 40 / height;  // 顶部内边距 ~40
        int gapX = width * 12 / width;          // 横向间距 ~12
        int gapY = height * 12 / height;        // 纵向间距 ~12

        // 输入框高度单独控制：高度约为总高度的 8%
        int displayHeight = height * 160 / height;   // ~100px for 1280
        // 按钮高度略大一点：总高度的约 9~10%
        int btnHeight = height * 120 / height;       // ~120px

        int availWidth = width - 2 * paddingSide;
        int btnWidth = (availWidth - 2 * gapX) / 3; // 3 列

        // 每一列的 left/right
        int x0Left = paddingSide;
        int x0Right = x0Left + btnWidth;
        int x1Left = x0Right + gapX;
        int x1Right = x1Left + btnWidth;
        int x2Left = x1Right + gapX;
        int x2Right = x2Left + btnWidth;

        // 4. 输入框 Rect（顶部）
        int displayTop = paddingTop * 10;
        int displayBottom = displayTop + displayHeight;
        Rect displayRect = new Rect(paddingSide, displayTop,
                width - paddingSide, displayBottom);
        result.displayRect = displayRect;

        canvas.drawRect(displayRect, fillPaint);
        canvas.drawRect(displayRect, rectPaint);

        // 5. 计算各行的 top/bottom
        int row1Top = displayBottom + gapY;
        int row1Bottom = row1Top + btnHeight;

        int row2Top = row1Bottom + gapY;
        int row2Bottom = row2Top + btnHeight;

        int row3Top = row2Bottom + gapY;
        int row3Bottom = row3Top + btnHeight;

        int row4Top = row3Bottom + gapY;
        int row4Bottom = row4Top + btnHeight;

        // 最后一行：ONLY OK 在右下角
        int row5Top = row4Bottom + gapY;
        int row5Bottom = row5Top + btnHeight;

        // 工具方法：画一个键并记录 Rect
        MyBiConsumer<String, Rect> drawKey = new MyBiConsumer<String, Rect>() {
            @Override
            public void accept(String label, Rect rect) {
                canvas.drawRect(rect, fillPaint);
                canvas.drawRect(rect, rectPaint);

                Paint.FontMetrics fm = textPaint.getFontMetrics();
                float cx = rect.centerX();
                float cy = rect.centerY() - (fm.ascent + fm.descent) / 2;
                canvas.drawText(label, cx, cy, textPaint);

                result.keyRects.put(label, rect);
            }
        };

        // 6. 数字键 1~9

        // Row 1: 1,2,3
        drawKey.accept("1", new Rect(x0Left, row1Top, x0Right, row1Bottom));
        drawKey.accept("2", new Rect(x1Left, row1Top, x1Right, row1Bottom));
        drawKey.accept("3", new Rect(x2Left, row1Top, x2Right, row1Bottom));

        // Row 2: 4,5,6
        drawKey.accept("4", new Rect(x0Left, row2Top, x0Right, row2Bottom));
        drawKey.accept("5", new Rect(x1Left, row2Top, x1Right, row2Bottom));
        drawKey.accept("6", new Rect(x2Left, row2Top, x2Right, row2Bottom));

        // Row 3: 7,8,9
        drawKey.accept("7", new Rect(x0Left, row3Top, x0Right, row3Bottom));
        drawKey.accept("8", new Rect(x1Left, row3Top, x1Right, row3Bottom));
        drawKey.accept("9", new Rect(x2Left, row3Top, x2Right, row3Bottom));

        // 7. 第 4 行：CLEAR 0 CANCEL
        drawKey.accept("CLEAR", new Rect(x0Left, row4Top, x0Right, row4Bottom));
        drawKey.accept("0",     new Rect(x1Left, row4Top, x1Right, row4Bottom));
        drawKey.accept("CANCEL",new Rect(x2Left, row4Top, x2Right, row4Bottom));

        // 8. 第 5 行：ONLY OK 在右下角，其它两格暂时空着，后续你也可以再用
        Rect okRect = new Rect(x0Left, row5Top, x2Right, row5Bottom);
        drawKey.accept("OK", okRect);

        result.bitmap = bmp;
        return result;
    }

}
