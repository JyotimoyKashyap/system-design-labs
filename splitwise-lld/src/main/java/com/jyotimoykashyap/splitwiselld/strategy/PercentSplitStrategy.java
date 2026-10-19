package com.jyotimoykashyap.splitwiselld.strategy;

import com.jyotimoykashyap.splitwiselld.models.Split;
import com.jyotimoykashyap.splitwiselld.models.SplitType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class PercentSplitStrategy implements SplitStrategy {

    private static final BigDecimal HUNDRED = new BigDecimal("100.00");

    @Override
    public SplitType getSplitType() {
        return SplitType.PERCENT;
    }

    @Override
    public List<Split> calculateSplits(BigDecimal totalAmount, List<SplitRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Requests list cannot be empty for PERCENT split");
        }

        BigDecimal totalPercent = BigDecimal.ZERO;
        for (SplitRequest req : requests) {
            if (req.percentage() == null || req.percentage().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Percentage must be specified and non-negative for " + req.user().getId());
            }
            totalPercent = totalPercent.add(req.percentage());
        }

        if (totalPercent.setScale(2, RoundingMode.HALF_UP).compareTo(HUNDRED) != 0) {
            throw new IllegalArgumentException("Total percentage must sum to 100.00%, got: " + totalPercent);
        }

        List<Split> splits = new ArrayList<>();
        BigDecimal sumCalculated = BigDecimal.ZERO;

        for (SplitRequest req : requests) {
            BigDecimal share = totalAmount.multiply(req.percentage())
                    .divide(HUNDRED, 2, RoundingMode.FLOOR);
            sumCalculated = sumCalculated.add(share);
            splits.add(new Split(req.user(), share));
        }

        // distribute fractional cent remainder to the first split
        BigDecimal remainder = totalAmount.subtract(sumCalculated);
        if (remainder.compareTo(BigDecimal.ZERO) > 0) {
            Split first = splits.getFirst();
            splits.set(0, new Split(first.user(), first.amount().add(remainder)));
        }

        return splits;
    }
}
