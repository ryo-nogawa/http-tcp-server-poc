package com.example.http.tcp.server.poc.domain.service.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PaymentServiceImplTest {

    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PaymentServiceImpl();
    }

    @Nested
    class Approve {

        @Nested
        class 正常系 {

            @Test
            @DisplayName("Given: カード番号と金額が与えられたとき, When: approveを実行すると, Then: 承認メッセージと金額を返す")
            void returnsApprovalMessageWithAmount() {
                String result = service.approve("1234567890123456", "1000");
                assertEquals("PAYMENT APPROVED AMOUNT 1000", result);
            }

            @Test
            @DisplayName("Given: 異なる金額が与えられたとき, When: approveを実行すると, Then: 正しい金額が含まれたメッセージを返す")
            void includesCorrectAmountInMessage() {
                String result = service.approve("1234567890123456", "5000");
                assertEquals("PAYMENT APPROVED AMOUNT 5000", result);
            }

            @Test
            @DisplayName("Given: 異なるカード番号と金額が与えられたとき, When: approveを実行すると, Then: 正しいメッセージ形式で返される")
            void messageFormatIsCorrect() {
                String result = service.approve("9999999999999999", "999");
                assertEquals("PAYMENT APPROVED AMOUNT 999", result);
            }

            @Test
            @DisplayName("Given: ゼロ金額が与えられたとき, When: approveを実行すると, Then: 金額0を含むメッセージを返す")
            void worksWithZeroAmount() {
                String result = service.approve("1234567890123456", "0");
                assertEquals("PAYMENT APPROVED AMOUNT 0", result);
            }

            @Test
            @DisplayName("Given: 異なるカード番号が与えられたとき, When: 同じ金額でapproveを実行すると, Then: 同じメッセージを返す（カード番号は無視される）")
            void ignoresCardNumberParameter() {
                String result1 = service.approve("1111111111111111", "1000");
                String result2 = service.approve("2222222222222222", "1000");
                assertEquals(result1, result2);
            }
        }
    }
}
