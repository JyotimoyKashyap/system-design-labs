---
title: "How the Claude Code team does code review"
description: "How do you review code at the volume agents produce it? On the Claude Code team, once we had high coverage and deep reviews, we started optimizing for the minimum amount of human review time that doesn't raise reverts or incidents."
publishedAt: "2026-10-15"
category: "Clipping"
tags: ["clippings"]
readTimeMinutes: 2
sourceUrl: "https://claude.dev/blog/solving-code-review-at-anthropic/#layer-by-layer"
author: "Addy Osmani"
featured: false
---

Noise costs more when the author is an agent. A person can wave off a nit in seconds. An agent fixes it, which means another commit and another round of review.

**Review has to converge.** An agent author and an agent reviewer can keep each other busy indefinitely. One CLI PR went through more than fifty rounds of review and fixes overnight before its author stopped it.

When we ran git blame on what the reviewer kept flagging, about 59% of it was code that had been there since the reviewer's first pass. The reviewer was rarely finding new bugs, it was finding a few more old ones each round.

We made it worse ourselves. We'd gotten used to running several rounds by default, since each pass catches something new. This can be worthwhile on a risky change, but not on every PR. The team now tracks rounds-to-pass alongside recall, and changed re-review:

- **A round cap.** After three rounds, the reviewer asks the author to step back and reconsider the approach.
- **Diff-only re-review** after the first few passes.
- **Flag the class of bug,** so the author fixes every instance, not one line.
- **No repeats** of findings already posted.

Review agents also run nightly over existing code, helping us catch old bugs before they show up somewhere else.