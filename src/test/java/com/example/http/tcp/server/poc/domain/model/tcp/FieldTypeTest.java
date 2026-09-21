package com.example.http.tcp.server.poc.domain.model.tcp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FieldTypeTest {

    @Nested
    class TxtFieldType {

        @Nested
        class 正常系 {

            @Test
            @DisplayName("Given: 有効なASCIIバイト列が与えられたとき, When: TXT型でデコードすると, Then: 対応する文字列が返される")
            void decodeReturnsStringFromByteArray() {
                byte[] raw = {0x48, 0x45, 0x4C, 0x4C, 0x4F};
                String result = FieldType.TXT.decode(raw);
                assertEquals("HELLO", result);
            }

            @Test
            @DisplayName("Given: 指定長より短い文字列が与えられたとき, When: TXT型でエンコードすると, Then: 右側がスペース埋めされたバイト列が返される")
            void encodeReturnsRightPaddedByteArray() {
                String value = "AB";
                byte[] result = FieldType.TXT.encode(value, 4);
                byte[] expected = {0x41, 0x42, 0x20, 0x20};
                assertArrayEquals(expected, result);
            }

            @Test
            @DisplayName("Given: 指定長と同じ長さの文字列が与えられたとき, When: TXT型でエンコードすると, Then: そのままのバイト列が返される")
            void encodeWithFullLengthReturnsExactByteArray() {
                String value = "TEST";
                byte[] result = FieldType.TXT.encode(value, 4);
                byte[] expected = {0x54, 0x45, 0x53, 0x54};
                assertArrayEquals(expected, result);
            }

            @Test
            @DisplayName("Given: テキストをエンコードしてからデコードしたとき, When: ラウンドトリップを実行すると, Then: 右スペース埋めされた元の値が保持される")
            void encodeDecodeRoundTripPreservesTrimmedValue() {
                String original = "HELLO";
                byte[] encoded = FieldType.TXT.encode(original, 8);
                String decoded = FieldType.TXT.decode(encoded);
                assertEquals("HELLO   ", decoded);
            }
        }

        @Nested
        class 異常系 {

            @Test
            @DisplayName("Given: 指定長を超える長さの文字列が与えられたとき, When: TXT型でエンコードすると, Then: TcpMessageFormatExceptionがスローされる")
            void encodeThrowsExceptionWhenValueExceedsLength() {
                String value = "HELLO";
                assertThrows(TcpMessageFormatException.class, () -> {
                    FieldType.TXT.encode(value, 4);
                });
            }
        }
    }

    @Nested
    class BcdFieldType {

        @Nested
        class 正常系 {

            @Test
            @DisplayName("Given: 有効なBCDバイト列が与えられたとき, When: BCD型でデコードすると, Then: 対応する数字文字列が返される")
            void decodeReturnsStringFromBcdBytes() {
                byte[] raw = {0x12, 0x34, 0x56};
                String result = FieldType.BCD.decode(raw);
                assertEquals("123456", result);
            }

            @Test
            @DisplayName("Given: 指定長（バイト数）より少ない桁数の数字が与えられたとき, When: BCD型でエンコードすると, Then: 左側がゼロ埋めされたバイト列が返される")
            void encodeReturnsLeftZeroPaddedByteArray() {
                String value = "123";
                byte[] result = FieldType.BCD.encode(value, 2);
                byte[] expected = {0x01, 0x23};
                assertArrayEquals(expected, result);
            }

            @Test
            @DisplayName("Given: 数字文字列をエンコードしてからデコードしたとき, When: ラウンドトリップを実行すると, Then: 元の値が保持される")
            void encodeDecodeRoundTripPreservesValue() {
                String original = "9876543210";
                byte[] encoded = FieldType.BCD.encode(original, 5);
                String decoded = FieldType.BCD.decode(encoded);
                assertEquals(original, decoded);
            }

            @Test
            @DisplayName("Given: 先頭ゼロを含む数字が与えられたとき, When: BCD型でエンコードすると, Then: 先頭ゼロが保持されたバイト列が返される")
            void encodeWithLeadingZerosPreservesLeadingZeros() {
                String value = "00123";
                byte[] result = FieldType.BCD.encode(value, 3);
                byte[] expected = {0x00, 0x01, 0x23};
                assertArrayEquals(expected, result);
            }
        }

        @Nested
        class 異常系 {

            @Test
            @DisplayName("Given: 不正なニブル値0x0Aを含むバイト列が与えられたとき, When: BCD型でデコードすると, Then: TcpMessageFormatExceptionがスローされる")
            void decodeThrowsExceptionWhenNibbleIsA() {
                byte[] raw = {0x0A};
                assertThrows(TcpMessageFormatException.class, () -> {
                    FieldType.BCD.decode(raw);
                });
            }

            @Test
            @DisplayName("Given: 不正なニブル値0xF0を含むバイト列が与えられたとき, When: BCD型でデコードすると, Then: TcpMessageFormatExceptionがスローされる")
            void decodeThrowsExceptionWhenNibbleIsF() {
                byte[] raw = {(byte) 0xF0};
                assertThrows(TcpMessageFormatException.class, () -> {
                    FieldType.BCD.decode(raw);
                });
            }

            @Test
            @DisplayName("Given: 指定長を超える桁数の数字が与えられたとき, When: BCD型でエンコードすると, Then: TcpMessageFormatExceptionがスローされる")
            void encodeThrowsExceptionWhenValueExceedsCapacity() {
                String value = "12345";
                assertThrows(TcpMessageFormatException.class, () -> {
                    FieldType.BCD.encode(value, 2);
                });
            }
        }
    }

    @Nested
    class HexFieldType {

        @Nested
        class 正常系 {

            @Test
            @DisplayName("Given: 有効なHEXバイト列（16進数）が与えられたとき, When: HEX型でデコードすると, Then: 対応する10進数文字列が返される")
            void decodeReturnsDecimalStringFromHexBytes() {
                byte[] raw = {0x00, 0x10};
                String result = FieldType.HEX.decode(raw);
                assertEquals("16", result);
            }

            @Test
            @DisplayName("Given: 10進数の文字列が与えられたとき, When: HEX型でエンコードすると, Then: 対応する16進バイト列が返される")
            void encodeReturnsHexBytesFromDecimalString() {
                String value = "256";
                byte[] result = FieldType.HEX.encode(value, 2);
                byte[] expected = {0x01, 0x00};
                assertArrayEquals(expected, result);
            }

            @Test
            @DisplayName("Given: 指定バイト数より少ない値が与えられたとき, When: HEX型でエンコードすると, Then: 左側がゼロ埋めされたバイト列が返される")
            void encodeWithLeadingZeroPadding() {
                String value = "10";
                byte[] result = FieldType.HEX.encode(value, 4);
                assertEquals(4, result.length);
                assertEquals(0x00, result[0]);
                assertEquals(0x00, result[1]);
                assertEquals(0x00, result[2]);
                assertEquals(0x0A, result[3]);
            }

            @Test
            @DisplayName("Given: 10進数をエンコードしてからデコードしたとき, When: ラウンドトリップを実行すると, Then: 元の値が保持される")
            void encodeDecodeRoundTripPreservesValue() {
                String original = "65535";
                byte[] encoded = FieldType.HEX.encode(original, 4);
                String decoded = FieldType.HEX.decode(encoded);
                assertEquals(original, decoded);
            }
        }

        @Nested
        class 異常系 {

            @Test
            @DisplayName("Given: 符号付きで負の値を表すバイト列が与えられたとき, When: HEX型でデコードすると, Then: TcpMessageFormatExceptionがスローされる")
            void decodeThrowsExceptionForNegativeValue() {
                byte[] raw = {(byte) 0xFF, (byte) 0xFF};
                assertThrows(TcpMessageFormatException.class, () -> {
                    FieldType.HEX.decode(raw);
                });
            }

            @Test
            @DisplayName("Given: 負号を含む数字文字列が与えられたとき, When: HEX型でエンコードすると, Then: TcpMessageFormatExceptionがスローされる")
            void encodeThrowsExceptionForNegativeString() {
                String value = "-100";
                assertThrows(TcpMessageFormatException.class, () -> {
                    FieldType.HEX.encode(value, 2);
                });
            }

            @Test
            @DisplayName("Given: 非数字文字を含む文字列が与えられたとき, When: HEX型でエンコードすると, Then: TcpMessageFormatExceptionがスローされる")
            void encodeThrowsExceptionForNonNumericString() {
                String value = "ABC";
                assertThrows(TcpMessageFormatException.class, () -> {
                    FieldType.HEX.encode(value, 2);
                });
            }

            @Test
            @DisplayName("Given: 指定バイト数で表現できる最大値を超える値が与えられたとき, When: HEX型でエンコードすると, Then: TcpMessageFormatExceptionがスローされる")
            void encodeThrowsExceptionWhenValueExceedsFieldLength() {
                String value = "65536";
                assertThrows(TcpMessageFormatException.class, () -> {
                    FieldType.HEX.encode(value, 2);
                });
            }
        }
    }
}
