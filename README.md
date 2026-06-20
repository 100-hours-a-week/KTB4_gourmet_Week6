# KTB4_gourmet_Week6

## 1. 프로젝트 개요

기존 InMemory 방식으로 구현되어 있던 커뮤니티 API를 H2 Database와 Spring Data JPA 기반으로 전환한 프로젝트입니다.

기존에는 Java 내부의 `MemoryStore`를 사용하여 회원, 게시글, 댓글 데이터를 저장하고 조회하였지만, 이번 과제에서는 ERD 설계를 기반으로 Entity를 구성하고 Repository 계층을 추가하여 실제 DB에 데이터를 저장하도록 변경하였습니다.

최종적으로 기존 API가 H2 DB와 JPA 환경에서도 동일하게 동작하는지 Postman과 H2 Console을 통해 확인하였습니다.

---

## 2. 과제 목표

이번 과제의 주요 목표는 다음과 같습니다.

1. ERD 설계를 기반으로 JPA Entity 구성
2. 기존 InMemory DB 구조를 H2 DB와 Spring Data JPA 기반으로 변경
3. 기존 API가 동일하게 수행되는지 확인
4. H2 Console을 통해 실제 테이블 생성 및 데이터 저장 여부 확인

---

## 3. 프로젝트 구조

```text
src/main/java/KTB4_gourmet_Week6/Assignment
├── controller
│   ├── UserController
│   ├── PostController
│   └── CommentController
│
├── service
│   ├── UserService
│   ├── PostService
│   └── CommentService
│
├── repository
│   ├── UserRepository
│   ├── PostRepository
│   ├── CommentRepository
│   ├── PostLikeRepository
│   └── PostImageRepository
│
├── entity
│   ├── User
│   ├── Post
│   ├── Comment
│   ├── PostLike
│   └── PostImage
│
├── dto
│   ├── UserSignupRequestDto
│   ├── UserUpdateRequestDto
│   ├── LoginRequestDto
│   ├── UserResponseDto
│   ├── PostCreateRequestDto
│   ├── PostUpdateRequestDto
│   ├── PostResponseDto
│   ├── CommentCreateRequestDto
│   ├── CommentUpdateRequestDto
│   └── CommentResponseDto
│
└── exception
    ├── GlobalExceptionHandler
    ├── ErrorResponse
    ├── UserNotFoundException
    ├── PostNotFoundException
    ├── CommentNotFoundException
    ├── DuplicateEmailException
    └── InvalidLoginException
```

---

## 4. H2 Database 설정

`application.yaml`에 H2 Database 설정을 추가하였습니다.

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:community
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true
      path: /h2-console

  jpa:
    hibernate:
      ddl-auto: create
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

H2 Console 접속 정보는 다음과 같습니다.

```text
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:community
User Name: sa
Password: 비워두기
```

현재 설정은 InMemory H2 DB를 사용하기 때문에 서버를 재시작하면 데이터는 초기화됩니다.
비밀번호는 설정하지 않아서 빈칸으로 `connect`하면 연결이 된다.

---

## 5. ERD 기반 Entity 구성

지난 ERD 설계를 기반으로 다음 Entity를 구성하였습니다.

| Entity    | Table       | 설명         |
| --------- | ----------- | ---------- |
| User      | users       | 회원 정보      |
| Post      | posts       | 게시글 정보     |
| Comment   | comments    | 댓글 정보      |
| PostLike  | post_likes  | 게시글 좋아요 정보 |
| PostImage | post_images | 게시글 이미지 정보 |

주요 연관관계는 다음과 같습니다.

* 회원 1명은 여러 게시글을 작성할 수 있습니다.
* 회원 1명은 여러 댓글을 작성할 수 있습니다.
* 게시글 1개는 여러 댓글을 가질 수 있습니다.
* 게시글 1개는 여러 이미지를 가질 수 있습니다.
* 회원과 게시글은 좋아요를 통해 N:M 관계를 가질 수 있으며, 이를 `post_likes` 연결 테이블로 분리하였습니다.

JPA에서는 게시글과 회원, 댓글과 게시글, 댓글과 회원의 관계를 `@ManyToOne`으로 매핑하였습니다.

---

## 6. InMemory DB에서 JPA Repository로 변경

기존 구조는 다음과 같았습니다.

```text
Controller → Service → MemoryStore
```

변경 후 구조는 다음과 같습니다.

```text
Controller → Service → Repository → H2 DB
```

기존에 `MemoryStore`에서 처리하던 저장, 조회, 수정, 삭제 기능을 Spring Data JPA의 Repository로 변경하였습니다.

예를 들어 기존에는 회원 저장 시 `MemoryStore.saveUser()`를 사용했지만, 변경 후에는 `userRepository.save()`를 사용하도록 수정하였습니다.

---

## 7. Repository 구성

각 Entity에 맞는 Repository를 생성하였습니다.

* `UserRepository`
* `PostRepository`
* `CommentRepository`
* `PostLikeRepository`
* `PostImageRepository`

