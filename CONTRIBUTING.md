# Contributing to System Design Labs 🛠️

Thank you for your interest in contributing to **System Design Labs**! 

This repository is an open-source engineering workbench and interactive laboratory for distributed systems, concurrency primitives, and production-grade Low-Level Design (LLD). Whether you are contributing a new interactive visualizer, an LLD solution, architectural deep-dives, or improving documentation, this guide will help you get started smoothly.

---

## 🧭 Monorepo Architecture at a Glance

This repository is built around **Zero-Command Auto-Discovery**. You do **not** need to manually edit routing tables, registry arrays, or Astro pages when adding new content.

```
personal-lab/
├── apps/
│   └── web/                # Astro 5 + React 19 web application & documentation hub
├── packages/
│   ├── ui/                 # @repo/ui: Centralized Paper Brutalist design system tokens & components
│   └── shared/             # Shared TypeScript utilities
├── <system-directory>/     # Independent system directories at root (e.g. cache-lld/, rabbitmq-lab/)
│   ├── README.md           # Documentation with mandatory <!-- hub-metadata --> block
│   ├── *visualizer*.tsx    # (Optional) Interactive React visualizer mounted at /labs/<slug>
│   ├── lld.md              # (Optional) Low-Level Design specification published at /lld/<slug>
│   └── notes.md            # (Optional) Architectural note published at /notes/<slug>
├── knowledge/              # Markdown notes, incident clippings, and distributed systems deep-dives
├── scripts/
│   └── auto-discover-content.mjs  # Auto-discovery compiler run before dev, build, and test
└── tests/                  # Fast node:test suite (~150ms) enforcing metadata and anti-drift rules
```

---

## ⚡ Quickstart: Local Development

### 1. Prerequisites
- **Node.js**: v20+ (recommended v22+)
- **npm**: v10+
- **Docker & Docker Compose**: (Optional, for running multi-container distributed labs or containerized builds)

### 2. Setup & Running
Clone the repository and install workspace dependencies:

```bash
# Clone the repository
git clone https://github.com/JyotimoyKashyap/system-design-labs.git
cd system-design-labs

# Install all monorepo dependencies
npm install

# Start the interactive development hub (Astro + Vite)
npm run dev
```

