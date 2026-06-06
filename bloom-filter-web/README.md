# Bloom Filter Visualizer 🌸

A fully client-side, interactive visualizer for the **Bloom Filter** probabilistic data structure!

![Aesthetic](https://img.shields.io/badge/Aesthetic-Brutalist%20%2F%20Claude-fdfcfb)
![Tech Stack](https://img.shields.io/badge/Stack-React%20%7C%20TypeScript%20%7C%20Vite-blue)
![Styling](https://img.shields.io/badge/Styling-Tailwind%20v4%20%7C%20Framer%20Motion-emerald)

## 🎯 Features

- **Interactive Bit Array:** View the exact `m = 128` bits representing the internal state of the filter.
- **Real-Time Hashing:** Type a word and watch the three independent hashing algorithms (`FNV-1a`, `DJB2`, and `SDBM`) calculate their respective bit indices instantly.
- **Probabilistic Checks:** Try adding items, then check if they exist! Discover how the structure guarantees **No False Negatives** but allows for **False Positives**.
- **Brutalist UI:** A sharp, aesthetic interface utilizing massive data grids and perfectly squared components, powered by Tailwind v4.

## 🧠 Low-Level Design (TypeScript)

This web visualizer flawlessly replicates the backend Java logic entirely in the browser:

1. **`HashProvider.ts`:** Implements fast, non-cryptographic string hashing logic (`FNV-1a`, `DJB2`, `SDBM`) to generate $k=3$ uniformly distributed index locations.
2. **`BloomFilter.ts`:** A decoupled class that manages the underlying boolean array, exposing `add(item)` and `check(item)` methods returning the computed indices for the UI to animate.
3. **`BitGrid.tsx`:** Uses `framer-motion` to physically scale and highlight the bits in the array when hashes are calculated!

## 🚀 How to Run Locally

1. **Clone the repository** (if you haven't already).
2. **Navigate to the project directory:**
   ```bash
   cd bloom-filter-web
   ```
3. **Install dependencies:**
   ```bash
   npm install
   ```
4. **Start the development server:**
   ```bash
   npm run dev
   ```
5. **Open your browser:** Visit `http://localhost:5173`.
6. Add items and watch the magic!
