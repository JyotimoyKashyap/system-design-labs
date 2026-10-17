---
title: "BookMyShow Cinema Ticketing Engine"
description: "Production-grade Low-Level Design (LLD) for a high-concurrency cinema ticket reservation platform in Java 21 and Spring Boot."
publishedAt: "2026-10-17"
category: "System"
tags: ["system-design","lld","java","concurrency"]
readTimeMinutes: 2
featured: false
---

Production-grade Low-Level Design (LLD) for a high-concurrency cinema ticket reservation platform in Java 21 and Spring Boot.

* **Codebase:** [book-my-show-design/](https://github.com/JyotimoyKashyap/system-design-labs/tree/main/book-my-show-design)
* **Core Locker:** [InMemorySeatLockProvider.java](https://github.com/JyotimoyKashyap/system-design-labs/blob/main/book-my-show-design/src/main/java/com/jyotimoykashyap/bookmyshowdesign/locker/InMemorySeatLockProvider.java)
* **Booking Service:** [BookingServiceImpl.java](https://github.com/JyotimoyKashyap/system-design-labs/blob/main/book-my-show-design/src/main/java/com/jyotimoykashyap/bookmyshowdesign/service/impl/BookingServiceImpl.java)

---

## Core Engineering Primitives Used

### 1. [Canonical Lock Ordering](/learning/canonical-lock-ordering)
* **Challenge:** Preventing deadlocks when concurrent users attempt to lock overlapping batches of seats in reverse order (e.g., User A holds `[5, 12]` while User B holds `[12, 5]`).
* **Solution:** Composite key `ShowSeatKey(showId, seatId)` implements `Comparable`. All seat keys are sorted into strict monotonic order before attempting non-blocking `tryLock()` mutex acquisitions.

### 2. [Two-Phase Reservation Pattern](/learning/two-phase-reservation-pattern)
* **Challenge:** Guaranteeing zero double-booking without permanently holding inventory if a customer abandons payment.
* **Solution:**
  * **Phase 1 (Hold):** All-or-nothing acquisition of requested seats for 10 minutes (`PENDING` booking reference).
  * **Phase 2 (Commit):** Transition from `LOCKED` to `BOOKED` upon verified payment gateway confirmation.

### 3. Lazy Expiration (Zero-Sweeper Overhead)
* **Challenge:** Constant polling and scanning database tables (`UPDATE show_seats SET status='AVAILABLE' WHERE expires_at < NOW()`) creates unacceptable DB lock contention during high traffic.
* **Solution:** Expired holds are evaluated lazily in memory on query access (`removeIf(entry.getValue().isExpired())`) and overlaid dynamically onto catalog reads (`ShowCatalogServiceImpl`).

### 4. Separation of Spatial vs. Contextual State
* **`Seat` (Spatial & Immutable):** Represents physical furniture bolted to the auditorium floor (`row`, `number`, `seatType`).
* **`ShowSeat` (Contextual & Mutable):** Represents the right to sit in a `Seat` for a specific `Show` (`AVAILABLE`, `LOCKED`, `BOOKED`, runtime price).

---

## Concurrency Guarantees & Test Coverage
Verified via [BookingServiceConcurrencyTest.java](https://github.com/JyotimoyKashyap/system-design-labs/blob/main/book-my-show-design/src/test/java/com/jyotimoykashyap/bookmyshowdesign/service/BookingServiceConcurrencyTest.java):
* **20-Thread Race on Identical Seats:** Exactly 1 winner, 19 instant `SeatUnavailableException` rejects in 95 ms.
* **Opposite Order Deadlock Test:** Concurrently acquiring `[1, 2]` and `[2, 1]` resolves without thread starvation or timeouts in 58 ms.