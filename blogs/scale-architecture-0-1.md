# API Servers at Scale — The Full Picture

## Level 0: The Naive Setup — No Pools At All

The simplest possible server. Every request spawns a **new thread** and opens a **new DB connection**. No reuse, no pooling.

```mermaid
graph TB
    C1["👤 Client A"] --> S["🖥️ API Server"]
    C2["👤 Client B"] --> S

    subgraph S_INTERNAL ["Inside the Server"]
        direction TB
        REQ1["Request A arrives<br/>→ spawn new Thread<br/>→ open new DB connection"] 
        REQ2["Request B arrives<br/>→ spawn new Thread<br/>→ open new DB connection"]
    end

    REQ1 -- "new conn each time" --> DB["🗄️ Database"]
    REQ2 -- "new conn each time" --> DB
```

**What goes wrong at scale:**

| Problem | Why it hurts |
|---|---|
| **Thread creation overhead** | Spawning a thread costs ~1ms + ~1MB stack memory. At 1000 req/sec, that's 1000 threads and ~1GB RAM just for stacks |
| **DB connection overhead** | Opening a connection takes ~20-50ms (TCP handshake + auth). That's added to *every* request |
| **No upper bound** | Traffic spike → unlimited threads → OS runs out of memory → crash |
| **Connection exhaustion** | Most databases cap connections (e.g., PostgreSQL default: 100). 101st request gets rejected |

This is fine for a hobby project doing 10 req/sec. It falls apart the moment you get real traffic.

---

## Level 1 → Level 2: From Pooled Server to Fleet

**Level 1** fixes Level 0 by introducing **thread pools** and **connection pools** — reuse instead of recreate. This is the standard single-server setup.

```mermaid
graph TB
    subgraph "Level 1 — What we started with"
        C_SIMPLE["👤 Clients"] --> S_SIMPLE["🖥️ API Server<br/><i>Thread Pool + Conn Pool</i>"]
        S_SIMPLE --> DB_SIMPLE["🗄️ Database"]
    end
```

**Level 2** scales out by cloning that pair and putting a load balancer in front 👇

## The Full Scaled Architecture

```mermaid
graph TB
    C1["👤 Client A<br/>user_id: 1-1000"] 
    C2["👤 Client B<br/>user_id: 1001-2000"]
    C3["👤 Client C<br/>user_id: 2001-3000"]
    C4["👤 Client D<br/>user_id: 1500"]

    LB["⚖️ Load Balancer<br/><i>Consistent Hashing on user_id</i><br/>nginx / HAProxy / AWS ALB"]

    C1 --> LB
    C2 --> LB
    C3 --> LB
    C4 --> LB

    subgraph SERVER_1 ["🖥️ API Server 1 (Process)"]
        direction TB
        TP1["🧵 Thread Pool<br/>T1 T2 T3 T4 T5"]
        CP1["🔗 Connection Pool<br/>to Shard A"]
    end

    subgraph SERVER_2 ["🖥️ API Server 2 (Process)"]
        direction TB
        TP2["🧵 Thread Pool<br/>T1 T2 T3 T4 T5"]
        CP2["🔗 Connection Pool<br/>to Shard B"]
    end

    subgraph SERVER_3 ["🖥️ API Server 3 (Process)"]
        direction TB
        TP3["🧵 Thread Pool<br/>T1 T2 T3 T4 T5"]
        CP3["🔗 Connection Pool<br/>to Shard C"]
    end

    LB -- "user_id 1-1000" --> SERVER_1
    LB -- "user_id 1001-2000" --> SERVER_2
    LB -- "user_id 2001-3000" --> SERVER_3

    subgraph SHARD_A ["🗄️ Shard A"]
        DB_A_P["Primary<br/><i>Reads + Writes</i>"]
        DB_A_R["Read Replica<br/><i>Reads only</i>"]
        DB_A_P -- "async replication" --> DB_A_R
    end

    subgraph SHARD_B ["🗄️ Shard B"]
        DB_B_P["Primary<br/><i>Reads + Writes</i>"]
        DB_B_R["Read Replica<br/><i>Reads only</i>"]
        DB_B_P -- "async replication" --> DB_B_R
    end

    subgraph SHARD_C ["🗄️ Shard C"]
        DB_C_P["Primary<br/><i>Reads + Writes</i>"]
        DB_C_R["Read Replica<br/><i>Reads only</i>"]
        DB_C_P -- "async replication" --> DB_C_R
    end

    CP1 -- "writes" --> DB_A_P
    CP1 -. "reads" .-> DB_A_R
    CP2 -- "writes" --> DB_B_P
    CP2 -. "reads" .-> DB_B_R
    CP3 -- "writes" --> DB_C_P
    CP3 -. "reads" .-> DB_C_R
```

