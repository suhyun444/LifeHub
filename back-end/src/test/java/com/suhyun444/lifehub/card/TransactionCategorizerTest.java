package com.suhyun444.lifehub.card;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.suhyun444.lifehub.card.Component.KeywordProvider;
import com.suhyun444.lifehub.card.Component.TransactionCategorizer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TransactionCategorizerTest {

    @Mock
    private KeywordProvider keywordProvider;

    private TransactionCategorizer transactionCategorizer;

    @BeforeEach
    void setUp() {
        // 가짜 키워드 맵 설정
        Map<String, String> mockMap = new HashMap<>();
        mockMap.put("스타벅스", "식비");
        mockMap.put("스타벅스 강남점", "데이트"); // 더 긴 키워드
        mockMap.put("편의점", "생필품");
        
        given(keywordProvider.getKeywordMap()).willReturn(mockMap);
        
        transactionCategorizer = new TransactionCategorizer(keywordProvider);
    }

    @Test
    @DisplayName("getCategory: DB(이전 내역)에 매칭되는 결과가 있다면, 키워드 맵보다 우선해야 한다.")
    void getCategory_DatabasePriority() {
        // given
        String merchant = "스타벅스 역삼점";
        Optional<String> dbResult = Optional.of("커피"); // DB에는 '커피'로 저장되어 있음

        // when
        String result = transactionCategorizer.getCategory(merchant, dbResult);

        // then
        assertThat(result).isEqualTo("커피"); // '식비'가 아니라 '커피'여야 함
    }

    @Test
    @DisplayName("getCategory: DB 결과가 없을 때, 키워드 맵에 포함된 단어가 있으면 해당 카테고리를 반환해야 한다.")
    void getCategory_KeywordMatch() {
        // given
        String merchant = "CU 편의점";
        Optional<String> dbResult = Optional.empty();

        // when
        String result = transactionCategorizer.getCategory(merchant, dbResult);

        // then
        assertThat(result).isEqualTo("생필품");
    }

    @Test
    @DisplayName("getCategory: 키워드 매칭 시, 더 긴 단어가 우선순위를 가져야 한다. (Longest Match Rule)")
    void getCategory_LongestMatch() {
        // given
        // "스타벅스"(식비) vs "스타벅스 강남점"(데이트)
        String merchant = "이번주는 스타벅스 강남점에서 만남"; 
        Optional<String> dbResult = Optional.empty();

        // when
        String result = transactionCategorizer.getCategory(merchant, dbResult);

        // then
        // '식비'가 아니라 더 구체적인 '데이트'로 분류되어야 함
        assertThat(result).isEqualTo("데이트");
    }

    @Test
    @DisplayName("getCategory: DB에도 없고 키워드 맵에도 없으면 '기타'를 반환해야 한다.")
    void getCategory_NoMatch() {
        // given
        String merchant = "알 수 없는 상점";
        Optional<String> dbResult = Optional.empty();

        // when
        String result = transactionCategorizer.getCategory(merchant, dbResult);

        // then
        assertThat(result).isEqualTo("기타");
    }
}