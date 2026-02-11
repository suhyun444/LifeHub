package com.suhyun444.lifehub.marker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.suhyun444.lifehub.User.UserRepository;
import com.suhyun444.lifehub.card.Component.SpendingAnalyzer;
import com.suhyun444.lifehub.card.Component.TransactionCategorizer;
import com.suhyun444.lifehub.card.DTO.TransactionDto;
import com.suhyun444.lifehub.card.Entity.User;
import com.suhyun444.lifehub.card.Repository.AnalysisHistoryRepository;
import com.suhyun444.lifehub.card.Repository.TransactionRepository;
import com.suhyun444.lifehub.marker.DTO.LinkDto;
import com.suhyun444.lifehub.marker.DTO.MarkerDto;
import com.suhyun444.lifehub.marker.Entity.Link;
import com.suhyun444.lifehub.marker.Entity.Marker;

import jakarta.transaction.Transactional;

@Service
public class MarkerService {
    private final MarkerRepository markerRepository;
    private final UserRepository userRepository;
    private final LinkRepository linkRepository;
    
    public MarkerService(MarkerRepository markerRepository,
                        UserRepository userRepository,
                        LinkRepository linkRepository) 
    {
        this.markerRepository = markerRepository;
        this.userRepository = userRepository;
        this.linkRepository = linkRepository;
    }   

    @Transactional
    public Long createMarker(String email, MarkerDto dto) {
        User user = userRepository.findByEmail(email).orElseThrow();
        
        Marker marker = new Marker(dto.getTitle(), dto.getColor(), user);
        return markerRepository.save(marker).getId();
    }

    @Transactional
    public void deleteMarker(Long markerId)
    {
        markerRepository.deleteById(markerId);
    }

    @Transactional
    public Long addLink(Long markerId, LinkDto dto) {
        Marker marker = markerRepository.findById(markerId)
                .orElseThrow(() -> new IllegalArgumentException("없는 게임입니다."));

        Link link = new Link(null, dto.getTitle(), dto.getUrl(), marker);
        
        marker.getLinks().add(link); 

        return linkRepository.save(link).getId();
    }

    public List<MarkerDto> GetMarkers(String email)
    {
        User user = userRepository.findByEmail(email).orElseThrow();
        List<Marker> markers = markerRepository.findByUserIdOrderByIdDesc(user.getId());

        List<MarkerDto> results = markers.stream().map(MarkerDto::from).collect(Collectors.toList());
        return results;
    }

    @Transactional
    public void deleteLink(LinkDto linkDto)
    {
        Link link = linkRepository.findById(linkDto.getId()).orElseThrow();
        linkRepository.delete(link);
    }
}
