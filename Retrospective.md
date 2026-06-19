# 회고 & AI 사용 기록

# 회고
## 1. 리팩토링을 진행하며 느낀 점

이번 과제를 진행하기 전에 기존 코드를 먼저 리팩토링하였다. 처음에는 Controller에서 대부분의 요청 처리와 비즈니스 로직을 같이 담당하고 있었고, 데이터 저장도 `MemoryStore`에 직접 접근하는 방식이었다. 기능은 동작했지만 코드가 커질수록 어느 부분에서 어떤 책임을 가져야 하는지 구분하기 어려웠다.

그래서 Controller는 요청과 응답을 담당하고, 실제 비즈니스 로직은 Service로 분리하는 방식으로 구조를 정리하였다. 이 과정에서 처음에는 단순히 파일만 나누면 된다고 생각했지만, 실제로는 Controller와 Service의 메서드 인자, 반환 타입, 예외 처리 방식까지 같이 맞춰야 했다. 특히 게시글 목록 조회에서 Controller는 `page`, `size`를 넘기는데 Service 메서드는 인자를 받지 않도록 작성되어 오류가 발생했던 점이 기억에 남는다. 이 경험을 통해 계층을 나누는 것은 단순히 클래스를 분리하는 것이 아니라, 각 계층 사이의 역할과 메서드 계약을 맞추는 작업이라는 것을 알게 되었다.

또한 예외 처리 구조를 만들면서도 어려움이 있었다. `DuplicateEmailException`, `InvalidLoginException`, `UserNotFoundException` 같은 예외를 만들고 `GlobalExceptionHandler`에서 상태 코드별로 응답을 내려주도록 구성했는데, Service에서 예외를 던질 때 예외 클래스의 생성자 형태와 맞지 않아 오류가 발생했다. 예를 들어 메시지를 받는 생성자만 있는데 빈 생성자로 호출해서 문제가 생겼다. 이를 해결하면서 예외 클래스도 단순히 만들어두는 것이 아니라, 실제로 Service에서 어떻게 사용할지까지 고려해야 한다는 것을 느꼈다.

---

## 2. H2 DB 설정 과정에서 겪은 어려움

H2 DB를 설정하면서 처음에는 H2가 MySQL Workbench와 같은 도구인지, 아니면 DB 자체인지 헷갈렸다. 이전에는 MySQL을 사용할 때 Workbench로 DB를 확인했기 때문에 H2 Console도 비슷한 도구라고 생각했는데, 실제로는 H2가 가벼운 DB이고 H2 Console은 그 DB를 확인하기 위한 화면이라는 점을 이해하게 되었다.

또한 H2 Console 접속이 처음에 되지 않아 원인을 찾는 데 시간이 걸렸다. H2 DB 연결 로그는 정상적으로 뜨는데 `/h2-console` 경로가 404로 나와서 헷갈렸다. 확인해보니 `application.yaml`의 들여쓰기나 H2 Console 관련 의존성 설정이 중요했다. 이 과정에서 YAML은 들여쓰기가 조금만 잘못되어도 설정이 전혀 다르게 인식될 수 있다는 것을 다시 느꼈다.

그리고 `jdbc:h2:mem:community`처럼 InMemory 방식으로 설정하면 서버를 껐다 켤 때 데이터가 사라진다는 점도 직접 확인했다. 처음에는 회원가입했던 `user_id = 1` 데이터가 서버 재시작 후 사라지는 것이 문제처럼 느껴졌지만, 메모리 DB 특성상 정상 동작이라는 것을 알게 되었다. 이후 테스트할 때는 서버를 재실행하면 회원가입부터 다시 진행해야 한다는 흐름을 이해하게 되었다.

---

## 3. JPA Entity를 구성하며 겪은 어려움

ERD와 DDL을 기준으로 `User`, `Post`, `Comment`, `PostLike`, `PostImage` Entity를 구성하였다. 처음에는 DDL의 컬럼을 Java 필드로 옮기는 정도라고 생각했지만, JPA에서는 단순히 `userId`, `postId` 같은 숫자 값만 두는 것이 아니라 객체 간 연관관계를 표현해야 한다는 점이 어려웠다.

