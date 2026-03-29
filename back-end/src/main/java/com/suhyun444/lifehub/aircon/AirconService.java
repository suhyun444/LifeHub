package com.suhyun444.lifehub.aircon;

import com.suhyun444.lifehub.aircon.Aircon;
import com.suhyun444.lifehub.aircon.AirconDto;
import com.suhyun444.lifehub.aircon.AirconRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AirconService {

    private final AirconRepository airconRepository;

    public AirconService(AirconRepository airconRepository) {
        this.airconRepository = airconRepository;
    }

    // 서버 시작 시 DB에 공용 에어컨(id=1, 온도 20도)이 없으면 생성해두는 초기화 로직
    @PostConstruct
    public void init() {
        if (!airconRepository.existsById(1L)) {
            airconRepository.save(new Aircon(1L, 20));
        }
    }

    @Transactional(readOnly = true)
    public AirconDto getTemperature() {
        Aircon aircon = airconRepository.findById(1L).orElseThrow();
        return new AirconDto(aircon.getTemperature());
    }

    @Transactional
    public AirconDto increaseTemperature() {
        Aircon aircon = airconRepository.findById(1L).orElseThrow();
        aircon.setTemperature(aircon.getTemperature() + 1);
        return new AirconDto(aircon.getTemperature());
    }

    @Transactional
    public AirconDto decreaseTemperature() {
        Aircon aircon = airconRepository.findById(1L).orElseThrow();
        aircon.setTemperature(aircon.getTemperature() - 1);
        return new AirconDto(aircon.getTemperature());
    }
    
    // 테스트 초기화를 위한 온도 리셋 기능
    @Transactional
    public void resetTemperature() {
        Aircon aircon = airconRepository.findById(1L).orElseThrow();
        aircon.setTemperature(20);
    }
}