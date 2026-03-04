package just.meh.apps.utils;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for byte-level manipulation of multi-byte strings.
 */
public final class MbStringUtil {

    private static final String EMPTY_STRING = "";
    private static final char PADDING_CHAR = ' ';

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private MbStringUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Extracts a substring from a string based on character count.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.substr(null, 0, 1)      = ""
     * MbStringUtil.substr("", 0, 1)        = ""
     *
     * // start is positive
     * MbStringUtil.substr("가나다abc", 0, 2) = "가나"
     * MbStringUtil.substr("가나다abc", 3, 2) = "ab"
     *
     * // start is negative
     * MbStringUtil.substr("가나다abc", -5, 2) = "나다"
     * MbStringUtil.substr("가나다abc", -2, 2) = "bc"
     *
     * // start is out of bounds
     * MbStringUtil.substr("가나다abc", 100, 2) = ""
     * MbStringUtil.substr("가나다abc", -100, 2) = ""
     *
     * // len is zero or negative
     * MbStringUtil.substr("가나다abc", 2, 0)  = ""
     * MbStringUtil.substr("가나다abc", 2, -2) = ""
     * </pre>
     *
     * @param str The source string.
     * @param start The starting character index (0-based). If negative, it is an offset from the end.
     * @param len The desired length of the substring in characters.
     * @return The resulting substring.
     */
    public static String substr(String str, int start, int len) {
        if (str == null || str.isEmpty() || len <= 0) {
            return EMPTY_STRING;
        }

        int strLen = str.length();
        int effectiveStart;

        if (start >= 0) {
            effectiveStart = start;
        } else { // start < 0
            effectiveStart = strLen + start;
        }

        if (effectiveStart < 0 || effectiveStart >= strLen) {
            return EMPTY_STRING;
        }

        int effectiveEnd = Math.min(effectiveStart + len, strLen);

        if (effectiveStart >= effectiveEnd) {
            return EMPTY_STRING;
        }

        return str.substring(effectiveStart, effectiveEnd);
    }

    /**
     * Extracts a substring from a string based on byte length. If the desired length
     * truncates a multi-byte character, the broken part is replaced with spaces.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.substrByBytes(null, 0, 1, StandardCharsets.UTF_8)      = ""
     * MbStringUtil.substrByBytes("", 0, 1, StandardCharsets.UTF_8)        = ""
     *
     * // len is zero or negative
     * MbStringUtil.substrByBytes("가나다abc", 2, 0, StandardCharsets.UTF_8)  = ""
     * MbStringUtil.substrByBytes("가나다abc", 2, -2, StandardCharsets.UTF_8) = ""
     *
     * // start is out of bounds
     * MbStringUtil.substrByBytes("가나다abc", 100, 2, StandardCharsets.UTF_8) = ""
     * MbStringUtil.substrByBytes("가나다abc", -100, 2, StandardCharsets.UTF_8) = ""
     *
     * // EUC-KR Examples
     * Charset euckr = Charset.forName("EUC-KR");
     * MbStringUtil.substrByBytes("가나다abc", 0, 2, euckr) = "가"
     * MbStringUtil.substrByBytes("가나다abc", 1, 2, euckr) = "  "
     * MbStringUtil.substrByBytes("가나다abc", 4, 3, euckr) = "다a"
     * MbStringUtil.substrByBytes("가나다abc", 5, 2, euckr) = " a"
     * MbStringUtil.substrByBytes("가나다abc", 0, 3, euckr) = "가 "
     * MbStringUtil.substrByBytes("가나다abc", 1, 3, euckr) = " 나"
     *
     * // UTF-8 Examples
     * MbStringUtil.substrByBytes("가나다abc", 0, 2, StandardCharsets.UTF_8) = "  "
     * MbStringUtil.substrByBytes("가나다abc", 1, 2, StandardCharsets.UTF_8) = "  "
     * MbStringUtil.substrByBytes("가나다abc", 4, 3, StandardCharsets.UTF_8) = "   "
     * MbStringUtil.substrByBytes("가나다abc", 0, 3, StandardCharsets.UTF_8) = "가"
     * MbStringUtil.substrByBytes("가나다abc", 2, 4, StandardCharsets.UTF_8) = " 나"
     * MbStringUtil.substrByBytes("가나다abc", 2, 5, StandardCharsets.UTF_8) = " 나 "
     * </pre>
     *
     * @param str The source string.
     * @param start The starting byte offset (0-based). If negative, it is an offset from the end.
     * @param len The desired length of the substring in bytes.
     * @param charset The character set to use.
     * @return The resulting substring, padded with spaces if necessary.
     */
    public static String substrByBytes(String str, int start, int len, Charset charset) {
        if (str == null || str.isEmpty() || len <= 0) {
            return EMPTY_STRING;
        }

        // 1. Create maps for character boundaries
        CharsetEncoder encoder = charset.newEncoder();
        List<Integer> charByteOffsets = new ArrayList<>();
        List<Integer> charByteLengths = new ArrayList<>();
        charByteOffsets.add(0);
        int totalBytes = 0;

        for (int i = 0; i < str.length(); i++) {
            try {
                ByteBuffer bb = encoder.encode(java.nio.CharBuffer.wrap(new char[]{str.charAt(i)}));
                int byteLength = bb.limit();
                charByteLengths.add(byteLength);
                totalBytes += byteLength;
                charByteOffsets.add(totalBytes);
            } catch (CharacterCodingException e) {
                throw new RuntimeException("Failed to encode character", e);
            }
        }
        charByteOffsets.remove(charByteOffsets.size() - 1); // remove last element which is totalBytes

        // 2. Determine effective byte range
        int effectiveStart;
        if (start >= 0) {
            effectiveStart = start;
        } else { // start < 0
            effectiveStart = totalBytes + start;
        }

        if (effectiveStart < 0 || effectiveStart >= totalBytes) {
            return EMPTY_STRING;
        }

        int effectiveEnd = Math.min(effectiveStart + len, totalBytes);
        if (effectiveEnd <= effectiveStart) {
            return EMPTY_STRING;
        }

        // 3. Build the result string byte by byte
        StringBuilder result = new StringBuilder();
        for (int currentByte = effectiveStart; currentByte < effectiveEnd; ) {
            int charIndex = -1;
            for(int i = 0; i < charByteOffsets.size(); i++){
                if(charByteOffsets.get(i) == currentByte){
                    charIndex = i;
                    break;
                }
            }

            if (charIndex != -1) {
                // Current byte is the beginning of a character
                int charByteLength = charByteLengths.get(charIndex);
                if (currentByte + charByteLength <= effectiveEnd) {
                    // Character fits completely within the selection
                    result.append(str.charAt(charIndex));
                    currentByte += charByteLength;
                } else {
                    // Character is truncated by the end of the selection
                    result.append(PADDING_CHAR);
                    currentByte++;
                }
            } else {
                // Current byte is in the middle of a multi-byte character
                result.append(PADDING_CHAR);
                currentByte++;
            }
        }

        return result.toString();
    }
}
