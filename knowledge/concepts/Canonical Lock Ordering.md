---
tags:
  - concurrency
  - distributed-systems
  - system-design
type:
  - concept
created: 2026-10-17
title: Canonical Lock Ordering
---
## The Problem: Circular Wait Deadlock
When a transaction or worker thread needs to acquire mutual exclusion over multiple distinct resources simultaneously (e.g., booking seats `[A1, A2]`, or transferring money between `Account A` and `Account B`), non-deterministic acquisition ordering introduces the risk of a **Coffman Circular Wait Deadlock**.

If Thread 1 attempts to lock resource $R_1$ then $R_2$, while Thread 2 attempts to lock resource $R_2$ then $R_1$:
* Thread 1 acquires $R_1$, blocks waiting for $R_2$.
* Thread 2 acquires $R_2$, blocks waiting for $R_1$.
* Both threads remain stalled indefinitely.

```
Thread 1 (holds R1) ── waiting for ──► Resource R2
      ▲                                       │
      │                                       ▼
Resource R1 ◄── waiting for ── Thread 2 (holds R2)
```

---

## The Core Solution: Total Global Order
**Canonical Lock Ordering** solves this by enforcing a strict, monotonic ordering constraint across all lock keys before any lock acquisition is attempted.

If every participant in the system is forced to acquire locks in the exact same sorted sequence ($R_a < R_b < R_c$), a circular dependency graph is mathematically impossible to form ($T_1 \to T_2 \to \dots \to T_1$).

### Mathematical Invariant
For any requested batch of resource identifiers $S = \{k_1, k_2, \dots, k_n\}$:
1. Sort keys into canonical order: $k_{(1)} < k_{(2)} < \dots < k_{(n)}$ using a deterministic comparator.
2. Acquire locks sequentially from $k_{(1)}$ to $k_{(n)}$.
3. Release locks upon completion in reverse or all at once.

---

## Implementation Patterns

### 1. In-Memory JVM Locking (Composite Key Ordering)
When locking discrete items in memory, define a composite key that implements `Comparable`:

```java
public record ResourceKey(int partitionId, int resourceId) implements Comparable<ResourceKey> {
    @Override
    public int compareTo(ResourceKey other) {
        int cmp = Integer.compare(this.partitionId, other.partitionId);
        return cmp != 0 ? cmp : Integer.compare(this.resourceId, other.resourceId);
    }
}

// In the service layer:
List<ResourceKey> sortedKeys = keys.stream().sorted().toList();
for (ResourceKey key : sortedKeys) {
    locks.get(key).lock();
}
```

### 2. Distributed Locking (Redis / Database)
In distributed systems, canonical ordering is equally critical:
* **Redis Multi-Key Transactions / Lua:** Sort Redis keys alphabetically or numerically before evaluating atomic lock acquisition scripts (`EVAL`).
* **Database Row Locking:** When executing `SELECT ... FOR UPDATE` on multiple rows, always append `ORDER BY id ASC` to guarantee identical lock acquisition order across transactions.

---

## All-or-Nothing Acquisition (Fail-Fast)
To prevent lock starvation or leaving resources partially locked when contention is detected:
1. Attempt non-blocking lock acquisition (`tryLock()`).
2. If any lock in the sorted batch cannot be acquired, immediately release all already-acquired locks and fail fast or retry with exponential backoff.

---

## Real-World Applications & Systems
* **[[BookMyShow Cinema Ticketing Engine]]:** Sorting seat keys `(showId, seatId)` before acquiring locks prevents deadlocks during high-concurrency ticket drops.
* **Double-Entry Financial Ledgers:** Locking debit and credit accounts in ascending account-ID order prevents deadlocks during bidirectional transfers.
* **Database Query Engines:** Enforcing lock ordering on join and batch update operations.

## Related Concepts
* [[Two-Phase Reservation Pattern]]
