package just.meh.apps.utils;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashMap;
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
     * Extracts a substring from a string based on character (code point) count.
     * This method is safe for supplementary characters (e.g., emojis).
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
     *
     * // Emoji examples
     * MbStringUtil.substr("👍a가나", 0, 2) = "👍a"
     * MbStringUtil.substr("👍a가나", 1, 2) = "a가"
     * MbStringUtil.substr("👍a가나", -2, 1) = "가"
     * </pre>
     *
     * @param str The source string.
     * @param start The starting code point index (0-based). If negative, it is an offset from the end.
     * @param len The desired length of the substring in code points.
     * @return The resulting substring.
     */
    public static String substr(String str, int start, int len) {
        if (str == null || str.isEmpty() || len <= 0) {
            return EMPTY_STRING;
        }

        int codePointCount = str.codePointCount(0, str.length());
        int effectiveStart;

        if (start >= 0) {
            effectiveStart = start;
        } else { // start < 0
            effectiveStart = codePointCount + start;
        }

        if (effectiveStart < 0 || effectiveStart >= codePointCount) {
            return EMPTY_STRING;
        }

        int effectiveLen = Math.min(len, codePointCount - effectiveStart);
        if (effectiveLen <= 0) {
            return EMPTY_STRING;
        }
        
        int startCharIndex = str.offsetByCodePoints(0, effectiveStart);
        int endCharIndex = str.offsetByCodePoints(startCharIndex, effectiveLen);

        return str.substring(startCharIndex, endCharIndex);
    }

    /**
     * Extracts a substring from a string based on byte length. If the desired length
     * truncates a multi-byte character, the broken part is replaced with spaces.
     * This method is safe for supplementary characters (e.g., emojis).
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
     *
     * // UTF-8 Emoji Examples ("👍a가" is 8 bytes: 4 + 1 + 3)
     * MbStringUtil.substrByBytes("👍a가", 0, 3, StandardCharsets.UTF_8) = "   "
     * MbStringUtil.substrByBytes("👍a가", 0, 4, StandardCharsets.UTF_8) = "👍"
     * MbStringUtil.substrByBytes("👍a가", 0, 5, StandardCharsets.UTF_8) = "👍a"
     * MbStringUtil.substrByBytes("👍a가", 3, 3, StandardCharsets.UTF_8) = " a "
     * MbStringUtil.substrByBytes("👍a가", 4, 4, StandardCharsets.UTF_8) = "a가"
     *
     * // Unencodable character example with EUC-KR
     * MbStringUtil.substrByBytes("a👍가", 0, 4, euckr) = "a 가"
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

        // 1. Create maps for code point boundaries
        CharsetEncoder encoder = charset.newEncoder();
        List<Integer> codePointStartIndices = new ArrayList<>(); // Stores the char index for the start of each code point
        List<Integer> codePointByteLengths = new ArrayList<>();
        HashMap<Integer, Integer> byteOffsetToCodePointIndexMap = new HashMap<>();
        int totalBytes = 0;

        for (int i = 0; i < str.length(); ) {
            int codePoint = str.codePointAt(i);
            
            byteOffsetToCodePointIndexMap.put(totalBytes, codePointStartIndices.size());
            codePointStartIndices.add(i);

            try {
                String codePointStr = new String(Character.toChars(codePoint));
                ByteBuffer bb = encoder.encode(java.nio.CharBuffer.wrap(codePointStr));
                int byteLength = bb.limit();
                codePointByteLengths.add(byteLength);
                totalBytes += byteLength;
            } catch (CharacterCodingException e) {
                // For unencodable characters, mark with a special length (-1) and assume a byte length of 1
                // for placeholder purposes, as they will be replaced by a single space.
                codePointByteLengths.add(-1);
                totalBytes += 1; // Assume 1 byte for the placeholder space
            }
            i += Character.charCount(codePoint);
        }

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
            Integer codePointIndex = byteOffsetToCodePointIndexMap.get(currentByte);

            if (codePointIndex != null) {
                // Current byte is the beginning of a code point
                int charByteLength = codePointByteLengths.get(codePointIndex);

                if (charByteLength == -1) {
                    // This was an unencodable character.
                    result.append(PADDING_CHAR);
                    currentByte++;
                } else {
                    int charStartIndex = codePointStartIndices.get(codePointIndex);
                    if (currentByte + charByteLength <= effectiveEnd) {
                        // Code point fits completely within the selection
                        int codePoint = str.codePointAt(charStartIndex);
                        result.append(Character.toChars(codePoint));
                        currentByte += charByteLength;
                    } else {
                        // Code point is truncated by the end of the selection
                        result.append(PADDING_CHAR);
                        currentByte++;
                    }
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
