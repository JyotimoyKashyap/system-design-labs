package com.jyotimoykashyap.splitwiselld.service;

import com.jyotimoykashyap.splitwiselld.dto.CreateExpenseRequest;
import com.jyotimoykashyap.splitwiselld.models.*;
import com.jyotimoykashyap.splitwiselld.repository.ExpenseRepository;
import com.jyotimoykashyap.splitwiselld.repository.UserBalanceRepository;
import com.jyotimoykashyap.splitwiselld.repository.UserRepository;
import com.jyotimoykashyap.splitwiselld.strategy.SplitRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExpenseServiceIntegrationTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private BalanceSheetService balanceSheetService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    private User alice;
    private User bob;
    private User charlie;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
        userBalanceRepository.deleteAll();
        userRepository.deleteAll();

        alice = userRepository.save(User.builder().id("alice").name("Alice").email("alice@test.com").build());
        bob = userRepository.save(User.builder().id("bob").name("Bob").email("bob@test.com").build());
        charlie = userRepository.save(User.builder().id("charlie").name("Charlie").email("charlie@test.com").build());

        userBalanceRepository.save(UserBalance.builder().user(alice).friend(bob).netBalance(BigDecimal.ZERO).build());
        userBalanceRepository.save(UserBalance.builder().user(bob).friend(alice).netBalance(BigDecimal.ZERO).build());
    }

    @Test
    @DisplayName("Single Expense: Alice pays $60 split equally with Bob ($30 each)")
    void testSingleExpenseEqualSplit() {
        CreateExpenseRequest request = new CreateExpenseRequest(
                "exp-1",
                "Dinner",
                new BigDecimal("60.00"),
                "alice",
                null,
                SplitType.EQUAL,
                List.of(SplitRequest.equal(alice), SplitRequest.equal(bob))
        );

        Expense expense = expenseService.createExpense(request);
        assertThat(expense).isNotNull();

        // 1. Alice's balance sheet: Bob owes Alice $30.00
        UserBalanceSheet aliceSheet = balanceSheetService.getBalanceSheet("alice");
        assertThat(aliceSheet.getTotalBack()).isEqualByComparingTo("30.00");
        assertThat(aliceSheet.getTotalOwe()).isEqualByComparingTo("0.00");
        assertThat(aliceSheet.getNetBalance()).isEqualByComparingTo("30.00");
        assertThat(aliceSheet.getBalances().get(bob)).isEqualByComparingTo("30.00");

        // 2. Bob's balance sheet: Bob owes Alice $30.00
        UserBalanceSheet bobSheet = balanceSheetService.getBalanceSheet("bob");
        assertThat(bobSheet.getTotalBack()).isEqualByComparingTo("0.00");
        assertThat(bobSheet.getTotalOwe()).isEqualByComparingTo("30.00");
        assertThat(bobSheet.getNetBalance()).isEqualByComparingTo("-30.00");
        assertThat(bobSheet.getBalances().get(alice)).isEqualByComparingTo("-30.00");

        // 3. Zero-Sum System Invariant
        assertThat(aliceSheet.getNetBalance().add(bobSheet.getNetBalance()))
                .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Concurrency Stress Test: 20 parallel threads racing between Alice and Bob with zero deadlocks")
    void testConcurrentOpposingExpensesZeroDeadlock() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    // Block until all 20 threads are ready, then fire at the exact same instant
                    startLatch.await();

                    // Even threads: Alice pays $10 for Alice & Bob ($5 Bob owes Alice)
                    // Odd threads:  Bob pays $10 for Alice & Bob ($5 Alice owes Bob)
                    if (index % 2 == 0) {
                        expenseService.createExpense(new CreateExpenseRequest(
                                "exp-alice-" + index,
                                "Alice Lunch " + index,
                                new BigDecimal("10.00"),
                                "alice",
                                null,
                                SplitType.EQUAL,
                                List.of(SplitRequest.equal(alice), SplitRequest.equal(bob))
                        ));
                    } else {
                        expenseService.createExpense(new CreateExpenseRequest(
                                "exp-bob-" + index,
                                "Bob Coffee " + index,
                                new BigDecimal("10.00"),
                                "bob",
                                null,
                                SplitType.EQUAL,
                                List.of(SplitRequest.equal(bob), SplitRequest.equal(alice))
                        ));
                    }
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Thread failed: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown(); // FIRE ALL THREADS SIMULTANEOUSLY!
        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);

        assertThat(finished).isTrue();
        assertThat(successCount.get()).isEqualTo(threadCount);

        // Since 10 payments were Alice -> Bob ($5) and 10 were Bob -> Alice ($5), net balance must be exactly $0.00!
        UserBalanceSheet aliceSheet = balanceSheetService.getBalanceSheet("alice");
        UserBalanceSheet bobSheet = balanceSheetService.getBalanceSheet("bob");

        assertThat(aliceSheet.getNetBalance()).isEqualByComparingTo("0.00");
        assertThat(bobSheet.getNetBalance()).isEqualByComparingTo("0.00");
        assertThat(aliceSheet.getNetBalance().add(bobSheet.getNetBalance())).isEqualByComparingTo("0.00");

        executorService.shutdown();
    }
}
