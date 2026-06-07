# System Design Labs 🚀

Welcome to my System Design Mono-repo! This repository is a collection of various experimental projects, proof-of-concepts, and lab environments where I explore and implement core system design concepts.

🌟 **Interactive Web Visualizers:** Check out the live deployments of these algorithms here: **[https://jyotimoykashyap.github.io/system-design-labs/](https://jyotimoykashyap.github.io/system-design-labs/)**

## 🚀 Running the Hub Locally

The entire Deployment Hub and all React frontend visualizers are fully containerized! You can run the entire frontend ecosystem and generated documentation locally with a single command without worrying about `npm` dependencies:

```bash
docker compose up --build -d
```

Once the container is built and running, navigate to **[http://localhost:3000](http://localhost:3000)** to access the interactive labs!

## 📂 Projects Overview

### 1. [Raft Consensus Visualizer](./leader-election-web)
A beautifully animated, fully client-side visualization of the Raft Leader Election algorithm built with React, TypeScript, and Framer Motion. It simulates true event-loop multithreading, network latency, and chaos engineering directly in the browser!

### 2. [Bloom Filter Lab](./bloom-filter-design)
Interactive implementation and visualization of probabilistic data structures used for high-performance set membership testing.

### 3. [RabbitMQ Lab](./rabbitmq-lab)
A demonstration of the **Competing Consumers Pattern** using RabbitMQ. It includes a Spring Boot Producer emitting messages and multiple Spring Boot Consumer replicas processing them in a load-balanced, round-robin fashion using Docker Compose.

### 4. [DB Scaling Lab](./db-scaling)
A database replication setup demonstrating **Master-Slave Architecture**. It is configured to handle read/write splitting where writes go to the master database and reads are handled by read replicas (slaves) to horizontally scale database throughput.

### 5. [Apache Kafka Playground](./apache-kafka)
A complete local environment to explore message streams with Apache Kafka (KRaft mode). It includes a Spring Boot Producer and two distinct consumers (Spring Boot and Node.js) configured in different Consumer Groups to demonstrate a true Pub-Sub broadcast model.

### 6. [Redis Cache Lab](./redis-cache)
A demonstration of the **Cache-Aside Pattern** using Redis and Spring Boot. This lab shows how to dramatically reduce database load and response times by caching frequently accessed records (posts) in an in-memory Redis store with an LRU eviction policy.

## 🛠 Prerequisites
All backend labs and frontend visualizers in this repository rely on containerization to simulate distributed systems locally without the hassle of local dependencies. Make sure you have the following installed:
- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)

## 📜 License
This repository is for personal learning and exploration. Feel free to explore the code!
