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
    }
}
