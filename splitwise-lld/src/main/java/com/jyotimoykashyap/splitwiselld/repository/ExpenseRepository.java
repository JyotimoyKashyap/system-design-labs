package com.jyotimoykashyap.splitwiselld.repository;

import com.jyotimoykashyap.splitwiselld.models.Expense;
import com.jyotimoykashyap.splitwiselld.models.Group;
import com.jyotimoykashyap.splitwiselld.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {
    List<Expense> findByGroup(Group group);
    List<Expense> findByPaidByUser(User user);
}