예를 들어 기존 InMemory 구조에서는 게시글이 작성자 ID를 `Long userId`로 가지고 있었지만, JPA에서는 `Post`가 `User` 객체를 `@ManyToOne`으로 참조하도록 바꿔야 했다. 댓글도 마찬가지로 `postId`, `userId`만 가지는 것이 아니라 `Post`, `User`를 각각 참조하도록 변경하였다.

이 과정에서 기존 DTO나 응답 코드와의 호환을 위해 임시로 `@Transient` 필드를 두었다가, 나중에 이를 제거하면서 오류가 발생했다. `userId` 필드는 삭제했는데 `getUserId()` 메서드 안에서 여전히 `return userId;`를 하고 있어 컴파일 오류가 났다. 이 경험을 통해 임시 코드를 제거할 때는 필드만 지우는 것이 아니라, 해당 필드를 참조하는 생성자와 getter까지 함께 정리해야 한다는 것을 배웠다.

---

## 4. Repository 적용 과정에서 겪은 어려움

기존에는 `MemoryStore.saveUser()`, `MemoryStore.findUserById()`처럼 직접 저장소 메서드를 호출했다. JPA 적용 후에는 이를 `userRepository.save()`, `userRepository.findById()`처럼 Repository를 사용하는 방식으로 변경하였다.

처음에는 Repository에 메서드를 많이 만들어두면 좋다고 생각했지만, 진행하면서 실제로 현재 기능에서 사용하는 메서드와 ERD 기준으로 나중에 필요할 수 있는 메서드를 구분해야 한다는 것을 느꼈다. 예를 들어 좋아요 API는 아직 구현하지 않았는데 `existsByUser_IdAndPost_Id`, `countByPost_Id` 같은 메서드를 미리 만들어두니, 현재 과제 기준에서는 사용하지 않는 코드가 많아 보였다.

그래서 최종적으로 현재 Service에서 실제 사용하는 메서드는 유지하고, 좋아요나 이미지 조회처럼 ERD에는 있지만 아직 API로 구현하지 않은 기능의 메서드는 주석 처리하는 방식으로 정리하였다. 이를 통해 Repository도 무조건 많이 만들어두는 것이 아니라, 현재 기능에 필요한 메서드와 추후 확장 가능성이 있는 메서드를 구분해서 관리해야 한다는 것을 알게 되었다.

---

## 5. JPA Query Method 이름을 이해하며 느낀 점

Spring Data JPA의 Query Method도 처음에는 낯설었다. `findByUser_IdOrderByIdAsc`, `deleteByPost_Id` 같은 메서드 이름이 길어서 복잡하게 느껴졌지만, Entity의 연관관계를 따라가는 방식이라는 것을 이해하고 나니 왜 이런 이름이 필요한지 알게 되었다.

특히 `Post` Entity 안에는 `Long userId`가 있는 것이 아니라 `User user`가 있기 때문에, 사용자 ID로 검색하려면 `findByUser_Id`처럼 `user` 객체 안의 `id`를 기준으로 검색한다는 의미를 표현해야 했다. 단순히 DB 컬럼 이름만 생각하는 것이 아니라, JPA에서는 Entity 필드와 객체 관계를 기준으로 메서드명을 작성해야 한다는 점을 배웠다.

---

## 6. 삭제 방식에 대해 고민한 점

ERD에는 `deleted_at` 컬럼을 넣어두었지만, 실제 구현에서는 `Repository.delete()`를 사용하여 행을 실제로 삭제하는 물리 삭제 방식으로 구현하였다. 처음에는 `deleted_at` 컬럼이 있는데 삭제하면 행이 사라지기 때문에 이 컬럼이 의미가 있는지 헷갈렸다.

확인해보니 `deleted_at`은 Soft Delete 방식에서 의미가 있는 컬럼이었다. Soft Delete는 행을 실제로 삭제하지 않고 `deleted_at`에 삭제 시간을 기록한 뒤, 조회할 때 `deleted_at IS NULL` 조건을 사용해 삭제되지 않은 데이터만 보여주는 방식이다.

이번 과제에서는 기존 InMemory 구현과 동일한 삭제 동작을 유지하기 위해 물리 삭제를 선택했다. 따라서 현재 구현에서는 삭제 시 행이 제거되고, `deleted_at`은 추후 Soft Delete 방식으로 확장할 때 사용할 수 있는 컬럼으로 남겨두었다. 이 과정을 통해 DB 설계에 컬럼이 있다고 해서 자동으로 그 기능이 동작하는 것이 아니라, 애플리케이션 코드와 조회 조건까지 함께 맞춰야 한다는 점을 알게 되었다.