현재 과제에서 실제 사용하는 기능에 필요한 메서드만 활성화하였고, ERD 기준으로 설계에는 포함되지만 현재 API에서는 아직 사용하지 않는 좋아요 조회, 이미지 목록 조회, Soft Delete 관련 메서드는 주석으로 남겨 추후 확장 가능성을 표시하였습니다.

---

## 8. 구현 기능

### 회원 기능

* 회원가입
* 로그인
* 회원 전체 조회
* 회원 단건 조회
* 회원 정보 수정
* 회원 삭제

회원가입 시 이메일 중복 검사를 수행하며, 로그인 시 이메일과 비밀번호를 기준으로 회원을 조회합니다.

---

### 게시글 기능

* 게시글 생성
* 게시글 전체 조회
* 게시글 단건 조회
* 게시글 수정
* 게시글 삭제

게시글 생성 시 작성자 회원을 먼저 조회한 뒤, 해당 회원과 연결된 게시글 Entity를 생성합니다.

게시글 목록 조회는 `createdAt` 기준 내림차순 정렬을 적용하여 최신순으로 조회되도록 구현하였습니다.

---

### 댓글 기능

* 댓글 생성
* 댓글 목록 조회
* 댓글 단건 조회
* 댓글 수정
* 댓글 삭제

댓글 생성 시 댓글이 작성될 게시글과 작성자 회원을 각각 조회한 뒤 댓글 Entity를 생성합니다.

댓글 단건 조회, 수정, 삭제 시에는 URL의 `postId`와 실제 댓글이 속한 게시글 ID가 일치하는지 검증하도록 구현하였습니다.

---

## 10. API 목록

### User API

| Method | URL               | 설명       |
| ------ | ----------------- | -------- |
| POST   | `/users/signup`   | 회원가입     |
| POST   | `/users/login`    | 로그인      |
| GET    | `/users`          | 회원 전체 조회 |
| GET    | `/users/{userId}` | 회원 단건 조회 |
| PATCH  | `/users/{userId}` | 회원 정보 수정 |
| DELETE | `/users/{userId}` | 회원 삭제    |

---

### Post API

| Method | URL                     | 설명        |
| ------ | ----------------------- | --------- |
| POST   | `/users/{userId}/posts` | 게시글 생성    |
| GET    | `/posts?page=0&size=10` | 게시글 전체 조회 |
| GET    | `/posts/{postId}`       | 게시글 단건 조회 |
| PATCH  | `/posts/{postId}`       | 게시글 수정    |
| DELETE | `/posts/{postId}`       | 게시글 삭제    |

---

### Comment API

| Method | URL                                    | 설명       |
| ------ | -------------------------------------- | -------- |
| POST   | `/posts/{postId}/comments`             | 댓글 생성    |
| GET    | `/posts/{postId}/comments`             | 댓글 목록 조회 |
| GET    | `/posts/{postId}/comments/{commentId}` | 댓글 단건 조회 |
| PATCH  | `/posts/{postId}/comments/{commentId}` | 댓글 수정    |
| DELETE | `/posts/{postId}/comments/{commentId}` | 댓글 삭제    |

---

## 11. 테스트 순서

Postman을 사용하여 다음 순서로 API 동작을 확인하였습니다.

1. 회원가입
2. 로그인
3. 회원 전체 조회
4. 회원 단건 조회
5. 회원 정보 수정
6. 게시글 생성
7. 게시글 전체 조회
8. 게시글 단건 조회
9. 게시글 수정
10. 댓글 생성
11. 댓글 목록 조회
12. 댓글 단건 조회
13. 댓글 수정
14. 댓글 삭제
15. 게시글 삭제
16. 회원 삭제

각 요청 후 H2 Console에서 실제 데이터가 저장, 수정, 삭제되는 것을 확인하였습니다.

---

## 12. 삭제 방식

현재 구현에서는 `Repository.delete()`를 사용한 물리 삭제 방식을 적용하였습니다.

따라서 삭제 API 실행 시 행이 실제로 DB에서 제거됩니다.

ERD에는 `deleted_at` 컬럼을 두어 Soft Delete 확장 가능성을 고려하였지만, 이번 과제에서는 기존 InMemory 구현과 동일한 삭제 동작을 유지하기 위해 물리 삭제 방식으로 구현하였습니다.

추후 Soft Delete를 적용할 경우 삭제 시 행을 제거하지 않고 `deleted_at`에 삭제 시간을 기록하고, 조회 시 `deleted_at IS NULL` 조건을 추가하는 방식으로 확장할 수 있습니다.

---

## 13. 최종 결과

이번 과제를 통해 기존 InMemory 기반 커뮤니티 API를 H2 DB와 Spring Data JPA 기반으로 전환하였습니다.

ERD를 기반으로 Entity를 구성하고, Repository 계층을 추가하여 실제 DB와 연결되는 구조를 만들었습니다.

또한 `UserService`, `PostService`, `CommentService`에서 기존 `MemoryStore` 의존을 제거하고 Repository를 사용하도록 변경하였습니다.

최종적으로 회원, 게시글, 댓글 기능이 H2 DB에 정상적으로 저장되고 조회되는 것을 확인하였으며, 기존 API가 JPA 환경에서도 동일하게 동작하는 것을 확인하였습니다.
