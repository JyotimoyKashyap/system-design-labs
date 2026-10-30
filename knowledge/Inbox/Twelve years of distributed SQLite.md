---
title: "Twelve years of distributed SQLite"
source: "https://philipotoole.com/twelve-years-of-distributed-sqlite/"
author: "Philip O'Toole"
published: "2026-10-23T21:16:26-04:00"
created: "2026-10-24T16:56:00+05:30"
description: "In September 2014 I published a short post announcing rqlite. The idea was simple: take SQLite, put a Raft log in front of it, and replicate every write to a cluster so that the loss of a node did not mean the loss of the data. The source went on GitHub that same weekend. As…"
tags:
  - clippings
category: "Clipping"
---
![](https://philipotoole.com/wp-content/uploads/2014/09/rqlite-design.png)

In September 2014 I published a [short post announcing](https://philipotoole.com/replicating-sqlite-using-raft-consensus/) [rqlite](https://rqlite.io). The idea was simple: take [SQLite](https://sqlite.org/), put a [Raft log](https://raft.github.io) in front of it, and replicate every write to a cluster so that the loss of a node did not mean the loss of the data. The [source went on GitHub](https://github.com/rqlite/rqlite) that same weekend. As far as I can tell, that was the first open-source system to combine Raft and SQLite. Nobody else was doing it, and for several years nobody else tried.

Today “distributed SQLite” is a category with a dozen projects in it, a few of them venture-funded, two of them acquired. Since the people now writing about the category tend to start the story around 2021, I thought I should write down the version that starts in 2014. I was there for all of it.