---

## 7. 최신순 조회와 정렬에 대해 배운 점

게시글 목록 조회에서 최신순 정렬도 고민이 되었다. 처음에는 `id` 오름차순으로 조회하고 있었는데, 최신순이라면 `createdAt` 기준 내림차순으로 정렬해야 한다는 것을 알게 되었다.

JPA에서 정렬할 때는 DB 컬럼명인 `created_at`이 아니라 Entity 필드명인 `createdAt`을 사용해야 했다. 그래서 게시글 목록 조회는 `PageRequest`와 `Sort.by(Sort.Direction.DESC, "createdAt")`을 사용하여 최신순으로 정렬하도록 수정하였다.

이를 통해 JPA에서는 SQL 컬럼명보다 Entity 필드명을 기준으로 코드를 작성하는 경우가 많다는 것을 다시 확인하였다.

---

## 8. 최종적으로 배운 점

이번 과제를 통해 단순히 “DB를 연결했다”는 수준을 넘어서, 기존 InMemory 기반 구조를 실제 DB 기반 구조로 바꾸는 흐름을 경험할 수 있었다.

특히 다음 내용을 직접 겪으며 이해할 수 있었다.

* Controller, Service, Repository의 역할 분리
* H2 DB와 H2 Console 설정
* ERD와 DDL을 기반으로 Entity를 구성하는 방법
* `@ManyToOne`, `@JoinColumn`을 이용한 연관관계 매핑
* `JpaRepository`를 이용한 저장, 조회, 수정, 삭제
* JPA 변경 감지
* Query Method 작성 방식
* Hard Delete와 Soft Delete의 차이
* 최신순 조회를 위한 정렬 처리
* 현재 기능에서 필요한 코드와 추후 확장용 코드를 구분하는 방법

처음에는 오류가 하나 생기면 어디서부터 봐야 할지 헷갈렸지만, 진행하면서 Controller와 Service의 메서드 형태가 맞는지, Repository에 필요한 메서드가 있는지, Entity 필드명과 Query Method가 맞는지 순서대로 확인하는 방식이 조금씩 익숙해졌다.

이번 과제를 통해 JPA가 단순히 SQL을 대신 작성해주는 도구가 아니라, 객체와 테이블의 관계를 어떻게 설계하고 관리할지 고민해야 하는 기술이라는 것을 느꼈다.

---

# AI 사용 기록

## 1. 사용 목적

이번 과제에서는 기존 InMemory 기반 커뮤니티 API를 H2 DB와 Spring Data JPA 기반으로 전환하는 과정에서 AI를 활용하였다.

AI를 사용한 목적은 과제 전체를 대신 맡기기 위한 것이 아니라, 리팩토링 방향을 점검하고 JPA, H2 DB, Repository, Entity 연관관계 등 아직 익숙하지 않은 개념을 이해하기 위해 질문하는 방식이었다.

특히 직접 코드를 작성하고 실행하면서 발생한 오류를 바탕으로 원인을 질문하고, 그 답변을 참고하여 수정 방향을 결정하였다.

---

## 2. 리팩토링 과정에서의 활용

JPA를 적용하기 전에 기존 Controller 중심 코드 구조를 먼저 정리하였다.
기존에는 Controller에서 요청 처리와 비즈니스 로직을 함께 담당하고 있었기 때문에 코드가 커질수록 책임 구분이 어려웠다.

이 과정에서 AI에게 Controller, Service, Repository의 역할 분리에 대해 질문하였고, Controller는 요청과 응답을 담당하고 Service는 비즈니스 로직을 담당하는 구조로 정리하는 방향을 잡았다.

또한 DTO를 요청용과 응답용으로 분리하는 이유, 예외 처리를 `GlobalExceptionHandler`로 모아 관리하는 이유에 대해 질문하며 구조를 개선하였다.

주요 질문 내용은 다음과 같다.

* Controller에 있던 로직을 Service로 분리하는 기준
* Request DTO와 Response DTO를 분리해야 하는 이유
* 예외 응답을 상태 코드별로 관리하는 방법
* 기존 API 경로를 더 일관성 있게 정리하는 방법
* 게시글, 댓글, 회원 삭제 시 연결된 데이터를 어떤 순서로 처리해야 하는지

