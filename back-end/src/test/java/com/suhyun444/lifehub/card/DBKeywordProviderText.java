package com.suhyun444.lifehub.card;

import com.suhyun444.lifehub.card.Component.DBKeywordProvider;
import com.suhyun444.lifehub.card.Entity.Keyword;
import com.suhyun444.lifehub.card.Repository.KeywordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DBKeywordProviderTest {

    @Mock
    private KeywordRepository keywordRepository;

    @Test
    @DisplayName("Constructor: Repository의 모든 키워드를 조회하여 Map으로 변환해야 한다.")
    void constructor_LoadsKeywords() {
        // given
        Keyword k1 = new Keyword("편의점", "생필품");
        Keyword k2 = new Keyword("택시", "교통");
        List<Keyword> keywords = Arrays.asList(k1, k2);

        given(keywordRepository.findAll()).willReturn(keywords);

        // when
        DBKeywordProvider provider = new DBKeywordProvider(keywordRepository);
        Map<String, String> map = provider.getKeywordMap();

        // then
        assertThat(map).hasSize(2);
        assertThat(map.get("편의점")).isEqualTo("생필품");
        assertThat(map.get("택시")).isEqualTo("교통");
    }
}