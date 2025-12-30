package com.cloudpos.pinpad.newui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cloudpos.POSTerminal;
import com.wizarpos.tlvs.debug.DebugConfig;
import com.wizarpos.tlvs.debug.testcase.TlvsUiValidator;
import com.wizarpos.util.Logger;


public class MainActivity extends Activity implements View.OnClickListener {

    protected TextView log_text;
    protected TextView text_warning;
    protected UiHandler mHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn_test1 = (Button) this.findViewById(R.id.btn_test1);
        Button btn_test2 = (Button) this.findViewById(R.id.btn_test2);
        Button btn_test3 = (Button) this.findViewById(R.id.btn_test3);

        Button btn_log_clean = (Button) this.findViewById(R.id.btn_log_clean);
        btn_test1.setOnClickListener(this);
        btn_test2.setOnClickListener(this);
        btn_test3.setOnClickListener(this);
        btn_log_clean.setOnClickListener(this);

        log_text = (TextView) this.findViewById(R.id.text_result);
        text_warning = (TextView) this.findViewById(R.id.text_warning);

        log_text.setMovementMethod(ScrollingMovementMethod.getInstance());
        text_warning.setMovementMethod(ScrollingMovementMethod.getInstance());

        mHandler = new UiHandler(this, log_text, text_warning);

        POSTerminal.getInstance(this);
    }

    @Override
    public void onClick(View v) {
        int index = v.getId();
        if (index == R.id.btn_test1) {
            Intent intent = new Intent(this, InputPINActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        } else if (index == R.id.btn_test2) {
            try {
                TlvsUiValidator.getInstance().showTlvsLocalUi(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (index == R.id.btn_test3) {

        } else if (index == R.id.btn_log_clean) {
            mHandler.sendEmptyMessage(R.id.log_clean);
        }
    }


    public void writerInLog(String obj, int id) {
        if (mHandler != null) {
            Message msg = new Message();
            msg.what = id;
            msg.obj = obj;
            mHandler.sendMessage(msg);
        }
    }

    public void writerInSuccessLog(String obj) {
        Logger.debug(obj);
        writerInLog(obj, R.id.log_success);
    }

    public void writerInFailedLog(String obj) {
        writerInLog(obj, R.id.log_failed);
    }

}
