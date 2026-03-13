# 서비스 계층 분석 리포트

다음은 서비스 계층 클래스(`TransactionService`, `MarkerService`, `CustomOAuth2UserService`)를 분석한 결과입니다. 비효율적인 로직, 불필요한 `@Transactional` 어노테이션, 예상치 못한 동작을 유발할 수 있는 부분과 개선 전/후 코드를 정리했습니다.

---

## 1. `TransactionService.java`

### 불필요하거나 비효율적인 `@Transactional` 사용
*   **문제점**: `getTransactions(String email)` 메서드는 데이터를 읽기만 하는 작업입니다. 불필요하게 `@Transactional`이 적용되어 있으며, 적용하더라도 성능 최적화(예: 더티 체킹 비활성화 등)를 위해 `readOnly = true` 옵션을 사용하는 것이 좋습니다.

**개선 전 (Before):**
```java
@Transactional
public List<TransactionDto> getTransactions(String email) {
    User user = userRepository.findByEmail(email).orElseThrow();
    List<Transaction> transactions = transactionRepository.findByUserIdAndIsDeletedFalse(user.getId());
    // ...
}
```

**개선 후 (After):**
```java
@Transactional(readOnly = true)
public List<TransactionDto> getTransactions(String email) {
    User user = userRepository.findByEmail(email).orElseThrow();
    List<Transaction> transactions = transactionRepository.findByUserIdAndIsDeletedFalse(user.getId());
    // ...
}
```

### 비효율적인 로직: 과도한 데이터 반환 및 조회
*   **문제점 1**: `uploadAndParseExcel` 메서드는 새 엑셀 파일을 업로드하고 파싱하여 저장한 후, **해당 사용자의 모든 트랜잭션**을 다시 DB에서 불러와 반환합니다. 데이터가 많아질수록 비효율적이므로, 새로 추가된 데이터만 반환하거나 단순 성공 메시지를 반환하는 것이 좋습니다.
*   **문제점 2**: `clearTransactions` 메서드는 트랜잭션 삭제를 위해 단순히 `userId`만 필요한데 `User` 엔티티 전체를 DB에서 로드하고 있습니다.

**개선 전 (Before):**
```java
// uploadAndParseExcel 내 비효율적 반환 로직
importTransactions(transactions,user);
List<TransactionDto> result = transactionRepository.findByUserIdAndIsDeletedFalse(user.getId()).stream().map(TransactionDto::from).collect(Collectors.toList());
return result;

// clearTransactions 내 불필요한 엔티티 전체 조회
@Transactional
public void clearTransactions(String email) throws Exception {
    User user = userRepository.findByEmail(email).orElseThrow();
    transactionRepository.deleteByUserId(user.getId());
    return;
}
```

**개선 후 (After):**
```java
// uploadAndParseExcel 개선: 새로 저장한 항목만 DTO로 변환해 반환하거나 성공 상태만 반환
importTransactions(transactions,user);
return newTransactions.stream().map(TransactionDto::from).collect(Collectors.toList());

// clearTransactions 개선: ID만 가져오는 쿼리 사용 (Repository에 메서드 추가 필요)
@Transactional
public void clearTransactions(String email) throws Exception {
    Long userId = userRepository.findIdByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    transactionRepository.deleteByUserId(userId);
}
```

### 예상치 못한 결과를 유발할 수 있는 로직: 예외 처리 및 Optional 안티 패턴
*   **문제점 1**: `orElseThrow()`를 인자 없이 사용하여, 값이 없을 경우 `NoSuchElementException`이 발생합니다. 이는 HTTP 500 에러를 유발하기 쉬우므로 커스텀 예외로 처리하여 컨트롤러 단에서 적절한 HTTP 상태 코드(예: 404 Not Found)를 응답하도록 하는 것이 좋습니다.
*   **문제점 2**: `getAnalysis` 메서드에서 `Optional<List<T>>` 형태를 처리하고 있습니다. JPA 리포지토리에서 컬렉션을 반환할 때 `Optional`을 사용하는 것은 안티 패턴이며, 결과가 없을 때는 빈 리스트(`emptyList`)를 반환하는 것이 바람직합니다.

