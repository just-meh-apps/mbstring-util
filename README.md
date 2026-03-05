# MbStringUtil - A Multi-byte String Utility

## Overview

`MbStringUtil` is a utility class for safely handling multi-byte encoded strings (such as UTF-8, EUC-KR, etc.) in Java.

This library provides functionality to extract substrings based on character length and byte length. It is particularly useful for handling multi-byte characters, preventing them from being broken by replacing truncated parts with spaces.

## Key Features

- **`substr(String, int, int)`**: Extracts a substring based on the number of characters.
- **`substrByBytes(String, int, int, Charset)`**: Extracts a substring based on byte length, preventing character corruption.
- **Negative Offsets**: Supports negative indexing to calculate positions from the end of the string.
- **Multi-encoding Support**: Can be used with any charset supported by Java, such as UTF-8 and EUC-KR.

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

## Building

To build the project and run tests, use the following Maven command:

```shell
mvn clean install
```
