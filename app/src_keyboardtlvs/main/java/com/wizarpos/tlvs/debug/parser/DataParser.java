package com.wizarpos.tlvs.debug.parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.WindowManager;

import com.wizarpos.tlvs.debug.DebugConfig;
import com.wizarpos.tlvs.debug.config.UiInfo;
import com.wizarpos.tlvs.debug.parser.tlv.Tlv;
import com.wizarpos.tlvs.debug.parser.tlv.TlvParser;
import com.wizarpos.util.BitmapHelper;
import com.wizarpos.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class DataParser {
    private static volatile DataParser instance = null;

    private DataParser() {}

    public static DataParser getInstance() {
        if (instance == null) {
            synchronized (DataParser.class) {
                if (instance == null) {
                    instance = new DataParser();
                }
            }
        }
        return instance;
    }

    public static byte[] readRawBytes(Context context, int rawResId) throws IOException {
        try (InputStream is = context.getResources().openRawResource(rawResId);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8 * 1024]; // 8KB buffer
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        }
    }

    private int TAG_UI_LOC = 0x11;
    private int TAG_UI = 0x12;
    private int TAG_TEXT_LOC = 0x13;

    private int TAG_K0 = 0x20;//0x20 - 0x29 共10个tag
    private int TAG_K1 = 0x21;
    private int TAG_K2 = 0x22;
    private int TAG_K3 = 0x23;
    private int TAG_K4 = 0x24;
    private int TAG_K5 = 0x25;
    private int TAG_K6 = 0x26;
    private int TAG_K7 = 0x27;
    private int TAG_K8 = 0x28;
    private int TAG_K9 = 0x29;

    private int TAG_OK = 0x2A;      //Button Enter的Image Rect
    private int TAG_CLEAR = 0x2B;   //Button Clear的Image Rect
    private int TAG_CANCEL = 0x2C;  //Button Cancel的Image Rect


    public UiInfo parserData(Context context, byte[] bytes) throws Exception{
        Point size = getScreenSize(context);
        return parserData(bytes, size);

    }
    private UiInfo parserData(byte[] bytes, Point size) throws Exception{
        Map<Integer, Tlv> tlvs = TlvParser.parseAll(bytes);
        UiInfo uiInfo = new UiInfo();

        Tlv tlv = tlvs.get(TAG_UI_LOC);
        if(tlv != null){
            uiInfo.setUiLoc(getKeyRect(tlv));
            Logger.debug("parserData(%s) : %s, %s", size, uiInfo.getUiLoc().width(), uiInfo.getUiLoc().height());
//            int correctionValue = size.y - uiInfo.getUiLoc().height();// TODO 去除相对坐标
//            uiInfo.setCorrectionValue(correctionValue);

            tlv = tlvs.get(TAG_UI);
            if(tlv != null && tlv.value !=null){
                Bitmap targetBitmap = BitmapFactory.decodeByteArray(tlv.value, 0, tlv.value.length);

                Rect uiLoc = uiInfo.getUiLoc();
                if(DebugConfig.DEBUG_TARGET_SAVE_PNG){
                    BitmapHelper.saveBitmapAsPng(targetBitmap, new File("sdcard/test/src_target.png"));
                }
                if(size.x == uiLoc.width() && size.y == uiLoc.height()){
                    uiInfo.setSrc(targetBitmap);
                }else{
                    Logger.debug("parserData(src_raw2 = %s)", uiLoc);
                    Bitmap src = BitmapHelper.createBitmapWithOverlay(targetBitmap, size.x, size.y, uiLoc.left, uiLoc.top);
                    uiInfo.setSrc(src);
                    if(DebugConfig.DEBUG_TARGET_SAVE_PNG){
                        BitmapHelper.saveBitmapAsPng(src, new File("sdcard/final_src_target.png"));
                    }
                }
//            Logger.debug("parserData(%s)", src);
//            BitmapHelper.saveBitmapAsPng(src, new File("sdcard/raw.png"));

                tlv = tlvs.get(TAG_TEXT_LOC);
                if(tlv != null){
                    uiInfo.setTextLoc(getKeyRect(tlv));
                }

                initKeys(tlvs, uiInfo);
                initBtn(tlvs, uiInfo);
                return uiInfo;
            }else{
                Logger.debug("parserTestData(TAG_UI is null:%s", tlv);
                throw new Exception("getKeparserTestDatayRect failed:" + tlv);
            }
        }else{
            Logger.debug("parserTestData( TAG_UI_LOC  is null:%s", tlv);
            throw new Exception("getKeparserTestDatayRect failed:" + tlv);
        }


    }

    private void initBtn(Map<Integer, Tlv> tlvs, UiInfo uiInfo) throws Exception {
        Tlv tlv = tlvs.get(TAG_OK);
        uiInfo.setBtnOk(getKeyRect(tlv));
        tlv = tlvs.get(TAG_CLEAR);
        uiInfo.setBtnClear(getKeyRect(tlv));
        tlv = tlvs.get(TAG_CANCEL);
        uiInfo.setBtnCancel(getKeyRect(tlv));

    }
    public static Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size); // 包含导航栏
        return size;
    }
    private void initKeys(Map<Integer, Tlv> tlvs, UiInfo uiInfo) throws Exception {

        Tlv tlv = tlvs.get(TAG_K0);
        uiInfo.setKey0(getKeyRect(tlv));
        tlv = tlvs.get(TAG_K1);
        uiInfo.setKey1(getKeyRect(tlv));
        tlv = tlvs.get(TAG_K2);
        uiInfo.setKey2(getKeyRect(tlv));
        tlv = tlvs.get(TAG_K3);
        uiInfo.setKey3(getKeyRect(tlv));
        tlv = tlvs.get(TAG_K4);
        uiInfo.setKey4(getKeyRect(tlv));
        tlv = tlvs.get(TAG_K5);
        uiInfo.setKey5(getKeyRect(tlv));
        tlv = tlvs.get(TAG_K6);
        uiInfo.setKey6(getKeyRect(tlv));
        tlv = tlvs.get(TAG_K7);
        uiInfo.setKey7(getKeyRect(tlv));
        tlv = tlvs.get(TAG_K8);
        uiInfo.setKey8(getKeyRect(tlv));
        tlv = tlvs.get(TAG_K9);
        uiInfo.setKey9(getKeyRect(tlv));

    }

    private Rect getKeyRect(Tlv tlv) throws Exception {
        return getKeyRect(tlv, 0);
    }

    private Rect getKeyRect(Tlv tlv, int correctionValue) throws Exception {
        if(tlv != null && tlv.value !=null && tlv.length == 8){
            byte[] loc = tlv.value;
            int left = twoBytesToInt(loc[0], loc[1]);
            int top = twoBytesToInt(loc[2], loc[3]) - correctionValue;
            int right = twoBytesToInt(loc[4], loc[5]);
            int bottom = twoBytesToInt(loc[6], loc[7]) - correctionValue;
            Rect rect = new Rect(left, top, right, bottom);
            Logger.debug("getKeyRect( %s = %s , %s, %s, %s):%s %s", tlv.tagHex(), left, top, right, bottom, rect.width(), rect.height());
            return rect;
        }else{
            Logger.error("getKeyRect failed!(%s)", tlv);
            throw new Exception("getKeyRect failed:" + tlv);
        }
    }

    public static int twoBytesToInt(byte high, byte low) {
        return ((high & 0xFF) << 8) | (low & 0xFF);
    }


}
