---
name: system-design-content-creator
description: Instructions for authoring Notes, Low-Level Designs (LLD), and Labs with metadata and strict content isolation in system-design-labs.
---

# System Design Content Creator

Use this skill whenever creating or editing content (Notes, LLDs, Labs) in the `system-design-labs` monorepo.

---

## 1. Zero-Command Content Publishing
The monorepo features a GitOps auto-discovery engine. When you create a directory (e.g. `connection-pool/`), you can write any combination of content:

| Content Type | File Pattern | Published Route | Target Section |
| :--- | :--- | :--- | :--- |
| **Interactive Lab** | `*visualizer*.tsx` | `/labs/<slug>` | Interactive Visualizers |
| **Lab Documentation** | `README.md` | `/labs/<slug>/docs` | Lab Architecture Docs |
| **System Design Note** | `notes.md`, `note.md`, `architecture.md` | `/notes/<slug>` | Notes Directory |
| **Low-Level Design** | `lld.md`, `design.md`, `*-lld/` | `/lld/<slug>` | LLD Directory |

---

## 2. Mandatory Metadata Standards (Enforced by Tests)
Every `README.md` and content article **MUST** include metadata. The automated test suite (`npm test`) will fail if metadata is missing.

### Format A: Invisible HTML Comment (`<!-- hub-metadata -->`)
Ideal for `README.md` files so that GitHub previews remain clean:
```markdown
<!-- hub-metadata
type: lab | visualizer | lld | note
title: Consistent Hashing Visualizer
description: Interactive demonstration of hash ring partitioning and minimal node rebalancing.
tag: Distributed Systems
tagColor: #eab308
-->
```

### Format B: YAML Frontmatter
Ideal for `.md` and `.mdx` files:
```yaml
---
title: "API Servers at Scale — The Full Picture"
description: "From naive unpooled servers to thread pools, connection pools, and distributed server fleets."
publishedAt: "2026-08-15"
category: "Architecture"
tags: ["Backend", "Connection Pooling", "Scalability"]
---
```

---

## 3. Strict Content Isolation Rule (No Forced Cross-Linking)
**DO NOT** inject cross-domain promotional links or banners:
- Notes must strictly link back to `/notes` ("Back to All Notes").
- LLDs must strictly link back to `/lld` ("Back to All LLD").
- Labs must strictly link back to `/labs` ("Back to Labs Directory").
- Keep specific content in its specific location unless the author manually writes natural Markdown links in body prose.

---

## 4. Verification
Run the instant test runner:
```bash
npm test
```
The `tests/metadata-linter.test.mjs` test will automatically verify that all project READMEs and content collections satisfy the schemas.
