package com.cloudpos.pinpad.newui;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Message;
import android.widget.TextView;

import com.wizarpos.util.Logger;
import com.wizarpos.util.TextViewUtil;


public class UiHandler extends android.os.Handler {

    protected NotificationManager manager;
    protected MainActivity activity;

    private TextView log_text;
    private TextView text_warning;
    public UiHandler(MainActivity activity, TextView log_text, TextView text_warning) {
        this.manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        this.activity = activity;
        this.log_text = log_text;
        this.text_warning = text_warning;
    }

    @Override
    public void handleMessage(Message message) {
        String str = message.obj + "\n";
        if (message.arg1 == R.id.log_default) {
            log_text.append(str);
        } else if (message.arg1 == R.id.log_success) {
            TextViewUtil.infoBlueTextView(log_text, str);
        } else if (message.arg1 == R.id.log_failed) {
            TextViewUtil.infoRedTextView(text_warning, str);
        } else if (message.arg1 == R.id.log_clean) {
            log_text.setText("");
        }
    }

}
