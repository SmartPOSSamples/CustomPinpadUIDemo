package com.cloudpos.pinpad.newui.testcase;

import android.content.Context;

import com.cloudpos.pinpad.newui.MainActivity;
import com.cloudpos.pinpad.newui.R;
import com.cloudpos.pinpad.newui.UiHandler;
import com.wizarpos.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractTest {
    protected MainActivity mHandler;
    public void initUiHandler(MainActivity handler){
        this.mHandler = handler;
    }

    protected void sendDefalutLog(String msg){
        Logger.debug(msg);
        mHandler.writerInLog(msg, R.id.log_default);
    }

    protected void sendSuccessLog(String msg){
        mHandler.writerInSuccessLog(msg);
    }

    protected void sendErrorLog(String msg){
        mHandler.writerInFailedLog(msg);
    }
    protected Thread th;
    protected String getTime(){
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss");
        String time = format.format(date.getTime());
        return time;
    }

    public abstract void test(Context context );

    public byte[] readRawBytes(Context context, int rawResId) throws IOException {
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

}
