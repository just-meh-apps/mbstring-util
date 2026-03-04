# MbStringUtil - 다중 바이트 문자열 유틸리티

## 개요

`MbStringUtil`은 Java에서 UTF-8, EUC-KR 등과 같은 다중 바이트 인코딩 문자열을 안전하게 다루기 위한 유틸리티 클래스입니다.

이 라이브러리는 문자 길이 및 바이트 길이를 기준으로 서브스트링을 추출하는 기능을 제공합니다. 특히 바이트 단위로 문자열을 다룰 때 다중 바이트 문자가 깨지는 것을 방지하기 위해, 잘리는 부분은 공백으로 안전하게 치환하는 기능을 포함합니다.

## 주요 기능

- **`substr(String, int, int)`**: 문자 수를 기준으로 서브스트링을 추출합니다.
- **`substrByBytes(String, int, int, Charset)`**: 바이트 길이를 기준으로 서브스트링을 추출하며, 문자 깨짐을 방지합니다.
- **음수 오프셋**: 문자열의 끝에서부터 위치를 계산하는 음수 인덱싱을 지원합니다.
- **다양한 인코딩 지원**: UTF-8, EUC-KR 등 Java에서 지원하는 모든 문자셋을 사용할 수 있습니다.

## 사용법 (API)

---

### `substr`

문자 수를 기준으로 서브스트링을 추출합니다.

```java
// str is null or empty
MbStringUtil.substr(null, 0, 1)      // 반환값: ""
MbStringUtil.substr("", 0, 1)        // 반환값: ""

// start is positive
MbStringUtil.substr("가나다abc", 0, 2) // 반환값: "가나"
MbStringUtil.substr("가나다abc", 3, 2) // 반환값: "ab"

// start is negative
MbStringUtil.substr("가나다abc", -5, 2) // 반환값: "나다"
MbStringUtil.substr("가나다abc", -2, 2) // 반환값: "bc"
```

---

### `substrByBytes`

바이트 길이를 기준으로 서브스트링을 추출합니다. 다중 바이트 문자가 잘릴 경우 안전하게 공백으로 치환합니다.

```java
Charset euckr = Charset.forName("EUC-KR");

// EUC-KR 예시
MbStringUtil.substrByBytes("가나다abc", 0, 2, euckr) // 반환값: "가"
MbStringUtil.substrByBytes("가나다abc", 1, 2, euckr) // 반환값: "  "
MbStringUtil.substrByBytes("가나다abc", 4, 3, euckr) // 반환값: "다a"
MbStringUtil.substrByBytes("가나다abc", 5, 2, euckr) // 반환값: " a"

// UTF-8 예시
MbStringUtil.substrByBytes("가나다abc", 0, 3, StandardCharsets.UTF_8) // 반환값: "가"
MbStringUtil.substrByBytes("가나다abc", 2, 4, StandardCharsets.UTF_8) // 반환값: " 나"
MbStringUtil.substrByBytes("가나다abc", 2, 5, StandardCharsets.UTF_8) // 반환값: " 나 "
```

## 빌드하기

프로젝트를 빌드하고 테스트를 실행하려면 다음 Maven 명령을 사용하세요.

```shell
mvn clean install
```
