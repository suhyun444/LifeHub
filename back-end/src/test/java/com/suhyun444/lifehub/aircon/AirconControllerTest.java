package com.suhyun444.lifehub.aircon;

import com.suhyun444.lifehub.User.CustomOAuth2UserService;
import com.suhyun444.lifehub.User.JwtTokenProvider;
import com.suhyun444.lifehub.User.OAuth2SuccessHandler;
import com.suhyun444.lifehub.aircon.AirconDto;
import com.suhyun444.lifehub.aircon.AirconService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AirconController.class)
@MockitoBean(types = JpaMetamodelMappingContext.class)
class AirconControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // SecurityConfig 로드를 위한 필수 Mock 빈들 (CardControllerTest와 동일 규격)
    @MockitoBean private CustomOAuth2UserService customOAuth2UserService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private AirconService airconService;

    @MockitoBean
    private AirconOptimisticLockFacade airconFacade;

    // --- 1. GET /api/aircon ---
    @Test
    @DisplayName("getAircon: 현재 에어컨 온도를 조회한다")
    @WithMockUser
    void getAircon_ReturnsCurrentTemp() throws Exception {
        given(airconService.getTemperature()).willReturn(new AirconDto(20));

        mockMvc.perform(get("/api/aircon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(20));
    }

    // --- 2. POST /api/aircon/up ---
    @Test
    @DisplayName("up: 에어컨 온도를 올린다")
    @WithMockUser
    void up_IncreasesTemp() throws Exception {
        given(airconFacade.increaseTemperatureWithRetry()).willReturn(new AirconDto(21));

        mockMvc.perform(post("/api/aircon/up").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(21));
    }

    // --- 3. POST /api/aircon/down ---
    @Test
    @DisplayName("down: 에어컨 온도를 내린다")
    @WithMockUser
    void down_DecreasesTemp() throws Exception {
        given(airconFacade.decreaseTemperatureWithRetry()).willReturn(new AirconDto(19));

        mockMvc.perform(post("/api/aircon/down").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(19));
    }

    // --- 4. POST /api/aircon/reset ---
    @Test
    @DisplayName("reset: 에어컨 온도를 초기화한다")
    @WithMockUser
    void reset_ResetsTemp() throws Exception {
        doNothing().when(airconService).resetTemperature();

        mockMvc.perform(post("/api/aircon/reset").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reset to 20°C"));
        
        verify(airconService).resetTemperature();
    }
}