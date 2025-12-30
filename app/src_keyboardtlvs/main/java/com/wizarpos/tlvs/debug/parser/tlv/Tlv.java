package com.wizarpos.tlvs.debug.parser.tlv;

public class Tlv {
    public final int tag;        // 1 byte, 0~255
    public final long length;    // 4 bytes unsigned, use long
    public final byte[] value;

    public Tlv(int tag, long length, byte[] value) {
        this.tag = tag;
        this.length = length;
        this.value = value;
    }

    public String tagHex() { return String.format("%02X", tag); }

    public String valueHex() {
        if(length > 64){
            return value.toString();
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : value) sb.append(String.format("%02X", b));
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Tlv{tag=0x" + tagHex() +
                ", length=" + length +
                ", value=" + valueHex() + "}";
    }
}
