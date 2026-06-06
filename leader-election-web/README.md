# Raft Consensus Visualizer 🌐

A fully client-side, animated visualizer for the **Raft Leader Election Algorithm**. This project was built to demystify how distributed systems achieve consensus by showing network packets, randomized timeouts, and state transitions in real-time.

![Aesthetic](https://img.shields.io/badge/Aesthetic-Material%203%20%2F%20Claude-fdfcfb)
![Tech Stack](https://img.shields.io/badge/Stack-React%20%7C%20TypeScript%20%7C%20Vite-blue)
![Styling](https://img.shields.io/badge/Styling-Tailwind%20v4%20%7C%20Framer%20Motion-emerald)

## 🎯 Features

- **True Event-Loop Multithreading:** The underlying logic operates exactly like a real distributed system. Nodes are decoupled class instances that rely purely on asynchronous JS timeouts (`setTimeout` / `setInterval`) rather than a master `tick()` loop.
- **Simulated Network Latency:** A `Network` broker intercepts all RPCs and artificially delays them by `400ms - 800ms` to simulate realistic, congested network conditions.
- **Dynamic Animations:** Real-time rendering of network packets (`RequestVote`, `VoteGranted`, `AppendEntries`) flying across the screen using Framer Motion.
- **Chaos Engineering:** Click on the current Leader node to "Kill" it. Watch as heartbeats stop, followers timeout, and a new election gracefully orchestrates a replacement!
- **Beautiful UI:** A brutalist, sharp-edged, expressive light theme inspired by Claude and Material 3 design systems.

## 🛠️ Tech Stack

* **React 19 + Vite:** Blazing fast client-side rendering.
* **TypeScript:** Strict typing for all Raft State Machine logic and RPC messages.
* **Tailwind CSS v4:** Zero-config, massive utility-class styling for layout and themes.
* **Framer Motion:** Handles all coordinate tracking, SVG progress bars, and packet animations seamlessly.

## 🏗️ Low-Level Design (LLD)

The core algorithm is cleanly decoupled from the React UI and lives entirely inside `src/simulation/`.

1. **`types.ts`:** Defines the strict `RPCMessage` types and Node states (`FOLLOWER`, `CANDIDATE`, `LEADER`, `DEAD`).
2. **`Network.ts`:** Acts as the message broker. It maintains a registry of all active nodes and handles the `send()` and `broadcast()` APIs, injecting random latency before delivering payloads.
3. **`Node.ts`:** The brain of the operation. Each node maintains its own `currentTerm`, `votedFor`, and an independent randomized election timer. It receives RPCs from the Network and independently decides whether to step down, grant votes, or transition to a Candidate.
4. **`ClusterManager.ts`:** The orchestrator that boots up the `N` nodes, registers them to the Network, and bridges the Backend Node events (`onStateChange`, `onTimerReset`) up to the React Frontend.

## 🚀 How to Run Locally

1. **Clone the repository** (if you haven't already).
2. **Navigate to the project directory:**
   ```bash
   cd leader-election-web
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
6. Click **Start Simulation** and enjoy!
