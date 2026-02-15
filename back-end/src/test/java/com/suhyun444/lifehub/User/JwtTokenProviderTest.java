package com.suhyun444.lifehub.User; // 패키지명 소문자 주의

import com.suhyun444.lifehub.card.Entity.User;
import com.suhyun444.lifehub.User.JwtTokenProvider; // 실제 클래스 경로 import
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private final String SECRET_KEY = "c29tZXJlYWxseWxvbmdzZWNyZXRrZXl0aGF0aXNzdWZmaWNpZW50bHlsb25nZm9yYWxnb3JpdGhtMTIzaHM1MTJzZWNyZXRrZXlmb3J0ZXN0aW5ncHVycG9zZXM=";
    private final long VALIDITY_IN_MS = 3600000; // 1시간

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET_KEY, VALIDITY_IN_MS);
    }

    // --- 1. generateToken 테스트 ---

    @Test
    @DisplayName("generateToken: 유효한 사용자 정보로 토큰을 생성하면 비어있지 않은 문자열이 반환되어야 한다.")
    void generateToken_Success() {
        // given
        User user = new User("test@example.com");

        // when
        String token = jwtTokenProvider.generateToken(user);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("generateToken: 생성된 토큰은 올바른 Subject(User Email)를 포함해야 한다.")
    void generateToken_ContainsCorrectSubject() {
        // given
        String email = "user@test.com";
        User user = new User(email);

        // when
        String token = jwtTokenProvider.generateToken(user);

        // then
        String subject = jwtTokenProvider.getUserIdFromToken(token);
        assertThat(subject).isEqualTo(email);
    }

    @Test
    @DisplayName("generateToken: 생성된 토큰의 만료 시간은 현재 시간 이후여야 한다.")
    void generateToken_ExpirationCheck() {
        // given
        User user = new User("time@test.com");

        // when
        String token = jwtTokenProvider.generateToken(user);

        // then
        // 직접 파싱하여 만료시간 확인
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getExpiration();
        
        assertThat(expiration).isAfter(new Date());
    }

    // --- 2. getUserIdFromToken 테스트 ---

    @Test
    @DisplayName("getUserIdFromToken: 유효한 토큰에서 사용자 ID(Email)를 정확히 추출해야 한다.")
    void getUserIdFromToken_ValidToken() {
        // given
        String email = "extract@test.com";
        String token = jwtTokenProvider.generateToken(new User(email));

        // when
        String extractedId = jwtTokenProvider.getUserIdFromToken(token);

        // then
        assertThat(extractedId).isEqualTo(email);
    }

    @Test
    @DisplayName("getUserIdFromToken: 서로 다른 사용자는 서로 다른 토큰 값을 가져야 한다.")
    void getUserIdFromToken_DifferentUsers() {
        // given
        String token1 = jwtTokenProvider.generateToken(new User("user1@test.com"));
        String token2 = jwtTokenProvider.generateToken(new User("user2@test.com"));

        // then
        assertThat(token1).isNotEqualTo(token2);
    }
    
    // --- 3. validateToken 테스트 ---

    @Test
    @DisplayName("validateToken: 유효한 토큰은 true를 반환해야 한다.")
    void validateToken_Valid() {
        // given
        String token = jwtTokenProvider.generateToken(new User("valid@test.com"));

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("validateToken: 만료된 토큰은 false를 반환해야 한다.")
    void validateToken_Expired() {
        // given
        // 만료 시간이 0인 프로바이더 생성 (이미 생성된 시점에 만료됨)
        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET_KEY, 0); 
        
        // 주의: generateToken 내부에서 new Date()를 찍으므로 0ms 유효기간이면 즉시 만료된 것으로 간주될 가능성 높음
        // 더 확실한 테스트를 위해 유효기간을 -1000으로 설정하거나, Thread.sleep을 줄 수 있음.
        // 여기서는 -1000(과거)으로 설정
        JwtTokenProvider pastProvider = new JwtTokenProvider(SECRET_KEY, -1000);
        String expiredToken = pastProvider.generateToken(new User("expired@test.com"));

        // when
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken: 잘못된 서명(다른 키로 서명된 토큰)은 false를 반환해야 한다.")
    void validateToken_InvalidSignature() {
        // given
        // 서명 검증 실패 테스트용 다른 키 (이것도 충분히 길어야 함)
        String otherKey = "ZGlmZmVyZW50bG9uZ3NlY3JldGtleWZvcmhhY2luZ3Rlc3RpbmdwdXJwb3Nlc3RoYXRpc2Fsc292ZXJ5bG9uZw==";
        JwtTokenProvider otherProvider = new JwtTokenProvider(otherKey, VALIDITY_IN_MS);
        
        // 해커가 자신의 키로 서명한 토큰 생성
        String invalidToken = otherProvider.generateToken(new User("hacker@test.com"));

        // when
        // 내 서버의 키로 검증 시도
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken: 형식이 잘못된 토큰(Malformed)은 false를 반환해야 한다.")
    void validateToken_Malformed() {
        // given
        String malformedToken = "eyJhbGciOiJIUzUxMiJ9.invalid.payload";

        // when
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // then
        assertThat(isValid).isFalse();
    }
}