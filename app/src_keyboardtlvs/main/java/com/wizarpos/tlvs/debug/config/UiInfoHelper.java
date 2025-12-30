package com.wizarpos.tlvs.debug.config;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;

import com.wizarpos.tlvs.debug.DebugConfig;
import com.wizarpos.util.BitmapHelper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class UiInfoHelper {
    private static volatile UiInfoHelper instance = null;

    private UiInfoHelper() {}

    public static UiInfoHelper getInstance() {
        if (instance == null) {
            synchronized (UiInfoHelper.class) {
                if (instance == null) {
                    instance = new UiInfoHelper();
                }
            }
        }
        return instance;
    }

    private UiInfo mUiInfo;

    private Map<Byte, Bitmap> mRawKeys;


    public void initEnv(UiInfo uiInfo){
        this.mUiInfo = uiInfo;
        this.mRawKeys = getRawKeys();

    }

    public Bitmap updateBitmap(byte[] keyboard){
        Bitmap copyBitmap = mUiInfo.getSrc().copy(mUiInfo.getSrc().getConfig(), true);
        Map<Rect, Bitmap> fixedKeys = new HashMap<>();

        fixedKeys.put(mUiInfo.getKey1(), mRawKeys.get(keyboard[0]));
        fixedKeys.put(mUiInfo.getKey2(), mRawKeys.get(keyboard[1]));
        fixedKeys.put(mUiInfo.getKey3(), mRawKeys.get(keyboard[2]));
        fixedKeys.put(mUiInfo.getKey4(), mRawKeys.get(keyboard[3]));
        fixedKeys.put(mUiInfo.getKey5(), mRawKeys.get(keyboard[4]));
        fixedKeys.put(mUiInfo.getKey6(), mRawKeys.get(keyboard[5]));
        fixedKeys.put(mUiInfo.getKey7(), mRawKeys.get(keyboard[6]));
        fixedKeys.put(mUiInfo.getKey8(), mRawKeys.get(keyboard[7]));
        fixedKeys.put(mUiInfo.getKey9(), mRawKeys.get(keyboard[8]));
        fixedKeys.put(mUiInfo.getKey0(), mRawKeys.get(keyboard[9]));

        Bitmap finalBitmap = BitmapHelper.mergeBitmaps(copyBitmap, fixedKeys);

        if(DebugConfig.DEBUG_FUNCTION_KEYS){
            finalBitmap = BitmapHelper.drawRectBorder(finalBitmap, mUiInfo.getBtnOk(), 5, Color.GREEN);
            finalBitmap = BitmapHelper.drawRectBorder(finalBitmap, mUiInfo.getBtnClear(), 5, Color.BLACK);
            finalBitmap = BitmapHelper.drawRectBorder(finalBitmap, mUiInfo.getBtnCancel(), 5, Color.RED);
        }
        if(DebugConfig.DEBUG_SHOW_SAVE_PNG){
            BitmapHelper.saveBitmapAsPng(finalBitmap, new File("/sdcard/test/show"));
        }

        // 如何切换键盘layout。
        return finalBitmap;
    }




    private Map<Byte, Bitmap> getRawKeys(){
        Bitmap src = mUiInfo.getSrc();
        if(DebugConfig.DEBUG_SRC_SAVE_PNG){
            BitmapHelper.saveBitmapAsPng(src, new File("/sdcard/test/src.png"));
        }
        Map<Byte, Bitmap> rawKeys = new HashMap<>();
        Bitmap rawK0, rawK1, rawK2, rawK3, rawK4, rawK5, rawK6, rawK7, rawK8, rawK9;
        if(DebugConfig.DEBUG_KEYBOARD_KEYS){
            rawK0 = BitmapHelper.cropBitmap(src, mUiInfo.getKey0(), Color.BLACK);
            rawK1 = BitmapHelper.cropBitmap(src, mUiInfo.getKey1(), Color.RED);
            rawK2 = BitmapHelper.cropBitmap(src, mUiInfo.getKey2(), Color.GRAY);
            rawK3 = BitmapHelper.cropBitmap(src, mUiInfo.getKey3(), Color.LTGRAY);
            rawK4 = BitmapHelper.cropBitmap(src, mUiInfo.getKey4(), Color.RED);//WHITE
            rawK5 = BitmapHelper.cropBitmap(src, mUiInfo.getKey5(), Color.RED);
            rawK6 = BitmapHelper.cropBitmap(src, mUiInfo.getKey6(), Color.GREEN);
            rawK7 = BitmapHelper.cropBitmap(src, mUiInfo.getKey7(), Color.BLUE);
            rawK8 = BitmapHelper.cropBitmap(src, mUiInfo.getKey8(), Color.YELLOW);
            rawK9 = BitmapHelper.cropBitmap(src, mUiInfo.getKey9(), Color.CYAN);
        }else{
            rawK0 = BitmapHelper.cropBitmap(src, mUiInfo.getKey0());
            rawK1 = BitmapHelper.cropBitmap(src, mUiInfo.getKey1());
            rawK2 = BitmapHelper.cropBitmap(src, mUiInfo.getKey2());
            rawK3 = BitmapHelper.cropBitmap(src, mUiInfo.getKey3());
            rawK4 = BitmapHelper.cropBitmap(src, mUiInfo.getKey4());//WHITE
            rawK5 = BitmapHelper.cropBitmap(src, mUiInfo.getKey5());
            rawK6 = BitmapHelper.cropBitmap(src, mUiInfo.getKey6());
            rawK7 = BitmapHelper.cropBitmap(src, mUiInfo.getKey7());
            rawK8 = BitmapHelper.cropBitmap(src, mUiInfo.getKey8());
            rawK9 = BitmapHelper.cropBitmap(src, mUiInfo.getKey9());
        }

        if(DebugConfig.DEBUG_KEYBOARD_KEYS_SAVE_PNG){
            BitmapHelper.saveBitmapAsPng(rawK0, new File("/sdcard/test/rawK0"));
            BitmapHelper.saveBitmapAsPng(rawK1, new File("/sdcard/test/rawK1"));
            BitmapHelper.saveBitmapAsPng(rawK2, new File("/sdcard/test/rawK2"));
            BitmapHelper.saveBitmapAsPng(rawK3, new File("/sdcard/test/rawK3"));
            BitmapHelper.saveBitmapAsPng(rawK4, new File("/sdcard/test/rawK4"));
            BitmapHelper.saveBitmapAsPng(rawK5, new File("/sdcard/test/rawK5"));
            BitmapHelper.saveBitmapAsPng(rawK6, new File("/sdcard/test/rawK6"));
            BitmapHelper.saveBitmapAsPng(rawK7, new File("/sdcard/test/rawK7"));
            BitmapHelper.saveBitmapAsPng(rawK8, new File("/sdcard/test/rawK8"));
            BitmapHelper.saveBitmapAsPng(rawK9, new File("/sdcard/test/rawK9"));
        }


        rawKeys.put((byte)0x00, rawK0);
        rawKeys.put((byte)0x01, rawK1);
        rawKeys.put((byte)0x02, rawK2);
        rawKeys.put((byte)0x03, rawK3);
        rawKeys.put((byte)0x04, rawK4);
        rawKeys.put((byte)0x05, rawK5);
        rawKeys.put((byte)0x06, rawK6);
        rawKeys.put((byte)0x07, rawK7);
        rawKeys.put((byte)0x08, rawK8);
        rawKeys.put((byte)0x09, rawK9);
        return rawKeys;
    }


}
