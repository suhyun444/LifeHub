package com.suhyun444.lifehub.card.Component.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suhyun444.lifehub.card.DTO.AnalysisDto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class TrendListConverter implements AttributeConverter<List<AnalysisDto.Trend>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1. DB에 저장할 때: List -> String(JSON)
    @Override
    public String convertToDatabaseColumn(List<AnalysisDto.Trend> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]"; // 빈 리스트면 빈 배열 JSON 저장
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }

    // 2. DB에서 읽어올 때: String(JSON) -> List
    @Override
    public List<AnalysisDto.Trend> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<AnalysisDto.Trend>>() {});
        } catch (JsonProcessingException e) {
            // 파싱 실패 시 빈 리스트 반환 (안전장치)
            return new ArrayList<>();
        }
    }
}