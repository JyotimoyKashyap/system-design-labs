# 🎨 System Design Labs — Design System & UI/UX Rules

When building or modifying web visualizers (`*-web` projects) or pages inside this mono-repo, you **MUST STRICTLY** adhere to the "Paper Brutalist" design system.

---

## 🏛️ Single Source of Truth

All design tokens, component variants, and theme variables are centralized:
- **Tokens**: [`packages/ui/src/tokens.ts`](file:///Users/jyotimoykashyap/Developer/personal-lab/packages/ui/src/tokens.ts) (colors, shadows, borders, typography, spacing)
- **Living Showcase / Styleguide**: [`apps/web/src/pages/design.astro`](file:///Users/jyotimoykashyap/Developer/personal-lab/apps/web/src/pages/design.astro) & [`packages/ui/src/components/DesignShowcase.tsx`](file:///Users/jyotimoykashyap/Developer/personal-lab/packages/ui/src/components/DesignShowcase.tsx)
- **Live Preview URL**: `http://localhost:4321/design`

> [!IMPORTANT]
> **Rule for Future AI Agents & Developers:**
> If you need to change colors, button styles, card borders, fonts, or shadows, **DO NOT** edit individual pages or components across the repo.
> Edit [`packages/ui/src/tokens.ts`](file:///Users/jyotimoykashyap/Developer/personal-lab/packages/ui/src/tokens.ts) or [`packages/ui/src/components/ui/Button.tsx`](file:///Users/jyotimoykashyap/Developer/personal-lab/packages/ui/src/components/ui/Button.tsx).
> Then verify your changes live at `http://localhost:4321/design`.

---

## 🎨 Core Color Palette

| Token | Name | HEX | Purpose / Usage |
|---|---|---|---|
| `colors.primary` | Coral Pink | `#FF6B6B` | Primary CTAs, "Launch App", hero triggers |
| `colors.secondary` | Mint Turquoise | `#4ECDC4` | Secondary actions, "Read Docs", "Read Note", "Read Design" |
| `colors.tertiary` | Pure White | `#FFFFFF` | Tertiary buttons, card backgrounds, panels |
| `colors.dark` | Stone 900 Ink | `#1C1917` | Text, borders, high-contrast dark buttons |
| `colors.accent.amber` | Amber Yellow | `#FBBF24` | GitHub pills, badges, highlights |
| `colors.accent.orange` | Vivid Orange | `#F97316` | Interactive lab tags, warning alerts |
| `colors.accent.emerald` | Emerald Green | `#10B981` | Success states, active node status indicators |
| `colors.background` | Off-white Paper | `#FDFCFB` | Page canvas background |

---

## 🌙 Dark Mode Palette & Architecture ("Obsidian Paper")

Dark mode preserves the physical paper brutalist aesthetic in high-contrast "Obsidian Paper" night mode:

| Token | Name | HEX | Purpose / Usage |
|---|---|---|---|
| `darkColors.background` | Obsidian Canvas | `#0C0A09` | Dark mode page canvas background |
| `darkColors.surface` | Stone 900 Surface | `#1C1917` | Card backgrounds, drawer panels, navbar |
| `darkColors.surfaceMuted` | Stone 800 Surface | `#292524` | Secondary code panels, table zebra stripes |
| `darkColors.ink` | Crisp White Ink | `#FDFCFB` | Primary headings, body prose, high-contrast labels |
| `darkColors.border` | Stone 700 Border | `#44403C` | Sharp brutalist borders in dark mode (`border-stone-700`) |
| `darkColors.shadow` | Pitch Black Shadow | `#000000` | Solid unblurred offset drop shadows |
| Accent Retainers | Coral / Mint / Amber | `#FF6B6B` / `#4ECDC4` / `#FBBF24` | Uncompromised vibrant brand accents |

### ⚙️ Dark Mode Mechanics:
1. **Zero-FOUC Bootstrapper**: An inline script executes in the `<head>` of [`BaseLayout.astro`](file:///Users/jyotimoykashyap/Developer/personal-lab/apps/web/src/layouts/BaseLayout.astro) before DOM rendering, preventing flash of unstyled content.
2. **System Preference First**: Defaults to `window.matchMedia('(prefers-color-scheme: dark)')`. If the user's OS is in dark mode, the site opens in dark mode; otherwise light.
3. **Manual Override & Persistence**: The toggle button on the right side of [`Navbar.astro`](file:///Users/jyotimoykashyap/Developer/personal-lab/apps/web/src/components/Navbar.astro) toggles modes and persists the user preference in `localStorage.getItem('theme')`.
4. **Tailwind v4 Variant**: Declared via `@variant dark (&:where(.dark, .dark *));` in [`apps/web/src/styles/global.css`](file:///Users/jyotimoykashyap/Developer/personal-lab/apps/web/src/styles/global.css).
5. **Dynamic Mermaid Theme**: Code diagram blocks dynamically adapt between `neutral` and `dark` themes when toggled or loaded.

---

## 🔲 Geometry & Shadows

- **NO ROUNDED CORNERS**: Unconditionally enforce `rounded-none` across all UI elements, buttons, badges, modals, and cards (graphs/nodes are exceptions and can be circular).
- **Solid Offset Shadows**: Do NOT use soft drop-shadows or blurs. Always use hard, offset brutalist shadows:
  - **Buttons**: `shadow-[3px_3px_0px_0px_rgba(28,25,23,1)]`
  - **Cards**: `shadow-[8px_8px_0px_0px_rgba(28,25,23,1)]`
  - **Badges**: `shadow-[2px_2px_0px_0px_rgba(28,25,23,1)]`
- **Borders**: Sharp, dark borders (`border-2 border-stone-900` or `border-3 border-stone-900`).

---

## ⚡ Kinetic Micro-Interactions (Hover & Active)

All interactive elements must provide tactile physical feedback:
- **Button Hover**: `hover:-translate-x-0.5 hover:-translate-y-0.5 hover:shadow-[5px_5px_0px_0px_rgba(28,25,23,1)]` (lifts up and expands shadow)
- **Button Active (Click)**: `active:translate-x-0.5 active:translate-y-0.5 active:shadow-[1px_1px_0px_0px_rgba(28,25,23,1)]` (pushes down into the paper)
- **Card Hover**: `hover:-translate-x-1 hover:-translate-y-1 hover:shadow-[12px_12px_0px_0px_rgba(28,25,23,1)]`

---

## ✍️ Typography Standards

1. **Brand / Display / Hero Headers**: `Silkscreen` (`font-silkscreen not-italic text-stone-900 tracking-tight`)
2. **Body & Paragraphs**: `Inter` (`font-sans text-stone-700 leading-relaxed`)
3. **Buttons, Badges, Metadata, Code**: `JetBrains Mono` (`font-mono uppercase font-bold tracking-wider`)
4. **NO SERIF**: Do not use Playfair Display or italic serif fonts.

---

## 📦 Shared Component APIs (`@repo/ui`)

Always import and use standard variants rather than writing custom Tailwind strings:

```tsx
// React (Visualizers & Components)
import { Button, Card, Badge, buttonVariants, cardVariants, badgeVariants } from '@repo/ui';

<Button variant="primary" size="default">Launch App</Button>
<Button variant="secondary" size="sm">Read Docs</Button>
<Button variant="accent" size="sm">GitHub ↗</Button>
```

```astro
---
// Astro Pages & Templates
import { buttonVariants, cardVariants } from '@repo/ui';
---

<a href="/labs/raft" class={buttonVariants({ variant: 'primary' })}>
  Launch App
</a>

<div class={cardVariants({ variant: 'interactive', className: 'p-6' })}>
  ...
</div>
```

---

## 🧪 Automated Design System Enforcement Tests

We enforce all design rules automatically via a dedicated test suite in [`tests/design-system.test.mjs`](file:///Users/jyotimoykashyap/Developer/personal-lab/tests/design-system.test.mjs).

To run the tests:
```bash
npm run test:design
# or
npm test
```

### What These Tests Enforce:
1. **Token Contracts**: Checks that `#FF6B6B`, `#4ECDC4`, `#fdfcfb`, and all required tokens exist and match specifications.
2. **Variant Dynamics**: Asserts that `buttonVariants` contains the required kinetic micro-interaction classes (`hover:-translate-x-0.5`, `active:translate-x-0.5`, `rounded-none`, etc.).
3. **No Serif Fonts**: Automatically scans all `.astro`, `.tsx`, and `.ts` files and **fails the build** if `font-serif` or `Playfair` is introduced.
4. **No Arbitrary Rounded Corners**: Automatically scans all pages and components and **fails** if `rounded-sm/md/lg/xl` is detected.
5. **Button Consistency**: Asserts that all standard pages consume `buttonVariants` instead of reinventing custom classes.

