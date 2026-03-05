package just.meh.apps.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MbStringUtilTest {

    @Test
    @DisplayName("Extracts substring based on character count")
    void testSubstring() {
        // str is null or empty
        assertEquals("", MbStringUtil.substring(null, 0, 1));
        assertEquals("", MbStringUtil.substring("", 0, 1));

        // start is positive
        assertEquals("가나", MbStringUtil.substring("가나다abc", 0, 2));
        assertEquals("ab", MbStringUtil.substring("가나다abc", 3, 5));

        // start is negative
        assertEquals("나다", MbStringUtil.substring("가나다abc", -5, -3));
        assertEquals("bc", MbStringUtil.substring("가나다abc", -2, 6));

        // start is out of bounds
        assertEquals("", MbStringUtil.substring("가나다abc", 100, 102));
        assertEquals("", MbStringUtil.substring("가나다abc", -100, -98));

        // start >= end
        assertEquals("", MbStringUtil.substring("가나다abc", 2, 2));
        assertEquals("", MbStringUtil.substring("가나다abc", 2, 1));

        // Emoji examples
        assertEquals("👍a", MbStringUtil.substring("👍a가나", 0, 2));
        assertEquals("a가", MbStringUtil.substring("👍a가나", 1, 3));
        assertEquals("가", MbStringUtil.substring("👍a가나", -2, -1));
    }

    @Test
    @DisplayName("Extracts substring based on byte length")
    void testSubstringByBytes() {
        Charset euckr = Charset.forName("EUC-KR");

        // str is null or empty
        assertEquals("", MbStringUtil.substringByBytes(null, 0, 1, StandardCharsets.UTF_8));
        assertEquals("", MbStringUtil.substringByBytes("", 0, 1, StandardCharsets.UTF_8));

        // start >= end
        assertEquals("", MbStringUtil.substringByBytes("가나다abc", 2, 2, StandardCharsets.UTF_8));
        assertEquals("", MbStringUtil.substringByBytes("가나다abc", 2, 1, StandardCharsets.UTF_8));

        // start is out of bounds
        assertEquals("", MbStringUtil.substringByBytes("가나다abc", 100, 102, StandardCharsets.UTF_8));
        assertEquals("", MbStringUtil.substringByBytes("가나다abc", -100, -98, StandardCharsets.UTF_8));

        // EUC-KR Examples
        assertEquals("가", MbStringUtil.substringByBytes("가나다abc", 0, 2, euckr));
        assertEquals("  ", MbStringUtil.substringByBytes("가나다abc", 1, 3, euckr));
        assertEquals("다a", MbStringUtil.substringByBytes("가나다abc", 4, 7, euckr));
        assertEquals(" a", MbStringUtil.substringByBytes("가나다abc", 5, 7, euckr));
        assertEquals("가 ", MbStringUtil.substringByBytes("가나다abc", 0, 3, euckr));
        assertEquals(" 나", MbStringUtil.substringByBytes("가나다abc", 1, 4, euckr));

        // UTF-8 Examples
        assertEquals("  ", MbStringUtil.substringByBytes("가나다abc", 0, 2, StandardCharsets.UTF_8));
        assertEquals("  ", MbStringUtil.substringByBytes("가나다abc", 1, 3, StandardCharsets.UTF_8));
        assertEquals("   ", MbStringUtil.substringByBytes("가나다abc", 4, 7, StandardCharsets.UTF_8));
        assertEquals("가", MbStringUtil.substringByBytes("가나다abc", 0, 3, StandardCharsets.UTF_8));
        assertEquals(" 나", MbStringUtil.substringByBytes("가나다abc", 2, 6, StandardCharsets.UTF_8));
        assertEquals(" 나 ", MbStringUtil.substringByBytes("가나다abc", 2, 7, StandardCharsets.UTF_8));

        // UTF-8 Emoji Examples ("👍a가" is 8 bytes: 4 + 1 + 3)
        assertEquals("   ", MbStringUtil.substringByBytes("👍a가", 0, 3, StandardCharsets.UTF_8));
        assertEquals("👍", MbStringUtil.substringByBytes("👍a가", 0, 4, StandardCharsets.UTF_8));
        assertEquals("👍a", MbStringUtil.substringByBytes("👍a가", 0, 5, StandardCharsets.UTF_8));
        assertEquals(" a ", MbStringUtil.substringByBytes("👍a가", 3, 6, StandardCharsets.UTF_8));
        assertEquals("a가", MbStringUtil.substringByBytes("👍a가", 4, 8, StandardCharsets.UTF_8));

        // Unencodable character example with EUC-KR
        assertEquals("a 가", MbStringUtil.substringByBytes("a👍가", 0, 5, euckr));
    }
}
