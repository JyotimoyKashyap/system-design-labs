# RabbitMQ Competing Consumers Lab 🐇

This lab demonstrates the **Competing Consumers Pattern** using RabbitMQ, Spring Boot (Java), and Docker Compose.

## 🏗 Architecture
- **RabbitMQ**: The central message broker managing the `demo_queue`.
- **Producer**: A Spring Boot service using `@Scheduled` to continuously publish events to the broker.
- **Consumer Replicas**: Two identical Spring Boot consumer instances that listen to `demo_queue`. RabbitMQ routes messages to them in a round-robin fashion, ensuring tasks are processed efficiently without duplication.

## 🚀 How to Run

1. Make sure you are in the `rabbitmq-lab` directory.
2. Build and run the cluster using Docker Compose:
   ```bash
   docker-compose up --build
   ```
3. Watch the logs! You will see the Producer sending messages, and the two Consumer instances taking turns processing them based on their unique Instance IDs.

## 💡 Key System Design Concepts Explored
- **Decoupling**: The producer does not know who or how many consumers exist.
- **Load Balancing (Worker Queues)**: Distributing heavy background processing across multiple worker nodes.
- **Idempotency**: Both producer and consumer declare the exact same queue structure safely.
- **Race Condition Handling**: Ensuring queues are created securely before consumers attempt to connect.
