# System Design Labs 🚀

Welcome to my System Design Mono-repo! This repository is a collection of various experimental projects, proof-of-concepts, and lab environments where I explore and implement core system design concepts.

## 📂 Projects Overview

### 1. [RabbitMQ Lab](./rabbitmq-lab)
A demonstration of the **Competing Consumers Pattern** using RabbitMQ. It includes a Spring Boot Producer emitting messages and multiple Spring Boot Consumer replicas processing them in a load-balanced, round-robin fashion using Docker Compose.

### 2. [DB Scaling Lab](./db-scaling)
A database replication setup demonstrating **Master-Slave Architecture**. It is configured to handle read/write splitting where writes go to the master database and reads are handled by read replicas (slaves) to horizontally scale database throughput.

## 🛠 Prerequisites
Most labs in this repository rely on containerization to simulate distributed systems locally. Make sure you have the following installed:
- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)

## 📜 License
This repository is for personal learning and exploration. Feel free to explore the code!
