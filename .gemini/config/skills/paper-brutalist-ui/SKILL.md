---
name: paper-brutalist-ui
description: Deep dive into the Paper Brutalist design language, design tokens, and component variants in @repo/ui.
---

# Paper Brutalist UI & Design System

This repository enforces a strict, uncompromising **Paper Brutalist** aesthetic across all web interfaces.

---

## 1. Core Design Tokens (`packages/ui/src/tokens.ts`)
The single source of truth is located in `@repo/ui`:

- **Brand Palette**:
  - `colors.primary.bg`: `#FF6B6B` (Coral Pink)
  - `colors.secondary.bg`: `#4ECDC4` (Mint Teal)
  - `colors.background`: `#FDFCFB` (Paper White Canvas)
  - `colors.dark.bg`: `#1C1917` (Deep Stone 900 Pitch)
  - `colors.surface`: `#FFFFFF` (Card White)

- **Typography**:
  - **Display**: `'Silkscreen', cursive` (Hero titles, badges, section labels)
  - **Body**: `'Inter', sans-serif` (Paragraphs, descriptions, clean prose)
  - **Mono**: `'JetBrains Mono', 'Space Mono', monospace` (Code, metadata tags, metric readouts)
  - **PROHIBITED**: Any serif fonts (`font-serif`, `Playfair Display`, Georgia, Times).

- **Borders & Corners**:
  - All corners **MUST** be `rounded-none`.
  - Borders are heavy: `border-2` or `border-3 border-stone-900`.

- **Shadows**:
  - Solid, offset brutalist shadows: `shadow-[4px_4px_0px_0px_rgba(28,25,23,1)]`.

---

## 2. Component Variants (`packages/ui/src/variants.ts`)
Never write ad-hoc inline Tailwind strings for buttons or cards. Use the pure TypeScript variant functions:

```tsx
import { buttonVariants, cardVariants, badgeVariants } from '@repo/ui';

// Paper Brutalist Buttons
<button className={buttonVariants({ variant: 'primary', size: 'md' })}>Launch</button>
<a href="/labs" className={buttonVariants({ variant: 'secondary', size: 'sm' })}>Directory</a>
<a href="/docs" className={buttonVariants({ variant: 'tertiary', size: 'sm' })}>Docs</a>

// Paper Brutalist Cards
<div className={cardVariants({ variant: 'default', className: 'p-6' })}>...</div>

// Sharp Brutalist Badges
<span className={badgeVariants({ variant: 'accent' })}>NEW</span>
```

---

## 3. Living Design Showcase
To inspect all components, variants, and color tokens interactively:
- Visit `http://localhost:4321/design` in development.
- Powered by `packages/ui/src/components/DesignShowcase.tsx`.

---

## 4. Automated Verification
Run:
```bash
npm test
# or
npm run test:design
```
This tests for serif font leaks, accidental rounded corners, and token mismatches across the repository.
