---
name: system-design-visualizer
description: Best practices and rules for creating a "Paper Brutalist" interactive web visualizer in the system-design-labs monorepo.
---

# System Design Visualizer (Paper Brutalist)

Use this skill whenever the user asks you to build or modify a web visualizer (`*-web`) inside the `system-design-labs` monorepo.

> **CRITICAL REPOSITORY GUIDELINES**
> Before you begin building or modifying any UI components, you **MUST** read the `DESIGN.md` file located at the root of the user's workspace (`/Users/jyotimoykashyap/Developer/personal-lab/DESIGN.md`). It contains the strict, foundational rules for the "Paper Brutalist" design language central to this repository. Do not guess the design language—refer to `DESIGN.md` first.

## 1. Project Initialization & Directory Structure
- **CRITICAL DIRECTORY RULE**: There should only be **ONE** top-level directory for a given system design concept (e.g., `consistent-hashing`). The web visualizer must be nested inside that concept's directory alongside any backend implementations (e.g., `consistent-hashing/consistent-hashing-web`). Do not create visualizer projects at the root of the monorepo.
- Scaffold a new Vite React TS app in the nested folder: `npx -y create-vite@latest ./ --template react-ts`
- Ensure the root `package.json`'s `workspaces` array is updated to include the new path (e.g., `"consistent-hashing/*-web"`).
- Modify the `package.json` to link the central UI components: `"@repo/ui": "*"`
- Install necessary packages: `tailwindcss`, `@tailwindcss/vite`, `framer-motion`, `lucide-react`.
- Ensure `vite.config.ts` includes `tailwindcss()`.
- Run `npm install` at the **root** of the monorepo to link workspaces.

## 2. Tailwind Configuration & UI Components
- In `src/index.css`, you MUST include:
  ```css
  @import "tailwindcss";
  @source "../../packages/ui/src";
  ```
- Failure to include the `@source` directive will result in unstyled components.
- Always use the `<Button>` and `<Input>` components from `@repo/ui`. Do not recreate standard HTML buttons or inputs with custom Tailwind classes. 

## 3. The "Paper Brutalist" Aesthetic
- Background MUST be `#fdfcfb`. 
- Text is `stone-800` or `stone-900`. 
- Primary buttons are `orange-500` or `stone-900`.
- **NO ROUNDED CORNERS**: Unconditionally enforce `rounded-none`. (Circles for nodes/points in SVG are exceptions).
- Use solid brutalist drop shadows: `shadow-[4px_4px_0px_0px_rgba(28,25,23,1)]`.
- Use a stark screen layout dividing the page into two parts (e.g., 40% left controls, 60% right visualization) using a full-height border `border-r-2 border-stone-900` instead of bounded container boxes.
- Typography: Serif headers (`font-serif italic text-stone-900`), and monospaced text for event logs and data ids (`font-mono`).

## 4. Animation Guidelines (Framer Motion)
- For SVG animations (especially circular or perimeter movements), **AVOID** using CSS `transform-origin` combined with `rotate()` strings inside Framer Motion `<motion.g>`. Browsers render this inconsistently for SVG elements.
- **Instead, use trigonometry.** Animate an abstract angle using `useMotionValue`, and calculate precise `cx` and `cy` coordinates using `useTransform` with `Math.cos()` and `Math.sin()`. 
  ```tsx
  const angle = useMotionValue(startAngle);
  const cx = useTransform(angle, a => center + radius * Math.cos((a - 90) * (Math.PI / 180)));
  const cy = useTransform(angle, a => center + radius * Math.sin((a - 90) * (Math.PI / 180)));
  ```

## 5. Interaction & Simulation
- **Avoid manual typing** where possible. For example, instead of making the user type a new node name, provide an "Add Node" button that auto-generates names sequentially (Node A, Node B, etc.).
- Build a **Simulation Loop**. Provide a "Start Simulation" button that dynamically spawns traffic/messages, animates them through the system (using a sequential id like `msg_1`, `msg_2` for reliable hashing if needed), and then despawns them to keep the UI clean.
