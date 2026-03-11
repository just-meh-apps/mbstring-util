[English](./README.md)

# MbStringUtil - 다중 바이트 문자열 유틸리티

## 개요

`MbStringUtil`은 Java에서 UTF-8, EUC-KR 등과 같은 다중 바이트 인코딩 문자열을 안전하게 다루기 위한 유틸리티 클래스입니다.

이 라이브러리는 문자(코드 포인트) 및 바이트 길이를 기준으로 서브스트링을 추출하고 문자열을 패딩하는 기능을 제공합니다. 특히 바이트 단위로 문자열을 다룰 때 다중 바이트 문자가 깨지는 것을 방지하기 위해, 잘리는 부분은 공백으로 안전하게 치환하는 기능을 포함합니다.

## 주요 기능

- **서브스트링 추출**:
    - `substr(String, int, int)`: 문자 수를 기준으로 서브스트링을 추출합니다.
    - `substrByBytes(String, int, int, Charset)`: 바이트 길이를 기준으로 서브스트링을 추출하며, 문자 깨짐을 방지합니다.
    - `substring(String, int, int)`: `substr`의 직관적인, 인덱스 기반 버전입니다.
    - `substringByBytes(String, int, int, Charset)`: `substrByBytes`의 직관적인, 인덱스 기반 버전입니다.
- **문자열 패딩**:
    - `leftPad(String, int, String)` 및 `rightPad(String, int, String)`: 지정된 문자 길이에 맞게 문자열을 패딩합니다.
    - `leftPadByBytes(String, int, String, Charset)` 및 `rightPadByBytes(String, int, String, Charset)`: 지정된 바이트 길이에 맞게 문자열을 패딩합니다.
- **길이 계산**:
    - `length(String)`: 문자열의 문자(코드 포인트) 개수를 반환합니다.
    - `lengthByBytes(String, Charset)`: 주어진 문자셋에 대한 문자열의 바이트 길이를 반환합니다.
- **안전한 다중 바이트 문자 처리**: 문자 깨짐을 방지하고 인코딩 불가능한 문자를 정상적으로 처리합니다.
- **음수 오프셋**: 문자열의 끝에서부터 위치를 계산하는 음수 인덱싱을 지원합니다.
- **다양한 인코딩 지원**: UTF-8, EUC-KR 등 Java에서 지원하는 모든 문자셋을 사용할 수 있습니다.

## 요구사항

이 프로젝트는 Java 8을 사용하여 빌드되었습니다. 이 라이브러리를 빌드하고 사용하려면 다음이 필요합니다.

- Java 8 이상

## 사용법 (API)

---

### `substr`

문자 수를 기준으로 서브스트링을 추출합니다.

```java
// str이 null이거나 비어있을 경우
MbStringUtil.substr(null, 0, 1)      // 반환값: ""
MbStringUtil.substr("", 0, 1)        // 반환값: ""

// start가 양수일 경우
MbStringUtil.substr("가나다abc", 0, 2) // 반환값: "가나"
MbStringUtil.substr("가나다abc", 3, 2) // 반환값: "ab"

// start가 음수일 경우
MbStringUtil.substr("가나다abc", -5, 2) // 반환값: "나다"
MbStringUtil.substr("가나다abc", -2, 2) // 반환값: "bc"
```

---

### `substrByBytes`

바이트 길이를 기준으로 서브스트링을 추출합니다. 다중 바이트 문자가 잘릴 경우 안전하게 공백으로 치환됩니다. 또한, 명시된 문자셋으로 인코딩할 수 없는 문자(예: EUC-KR 인코딩에서의 이모지) 역시 하나의 공백으로 치환됩니다.

```java
Charset euckr = Charset.forName("EUC-KR");

// EUC-KR 예시
MbStringUtil.substrByBytes("가나다abc", 0, 2, euckr) // 반환값: "가"
MbStringUtil.substrByBytes("가나다abc", 1, 2, euckr) // 반환값: "  "
MbStringUtil.substrByBytes("가나다abc", 4, 3, euckr) // 반환값: "다a"
MbStringUtil.substrByBytes("가나다abc", 5, 2, euckr) // 반환값: " a"

// 인코딩 불가능한 문자 예시
MbStringUtil.substrByBytes("a👍가", 0, 4, euckr)      // 반환값: "a 가"

// UTF-8 예시
MbStringUtil.substrByBytes("가나다abc", 0, 3, StandardCharsets.UTF_8) // 반환값: "가"
MbStringUtil.substrByBytes("가나다abc", 2, 4, StandardCharsets.UTF_8) // 반환값: " 나"
MbStringUtil.substrByBytes("가나다abc", 2, 5, StandardCharsets.UTF_8) // 반환값: " 나 "
```

---

### 기타 메소드

- **`substring(String, int, int)`** 및 **`substringByBytes(String, int, int, Charset)`**
  - `substr` 및 `substrByBytes`의 변형으로, 길이 대신 시작 및 끝 인덱스를 사용합니다.

- **`leftPad(String, int, String)`** 및 **`rightPad(String, int, String)`**
  - 지정된 문자 길이에 맞게 문자열을 패딩합니다.

- **`leftPadByBytes(String, int, String, Charset)`** 및 **`rightPadByBytes(String, int, String, Charset)`**
  - 지정된 바이트 길이에 맞게 문자열을 패딩합니다.

---

### `length` 및 `lengthByBytes`

문자열의 길이를 문자(코드 포인트) 또는 바이트 단위로 계산합니다. null 또는 빈 문자열의 경우 0을 반환합니다.

`length` 메소드는 `String.length()`와 다른 결과를 낼 수 있습니다. 이 메소드는 실제 문자의 개수(코드 포인트)를 세는 반면, `String.length()`는 16비트 `char` 단위의 수를 셉니다. 예를 들어, 이모지("👍")와 같은 보충 문자는 이 메소드에서는 하나의 문자로 취급되지만, `String.length()`에서는 두 개의 `char`로 취급됩니다.

```java
// length
MbStringUtil.length(null)      // 반환값: 0
MbStringUtil.length("👍a가")   // 반환값: 3
// 참고: "👍a가".length()의 반환값은 4입니다.

// lengthByBytes
Charset euckr = Charset.forName("EUC-KR");
MbStringUtil.lengthByBytes("가나다", euckr) // 반환값: 6
MbStringUtil.lengthByBytes("👍a가", StandardCharsets.UTF_8) // 반환값: 8
```

## 빌드하기

프로젝트를 빌드하고 테스트를 실행하려면 다음 Maven 명령을 사용하세요.

```shell
mvn clean install
```
