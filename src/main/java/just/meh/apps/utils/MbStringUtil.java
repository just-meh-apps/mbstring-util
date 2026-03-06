// Copyright (c) 2026 just.meh.apps@gmail.com
// SPDX-License-Identifier: MIT

package just.meh.apps.utils;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
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

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8; 

    /**
     * Private constructor to prevent instantiation of this utility class.
     * <p>
     * 이 유틸리티 클래스의 인스턴스화를 방지하기 위한 private 생성자입니다.
     */
    private MbStringUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Extracts a substring from the {@code start} code point index to the end of the string.
     * This method is safe for supplementary characters (e.g., emojis).
     * <p>
     * {@code start} 코드 포인트 인덱스부터 문자열 끝까지의 하위 문자열을 추출합니다.
     * 이모티콘과 같은 보충 문자에 안전합니다.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.substr(null, 0)      = ""
     * MbStringUtil.substr("", 0)        = ""
     *
     * // start is positive
     * MbStringUtil.substr("가나다abc", 3)  = "abc"
     *
     * // start is negative
     * MbStringUtil.substr("가나다abc", -2) = "bc"
     *
     * // Emoji examples
     * MbStringUtil.substr("👍a가나", 1)  = "a가나"
     * </pre>
     *
     * @param str The source string. (원본 문자열)
     * @param start The starting code point index (0-based). If negative, it is an offset from the end. (시작 코드 포인트 인덱스 (0부터 시작). 음수일 경우 끝에서의 오프셋입니다.)
     * @return The resulting substring. (결과 하위 문자열)
     * @see #substr(String, int, int)
     */
    public static String substr(String str, int start) {
        return substr(str, start, length(str));
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
     * Extracts a substring from the {@code start} byte offset to the end of the string, using the default UTF-8 charset.
     * <p>
     * 기본 UTF-8 문자 집합을 사용하여 {@code start} 바이트 오프셋부터 문자열 끝까지의 하위 문자열을 추출합니다.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.substrByBytes(null, 0)      = ""
     * MbStringUtil.substrByBytes("", 0)        = ""
     *
     * // "가나다abc" is 12 bytes in UTF-8 (3 * 3 + 3 * 1)
     * MbStringUtil.substrByBytes("가나다abc", 3)  = "나다abc"
     * MbStringUtil.substrByBytes("가나다abc", -5) = "  abc"
     *
     * // "👍a가" is 8 bytes in UTF-8 (4 + 1 + 3)
     * MbStringUtil.substrByBytes("👍a가", 4) = "a가"
     * </pre>
     *
     * @param str The source string. (원본 문자열)
     * @param start The starting byte offset (0-based). If negative, it is an offset from the end. (시작 바이트 오프셋 (0부터 시작). 음수일 경우 끝에서의 오프셋입니다.)
     * @return The resulting substring, padded with spaces if necessary. (결과 하위 문자열, 필요한 경우 공백으로 채워집니다.)
     * @see #substrByBytes(String, int, int, Charset)
     */
    public static String substrByBytes(String str, int start) {
        return substrByBytes(str, start, lengthByBytes(str));
    }

    /**
     * Extracts a substring from a string based on byte length using the default UTF-8 charset.
     * <p>
     * 기본 UTF-8 문자 집합을 사용하여 바이트 길이를 기준으로 문자열에서 하위 문자열을 추출합니다.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.substrByBytes(null, 0, 1)      = ""
     * MbStringUtil.substrByBytes("", 0, 1)        = ""
     *
     * // len is zero or negative
     * MbStringUtil.substrByBytes("가나다abc", 2, 0)  = ""
     * MbStringUtil.substrByBytes("가나다abc", 2, -2) = ""
     *
     * // UTF-8 Examples
     * MbStringUtil.substrByBytes("가나다abc", 0, 3) = "가"
     * MbStringUtil.substrByBytes("가나다abc", 2, 4) = " 나"
     * MbStringUtil.substrByBytes("가나다abc", 2, 5) = " 나 "
     *
     * // UTF-8 Emoji Examples ("👍a가" is 8 bytes: 4 + 1 + 3)
     * MbStringUtil.substrByBytes("👍a가", 0, 4) = "👍"
     * MbStringUtil.substrByBytes("👍a가", 0, 5) = "👍a"
     * </pre>
     *
     * @param str The source string. (원본 문자열)
     * @param start The starting byte offset (0-based). If negative, it is an offset from the end. (시작 바이트 오프셋 (0부터 시작). 음수일 경우 끝에서의 오프셋입니다.)
     * @param len The desired length of the substring in bytes. (바이트 단위의 원하는 하위 문자열 길이)
     * @return The resulting substring, padded with spaces if necessary. (결과 하위 문자열, 필요한 경우 공백으로 채워집니다.)
     * @see #substrByBytes(String, int, int, Charset)
     */
    public static String substrByBytes(String str, int start, int len) {
        return substrByBytes(str, start, len, DEFAULT_CHARSET);
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
     * Extracts a substring from the {@code start} code point index to the end of the string.
     * This method is a wrapper around {@link #substring(String, int, int)}.
     * <p>
     * {@code start} 코드 포인트 인덱스부터 문자열 끝까지의 하위 문자열을 추출합니다.
     * 이 메소드는 {@link #substring(String, int, int)}를 감싸는 래퍼입니다.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.substring(null, 0)      = ""
     * MbStringUtil.substring("", 0)        = ""
     *
     * // start is positive
     * MbStringUtil.substring("가나다abc", 3)  = "abc"
     *
     * // start is negative
     * MbStringUtil.substring("가나다abc", -2) = "bc"
     *
     * // Emoji examples
     * MbStringUtil.substring("👍a가나", 1)  = "a가나"
     * </pre>
     *
     * @param str The source string. (원본 문자열)
     * @param start The beginning code point index, inclusive. Negative values are offsets from the end. (시작 코드 포인트 인덱스(포함). 음수 값은 끝에서의 오프셋입니다.)
     * @return The specified substring. (지정된 하위 문자열)
     * @see #substring(String, int, int)
     */
    public static String substring(String str, int start) {
        return substring(str, start, length(str));
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

        // 유효 시작 인덱스 계산
        int actualStart = (start >= 0) ? start : codePointCount + start;

        // 시작 인덱스가 범위를 벗어나면 빈 문자열을 반환합니다.
        if (actualStart < 0 || actualStart >= codePointCount) {
            return EMPTY_STRING;
        }

        // 유효 끝 인덱스 계산
        int actualEnd = (end >= 0) ? end : codePointCount + end;
        
        // 끝 인덱스가 범위를 벗어나면 유효한 범위 [0, codePointCount]로 조정합니다.
        actualEnd = Math.max(0, actualEnd);
        actualEnd = Math.min(codePointCount, actualEnd);

        // start >= end 이면 빈 문자열을 반환합니다.
        if (actualStart >= actualEnd) {
            return EMPTY_STRING;
        }

        int len = actualEnd - actualStart;
        return substr(str, actualStart, len);
    }
    
    /**
     * Extracts a substring from the {@code start} byte offset to the end of the string, using the default UTF-8 charset.
     * <p>
     * 기본 UTF-8 문자 집합을 사용하여 {@code start} 바이트 오프셋부터 문자열 끝까지의 하위 문자열을 추출합니다.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.substringByBytes(null, 0)      = ""
     * MbStringUtil.substringByBytes("", 0)        = ""
     *
     * // "가나다abc" is 12 bytes in UTF-8
     * MbStringUtil.substringByBytes("가나다abc", 3)  = "나다abc"
     * MbStringUtil.substringByBytes("가나다abc", -5) = "  abc"
     *
     * // "👍a가" is 8 bytes in UTF-8
     * MbStringUtil.substringByBytes("👍a가", 4) = "a가"
     * </pre>
     *
     * @param str The source string. (원본 문자열)
     * @param start The beginning byte index, inclusive. Negative values are offsets from the end. (시작 바이트 인덱스(포함). 음수 값은 끝에서의 오프셋입니다.)
     * @return The specified substring. (지정된 하위 문자열)
     * @see #substringByBytes(String, int, int, Charset)
     */
    public static String substringByBytes(String str, int start) {
        return substringByBytes(str, start, lengthByBytes(str));        
    }

    /**
     * Extracts a substring from a string using start and end byte indices, with the default UTF-8 charset.
     * <p>
     * 기본 UTF-8 문자 집합을 사용하여 시작 및 끝 바이트 인덱스로 문자열에서 하위 문자열을 추출합니다.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.substringByBytes(null, 0, 1)      = ""
     * MbStringUtil.substringByBytes("", 0, 1)        = ""
     *
     * // start >= end
     * MbStringUtil.substringByBytes("가나다abc", 2, 2)  = ""
     * MbStringUtil.substringByBytes("가나다abc", 2, 1) = ""
     *
     * // UTF-8 Examples
     * MbStringUtil.substringByBytes("가나다abc", 0, 3) = "가"
     * MbStringUtil.substringByBytes("가나다abc", 2, 6) = " 나"
     * MbStringUtil.substringByBytes("가나다abc", 2, 7) = " 나 "
     *
     * // UTF-8 Emoji Examples ("👍a가" is 8 bytes: 4 + 1 + 3)
     * MbStringUtil.substringByBytes("👍a가", 0, 4) = "👍"
     * MbStringUtil.substringByBytes("👍a가", 0, 5) = "👍a"
     * </pre>
     *
     * @param str The source string. (원본 문자열)
     * @param start The beginning byte index, inclusive. Negative values are offsets from the end. (시작 바이트 인덱스(포함). 음수 값은 끝에서의 오프셋입니다.)
     * @param end The ending byte index, exclusive. Negative values are offsets from the end. (끝 바이트 인덱스(제외). 음수 값은 끝에서의 오프셋입니다.)
     * @return The specified substring. (지정된 하위 문자열)
     * @see #substringByBytes(String, int, int, Charset)
     */
    public static String substringByBytes(String str, int start, int end) {
        return substringByBytes(str, start, end, DEFAULT_CHARSET);
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

        // 음수 인덱스를 올바르게 처리하려면 전체 바이트 길이가 필요합니다.
        int totalBytes = lengthByBytes(str, charset);

        // 유효 시작 인덱스 계산
        int actualStart = (start >= 0) ? start : totalBytes + start;

        // 시작 인덱스가 범위를 벗어나면 빈 문자열을 반환합니다.
        if (actualStart < 0 || actualStart >= totalBytes) {
            return EMPTY_STRING;
        }
        
        // 유효 끝 인덱스 계산
        int actualEnd = (end >= 0) ? end : totalBytes + end;

        // 끝 인덱스가 범위를 벗어나면 유효한 범위 [0, totalBytes]로 조정합니다.
        actualEnd = Math.max(0, actualEnd);
        actualEnd = Math.min(totalBytes, actualEnd);

        // start >= end 이면 빈 문자열을 반환합니다.
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
     * Returns the byte length of a string using the default UTF-8 charset.
     * <p>
     * 기본 UTF-8 문자 집합을 사용하여 문자열의 바이트 길이를 반환합니다.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.lengthByBytes(null)      = 0
     * MbStringUtil.lengthByBytes("")        = 0
     *
     * // UTF-8 Examples
     * MbStringUtil.lengthByBytes("abc")   = 3
     * MbStringUtil.lengthByBytes("가나다") = 9
     * MbStringUtil.lengthByBytes("👍a가") = 8
     * </pre>
     *
     * @param str The string to measure. (측정할 문자열)
     * @return The length of the string in bytes. (문자열의 바이트 단위 길이)
     * @see #lengthByBytes(String, Charset)
     */
    public static int lengthByBytes(String str) {
        return lengthByBytes(str, DEFAULT_CHARSET);
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

    /**
     * Left pads a string with spaces to a certain length.
     * <p>
     * 지정된 길이만큼 문자열 왼쪽에 공백을 채웁니다.
     *
     * <pre>
     * MbStringUtil.leftPad(null, 5)     = "     "
     * MbStringUtil.leftPad("", 5)       = "     "
     * MbStringUtil.leftPad("한글", 5) = "   한글"
     * MbStringUtil.leftPad("한글", 2) = "한글"
     * MbStringUtil.leftPad("한글", -1) = "한글"
     * </pre>
     *
     * @param str    The string to pad. (패딩할 문자열)
     * @param padLen The total length to pad to. (패딩할 총 길이)
     * @return The padded string. (패딩된 문자열)
     * @see #leftPad(String, int, String)
     */
    public static String leftPad(String str, int padLen) {
        return leftPad(str, padLen, PADDING_CHAR);
    }

    /**
     * Left pads a string with a specified character to a certain length.
     * <p>
     * 지정된 길이만큼 문자열 왼쪽에 특정 문자를 채웁니다.
     *
     * <pre>
     * MbStringUtil.leftPad(null, 5, '#')     = "#####"
     * MbStringUtil.leftPad("", 5, '#')       = "#####"
     * MbStringUtil.leftPad("한글", 5, '#') = "###한글"
     * MbStringUtil.leftPad("한글", 2, '#') = "한글"
     * MbStringUtil.leftPad("한글", -1, '#') = "한글"
     * </pre>
     *
     * @param str     The string to pad. (패딩할 문자열)
     * @param padLen  The total length to pad to. (패딩할 총 길이)
     * @param padChar The character to pad with. (패딩에 사용할 문자)
     * @return The padded string. (패딩된 문자열)
     * @see #leftPad(String, int, String)
     */
    public static String leftPad(String str, int padLen, char padChar) {
        return leftPad(str, padLen, String.valueOf(padChar));
    }

    /**
     * Left pads a string with a specified string to a certain length.
     * Padding is based on character (code point) count.
     * <p>
     * 지정된 문자열로 문자열을 특정 길이까지 왼쪽으로 채웁니다.
     * 패딩은 문자(코드 포인트) 수를 기준으로 합니다.
     *
     * <pre>
     * MbStringUtil.leftPad("한글", 7, "ㅎㄹㄹ")  = "ㅎㄹㄹㅎㄹ한글"
     * MbStringUtil.leftPad("한글", 5, "ㅎㄹㄹ")  = "ㅎㄹㄹ한글"
     * MbStringUtil.leftPad("한글", 4, "ㅎㄹㄹ")  = "ㅎㄹ한글"
     * MbStringUtil.leftPad("한글", 2, "ㅎㄹㄹ")  = "한글"
     * MbStringUtil.leftPad("한글", -1, "ㅎㄹㄹ") = "한글"
     * MbStringUtil.leftPad("한글", 5, null)     = "   한글"
     * </pre>
     *
     * @param str    The string to pad. (패딩할 문자열)
     * @param padLen The total length to pad to. (패딩할 총 길이)
     * @param padStr The string to pad with. If null or empty, a space is used. (패딩에 사용할 문자열. null이거나 비어 있으면 공백이 사용됩니다.)
     * @return The padded string. (패딩된 문자열)
     */
    public static String leftPad(String str, int padLen, String padStr) {
        return pad(str, padLen, padStr, true);
    }

    /**
     * Right pads a string with spaces to a certain length.
     * <p>
     * 지정된 길이만큼 문자열 오른쪽에 공백을 채웁니다.
     *
     * <pre>
     * MbStringUtil.rightPad(null, 5)     = "     "
     * MbStringUtil.rightPad("", 5)      = "     "
     * MbStringUtil.rightPad("한글", 5) = "한글   "
     * MbStringUtil.rightPad("한글", 2) = "한글"
     * MbStringUtil.rightPad("한글", -1) = "한글"
     * </pre>
     *
     * @param str    The string to pad. (패딩할 문자열)
     * @param padLen The total length to pad to. (패딩할 총 길이)
     * @return The padded string. (패딩된 문자열)
     * @see #rightPad(String, int, String)
     */
    public static String rightPad(String str, int padLen) {
        return rightPad(str, padLen, PADDING_CHAR);
    }

    /**
     * Right pads a string with a specified character to a certain length.
     * <p>
     * 지정된 길이만큼 문자열 오른쪽에 특정 문자를 채웁니다.
     *
     * <pre>
     * MbStringUtil.rightPad(null, 5, '#')     = "#####"
     * MbStringUtil.rightPad("", 5, '#')      = "#####"
     * MbStringUtil.rightPad("한글", 5, '#') = "한글###"
     * MbStringUtil.rightPad("한글", 2, '#') = "한글"
     * MbStringUtil.rightPad("한글", -1, '#') = "한글"
     * </pre>
     *
     * @param str     The string to pad. (패딩할 문자열)
     * @param padLen  The total length to pad to. (패딩할 총 길이)
     * @param padChar The character to pad with. (패딩에 사용할 문자)
     * @return The padded string. (패딩된 문자열)
     * @see #rightPad(String, int, String)
     */
    public static String rightPad(String str, int padLen, char padChar) {
        return rightPad(str, padLen, String.valueOf(padChar));
    }
    
    /**
     * Right pads a string with a specified string to a certain length.
     * Padding is based on character (code point) count.
     * <p>
     * 지정된 문자열로 문자열을 특정 길이까지 오른쪽으로 채웁니다.
     * 패딩은 문자(코드 포인트) 수를 기준으로 합니다.
     *
     * <pre>
     * MbStringUtil.rightPad("한글", 7, "ㅎㄹㄹ")  = "한글ㅎㄹㄹㅎㄹ"
     * MbStringUtil.rightPad("한글", 5, "ㅎㄹㄹ")  = "한글ㅎㄹㄹ"
     * MbStringUtil.rightPad("한글", 4, "ㅎㄹㄹ")  = "한글ㅎㄹ"
     * MbStringUtil.rightPad("한글", 2, "ㅎㄹㄹ")  = "한글"
     * MbStringUtil.rightPad("한글", -1, "ㅎㄹㄹ") = "한글"
     * MbStringUtil.rightPad("한글", 5, null)     = "한글   "
     * </pre>
     *
     * @param str    The string to pad. (패딩할 문자열)
     * @param padLen The total length to pad to. (패딩할 총 길이)
     * @param padStr The string to pad with. If null or empty, a space is used. (패딩에 사용할 문자열. null이거나 비어 있으면 공백이 사용됩니다.)
     * @return The padded string. (패딩된 문자열)
     */
    public static String rightPad(String str, int padLen, String padStr) {
        return pad(str, padLen, padStr, false);
    }

    /**
     * Left pads a string with spaces to a certain byte length using the default UTF-8 charset.
     * <p>
     * 기본 UTF-8 문자 집합을 사용하여 지정된 바이트 길이만큼 문자열 왼쪽에 공백을 채웁니다.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.leftPadByBytes(null, 5)     = "     "
     * MbStringUtil.leftPadByBytes("", 5)       = "     "
     *
     * // '한글' is 6 bytes in UTF-8
     * MbStringUtil.leftPadByBytes("한글", 9) = "   한글"
     * MbStringUtil.leftPadByBytes("한글", 6) = "한글"
     * MbStringUtil.leftPadByBytes("한글", -1) = "한글"
     * </pre>
     *
     * @param str    The string to pad. (패딩할 문자열)
     * @param padLen The total byte length to pad to. (패딩할 총 바이트 길이)
     * @return The padded string. (패딩된 문자열)
     * @see #leftPadByBytes(String, int, String, Charset)
     */
    public static String leftPadByBytes(String str, int padLen) {
        return leftPadByBytes(str, padLen, PADDING_CHAR);
    }

    /**
     * Left pads a string with a specified character to a certain byte length using the default UTF-8 charset.
     * <p>
     * 기본 UTF-8 문자 집합을 사용하여 지정된 바이트 길이만큼 문자열 왼쪽에 특정 문자를 채웁니다.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.leftPadByBytes(null, 5, '#')     = "#####"
     * MbStringUtil.leftPadByBytes("", 5, '#')       = "#####"
     * 
     * // '한글' is 6 bytes in UTF-8, '#' is 1 byte
     * MbStringUtil.leftPadByBytes("한글", 9, '#') = "###한글"
     * MbStringUtil.leftPadByBytes("한글", 6, '#') = "한글"
     * MbStringUtil.leftPadByBytes("한글", -1, '#') = "한글"
     * </pre>
     *
     * @param str     The string to pad. (패딩할 문자열)
     * @param padLen  The total byte length to pad to. (패딩할 총 바이트 길이)
     * @param padChar The character to pad with. (패딩에 사용할 문자)
     * @return The padded string. (패딩된 문자열)
     * @see #leftPadByBytes(String, int, String, Charset)
     */
    public static String leftPadByBytes(String str, int padLen, char padChar) {
        return leftPadByBytes(str, padLen, String.valueOf(padChar));
    }

    /**
     * Left pads a string with a specified string to a certain byte length using the default UTF-8 charset.
     * <p>
     * 기본 UTF-8 문자 집합을 사용하여 지정된 바이트 길이만큼 문자열 왼쪽에 특정 문자열을 채웁니다.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.leftPadByBytes(null, 8, "ㅎㄹ")      = "ㅎㄹ  "
     * MbStringUtil.leftPadByBytes("", 8, "ㅎㄹ")        = "ㅎㄹ  "
     * 
     * // Using UTF-8 where '한' is 3 bytes, 'ㅎ' is 3 bytes.
     * MbStringUtil.leftPadByBytes("한글", 12, "ㅎㄹㄹ") = "ㅎㄹ한글"
     * MbStringUtil.leftPadByBytes("한글", 11, "ㅎㄹㄹ") = "ㅎ  한글"
     * MbStringUtil.leftPadByBytes("한글", 6, "ㅎㄹㄹ")  = "한글"
     * MbStringUtil.leftPadByBytes("한글", -1, "ㅎㄹㄹ") = "한글"
     * </pre>
     *
     * @param str     The string to pad. (패딩할 문자열)
     * @param padLen  The total byte length to pad to. (패딩할 총 바이트 길이)
     * @param padStr  The string to pad with. (패딩에 사용할 문자열)
     * @return The padded string. (패딩된 문자열)
     * @see #leftPadByBytes(String, int, String, Charset)
     */
    public static String leftPadByBytes(String str, int padLen, String padStr) {
        return leftPadByBytes(str, padLen, padStr, DEFAULT_CHARSET);
    }

    /**
     * Left pads a string with a specified string to a certain byte length.
     * Padding is based on byte length for a given charset.
     * <p>
     * 지정된 문자열로 문자열을 특정 바이트 길이까지 왼쪽으로 채웁니다.
     * 패딩은 주어진 문자 집합의 바이트 길이를 기준으로 합니다.
     *
     * <pre>
     * // Using StandardCharsets.UTF_8 where '한' is 3 bytes, 'ㅎ' is 3 bytes.
     * MbStringUtil.leftPadByBytes("한글", 15, "ㅎㄹㄹ", StandardCharsets.UTF_8) = "ㅎㄹㄹ한글"
     * MbStringUtil.leftPadByBytes("한글", 12, "ㅎㄹㄹ", StandardCharsets.UTF_8) = "ㅎㄹ한글"
     * MbStringUtil.leftPadByBytes("한글", 11, "ㅎㄹㄹ", StandardCharsets.UTF_8) = "ㅎ  한글"
     * MbStringUtil.leftPadByBytes("한글", 6, "ㅎㄹㄹ", StandardCharsets.UTF_8)  = "한글"
     * MbStringUtil.leftPadByBytes("한글", -1, "ㅎㄹㄹ", StandardCharsets.UTF_8) = "한글"
     * MbStringUtil.leftPadByBytes("한글", 9, null, StandardCharsets.UTF_8)    = "   한글"
     * </pre>
     *
     * @param str     The string to pad. (패딩할 문자열)
     * @param padLen  The total byte length to pad to. (패딩할 총 바이트 길이)
     * @param padStr  The string to pad with. If null or empty, a space is used. (패딩에 사용할 문자열. null이거나 비어 있으면 공백이 사용됩니다.)
     * @param charset The charset to use for byte calculations. (바이트 계산에 사용할 문자 집합)
     * @return The padded string. (패딩된 문자열)
     */
    public static String leftPadByBytes(String str, int padLen, String padStr, Charset charset) {
        return padByBytes(str, padLen, padStr, charset, true);
    }

    /**
     * Right pads a string with spaces to a certain byte length using the default UTF-8 charset.
     * <p>
     * 기본 UTF-8 문자 집합을 사용하여 지정된 바이트 길이만큼 문자열 오른쪽에 공백을 채웁니다.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.rightPadByBytes(null, 5)     = "     "
     * MbStringUtil.rightPadByBytes("", 5)       = "     "
     *
     * // '한글' is 6 bytes in UTF-8
     * MbStringUtil.rightPadByBytes("한글", 9) = "한글   "
     * MbStringUtil.rightPadByBytes("한글", 6) = "한글"
     * MbStringUtil.rightPadByBytes("한글", -1) = "한글"
     * </pre>
     *
     * @param str    The string to pad. (패딩할 문자열)
     * @param padLen The total byte length to pad to. (패딩할 총 바이트 길이)
     * @return The padded string. (패딩된 문자열)
     * @see #rightPadByBytes(String, int, String, Charset)
     */
    public static String rightPadByBytes(String str, int padLen) {
        return rightPadByBytes(str, padLen, PADDING_CHAR);
    }

    /**
     * Right pads a string with a specified character to a certain byte length using the default UTF-8 charset.
     * <p>
     * 기본 UTF-8 문자 집합을 사용하여 지정된 바이트 길이만큼 문자열 오른쪽에 특정 문자를 채웁니다.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.rightPadByBytes(null, 5, '#')     = "#####"
     * MbStringUtil.rightPadByBytes("", 5, '#')       = "#####"
     * 
     * // '한글' is 6 bytes in UTF-8, '#' is 1 byte
     * MbStringUtil.rightPadByBytes("한글", 9, '#') = "한글###"
     * MbStringUtil.rightPadByBytes("한글", 6, '#') = "한글"
     * MbStringUtil.rightPadByBytes("한글", -1, '#') = "한글"
     * </pre>
     *
     * @param str     The string to pad. (패딩할 문자열)
     * @param padLen  The total byte length to pad to. (패딩할 총 바이트 길이)
     * @param padChar The character to pad with. (패딩에 사용할 문자)
     * @return The padded string. (패딩된 문자열)
     * @see #rightPadByBytes(String, int, String, Charset)
     */
    public static String rightPadByBytes(String str, int padLen, char padChar) {
        return rightPadByBytes(str, padLen, String.valueOf(padChar));
    }

    /**
     * Right pads a string with a specified string to a certain byte length using the default UTF-8 charset.
     * <p>
     * 기본 UTF-8 문자 집합을 사용하여 지정된 바이트 길이만큼 문자열 오른쪽에 특정 문자열을 채웁니다.
     *
     * <pre>
     * // str is null or empty
     * MbStringUtil.rightPadByBytes(null, 8, "ㅎㄹ")      = "ㅎㄹ  "
     * MbStringUtil.rightPadByBytes("", 8, "ㅎㄹ")        = "ㅎㄹ  "
     *
     * // Using UTF-8 where '한' is 3 bytes, 'ㅎ' is 3 bytes.
     * MbStringUtil.rightPadByBytes("한글", 12, "ㅎㄹㄹ") = "한글ㅎㄹ"
     * MbStringUtil.rightPadByBytes("한글", 11, "ㅎㄹㄹ") = "한글ㅎ  "
     * MbStringUtil.rightPadByBytes("한글", 6, "ㅎㄹㄹ")  = "한글"
     * MbStringUtil.rightPadByBytes("한글", -1, "ㅎㄹㄹ") = "한글"
     * </pre>
     *
     * @param str     The string to pad. (패딩할 문자열)
     * @param padLen  The total byte length to pad to. (패딩할 총 바이트 길이)
     * @param padStr  The string to pad with. (패딩에 사용할 문자열)
     * @return The padded string. (패딩된 문자열)
     * @see #rightPadByBytes(String, int, String, Charset)
     */
    public static String rightPadByBytes(String str, int padLen, String padStr) {
        return rightPadByBytes(str, padLen, padStr, DEFAULT_CHARSET);
    }
    
    /**
     * Right pads a string with a specified string to a certain byte length.
     * Padding is based on byte length for a given charset.
     * <p>
     * 지정된 문자열로 문자열을 특정 바이트 길이까지 오른쪽으로 채웁니다.
     * 패딩은 주어진 문자 집합의 바이트 길이를 기준으로 합니다.
     *
     * <pre>
     * // Using StandardCharsets.UTF_8 where '한' is 3 bytes, 'ㅎ' is 3 bytes.
     * MbStringUtil.rightPadByBytes("한글", 15, "ㅎㄹㄹ", StandardCharsets.UTF_8) = "한글ㅎㄹㄹ"
     * MbStringUtil.rightPadByBytes("한글", 12, "ㅎㄹㄹ", StandardCharsets.UTF_8) = "한글ㅎㄹ"
     * MbStringUtil.rightPadByBytes("한글", 11, "ㅎㄹㄹ", StandardCharsets.UTF_8) = "한글ㅎ  "
     * MbStringUtil.rightPadByBytes("한글", 6, "ㅎㄹㄹ", StandardCharsets.UTF_8)  = "한글"
     * MbStringUtil.rightPadByBytes("한글", -1, "ㅎㄹㄹ", StandardCharsets.UTF_8) = "한글"
     * MbStringUtil.rightPadByBytes("한글", 9, null, StandardCharsets.UTF_8)    = "한글   "
     * </pre>
     *
     * @param str     The string to pad. (패딩할 문자열)
     * @param padLen  The total byte length to pad to. (패딩할 총 바이트 길이)
     * @param padStr  The string to pad with. If null or empty, a space is used. (패딩에 사용할 문자열. null이거나 비어 있으면 공백이 사용됩니다.)
     * @param charset The charset to use for byte calculations. (바이트 계산에 사용할 문자 집합)
     * @return The padded string. (패딩된 문자열)
     */
    public static String rightPadByBytes(String str, int padLen, String padStr, Charset charset) {
        return padByBytes(str, padLen, padStr, charset, false);
    }

    private static String pad(String str, int totalLen, String padStr, boolean isLeft) {
        if (str == null) str = EMPTY_STRING;
        
        // 코드 포인트 단위로 문자열 길이를 계산합니다.
        int strLen = length(str);
        
        // 목표 길이가 현재 문자열 길이보다 작거나 같으면 원본 문자열을 그대로 반환합니다.
        if (totalLen <= strLen) {
            return str;
        }

        // 패딩 문자열이 null이거나 비어 있으면 공백 문자를 기본값으로 사용합니다.
        String effectivePadStr = (padStr == null || padStr.isEmpty()) ? String.valueOf(PADDING_CHAR) : padStr;
        int padStrLen = length(effectivePadStr);

        // 채워야 할 총 길이를 계산합니다.
        int paddingNeeded = totalLen - strLen;
        StringBuilder padding = new StringBuilder();

        // 패딩 문자열을 반복해서 추가합니다.
        int repeats = paddingNeeded / padStrLen;
        for (int i = 0; i < repeats; i++) {
            padding.append(effectivePadStr);
        }
        
        // 남은 길이만큼 패딩 문자열의 일부를 잘라 추가합니다.
        int remaining = paddingNeeded % padStrLen;
        if (remaining > 0) {
            padding.append(substr(effectivePadStr, 0, remaining));
        }

        // isLeft 플래그에 따라 패딩을 왼쪽에 추가할지 오른쪽에 추가할지 결정합니다.
        if (isLeft) {
            return padding.toString() + str;
        } else {
            return str + padding.toString();
        }
    }

    private static String padByBytes(String str, int totalLen, String padStr, Charset charset, boolean isLeft) {
        if (str == null) str = EMPTY_STRING;
        
        // 바이트 단위로 문자열 길이를 계산합니다.
        int strBytes = lengthByBytes(str, charset);
        
        // 목표 바이트 길이가 현재 문자열 바이트 길이보다 작거나 같으면 원본 문자열을 그대로 반환합니다.
        if (totalLen <= strBytes) {
            return str;
        }

        // 패딩 문자열이 null이거나 비어 있으면 공백 문자를 기본값으로 사용합니다.
        String effectivePadStr = (padStr == null || padStr.isEmpty()) ? String.valueOf(PADDING_CHAR) : padStr;
        int padStrBytes = lengthByBytes(effectivePadStr, charset);
        
        // 패딩 문자열의 바이트 길이가 0인 경우(일반적인 상황은 아님) 0으로 나누는 오류를 방지합니다.
        // 이 경우 공백을 기본 패딩 문자로 다시 시도합니다.
        if (padStrBytes == 0) {
            effectivePadStr = String.valueOf(PADDING_CHAR);
            padStrBytes = lengthByBytes(effectivePadStr, charset);
            
            // 공백조차 바이트 길이가 0이라면 패딩이 불가능하므로 원본 문자열을 반환합니다.
            if (padStrBytes == 0) {
                return str;
            }
        }

        // 채워야 할 총 바이트 수를 계산합니다.
        int bytesToPad = totalLen - strBytes;
        StringBuilder padding = new StringBuilder();

        // 패딩 문자열을 반복해서 추가합니다.
        int repeats = bytesToPad / padStrBytes;
        for (int i = 0; i < repeats; i++) {
            padding.append(effectivePadStr);
        }
        
        // 남은 바이트만큼 패딩 문자열의 일부를 잘라 추가합니다.
        int remainingBytes = bytesToPad % padStrBytes;
        if (remainingBytes > 0) {
            padding.append(substrByBytes(effectivePadStr, 0, remainingBytes, charset));
        }

        // isLeft 플래그에 따라 패딩을 왼쪽에 추가할지 오른쪽에 추가할지 결정합니다.
        if (isLeft) {
            return padding.toString() + str;
        } else {
            return str + padding.toString();
        }
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
