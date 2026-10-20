package com.jyotimoykashyap.splitwiselld.service;

import com.jyotimoykashyap.splitwiselld.dto.CreateExpenseRequest;
import com.jyotimoykashyap.splitwiselld.models.Expense;

public interface ExpenseService {
    Expense createExpense(CreateExpenseRequest request);
}
