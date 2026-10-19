package com.jyotimoykashyap.splitwiselld.strategy;

import com.jyotimoykashyap.splitwiselld.models.Split;
import com.jyotimoykashyap.splitwiselld.models.SplitType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ExactSplitStrategy implements SplitStrategy {
    @Override
    public SplitType getSplitType() {
        return SplitType.EXACT;
    }

    @Override
    public List<Split> calculateSplits(BigDecimal totalAmount, List<SplitRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Requests lists cannot be empty for EXACT split");
        }

        BigDecimal sum = BigDecimal.ZERO;
        List<Split> splits = new ArrayList<>();

        for (SplitRequest req : requests) {
            if (req.amount() == null || req.amount().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Exact amount must be specified and non-negative for " + req.user().getId());
            }

            BigDecimal scaledAmount = req.amount().setScale(2, RoundingMode.HALF_UP);
            sum = sum.add(scaledAmount);
            splits.add(new Split(req.user(), scaledAmount));
        }

        if (sum.compareTo(totalAmount.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new IllegalArgumentException(
                    String.format("Exact split sum (%s) does not match total amount (%s)", sum, totalAmount)
            );
        }

        return splits;
    }
}
