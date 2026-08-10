package com.cloudpos.pinpad.newui;

/**
 * Holds the user's selection of a raw TLV resource so that
 * InputPINActivity can fill its tlvs byte array from it.
 */
public class RawTlvSource {

    /** 0 means "not set" -> fall back to the generated tlvs. */
    public static volatile int selectedRawResId = 0;

    public static final int[] RAW_TLV_RES_IDS = {
            R.raw.gui,
            R.raw.q3mu,
            R.raw.q2,
            R.raw.q21,
            0
    };

    public static final String[] RAW_TLV_NAMES = {
            "gui.tlv",
            "q3mu.tlv",
            "q2.tlv",
            "q21.tlv",
            "default (generated)"
    };

    private RawTlvSource() {
    }
}