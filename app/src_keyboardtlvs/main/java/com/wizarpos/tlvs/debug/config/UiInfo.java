package com.wizarpos.tlvs.debug.config;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class UiInfo {
    private Rect key0;
    private Rect key1;
    private Rect key2;
    private Rect key3;
    private Rect key4;
    private Rect key5;
    private Rect key6;
    private Rect key7;
    private Rect key8;
    private Rect key9;

    private int correctionValue;

    private Rect uiLoc;
    private Rect btnOk;
    private Rect btnClear;
    private Rect btnCancel;

    private Rect textLoc;

    private Rect displayLayout;

    private boolean allowBeep;

    private String displayBackgroundColor = null;
    private String inputTextColor = null;

    private Bitmap src;

    public Rect getKey0() {
        return key0;
    }

    public void setKey0(Rect key0) {
        this.key0 = key0;
    }

    public Rect getKey1() {
        return key1;
    }

    public void setKey1(Rect key1) {
        this.key1 = key1;
    }

    public Rect getKey2() {
        return key2;
    }

    public void setKey2(Rect key2) {
        this.key2 = key2;
    }

    public Rect getKey3() {
        return key3;
    }

    public void setKey3(Rect key3) {
        this.key3 = key3;
    }

    public Rect getKey4() {
        return key4;
    }

    public void setKey4(Rect key4) {
        this.key4 = key4;
    }

    public Rect getKey5() {
        return key5;
    }

    public void setKey5(Rect key5) {
        this.key5 = key5;
    }

    public Rect getKey6() {
        return key6;
    }

    public void setKey6(Rect key6) {
        this.key6 = key6;
    }

    public Rect getKey7() {
        return key7;
    }

    public void setKey7(Rect key7) {
        this.key7 = key7;
    }

    public Rect getKey8() {
        return key8;
    }

    public void setKey8(Rect key8) {
        this.key8 = key8;
    }

    public Rect getKey9() {
        return key9;
    }

    public void setKey9(Rect key9) {
        this.key9 = key9;
    }

    public Rect getDisplayLayout() {
        return displayLayout;
    }

    public void setDisplayLayout(Rect displayLayout) {
        this.displayLayout = displayLayout;
    }

    public boolean isAllowBeep() {
        return allowBeep;
    }

    public void setAllowBeep(boolean allowBeep) {
        this.allowBeep = allowBeep;
    }

    public String getDisplayBackgroundColor() {
        return displayBackgroundColor;
    }

    public void setDisplayBackgroundColor(String displayBackgroundColor) {
        this.displayBackgroundColor = displayBackgroundColor;
    }

    public String getInputTextColor() {
        return inputTextColor;
    }

    public void setInputTextColor(String inputTextColor) {
        this.inputTextColor = inputTextColor;
    }

    public Bitmap getSrc() {
        return src;
    }

    public void setSrc(Bitmap src) {
        this.src = src;
    }

    public Rect getBtnOk() {
        return btnOk;
    }

    public void setBtnOk(Rect btnOk) {
        this.btnOk = btnOk;
    }

    public Rect getBtnClear() {
        return btnClear;
    }

    public void setBtnClear(Rect btnClear) {
        this.btnClear = btnClear;
    }

    public Rect getBtnCancel() {
        return btnCancel;
    }

    public void setBtnCancel(Rect btnCancel) {
        this.btnCancel = btnCancel;
    }

    public Rect getTextLoc() {
        return textLoc;
    }

    public void setTextLoc(Rect textLoc) {
        this.textLoc = textLoc;
    }

    public Rect getUiLoc() {
        return uiLoc;
    }

    public void setUiLoc(Rect uiLoc) {
        this.uiLoc = uiLoc;
    }

    public int getCorrectionValue() {
        return correctionValue;
    }

    public void setCorrectionValue(int correctionValue) {
        this.correctionValue = correctionValue;
    }
}
