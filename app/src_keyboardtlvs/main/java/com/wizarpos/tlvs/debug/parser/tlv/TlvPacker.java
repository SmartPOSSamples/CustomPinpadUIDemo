package com.wizarpos.tlvs.debug.parser.tlv;

import java.util.List;

public class TlvPacker {
    /**
     * 生成一个 TLV 块：1字节Tag + 4字节length(BigEndian) + value
     *
     * @param tag   1字节 Tag
     * @param value Value 字节数组
     * @return TLV 格式 byte[]
     */
    public static byte[] buildTLV(byte tag, byte[] value) {
        if (value == null) value = new byte[0];
        int length = value.length;

        byte[] tlv = new byte[1 + 4 + length];

        int offset = 0;

        // 写入 Tag（1 byte）
        tlv[offset++] = tag;

        // 写入 Length（4 bytes Big Endian）
        tlv[offset++] = (byte) ((length >> 24) & 0xFF);
        tlv[offset++] = (byte) ((length >> 16) & 0xFF);
        tlv[offset++] = (byte) ((length >> 8) & 0xFF);
        tlv[offset++] = (byte) (length & 0xFF);

        // 写入 Value
        System.arraycopy(value, 0, tlv, offset, length);

        return tlv;
    }

    /**
     * 拼接多个 TLV 块为一个整体 byte[]
     *
     * @param tlvs 多个 TLV 块，每个都是 byte[]
     * @return 合并后的 byte[]
     */
    public static byte[] mergeTLVs(List<byte[]> tlvs) {
        int total = 0;
        for (byte[] t : tlvs) {
            total += t.length;
        }

        byte[] result = new byte[total];
        int offset = 0;

        for (byte[] t : tlvs) {
            System.arraycopy(t, 0, result, offset, t.length);
            offset += t.length;
        }
        return result;
    }


}
