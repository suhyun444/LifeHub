package com.suhyun444.lifehub.marker;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class MarkerController {

    private final MarkerService markerService;
    MarkerController(MarkerService markerService)
    {
        this.markerService = markerService;
    }
    
    @GetMapping("api/user/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Object principal) {
        
        return ResponseEntity.ok(Map.of(
            "message", "Logged in successfully",
            "user", principal // 사용자 ID 또는 정보 반환
        ));
    }
}