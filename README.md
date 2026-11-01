<div align="center">

# System Design Labs ⚡

**An interactive engineering laboratory and open-source workbench for distributed systems, concurrency primitives, and production-grade Low-Level Design (LLD).**

[![Deploy Pages](https://github.com/JyotimoyKashyap/system-design-labs/actions/workflows/deploy-pages.yml/badge.svg)](https://github.com/JyotimoyKashyap/system-design-labs/actions/workflows/deploy-pages.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-black.svg?style=flat-square)](LICENSE)
[![Docker Containerized](https://img.shields.io/badge/Docker-Containerized-2496ED.svg?style=flat-square&logo=docker&logoColor=white)](docker-compose.yml)
[![Built with Astro 5](https://img.shields.io/badge/Astro-5.0-BC52EE.svg?style=flat-square&logo=astro&logoColor=white)](https://astro.build)
[![UI: Paper Brutalist](https://img.shields.io/badge/UI-Paper_Brutalist-f97316.svg?style=flat-square)](packages/ui)
[![Tests: node:test](https://img.shields.io/badge/Tests-Passing_(<200ms)-10b981.svg?style=flat-square)](tests)

[Explore Interactive Hub](https://jyotimoykashyap.github.io/system-design-labs/) • [Contributing Guide](CONTRIBUTING.md) • [Design System](packages/ui) • [AI Pair Programming](AGENTS.md)

---

</div>

## 🌐 The Live Interactive Hub

Experience distributed consensus, consistent hash rings, and rate limiting directly in your browser without spinning up external infrastructure:

👉 **[https://jyotimoykashyap.github.io/system-design-labs/](https://jyotimoykashyap.github.io/system-design-labs/)**

| Simulation | Core Concept Demonstrated | Highlights |
| :--- | :--- | :--- |
| **Raft Consensus** | Distributed State Machine Replication | Leader election, heartbeat timeouts, split votes, and simulated network partitions. |
| **Distributed Token Bucket** | API Gateway Traffic Throttling | Sliding window token refill across distributed Redis nodes under high-concurrency bursts. |
| **Consistent Hashing** | Dynamic Hash Space Partitioning | Ring topology, virtual node balancing, and minimized key redistribution during node churn. |
| **Probabilistic Bloom Filter** | High-Performance Set Membership | Multiple hash distributions, bit arrays, and empirical false-positive rate curves. |
| **Master-Slave DB Scaling** | Read/Write Splitting | Write delegation to master nodes with horizontally scalable read replica pools. |
| **Competing Consumers** | Asynchronous Work Queues | Multi-replica worker pools balancing round-robin task loads via RabbitMQ. |

---

## 🏛️ Monorepo Blueprint

This repository is organized as an automated, multi-tenant monorepo. All projects, notes, and visualizers are automatically cataloged by our zero-command discovery pipeline:

```
personal-lab/
├── apps/
│   └── web/                     # Unified Astro 5 + React 19 documentation & simulation platform
├── packages/
│   ├── ui/                      # @repo/ui: Canonical Paper Brutalist design system tokens & components
│   └── shared/                  # Shared data structures and utility libraries
├── <concept>-web/               # Interactive frontend simulations (Raft, Consistent Hashing, etc.)
├── <concept>-lld/               # Clean architecture Low-Level Design solutions in Java & TypeScript
├── <infrastructure-lab>/        # Distributed backend sandboxes (Kafka KRaft, RabbitMQ, Redis, DB Scaling)
├── knowledge/                   # Deep dives: Incident post-mortems, algorithm notes & system blueprints
└── tests/                       # Instant anti-drift, schema, and metadata verification test suite
```

### The 4 Core Pillars

1. 🔬 **Interactive Labs & Visualizers**: Client-side distributed systems sandboxes with step-by-step state ticks, latency simulation, and chaos injection.
2. 📐 **Low-Level Design (LLD)**: Production-grade object-oriented implementations (Cache Striping, Splitwise, Vending Machine, Parking Lot, Booking Engine) with thread-safe concurrency guarantees.
3. 📝 **Architecture & Knowledge Base**: Incident post-mortems, whitepaper breakdowns, and real-world distributed systems clippings.
4. 🎨 **Paper Brutalist Design System**: Centralized design tokens and micro-interactions in [`@repo/ui`](./packages/ui) enforcing sharp contrast, zero rounded corners, and crisp typography.

---

## ⚡ Quickstart

### Option 1: Docker Container (Zero Local Dependencies)

Run the entire frontend hub and interactive simulations inside a hermetic, production-grade container:

```bash
# Build and run the local container
docker compose up --build -d
```

Open **[http://localhost:3000](http://localhost:3000)** to browse all labs and documentation.

---

### Option 2: Local Development Workspace

```bash
# 1. Clone the repository
git clone https://github.com/JyotimoyKashyap/system-design-labs.git
cd system-design-labs

# 2. Install workspace dependencies
npm install

# 3. Start local development server with instant HMR
npm run dev
```

Navigate to **[http://localhost:4321](http://localhost:4321)**.

---

### Option 3: Instant Test Suite (<200ms)

Verify metadata, auto-discovery integrity, design system token compliance, and anti-drift rules:

```bash
npm test
```

---

## 🚀 Zero-Command Auto-Discovery

You never need to edit routing files or Astro component registries when adding content:

1. **Drop a folder** at the root of the monorepo (e.g. `rate-limiter/`).
2. Add a `*visualizer*.tsx` component &rarr; automatically mounted at `/labs/<slug>`.
3. Add `lld.md` or `design.md` &rarr; automatically published at `/lld/<slug>`.
4. Add `notes.md` or `architecture.md` &rarr; automatically published at `/notes/<slug>`.
5. Add `README.md` with a `<!-- hub-metadata -->` block &rarr; published as interactive documentation.
6. The `npm test` and `npm run dev` hooks auto-sync everything instantly!

---

## 🤝 Contributing

Contributions are warmly welcome! Whether you are implementing a new Low-Level Design problem, adding an interactive distributed systems visualizer, or writing architectural deep-dives:

1. Read our comprehensive **[Contributing Guide (CONTRIBUTING.md)](CONTRIBUTING.md)**.
2. Review the Paper Brutalist UI standards in **[DESIGN.md](DESIGN.md)**.
3. Ensure every new project folder contains a `README.md` with a metadata block:

```markdown
<!-- hub-metadata
type: lab | visualizer | lld | note
title: Your Project Title
description: Concise one or two sentence summary of the architecture.
tag: Distributed Systems
tagColor: #f97316
category: Distributed Systems
-->
```

4. Run `npm test` before submitting your PR to ensure all quality gates pass!

---

## 🤖 Built-in AI Agents & Skills

This repository is **agent-native** — engineered to be explored, developed, and maintained by both human developers and modern AI coding assistants (such as Google DeepMind's **Antigravity**, Claude Code, or Cursor). 

We ship dedicated agent personas and domain skills in [`.agents/`](.agents/) that you can leverage while contributing:

### 🤖 Specialized AI Agents ([`.agents/agents/`](.agents/agents/))
| Agent | Role & Capabilities |
| :--- | :--- |
| **[`lld-mentor`](.agents/agents/lld-mentor.md)** | Socratic Low-Level Design architect that guides engineers through designing, implementing, and documenting production-grade, concurrency-safe object-oriented systems with phase-driven milestones and empirical multithreaded test verification. |

### 🛠️ Specialized Skills ([`.agents/skills/`](.agents/skills/))
| Skill | Purpose |
| :--- | :--- |
| **[`low-level-design-architect`](.agents/skills/low-level-design-architect/SKILL.md)** | Canonical blueprint for authoring production-grade LLD specifications, Mermaid diagrams, clean architecture, and READMEs. |
| **[`system-design-visualizer`](.agents/skills/system-design-visualizer/SKILL.md)** | Rules and best practices for creating and modifying interactive web visualizers with automatic discovery. |
| **[`paper-brutalist-ui`](.agents/skills/paper-brutalist-ui/SKILL.md)** | Design tokens, `@repo/ui` component variants, kinetic micro-interactions, and anti-drift design rules. |
| **[`system-design-content-creator`](.agents/skills/system-design-content-creator/SKILL.md)** | Guidelines for writing Notes, LLD specs, and Labs with metadata and strict content isolation. |
| **[`monorepo-quality-guardian`](.agents/skills/monorepo-quality-guardian/SKILL.md)** | Fast feedback testing (`npm test`), Docker container verification, and pre-commit checks. |

> [!TIP]
> Read **[`AGENTS.md`](AGENTS.md)** for detailed instructions on agent personas, prompt templates, and the automated guardrails enforced by our test suite.

---

## 📜 License

This project is open-source software licensed under the **[MIT License](LICENSE)**. Feel free to use, modify, and build upon this code.

---

<div align="center">
  <sub>Engineered with precision by <a href="https://github.com/JyotimoyKashyap">Jyotimoy Kashyap</a></sub>
</div>
