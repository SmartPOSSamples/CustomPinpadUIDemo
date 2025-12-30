package com.wizarpos.tlvs.debug.ui;

import android.graphics.Rect;

public interface MyBiConsumer<T, T1> {
    void accept(String label, Rect rect);
}
