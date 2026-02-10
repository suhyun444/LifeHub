package com.suhyun444.lifehub.card.Component;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.suhyun444.lifehub.card.Entity.Keyword;
import com.suhyun444.lifehub.card.Repository.KeywordRepository;

@Component
public class DBKeywordProvider implements KeywordProvider{
    private Map<String,String> keywordMap;
    public DBKeywordProvider(KeywordRepository keywordRepository)
    {
        this.keywordMap = keywordRepository.findAll().stream()
        .collect(Collectors.toMap(Keyword::getName, Keyword::getCategory));
    }
    @Override
    public Map<String,String> getKeywordMap()
    {
        return keywordMap;
    }
}
