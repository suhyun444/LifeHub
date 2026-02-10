package com.suhyun444.lifehub.User;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.suhyun444.lifehub.card.Entity.User;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final Key key;
    private final long tokenValidityInMilliseconds;

    // application.yml에 설정된 값들을 주입받습니다.
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.expiration-time}") long tokenValidityInMilliseconds) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.tokenValidityInMilliseconds = tokenValidityInMilliseconds;
    }
    

    /**
     * 사용자 정보를 받아 JWT를 생성합니다. (토큰 발급)
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + this.tokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getEmail())) // 토큰의 주체 (사용자 ID)
                .setIssuedAt(now) // 발급 시간
                .setExpiration(validity) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS512) // 사용할 암호화 알고리즘과 키
                .compact();
    }

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     */
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * 토큰의 유효성과 만료일을 검증합니다.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            logger.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            logger.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            logger.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            logger.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}