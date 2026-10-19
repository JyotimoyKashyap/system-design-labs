package com.jyotimoykashyap.splitwiselld.service;

import com.jyotimoykashyap.splitwiselld.models.Split;
import com.jyotimoykashyap.splitwiselld.models.User;
import com.jyotimoykashyap.splitwiselld.models.UserBalance;
import com.jyotimoykashyap.splitwiselld.models.UserBalanceSheet;
import com.jyotimoykashyap.splitwiselld.repository.UserBalanceRepository;
import com.jyotimoykashyap.splitwiselld.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BalanceSheetServiceImpl implements BalanceSheetService {

    private final UserBalanceRepository balanceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserBalanceSheet getBalanceSheet(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        List<UserBalance> records = balanceRepository.findByUser(user);
        Map<User, BigDecimal> balanceMap = new HashMap<>();

        for (UserBalance record : records) {
            balanceMap.put(record.getFriend(), record.getNetBalance());
        }

        return new UserBalanceSheet(user, balanceMap);
    }

    @Override
    @Transactional
    public void applyExpenseSplits(User paidByUser, List<Split> splits) {
        // filter out payer's own share and sort counterparties canonically by user ID
        List<Split> nonPayerSplits = splits.stream()
                .filter(s -> !s.user().equals(paidByUser))
                .sorted(Comparator.comparing(Split::user))
                .toList();

        for (Split split : nonPayerSplits) {
            User friend = split.user();
            BigDecimal amountOwed = split.amount();

            // canonical DB lock ordering:
            // Betn payer and friend, always update the lexicographically smaller user row first
            User firstUser = (paidByUser.compareTo(friend) < 0) ? paidByUser : friend;
            User secondUser = (paidByUser.compareTo(friend) < 0) ? friend : paidByUser;

            if (firstUser.equals(paidByUser)) {
                // payer is first: update (payer, friend, +amount) then (friend, payer, -amount)
                updatePairwiseBalance(paidByUser, friend, amountOwed);
                updatePairwiseBalance(friend, paidByUser, amountOwed.negate());
            } else {
                updatePairwiseBalance(friend, paidByUser, amountOwed.negate());
                updatePairwiseBalance(paidByUser, friend, amountOwed);
            }
        }
    }

    private void updatePairwiseBalance(User user, User friend, BigDecimal delta) {
        UserBalance balance = balanceRepository.findByUserAndFriend(user, friend)
                .orElseGet(() -> UserBalance.builder()
                        .user(user)
                        .friend(friend)
                        .netBalance(BigDecimal.ZERO)
                        .build());

        balance.adjust(delta);
        balanceRepository.save(balance);
    }
}