Navigate to **[http://localhost:4321](http://localhost:4321)** to access the live hub with instant Hot Module Replacement (HMR).

### 3. Containerized Run (Zero Local Toolchains)
To verify the production build in a hermetic Docker environment matching our deployment:

```bash
# Build and run the multi-stage container
docker compose up --build -d
```
Then open **[http://localhost:3000](http://localhost:3000)**.

---

## 🧪 Testing & Anti-Drift Quality Gates

We enforce high code quality and prevent UI drift using an ultra-fast test suite running on Node's native `--test` runner.

Run the test suite anytime:
```bash
npm test
```

> [!NOTE]
> The test suite executes 39+ automated checks in **~150ms**:
> - **Metadata validation**: Ensures all projects contain required metadata headers.
> - **Visualizer resolution**: Confirms all discovered visualizers resolve to valid TypeScript components.
> - **Anti-drift linting**: Enforces Paper Brutalist design system tokens (no arbitrary rounded corners, no serif fonts).
> - **Content isolation**: Verifies clean separation between Labs, LLD, and Notes.

---

## 🏷️ Mandatory Metadata Standards

Every new project directory at the monorepo root **must** contain a `README.md` with a metadata block. This enables the auto-discovery engine to categorize and publish it correctly.

Add this comment block (or equivalent YAML frontmatter) at the very top of your project `README.md`:

```markdown
<!-- hub-metadata
type: lab | visualizer | lld | note
title: Distributed Token Bucket Rate Limiter
description: Interactive simulation of token bucket rate limiting across an API Gateway cluster.
tag: Distributed Systems
tagColor: #f97316
category: Distributed Systems
-->
```

### Supported Metadata Fields:
| Field | Type | Description |
| :--- | :--- | :--- |
| `type` | `string` | One of `lab`, `visualizer`, `lld`, or `note` |
| `title` | `string` | Clear, concise title of the system or experiment |
| `description` | `string` | 1–2 sentence description of the architecture |
| `tag` | `string` | Display category pill (e.g., `Distributed Systems`, `Concurrency`, `Algorithms`) |
| `tagColor` | `string` | Hex code for tag accent (e.g. `#f97316`, `#3b82f6`, `#10b981`) |

---

## 🎨 Design System: Paper Brutalist UI

All frontend UI components adhere to our **Paper Brutalist** design language defined in [`@repo/ui`](./packages/ui):

* **Single Source of Truth**: Use tokens and variants from `@repo/ui` (`packages/ui/src/tokens.ts` and `packages/ui/src/variants.ts`).
* **Living Style Guide**: View all components, buttons, badges, and cards at **[http://localhost:4321/design](http://localhost:4321/design)**.
* **Prohibited Styling**:
  - ❌ **NO SERIF FONTS** (Playfair Display / font-serif). Use monospace or crisp sans-serif only.
  - ❌ **NO ROUNDED CORNERS** on cards, buttons, or badges (`rounded-none` only).
  - ❌ **NO AD-HOC BUTTONS**: Consume `buttonVariants` from `@repo/ui`.
  - ❌ **NO ARBITRARY SHADOWS**: Use brutalist hard-offset shadows (`shadow-[3px_3px_0px_0px_rgba(0,0,0,1)]`).

---

## 📐 How to Add a Low-Level Design (LLD)

We hold LLD implementations to production engineering standards. If you are adding a new design:

1. **Create Directory**: Create a root folder named `<concept>-lld` (e.g. `rate-limiter-lld/`).
2. **Author Specification (`lld.md`)**:
   - **Problem Statement & Requirements**: Clear functional and non-functional requirements.
   - **Class & Architecture Diagrams**: Visualized using GitHub-flavored Mermaid (`classDiagram`, `sequenceDiagram`).
   - **Design Patterns**: Document pattern choices (Strategy, Factory, Observer, State, etc.) and rationale.
   - **Concurrency & Thread Safety**: Detail synchronization primitives, lock striping, or lock-free atomics.
3. **Provide Clean Source Code**:
   - Idiomatic Java / TypeScript / Go code organized into clean packages (`model/`, `service/`, `strategy/`, `repository/`).
4. **Empirical Verification**:
   - Include unit tests verifying both business correctness and multithreaded concurrency safety under contention.
5. **Add `README.md`**: Include the `<!-- hub-metadata -->` block.

---

## 🔬 How to Add an Interactive Web Visualizer

1. **Create Directory**: Create a root directory (e.g. `consistent-hashing/`).
2. **Add Component**: Name the primary component `*visualizer*.tsx` (e.g. `consistent-hashing-visualizer.tsx`) or house it inside a `*-web` subpackage.
3. **Simulate Real Behavior**:
   - Provide true event-loop simulation, state ticks, and latency controls (pause, play, speed slider, step).
   - Visualize edge cases and failure modes (chaos engineering, node drops, partition splits).
4. **Auto-Discovery**: Run `npm test` — the auto-discovery engine will immediately register your visualizer into `apps/web/src/labs/registry.ts` and generate `/labs/<slug>`.

---

## 📥 Pull Request Workflow

1. **Fork & Branch**: Create a feature branch from `main`:
   ```bash
   git checkout -b feat/raft-chaos-simulation
   ```
2. **Develop & Test**: Ensure all tests pass:
   ```bash
   npm test
   ```
3. **Commit Conventions**: Use concise, conventional commit messages:
   - `feat(visualizer): add virtual node distribution to consistent hashing`
   - `feat(lld): implement thread-safe splitwise balance sheet`
   - `fix(metadata): add missing description tag to kafka lab`
   - `docs(readme): clarify local development prerequisites`
4. **Open a PR**: Open a pull request against `main`. Ensure your PR description includes:
   - Summary of the changes.
   - Link to any related issue or design discussion.
   - Confirmation that `npm test` passed locally.

---

## 🤖 Supercharging Contributions with AI Agents & Skills

If you pair-program with AI coding assistants (such as Google DeepMind's **Antigravity**, Claude Code, or Cursor), this repository provides built-in agent specifications and domain skills in [`.agents/`](.agents/) to accelerate your development:

### Available AI Agents & Skills
- **Designing an LLD?** 
  - Invoke the **[`lld-mentor`](.agents/agents/lld-mentor.md)** subagent: Guides you through Socratic design refinement, class definitions, and empirical multithreaded test harnesses.
  - Read the **[`low-level-design-architect`](.agents/skills/low-level-design-architect/SKILL.md)** skill for Mermaid syntax rules and structural templates.
- **Building a Visualizer?**
  - Use the **[`system-design-visualizer`](.agents/skills/system-design-visualizer/SKILL.md)** skill: Covers interactive canvas/SVG state loops and reactive controls.
  - Check the **[`paper-brutalist-ui`](.agents/skills/paper-brutalist-ui/SKILL.md)** skill: Ensures all components consume canonical `@repo/ui` tokens and avoid prohibited design patterns (no serif fonts, no rounded corners).
- **Writing Documentation & Notes?**
  - Follow the **[`system-design-content-creator`](.agents/skills/system-design-content-creator/SKILL.md)** skill: Ensures proper frontmatter metadata and strict content isolation between Labs, Notes, and LLDs.
- **Pre-commit Verification?**
  - Use the **[`monorepo-quality-guardian`](.agents/skills/monorepo-quality-guardian/SKILL.md)** skill: Guides you through running `npm test` and checking Docker container builds.

Refer to **[`AGENTS.md`](AGENTS.md)** for detailed prompt conventions and agent workflows.

---

## 📜 Licensing

By contributing to **System Design Labs**, you agree that your contributions will be licensed under the project's [MIT License](./LICENSE).
