package com.suhyun444.lifehub.card.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.suhyun444.lifehub.card.Entity.Keyword;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword,String> {
    
}
