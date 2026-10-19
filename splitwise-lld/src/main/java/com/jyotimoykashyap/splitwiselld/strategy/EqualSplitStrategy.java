package com.jyotimoykashyap.splitwiselld.strategy;

import com.jyotimoykashyap.splitwiselld.models.Split;
import com.jyotimoykashyap.splitwiselld.models.SplitType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class EqualSplitStrategy implements SplitStrategy {
    @Override
    public SplitType getSplitType() {
        return SplitType.EQUAL;
    }

    @Override
    public List<Split> calculateSplits(BigDecimal totalAmount, List<SplitRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Requests list cannot be empty for EQUAL split");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be greater than zero");
        }

        int count = requests.size();
        BigDecimal baseShare = totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.FLOOR);
        BigDecimal totalCalculated = baseShare.multiply(BigDecimal.valueOf(count));
        BigDecimal remainder = totalAmount.subtract(totalCalculated);

        // convert remainder to integer cents (e.g 0.02 -> 2 cents to distribute)
        int remainderCents = remainder.movePointRight(2).intValueExact();

        List<Split> splits = new ArrayList<>();
        for (int i=0; i<count; i++) {
            BigDecimal userAmount = baseShare;

            if (i < remainderCents) {
                userAmount = userAmount.add(new BigDecimal("0.01"));
            }
            splits.add(new Split(requests.get(i).user(), userAmount));
        }

        return splits;
    }
}
