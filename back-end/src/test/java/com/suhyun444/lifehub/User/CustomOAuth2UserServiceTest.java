package com.suhyun444.lifehub.User;

import com.suhyun444.lifehub.card.Entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> mockDelegate; // 가짜 외부 서비스

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private OAuth2UserRequest oAuth2UserRequest;

    @Mock
    private OAuth2User oAuth2User;

    @BeforeEach
    void setUp() {
        // 테스트 시작 전 가짜 델리게이트를 주입
        customOAuth2UserService.setDelegate(mockDelegate);
    }

    @Test
    @DisplayName("loadUser: DB에 없는 새로운 이메일일 경우, 회원을 저장하고 결과(CustomOAuth2User)를 반환해야 한다.")
    void loadUser_NewUser_SavesUser() {
        // given
        String email = "new@example.com";
        Map<String, Object> attributes = Map.of("email", email);
        
        // 1. 외부 API(Delegate)가 가짜 유저 정보를 반환하도록 설정
        given(mockDelegate.loadUser(any())).willReturn(oAuth2User);
        given(oAuth2User.getAttribute("email")).willReturn(email);
        given(oAuth2User.getAttributes()).willReturn(attributes);
        
        // 2. DB에 해당 이메일이 없음 (신규 유저 시나리오)
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());
        
        // 3. save 호출 시 저장하려는 객체 그대로 반환
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        // then
        assertThat(result).isInstanceOf(CustomOAuth2User.class);
        verify(userRepository, times(1)).save(any(User.class)); // 저장이 1번 호출되었는지 검증
        assertThat(((CustomOAuth2User) result).getUser().getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("loadUser: 이미 DB에 존재하는 이메일일 경우, 저장하지 않고 기존 회원을 사용하여 반환해야 한다.")
    void loadUser_ExistingUser_ReturnsUser() {
        // given
        String email = "existing@example.com";
        User existingUser = new User(email);
        Map<String, Object> attributes = Map.of("email", email);

        // 1. 외부 API 설정
        given(mockDelegate.loadUser(any())).willReturn(oAuth2User);
        given(oAuth2User.getAttribute("email")).willReturn(email);
        given(oAuth2User.getAttributes()).willReturn(attributes);

        // 2. DB에 이미 존재함
        given(userRepository.findByEmail(email)).willReturn(Optional.of(existingUser));

        // when
        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        // then
        verify(userRepository, never()).save(any(User.class)); // 저장이 호출되지 않아야 함
        assertThat(((CustomOAuth2User) result).getUser()).isEqualTo(existingUser);
    }

    @Test
    @DisplayName("loadUser: OAuth2 공급자 연동 실패 시 예외가 그대로 전파되어야 한다.")
    void loadUser_ProviderFailure_ThrowsException() {
        // given
        // 외부 서비스 호출 실패 시뮬레이션
        given(mockDelegate.loadUser(any())).willThrow(new OAuth2AuthenticationException("Provider Error"));

        // when & then
        assertThrows(OAuth2AuthenticationException.class, () -> {
            customOAuth2UserService.loadUser(oAuth2UserRequest);
        });
        
        // DB 로직까지 도달하지 않아야 함
        verify(userRepository, never()).findByEmail(anyString());
    }
    @Test
    @DisplayName("loadUser: OAuth2 공급자가 이메일을 주지 않는 경우 예외가 발생해야 한다.")
    void loadUser_NoEmail_ThrowsException() {
        Map<String, Object> dummyAttributes = Map.of("id", "12345"); 
        
        given(mockDelegate.loadUser(any())).willReturn(oAuth2User);
        given(oAuth2User.getAttribute("email")).willReturn(null);

        // when & then
        assertThrows(OAuth2AuthenticationException.class, () -> {
            customOAuth2UserService.loadUser(oAuth2UserRequest);
        });
    }
}