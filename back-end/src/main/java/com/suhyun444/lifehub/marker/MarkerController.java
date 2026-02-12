package com.suhyun444.lifehub.marker;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import com.suhyun444.lifehub.marker.DTO.LinkDto;
import com.suhyun444.lifehub.marker.DTO.MarkerDto;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
public class MarkerController {

    private final MarkerService markerService;
    MarkerController(MarkerService markerService)
    {
        this.markerService = markerService;
    }
    
    @PostMapping("api/markers")
    public ResponseEntity<Long> createMarker(@AuthenticationPrincipal String email,@RequestBody MarkerDto markerDto) {
        
        return ResponseEntity.ok(markerService.createMarker(email, markerDto));
    }
    
    @GetMapping("api/markers")
    public ResponseEntity<List<MarkerDto>> getMarkers(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(markerService.GetMarkers(email));
    }

    @DeleteMapping("api/markers/{markerId}")
    public ResponseEntity<?> deleteMarker(@PathVariable Long markerId) 
    {
        markerService.deleteMarker(markerId);
        return ResponseEntity.ok(Map.of("message","Success to delete"));
    }
    
    @PostMapping("api/markers/{markerId}/links")
    public ResponseEntity<Long> addLink(@PathVariable Long markerId, @RequestBody LinkDto linkDto) {
        return ResponseEntity.ok(markerService.addLink(markerId, linkDto));
    }
    @DeleteMapping("api/links/{linkId}")
    public ResponseEntity<?> deleteLink(@PathVariable Long linkId) 
    {
        markerService.deleteLink(linkId);
        return ResponseEntity.ok(Map.of("message","Success to delete"));
    }
    @PatchMapping("/{markerId}/move")
    public ResponseEntity<Void> moveMarker(@AuthenticationPrincipal String email,@PathVariable Long markerId, @RequestBody Map<String, Long> request) {
        markerService.moveMarker(email,markerId, request.get("newOrder"));
        return ResponseEntity.ok().build();
    }
}