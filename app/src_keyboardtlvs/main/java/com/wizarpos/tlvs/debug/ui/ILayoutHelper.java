package com.wizarpos.tlvs.debug.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;


/**
 * Created by pengli on 17-7-20.
 */

public interface ILayoutHelper {
    int PIN_ALIGN_LEFT = 0;
    int PIN_ALIGN_CENTER = 1;
    int PIN_ALIGN_RIGHT = 2;
    int PIN_ALIGN_DEFAULT = PIN_ALIGN_RIGHT;
    AlertDialog createAlertDialog(Context context, View view, boolean existDisplay, String line1Text, String line2Text, int pinAlign);

    void updateText(AlertDialog alertDialog, String line1Text, String line2Text, int pinAlign);
    void cancel();
    boolean isShowing();
}
