package com.jyotimoykashyap.splitwiselld.service;

import com.jyotimoykashyap.splitwiselld.models.Split;
import com.jyotimoykashyap.splitwiselld.models.User;
import com.jyotimoykashyap.splitwiselld.models.UserBalanceSheet;

import java.util.List;

public interface BalanceSheetService {
    UserBalanceSheet getBalanceSheet(String userId);
    void applyExpenseSplits(User paidByUser, List<Split> splits);
}
