---
name: monorepo-quality-guardian
description: Procedures for maintaining codebase quality, running the comprehensive test suite, Docker builds, and preventing drift.
---

# Monorepo Quality Guardian

Use this skill whenever verifying pull requests, reviewing code changes, or ensuring zero drift in `system-design-labs`.

---

## 1. Fast Feedback Loop (<150ms)
The project provides a comprehensive, sub-second test suite running with Node's native test runner:

```bash
npm test
```

### What `npm test` Validates:
1. **Design System & Tokens**: Ensures core brand colors, typography, shadows, and borders match `packages/ui/src/tokens.ts`.
2. **Codebase Linter**:
   - Strictly prohibits serif fonts (`Playfair Display`, `font-serif`).
   - Strictly prohibits arbitrary rounded corners (`rounded-md`, `rounded-lg`, etc.).
   - Asserts all pages consume `buttonVariants` and `cardVariants`.
3. **Metadata Linter (`tests/metadata-linter.test.mjs`)**:
   - Asserts that every project root directory contains a `README.md` with non-empty `title` and `description` (in frontmatter or `<!-- hub-metadata -->`).
   - Asserts that all Notes, LLDs, and Labs content files conform strictly to Astro schemas.
4. **Auto-Discovery & Registry (`tests/auto-discovery.test.mjs`)**:
   - Asserts that every visualizer in `apps/web/src/labs/registry.ts` resolves to an existing file on disk.
   - Tests mock folder discovery lifecycle end-to-end.
5. **Content Isolation (`tests/content-isolation.test.mjs`)**:
   - Asserts that Notes and LLD pages only link back to their respective directory and contain no forced cross-links.
6. **Docker Pipeline (`tests/docker-pipeline.test.mjs`)**:
   - Asserts Dockerfile multi-stage build, Nginx configuration, and docker-compose settings.

---

## 2. Docker Pipeline Verification
To verify that the application builds cleanly in an isolated Linux environment without local dependency interference:

```bash
npm run docker:build
# Run container locally:
npm run docker:run
# Or using compose:
docker compose up --build
```

---

## 3. Pre-Commit Checklist for AI Agents & Developers
Before submitting any task:
1. Run `npm test` &rarr; Ensure **all 29+ tests pass**.
2. If adding a new concept directory, ensure `README.md` includes metadata.
3. If adding UI, use `@repo/ui` variants and `rounded-none`.
4. Ensure no uncommitted scratch files or temporary tests are left behind.
