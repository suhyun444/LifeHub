package com.suhyun444.lifehub.marker;

import org.springframework.stereotype.Service;

import com.suhyun444.lifehub.User.UserRepository;
import com.suhyun444.lifehub.card.Component.SpendingAnalyzer;
import com.suhyun444.lifehub.card.Component.TransactionCategorizer;
import com.suhyun444.lifehub.card.Repository.AnalysisHistoryRepository;
import com.suhyun444.lifehub.card.Repository.TransactionRepository;

@Service
public class MarkerService {
    private final MarkerRepository markerRepository;
    private final UserRepository userRepository;
    public MarkerService(MarkerRepository markerRepository,
                        UserRepository userRepository
    ) {
        this.markerRepository = markerRepository;
        this.userRepository = userRepository;
    }   
}
