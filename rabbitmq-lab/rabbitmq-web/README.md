# RabbitMQ Visualizer 🐇

A beautiful, interactive, and purely client-side simulation of the **Competing Consumers Pattern**.

![Aesthetic](https://img.shields.io/badge/Aesthetic-Paper%20Brutalist-fdfcfb)
![Tech Stack](https://img.shields.io/badge/Stack-React%20%7C%20TypeScript%20%7C%20Vite-blue)
![Styling](https://img.shields.io/badge/Styling-Tailwind%20v4%20%7C%20Framer%20Motion-emerald)

## 🎯 Features

- **Decoupled Simulation Engine:** The entire messaging architecture (`Broker`, `Producer`, `Consumer`) is decoupled from the UI framework, relying purely on JavaScript's Event Loop to simulate true asynchronous message passing and processing bottlenecks.
- **Dynamic Horizontal Scaling:** Click the `+` and `-` buttons to instantly spawn or destroy Producer and Consumer nodes in real-time. Watch how adding more consumers instantly resolves queue backpressure!
- **Conveyor Belt Animation:** Framer Motion physically translates message packets through a high-contrast brutalist queue track. 

## 🚀 How to Run Locally

1. **Clone the repository** (if you haven't already).
2. **Navigate to the project directory:**
   ```bash
   cd rabbitmq-lab/rabbitmq-web
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
6. Tweak the number of nodes to watch the queue explode or perfectly load-balance!
