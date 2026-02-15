package com.suhyun444.lifehub.card;

import com.suhyun444.lifehub.card.DTO.PaymentStatus;
import com.suhyun444.lifehub.card.Entity.Transaction;
import com.suhyun444.lifehub.card.Parser.KookminTransactionParser;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KookminTransactionParserTest {

    private final KookminTransactionParser parser = new KookminTransactionParser();

    @Test
    @DisplayName("parse: 정상적인 엑셀 시트 데이터를 Transaction 객체 리스트로 변환해야 한다.")
    void parse_ValidSheet() {
        // given
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        
        // 국민은행 포맷에 맞춰 더미 데이터 생성 (헤더 등 앞부분 5줄 스킵 고려)
        for (int i = 0; i < 5; i++) sheet.createRow(i); // 0~4행 스킵

        Row row = sheet.createRow(5); // 5행부터 데이터
        row.createCell(0).setCellValue("2024.02.14 12:00:00"); // 날짜
        row.createCell(2).setCellValue("멋진상점"); // 상점명
        row.createCell(4).setCellValue("10,000"); // 금액 (콤마 포함 문자열)
        row.createCell(7).setCellValue("체크카드"); // 결제수단

        sheet.createRow(6);


        // when
        List<Transaction> result = parser.parse(sheet);

        // then
        assertThat(result).hasSize(1);
        Transaction t = result.get(0);
        assertThat(t.getMerchant()).isEqualTo("멋진상점");
        assertThat(t.getAmount()).isEqualTo(10000);
        assertThat(t.getStatus()).isEqualTo(PaymentStatus.completed);
        assertThat(t.getDate()).isEqualTo("2024.02.14 12:00:00");
    }

    @Test
    @DisplayName("parse: 출금액이 0원인 행은 무시해야 한다.")
    void parse_SkipZeroAmount() {
        // given
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        for (int i = 0; i < 5; i++) sheet.createRow(i);

        Row row = sheet.createRow(5);
        row.createCell(0).setCellValue("2024.02.14");
        row.createCell(2).setCellValue("취소건");
        row.createCell(4).setCellValue("0"); // 금액 0
        row.createCell(7).setCellValue("카드");

        // when
        List<Transaction> result = parser.parse(sheet);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("parse: TransactionKey(중복 방지 키)가 공백 없이 올바르게 생성되어야 한다.")
    void parse_TransactionKeyGeneration() {
        // given
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        for (int i = 0; i < 5; i++) sheet.createRow(i);

        Row row = sheet.createRow(5);
        row.createCell(0).setCellValue("2024.02.14");
        row.createCell(2).setCellValue("스타벅스 동백점"); // 공백 포함
        row.createCell(4).setCellValue("5,000");
        row.createCell(7).setCellValue("카드");
        
        sheet.createRow(6);

        List<Transaction> result = parser.parse(sheet);

        String key = result.get(0).getTransactionKey();
        assertThat(key).isEqualTo("2024.02.14_5000_스타벅스동백점");
    }
}