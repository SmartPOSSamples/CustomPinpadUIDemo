package com.wizarpos.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

public class BitmapHelper {
    public static boolean saveBitmapAsPng(Bitmap bitmap, File outFile) {
        if (bitmap == null || outFile == null) return false;

        FileOutputStream fos = null;
        try {
            // 确保目录存在
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            fos = new FileOutputStream(outFile);
            // PNG 格式固定为无损，不需要设置 quality
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;

        } finally {
            try {
                if (fos != null) fos.close();
            } catch (Exception ignored) {}
        }
    }

    public static Bitmap cropBitmap(Bitmap src, Rect rect) {
        if (src == null || rect == null) return null;

        // 限制 rect 在 bitmap 范围内（防越界）
        Rect safeRect = new Rect(
                Math.max(0, rect.left),
                Math.max(0, rect.top),
                Math.min(src.getWidth(), rect.right),
                Math.min(src.getHeight(), rect.bottom)
        );

        int width = safeRect.width();
        int height = safeRect.height();

        if (width <= 0 || height <= 0) {
            return null; // 无效区域
        }
//        Bitmap bitmap = Bitmap.createBitmap(src, safeRect.left, safeRect.top, width, height);
//        return addInnerBorder(bitmap, 10, Color.BLACK);
        return Bitmap.createBitmap(src, safeRect.left, safeRect.top, width, height);
    }
    public static Bitmap cropBitmap(Bitmap src, Rect rect, int color) {
        if (src == null || rect == null) return null;

        // 限制 rect 在 bitmap 范围内（防越界）
        Rect safeRect = new Rect(
                Math.max(0, rect.left),
                Math.max(0, rect.top),
                Math.min(src.getWidth(), rect.right),
                Math.min(src.getHeight(), rect.bottom)
        );

        int width = safeRect.width();
        int height = safeRect.height();

        if (width <= 0 || height <= 0) {
            return null; // 无效区域
        }
        Bitmap bitmap = Bitmap.createBitmap(src, safeRect.left, safeRect.top, width, height);
        return addInnerBorder(bitmap, 10, color);
//        return Bitmap.createBitmap(src, safeRect.left, safeRect.top, width, height);
    }

    public static Bitmap mergeBitmaps(Bitmap baseBitmap, Map<Rect, Bitmap> overlays) {
        if (baseBitmap == null || overlays == null || overlays.isEmpty()) {
            return baseBitmap;
        }

        // 创建一个可编辑副本（ARGB_8888）
        Bitmap result = baseBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        for (Map.Entry<Rect, Bitmap> entry : overlays.entrySet()) {
            Rect rect = entry.getKey();
            Bitmap overlay = entry.getValue();
            if(overlay != null){
                // 画上去
                canvas.drawBitmap(overlay, rect.left, rect.top, paint);
            }else{
                Logger.error("mergeBitmaps happen error(%s)",rect);
            }
        }

        return result;
    }

    /**
     * 创建一个透明背景的 Bitmap，将 sourceBitmap 放置在指定位置并返回合成后的 Bitmap
     *
     * @param sourceBitmap 要绘制的原始 bitmap
     * @param bgWidth 背景的宽度
     * @param bgHeight 背景的高度
     * @param left 放置位置的 X 坐标（背景坐标）
     * @param top 放置位置的 Y 坐标（背景坐标）
     * @return 合成后的 bitmap（ARGB_8888）
     */
    public static Bitmap createBitmapWithOverlay(Bitmap sourceBitmap,
                                                 int bgWidth,
                                                 int bgHeight,
                                                 int left,
                                                 int top) {

        if (sourceBitmap == null) return null;

        // 创建一个透明背景的空白Bitmap（必须使用 ARGB_8888 才能保留透明像素）
        Bitmap bgBitmap = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);

        // 创建画布
        Canvas canvas = new Canvas(bgBitmap);

        // 清空为完全透明 (可选，通常 createBitmap 就是透明的)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // 将 sourceBitmap 绘制到指定位置
        canvas.drawBitmap(sourceBitmap, left, top, null);

        return bgBitmap;
    }

    /**
     * 在 Bitmap 上绘制内边框（不会改变图像尺寸）
     *
     * @param bitmap   原始 Bitmap
     * @param borderWidth 边框宽度（px）
     * @param borderColor 边框颜色（如 Color.RED）
     * @return 带有内边框的新 Bitmap
     */
    public static Bitmap addInnerBorder(Bitmap bitmap, int borderWidth, int borderColor) {
        if (bitmap == null) return null;

        // 创建可编辑的 ARGB_8888 bitmap（保留透明度）
        Bitmap output = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(borderColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);

        // 内边框的范围：注意边框是居中在 Stroke 上的，因此必须 offset 一半
        float half = borderWidth / 2f;
        RectF rect = new RectF(
                half,
                half,
                bitmap.getWidth() - half,
                bitmap.getHeight() - half
        );

        canvas.drawRect(rect, paint);
        return output;
    }

    public static byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap getBitmapById(Context context, int resId){
        Bitmap bitmap = BitmapFactory.decodeResource(
                context.getResources(),
                resId
        );
        return bitmap;
    }

    public static byte[] bitmapToBytesById(Context context, int resId) {
        Bitmap bitmap = getBitmapById(context, resId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap drawRectBorder(
            Bitmap src,
            Rect rect,
            int borderWidthPx,
            int borderColor
    ) {
        if (src == null || rect == null || borderWidthPx <= 0) {
            return src;
        }

        // 创建可变 Bitmap（不修改原图）
        Bitmap mutable = src.copy(
                src.getConfig() != null ? src.getConfig() : Bitmap.Config.ARGB_8888,
                true
        );

        Canvas canvas = new Canvas(mutable);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(borderColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidthPx);

        /*
         * 由于 STROKE 是以 Rect 边缘为中心绘制，
         * 需要 inset 一半线宽，保证边框不被裁剪
         */
        RectF rectF = new RectF(rect);
        float half = borderWidthPx / 2f;
        rectF.inset(half, half);

        canvas.drawRect(rectF, paint);

        return mutable;
    }


}
