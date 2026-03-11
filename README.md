[한국어](./README-ko.md)

# MbStringUtil - A Multi-byte String Utility

## Overview

`MbStringUtil` is a utility class for safely handling multi-byte encoded strings (such as UTF-8, EUC-KR, etc.) in Java.

This library provides functionality to extract substrings and pad strings based on both character (code point) and byte lengths. It is particularly useful for handling multi-byte characters, preventing them from being broken by replacing truncated parts with spaces.

## Key Features

- **Substring Extraction**:
    - `substr(String, int, int)`: Extracts a substring based on the number of characters.
    - `substrByBytes(String, int, int, Charset)`: Extracts a substring based on byte length, preventing character corruption.
    - `substring(String, int, int)`: An intuitive, index-based version of `substr`.
    - `substringByBytes(String, int, int, Charset)`: An intuitive, index-based version of `substrByBytes`.
- **String Padding**:
    - `leftPad(String, int, String)` and `rightPad(String, int, String)`: Pads a string to a specified character length.
    - `leftPadByBytes(String, int, String, Charset)` and `rightPadByBytes(String, int, String, Charset)`: Pads a string to a specified byte length.
- **Length Calculation**:
    - `length(String)`: Returns the number of characters (code points) in a string.
    - `lengthByBytes(String, Charset)`: Returns the byte length of a string for a given charset.
- **Safe Handling of Multi-byte Characters**: Prevents characters from being broken and handles unencodable characters gracefully.
- **Negative Offsets**: Supports negative indexing to calculate positions from the end of the string.
- **Multi-encoding Support**: Can be used with any charset supported by Java, such as UTF-8 and EUC-KR.

## Requirements

This project is built using Java 8. To build and use this library, you will need:

- Java 8 or higher

## API Usage

---

### `substr`

Extracts a substring based on character count.

```java
// str is null or empty
MbStringUtil.substr(null, 0, 1)      // returns ""
MbStringUtil.substr("", 0, 1)        // returns ""

// start is positive
MbStringUtil.substr("가나다abc", 0, 2) // returns "가나"
MbStringUtil.substr("가나다abc", 3, 2) // returns "ab"

// start is negative
MbStringUtil.substr("가나다abc", -5, 2) // returns "나다"
MbStringUtil.substr("가나다abc", -2, 2) // returns "bc"
```

---

### `substrByBytes`

Extracts a substring based on byte length. It safely replaces parts of truncated multi-byte characters with spaces. Additionally, characters that cannot be encoded in the specified charset (e.g., an emoji in EUC-KR) are also replaced with a single space.

```java
Charset euckr = Charset.forName("EUC-KR");

// EUC-KR Examples
MbStringUtil.substrByBytes("가나다abc", 0, 2, euckr) // returns "가"
MbStringUtil.substrByBytes("가나다abc", 1, 2, euckr) // returns "  "
MbStringUtil.substrByBytes("가나다abc", 4, 3, euckr) // returns "다a"
MbStringUtil.substrByBytes("가나다abc", 5, 2, euckr) // returns " a"

// Unencodable character example
MbStringUtil.substrByBytes("a👍가", 0, 4, euckr)      // returns "a 가"

// UTF-8 Examples
MbStringUtil.substrByBytes("가나다abc", 0, 3, StandardCharsets.UTF_8) // returns "가"
MbStringUtil.substrByBytes("가나다abc", 2, 4, StandardCharsets.UTF_8) // returns " 나"
MbStringUtil.substrByBytes("가나다abc", 2, 5, StandardCharsets.UTF_8) // returns " 나 "
```

---

### Other Methods

- **`substring(String, int, int)`** and **`substringByBytes(String, int, int, Charset)`**
  - These are variants of `substr` and `substrByBytes` that use start and end indices instead of a length.

- **`leftPad(String, int, String)`** and **`rightPad(String, int, String)`**
  - Pads a string to a specified character length.

- **`leftPadByBytes(String, int, String, Charset)`** and **`rightPadByBytes(String, int, String, Charset)`**
  - Pads a string to a specified byte length.

---

### `length` and `lengthByBytes`

Calculates the length of a string in characters (code points) or bytes. Returns 0 for null or empty strings.

This `length` method may produce a different result from `String.length()`. This method counts the actual number of characters (code points), while `String.length()` counts the number of 16-bit `char` units. For example, a supplementary character like an emoji ("👍") is treated as a single character here, but as two `char`s by `String.length()`.

```java
// length
MbStringUtil.length(null)       // returns 0
MbStringUtil.length("👍a가")    // returns 3
// Note: "👍a가".length() would return 4

// lengthByBytes
Charset euckr = Charset.forName("EUC-KR");
MbStringUtil.lengthByBytes("가나다", euckr) // returns 6
MbStringUtil.lengthByBytes("👍a가", StandardCharsets.UTF_8) // returns 8
```

## Building

To build the project and run tests, use the following Maven command:

```shell
mvn clean install
```