이를 통해 단순히 코드만 나누는 것이 아니라 각 계층이 어떤 책임을 가져야 하는지 이해하는 데 도움을 받았다.

---

## 3. H2 DB 설정 과정에서의 활용

H2 DB를 설정하는 과정에서 처음에는 H2가 MySQL Workbench와 같은 도구인지, 아니면 DB 자체인지 헷갈렸다.
이 부분을 AI에게 질문하여 H2는 가벼운 DB이고, H2 Console은 그 DB를 확인하기 위한 화면이라는 점을 정리하였다.

또한 `/h2-console` 접속이 되지 않았을 때, 로그에는 H2 DB 연결이 성공한 것처럼 보이는데 브라우저에서는 404가 발생하였다.
이때 AI에게 상황을 설명하고 `application.yaml`의 들여쓰기, H2 Console 활성화 설정, 의존성 추가 여부를 점검하였다.

주요 질문 내용은 다음과 같다.

* H2 DB와 MySQL Workbench의 차이
* `username: sa`, `password:` 설정의 의미
* `jdbc:h2:mem:community`가 의미하는 것
* 서버를 재시작하면 데이터가 사라지는 이유
* H2 Console 접속 URL과 JDBC URL을 맞춰야 하는 이유
* H2 Console이 404가 날 때 확인해야 할 설정

이 과정을 통해 H2 InMemory DB는 서버 실행 중에만 유지되는 임시 DB이며, 서버를 재시작하면 데이터가 초기화된다는 점을 이해하였다.

---

## 4. JPA Entity 구성 과정에서의 활용

ERD와 DDL을 기준으로 JPA Entity를 구성하면서 AI를 활용하였다.
처음에는 DDL의 컬럼을 Java 필드로 옮기면 된다고 생각했지만, JPA에서는 외래 키를 단순히 `Long userId`, `Long postId`로만 표현하는 것이 아니라 객체 연관관계로 표현할 수 있다는 점을 질문을 통해 이해하였다.

예를 들어 게시글은 작성자인 회원을 참조하므로 `Post` Entity에서 `User`를 `@ManyToOne`으로 매핑하였고, 댓글은 게시글과 작성자를 모두 참조하므로 `Post`, `User`를 각각 `@ManyToOne`으로 매핑하였다.

주요 질문 내용은 다음과 같다.

* DDL의 `DATETIME`을 Java에서 어떤 타입으로 매핑해야 하는지
* `@Entity`, `@Table`, `@Id`, `@GeneratedValue`의 역할
* `AUTO_INCREMENT`를 JPA에서 어떻게 표현하는지
* `@ManyToOne`과 `@JoinColumn`을 사용하는 이유
* 기존 `userId`, `postId` 필드를 JPA 연관관계로 바꾸는 방법
* `@Transient` 임시 필드를 언제 제거해야 하는지

특히 임시 필드를 제거하는 과정에서 `userId` 필드는 삭제했지만 `getUserId()` 메서드에서 여전히 `return userId;`를 하고 있어 오류가 발생하였다.
이 오류를 해결하면서 필드, 생성자, getter가 서로 연결되어 있기 때문에 임시 코드를 제거할 때는 관련 코드도 함께 정리해야 한다는 점을 알게 되었다.

---

## 5. Repository와 Query Method 적용 과정에서의 활용

기존 `MemoryStore`를 제거하고 `JpaRepository`를 적용하는 과정에서도 AI를 활용하였다.

처음에는 Repository에 필요한 메서드를 어느 정도까지 만들어야 하는지 헷갈렸다.
AI에게 현재 구현된 API와 ERD에만 존재하는 기능을 구분하는 기준을 질문하였고, 현재 Service에서 실제로 호출하는 메서드는 유지하고, 좋아요나 이미지 조회처럼 아직 API로 구현하지 않은 기능은 주석으로 남기는 방식으로 정리하였다.

주요 질문 내용은 다음과 같다.

* `JpaRepository`가 기본으로 제공하는 기능
* 직접 Query Method를 만들어야 하는 경우
* `findByUser_IdOrderByIdAsc`처럼 이름이 길어지는 이유
* Entity 필드 기준으로 Query Method를 작성해야 하는 이유
* 현재 사용하지 않는 Repository 메서드를 어떻게 정리할지
* ERD 기준으로는 필요하지만 현재 과제에서는 사용하지 않는 메서드를 주석 처리하는 기준

