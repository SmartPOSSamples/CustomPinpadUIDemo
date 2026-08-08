package com.wizarpos.tlvs.debug.testcase;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;
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
    private int[] cancelKeyLocs = new int[]{ 0, 0, 190, 100};

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

    public int[] getImageSize(Context context, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);
        return new int[]{options.outWidth, options.outHeight};
    }

    public byte[] createKeyBoardTlvs(Context context){
        return createKeyBoardTlvs(context, R.drawable.below3);
    }

    public byte[] createKeyBoardTlvs(Context context, int resId){
        int[] imgSize = getImageSize(context, resId);
        Log.d("keyBoardImg", "w=" + imgSize[0] + " h=" + imgSize[1]);

        int[][] keyLocs = new int[][]{
                {174, 260, 306, 330}/*Key0*/,
                {25, 20, 157, 90}/*Key1*/, {174, 20, 306, 90}/*Key2*/, {323, 20, 455, 90}/*Key3*/,
                {25, 100, 157, 170}/*Key4*/, {174, 100, 306, 170}/*Key5*/, {323, 100, 455, 170}/*Key6*/,
                {25, 180, 157, 250}/*Key7*/, {174, 180, 306, 250}/*Key8*/, {323, 180, 455, 250}/*Key9*/
        };

        int[][] functionKeyLocs = new int[][]{
                // Key                 BackSpace(Clear)      Cancel
                {25, 260, 157, 330},  {323, 260, 455, 330}, null//,  { 0, 0-uiLoc[1], 190, 100-uiLoc[1] }
        };


        int[] inputTextLoc = null;//No need to use

        Point screenSize = DataParser.getScreenSize(context);

        int[] uiLoc = new int[]{0, screenSize.y - imgSize[1], screenSize.x, screenSize.y};

        getLocRelativeToUi(functionKeyLocs, uiLoc)[2] = cancelKeyLocs;

        return getTlvs(context, uiLoc, inputTextLoc,
                getLocRelativeToUi(keyLocs, uiLoc),
                functionKeyLocs, resId);
    }


    public void showTlvsLocalUi(Context context) throws Exception {
        byte[] tlvs = createKeyBoardTlvs(context);
        debugUI(context, tlvs);
    }

    public void showTlvsFromArrays(Context context, int[][] keyLocs, int[][] functionKeyLocs) {
        debugUI(context, getTlvsFromArrays(context, keyLocs, functionKeyLocs));
    }

    public byte[] getTlvsFromArrays(Context context, int[][] keyLocs, int[][] functionKeyLocs) {
        return getTlvsFromArrays(context, keyLocs, functionKeyLocs, R.drawable.below3);
    }

    public byte[] getTlvsFromArrays(Context context, int[][] keyLocs, int[][] functionKeyLocs, int resId) {
        int[] imgSize = getImageSize(context, resId);
        Log.d("keyBoardImg", "w=" + imgSize[0] + " h=" + imgSize[1]);
        Point screenSize = DataParser.getScreenSize(context);
        int[] uiLoc = new int[]{0, screenSize.y - imgSize[1], screenSize.x, screenSize.y};
        getLocRelativeToUi(functionKeyLocs, uiLoc)[2] = cancelKeyLocs;
        return getTlvs(context, uiLoc, null,
                getLocRelativeToUi(keyLocs, uiLoc),
                functionKeyLocs, resId);
    }


    private int[][] getLocRelativeToUi(int[][] keyLocs, int[] uiLoc) {
        if (keyLocs == null || uiLoc == null || uiLoc.length < 2) {
            return keyLocs;
        }
        int offsetX = uiLoc[0];
        int offsetY = uiLoc[1];
        for (int i = 0; i < keyLocs.length; i++) {
            int[] loc = keyLocs[i];
            if(loc == null) break;
            loc[0] = loc[0] + offsetX;
            loc[1] = loc[1] + offsetY;
            loc[2] = loc[2] + offsetX;
            loc[3] = loc[3] + offsetY;
        }
        return keyLocs;
    }

    public byte[] getTlvs(final Context context, int[] uiLoc, int[] inputTextLoc, int[][] keyLocs, int[][]functionKeyLocs, int resId){
        try {
            return TlvGenerator.getInstance().generateTlvs(context, resId, uiLoc, inputTextLoc, keyLocs, functionKeyLocs);
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
