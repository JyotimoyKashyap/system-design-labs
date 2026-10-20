---
title: "GitHub autoscaler Incident Report : 17 August 2026"
source: "https://www.youtube.com/shorts/vR3m5mDEafE"
author: "Xtream Droid"
published: ""
created: "2026-10-19T21:32:52+05:30"
description: "Enjoy the videos and music you love, upload original content, and share it all with friends, family, and the world on YouTube."
tags:
  - clippings
category: "Clipping"
---

![](https://www.youtube.com/watch?v=vR3m5mDEafE)

## Transcript

**0:00** · On the 17th of August, 2026, GitHub had a major global outage.

**0:04** · This is a summary of its incident report.

**0:06** · The root cause was GitHub's autoscaling policy.

**0:09** · It adds more copies of a service as the load increases.

**0:12** · GitHub services run with a sidecar, which manages the network traffic.

**0:16** · GitHub's autoscaler was monitoring the services, but not the sidecar.

**0:19** · As traffic in the data center increased, the sidecar maxed out in the total number of requests that can be passed in the network.

**0:25** · As it queued the requests in the network, the data center's load balancers ran out of network connections.

**0:29** · And every service, including the authentication service, that relies on load balancers started failing.

**0:34** · Every single feature of GitHub, including issues, pull requests, automated actions and Copilot failed.

**0:40** · This resulted in the clients trying to retry the request, which was a thundering herd, slowing down Copilot's recovery even more.

**0:45** · At its peak, more than half the requests to the services either slowed down or failed outright, and about 29 thousand organization repos were affected.

**0:52** · Eventually GitHub's engineers stopped the overloaded load balancers and blocked the retries, resulting in a data center recovery.

**0:58** · In future, GitHub will also monitor the network traffic per [[Sidecar Containers in Kubernetes]]and try to automatically scale them.

**1:03** · As an engineer, this highlights why load balancing, autoscaling and core system design fundamentals are still essential.

**1:08** · You can learn more about this at aiengg.dev.

**1:11** · Thanks for watching. Cheers!