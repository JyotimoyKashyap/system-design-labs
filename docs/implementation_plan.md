# Architecture & Implementation Plan: Unified Platform for JyotimoyKashyap.me

A comprehensive blueprint to evolve this repository into a unified personal website (**`jyotimoykashyap.me`**) incorporating a personal portfolio, high-level system design blogs, low-level design (LLD) case studies, and interactive distributed systems visualizers. The architecture targets an ultra-low-cost AWS serverless model (\$0.00/mo infrastructure) managed via Cloudflare DNS and automated GitOps CI/CD.

---

## Homepage Design Blueprint (Inspired by ArpitBhayani.me in Paper Brutalism)

We scraped and analyzed [arpitbhayani.me](https://arpitbhayani.me/) (which is also built with Astro 5!). We will translate its high-impact, content-centric layout into our strict **Paper Brutalist** design system ([`DESIGN.md`](file:///Users/jyotimoykashyap/Developer/personal-lab/DESIGN.md)):

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│ [Jyotimoy Kashyap]                                [Blogs] [LLD] [Labs] [Resume] [GitHub]│
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  Hey, I am Jyotimoy                                          ┌───────────────────────┐  │
│  distributed systems, backend architecture, and LLD.          │                       │  │
│                                                              │  [Profile Image]      │  │
│  I am a software engineer focused on distributed systems,     │  border-3 stone-900   │  │
│  backend architectures, and high-performance services.       │  shadow-[8px_8px...]  │  │
│  I build interactive labs to explore consensus algorithms,   │  rounded-none         │  │
│  caching mechanics, and concurrency primitives.              └───────────────────────┘  │
│                                                                                         │
│  [ GitHub ]   [ LinkedIn ]   [ Twitter/X ]   [ Resume PDF ]                             │
│  (Brutalist offset-shadow pill buttons)                                                 │
│                                                                                         │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│  EXPLORE WORK & WRITINGS                                                                │
│  Everything I build, write, publish, and engineer in the open.                          │
│                                                                                         │
│  ┌──────────────────────┐ ┌──────────────────────┐ ┌──────────────────────┐             │
│  │ INTERACTIVE     6    │ │ ARCHITECTURE   BLOGS │ │ OBJECT-ORIENTED LLD  │             │
│  │ Interactive Labs     │ │ System Design Blogs  │ │ Low-Level Design     │             │
│  │ Browser simulations  │ │ Deep dives into API  │ │ In-memory caches,    │             │
│  │ of Raft, Kafka,      │ │ scaling 0-1, caching │ │ DAOs, and concurrent │             │
│  │ Bloom Filter & Ring  │ │ & replication.       │ │ patterns.            │             │
│  │                      │ │                      │ │                      │             │
│  │ Launch labs →        │ │ Read blogs →         │ │ Explore LLD →        │             │
│  └──────────────────────┘ └──────────────────────┘ └──────────────────────┘             │
│                                                                                         │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│  RECENT CONTENT COLUMNS                                                                 │
│  ┌─────────────────────────────────┐   ┌─────────────────────────────────┐              │
│  │ Recent System Design Blogs      │   │ Recent LLD & Interactive Labs   │              │
│  │ • [Date] API Servers at Scale   │   │ • [Lab] Consistent Hashing Ring │              │
│  │ • [Date] Distributed Caching    │   │ • [LLD] Extensible Cache Engine │              │
│  │ • [Date] Database Replication   │   │ • [Lab] Raft Leader Election    │              │
│  │ [View full archive →]           │   │ [View all labs →]               │              │
│  └─────────────────────────────────┘   └─────────────────────────────────┘              │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

### Brutalist Adaptations of Arpit's Elements:
* **Geometry:** Absolutely **no rounded corners** (`rounded-none` on cards, image frames, buttons, and badges).
* **Borders & Depth:** Stark `border-3 border-stone-900` with solid offset shadows (`shadow-[6px_6px_0px_0px_rgba(28,25,23,1)]`) that depress on hover (`hover:-translate-x-1 hover:-translate-y-1 hover:shadow-[10px_10px_0px_0px_rgba(28,25,23,1)]`).
* **Canvas:** `#fdfcfb` with subtle graph-grid background lines.
* **Typography:** Elegant Playfair/Serif italics for headings (`Hey, I am Jyotimoy`, `Explore Work & Writings`) paired with technical monospaced uppercase tags (`font-mono text-xs text-stone-500`).

---

## Scope & Target Directory Layout (`apps/web`)

```text
apps/web/
├── package.json                 # Astro 5, React 19, Tailwind v4, @repo/ui
├── astro.config.mjs             # Configured with React, MDX, and Tailwind
├── tsconfig.json
├── src/
│   ├── content.config.ts        # Zod content schemas for blogs, lld, and labs
│   ├── content/
│   │   ├── blogs/               # .mdx files for macro system design
│   │   ├── lld/                 # .mdx files for low-level design
│   │   └── labs/                # .json files for interactive lab metadata
│   ├── styles/
│   │   └── global.css           # Tailwind v4 import & paper brutalist base styles
│   ├── components/
│   │   ├── Navbar.astro         # Sticky Brutalist top navigation
│   │   ├── Footer.astro         # Brutalist footer with socials & copyright
│   │   ├── ExploreCard.astro    # Brutalist category card with counter & hover
│   │   └── SocialButton.astro   # Brutalist social link button
│   ├── layouts/
│   │   ├── BaseLayout.astro     # Global layout (head, font imports, nav, footer)
│   │   └── ContentLayout.astro  # Prose reader layout for blogs & LLD
│   └── pages/
│       ├── index.astro          # Arpit-inspired Brutalist Homepage
│       ├── blogs/
│       │   ├── index.astro      # Filterable blog archive
│       │   └── [slug].astro     # Dynamic blog article reader
│       ├── lld/
│       │   ├── index.astro      # Filterable LLD catalog
│       │   └── [slug].astro     # Dynamic LLD deep dive reader
│       └── labs/
│           ├── index.astro      # Interactive lab showroom gallery
│           └── [slug].astro     # Dedicated full-screen interactive simulation
```

---

## Execution Steps

### 1. Workspace & Scaffolding
* Update root `package.json` to include `"apps/*"` in workspaces.
* Initialize `apps/web` with Astro 5, `@astrojs/react`, `@astrojs/mdx`, `@tailwindcss/vite`, `tailwindcss@4`, `lucide-react`, and `@repo/ui`.
* Configure `astro.config.mjs` and `apps/web/src/styles/global.css`.

### 2. Content Schemas & Data Seeding
* Implement `apps/web/src/content.config.ts` with Zod collections.
* Seed initial content from current repo:
  * `blogs/scale-architecture-0-1.md` -> `apps/web/src/content/blogs/scale-architecture-0-1.mdx`
  * `cache-lld/README.md` -> `apps/web/src/content/lld/cache-lld.mdx`
  * `task-management-system/README.md` -> `apps/web/src/content/lld/task-management-system.mdx`
  * Metadata for all 6 visualizers -> `apps/web/src/content/labs/*.json`

### 3. Layouts & Front Page
* Create `BaseLayout.astro`, `Navbar.astro`, `Footer.astro`.
* Build `index.astro` implementing the Arpit-inspired hero, bio, social badges, Explore Work & Writings grid, and recent content columns.

### 4. Archive & Reader Routes
* Build `/blogs`, `/blogs/[slug]`, `/lld`, `/lld/[slug]`, `/labs`, and `/labs/[slug]`.

---

## Verification Plan

### Automated Verification
* `npm install` runs cleanly across monorepo.
* `npm --workspace=apps/web run build` compiles static HTML without schema errors.

### Visual & Interactive Verification
* Launch dev server at `http://localhost:4321`.
* Verify homepage typography, 0-radius brutalist cards, offset shadows, and responsiveness.
* Verify clicking category cards navigates to `/blogs`, `/lld`, and `/labs`.
