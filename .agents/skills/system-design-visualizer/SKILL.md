---
name: system-design-visualizer
description: Guidelines and rules for building interactive Paper Brutalist web visualizers with automatic discovery in system-design-labs.
---

# System Design Visualizer (Paper Brutalist)

Use this skill whenever building or modifying an interactive web visualizer in the `system-design-labs` monorepo.

> **CRITICAL REPOSITORY GUIDELINES**
> 1. Read `DESIGN.md` at the repository root before making UI modifications.
> 2. The design system is **Paper Brutalist**: strictly **NO serif fonts**, strictly **NO rounded corners** (`rounded-none`), heavy black borders (`border-2` or `border-3 border-stone-900`), and hard offset drop shadows (`shadow-[4px_4px_0px_0px_rgba(28,25,23,1)]`).
> 3. Never invent ad-hoc styling; always consume `@repo/ui` tokens and variants.

---

## 1. Zero-Command Auto-Discovery
You do not need to register visualizers manually in routing tables or Astro page files.
- Place your visualizer component anywhere in a concept directory (e.g. `connection-pool/connection-visualizer.tsx` or `consistent-hashing/consistent-hashing-web/src/App.tsx`).
- Any file ending in `*visualizer*.tsx` or `*Visualizer*.tsx` is automatically discovered by `scripts/auto-discover-content.mjs`.
- It is dynamically registered in `apps/web/src/labs/registry.ts` and mounted on `/labs/<slug>`.

---

## 2. Mandatory Component & Variant Rules
Always import variants and components from `@repo/ui`:
```tsx
import { buttonVariants, cardVariants, badgeVariants } from '@repo/ui';
```

- **Buttons**:
  ```tsx
  <button className={buttonVariants({ variant: 'primary', size: 'sm' })}>
    Action
  </button>
  ```
  Supported variants: `'primary'` (Coral Pink), `'secondary'` (Mint Teal), `'tertiary'` (Stone 900), `'accent'` (Amber), `'ghost'`, `'destructive'`.

- **Cards & Panels**:
  ```tsx
  <div className={cardVariants({ variant: 'default', className: 'p-6' })}>
    ...
  </div>
  ```

- **Badges & Status Tags**:
  ```tsx
  <span className={badgeVariants({ variant: 'neutral' })}>
    Interactive Simulation
  </span>
  ```

---

## 3. Typography & Aesthetic Checklist
- **Headings**: Use `font-silkscreen font-bold not-italic text-stone-900` or `font-sans font-black tracking-tight`. **SERIF FONTS ARE STRICTLY FORBIDDEN**.
- **Data / Logs**: Use `font-mono text-xs text-stone-700`.
- **Canvas Background**: Always Paper `#fdfcfb` (`bg-[#fdfcfb]`).
- **Dividers & Borders**: Solid high-contrast borders (`border-stone-900` or `border-stone-300`).

---

## 4. Animation Guidelines (Framer Motion)
- Avoid animating SVG rotation via CSS `transform-origin` with Framer Motion `<motion.g>` (inconsistent across browsers).
- **Use trigonometry**: calculate precise `cx` and `cy` coordinates using `useTransform` and `Math.cos()` / `Math.sin()`.
- Provide a clean "Start Simulation" / "Reset" cycle with auto-cleanup of completed nodes/packets to avoid memory leaks.

---

## 5. Instant Verification
Before finishing any change:
```bash
npm test
```
This runs the entire 29+ test suite in under ~120ms, asserting token compliance, linter rules, and component registration.
