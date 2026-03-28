package com.suhyun444.lifehub.aircon;

import com.suhyun444.lifehub.aircon.AirconDto;
import com.suhyun444.lifehub.aircon.Aircon;
import com.suhyun444.lifehub.aircon.AirconRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AirconServiceTest {

    @Mock
    private AirconRepository airconRepository;

    @InjectMocks
    private AirconService airconService;

    // --- 1. init() 테스트 ---
    @Test
    @DisplayName("init: DB에 에어컨 데이터가 없으면 새로 생성한다")
    void init_CreatesNewAircon_WhenNotExists() {
        when(airconRepository.existsById(1L)).thenReturn(false);
        airconService.init();
        verify(airconRepository, times(1)).save(any(Aircon.class));
    }

    @Test
    @DisplayName("init: DB에 에어컨 데이터가 이미 존재하면 생성하지 않는다")
    void init_DoesNothing_WhenExists() {
        when(airconRepository.existsById(1L)).thenReturn(true);
        airconService.init();
        verify(airconRepository, never()).save(any(Aircon.class));
    }

    // --- 2. getTemperature() 테스트 ---
    @Test
    @DisplayName("getTemperature: 현재 온도를 정상적으로 반환한다")
    void getTemperature_Success() {
        Aircon mockAircon = new Aircon(1L, 24);
        when(airconRepository.findById(1L)).thenReturn(Optional.of(mockAircon));

        AirconDto result = airconService.getTemperature();

        assertEquals(24, result.temperature());
    }

    @Test
    @DisplayName("getTemperature: DB에 데이터가 없으면 예외를 던진다")
    void getTemperature_ThrowsException_WhenNotFound() {
        when(airconRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> airconService.getTemperature());
    }

    // --- 3. increaseTemperature() 테스트 ---
    @Test
    @DisplayName("increaseTemperature: 온도를 1 올리고 저장한다")
    void increaseTemperature_Success() {
        Aircon mockAircon = new Aircon(1L, 20);
        when(airconRepository.findById(1L)).thenReturn(Optional.of(mockAircon));

        AirconDto result = airconService.increaseTemperature();

        assertEquals(21, result.temperature());
        verify(airconRepository, times(1)).saveAndFlush(mockAircon);
    }

    // --- 4. decreaseTemperature() 테스트 ---
    @Test
    @DisplayName("decreaseTemperature: 온도를 1 내리고 저장한다")
    void decreaseTemperature_Success() {
        Aircon mockAircon = new Aircon(1L, 20);
        when(airconRepository.findById(1L)).thenReturn(Optional.of(mockAircon));

        AirconDto result = airconService.decreaseTemperature();

        assertEquals(19, result.temperature());
        verify(airconRepository, times(1)).saveAndFlush(mockAircon);
    }

    // --- 5. resetTemperature() 테스트 ---
    @Test
    @DisplayName("resetTemperature: 온도를 다시 20도로 초기화한다")
    void resetTemperature_Success() {
        Aircon mockAircon = new Aircon(1L, 35); // 비정상적으로 높은 온도
        when(airconRepository.findById(1L)).thenReturn(Optional.of(mockAircon));

        airconService.resetTemperature();

        assertEquals(20, mockAircon.getTemperature());
        verify(airconRepository, times(1)).saveAndFlush(mockAircon);
    }
}