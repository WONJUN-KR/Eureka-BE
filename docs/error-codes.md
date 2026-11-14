# 에러 코드 정의

## 코드 체계

```
{도메인}{숫자3자리}

도메인:
- C  : Common (공통)
- A  : Auth (인증)
- U  : User (사용자)
- P  : Post (게시글)
- PL : Place (들른 장소)
- I  : Image (이미지)
- L  : Like (좋아요)
- CM : Comment (댓글)
- BM : Bookmark (북마크)
- R  : Region (지역)
- RP : Report (신고)
```

---

## 공통 에러 (C)

| 코드 | HTTP | 메시지 |
|------|------|--------|
| C001 | 400 | 잘못된 요청입니다 |
| C002 | 400 | 필수 파라미터가 누락되었습니다 |
| C003 | 400 | 유효하지 않은 파라미터입니다 |
| C004 | 500 | 서버 오류가 발생했습니다 |
| C005 | 404 | 요청한 리소스를 찾을 수 없습니다 |

---

## 인증 에러 (A)

| 코드 | HTTP | 메시지 |
|------|------|--------|
| A001 | 401 | 로그인이 필요합니다 |
| A002 | 403 | 권한이 없습니다 |
| A003 | 401 | 토큰이 만료되었습니다 |
| A004 | 401 | 유효하지 않은 토큰입니다 (카카오 로그인 코드 교환 실패도 이 코드) |
| A005 | 502 | 카카오 로그인에 실패했습니다 (콜백 · 시작에서는 프론트로 `#error=kakao_login_failed`) |

---

## 사용자 에러 (U)

| 코드 | HTTP | 메시지 |
|------|------|--------|
| U001 | 409 | 이미 사용 중인 이메일입니다 |
| U002 | 409 | 이미 사용 중인 닉네임입니다 |
| U003 | 404 | 사용자를 찾을 수 없습니다 |
| U004 | 400 | 비밀번호가 일치하지 않습니다 |

---

## 게시글 에러 (P)

| 코드 | HTTP | 메시지 |
|------|------|--------|
| P001 | 404 | 게시글을 찾을 수 없습니다 |
| P002 | 403 | 본인의 게시글만 수정할 수 있습니다 |
| P003 | 403 | 본인의 게시글만 삭제할 수 있습니다 |

---

## 이미지 에러 (I)

| 코드 | HTTP | 메시지 |
|------|------|--------|
| I001 | 404 | 이미지를 찾을 수 없습니다 |
| I002 | 500 | 이미지 업로드에 실패했습니다 |
| I003 | 400 | 장소 없이 올리는 사진은 게시글당 최대 10장까지 업로드 가능합니다 |
| I004 | 400 | 허용되지 않는 파일 형식입니다 |
| I005 | 403 | 이미지에 대한 권한이 없습니다 |
| I006 | 500 | 잘못된 이미지 URL 형식입니다 |
| I007 | 500 | 이미지 삭제에 실패했습니다 |

---

## 좋아요 에러 (L)

| 코드 | HTTP | 메시지 |
|------|------|--------|
| L001 | 409 | 이미 좋아요한 게시글입니다 |
| L002 | 404 | 좋아요 정보를 찾을 수 없습니다 |

---

## 장소 에러 (PL)

| 코드 | HTTP | 메시지 |
|------|------|--------|
| PL001 | 404 | 장소를 찾을 수 없습니다 |
| PL002 | 400 | 장소를 1곳 이상 넣어 주세요 |
| PL003 | 400 | 장소는 게시글당 최대 20곳까지 넣을 수 있습니다 |
| PL004 | 400 | 장소당 사진은 최대 3장까지 올릴 수 있습니다 |

---

## 신고 에러 (RP)

| 코드 | HTTP | 메시지 |
|------|------|--------|
| RP001 | 409 | 이미 신고한 게시글입니다 |
| RP002 | 400 | 본인의 게시글은 신고할 수 없습니다 |

---

## 댓글 에러 (CM)

| 코드 | HTTP | 메시지 |
|------|------|--------|
| CM001 | 404 | 댓글을 찾을 수 없습니다 |
| CM002 | 403 | 본인의 댓글만 수정할 수 있습니다 |
| CM003 | 403 | 본인의 댓글만 삭제할 수 있습니다 |

---

## 북마크 에러 (BM)

| 코드 | HTTP | 메시지 |
|------|------|--------|
| BM001 | 409 | 이미 북마크한 게시글입니다 |
| BM002 | 404 | 북마크 정보를 찾을 수 없습니다 |

---

## 구현 예시

### ErrorCode 인터페이스

```java
public interface ErrorCode {
    HttpStatus getStatus();
    String getCode();
    String getMessage();
}
```

### 도메인별 ErrorCode Enum

```java
@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ErrorCode {

    POST_NOT_FOUND(NOT_FOUND, "P001", "게시글을 찾을 수 없습니다"),
    POST_UPDATE_UNAUTHORIZED(FORBIDDEN, "P002", "본인의 게시글만 수정할 수 있습니다"),
    POST_DELETE_UNAUTHORIZED(FORBIDDEN, "P003", "본인의 게시글만 삭제할 수 있습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
```

### 도메인 Exception

```java
public class PostException extends BusinessException {

    private PostException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static PostException notFound() {
        return new PostException(PostErrorCode.POST_NOT_FOUND);
    }
}
```

### 사용 예시

```java
@Service
public class PostService {

    public Post findById(Long id) {
        return postRepository.findById(id)
            .orElseThrow(PostException::notFound);
    }
}
```
