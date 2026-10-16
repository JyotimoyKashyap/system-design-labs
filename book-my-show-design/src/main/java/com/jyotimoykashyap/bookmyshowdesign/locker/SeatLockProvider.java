package com.jyotimoykashyap.bookmyshowdesign.locker;

import java.time.Duration;
import java.util.List;

public interface SeatLockProvider {

    /**
     * Default method with Duration set to 10 mins
     * 
     * @param showId
     * @param seatids
     * @param userId
     * @return
     */
    default boolean lockSeats(int showId, List<Integer> seatids, int userId) {
        return lockSeats(showId, seatids, userId, Duration.ofMinutes(10));
    }

    boolean lockSeats(int showId, List<Integer> seatIds, int userId, Duration ttl);
    void unlockSeats(int showId, List<Integer> seatIds, int userId);
    boolean validateLock(int showId, List<Integer> seatIds, int userId);
    List<Integer> getLockedSeats(int showId);
}
