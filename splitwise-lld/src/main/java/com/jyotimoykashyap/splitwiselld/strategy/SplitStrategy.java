package com.jyotimoykashyap.splitwiselld.strategy;

import com.jyotimoykashyap.splitwiselld.models.Split;
import com.jyotimoykashyap.splitwiselld.models.SplitType;

import java.math.BigDecimal;
import java.util.List;

public interface SplitStrategy {
    SplitType getSplitType();

    /**
     * Validates input requests and computes final immutable Split records.
     * Guaranteed invariant: sum(splits.amount) == totalAmount to the exact cent.
     */
    List<Split> calculateSplits(BigDecimal totalAmount, List<SplitRequest> requests);
}
