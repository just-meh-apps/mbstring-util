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

    // 자주 사용되는 상수를 정의하여 코드의 가독성과 유지보수성을 높입니다.
    private static final String EMPTY_STRING = ""; // 빈 문자열
    private static final char PADDING_CHAR = ' '; // 패딩에 사용될 문자
    private static final int UNENCODABLE_CHAR_LENGTH = -1; // 인코딩 불가능한 문자의 길이를 나타내는 값
    private static final int PLACEHOLDER_BYTE_LENGTH = 1; // 인코딩 불가능한 문자를 대체하는 플레이스홀더의 바이트 길이

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
        // 입력 문자열이 null이거나 비어있거나, 길이가 0 이하이면 빈 문자열을 반환합니다.
        if (str == null || str.isEmpty() || len <= 0) {
            return EMPTY_STRING;
        }

        // 문자열의 전체 코드 포인트 수를 계산합니다.
        int codePointCount = str.codePointCount(0, str.length());
        // 실제 시작 위치를 계산합니다. (음수 인덱스 처리 포함)
        int effectiveStart = calculateEffectiveStart(codePointCount, start);

        // 시작 위치가 유효 범위를 벗어나면 빈 문자열을 반환합니다.
        if (effectiveStart < 0 || effectiveStart >= codePointCount) {
            return EMPTY_STRING;
        }

        // 실제 추출할 길이를 계산합니다. (문자열 끝을 넘지 않도록 조정)
        int effectiveLen = Math.min(len, codePointCount - effectiveStart);
        // 길이가 0 이하면 빈 문자열을 반환합니다.
        if (effectiveLen <= 0) {
            return EMPTY_STRING;
        }

        // 코드 포인트 인덱스를 실제 char 인덱스로 변환합니다.
        int startCharIndex = str.offsetByCodePoints(0, effectiveStart);
        int endCharIndex = str.offsetByCodePoints(startCharIndex, effectiveLen);

        // substring을 추출하여 반환합니다.
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
        // 입력 문자열이 null이거나 비어있거나, 길이가 0 이하이면 빈 문자열을 반환합니다.
        if (str == null || str.isEmpty() || len <= 0) {
            return EMPTY_STRING;
        }

        // 문자열의 메타데이터(각 문자의 바이트 길이 등)를 분석합니다.
        StringMetadata metadata = analyzeString(str, charset);
        // 실제 시작 바이트 위치를 계산합니다. (음수 인덱스 처리 포함)
        int effectiveStart = calculateEffectiveStart(metadata.totalBytes, start);

        // 시작 위치가 유효 범위를 벗어나면 빈 문자열을 반환합니다.
        if (effectiveStart < 0 || effectiveStart >= metadata.totalBytes) {
            return EMPTY_STRING;
        }

        // 실제 추출할 끝 바이트 위치를 계산합니다.
        int effectiveEnd = Math.min(effectiveStart + len, metadata.totalBytes);
        // 끝 위치가 시작 위치보다 작거나 같으면 빈 문자열을 반환합니다.
        if (effectiveEnd <= effectiveStart) {
            return EMPTY_STRING;
        }

        // 메타데이터를 사용하여 하위 문자열을 구성합니다.
        return buildSubstring(str, metadata, effectiveStart, effectiveEnd);
    }

    /**
     * Extracts a substring from a string using start and end code point indices.
     * This method is a wrapper around {@link #substr(String, int, int)}.
     * <p>
     * 시작 및 끝 코드 포인트 인덱스를 사용하여 문자열에서 하위 문자열을 추출합니다.
     * 이 메소드는 {@link #substr(String, int, int)}를 감싸는 래퍼입니다.
     * 
     * <pre>
     * // str is null or empty
     * MbStringUtil.substring(null, 0, 1)      = ""
     * MbStringUtil.substring("", 0, 1)        = ""
     *
     * // start is positive
     * MbStringUtil.substring("가나다abc", 0, 2) = "가나"
     * MbStringUtil.substring("가나다abc", 3, 5) = "ab"
     *
     * // start is negative
     * MbStringUtil.substring("가나다abc", -5, -3) = "나다"
     * MbStringUtil.substring("가나다abc", -2, 6) = "bc"
     *
     * // start is out of bounds
     * MbStringUtil.substring("가나다abc", 100, 102) = ""
     * MbStringUtil.substring("가나다abc", -100, -98) = ""
     *
     * // start >= end
     * MbStringUtil.substring("가나다abc", 2, 2)  = ""
     * MbStringUtil.substring("가나다abc", 2, 1) = ""
     *
     * // Emoji examples
     * MbStringUtil.substring("👍a가나", 0, 2) = "👍a"
     * MbStringUtil.substring("👍a가나", 1, 3) = "a가"
     * MbStringUtil.substring("👍a가나", -2, -1) = "가"
     * </pre>
     *
     * @param str The source string. (원본 문자열)
     * @param start The beginning code point index, inclusive. Negative values are offsets from the end. (시작 코드 포인트 인덱스(포함). 음수 값은 끝에서의 오프셋입니다.)
     * @param end The ending code point index, exclusive. Negative values are offsets from the end. (끝 코드 포인트 인덱스(제외). 음수 값은 끝에서의 오프셋입니다.)
     * @return The specified substring. (지정된 하위 문자열)
     */
    public static String substring(String str, int start, int end) {
        if (str == null || str.isEmpty()) {
            return EMPTY_STRING;
        }

        int codePointCount = str.codePointCount(0, str.length());

        // Calculate effective start index
        int actualStart = (start >= 0) ? start : codePointCount + start;

        // Per requirement, if start is out of bounds, return empty string.
        if (actualStart < 0 || actualStart >= codePointCount) {
            return EMPTY_STRING;
        }

        // Calculate effective end index
        int actualEnd = (end >= 0) ? end : codePointCount + end;
        
        // Per requirement, if end is out of bounds, clamp it to the valid range [0, codePointCount].
        actualEnd = Math.max(0, actualEnd);
        actualEnd = Math.min(codePointCount, actualEnd);

        // Per requirement, if start >= end, return empty string.
        if (actualStart >= actualEnd) {
            return EMPTY_STRING;
        }

        int len = actualEnd - actualStart;
        return substr(str, actualStart, len);
    }

    /**
     * Extracts a substring from a string using start and end byte indices.
     * This method is a wrapper around {@link #substrByBytes(String, int, int, Charset)}.
     * <p>
     * 시작 및 끝 바이트 인덱스를 사용하여 문자열에서 하위 문자열을 추출합니다.
     * 이 메소드는 {@link #substrByBytes(String, int, int, Charset)}를 감싸는 래퍼입니다.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.substringByBytes(null, 0, 1, StandardCharsets.UTF_8)      = ""
     * MbStringUtil.substringByBytes("", 0, 1, StandardCharsets.UTF_8)        = ""
     *
     * // start >= end
     * MbStringUtil.substringByBytes("가나다abc", 2, 2, StandardCharsets.UTF_8)  = ""
     * MbStringUtil.substringByBytes("가나다abc", 2, 1, StandardCharsets.UTF_8) = ""
     *
     * // start is out of bounds
     * MbStringUtil.substringByBytes("가나다abc", 100, 102, StandardCharsets.UTF_8) = ""
     * MbStringUtil.substringByBytes("가나다abc", -100, -98, StandardCharsets.UTF_8) = ""
     *
     * // EUC-KR Examples
     * Charset euckr = Charset.forName("EUC-KR");
     * MbStringUtil.substringByBytes("가나다abc", 0, 2, euckr) = "가"
     * MbStringUtil.substringByBytes("가나다abc", 1, 3, euckr) = "  "
     * MbStringUtil.substringByBytes("가나다abc", 4, 7, euckr) = "다a"
     * MbStringUtil.substringByBytes("가나다abc", 5, 7, euckr) = " a"
     * MbStringUtil.substringByBytes("가나다abc", 0, 3, euckr) = "가 "
     * MbStringUtil.substringByBytes("가나다abc", 1, 4, euckr) = " 나"
     *
     * // UTF-8 Examples
     * MbStringUtil.substringByBytes("가나다abc", 0, 2, StandardCharsets.UTF_8) = "  "
     * MbStringUtil.substringByBytes("가나다abc", 1, 3, StandardCharsets.UTF_8) = "  "
     * MbStringUtil.substringByBytes("가나다abc", 4, 7, StandardCharsets.UTF_8) = "   "
     * MbStringUtil.substringByBytes("가나다abc", 0, 3, StandardCharsets.UTF_8) = "가"
     * MbStringUtil.substringByBytes("가나다abc", 2, 6, StandardCharsets.UTF_8) = " 나"
     * MbStringUtil.substringByBytes("가나다abc", 2, 7, StandardCharsets.UTF_8) = " 나 "
     *
     * // UTF-8 Emoji Examples ("👍a가" is 8 bytes: 4 + 1 + 3)
     * MbStringUtil.substringByBytes("👍a가", 0, 3, StandardCharsets.UTF_8) = "   "
     * MbStringUtil.substringByBytes("👍a가", 0, 4, StandardCharsets.UTF_8) = "👍"
     * MbStringUtil.substringByBytes("👍a가", 0, 5, StandardCharsets.UTF_8) = "👍a"
     * MbStringUtil.substringByBytes("👍a가", 3, 6, StandardCharsets.UTF_8) = " a "
     * MbStringUtil.substringByBytes("👍a가", 4, 8, StandardCharsets.UTF_8) = "a가"
     *
     * // Unencodable character example with EUC-KR
     * MbStringUtil.substringByBytes("a👍가", 0, 5, euckr) = "a 가"
     * </pre>
     *
     * @param str The source string. (원본 문자열)
     * @param start The beginning byte index, inclusive. Negative values are offsets from the end. (시작 바이트 인덱스(포함). 음수 값은 끝에서의 오프셋입니다.)
     * @param end The ending byte index, exclusive. Negative values are offsets from the end. (끝 바이트 인덱스(제외). 음수 값은 끝에서의 오프셋입니다.)
     * @param charset The character set to use. (사용할 문자 집합)
     * @return The specified substring. (지정된 하위 문자열)
     */
    public static String substringByBytes(String str, int start, int end, Charset charset) {
        if (str == null || str.isEmpty()) {
            return EMPTY_STRING;
        }

        // We need the total byte length to handle negative indices correctly.
        int totalBytes = lengthByBytes(str, charset);

        // Calculate effective start index
        int actualStart = (start >= 0) ? start : totalBytes + start;

        // Per requirement, if start is out of bounds, return empty string.
        if (actualStart < 0 || actualStart >= totalBytes) {
            return EMPTY_STRING;
        }
        
        // Calculate effective end index
        int actualEnd = (end >= 0) ? end : totalBytes + end;

        // Per requirement, if end is out of bounds, clamp it to the valid range [0, totalBytes].
        actualEnd = Math.max(0, actualEnd);
        actualEnd = Math.min(totalBytes, actualEnd);

        // Per requirement, if start >= end, return empty string.
        if (actualStart >= actualEnd) {
            return EMPTY_STRING;
        }

        int len = actualEnd - actualStart;
        return substrByBytes(str, actualStart, len, charset);
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
        // 문자열이 null이거나 비어있으면 0을 반환합니다.
        if (str == null || str.isEmpty()) {
            return 0;
        }
        // 문자열의 코드 포인트 수를 반환합니다.
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
        // 문자열이 null이거나 비어있으면 0을 반환합니다.
        if (str == null || str.isEmpty()) {
            return 0;
        }
        // 지정된 문자 집합으로 문자열을 인코딩한 후 바이트 길이를 반환합니다.
        return str.getBytes(charset).length;
    }

    // 문자열 분석 결과를 저장하는 내부 클래스입니다.
    private static class StringMetadata {
        final List<Integer> codePointStartIndices = new ArrayList<>(); // 각 코드 포인트의 시작 char 인덱스
        final List<Integer> codePointByteLengths = new ArrayList<>(); // 각 코드 포인트의 바이트 길이
        final HashMap<Integer, Integer> byteOffsetToCodePointIndexMap = new HashMap<>(); // 바이트 오프셋을 코드 포인트 인덱스로 매핑
        int totalBytes = 0; // 전체 바이트 길이
    }

    // 문자열을 분석하여 메타데이터를 생성하는 private 헬퍼 메소드입니다.
    private static StringMetadata analyzeString(String str, Charset charset) {
        StringMetadata metadata = new StringMetadata();
        CharsetEncoder encoder = charset.newEncoder();

        // 문자열을 순회하며 각 코드 포인트를 처리합니다.
        for (int i = 0; i < str.length(); ) {
            int codePoint = str.codePointAt(i);

            // 현재 바이트 오프셋을 현재 코드 포인트 인덱스에 매핑합니다.
            metadata.byteOffsetToCodePointIndexMap.put(metadata.totalBytes, metadata.codePointStartIndices.size());
            // 현재 코드 포인트의 시작 char 인덱스를 저장합니다.
            metadata.codePointStartIndices.add(i);

            try {
                // 코드 포인트를 문자열로 변환하고 인코딩하여 바이트 길이를 계산합니다.
                String codePointStr = new String(Character.toChars(codePoint));
                ByteBuffer bb = encoder.encode(java.nio.CharBuffer.wrap(codePointStr));
                int byteLength = bb.limit();
                metadata.codePointByteLengths.add(byteLength);
                metadata.totalBytes += byteLength;
            } catch (CharacterCodingException e) {
                // 인코딩할 수 없는 문자인 경우, 특수 값으로 표시하고 플레이스홀더 길이를 더합니다.
                metadata.codePointByteLengths.add(UNENCODABLE_CHAR_LENGTH);
                metadata.totalBytes += PLACEHOLDER_BYTE_LENGTH;
            }
            // 다음 코드 포인트로 이동합니다. (보충 문자의 경우 2 char 이동)
            i += Character.charCount(codePoint);
        }
        return metadata;
    }

    // 분석된 메타데이터를 기반으로 하위 문자열을 구성하는 private 헬퍼 메소드입니다.
    private static String buildSubstring(String originalStr, StringMetadata metadata, int startByte, int endByte) {
        StringBuilder result = new StringBuilder();
        // 지정된 바이트 범위 내에서 순회합니다.
        for (int currentByte = startByte; currentByte < endByte; ) {
            // 현재 바이트 오프셋에 해당하는 코드 포인트 인덱스를 찾습니다.
            Integer codePointIndex = metadata.byteOffsetToCodePointIndexMap.get(currentByte);

            if (codePointIndex != null) {
                // 해당 코드 포인트의 바이트 길이를 가져옵니다.
                int charByteLength = metadata.codePointByteLengths.get(codePointIndex);

                if (charByteLength == UNENCODABLE_CHAR_LENGTH) {
                    // 인코딩 불가능한 문자인 경우, 패딩 문자를 추가하고 플레이스홀더 길이만큼 이동합니다.
                    result.append(PADDING_CHAR);
                    currentByte += PLACEHOLDER_BYTE_LENGTH;
                } else {
                    // 잘리지 않고 문자가 완전히 포함될 수 있는지 확인합니다.
                    if (currentByte + charByteLength <= endByte) {
                        // 문자를 결과에 추가하고 해당 바이트 길이만큼 이동합니다.
                        int charStartIndex = metadata.codePointStartIndices.get(codePointIndex);
                        int codePoint = originalStr.codePointAt(charStartIndex);
                        result.append(Character.toChars(codePoint));
                        currentByte += charByteLength;
                    } else {
                        // 문자가 잘리는 경우, 패딩 문자를 추가하고 1바이트만 이동합니다.
                        result.append(PADDING_CHAR);
                        currentByte++;
                    }
                }
            } else {
                // 현재 바이트 위치에 해당하는 문자가 없는 경우 (멀티바이트 문자의 중간 바이트), 패딩 문자를 추가하고 1바이트 이동합니다.
                result.append(PADDING_CHAR);
                currentByte++;
            }
        }
        return result.toString();
    }
    
    // 시작 위치를 계산하는 private 헬퍼 메소드입니다. (음수 인덱스 처리)
    private static int calculateEffectiveStart(int totalLength, int start) {
        // 시작 위치가 0 이상이면 그대로 반환하고, 음수이면 전체 길이에서 더하여 끝에서의 오프셋으로 계산합니다.
        return start >= 0 ? start : totalLength + start;
    }
}
