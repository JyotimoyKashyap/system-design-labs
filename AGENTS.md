# AI Agents & Assistants 🤖

This repository was heavily assisted and pair-programmed by AI agents, specifically the **Antigravity** agent by Google DeepMind.

---

## 🛠️ Specialized Skills for AI Agents
When working in this repository, activate the specialized skills located in `.agents/skills/` (and `.gemini/config/skills/`):

1. **[`system-design-visualizer`](./.agents/skills/system-design-visualizer/SKILL.md)**: Rules for creating and modifying interactive Paper Brutalist web visualizers with automatic discovery.
2. **[`system-design-content-creator`](./.agents/skills/system-design-content-creator/SKILL.md)**: Guidelines for writing Notes, Low-Level Designs (LLD), and Labs with metadata and strict content isolation.
3. **[`paper-brutalist-ui`](./.agents/skills/paper-brutalist-ui/SKILL.md)**: Design tokens, `@repo/ui` component variants, and anti-drift design rules.
4. **[`monorepo-quality-guardian`](./.agents/skills/monorepo-quality-guardian/SKILL.md)**: Fast feedback testing (`npm test`), Docker verification, and pre-commit checks.

---

## 📜 Core Agent Guardrails

### 1. Mandatory README Metadata (Enforced by Test Suite)
Every project directory **MUST** contain a `README.md` with either:
- A `<!-- hub-metadata -->` comment block with `title` and `description`
- OR YAML frontmatter (`--- ... ---`) with `title` and `description`

Missing metadata will immediately fail `npm test` via [`tests/metadata-linter.test.mjs`](./tests/metadata-linter.test.mjs).

```markdown
<!-- hub-metadata
type: lab | visualizer | lld | note
title: Concept Name
description: Short, clear description of the architecture.
tag: Distributed Systems
tagColor: #f97316
-->
```

### 2. Zero-Command Auto-Discovery
You do **NOT** need to edit routing tables or Astro page files when adding content:
- Drop a directory at the monorepo root (e.g. `connection-pool/`).
- `*visualizer*.tsx` &rarr; Discovered and mounted at `/labs/<slug>`.
- `notes.md` / `architecture.md` &rarr; Discovered and published at `/notes/<slug>`.
- `lld.md` / `design.md` &rarr; Discovered and published at `/lld/<slug>`.
- `README.md` &rarr; Discovered as documentation at `/labs/<slug>/docs`.
- Everything auto-syncs via `predev` and `prebuild` npm hooks!

### 3. Strict Content Isolation (No Forced Cross-Linking)
Keep specific content strictly in its dedicated section:
- Notes appear in `/notes` and `/notes/[slug]` (linking back only to `/notes`).
- LLD appears in `/lld` and `/lld/[slug]` (linking back only to `/lld`).
- Labs appear in `/labs` and `/labs/[slug]` (linking back only to `/labs`).
- **DO NOT** inject forced cross-linking banners or cards into articles.

### 4. Paper Brutalist UI & Design System
All styling is centralized in `@repo/ui`:
- **Single Source of Truth**: [`packages/ui/src/tokens.ts`](./packages/ui/src/tokens.ts) and [`packages/ui/src/variants.ts`](./packages/ui/src/variants.ts)
- **Living Style Guide**: [`http://localhost:4321/design`](http://localhost:4321/design)
- **Prohibited**: **NO SERIF FONTS** (Playfair Display / font-serif), **NO ROUNDED CORNERS** (`rounded-none` only), no ad-hoc classes on pages.

---

## ⚡ Instant Feedback Loop

Run the test suite anytime for instant feedback:
```bash
npm test
```
The test suite executes 29+ checks across tokens, anti-drift linters, metadata validation, auto-discovery, and Docker configuration in **~100ms**.

### Docker Containerized Builds
To build and run without touching local host dependencies:
```bash
# Build the standardized container:
npm run docker:build

# Run locally on port 3000:
npm run docker:run

# Or with Docker Compose:
docker compose up --build
```
