package com.suhyun444.lifehub.aircon;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AirconController {
    private final AirconService airconService;
    private final AirconOptimisticLockFacade airconFacade;

    public AirconController(AirconService airconService, AirconOptimisticLockFacade airconFacade) {
        this.airconService = airconService;
        this.airconFacade = airconFacade;
    }

    @GetMapping("api/aircon")
    public ResponseEntity<AirconDto> getAircon() {
        return ResponseEntity.ok(airconService.getTemperature());
    }

    @PostMapping("api/aircon/up")
    public ResponseEntity<AirconDto> up() throws InterruptedException{
        return ResponseEntity.ok(airconFacade.increaseTemperatureWithRetry());
    }

    @PostMapping("api/aircon/down")
    public ResponseEntity<AirconDto> down() throws InterruptedException{
        return ResponseEntity.ok(airconFacade.decreaseTemperatureWithRetry());
    }
    
    @PostMapping("api/aircon/reset")
    public ResponseEntity<?> reset() {
        airconService.resetTemperature();
        return ResponseEntity.ok(Map.of("message", "Reset to 20°C"));
    }
}