## Inside a Single API Server Process

Each box labeled "API Server" above is a **single OS process**. Here's what's inside:

```mermaid
graph TB
    subgraph PROCESS ["🖥️ API Server Process (PID: 48291)"]
        direction TB
        
        LISTENER["📡 Main Listener<br/><i>Port 8080</i>"]
        ROUTER["🔀 Router / Dispatcher"]
        MW["🛡️ Middleware Pipeline<br/><i>Auth → Logging → Rate Limit → Deadline Check</i>"]
        
        subgraph HEAP ["Heap Memory (Shared)"]
            CONFIG["📋 App Config<br/><i>static, immutable</i>"]
            CACHE["⚡ Local Cache<br/><i>ConcurrentHashMap</i>"]
            SHARD_MAP["🗺️ Shard Router<br/><i>Consistent Hash Ring</i>"]
        end

        subgraph THREAD_POOL ["🧵 Thread Pool (200 threads)"]
            T1["Thread 1<br/><i>Stack: local vars</i>"]
            T2["Thread 2<br/><i>Stack: local vars</i>"]
            T3["Thread 3<br/><i>Stack: local vars</i>"]
            TN["Thread ..N<br/><i>Stack: local vars</i>"]
        end

        subgraph CONN_POOL ["🔗 DB Connection Pool (20 conns)"]
            DC1["Conn 1"] 
            DC2["Conn 2"]
            DCN["Conn ..N"]
        end

        LISTENER --> ROUTER
        ROUTER --> MW
        MW --> THREAD_POOL
        T1 --> CONN_POOL
        T2 --> CONN_POOL
        T3 --> CONN_POOL
    end
```

> [!NOTE]
> Each thread gets its own **stack** (local variables, function call chain) but they all share the **heap** (config, cache, shard map, connection pool). This is why static mutable state is dangerous — exactly what we discussed earlier.

## Consistent Hashing — How the Load Balancer Routes

Instead of simple round-robin (which would send any user to any server), **consistent hashing** maps each user to a specific server deterministically:

```mermaid
graph LR
    subgraph RING ["Consistent Hash Ring (0 — 2³²)"]
        direction LR
        P0["Position 0"] --- S1_POS["🖥️ Server 1<br/>hash = 1000"]
        S1_POS --- P1["..."]
        P1 --- S2_POS["🖥️ Server 2<br/>hash = 15000"]
        S2_POS --- P2["..."]
        P2 --- S3_POS["🖥️ Server 3<br/>hash = 38000"]
        S3_POS --- P3["..."]
        P3 --- P0
    end
```

```
hash("user_42")   = 3500   → lands between Server 1 (1000) and Server 2 (15000)
                            → routes to Server 2 (next server clockwise)

hash("user_99")   = 40000  → lands between Server 3 (38000) and Server 1 (1000)  
                            → routes to Server 1 (wraps around)

hash("user_1337") = 12000  → lands between Server 1 (1000) and Server 2 (15000)
                            → routes to Server 2
```

### Why consistent hashing over simple modulo?

