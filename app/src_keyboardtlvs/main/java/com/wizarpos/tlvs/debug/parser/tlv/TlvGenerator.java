package com.wizarpos.tlvs.debug.parser.tlv;

import android.content.Context;
import android.graphics.Bitmap;

import com.wizarpos.tlvs.debug.DebugConfig;
import com.wizarpos.util.BitmapHelper;
import com.wizarpos.util.ByteConvert;
import com.wizarpos.util.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TlvGenerator {
    private static volatile TlvGenerator instance = null;

    private TlvGenerator() {}

    public static TlvGenerator getInstance() {
        if (instance == null) {
            synchronized (TlvGenerator.class) {
                if (instance == null) {
                    instance = new TlvGenerator();
                }
            }
        }
        return instance;
    }
    private byte TAG_UI_LOC = 0x11;
    private byte TAG_UI = 0x12;
    private byte TAG_TEXT_LOC = 0x13;

    private byte TAG_K0 = 0x20;//0x20 - 0x29 共10个tag
    private byte TAG_K1 = 0x21;
    private byte TAG_K2 = 0x22;
    private byte TAG_K3 = 0x23;
    private byte TAG_K4 = 0x24;
    private byte TAG_K5 = 0x25;
    private byte TAG_K6 = 0x26;
    private byte TAG_K7 = 0x27;
    private byte TAG_K8 = 0x28;
    private byte TAG_K9 = 0x29;

    private byte TAG_OK = 0x2A;      //Button Enter的Image Rect
    private byte TAG_CLEAR = 0x2B;   //Button Clear的Image Rect
    private byte TAG_CANCEL = 0x2C;  //Button Cancel的Image Rect

    private byte TAG_SOUND = 0x30;
    private byte TAG_BG = 0x31;
    private byte TAG_TG = 0x32;


    public byte[] generateTlvs(Bitmap bitmap, int [] uiLoc, int[] inputTextLoc, int [][] keys, int[][] functionKeys){
        byte[] bitmapData = BitmapHelper.bitmapToBytes(bitmap);
        return generateTlvs(bitmapData, uiLoc, inputTextLoc, keys, functionKeys);
    }


    public byte[] generateTlvs(byte[] bitmapData, int [] uiLoc, int[] inputTextLoc, int [][] keys, int[][] functionKeys){
        List<byte[]> tlvs = new ArrayList<>();

//        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        // left, top, right, bottom
        byte[] tlv = TlvPacker.buildTLV(TAG_UI_LOC, createLocationValue(uiLoc));
        tlvs.add(tlv);

        try {
            tlv = TlvPacker.buildTLV(TAG_UI, bitmapData);//
            tlvs.add(tlv);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        TAG_TEXT_LOC
        if(inputTextLoc != null){
            tlv = TlvPacker.buildTLV(TAG_TEXT_LOC, createLocationValue(inputTextLoc));
            tlvs.add(tlv);
        }

//        k0, k1, k2, k3, k4, k5, k6, k7, k8, k9
        for(int i =0 ; i < 10; i++){
            tlv = TlvPacker.buildTLV((byte)(TAG_K0 + i), createLocationValue(keys[i]));
            tlvs.add(tlv);
        }


//        ok clear cancel
        for(int i =0 ; i < 3; i++){
            tlv = TlvPacker.buildTLV((byte)(TAG_OK + i), createLocationValue(functionKeys[i]));
            tlvs.add(tlv);
        }

        tlv = TlvPacker.buildTLV((byte)TAG_SOUND, new byte[]{0x01});
        tlvs.add(tlv);
        tlv = TlvPacker.buildTLV((byte)TAG_BG, "#FFFFFF".getBytes());
        tlvs.add(tlv);
        tlv = TlvPacker.buildTLV((byte)TAG_TG, "#000000".getBytes());
        tlvs.add(tlv);

        byte[] finalTlvs = TlvPacker.mergeTLVs(tlvs);
        if(DebugConfig.DEBUG_TLVS){
            FileUtil.writeFile(new File("/sdcard/test/gui.tlv"), new ByteArrayInputStream(finalTlvs));
        }
        return finalTlvs;

    }
    public byte[] generateTlvs(Context context, int resId, int [] uiLoc, int[] inputTextLoc, int [][] keys, int[][] functionKeys){
        try {
            return generateTlvs(BitmapHelper.bitmapToBytesById(context, resId), uiLoc, inputTextLoc, keys, functionKeys);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
//    public static byte[] readRawBytes(Context context, int rawResId) throws IOException {
//        try (InputStream is = context.getResources().openRawResource(rawResId);
//             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//
//            byte[] buffer = new byte[8 * 1024]; // 8KB buffer
//            int len;
//            while ((len = is.read(buffer)) != -1) {
//                baos.write(buffer, 0, len);
//            }
//            return baos.toByteArray();
//        }
//    }

    private byte[] createLocationValue(int left, int top, int right, int bottom){
        return ByteConvert.contactBuff(ByteConvert.int2byte2(left), ByteConvert.int2byte2(top), ByteConvert.int2byte2(right), ByteConvert.int2byte2(bottom));
    }

    private byte[] createLocationValue(int [] locs){
        return ByteConvert.contactBuff(ByteConvert.int2byte2(locs[0]), ByteConvert.int2byte2(locs[1]), ByteConvert.int2byte2(locs[2]), ByteConvert.int2byte2(locs[3]));
    }
}
