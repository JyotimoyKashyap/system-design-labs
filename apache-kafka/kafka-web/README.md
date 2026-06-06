# Apache Kafka Web Visualizer 🚀

A highly interactive, client-side browser simulation of **Apache Kafka's Partitioned Log & Consumer Group Architecture**.

![Aesthetic](https://img.shields.io/badge/Aesthetic-Paper%20Brutalist-fdfcfb)
![Tech Stack](https://img.shields.io/badge/Stack-React%20%7C%20TypeScript%20%7C%20Vite-blue)

## 🎯 Features

- **Immutable Append-Only Logs:** Watch a Topic visually split into 3 Partitions. As the Producer streams data, watch the blocks append strictly sequentially.
- **Consumer Group Rebalancing:** Spawn consumers dynamically. Watch Kafka's strict `1 Partition -> 1 Consumer` rule in action! If you have 1 consumer, it reads all 3 partitions. Spawn a 2nd consumer, and watch them instantly rebalance the partition load.
- **Offset Tracking:** Instead of deleting messages when read (like a standard RabbitMQ queue), Kafka simply increments an Offset pointer. Watch the UI pointers slide across the partition logs!
- **Auto-Eviction UI:** To prevent browser memory leaks, the UI visually shifts old messages out of view while keeping the logical offsets monotonically increasing, perfectly simulating log retention policies.

## 🚀 How to Run Locally

1. **Clone the repository** (if you haven't already).
2. **Navigate to the project directory:**
   ```bash
   cd apache-kafka/kafka-web
   ```
3. **Install dependencies:**
   ```bash
   npm install
   ```
4. **Start the development server:**
   ```bash
   npm run dev
   ```
5. **Open your browser:** Visit `http://localhost:5173`.
