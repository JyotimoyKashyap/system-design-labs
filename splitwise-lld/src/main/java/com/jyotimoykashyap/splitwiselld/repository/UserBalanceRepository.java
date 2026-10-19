package com.jyotimoykashyap.splitwiselld.repository;

import com.jyotimoykashyap.splitwiselld.models.User;
import com.jyotimoykashyap.splitwiselld.models.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {
    // Retrieve all balances for a specific user to construct their UserBalanceSheet
    List<UserBalance> findByUser(User user);

    // Retrieve a specific pairwise balance between user and friend
    Optional<UserBalance> findByUserAndFriend(User user, User friend);
}
