package com.cloudpos.pinpad.newui.testcase;

import android.content.Context;

import com.cloudpos.AlgorithmConstants;
import com.cloudpos.DeviceException;
import com.cloudpos.OperationListener;
import com.cloudpos.OperationResult;
import com.cloudpos.POSTerminal;
import com.cloudpos.TimeConstants;
import com.cloudpos.pinpad.KeyInfo;
import com.cloudpos.pinpad.PINPadDevice;
import com.cloudpos.pinpad.PINPadOperationResult;
import com.cloudpos.pinpad.newui.R;
import com.cloudpos.sdk.pinpad.impl.PINPadDeviceImpl;
import com.wizarpos.util.ByteConvert;
import com.wizarpos.util.SystemUtils;

import java.util.Arrays;

public class PINPadUiTest extends AbstractTest{

    private static volatile PINPadUiTest instance = null;

    private PINPadUiTest() {}

    public static PINPadUiTest getInstance() {
        if (instance == null) {
            synchronized (PINPadUiTest.class) {
                if (instance == null) {
                    instance = new PINPadUiTest();
                }
            }
        }
        return instance;
    }
    private static final byte[] CipherPINKey = new byte[]{
            (byte)0xA6, (byte)0x60, (byte)0x32, (byte)0x06, (byte)0xF6, (byte)0x85, (byte)0xFC, (byte)0x32,
            (byte)0xA6, (byte)0x60, (byte)0x32, (byte)0x06, (byte)0xF6, (byte)0x85, (byte)0xFC, (byte)0x32
    };

    private static final byte[] ExpectedResult = new byte[]{
            (byte)0x7F,(byte)0xE3,(byte)0xBB,(byte)0x79,(byte)0x00,(byte)0xF8,(byte)0x0F,(byte)0x90
    };
    public void testPINPadUi(Context context){
        PINPadDevice device = (PINPadDevice)POSTerminal.getInstance(context).getDevice(POSTerminal.DEVICE_NAME_PINPAD);
        if(device != null){
            try{
                boolean result = false;
                device.open(0);
//              device.updateUserKey(0,0, CipherPINKey);
//              updateUserKey(int masterKeyID, int userKeyID, byte[] cipherNewUserKey, int checkType, byte[] checkValue, KeyInfo keyInfo) throws DeviceException {
                KeyInfo keyInfo = new KeyInfo(PINPadDevice.KEY_TYPE_MK_SK, 0, 0, AlgorithmConstants.ALG_3DES);
//                device.updateUserKey(0, 0, CipherPINKey, 0, null, keyInfo);
                //            key0, key1, key2, key3, key4, key5, key6, key7, key8, key9,

                byte[] tlvs = readRawBytes(context, R.raw.gui);
//                byte[] tlvs = TlvGenerator.getInstance().generateTlvs(context, R.raw.q3_full, uiLoc, inputTextLoc, keyLocs, functionKeyLocs);
                device.setGUIConfiguration(6, tlvs);

                String pan = "0123456789012345678";
                PINPadOperationResult pinPadResult = device.waitForPinBlock(keyInfo, pan, false, TimeConstants.FOREVER);
                if(pinPadResult.getResultCode() == OperationResult.SUCCESS ){
                    byte[] pinBlock = pinPadResult.getEncryptedPINBlock();
                    if(pinBlock != null && pinBlock.length >0){
                        result = Arrays.equals(pinBlock, ExpectedResult);
                        sendDefalutLog("PINBlock = " + ByteConvert.getBestString(pinBlock));
                        if(result){
                            sendSuccessLog("test success ！reason: verify password: 1234567890");
                        }else{
                            sendErrorLog("test failed！！reason: verify password failed.");
                        }
                    }else{
                        sendErrorLog("test failed！reason: unknown error.");
                    }
                }else{
                    sendErrorLog("test failed！reason: custom cancel or time out");
                }
//                device.listenForPinBlock(keyInfo, pan, false, listener, TimeConstants.FOREVER);
            }catch (Exception e){
                e.printStackTrace();
                sendErrorLog(e.getMessage());
            }finally {
                try {
                    device.close();
                } catch (DeviceException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void test(Context context) {
        testPINPadUi(context);
    }
}
