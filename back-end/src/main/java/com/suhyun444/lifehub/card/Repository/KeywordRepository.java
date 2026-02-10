package com.suhyun444.lifehub;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.suhyun444.lifehub.Entity.Keyword;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword,String> {
    
}
