package com.jyotimoykashyap.bookmyshowdesign.locker;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;


@Component 
public class InMemorySeatLockProvider implements SeatLockProvider {

    private final Map<ShowSeatKey, SeatLock> locks = new ConcurrentHashMap<>();
    private final Map<ShowSeatKey, ReentrantLock> seatLocks = new ConcurrentHashMap<>();

    @Override
    public boolean lockSeats(int showId, List<Integer> seatIds, int userId, Duration ttl) {
        if (seatIds == null || seatIds.isEmpty()) return false;


        List<ShowSeatKey> keys = seatIds.stream()
                            .sorted()
                            .map(seatId -> new ShowSeatKey(showId, seatId))
                            .toList();

        List<ReentrantLock> acquiredLocks = new ArrayList<>();

        try {
            // we will do this in phases
            // 1. Try to acquire the locks 
            for (ShowSeatKey key : keys) {
                ReentrantLock seatLock = seatLocks.computeIfAbsent(key, k -> new ReentrantLock());

                // try lock 
                if (!seatLock.tryLock()) {
                    return false; // contention : cannot lock, other thread has acquired the lock
                }

                acquiredLocks.add(seatLock);
            }

            // 2. We have the locks now we can read and write on the data
            // we will check if any previous lock is present which is not expired
            for (ShowSeatKey key : keys) {
                SeatLock existing = locks.get(key);

                if (existing != null && !existing.isExpired())
                    return false; // all-seats or nothing
            }

            // 3. All seats are free then put locks on the seat
            Instant now = Instant.now();
            SeatLock seatLock = new SeatLock(userId, now, now.plus(ttl));
            for (ShowSeatKey key : keys) {
                locks.put(key, seatLock);
            }

            return true;
        } finally {
            // 4. release the lock
            for (ReentrantLock lock : acquiredLocks) {
                lock.unlock();
            }
        }
        
    }

    @Override
    public void unlockSeats(int showId, List<Integer> seatIds, int userId) {
        for (int seatId : seatIds) {
            ShowSeatKey key = new ShowSeatKey(showId, seatId);
            SeatLock lock = locks.get(key);

            if (lock != null && lock.userId() == userId) {
                locks.remove(key);
            }
        }
    }

    @Override
    public boolean validateLock(int showId, List<Integer> seatIds, int userId) {
        for (int seatId : seatIds) {
            ShowSeatKey key = new ShowSeatKey(showId, seatId);
            SeatLock lock = locks.get(key);

            if (lock == null || lock.userId() != userId || lock.isExpired()) 
                return false;
        }
        return true;
    }

    @Override
    public List<Integer> getLockedSeats(int showId) {
        locks.entrySet().removeIf(entry -> 
            entry.getKey().showId() == showId && entry.getValue().isExpired()
        );

        return locks.entrySet().stream()
                    .filter(entry -> entry.getKey().showId() == showId)
                    .filter(entry -> !entry.getValue().isExpired())
                    .map(entry -> entry.getKey().seatId())
                    .toList();
    }

}
