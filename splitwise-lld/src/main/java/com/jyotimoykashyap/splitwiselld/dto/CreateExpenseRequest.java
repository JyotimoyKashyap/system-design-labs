package com.jyotimoykashyap.splitwiselld.dto;

import com.jyotimoykashyap.splitwiselld.models.SplitType;
import com.jyotimoykashyap.splitwiselld.strategy.SplitRequest;

import java.math.BigDecimal;
import java.util.List;

public record CreateExpenseRequest(
        String id,
        String description,
        BigDecimal amount,
        String paidByUserId,
        String groupId,
        SplitType splitType,
        List<SplitRequest> splitRequests
) {
}
