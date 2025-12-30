package com.wizarpos.tlvs.debug.testcase;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;
import android.view.View;

import com.cloudpos.pinpad.newui.R;
import com.wizarpos.tlvs.debug.config.UiInfo;
import com.wizarpos.tlvs.debug.parser.DataParser;
import com.wizarpos.tlvs.debug.parser.NewUIGenerator;
import com.wizarpos.tlvs.debug.parser.tlv.TlvGenerator;
import com.wizarpos.tlvs.debug.ui.ILayoutHelper;
import com.wizarpos.tlvs.debug.ui.NewUiLayoutHelperImpl;


public class TlvsUiValidator  {
    private static volatile TlvsUiValidator instance = null;


    public static TlvsUiValidator getInstance() {
        if (instance == null) {
            synchronized (TlvsUiValidator.class) {
                if (instance == null) {
                    instance = new TlvsUiValidator();
                }
            }
        }
        return instance;
    }

    public byte[] createKeyBoardTlvs(Context context){
        //            key0, key1, key2, key3, key4, key5, key6, key7, key8, key9,
//        468 * 468 (156 * 156)
        int[][] keyLocs = new int[][]{
                new int[]{ 296, 954, 424, 1082 },   // Key 0   156 * 156  We must start with it Key0

                new int[]{ 145, 551, 273, 679 },    // Key 1    156 * 156
                new int[]{ 296, 551, 424, 679 },    // key 2    156 * 156
                new int[]{ 446, 551, 574, 679 },    // key 3    156 * 156

                new int[]{ 145, 684, 273, 812 },    // key 4    156 * 156
                new int[]{ 296, 684, 424, 812 },    // key 5    156 * 156
                new int[]{ 446, 684, 574, 812 },    // key 6    156 * 156

                new int[]{ 145, 820, 273, 948 },    // key 7
                new int[]{ 296, 820, 424, 948 },    // key 8
                new int[]{ 446, 820, 574, 948},     // key 9
        };
//            ok clear cancel
        int[][]functionKeyLocs = new int[][]{
                { 145, 954, 273, 1082},   //ok
                { 446, 954, 574, 1082 },   //clear
                { 0, 0, 190, 100 },     //cancel
        };
        Point screenSize = DataParser.getScreenSize(context);
        int[] uiLoc = new int[]{0, screenSize.y - 761/**bitmap's height*/, screenSize.x, screenSize.y};
        int[] inputTextLoc = null;//No need to use

        byte[] tlvs = getTlvs(context, uiLoc, inputTextLoc, keyLocs, functionKeyLocs, R.drawable.below);

        return tlvs;
    }


    public void showTlvsLocalUi(Context context) throws Exception {
        byte[] tlvs = createKeyBoardTlvs(context);
        debugUI(context, tlvs);
    }


    public byte[] getTlvs(final Context context, int[] uiLoc, int[] inputTextLoc, int[][] keyLocs, int[][]functionKeyLocs, int resId){

        try {
            byte[] tlvs = TlvGenerator.getInstance().generateTlvs(context, resId, uiLoc, inputTextLoc, keyLocs, functionKeyLocs);

            return tlvs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void debugUI(Context context, byte[] tlvs){
        try {
            debugCustomPINPadUi(context, tlvs); // Local UI debugging
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void debugCustomPINPadUi(Context context, byte[] tlvs){
//        byte[] keyboard = new byte[]{
//                0x01, 0x02, 0x03,
//                0x04, 0x05, 0x06,
//                0x07, 0x08, 0x09,
//                0x00
//        };
        byte[] keyboard = new byte[]{
                0x08, 0x07, 0x09,
                0x02, 0x01, 0x04,
                0x03, 0x05, 0x06,
                0x00
        };
//        byte[] keyboard = new byte[]{
//                0x07, 0x08, 0x09,
//                0x01, 0x02, 0x03,
//                0x04, 0x05, 0x06,
//                0x00
//        };
//        byte[] keyboard = new byte[]{
//                 0x04, 0x05, 0x08,
//                 0x01, 0x07, 0x02,
//                 0x06, 0x03, 0x09,
//                 0x00
//        };
        debugCustomPINPadUi(context, tlvs, keyboard);
    }

    protected void debugCustomPINPadUi(Context context, byte[] tlvs, byte[] keyboard){
        try {
            UiInfo uiInfo = DataParser.getInstance().parserData(context, tlvs);
            View view = NewUIGenerator.getInstance().createNewUiView(context, uiInfo, keyboard);
            AlertDialog alertDialog = NewUiLayoutHelperImpl.getInstance().createAlertDialog(context, view, false, "", "", ILayoutHelper.PIN_ALIGN_DEFAULT);
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
