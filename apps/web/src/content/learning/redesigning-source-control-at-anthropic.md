---
title: "Redesigning source control at Anthropic"
description: "Interesting piece from Anthropic describing why and how they built their own custom alternative to Git, based on the dramatic increase in rate of changes to their code after the …"
publishedAt: "2026-10-17"
category: "Clipping"
tags: ["clippings"]
readTimeMinutes: 1
sourceUrl: "https://simonwillison.net/2026/Oct/13/redesigning-source-control-at-anthropic/"
author: "Simon Willison"
featured: false
---

"Antique" is their new code review system. The source control system is called Scion, and sounds to me like a reinvention of centralized source control along the lines of Subversion.

(**Update**: Anthropic credit Google's [Piper](https://en.wikipedia.org/wiki/Piper_\(source_control_system\)) and Meta's [Sapling](https://sapling-scm.com/) as "prior art" - it looks like the core concept of "derived data" [was heavily inspired by Mononoke](https://sunshowers.io/posts/anthropic-mononoke/), the server component of Sapling.)

They had some pretty extreme requirements:

> The history of our main branch alone is almost 100 GB, and a *compressed* working copy is over 25 GB. We perform on the order of 25 million code checkouts per day for CI, before counting developer environments, agent sandboxes, and research jobs. Fetched naively, that would be over 600 PB of git traffic per day.

The new system was built by a team of seven people, and the migration from Git was completed in five months.