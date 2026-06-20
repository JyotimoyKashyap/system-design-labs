# API Rate Limiter Visualizer 🚀

An interactive, Paper Brutalist web visualizer demonstrating the **Distributed Token Bucket** algorithm in a microservice architecture.

## Overview
This visualizer showcases how API traffic is managed and throttled across a distributed cluster using Redis. Rate limiting is a crucial system design concept used to protect APIs from being overwhelmed, mitigate DDoS attacks, and enforce usage quotas per IP address or user tier.

## Architecture & Implementation
The system is composed of several key components:
- **Clients**: Users generating traffic. Each user is identified by an IP address.
- **Proxy**: The central ingress point that intercepts all client requests.
- **Redis Shards**: A distributed key-value store (simulated as Shards 1, 2, and 3). The proxy hashes the incoming IP address to determine which shard holds the token bucket for that specific user.
- **API Server**: The backend service that actually processes the request. A request only reaches the API Server if the Proxy receives a positive authorization from the designated Redis Shard.

## The Algorithm: Sliding Window / Token Bucket
This simulation utilizes a hybrid Token Bucket / Sliding Window algorithm:
1. **Capacity**: The system allows a maximum of 3 requests per IP address within the active window.
2. **Execution**: When a request arrives, the Proxy queries the Redis shard. If the bucket has capacity, the token is consumed and the request flows to the API Server. If empty, the request is immediately rejected and dropped at the Proxy level.
3. **Decay (Reset)**: Every 10 seconds, the sliding window "decays", completely resetting all token buckets across the distributed cluster.

## UI & Interactions
- **Manual Overload**: You can manually click on the User boxes to aggressively spam traffic.
- **Visual Feedback**: When an IP hits the rate limit (3 requests), the User node visually degrades (dashed border, gray background) and displays a "RATE LIMITED" warning.
- **Auto-Simulation**: Use the "Start Traffic" button to unleash an automated chaos monkey that randomly fires requests from different users across the network.
- **Proxy Logs**: A live log terminal tracks every request, showing which shard was queried and whether the token was `ALLOWED` or `BLOCKED`.

## Technology Stack
- Built with **React** & **TypeScript**
- Styled with **Tailwind CSS v4** using the repository's native Paper Brutalist design system (`@repo/ui`).
- Animations driven by **Framer Motion** for precise SVG token flow mapping.