**개선 전 (Before):**
```java
// 제네릭 예외 발생
User user = userRepository.findByEmail(email).orElseThrow();

// Optional<List<T>> 안티 패턴
List<AnalysisHistory> histories = analysisHistoryRepository.findByUserId(user.getId()).orElseThrow();
```

**개선 후 (After):**
```java
// 명시적인 커스텀 예외 사용
User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자가 존재하지 않습니다."));

// List 자체를 반환받고 빈 리스트를 처리하는 방식으로 변경
List<AnalysisHistory> histories = analysisHistoryRepository.findByUserId(user.getId());
if (histories == null || histories.isEmpty()) {
    return Collections.emptyList();
}
```

---

## 2. `MarkerService.java`

### 예상치 못한 결과를 유발할 수 있는 로직: `@Transactional` 누락
*   **문제점**: `GetMarkers(String email)` 메서드에 `@Transactional` 어노테이션이 없습니다. `MarkerDto.from()` 매핑 과정에서 `Marker` 엔티티 내에 있는 지연 로딩(Lazy Loading)된 연관 컬렉션(예: `List<Link>`)에 접근할 경우 `LazyInitializationException`이 발생할 수 있습니다.

**개선 전 (Before):**
```java
public List<MarkerDto> GetMarkers(String email) {
    User user = userRepository.findByEmail(email).orElseThrow();
    List<Marker> markers = markerRepository.findAllByUserIdOrderBySortOrderDesc(user.getId());
    // ...
}
```

**개선 후 (After):**
```java
@Transactional(readOnly = true) // 지연 로딩 컬렉션 접근을 위한 트랜잭션 범위 할당
public List<MarkerDto> GetMarkers(String email) {
    User user = userRepository.findByEmail(email).orElseThrow();
    List<Marker> markers = markerRepository.findAllByUserIdOrderBySortOrderDesc(user.getId());
    // ...
}
```

### 비효율적인 로직: 불필요한 명시적 save 호출
*   **문제점**: `moveMarker` 메서드 마지막에 `markerRepository.save(marker);`를 호출하고 있습니다. 해당 메서드는 이미 `@Transactional`로 묶여 있어 영속성 컨텍스트(Persistence Context)의 관리를 받습니다. 따라서 객체의 상태(sortOrder)를 변경하기만 해도 트랜잭션 종료 시점에 변경 감지(Dirty Checking)가 동작하여 자동으로 UPDATE 쿼리가 날아갑니다. 중복되는 `save()` 호출은 불필요합니다.

**개선 전 (Before):**
```java
@Transactional
public void moveMarker(String email, Long markerId, Long newOrder) {
    // ...
    marker.setSortOrder(newOrder);
    markerRepository.save(marker); // 불필요
}
```

**개선 후 (After):**
```java
@Transactional
public void moveMarker(String email, Long markerId, Long newOrder) {
    // ...
    marker.setSortOrder(newOrder);
    // markerRepository.save(marker); 제거 - 더티 체킹에 의해 자동 반영됨
}
```

---

## 3. `CustomOAuth2UserService.java`

### 예상치 못한 결과를 유발할 수 있는 로직: 신규 유저 저장 시 트랜잭션 누락
*   **문제점**: `loadUser` 메서드 내부에서 DB에 사용자가 없을 경우 `userRepository.save(new User(email))`를 통해 DB에 쓰기 작업을 수행합니다. 하지만 해당 메서드나 클래스에 `@Transactional`이 없어 Auto-Commit 모드로 실행됩니다. 사용자가 동시에 여러 번 로그인 요청을 보내면 레이스 컨디션이나 중복 저장(또는 키 제약 조건 위배) 에러가 발생할 수 있습니다.

**개선 전 (Before):**
```java
@Override
public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
    // ...
    User user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(new User(email)));
    // ...
}
```

**개선 후 (After):**
```java
@Override
@Transactional // 신규 유저 생성의 안전성을 위해 트랜잭션 어노테이션 추가
public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
    // ...
    User user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(new User(email)));
    // ...
}
```