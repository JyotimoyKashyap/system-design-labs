package com.jyotimoykashyap.splitwiselld.strategy;

import com.jyotimoykashyap.splitwiselld.models.Split;
import com.jyotimoykashyap.splitwiselld.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SplitStrategyTest {

    private User u1, u2, u3, u4, u5, u6;
    private EqualSplitStrategy equalStrategy;
    private ExactSplitStrategy exactStrategy;
    private PercentSplitStrategy percentStrategy;

    @BeforeEach
    void setUp() {
        u1 = User.builder().id("u1").name("User 1").email("u1@test.com").build();
        u2 = User.builder().id("u2").name("User 2").email("u2@test.com").build();
        u3 = User.builder().id("u3").name("User 3").email("u3@test.com").build();
        u4 = User.builder().id("u4").name("User 4").email("u4@test.com").build();
        u5 = User.builder().id("u5").name("User 5").email("u5@test.com").build();
        u6 = User.builder().id("u6").name("User 6").email("u6@test.com").build();

        equalStrategy = new EqualSplitStrategy();
        exactStrategy = new ExactSplitStrategy();
        percentStrategy = new PercentSplitStrategy();
    }

    @Test
    @DisplayName("EQUAL: 100 split 3 ways distributes 1-cent remainder to first user (33.34, 33.33, 33.33)")
    void testEqualSplitWithThreeUsers() {
        List<SplitRequest> requests = List.of(
                SplitRequest.equal(u1),
                SplitRequest.equal(u2),
                SplitRequest.equal(u3)
        );

        List<Split> splits = equalStrategy.calculateSplits(new BigDecimal("100.00"), requests);

        assertThat(splits).hasSize(3);
        assertThat(splits.get(0).amount()).isEqualByComparingTo("33.34");
        assertThat(splits.get(1).amount()).isEqualByComparingTo("33.33");
        assertThat(splits.get(2).amount()).isEqualByComparingTo("33.33");

        // Zero financial drift assertion:
        BigDecimal total = splits.stream().map(Split::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(total).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("EQUAL: 100 split 6 ways distributes 4 remainder cents across first 4 users")
    void testEqualSplitWithSixUsers() {
        List<SplitRequest> requests = List.of(
                SplitRequest.equal(u1), SplitRequest.equal(u2), SplitRequest.equal(u3),
                SplitRequest.equal(u4), SplitRequest.equal(u5), SplitRequest.equal(u6)
        );

        List<Split> splits = equalStrategy.calculateSplits(new BigDecimal("100.00"), requests);

        // 100 / 6 = 16.66 base * 6 = 99.96 -> 4 remainder cents
        assertThat(splits.get(0).amount()).isEqualByComparingTo("16.67");
        assertThat(splits.get(1).amount()).isEqualByComparingTo("16.67");
        assertThat(splits.get(2).amount()).isEqualByComparingTo("16.67");
        assertThat(splits.get(3).amount()).isEqualByComparingTo("16.67");
        assertThat(splits.get(4).amount()).isEqualByComparingTo("16.66");
        assertThat(splits.get(5).amount()).isEqualByComparingTo("16.66");

        BigDecimal total = splits.stream().map(Split::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(total).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("EXACT: Throws exception when split amounts do not sum to total")
    void testExactSplitMismatch() {
        List<SplitRequest> requests = List.of(
                SplitRequest.exact(u1, new BigDecimal("40.00")),
                SplitRequest.exact(u2, new BigDecimal("50.00"))
        );

        assertThatThrownBy(() -> exactStrategy.calculateSplits(new BigDecimal("100.00"), requests))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match total amount");
    }

    @Test
    @DisplayName("PERCENT: Throws exception when percentages do not equal 100.00%")
    void testPercentSplitMismatch() {
        List<SplitRequest> requests = List.of(
                SplitRequest.percent(u1, new BigDecimal("50.00")),
                SplitRequest.percent(u2, new BigDecimal("40.00"))
        );

        assertThatThrownBy(() -> percentStrategy.calculateSplits(new BigDecimal("100.00"), requests))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Total percentage must sum to 100.00%");
    }
}