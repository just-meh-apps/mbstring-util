package just.meh.apps.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("MbStringUtilTest")
class MbStringUtilTest {

    private static final Charset EUCKR = Charset.forName("EUC-KR");

    @Test
    @DisplayName("substr(String, int, int)")
    void testSubstr() {
        // str is null or empty
        assertEquals("", MbStringUtil.substr(null, 0, 1));
        assertEquals("", MbStringUtil.substr("", 0, 1));

        // start is positive
        assertEquals("가나", MbStringUtil.substr("가나다abc", 0, 2));
        assertEquals("ab", MbStringUtil.substr("가나다abc", 3, 2));

        // start is negative
        assertEquals("나다", MbStringUtil.substr("가나다abc", -5, 2));
        assertEquals("bc", MbStringUtil.substr("가나다abc", -2, 2));

        // start is out of bounds
        assertEquals("", MbStringUtil.substr("가나다abc", 100, 2));
        assertEquals("", MbStringUtil.substr("가나다abc", -100, 2));

        // len is zero or negative
        assertEquals("", MbStringUtil.substr("가나다abc", 2, 0));
        assertEquals("", MbStringUtil.substr("가나다abc", 2, -2));

        // Emoji examples
        assertEquals("👍a", MbStringUtil.substr("👍a가나", 0, 2));
        assertEquals("a가", MbStringUtil.substr("👍a가나", 1, 2));
        assertEquals("가", MbStringUtil.substr("👍a가나", -2, 1));
    }

    @Test
    @DisplayName("substrByBytes(String, int, int, Charset)")
    void testSubstrByBytes() {
        // str is null or empty
        assertEquals("", MbStringUtil.substrByBytes(null, 0, 1, StandardCharsets.UTF_8));
        assertEquals("", MbStringUtil.substrByBytes("", 0, 1, StandardCharsets.UTF_8));

        // len is zero or negative
        assertEquals("", MbStringUtil.substrByBytes("가나다abc", 2, 0, StandardCharsets.UTF_8));
        assertEquals("", MbStringUtil.substrByBytes("가나다abc", 2, -2, StandardCharsets.UTF_8));
        
        // start is out of bounds
        assertEquals("", MbStringUtil.substrByBytes("가나다abc", 100, 2, StandardCharsets.UTF_8));
        assertEquals("", MbStringUtil.substrByBytes("가나다abc", -100, 2, StandardCharsets.UTF_8));

        // EUC-KR Examples
        assertEquals("가", MbStringUtil.substrByBytes("가나다abc", 0, 2, EUCKR));
        assertEquals("  ", MbStringUtil.substrByBytes("가나다abc", 1, 2, EUCKR));
        assertEquals("다a", MbStringUtil.substrByBytes("가나다abc", 4, 3, EUCKR));
        assertEquals(" a", MbStringUtil.substrByBytes("가나다abc", 5, 2, EUCKR)); // Corrected params
        assertEquals("가 ", MbStringUtil.substrByBytes("가나다abc", 0, 3, EUCKR));
        assertEquals(" 나", MbStringUtil.substrByBytes("가나다abc", 1, 3, EUCKR));

        // UTF-8 Examples
        assertEquals("  ", MbStringUtil.substrByBytes("가나다abc", 0, 2, StandardCharsets.UTF_8));
        assertEquals("  ", MbStringUtil.substrByBytes("가나다abc", 1, 2, StandardCharsets.UTF_8));
        assertEquals("   ", MbStringUtil.substrByBytes("가나다abc", 4, 3, StandardCharsets.UTF_8));
        assertEquals("가", MbStringUtil.substrByBytes("가나다abc", 0, 3, StandardCharsets.UTF_8));
        assertEquals(" 나", MbStringUtil.substrByBytes("가나다abc", 2, 4, StandardCharsets.UTF_8)); // Corrected params
        assertEquals(" 나 ", MbStringUtil.substrByBytes("가나다abc", 2, 5, StandardCharsets.UTF_8)); // Corrected params
        
        // UTF-8 Emoji Examples ("👍a가" is 8 bytes: 4 + 1 + 3)
        assertEquals("   ", MbStringUtil.substrByBytes("👍a가", 0, 3, StandardCharsets.UTF_8));
        assertEquals("👍", MbStringUtil.substrByBytes("👍a가", 0, 4, StandardCharsets.UTF_8));
        assertEquals("👍a", MbStringUtil.substrByBytes("👍a가", 0, 5, StandardCharsets.UTF_8));
        assertEquals(" a ", MbStringUtil.substrByBytes("👍a가", 3, 3, StandardCharsets.UTF_8));
        assertEquals("a가", MbStringUtil.substrByBytes("👍a가", 4, 4, StandardCharsets.UTF_8));

        // Unencodable character example with EUC-KR
        // "a" is 1 byte, "👍" is unencodable (becomes 1 space), "가" is 2 bytes.
        // Total pseudo-bytes = 1 + 1 + 2 = 4
        assertEquals("a 가", MbStringUtil.substrByBytes("a👍가", 0, 4, EUCKR));
        assertEquals("a  ", MbStringUtil.substrByBytes("a👍가", 0, 3, EUCKR));
        assertEquals(" 가", MbStringUtil.substrByBytes("a👍가", 1, 3, EUCKR));
        assertEquals(" ", MbStringUtil.substrByBytes("a👍가", 1, 1, EUCKR));
    }

    @Test
    @DisplayName("length(String)")
    void testLength() {
        // null or empty
        assertEquals(0, MbStringUtil.length(null));
        assertEquals(0, MbStringUtil.length(""));

        // regular strings
        assertEquals(3, MbStringUtil.length("abc"));
        assertEquals(3, MbStringUtil.length("가나다"));

        // string with supplementary characters
        assertEquals(3, MbStringUtil.length("👍a가"));
        assertEquals(1, MbStringUtil.length("👍"));
    }

    @Test
    @DisplayName("lengthByBytes(String, Charset)")
    void testLengthByBytes() {
        // str is null or empty
        assertEquals(0, MbStringUtil.lengthByBytes(null, StandardCharsets.UTF_8));
        assertEquals(0, MbStringUtil.lengthByBytes("", StandardCharsets.UTF_8));

        // EUC-KR Examples
        assertEquals(3, MbStringUtil.lengthByBytes("abc", EUCKR));
        assertEquals(6, MbStringUtil.lengthByBytes("가나다", EUCKR));

        // UTF-8 Examples
        assertEquals(3, MbStringUtil.lengthByBytes("abc", StandardCharsets.UTF_8));
        assertEquals(9, MbStringUtil.lengthByBytes("가나다", StandardCharsets.UTF_8));
        assertEquals(8, MbStringUtil.lengthByBytes("👍a가", StandardCharsets.UTF_8)); // 4 + 1 + 3

        // Unencodable character example (EUC-KR cannot encode emoji)
        // It gets replaced by a '?' which is 1 byte in EUC-KR.
        assertEquals(1, MbStringUtil.lengthByBytes("👍", EUCKR));
    }
}
