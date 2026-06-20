## 리팩토링 정리

강사님 피드백을 받고 기존 코드에서 구조를 먼저 정리했습니다.

### 1. Controller 책임 분리

기존에는 `Controller`에서 id 채번, 객체 생성, 검증, 저장, 응답 변환까지 모두 처리하고 있었습니다.

이를 수정해서 `Controller`는 요청을 받고 `Service`를 호출하는 역할만 하도록 바꿨습니다.

* `UserService` 추가
* `PostService` 추가
* `CommentService` 추가

변경 후 구조는 아래처럼 정리했습니다.

```text
Controller → Service → MemoryStore
```

---

### 2. 예외 처리 구조 추가

기존에는 없는 회원, 없는 게시글, 로그인 실패 등을 모두 `IllegalArgumentException`으로 처리하고 있었습니다.

이를 상황별 예외 클래스로 분리하고, `GlobalExceptionHandler`를 추가해서 실제 HTTP 상태 코드가 맞게 내려가도록 수정했습니다.

추가한 예외는 아래와 같습니다.

```text
UserNotFoundException
PostNotFoundException
CommentNotFoundException
InvalidLoginException
DuplicateEmailException
```

현재 응답 상태는 아래처럼 정리했습니다.

```text
없는 리소스 조회 → 404 Not Found
로그인 실패 → 401 Unauthorized
중복 이메일 → 409 Conflict
검증 실패 → 400 Bad Request
```

---

### 3. DTO 역할 분리

기존에는 하나의 요청 DTO를 여러 API에서 재사용하고 있었습니다.

회원가입과 회원수정처럼 목적이 다른 요청은 DTO를 나눴습니다.

```text
UserSignupRequestDto
UserUpdateRequestDto
LoginRequestDto

PostCreateRequestDto
PostUpdateRequestDto

CommentCreateRequestDto
CommentUpdateRequestDto
```

수정 요청에서 사용하지 않는 값이 조용히 무시되는 문제를 줄이기 위해 요청 목적에 맞게 DTO를 분리했습니다.

---

### 4. 요청값 검증 추가

`@Valid`, `@NotBlank`, `@Email`, `@NotNull`을 사용해서 빈 값이나 잘못된 형식의 요청이 들어오면 `400 Bad Request`가 나오도록 수정했습니다.

예를 들어 회원가입에서는 이메일, 비밀번호, 닉네임을 검증하고, 게시글과 댓글에서는 제목과 내용이 비어 있지 않도록 검증했습니다.

---

### 5. REST API 경로 정리

기존 경로 중 의미가 어색한 부분을 수정했습니다.

변경 전:

```text
POST /posts/users/{userId}
POST /posts/{postId}/comments/users/{userId}
```

변경 후:

```text
POST /users/{userId}/posts
POST /posts/{postId}/comments
```

댓글 생성 시 필요한 `userId`는 URL이 아니라 요청 body로 받도록 변경했습니다.

---

### 6. 댓글 목록 조회 API 추가

기존에는 댓글을 단건 조회만 할 수 있었습니다.

그래서 특정 게시글의 댓글 목록을 조회하는 API를 추가했습니다.

```text
GET /posts/{postId}/comments
```

게시글이 존재하지만 댓글이 없으면 빈 배열을 반환하고, 게시글 자체가 없으면 404가 나오도록 처리했습니다.

---

### 7. 삭제 시 연결 데이터 정리

게시글을 삭제했는데 댓글이 남는 문제가 있어서, 게시글 삭제 시 해당 게시글의 댓글도 같이 삭제되도록 수정했습니다.

또한 회원 삭제 시 해당 회원이 작성한 댓글, 게시글, 그리고 그 게시글에 달린 댓글도 함께 정리되도록 수정했습니다.

---

### 8. MemoryStore 캡슐화

기존에는 `MemoryStore`의 `Map`과 id 카운터를 외부에서 직접 접근하고 있었습니다.

이를 메서드로 감싸서 외부에서는 저장소 내부 구조를 직접 알 수 없도록 바꿨습니다.

변경 전:

```java
MemoryStore.users.get(userId);
MemoryStore.userId++;
```

변경 후:

```java
MemoryStore.findUserById(userId);
MemoryStore.generateUserId();
```

---

### 9. 동시성 안전성 개선

기존 `Long++` 방식의 id 증가와 `LinkedHashMap`은 동시 요청에 안전하지 않을 수 있다고 판단해서 아래처럼 수정했습니다.

```text
Long → AtomicLong
LinkedHashMap → ConcurrentHashMap
```

또한 `ConcurrentHashMap`은 순서를 보장하지 않기 때문에 목록 조회 시 id 기준으로 정렬해서 응답 순서가 안정적으로 나오도록 했습니다.

---

### 10. 상태 코드 확인

리팩토링 후 Postman으로 주요 상태 코드를 확인했습니다.

```text
로그인 성공 → 200 OK
게시글 생성 → 201 Created
댓글 생성 → 201 Created
댓글 목록 조회 → 200 OK
게시글 삭제 → 204 No Content
검증 실패 → 400 Bad Request
없는 리소스 조회 → 404 Not Found
로그인 실패 → 401 Unauthorized
중복 이메일 → 409 Conflict
```

이번 리팩토링은 H2/JPA 적용 전에 기존 InMemory 코드의 구조를 먼저 정리한 작업입니다.
다음 단계에서는 이 구조를 기반으로 `MemoryStore`를 제거하고 H2 DB와 JPA Repository로 전환할 예정입니다.
