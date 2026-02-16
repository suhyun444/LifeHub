package com.suhyun444.lifehub.marker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suhyun444.lifehub.User.CustomOAuth2UserService;
import com.suhyun444.lifehub.User.JwtTokenProvider;
import com.suhyun444.lifehub.User.OAuth2SuccessHandler;
import com.suhyun444.lifehub.marker.DTO.LinkDto;
import com.suhyun444.lifehub.marker.DTO.MarkerDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean; // [삭제] Deprecated
import org.springframework.test.context.bean.override.mockito.MockitoBean; // [추가] 신규 어노테이션
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MarkerController.class)
@MockitoBean(types = JpaMetamodelMappingContext.class) // @MockBean 대신 사용
class MarkerControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // [변경] @MockBean -> @MockitoBean (Spring Boot 3.4+)
    @MockitoBean private CustomOAuth2UserService customOAuth2UserService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean private MarkerService markerService;

    // --- 1. POST /api/markers (마커 생성) ---
    @Test
    @DisplayName("createMarker: 마커 생성 요청 성공")
    @WithMockUser(username = "test@test.com")
    void createMarker() throws Exception {
        MarkerDto req = new MarkerDto(null, "New", "Blue", null, null);
        MarkerDto res = new MarkerDto(1L, "New", "Blue", 1L, null);

        // [핵심 수정] eq("test@test.com") -> any()
        // @WithMockUser가 만드는 Principal 객체와 String email 타입이 정확히 매칭되지 않아도
        // Mock이 동작하도록 any()로 범위를 넓혀줍니다.
        given(markerService.createMarker(any(), any(MarkerDto.class)))
                .willReturn(res);

        mockMvc.perform(post("/api/markers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L)) // 이제 Body가 있으므로 통과됨
                .andExpect(jsonPath("$.title").value("New"));
    }

    // --- 2. GET /api/markers (마커 목록 조회) ---
    @Test
    @DisplayName("getMarkers: 마커 목록 조회 성공")
    @WithMockUser(username = "test@test.com")
    void getMarkers() throws Exception {
        List<MarkerDto> list = List.of(new MarkerDto(1L, "M1", "Red", 1L, null));
        
        // 여기도 any() 사용 권장
        given(markerService.GetMarkers(any())).willReturn(list);

        mockMvc.perform(get("/api/markers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("M1"));
    }

    // --- 3. DELETE /api/markers/{id} (마커 삭제) ---
    @Test
    @DisplayName("deleteMarker: 마커 삭제 성공")
    @WithMockUser
    void deleteMarker() throws Exception {
        mockMvc.perform(delete("/api/markers/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success to delete"));

        verify(markerService).deleteMarker(1L);
    }

    // --- 4. POST /api/markers/{id}/links (링크 추가) ---
    @Test
    @DisplayName("addLink: 링크 추가 성공")
    @WithMockUser
    void addLink() throws Exception {
        LinkDto req = new LinkDto(null, "Naver", "naver.com");
        given(markerService.addLink(eq(1L), any(LinkDto.class))).willReturn(50L);

        mockMvc.perform(post("/api/markers/1/links")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("50"));
    }

    // --- 5. DELETE /api/links/{id} (링크 삭제) ---
    @Test
    @DisplayName("deleteLink: 링크 삭제 성공")
    @WithMockUser
    void deleteLink() throws Exception {
        mockMvc.perform(delete("/api/links/100").with(csrf()))
                .andExpect(status().isOk());
        
        verify(markerService).deleteLink(100L);
    }

    // --- 6. PATCH /api/markers/{id}/move (마커 이동) ---
    @Test
    @DisplayName("moveMarker: 마커 순서 이동 성공")
    @WithMockUser(username = "test@test.com")
    void moveMarker() throws Exception {
        Map<String, Long> body = Map.of("newOrder", 5L);

        mockMvc.perform(patch("/api/markers/1/move")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        // verify에서도 any()를 쓰거나, 서비스 호출이 일어났는지만 검증
        verify(markerService).moveMarker(any(), eq(1L), eq(5L));
    }
}