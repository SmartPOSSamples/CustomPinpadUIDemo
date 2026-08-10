package com.cloudpos.pinpad.newui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.cloudpos.AlgorithmConstants;
import com.cloudpos.DeviceException;
import com.cloudpos.OperationResult;
import com.cloudpos.POSTerminal;
import com.cloudpos.TimeConstants;
import com.cloudpos.jniinterface.PinPadCallbackHandler;
import com.cloudpos.pinpad.KeyInfo;
import com.cloudpos.pinpad.PINPadDevice;
import com.cloudpos.pinpad.PINPadOperationResult;
import com.wizarpos.tlvs.debug.testcase.TlvsUiValidator;
import com.wizarpos.util.ByteConvert;
import com.wizarpos.util.Logger;
import com.wizarpos.util.TextViewUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class InputPINActivity extends Activity {
    public static final int MAX_PIN_LEN = 6;
    TextView pinLine;
    private Handler refreshHandler;

    private int[][] keyLocs = null;
    private int[][] functionKeyLocs = null;

    PINPadDevice device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshHandler = new Handler();

        setContentView(R.layout.input_pin);
        pinLine = (TextView)this.findViewById(R.id.pin_line);

        keyLocs = (int[][]) getIntent().getSerializableExtra("keyLocs");
        functionKeyLocs = (int[][]) getIntent().getSerializableExtra("functionKeyLocs");

        device = (PINPadDevice) POSTerminal.getInstance(this).getDevice("com.cloudpos.device.pinpad");

        try {
            device.open();
            initInputPINDisplay();
            startInputPIN();
        } catch (DeviceException e) {
            e.printStackTrace();
        }
    }
    private Thread th;
    private void startInputPIN(){
        if(th == null || th.getState() == Thread.State.TERMINATED){
            Logger.debug("startInputPIN = %s", th);
            th = new Thread(){
                @Override
                public void run() {
                    super.run();

                    try {
                        device.setPINLength(MAX_PIN_LEN , MAX_PIN_LEN);
                        KeyInfo keyInfo = new KeyInfo(PINPadDevice.KEY_TYPE_MK_SK, 0, 0, AlgorithmConstants.ALG_3DES);

                        byte[] tlvs;
                        if (RawTlvSource.selectedRawResId != 0) {
                            tlvs = readRawBytes(RawTlvSource.selectedRawResId);
                        } else if (keyLocs != null || functionKeyLocs != null) {
                            tlvs = TlvsUiValidator.getInstance().getTlvsFromArrays(InputPINActivity.this, keyLocs, functionKeyLocs, R.drawable.below3);
                        } else {
                            tlvs = TlvsUiValidator.getInstance().createKeyBoardTlvs(InputPINActivity.this, R.drawable.below3);
                        }
                        Logger.debug("run(%s)", tlvs.length);
                        device.setGUIConfiguration(6, tlvs);

                        String pan = "0123456789012345678";
                        PINPadOperationResult pinPadResult = device.waitForPinBlock(keyInfo, pan, false, TimeConstants.FOREVER);
                        Logger.debug("selectKey = %s", pinPadResult);

                        if(pinPadResult.getResultCode() == OperationResult.SUCCESS ){
                            Logger.debug("PIN:%s", ByteConvert.bytesToHexString(pinPadResult.getEncryptedPINBlock()));
                        }
                    } catch (DeviceException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        InputPINActivity.this.finish();
                        InputPINActivity.this.onDestroy();
                    }

                }
            };
            th.start();
        }
    }

    public byte[] readRawBytes(int rawResId) throws IOException {
        try (InputStream is = this.getResources().openRawResource(rawResId);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8 * 1024]; // 8KB buffer
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        }
    }
    public void refreshPinLine(final String inputPin, final int color){
        refreshHandler.post(new Runnable() {
            @Override
            public void run() {
                TextViewUtil.infoColorfulTextView(pinLine, inputPin, color);
            }
        });
    }

    private void initInputText(){
//        input title
//        q2    1(bold, big text size, black text color) // same
//              2(small text size, black text color)
//              pin(green or gray ●)                     // same
//              4(small text size, gray text color)
//        q3    1(bold, big text size, black text color) // same
//              pin(green or gray ●)                     // same

        //        init pinline
        int noInputPINLen = MAX_PIN_LEN;
        Logger.debug("noInputPINLen = %s", noInputPINLen);
        if(MAX_PIN_LEN > 0){
            String inputPin = "";
            for(int i = 0; i < noInputPINLen; i ++){
                inputPin = inputPin + "●";
            }
            refreshPinLine(inputPin, Color.GRAY);
        }
    }
    void initInputPINDisplay(){
        try {
            device.setupCallbackHandler(new PinPadCallbackHandler() {
                @Override
                public void processCallback(byte[] bytes) {
                    if(bytes != null){
                        Logger.debug("processCallback %s", ByteConvert.bytesToHexString(bytes));

                        refreshHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                pinLine.setText("");
                            }
                        });
                        int pinCount = bytes[0];
                        String inputPin = "";
                        for(int i = 0; i < pinCount; i ++){
                            inputPin = inputPin + "●";
                        }
                        refreshPinLine(inputPin, ContextCompat.getColor(InputPINActivity.this, R.color.colorBlue));

                        int noInputPINLen = MAX_PIN_LEN - inputPin.length();
                        Logger.debug("noInputPINLen = %s", noInputPINLen);
                        if(noInputPINLen > 0){
                            inputPin = "";
                            for(int i = 0; i < noInputPINLen; i ++){
                                inputPin = inputPin + "●";
                            }
                            refreshPinLine(inputPin, Color.GRAY);
                        }

                    }else{
                        pinLine.setText("");
                    }
                }

                @Override
                public void processCallback(int i, int i1) {
                    Logger.debug("processCallback(%s)", i, i1);
                }
            });
        } catch (DeviceException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            device.close();
        } catch (DeviceException e) {

        }
    }
}
