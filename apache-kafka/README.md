# Apache Kafka Playground

This directory contains a complete local playground for understanding Apache Kafka message streams. It features a Producer microservice and two distinct Consumer microservices, all orchestrated via Docker Compose.

## Architecture

- **Kafka Broker (KRaft Mode)**: The core message broker running without Zookeeper.
- **Kafdrop**: A web UI for monitoring Kafka topics, brokers, and consumer groups.
- **Producer Service (Spring Boot)**: Automatically publishes a new message every 3 seconds to the `user-events` topic.
- **Consumer Spring (Spring Boot)**: A consumer in the `spring-group` consumer group that listens to the `user-events` topic.
- **Consumer Node (Node.js)**: A consumer in the `node-group` consumer group that listens to the exact same `user-events` topic.

Since the two consumers are in *different* Consumer Groups, they act in a Pub-Sub broadcast model—meaning both applications independently receive every message sent by the producer.

## Getting Started

1. **Start the Infrastructure and Microservices**
   ```bash
   docker compose up -d --build
   ```

2. **View Topics and Messages (Kafdrop UI)**
   - Open your browser to [http://localhost:9000](http://localhost:9000).
   - Click on the `user-events` topic to inspect the live messages.

3. **Tail the Consumer Logs**
   See the messages being processed by the consumers in real-time:
   ```bash
   docker compose logs -f consumer-spring
   docker compose logs -f consumer-node
   ```

## Tech Stack
- Java 26
- Spring Boot 4.0.6
- Node.js (KafkaJS)
- Docker & Docker Compose
