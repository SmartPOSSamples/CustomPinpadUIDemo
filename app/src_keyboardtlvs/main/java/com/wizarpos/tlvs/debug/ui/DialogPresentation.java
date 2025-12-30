package com.wizarpos.tlvs.debug.ui;

import android.app.AlertDialog;
import android.app.Presentation;
import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.wizarpos.util.Logger;

public class DialogPresentation extends Presentation {

    private AlertDialog mAlertDialog;
    public DialogPresentation(Context outerContext, Display display) {
        super(outerContext, display);
        Logger.debug("display(%s)", display);
//        setContentView(R.layout.activity_main);//绑定副屏显示的布局
        mAlertDialog = new AlertDialog.Builder(outerContext).create();

        Window window = mAlertDialog.getWindow();
        // 设置全屏模式
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        // 隐藏导航栏和状态栏
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_FULLSCREEN
        );
        //不依赖任何activity。
        window.setAttributes(getWindow().getAttributes());

    }

    public AlertDialog getmAlertDialog() {
        return mAlertDialog;
    }

}
