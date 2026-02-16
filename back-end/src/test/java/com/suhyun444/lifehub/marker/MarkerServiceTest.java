package com.suhyun444.lifehub.marker;

import com.suhyun444.lifehub.User.UserRepository;
import com.suhyun444.lifehub.card.Entity.User;
import com.suhyun444.lifehub.marker.DTO.LinkDto;
import com.suhyun444.lifehub.marker.DTO.MarkerDto;
import com.suhyun444.lifehub.marker.Entity.Link;
import com.suhyun444.lifehub.marker.Entity.Marker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MarkerServiceTest {

    @Mock
    private MarkerRepository markerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LinkRepository linkRepository;

    @InjectMocks
    private MarkerService markerService;

    @Test
    @DisplayName("createMarker: (성공) 기존 마커가 있을 때, MaxOrder+1로 생성되어야 한다.")
    void createMarker_Success() {
        // given
        String email = "test@test.com";
        User user = new User(); user.setId(1L);
        MarkerDto dto = new MarkerDto(null, "새마커", "#FFFFFF", null, null);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(markerRepository.findMaxSortOrder(user.getId())).willReturn(Optional.of(5L));
        
        given(markerRepository.save(any(Marker.class))).willAnswer(invocation -> {
            Marker m = invocation.getArgument(0);
            m.setId(10L); // ID 생성
            return m;
        });

        MarkerDto result = markerService.createMarker(email, dto);

        assertThat(result.getTitle()).isEqualTo("새마커");
        assertThat(result.getSortOrder()).isEqualTo(6L); // 5 + 1 = 6
    }

@Test
    @DisplayName("createMarker: (신규 유저) 마커가 하나도 없을 때(Max=0), 순서 1번으로 생성된다.")
    void createMarker_FirstTime() {
        // given
        String email = "new@test.com";
        User user = new User(); user.setId(2L);
        MarkerDto dto = new MarkerDto(null, "First Marker", "#FFFFFF", null, null);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        // 마커가 없어서 0L 반환 (orElse(0L) 로직 대응)
        given(markerRepository.findMaxSortOrder(user.getId())).willReturn(Optional.of(0L)); // 혹은 Empty로 주고 서비스 로직 검증해도 됨. 
        // *참고: Repository가 실제로는 Optional.empty()를 줄 수도 있지만, 
        // 서비스에서 .orElse(0L) 처리했으므로 
        // Mock에서는 0L을 리턴한다고 가정하거나, Optional.empty()를 리턴하고 서비스가 잘 처리하는지 봐도 됨.
        // 가장 정확한 Mock은:
        given(markerRepository.findMaxSortOrder(user.getId())).willReturn(Optional.empty()); 
        
        given(markerRepository.save(any(Marker.class))).willAnswer(inv -> {
            Marker m = inv.getArgument(0);
            m.setId(20L);
            return m;
        });

        // when
        MarkerDto result = markerService.createMarker(email, dto);

        // then
        assertThat(result.getSortOrder()).isEqualTo(1L); // 0 + 1 = 1번 순서 확인
    }


    @Test
    @DisplayName("addLink: (성공) 마커에 링크를 추가하고 ID를 반환해야 한다.")
    void addLink_Success() {
        Long markerId = 1L;
        LinkDto linkDto = new LinkDto(null, "구글", "https://google.com");
        Marker marker = new Marker();
        marker.setId(markerId);
        marker.setLinks(new ArrayList<>());

        given(markerRepository.findById(markerId)).willReturn(Optional.of(marker));
        given(linkRepository.save(any(Link.class))).willAnswer(inv -> {
            Link l = inv.getArgument(0);
            l.setId(100L);
            return l;
        });

        Long linkId = markerService.addLink(markerId, linkDto);

        assertThat(linkId).isEqualTo(100L);
        assertThat(marker.getLinks()).hasSize(1);
    }

    @Test
    @DisplayName("addLink: (실패) 존재하지 않는 마커에는 링크를 추가할 수 없다.")
    void addLink_MarkerNotFound() {
        given(markerRepository.findById(999L)).willReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            markerService.addLink(999L, new LinkDto());
        });
    }


    @Test
    @DisplayName("moveMarker: (아래로 이동) 현재 순서(1) > 새 순서(3)일 때, 사이 값들을 앞으로(-1) 당겨야 한다.")
    void moveMarker_MoveDown() {
        // given
        String email = "test@test.com";
        User user = new User(); user.setId(1L);
        Long markerId = 10L;
        Long currentOrder = 1L;
        Long newOrder = 3L;

        Marker marker = new Marker();
        marker.setId(markerId);
        marker.setSortOrder(currentOrder);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(markerRepository.findById(markerId)).willReturn(Optional.of(marker));

        markerService.moveMarker(email, markerId, newOrder);

        verify(markerRepository).shiftOrdersDecrement(user.getId(), currentOrder, newOrder);
        assertThat(marker.getSortOrder()).isEqualTo(newOrder);
        verify(markerRepository).save(marker);
    }

    @Test
    @DisplayName("moveMarker: (위로 이동) 현재 순서(3) > 새 순서(1)일 때, 사이 값들을 뒤로(+1) 밀어야 한다.")
    void moveMarker_MoveUp() {
        String email = "test@test.com";
        User user = new User(); user.setId(1L);
        Long markerId = 10L;
        Long currentOrder = 3L;
        Long newOrder = 1L;

        Marker marker = new Marker();
        marker.setSortOrder(currentOrder);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(markerRepository.findById(markerId)).willReturn(Optional.of(marker));

        markerService.moveMarker(email, markerId, newOrder);

        verify(markerRepository).shiftOrdersIncrement(user.getId(), newOrder, currentOrder);
        assertThat(marker.getSortOrder()).isEqualTo(newOrder);
    }
    @Test
    @DisplayName("GetMarkers: (성공) 유저의 마커 목록을 조회하여 DTO 리스트로 반환한다.")
    void getMarkers_Success() {
        String email = "test@test.com";
        User user = new User(); user.setId(1L);
        
        Marker m1 = new Marker("M1", "Red", user, 2L);
        Marker m2 = new Marker("M2", "Blue", user, 1L);
        List<Marker> markers = List.of(m1, m2);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(markerRepository.findAllByUserIdOrderBySortOrderDesc(user.getId()))
                .willReturn(markers);

        List<MarkerDto> results = markerService.GetMarkers(email);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getTitle()).isEqualTo("M1");
        assertThat(results.get(1).getTitle()).isEqualTo("M2");
    }

    @Test
    @DisplayName("GetMarkers: (실패) 존재하지 않는 유저 이메일로 조회 시 예외가 발생해야 한다.")
    void getMarkers_UserNotFound() {
        String email = "unknown@test.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> {
            markerService.GetMarkers(email);
        });
    }


    @Test
    @DisplayName("deleteMarker: (성공) 마커 ID로 삭제 메서드가 호출되어야 한다.")
    void deleteMarker_Success() {
        Long markerId = 1L;

        markerService.deleteMarker(markerId);

        verify(markerRepository).deleteById(markerId);
    }


    @Test
    @DisplayName("deleteLink: (성공) 링크 ID로 삭제 메서드가 호출되어야 한다.")
    void deleteLink_Success() {
        Long linkId = 100L;

        markerService.deleteLink(linkId);

        verify(linkRepository).deleteById(linkId);
    }
}