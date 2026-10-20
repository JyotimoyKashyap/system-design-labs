package com.jyotimoykashyap.splitwiselld.repository;

import com.jyotimoykashyap.splitwiselld.models.User;
import com.jyotimoykashyap.splitwiselld.models.UserBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {
    // Retrieve all balances for a specific user to construct their UserBalanceSheet
    List<UserBalance> findByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ub FROM UserBalance ub WHERE ub.user = :user AND ub.friend = :friend")
    Optional<UserBalance> findByUserAndFriendForUpdate(@Param("user") User user, @Param("friend") User friend);

    // Retrieve a specific pairwise balance between user and friend
    Optional<UserBalance> findByUserAndFriend(User user, User friend);
}