| | Modulo (`user_id % N`) | Consistent Hashing |
|---|---|---|
| **Add a server** | Almost ALL users get remapped to different servers | Only ~1/N users get remapped |
| **Remove a server** | Same — mass remapping | Only the dead server's users move to the next one |
| **Cache locality** | Destroyed on scaling events | Mostly preserved |

This is critical because if you're doing **sticky sessions** or **local caching**, a mass remapping means every cache goes cold simultaneously — instant load spike on your databases.

## Sharding vs Read Replicas — They Solve Different Problems

You're right that with sharding you don't *necessarily* need replicas, but in practice most systems use both:

```mermaid
graph TB
    subgraph SHARDING ["Sharding — Splits DATA horizontally"]
        SA["Shard A<br/>users 1-1M"]
        SB["Shard B<br/>users 1M-2M"]
        SC["Shard C<br/>users 2M-3M"]
    end
    
    subgraph REPLICATION ["Replication — Copies ENTIRE shard"]
        P["Primary<br/><i>all users 1-1M</i>"]
        R1["Replica 1<br/><i>all users 1-1M</i>"]
        R2["Replica 2<br/><i>all users 1-1M</i>"]
        P -- "sync/async<br/>replication" --> R1
        P -- "sync/async<br/>replication" --> R2
    end
```

| | Sharding | Read Replicas |
|---|---|---|
| **Problem solved** | Dataset too large for one machine | Too many reads for one machine |
| **Data on each node** | Partial (subset of rows) | Full copy (all rows) |
| **Writes go to** | The correct shard only | Primary only |
| **Reads go to** | The correct shard only | Any replica |
| **Failure impact** | Lose access to that shard's data | Promote replica to primary, no data loss |

### When you combine both

Each **shard** gets its own **primary + replicas**:

- **Writes** → routed by consistent hash to the correct shard's **primary**
- **Reads** → routed to that shard's **replica** (offloading the primary)
- **Failover** → if a primary dies, its replica gets promoted

This is what you see at scale — **sharding for horizontal data distribution**, **replicas for read scaling and fault tolerance**. They're complementary, not alternatives.

## Complete Request Flow at Scale

```mermaid
sequenceDiagram
    participant C as 👤 Client
    participant LB as ⚖️ Load Balancer
    participant S as 🖥️ API Server 2
    participant T as 🧵 Thread 47
    participant CP as 🔗 Conn Pool
    participant R as 📖 Read Replica
    participant P as ✏️ Primary DB

    C->>LB: GET /api/users/1500
    Note over LB: hash("user_1500") → Server 2

    LB->>S: Forward request
    Note over S: Deadline check: 500ms SLA, clock starts

    S->>T: Assign from thread pool
    activate T

    Note over T: GET request → read-only → use replica

    T->>CP: Acquire connection (to replica)
    CP-->>T: Connection #7

    T->>R: SELECT * FROM users WHERE id=1500
    R-->>T: { id: 1500, name: "Jyotimoy" }

    T->>CP: Release connection #7
    T-->>S: Response ready
    deactivate T

    S-->>LB: 200 OK
    LB-->>C: 200 OK { "name": "Jyotimoy" }

    Note over C, P: Total: 12ms ✅ well within 500ms SLA
```

## The Recursive Insight

Your observation is exactly right:

```
At scale, the system is just:

    ┌──────────────────────────────────────────────┐
    │           Load Balancer + Routing             │
    └──────┬──────────┬──────────────┬──────────────┘
           │          │              │
    ┌──────▼───┐ ┌────▼─────┐ ┌─────▼────┐
    │ Server 1 │ │ Server 2 │ │ Server 3 │    ← Each one is the
    │ + Shard A│ │ + Shard B│ │ + Shard C│      SAME simple setup
    └──────────┘ └──────────┘ └──────────┘      we started with
```

Each pair is independently the **thread pool + connection pool + single database** model from our first diagram. The load balancer + consistent hashing is just the layer that **distributes traffic** so each pair only handles its slice.

**That's the beauty of good system design — it's fractal. Zoom in on any piece and it looks like the whole.**
