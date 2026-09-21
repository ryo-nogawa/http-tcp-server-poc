package com.example.http.tcp.server.poc.domain.model.tcp;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/**
 * TCP電文のフィールド型.
 *
 * <p>TXT・BCD・HEXの3つの属性を持ち、各属性が独自のエンコード・デコード処理を実装する。
 * 変換失敗時は{@link TcpMessageFormatException}をスローする。
 */
public enum FieldType {

    /**
     * テキスト型.
     *
     * <p>US-ASCIIで相互変換し、encode時は右スペース埋めで左詰めとなる。
     */
    TXT {
        /**
         * US-ASCIIテキストをデコードする.
         *
         * <p>バイト配列をUS-ASCII文字列に変換する。
         *
         * @param raw デコード対象のバイト配列
         * @return デコード済みの文字列
         */
        @Override
        public String decode(byte[] raw) {
            return new String(raw, StandardCharsets.US_ASCII);
        }

        /**
         * テキストをエンコードする.
         *
         * <p>文字列をUS-ASCIIバイト列に変換し、右スペース（0x20）で埋めて
         * 左詰めにする。
         *
         * @param value エンコード対象の文字列
         * @param length 出力バイト数
         * @return エンコード済みのバイト配列
         * @throws TcpMessageFormatException 値長がフィールド長を超える場合
         */
        @Override
        public byte[] encode(String value, int length) {
            if (value.length() > length) {
                throw new TcpMessageFormatException(
                        "TXT encoding error: value length exceeds field length");
            }
            byte[] result = new byte[length];
            byte[] valueBytes = value.getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(valueBytes, 0, result, 0, valueBytes.length);
            for (int i = valueBytes.length; i < length; i++) {
                result[i] = 0x20;
            }
            return result;
        }
    },

    /**
     * BCD型.
     *
     * <p>1バイトに数字2桁を上位4bit・下位4bitで格納する。
     * decodeは0x0A～0x0Fを不正ニブルとして検出し例外をスロー。
     * encodeはlength*2桁へ左ゼロ埋めする。
     */
    BCD {
        /**
         * BCD（二進化十進法）をデコードする.
         *
         * <p>1バイトに2桁の数字を上位4bit・下位4bitで格納した形式から
         * 文字列に変換する。0x0A～0x0Fの不正ニブルを検出した場合は例外をスロー。
         *
         * @param raw デコード対象のバイト配列
         * @return デコード済みの文字列
         * @throws TcpMessageFormatException 不正ニブル（0x0A～0x0F）を検出した場合
         */
        @Override
        public String decode(byte[] raw) {
            StringBuilder sb = new StringBuilder();
            for (byte b : raw) {
                int upper = (b >> 4) & 0x0F;
                int lower = b & 0x0F;
                if (upper > 9 || lower > 9) {
                    throw new TcpMessageFormatException(
                            "BCD decoding error: invalid nibble detected");
                }
                sb.append(upper).append(lower);
            }
            return sb.toString();
        }

        /**
         * テキストをBCDにエンコードする.
         *
         * <p>数字文字列を左ゼロ埋めし、各バイトに2桁の数字を
         * 上位4bit・下位4bitで格納する。
         *
         * @param value エンコード対象の文字列（数字のみ）
         * @param length 出力バイト数
         * @return エンコード済みのバイト配列
         * @throws TcpMessageFormatException 値長超過または非数字文字を検出した場合
         */
        @Override
        public byte[] encode(String value, int length) {
            int expectedLength = length * 2;
            if (value.length() > expectedLength) {
                throw new TcpMessageFormatException(
                        "BCD encoding error: value length exceeds field capacity");
            }
            String paddedValue = String.format("%0" + expectedLength + "d",
                    Long.parseLong(value));
            byte[] result = new byte[length];
            for (int i = 0; i < length; i++) {
                int upper = Character.digit(paddedValue.charAt(i * 2), 10);
                int lower = Character.digit(paddedValue.charAt(i * 2 + 1), 10);
                if (upper < 0 || lower < 0) {
                    throw new TcpMessageFormatException(
                            "BCD encoding error: non-digit character found");
                }
                result[i] = (byte) ((upper << 4) | lower);
            }
            return result;
        }
    },

    /**
     * HEX型.
     *
     * <p>符号付きビッグエンディアンのバイト列と10進文字列を相互変換する。
     * decode時に負数値で例外、encode時に非数字や負数で例外をスロー。
     */
    HEX {
        /**
         * HEX（16進数）をデコードする.
         *
         * <p>符号付きビッグエンディアンのバイト列を10進文字列に変換する。
         * 負数値を検出した場合は例外をスロー。
         *
         * @param raw デコード対象のバイト配列
         * @return デコード済みの文字列
         * @throws TcpMessageFormatException 負数値を検出した場合
         */
        @Override
        public String decode(byte[] raw) {
            BigInteger bi = new BigInteger(raw);
            if (bi.signum() < 0) {
                throw new TcpMessageFormatException(
                        "HEX decoding error: negative value detected");
            }
            return bi.toString();
        }

        /**
         * テキストをHEXにエンコードする.
         *
         * <p>10進文字列を符号付きビッグエンディアンのバイト列に変換する。
         * {@link BigInteger#toByteArray()}は値の表現に必要な最小バイト数しか
         * 返さないため、前にゼロバイトを追加してパディングする。
         *
         * @param value エンコード対象の文字列（10進非負整数）
         * @param length 出力バイト数
         * @return エンコード済みのバイト配列
         * @throws TcpMessageFormatException 非数字・負数または値長超過の場合
         */
        @Override
        public byte[] encode(String value, int length) {
            try {
                long longValue = Long.parseLong(value);
                if (longValue < 0) {
                    throw new TcpMessageFormatException(
                            "HEX encoding error: negative value not allowed");
                }
                BigInteger bi = BigInteger.valueOf(longValue);
                byte[] result = bi.toByteArray();
                if (result.length > length) {
                    throw new TcpMessageFormatException(
                            "HEX encoding error: value exceeds field length");
                }
                if (result.length < length) {
                    byte[] padded = new byte[length];
                    System.arraycopy(result, 0, padded,
                            length - result.length, result.length);
                    return padded;
                }
                return result;
            } catch (NumberFormatException e) {
                throw new TcpMessageFormatException(
                        "HEX encoding error: value is not a valid integer", e);
            }
        }
    };

    /**
     * バイト配列をデコードして文字列に変換する.
     *
     * @param raw デコード対象のバイト配列
     * @return デコード済みの文字列
     * @throws TcpMessageFormatException デコード失敗時
     */
    public abstract String decode(byte[] raw);

    /**
     * 文字列をエンコードしてバイト配列に変換する.
     *
     * @param value エンコード対象の文字列
     * @param length 出力バイト数
     * @return エンコード済みのバイト配列
     * @throws TcpMessageFormatException エンコード失敗時
     */
    public abstract byte[] encode(String value, int length);

}
