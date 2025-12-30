package com.wizarpos.tlvs.debug.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.cloudpos.pinpad.newui.R;
import com.wizarpos.util.Logger;

/**
 * Created by pengli on 17-7-20.
 */

public class NewUiLayoutHelperImpl implements ILayoutHelper {



    private static ILayoutHelper instance = new NewUiLayoutHelperImpl();
    public static ILayoutHelper getInstance(){
        return instance;
    }

    AlertDialog mAlertDialog = null;
    DialogPresentation presentation;

    public boolean isShowing(){
        if(mAlertDialog != null){
            return mAlertDialog.isShowing();
        }
        return false;
    }

    /**
     * 隐藏虚拟栏 ，显示的时候再隐藏掉
     *
     * @param window
     */
    private void hideNavigationBar(Window window) {
//        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
//                //布局位于状态栏下方
//                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
//                //全屏
//                View.SYSTEM_UI_FLAG_FULLSCREEN |
//                //隐藏导航栏
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        window.getDecorView().setSystemUiVisibility(uiOptions);
    }

    @Override
    public void cancel() {
        try{
            if(presentation != null){
                presentation.dismiss();
            }
            Window win = mAlertDialog.getWindow();
            win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
            win.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            win.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            mAlertDialog.cancel();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public AlertDialog createAlertDialog(Context mContext, View rootView, boolean existDisplay, String line1Text , String line2Text, int pinAlign) {
//        AlertDialog mAlertDialog = new AlertDialog.Builder(mContext).create();
        Window mAlertDialogWindow = null;
        if(mAlertDialog == null || !mAlertDialog.isShowing()){
            mAlertDialog = new AlertDialog.Builder(mContext).create();
            mAlertDialog.setCanceledOnTouchOutside(false);
            mAlertDialogWindow = mAlertDialog.getWindow();

//            不依赖任何activity。
//            mAlertDialogWindow.setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY));
//    		保证屏幕常亮
            mAlertDialogWindow.addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_SECURE //禁止录屏
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
//            requestWindowFeature(Window.FEATURE_NO_TITLE);
            mAlertDialogWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }else{
            mAlertDialogWindow = mAlertDialog.getWindow();
        }
        mAlertDialogWindow.setGravity( Gravity.BOTTOM | Gravity.LEFT | Gravity.RIGHT);// 从下网上布局,左右无边框
        mAlertDialog.show();// 只有在show完后，才能修改相关控件。

        mAlertDialogWindow.setContentView(rootView);
        hideNavigationBar(mAlertDialogWindow);
//        int pinAlign = getPinAlign(mContext);

        if(existDisplay){
            updateText(mAlertDialog, line1Text, line2Text, pinAlign);
        }else{
            View topPanel = rootView.findViewById(R.id.topPanel);
            topPanel.setVisibility(View.GONE);
        }
        WindowManager.LayoutParams params = mAlertDialogWindow.getAttributes();
        params.dimAmount = 0.0f;
        mAlertDialogWindow.setAttributes(params);
        return mAlertDialog;
    }


    @Override
    public synchronized void updateText(AlertDialog mAlertDialog, String line1Text, String line2Text, int pinAlign) {
        Window mAlertDialogWindow = mAlertDialog.getWindow();
        TextView textLine1 = (TextView)mAlertDialogWindow.findViewById(R.id.text_line1);
        TextView textLine2 = (TextView)mAlertDialogWindow.findViewById(R.id.pin_line);

        if(line1Text != null){
            textLine1.setText(line1Text);
        }else{
            Logger.error("not found line1Text");
        }
        if(line2Text != null){
            textLine2.setText(line2Text);
            if(pinAlign == PIN_ALIGN_LEFT){
                textLine2.setGravity(Gravity.LEFT);
            }else if (pinAlign == PIN_ALIGN_CENTER){
                textLine2.setGravity(Gravity.CENTER);
            }else if (pinAlign == PIN_ALIGN_RIGHT){
                textLine2.setGravity(Gravity.RIGHT);
            }else{
                Logger.debug("Use the previous configuration.");
            }
        }else{
            Logger.error("not found line2Text");
        }
    }
}