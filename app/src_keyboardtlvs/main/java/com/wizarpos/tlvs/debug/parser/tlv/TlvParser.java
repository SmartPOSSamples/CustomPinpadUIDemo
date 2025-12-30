package com.wizarpos.tlvs.debug.parser.tlv;

import com.wizarpos.util.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TlvParser {
    /** 解析整段 buffer，返回 TLV 列表 */
    public static Map<Integer, Tlv> parseAll(byte[] data) {
//        List<Tlv> out = new ArrayList<>();
        Map<Integer, Tlv> out = new HashMap<>();
        int offset = 0;

        while (offset < data.length) {
            ParseResult r = parseOne(data, offset);
//            out.add(r.tlv);
            Logger.debug("parseAll(%s)", r.tlv);
            out.put(r.tlv.tag, r.tlv);
            offset = r.nextOffset;
        }
        return out;
    }

    /** 从 offset 解析一个 TLV */
    private static ParseResult parseOne(byte[] data, int offset) {
        int start = offset;

        // 至少要有 Tag(1) + Length(4)
        if (offset + 5 > data.length) {
            throw new IllegalArgumentException(
                    "Not enough bytes for TLV header at offset " + offset);
        }

        // 1) Tag: 1 byte
        int tag = data[offset++];

        // 2) Length: 4 bytes Big Endian (unsigned)
        long length = readUint32BE(data, offset);
//        Logger.debug("parseOne(%s : %s)", tag, length);
        offset += 4;

        if (length < 0) {
            throw new IllegalArgumentException("Negative length? tag=0x" + String.format("%02X", tag));
        }

        if (length > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Length too large to allocate: " + length);
        }

        // 3) Value: length bytes
        int lenInt = (int) length;
        if (offset + lenInt > data.length) {
            throw new IllegalArgumentException(
                    "TLV value overflow. tag=0x" + String.format("%02X", tag) +
                            ", length=" + length +
                            ", remain=" + (data.length - offset));
        }

        byte[] value = Arrays.copyOfRange(data, offset, offset + lenInt);
        offset += lenInt;

        return new ParseResult(new Tlv(tag, length, value), offset, start);
    }

    /** 读 4 字节无符号整数（Big Endian） */
    private static long readUint32BE(byte[] data, int offset) {
        return ((long)(data[offset] & 0xFF) << 24) |
                ((long)(data[offset + 1] & 0xFF) << 16) |
                ((long)(data[offset + 2] & 0xFF) << 8) |
                ((long)(data[offset + 3] & 0xFF));
    }

    private static class ParseResult {
        final Tlv tlv;
        final int nextOffset;
        final int startOffset;
        ParseResult(Tlv tlv, int nextOffset, int startOffset) {
            this.tlv = tlv;
            this.nextOffset = nextOffset;
            this.startOffset = startOffset;
        }

    }

}
