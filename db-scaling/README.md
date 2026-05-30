# Database Scaling Lab 🗄️📈

This lab explores horizontal scaling techniques for relational databases using a **Master-Slave Replication** architecture.

## 🏗 Architecture
- **Master Database (`master`)**: The primary node responsible for handling all `WRITE` operations (INSERT, UPDATE, DELETE). It asynchronously streams changes to the replicas.
- **Slave Database (`slave`)**: A read-only replica that syncs data from the master node. It is used to handle `READ` operations (SELECT), offloading heavy read traffic from the master.
- **App (`app`)**: A simulated application layer designed to demonstrate read/write splitting across the database nodes.

## 🚀 How to Run

1. Make sure you are in the `db-scaling` directory.
2. Start the cluster:
   ```bash
   docker-compose up --build
   ```

## 💡 Key System Design Concepts Explored
- **Master-Slave Replication**: Copying data from a single authoritative source to multiple read-only sources.
- **Read/Write Splitting**: Routing database queries at the application or proxy layer to the appropriate node.
- **Eventual Consistency**: Understanding replication lag between the time a record is written to the master and when it becomes available on the slave.
