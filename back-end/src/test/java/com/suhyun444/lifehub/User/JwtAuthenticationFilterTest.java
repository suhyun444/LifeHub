package com.suhyun444.lifehub.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("doFilterInternal: 유효한 토큰이 헤더에 있으면 SecurityContext에 인증 정보를 저장해야 한다.")
    void doFilterInternal_ValidToken() throws Exception {
        // given
        String token = "valid-token";
        String bearerToken = "Bearer " + token;
        String userEmail = "test@example.com";

        // 1. 헤더 모킹
        given(request.getHeader("Authorization")).willReturn(bearerToken);
        
        // 2. 토큰 검증 성공 모킹
        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        
        // [수정 포인트] getAuthentication 대신 유저 ID(이메일) 추출 메서드를 모킹
        given(jwtTokenProvider.getUserIdFromToken(token)).willReturn(userEmail);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        // 1. 컨텍스트에 인증 정보가 설정되었는지 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        // 2. 생성된 인증 객체의 주체(Principal)가 우리가 설정한 이메일과 같은지 확인
        assertThat(auth.getPrincipal()).isEqualTo(userEmail);
        
        // 3. 필터 체인이 계속 진행되었는지 확인
        verify(filterChain).doFilter(request, response);
        
        // *테스트 후 정리
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal: 토큰이 없거나 유효하지 않으면 인증 설정 없이 필터를 통과해야 한다.")
    void doFilterInternal_InvalidOrEmptyToken() throws Exception {
        // given
        // 케이스 1: 헤더가 null인 경우
        given(request.getHeader("Authorization")).willReturn(null);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        // 1. 인증 정보가 없어야 함
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        
        // 2. 토큰 검증 로직이 호출되지 않았어야 함
        verify(jwtTokenProvider, never()).validateToken(anyString());
        
        // 3. 필터 체인은 진행되어야 함
        verify(filterChain).doFilter(request, response);
    }
}