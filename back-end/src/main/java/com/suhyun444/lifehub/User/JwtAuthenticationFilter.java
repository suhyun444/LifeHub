package com.suhyun444.lifehub.User;

import java.io.IOException;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 요청 헤더에서 JWT를 추출합니다.
            String token = parseBearerToken(request);
            // 2. 토큰이 유효한지 검증합니다.
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // 3. 토큰에서 사용자 ID(또는 이메일)를 꺼냅니다.
                String userEmail = jwtTokenProvider.getUserIdFromToken(token);

                // 4. Spring Security가 이해할 수 있는 인증 객체를 만듭니다.
                AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userEmail, // 보통 Principal에는 UserDetails 객체를 넣습니다.
                        null,
                        AuthorityUtils.NO_AUTHORITIES
                        );
                        
                // 5. SecurityContext에 인증 정보를 저장합니다. 이 요청 동안은 인증된 사용자로 간주됩니다.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            else
            {
                System.out.println("JWT Token Authentication Failed");
            }
        } 
        catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
            System.out.println("fail");
        }

        filterChain.doFilter(request, response);
    }

    private String parseBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}