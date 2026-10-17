---
tags:
  - system-design
  - concurrency
  - e-commerce
created: 2026-10-17
type:
  - concept
title: Two-Phase Reservation Pattern
---
# Two-Phase Reservation Pattern

## The Problem: The Checkout Paradox
In high-demand transactional platforms (concert ticketing, airline seats, hotel booking, flash sales), checkout cannot be completed in a single instantaneous step because payment processing takes time (typically 30 seconds to 10 minutes) and users must inspect totals before paying.

This creates two opposing failure modes:
1. **Immediate Hard Booking:** If you permanently mark inventory booked before charging the user, abandoned checkouts permanently leak inventory.
2. **Late Booking on Payment:** If you only check inventory availability after the payment succeeds, multiple concurrent users will pay for the exact same item, resulting in catastrophic double-bookings and refunds.

---

## The Solution: Two-Phase Hold & Commit
The **Two-Phase Reservation Pattern** decouples temporary reservation (holding inventory) from permanent commitment (selling inventory) via a finite-time lease (TTL).

```
        Phase 1: Hold (10m TTL)                Phase 2: Commit (Payment OK)
[AVAILABLE] ───────────────► [LOCKED / HELD] ───────────────────► [BOOKED]
     ▲                             │
     │      Hold Expired / Cancel  │
     └─────────────────────────────┘
```

### Lifecycle States
* **Phase 1: Hold (Soft Lock):** 
  * The user selects $N$ resources.
  * The system validates availability and acquires a temporary lease with a Time-To-Live (e.g., 10 minutes).
  * Inventory transitions from `AVAILABLE` to `LOCKED`.
* **Phase 2A: Commit (Hard Lock):** 
  * Payment gateway returns success within the TTL window.
  * The system transitions state from `LOCKED` to `BOOKED` and clears the temporary lease.
* **Phase 2B: Expiration / Release:** 
  * If payment is rejected, abandoned, or times out past the TTL, the hold expires and inventory reverts to `AVAILABLE`.

---

## Architectural Implementation Choices

| Dimension | In-Memory (JVM) | Distributed Cache (Redis) | Relational Database (Postgres) |
| :--- | :--- | :--- | :--- |
| **Throughput** | Ultra high (>100k ops/sec) | Very high (>50k ops/sec) | Moderate (limited by write IOPS) |
| **TTL Mechanism** | Lazy eviction / concurrent maps | Native Redis key TTL (`EXPIRE`) | Scheduled sweepers or lazy filters |
| **Failure Recovery** | Lost on process restart | Replicated / persistent (AOF) | Fully ACID compliant |
| **Best Used For** | Single-node LLD / edge services | Production microservices fleets | Low-throughput, high-audit systems |

---

## Key Invariants & Edge Cases
1. **All-or-Nothing Holds:** If a user requests 3 seats and only 2 are available, the entire operation must fail fast without holding a partial set.
2. **Deadlock Avoidance in Multi-Resource Holds:** When holding multiple items simultaneously, combine this pattern with [[Canonical Lock Ordering]] to prevent circular wait deadlocks.
3. **Lazy Expiration (Zero-Sweeper Overhead):** Avoid background cron jobs that constantly update expired database rows. Instead, evaluate expiration lazily on access or rely on cache TTLs.

---

## Real-World Applications & Systems
* **[[BookMyShow Cinema Ticketing Engine]]:** Grants 10-minute holds on theater seats while coordinating with third-party payment gateways.
* **Airline GDS (Global Distribution Systems):** PNR (Passenger Name Record) ticketing windows.
* **Hotel Property Management Systems (PMS):** Room allocation holds during checkout.
* **E-Commerce Flash Sales:** Holding shopping cart stock for 15 minutes.

## Related Concepts
* [[Canonical Lock Ordering]]
