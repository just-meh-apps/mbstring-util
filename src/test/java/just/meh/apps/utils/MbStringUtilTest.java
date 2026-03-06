package just.meh.apps.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MbStringUtilTest {

    @Nested
    @DisplayName("length methods")
    class Length {
        @Test
        @DisplayName("Calculates length in code points")
        void testLength() {
            assertEquals(0, MbStringUtil.length(null));
            assertEquals(0, MbStringUtil.length(""));
            assertEquals(3, MbStringUtil.length("abc"));
            assertEquals(3, MbStringUtil.length("가나다"));
            assertEquals(3, MbStringUtil.length("👍a가")); // contrast with "👍a가".length() which is 4
        }

        @Test
        @DisplayName("Calculates byte length with default (UTF-8) charset")
        void testLengthByBytes_defaultCharset() {
            assertEquals(0, MbStringUtil.lengthByBytes(null));
            assertEquals(0, MbStringUtil.lengthByBytes(""));
            assertEquals(3, MbStringUtil.lengthByBytes("abc"));
            assertEquals(9, MbStringUtil.lengthByBytes("가나다"));
            assertEquals(8, MbStringUtil.lengthByBytes("👍a가"));
        }

        @Test
        @DisplayName("Calculates byte length with specified charset")
        void testLengthByBytes_specifiedCharset() {
            Charset euckr = Charset.forName("EUC-KR");
            assertEquals(0, MbStringUtil.lengthByBytes(null, euckr));
            assertEquals(0, MbStringUtil.lengthByBytes("", euckr));
            assertEquals(3, MbStringUtil.lengthByBytes("abc", euckr));
            assertEquals(6, MbStringUtil.lengthByBytes("가나다", euckr));
            assertEquals(1, MbStringUtil.lengthByBytes("👍", euckr)); // unencodable
        }
    }

    @Nested
    @DisplayName("substr methods")
    class Substr {

        @Test
        @DisplayName("Extracts from start to end of string")
        void testSubstr_fromStart() {
            assertEquals("", MbStringUtil.substr(null, 0));
            assertEquals("", MbStringUtil.substr("", 0));
            assertEquals("abc", MbStringUtil.substr("가나다abc", 3));
            assertEquals("bc", MbStringUtil.substr("가나다abc", -2));
            assertEquals("a가나", MbStringUtil.substr("👍a가나", 1));
        }

        @Test
        @DisplayName("Extracts with start and length")
        void testSubstr_withLength() {
            assertEquals("", MbStringUtil.substr(null, 0, 1));
            assertEquals("", MbStringUtil.substr("", 0, 1));
            assertEquals("가나", MbStringUtil.substr("가나다abc", 0, 2));
            assertEquals("ab", MbStringUtil.substr("가나다abc", 3, 2));
            assertEquals("나다", MbStringUtil.substr("가나다abc", -5, 2));
            assertEquals("bc", MbStringUtil.substr("가나다abc", -2, 2));
            assertEquals("", MbStringUtil.substr("가나다abc", 100, 2));
            assertEquals("", MbStringUtil.substr("가나다abc", -100, 2));
            assertEquals("", MbStringUtil.substr("가나다abc", 2, 0));
            assertEquals("", MbStringUtil.substr("가나다abc", 2, -2));
            assertEquals("👍a", MbStringUtil.substr("👍a가나", 0, 2));
            assertEquals("a가", MbStringUtil.substr("👍a가나", 1, 2));
            assertEquals("가", MbStringUtil.substr("👍a가나", -2, 1));
        }
    }

    @Nested
    @DisplayName("substrByBytes methods")
    class SubstrByBytes {

        @Test
        @DisplayName("Extracts from start to end with default (UTF-8) charset")
        void testSubstrByBytes_fromStart() {
            assertEquals("", MbStringUtil.substrByBytes(null, 0));
            assertEquals("", MbStringUtil.substrByBytes("", 0));
            assertEquals("나다abc", MbStringUtil.substrByBytes("가나다abc", 3));
            assertEquals("  abc", MbStringUtil.substrByBytes("가나다abc", -5));
            assertEquals("a가", MbStringUtil.substrByBytes("👍a가", 4));
        }

        @Test
        @DisplayName("Extracts with start and length with default (UTF-8) charset")
        void testSubstrByBytes_withLength() {
            assertEquals("", MbStringUtil.substrByBytes(null, 0, 1));
            assertEquals("", MbStringUtil.substrByBytes("", 0, 1));
            assertEquals("", MbStringUtil.substrByBytes("가나다abc", 2, 0));
            assertEquals("", MbStringUtil.substrByBytes("가나다abc", 2, -2));
            assertEquals("가", MbStringUtil.substrByBytes("가나다abc", 0, 3));
            assertEquals(" 나", MbStringUtil.substrByBytes("가나다abc", 2, 4));
            assertEquals(" 나 ", MbStringUtil.substrByBytes("가나다abc", 2, 5));
            assertEquals("👍", MbStringUtil.substrByBytes("👍a가", 0, 4));
            assertEquals("👍a", MbStringUtil.substrByBytes("👍a가", 0, 5));
        }

        @Test
        @DisplayName("Extracts with start, length, and specified charset")
        void testSubstrByBytes_withLengthAndCharset() {
            Charset euckr = Charset.forName("EUC-KR");
            assertEquals("", MbStringUtil.substrByBytes(null, 0, 1, euckr));
            assertEquals("", MbStringUtil.substrByBytes("", 0, 1, euckr));
            assertEquals("", MbStringUtil.substrByBytes("가나다abc", 2, 0, euckr));
            assertEquals("", MbStringUtil.substrByBytes("가나다abc", 2, -2, euckr));
            assertEquals("", MbStringUtil.substrByBytes("가나다abc", 100, 2, euckr));
            assertEquals("", MbStringUtil.substrByBytes("가나다abc", -100, 2, euckr));
            assertEquals("가", MbStringUtil.substrByBytes("가나다abc", 0, 2, euckr));
            assertEquals("  ", MbStringUtil.substrByBytes("가나다abc", 1, 2, euckr));
            assertEquals("다a", MbStringUtil.substrByBytes("가나다abc", 4, 3, euckr));
            assertEquals(" a", MbStringUtil.substrByBytes("가나다abc", 5, 2, euckr));
            assertEquals("가 ", MbStringUtil.substrByBytes("가나다abc", 0, 3, euckr));
            assertEquals(" 나", MbStringUtil.substrByBytes("가나다abc", 1, 3, euckr));
            assertEquals("a 가", MbStringUtil.substrByBytes("a👍가", 0, 4, euckr));
        }
    }

    @Nested
    @DisplayName("substring methods")
    class Substring {

        @Test
        @DisplayName("Extracts from start to end of string")
        void testSubstring_fromStart() {
            assertEquals("", MbStringUtil.substring(null, 0));
            assertEquals("", MbStringUtil.substring("", 0));
            assertEquals("abc", MbStringUtil.substring("가나다abc", 3));
            assertEquals("bc", MbStringUtil.substring("가나다abc", -2));
            assertEquals("a가나", MbStringUtil.substring("👍a가나", 1));
        }

        @Test
        @DisplayName("Extracts from start to end index")
        void testSubstring_withRange() {
            assertEquals("", MbStringUtil.substring(null, 0, 1));
            assertEquals("", MbStringUtil.substring("", 0, 1));
            assertEquals("가나", MbStringUtil.substring("가나다abc", 0, 2));
            assertEquals("ab", MbStringUtil.substring("가나다abc", 3, 5));
            assertEquals("나다", MbStringUtil.substring("가나다abc", -5, -3));
            assertEquals("bc", MbStringUtil.substring("가나다abc", -2, 6));
            assertEquals("", MbStringUtil.substring("가나다abc", 100, 102));
            assertEquals("", MbStringUtil.substring("가나다abc", -100, -98));
            assertEquals("", MbStringUtil.substring("가나다abc", 2, 2));
            assertEquals("", MbStringUtil.substring("가나다abc", 2, 1));
            assertEquals("👍a", MbStringUtil.substring("👍a가나", 0, 2));
            assertEquals("a가", MbStringUtil.substring("👍a가나", 1, 3));
            assertEquals("가", MbStringUtil.substring("👍a가나", -2, -1));
        }
    }

    @Nested
    @DisplayName("substringByBytes methods")
    class SubstringByBytes {

        @Test
        @DisplayName("Extracts from start to end with default (UTF-8) charset")
        void testSubstringByBytes_fromStart() {
            assertEquals("", MbStringUtil.substringByBytes(null, 0));
            assertEquals("", MbStringUtil.substringByBytes("", 0));
            assertEquals("나다abc", MbStringUtil.substringByBytes("가나다abc", 3));
            assertEquals("  abc", MbStringUtil.substringByBytes("가나다abc", -5));
            assertEquals("a가", MbStringUtil.substringByBytes("👍a가", 4));
        }

        @Test
        @DisplayName("Extracts from start to end index with default (UTF-8) charset")
        void testSubstringByBytes_withRange() {
            assertEquals("", MbStringUtil.substringByBytes(null, 0, 1));
            assertEquals("", MbStringUtil.substringByBytes("", 0, 1));
            assertEquals("", MbStringUtil.substringByBytes("가나다abc", 2, 2));
            assertEquals("", MbStringUtil.substringByBytes("가나다abc", 2, 1));
            assertEquals("가", MbStringUtil.substringByBytes("가나다abc", 0, 3));
            assertEquals(" 나", MbStringUtil.substringByBytes("가나다abc", 2, 6));
            assertEquals(" 나 ", MbStringUtil.substringByBytes("가나다abc", 2, 7));
            assertEquals("👍", MbStringUtil.substringByBytes("👍a가", 0, 4));
            assertEquals("👍a", MbStringUtil.substringByBytes("👍a가", 0, 5));
        }

        @Test
        @DisplayName("Extracts from start to end index with specified charset")
        void testSubstringByBytes_withRangeAndCharset() {
            Charset euckr = Charset.forName("EUC-KR");
            assertEquals("", MbStringUtil.substringByBytes(null, 0, 1, euckr));
            assertEquals("", MbStringUtil.substringByBytes("", 0, 1, euckr));
            assertEquals("", MbStringUtil.substringByBytes("가나다abc", 2, 2, euckr));
            assertEquals("", MbStringUtil.substringByBytes("가나다abc", 2, 1, euckr));
            assertEquals("", MbStringUtil.substringByBytes("가나다abc", 100, 102, euckr));
            assertEquals("", MbStringUtil.substringByBytes("가나다abc", -100, -98, euckr));
            assertEquals("가", MbStringUtil.substringByBytes("가나다abc", 0, 2, euckr));
            assertEquals("  ", MbStringUtil.substringByBytes("가나다abc", 1, 3, euckr));
            assertEquals("다a", MbStringUtil.substringByBytes("가나다abc", 4, 7, euckr));
            assertEquals(" a", MbStringUtil.substringByBytes("가나다abc", 5, 7, euckr));
            assertEquals("가 ", MbStringUtil.substringByBytes("가나다abc", 0, 3, euckr));
            assertEquals(" 나", MbStringUtil.substringByBytes("가나다abc", 1, 4, euckr));
            assertEquals("a 가", MbStringUtil.substringByBytes("a👍가", 0, 5, euckr));
        }
    }

    @Nested
    @DisplayName("leftPad methods")
    class LeftPad {
        @Test
        @DisplayName("Pads with spaces")
        void testLeftPad_withSpace() {
            assertEquals("     ", MbStringUtil.leftPad(null, 5));
            assertEquals("     ", MbStringUtil.leftPad("", 5));
            assertEquals("   한글", MbStringUtil.leftPad("한글", 5));
            assertEquals("한글", MbStringUtil.leftPad("한글", 2));
            assertEquals("한글", MbStringUtil.leftPad("한글", -1));
        }

        @Test
        @DisplayName("Pads with a specific character")
        void testLeftPad_withChar() {
            assertEquals("#####", MbStringUtil.leftPad(null, 5, '#'));
            assertEquals("#####", MbStringUtil.leftPad("", 5, '#'));
            assertEquals("###한글", MbStringUtil.leftPad("한글", 5, '#'));
            assertEquals("한글", MbStringUtil.leftPad("한글", 2, '#'));
            assertEquals("한글", MbStringUtil.leftPad("한글", -1, '#'));
        }

        @Test
        @DisplayName("Pads with a specific string")
        void testLeftPad_withString() {
            assertEquals("ㅎㄹㄹㅎㄹ한글", MbStringUtil.leftPad("한글", 7, "ㅎㄹㄹ"));
            assertEquals("ㅎㄹㄹ한글", MbStringUtil.leftPad("한글", 5, "ㅎㄹㄹ"));
            assertEquals("ㅎㄹ한글", MbStringUtil.leftPad("한글", 4, "ㅎㄹㄹ"));
            assertEquals("한글", MbStringUtil.leftPad("한글", 2, "ㅎㄹㄹ"));
            assertEquals("한글", MbStringUtil.leftPad("한글", -1, "ㅎㄹㄹ"));
            assertEquals("   한글", MbStringUtil.leftPad("한글", 5, null));
        }
    }

    @Nested
    @DisplayName("rightPad methods")
    class RightPad {
        @Test
        @DisplayName("Pads with spaces")
        void testRightPad_withSpace() {
            assertEquals("     ", MbStringUtil.rightPad(null, 5));
            assertEquals("     ", MbStringUtil.rightPad("", 5));
            assertEquals("한글   ", MbStringUtil.rightPad("한글", 5));
            assertEquals("한글", MbStringUtil.rightPad("한글", 2));
            assertEquals("한글", MbStringUtil.rightPad("한글", -1));
        }

        @Test
        @DisplayName("Pads with a specific character")
        void testRightPad_withChar() {
            assertEquals("#####", MbStringUtil.rightPad(null, 5, '#'));
            assertEquals("#####", MbStringUtil.rightPad("", 5, '#'));
            assertEquals("한글###", MbStringUtil.rightPad("한글", 5, '#'));
            assertEquals("한글", MbStringUtil.rightPad("한글", 2, '#'));
            assertEquals("한글", MbStringUtil.rightPad("한글", -1, '#'));
        }

        @Test
        @DisplayName("Pads with a specific string")
        void testRightPad_withString() {
            assertEquals("한글ㅎㄹㄹㅎㄹ", MbStringUtil.rightPad("한글", 7, "ㅎㄹㄹ"));
            assertEquals("한글ㅎㄹㄹ", MbStringUtil.rightPad("한글", 5, "ㅎㄹㄹ"));
            assertEquals("한글ㅎㄹ", MbStringUtil.rightPad("한글", 4, "ㅎㄹㄹ"));
            assertEquals("한글", MbStringUtil.rightPad("한글", 2, "ㅎㄹㄹ"));
            assertEquals("한글", MbStringUtil.rightPad("한글", -1, "ㅎㄹㄹ"));
            assertEquals("한글   ", MbStringUtil.rightPad("한글", 5, null));
        }
    }

    @Nested
    @DisplayName("leftPadByBytes methods")
    class LeftPadByBytes {

        @Test
        @DisplayName("Pads with spaces using default (UTF-8) charset")
        void testLeftPadByBytes_withSpace() {
            assertEquals("     ", MbStringUtil.leftPadByBytes(null, 5));
            assertEquals("     ", MbStringUtil.leftPadByBytes("", 5));
            assertEquals("   한글", MbStringUtil.leftPadByBytes("한글", 9));
            assertEquals("한글", MbStringUtil.leftPadByBytes("한글", 6));
            assertEquals("한글", MbStringUtil.leftPadByBytes("한글", -1));
        }

        @Test
        @DisplayName("Pads with a char using default (UTF-8) charset")
        void testLeftPadByBytes_withChar() {
            assertEquals("#####", MbStringUtil.leftPadByBytes(null, 5, '#'));
            assertEquals("#####", MbStringUtil.leftPadByBytes("", 5, '#'));
            assertEquals("###한글", MbStringUtil.leftPadByBytes("한글", 9, '#'));
            assertEquals("한글", MbStringUtil.leftPadByBytes("한글", 6, '#'));
            assertEquals("한글", MbStringUtil.leftPadByBytes("한글", -1, '#'));
        }

        @Test
        @DisplayName("Pads with a string using default (UTF-8) charset")
        void testLeftPadByBytes_withString() {
            assertEquals("ㅎㄹ  ", MbStringUtil.leftPadByBytes(null, 8, "ㅎㄹ"));
            assertEquals("ㅎㄹ  ", MbStringUtil.leftPadByBytes("", 8, "ㅎㄹ"));
            assertEquals("ㅎㄹ한글", MbStringUtil.leftPadByBytes("한글", 12, "ㅎㄹㄹ"));
            assertEquals("ㅎ  한글", MbStringUtil.leftPadByBytes("한글", 11, "ㅎㄹㄹ"));
            assertEquals("한글", MbStringUtil.leftPadByBytes("한글", 6, "ㅎㄹㄹ"));
            assertEquals("한글", MbStringUtil.leftPadByBytes("한글", -1, "ㅎㄹㄹ"));
        }

        @Test
        @DisplayName("Pads with a string using specified charset")
        void testLeftPadByBytes_withStringAndCharset() {
            assertEquals("ㅎㄹㄹ한글", MbStringUtil.leftPadByBytes("한글", 15, "ㅎㄹㄹ", StandardCharsets.UTF_8));
            assertEquals("ㅎㄹ한글", MbStringUtil.leftPadByBytes("한글", 12, "ㅎㄹㄹ", StandardCharsets.UTF_8));
            assertEquals("ㅎ  한글", MbStringUtil.leftPadByBytes("한글", 11, "ㅎㄹㄹ", StandardCharsets.UTF_8));
            assertEquals("한글", MbStringUtil.leftPadByBytes("한글", 6, "ㅎㄹㄹ", StandardCharsets.UTF_8));
            assertEquals("한글", MbStringUtil.leftPadByBytes("한글", -1, "ㅎㄹㄹ", StandardCharsets.UTF_8));
            assertEquals("   한글", MbStringUtil.leftPadByBytes("한글", 9, null, StandardCharsets.UTF_8));
        }
    }

    @Nested
    @DisplayName("rightPadByBytes methods")
    class RightPadByBytes {

        @Test
        @DisplayName("Pads with spaces using default (UTF-8) charset")
        void testRightPadByBytes_withSpace() {
            assertEquals("     ", MbStringUtil.rightPadByBytes(null, 5));
            assertEquals("     ", MbStringUtil.rightPadByBytes("", 5));
            assertEquals("한글   ", MbStringUtil.rightPadByBytes("한글", 9));
            assertEquals("한글", MbStringUtil.rightPadByBytes("한글", 6));
            assertEquals("한글", MbStringUtil.rightPadByBytes("한글", -1));
        }

        @Test
        @DisplayName("Pads with a char using default (UTF-8) charset")
        void testRightPadByBytes_withChar() {
            assertEquals("#####", MbStringUtil.rightPadByBytes(null, 5, '#'));
            assertEquals("#####", MbStringUtil.rightPadByBytes("", 5, '#'));
            assertEquals("한글###", MbStringUtil.rightPadByBytes("한글", 9, '#'));
            assertEquals("한글", MbStringUtil.rightPadByBytes("한글", 6, '#'));
            assertEquals("한글", MbStringUtil.rightPadByBytes("한글", -1, '#'));
        }

        @Test
        @DisplayName("Pads with a string using default (UTF-8) charset")
        void testRightPadByBytes_withString() {
            assertEquals("ㅎㄹ  ", MbStringUtil.rightPadByBytes(null, 8, "ㅎㄹ"));
            assertEquals("ㅎㄹ  ", MbStringUtil.rightPadByBytes("", 8, "ㅎㄹ"));
            assertEquals("한글ㅎㄹ", MbStringUtil.rightPadByBytes("한글", 12, "ㅎㄹㄹ"));
            assertEquals("한글ㅎ  ", MbStringUtil.rightPadByBytes("한글", 11, "ㅎㄹㄹ"));
            assertEquals("한글", MbStringUtil.rightPadByBytes("한글", 6, "ㅎㄹㄹ"));
            assertEquals("한글", MbStringUtil.rightPadByBytes("한글", -1, "ㅎㄹㄹ"));
        }

        @Test
        @DisplayName("Pads with a string using specified charset")
        void testRightPadByBytes_withStringAndCharset() {
            assertEquals("한글ㅎㄹㄹ", MbStringUtil.rightPadByBytes("한글", 15, "ㅎㄹㄹ", StandardCharsets.UTF_8));
            assertEquals("한글ㅎㄹ", MbStringUtil.rightPadByBytes("한글", 12, "ㅎㄹㄹ", StandardCharsets.UTF_8));
            assertEquals("한글ㅎ  ", MbStringUtil.rightPadByBytes("한글", 11, "ㅎㄹㄹ", StandardCharsets.UTF_8));
            assertEquals("한글", MbStringUtil.rightPadByBytes("한글", 6, "ㅎㄹㄹ", StandardCharsets.UTF_8));
            assertEquals("한글", MbStringUtil.rightPadByBytes("한글", -1, "ㅎㄹㄹ", StandardCharsets.UTF_8));
            assertEquals("한글   ", MbStringUtil.rightPadByBytes("한글", 9, null, StandardCharsets.UTF_8));
        }
    }

    @Nested
    @DisplayName("Exception handling for invalid arguments")
    class InvalidArguments {
        @Test
        @DisplayName("Throws IllegalArgumentException for null charset")
        void testNullCharset() {
            // lengthByBytes
            assertThrows(IllegalArgumentException.class, () -> MbStringUtil.lengthByBytes("test", null));

            // substrByBytes
            assertThrows(IllegalArgumentException.class, () -> MbStringUtil.substrByBytes("test", 0, 1, null));

            // substringByBytes
            assertThrows(IllegalArgumentException.class, () -> MbStringUtil.substringByBytes("test", 0, 1, null));

            // leftPadByBytes
            assertThrows(IllegalArgumentException.class, () -> MbStringUtil.leftPadByBytes("test", 10, " ", null));

            // rightPadByBytes
            assertThrows(IllegalArgumentException.class, () -> MbStringUtil.rightPadByBytes("test", 10, " ", null));
        }
    }
}
