package com.jyotimoykashyap.splitwiselld.service;

import com.jyotimoykashyap.splitwiselld.dto.CreateExpenseRequest;
import com.jyotimoykashyap.splitwiselld.models.*;
import com.jyotimoykashyap.splitwiselld.repository.ExpenseRepository;
import com.jyotimoykashyap.splitwiselld.repository.GroupRepository;
import com.jyotimoykashyap.splitwiselld.repository.UserRepository;
import com.jyotimoykashyap.splitwiselld.strategy.SplitStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final Map<SplitType, SplitStrategy> strategyMap;
    private final BalanceSheetService balanceSheetService;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public ExpenseServiceImpl(
            List<SplitStrategy> strategies,
            BalanceSheetService balanceSheetService,
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            GroupRepository groupRepository
    ) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(SplitStrategy::getSplitType, Function.identity()));
        this.balanceSheetService = balanceSheetService;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    @Transactional
    public Expense createExpense(CreateExpenseRequest request) {
        // 1. Fetch & validate payer
        User paidByUser = userRepository.findById(request.paidByUserId())
                .orElseThrow(() -> new IllegalArgumentException("Payer not found : " + request.paidByUserId()));

        // 2. Fetch & validate group (if expense is inside a group)
        Group group = null;
        if (request.groupId() != null && !request.groupId().isBlank()) {
            group = groupRepository.findById(request.groupId())
                    .orElseThrow(() -> new IllegalArgumentException("Group not found: " + request.groupId()));
        }

        // 3. Delete to appropriate SplitStrategy
        SplitStrategy strategy = strategyMap.get(request.splitType());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported split type: " + request.splitType());
        }

        List<Split> computedSplits = strategy.calculateSplits(request.amount(), request.splitRequests());

        // 4. Construct & Persist the Expense entity
        String expenseId = (request.id() != null && !request.id().isBlank()) ? request.id() : UUID.randomUUID().toString();

        Expense expense = Expense.builder()
                .id(expenseId)
                .description(request.description())
                .amount(request.amount())
                .paidByUser(paidByUser)
                .group(group)
                .splitType(request.splitType())
                .splits(computedSplits)
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        // 5. Update pairwise balance sheets canonically
        balanceSheetService.applyExpenseSplits(paidByUser, computedSplits);

        return savedExpense;
    }
}
