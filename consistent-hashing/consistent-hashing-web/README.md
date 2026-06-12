# Consistent Hashing Visualizer ⭕️

A beautiful, interactive visualizer demonstrating the **Consistent Hashing** algorithm, designed to distribute data seamlessly across nodes in a cluster!

![Aesthetic](https://img.shields.io/badge/Aesthetic-Brutalist-fdfcfb)
![Tech Stack](https://img.shields.io/badge/Stack-React%20%7C%20TypeScript%20%7C%20Vite-blue)
![Styling](https://img.shields.io/badge/Styling-Tailwind%20v4%20%7C%20Framer%20Motion-emerald)

## 🎯 What is Consistent Hashing?

In traditional hashing, if you have `N` servers, you might assign data to servers using `hash(key) % N`. However, if you add or remove a server, `N` changes, causing almost all data to be re-routed!

**Consistent Hashing** solves this by mapping both the **data** and the **servers (nodes)** onto a conceptual "ring" (a hash space). Data is assigned to the first node it encounters by moving clockwise around the ring. This guarantees that when a node is added or removed, only a small fraction of keys (`1/N`) needs to be remapped, enabling massive horizontal scalability for databases and caches (like Redis or Cassandra).

## ✨ Features

- **Interactive Ring Layout**: A clean, 16-slot circular topology visualizing the hash space.
- **Dynamic Node Management**: Add and remove storage nodes dynamically. Watch them snap onto the ring based on their hash value!
- **Simulation Loop**: Spawn simulated data traffic. Packets appear at their hashed coordinate on the ring and visually traverse the perimeter clockwise until they land at their assigned node.
- **Paper Brutalist UI**: A raw, unboxed, sharp aesthetic using Tailwind v4. 
- **Flawless Trigonometric Animation**: Data packets traverse the ring using native $x, y$ trigonometric calculations (`Math.cos` / `Math.sin`) in `framer-motion`, completely bypassing browser SVG rotation bugs.

## 🧠 Low-Level Design (TypeScript)

This web visualizer flawlessly replicates the backend Java logic entirely in the browser:

1. **`HashUtils.ts`:** Implements the fast `FNV-1a` string hashing logic to distribute keys evenly across the ring.
2. **`ConsistentHashing.ts`:** A decoupled class that manages the virtual ring, sorts nodes by hash, and implements binary search to quickly resolve `getTargetNode(hash)`.
3. **`HashRing.tsx`:** Uses `framer-motion` to orchestrate the topological layout and data packet physics.


