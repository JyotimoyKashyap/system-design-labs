---
title: "I challenged Rust Developers"
source: "https://www.youtube.com/shorts/FBs--2nSPLQ"
author: "arjay_the_dev"
published: ""
created: "2026-10-18T11:11:03+05:30"
tags:
  - clippings
category: "Clipping"
---

![](https://www.youtube.com/watch?v=FBs--2nSPLQ)

## Transcript

**0:00** · I challenged Rust developers to build an API that could support as many users as possible on a $12 server. Here's how they did it, and the results were pretty insane. For context, I started off with a pretty basic Rust app using Axum, which is a popular web framework. The application uses SQLite for the database, sits behind Nginx, and has no crazy tuning. This implementation supported about 14,000 concurrent users.

**0:17** · The challenge was the developers had to build an app with the exact same functionality, and they weren't allowed to use specific shortcuts like caching responses. But beyond that, they could optimize pretty much everything, and the Rust developers went crazy. The submission which ended up supporting the most users did not use Nginx. It didn't use a web framework, an asynchronous runtime, or an ORM. Instead, they wrote their own HTTP server on top of something called IOU ring. If you're not familiar, IOU ring is a relatively new Linux interface for asynchronous IO.

**0:42** · Instead of making separate system calls for every single read and write, your application can submit operations through shared ring buffers, which process them more efficiently. But this wasn't the only optimization. They also used multi-shot accept and receive, which let it handle multiple incoming network connections. They shrunk connection records down to 64 bytes and reused buffers. So handling requests required almost no new memory allocation. They grouped SQLite reads in a transaction, batched writes together, and even compiled SQLite from source to get a few more optimizations in. This let them support 64,000 concurrent users for 5 minutes.

**1:09** · That's more than four and a half times my original implementation with a P99 latency of just over 200 milliseconds and only 52% CPU utilization. I thought that CPU utilization was low, so I looked into it and found out that the implementation wasn't even the bottleneck. The virtual machine was. Every virtual user for the test kept a connection open, and Linux's connection tracking table capped out at about 65,000 entries. So when I pushed it 65,000, the kernel started dropping packets. Not because Rust couldn't handle more requests, but because Linux was capping how many connections we could have. Meaning this implementation could probably handle a lot more users.