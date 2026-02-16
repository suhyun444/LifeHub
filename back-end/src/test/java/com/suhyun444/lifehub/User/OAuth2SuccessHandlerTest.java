package com.suhyun444.lifehub.User;

import com.suhyun444.lifehub.card.Entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private Authentication authentication;

    @InjectMocks
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Test
    @DisplayName("onAuthenticationSuccess: 로그인 성공 시 토큰을 생성하고 프론트엔드로 리다이렉트한다.")
    void onAuthenticationSuccess_RedirectsWithToken() throws IOException, jakarta.servlet.ServletException {
        String generatedToken = "access.token.jwt";
        
        User user = new User();
        user.setEmail("test@example.com");

        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        given(customOAuth2User.getUser()).willReturn(user);

        given(authentication.getPrincipal()).willReturn(customOAuth2User);

        given(jwtTokenProvider.generateToken(any(User.class))).willReturn(generatedToken);

        given(response.encodeRedirectURL(anyString())).willAnswer(invocation -> invocation.getArgument(0));

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect(contains("?token=" + generatedToken));
    }
}