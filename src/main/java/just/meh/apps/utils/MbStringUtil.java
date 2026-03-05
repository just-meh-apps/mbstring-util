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
 * <p>
 * 멀티바이트 문자열의 바이트 레벨 조작을 위한 유틸리티 클래스입니다.
 */
public final class MbStringUtil {

    private static final String EMPTY_STRING = "";
    private static final char PADDING_CHAR = ' ';
    private static final int UNENCODABLE_CHAR_LENGTH = -1;
    private static final int PLACEHOLDER_BYTE_LENGTH = 1;

    /**
     * Private constructor to prevent instantiation of this utility class.
     * <p>
     * 이 유틸리티 클래스의 인스턴스화를 방지하기 위한 private 생성자입니다.
     */
    private MbStringUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Extracts a substring from a string based on character (code point) count.
     * This method is safe for supplementary characters (e.g., emojis).
     * <p>
     * 문자(코드 포인트) 수를 기준으로 문자열에서 하위 문자열을 추출합니다.
     * 이모티콘과 같은 보충 문자에 안전합니다.
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
     * @param str The source string. (원본 문자열)
     * @param start The starting code point index (0-based). If negative, it is an offset from the end. (시작 코드 포인트 인덱스 (0부터 시작). 음수일 경우 끝에서의 오프셋입니다.)
     * @param len The desired length of the substring in code points. (코드 포인트 단위의 원하는 하위 문자열 길이)
     * @return The resulting substring. (결과 하위 문자열)
     */
    public static String substr(String str, int start, int len) {
        if (str == null || str.isEmpty() || len <= 0) {
            return EMPTY_STRING;
        }

        int codePointCount = str.codePointCount(0, str.length());
        int effectiveStart = calculateEffectiveStart(codePointCount, start);

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
     * <p>
     * 바이트 길이를 기준으로 문자열에서 하위 문자열을 추출합니다. 원하는 길이가 멀티바이트 문자를 자르는 경우,
     * 잘린 부분은 공백으로 대체됩니다. 이모티콘과 같은 보충 문자에 안전합니다.
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
     * @param str The source string. (원본 문자열)
     * @param start The starting byte offset (0-based). If negative, it is an offset from the end. (시작 바이트 오프셋 (0부터 시작). 음수일 경우 끝에서의 오프셋입니다.)
     * @param len The desired length of the substring in bytes. (바이트 단위의 원하는 하위 문자열 길이)
     * @param charset The character set to use. (사용할 문자 집합)
     * @return The resulting substring, padded with spaces if necessary. (결과 하위 문자열, 필요한 경우 공백으로 채워집니다.)
     */
    public static String substrByBytes(String str, int start, int len, Charset charset) {
        if (str == null || str.isEmpty() || len <= 0) {
            return EMPTY_STRING;
        }

        StringMetadata metadata = analyzeString(str, charset);
        int effectiveStart = calculateEffectiveStart(metadata.totalBytes, start);

        if (effectiveStart < 0 || effectiveStart >= metadata.totalBytes) {
            return EMPTY_STRING;
        }

        int effectiveEnd = Math.min(effectiveStart + len, metadata.totalBytes);
        if (effectiveEnd <= effectiveStart) {
            return EMPTY_STRING;
        }

        return buildSubstring(str, metadata, effectiveStart, effectiveEnd);
    }

    /**
     * Returns the length of a string in code points, returning 0 for null or empty strings.
     * This method is safe for supplementary characters (e.g., emojis).
     * <p>
     * 문자열의 길이를 코드 포인트 단위로 반환하며, null 또는 빈 문자열의 경우 0을 반환합니다.
     * 이모티콘과 같은 보충 문자에 안전합니다.
     * <p>
     * Note: This result may differ from {@code String.length()}, which counts 16-bit {@code char} units.
     * For example, a supplementary character like an emoji ("👍") is counted as a single character by this method,
     * but as two {@code char}s by {@code String.length()}.
     * <p>
     * 참고: 이 결과는 16비트 {@code char} 단위를 세는 {@code String.length()}와 다를 수 있습니다.
     * 예를 들어, 이모티콘("👍")과 같은 보충 문자는 이 메소드에서는 단일 문자로 계산되지만,
     * {@code String.length()}에서는 두 개의 {@code char}로 계산됩니다.
     *
     * <pre>
     * MbStringUtil.length(null)      = 0
     * MbStringUtil.length("")        = 0
     * MbStringUtil.length("abc")     = 3
     * MbStringUtil.length("가나다")   = 3
     * MbStringUtil.length("👍a가")   = 3 // contrast with "👍a가".length() which is 4
     * </pre>
     *
     * @param str The string to check. (확인할 문자열)
     * @return The number of code points in the string, or 0 if the string is null or empty. (문자열의 코드 포인트 수, 문자열이 null이거나 비어 있으면 0)
     */
    public static int length(String str) {
        if (str == null || str.isEmpty()) {
            return 0;
        }
        return str.codePointCount(0, str.length());
    }

    /**
     * Returns the byte length of a string for a given charset, returning 0 for null or empty strings.
     * <p>
     * 주어진 문자 집합에 대한 문자열의 바이트 길이를 반환하며, null 또는 빈 문자열의 경우 0을 반환합니다.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.lengthByBytes(null, StandardCharsets.UTF_8)    = 0
     * MbStringUtil.lengthByBytes("", StandardCharsets.UTF_8)      = 0
     *
     * // EUC-KR Examples
     * Charset euckr = Charset.forName("EUC-KR");
     * MbStringUtil.lengthByBytes("abc", euckr)   = 3
     * MbStringUtil.lengthByBytes("가나다", euckr) = 6
     *
     * // UTF-8 Examples
     * MbStringUtil.lengthByBytes("abc", StandardCharsets.UTF_8)   = 3
     * MbStringUtil.lengthByBytes("가나다", StandardCharsets.UTF_8) = 9
     * MbStringUtil.lengthByBytes("👍a가", StandardCharsets.UTF_8) = 8
     *
     * // Unencodable character example
     * MbStringUtil.lengthByBytes("👍", euckr) = 1 // '?'
     * </pre>
     *
     * @param str The string to measure. (측정할 문자열)
     * @param charset The character set to use for encoding. (인코딩에 사용할 문자 집합)
     * @return The length of the string in bytes, or 0 if the string is null or empty. (문자열의 바이트 단위 길이, 문자열이 null이거나 비어 있으면 0)
     */
    public static int lengthByBytes(String str, Charset charset) {
        if (str == null || str.isEmpty()) {
            return 0;
        }
        return str.getBytes(charset).length;
    }

    private static class StringMetadata {
        final List<Integer> codePointStartIndices = new ArrayList<>();
        final List<Integer> codePointByteLengths = new ArrayList<>();
        final HashMap<Integer, Integer> byteOffsetToCodePointIndexMap = new HashMap<>();
        int totalBytes = 0;
    }

    private static StringMetadata analyzeString(String str, Charset charset) {
        StringMetadata metadata = new StringMetadata();
        CharsetEncoder encoder = charset.newEncoder();

        for (int i = 0; i < str.length(); ) {
            int codePoint = str.codePointAt(i);

            metadata.byteOffsetToCodePointIndexMap.put(metadata.totalBytes, metadata.codePointStartIndices.size());
            metadata.codePointStartIndices.add(i);

            try {
                String codePointStr = new String(Character.toChars(codePoint));
                ByteBuffer bb = encoder.encode(java.nio.CharBuffer.wrap(codePointStr));
                int byteLength = bb.limit();
                metadata.codePointByteLengths.add(byteLength);
                metadata.totalBytes += byteLength;
            } catch (CharacterCodingException e) {
                metadata.codePointByteLengths.add(UNENCODABLE_CHAR_LENGTH);
                metadata.totalBytes += PLACEHOLDER_BYTE_LENGTH;
            }
            i += Character.charCount(codePoint);
        }
        return metadata;
    }

    private static String buildSubstring(String originalStr, StringMetadata metadata, int startByte, int endByte) {
        StringBuilder result = new StringBuilder();
        for (int currentByte = startByte; currentByte < endByte; ) {
            Integer codePointIndex = metadata.byteOffsetToCodePointIndexMap.get(currentByte);

            if (codePointIndex != null) {
                int charByteLength = metadata.codePointByteLengths.get(codePointIndex);

                if (charByteLength == UNENCODABLE_CHAR_LENGTH) {
                    result.append(PADDING_CHAR);
                    currentByte += PLACEHOLDER_BYTE_LENGTH;
                } else {
                    if (currentByte + charByteLength <= endByte) {
                        int charStartIndex = metadata.codePointStartIndices.get(codePointIndex);
                        int codePoint = originalStr.codePointAt(charStartIndex);
                        result.append(Character.toChars(codePoint));
                        currentByte += charByteLength;
                    } else {
                        result.append(PADDING_CHAR);
                        currentByte++;
                    }
                }
            } else {
                result.append(PADDING_CHAR);
                currentByte++;
            }
        }
        return result.toString();
    }
    
    private static int calculateEffectiveStart(int totalLength, int start) {
        return start >= 0 ? start : totalLength + start;
    }
}