이를 통해 Repository는 무조건 많은 메서드를 만들어두는 것이 아니라, 현재 기능에서 실제로 필요한 메서드와 추후 확장 가능성이 있는 메서드를 구분해서 관리해야 한다는 점을 배웠다.

---

## 6. JPA 적용 후 오류 해결 과정에서의 활용

JPA를 적용하면서 여러 오류가 발생했고, 오류 메시지와 코드 상황을 AI에게 설명하며 원인을 파악하였다.

예를 들어 `PostController`는 `page`, `size`를 Service로 넘기고 있었지만, `PostService`의 `getPosts()` 메서드는 인자를 받지 않도록 작성되어 있어 오류가 발생하였다.
이 문제를 통해 Controller와 Service의 메서드 시그니처가 서로 맞아야 한다는 것을 다시 확인하였다.

또한 예외 클래스에서 메시지를 받는 생성자만 있는데 Service에서 빈 생성자로 예외를 호출하여 오류가 발생하기도 하였다.
이때 예외 클래스의 생성자 형태와 Service에서 예외를 던지는 방식이 일치해야 한다는 점을 알게 되었다.

주요 질문 내용은 다음과 같다.

* Controller와 Service 메서드 인자 개수가 다를 때 발생하는 오류
* 예외 클래스 생성자와 Service 예외 호출 방식이 맞지 않을 때의 오류
* Repository 메서드를 삭제했을 때 Service에서 빨간 줄이 발생하는 이유
* Hibernate 로그에서 `?`가 표시되는 이유
* JPA가 실제로 어떤 SQL을 실행하는지 확인하는 방법

오류를 단순히 고치는 것보다 왜 오류가 발생했는지 이해하려고 질문한 점이 도움이 되었다.

---

## 7. 강사 피드백과 설계 고민에 대한 활용

지난 ERD 과제에 대한 강사 피드백을 이해하는 과정에서도 AI를 활용하였다.

특히 `deleted_at`, 인덱스, 최신순 조회, 좋아요 테이블 설계, 이미지 테이블 이름 등에 대해 질문하였다.
강사 피드백을 그대로 받아들이기보다, 현재 과제 구현과 어떻게 연결되는지 확인하는 방식으로 질문하였다.

주요 질문 내용은 다음과 같다.

* `deleted_at` 컬럼이 있는데 실제 삭제하면 행이 사라지는 것이 맞는지
* Hard Delete와 Soft Delete의 차이
* Soft Delete를 사용하면 조회 조건에 `deleted_at IS NULL`이 필요한 이유
* 최신순 조회와 Soft Delete가 같은 개념인지
* 게시글 최신순 조회를 `createdAt DESC`로 처리하는 방법
* `Sort.by()`에는 DB 컬럼명 `created_at`이 아니라 Entity 필드명 `createdAt`을 사용해야 하는 이유
* `post_likes`에서 `post_like_id`를 PK로 두고 `(user_id, post_id)`를 UNIQUE로 두는 의미
* `image` 테이블명을 `post_images`로 바꾸는 이유
* 이미지 순서나 대표 이미지 컬럼이 필요한 이유

이를 통해 ERD 설계는 단순히 테이블을 그리는 것이 아니라, 실제 조회 방식과 삭제 정책, 확장 가능성까지 함께 고려해야 한다는 점을 이해하였다.

---

## 8. 최종 정리

이번 과제에서 AI는 코드를 대신 작성해주는 도구라기보다, 내가 막힌 개념을 질문하고 방향을 점검하는 용도로 사용하였다.

특히 다음 부분에서 도움을 받았다.

* 리팩토링 방향 점검
* Controller, Service, Repository 역할 구분
* H2 DB와 H2 Console 설정 이해
* JPA Entity와 연관관계 매핑 이해
* Repository Query Method 작성 기준 이해
* JPA 적용 중 발생한 오류 원인 파악
* Hard Delete와 Soft Delete 차이 정리
* 강사 피드백을 현재 구현과 연결해서 해석

AI의 답변을 그대로 적용하기보다는 직접 실행해보고, 오류가 발생하면 원인을 다시 질문하며 수정하였다.
이번 과제를 통해 AI를 활용할 때도 내가 현재 코드의 구조와 요구사항을 이해하고 있어야 적절한 질문을 할 수 있다는 점을 느꼈다.

