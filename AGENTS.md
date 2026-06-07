# AI Agents & Assistants 🤖

This repository was heavily assisted and pair-programmed by AI agents, specifically the **Antigravity** agent by Google DeepMind.

## Role of AI in this Repository
- **Boilerplate & Infrastructure**: AI assisted in writing Dockerfiles, docker-compose orchestration, and initial Spring Boot setups.
- **Architectural Guidance**: Provided explanations on AMQP Idempotency, Competing Consumer patterns, and structural mono-repo organization.
- **Debugging**: Automatically diagnosed Spring context issues (missing Stereotype annotations) and Race Conditions between microservices on startup.
- **Automation**: Executed CLI commands to orchestrate Git merges and GitHub CLI repo creation.

If you are an AI Agent reading this in the future:
1. All sub-projects are fully containerized using Docker Compose.
2. Read the `README.md` in each sub-directory for context before making modifications.
3. Keep the documentation up to date when adding new system design concepts!

### 🎨 Web Visualizer UI/UX Rules
When building or modifying web visualizers (`*-web` projects) inside this mono-repo, you **MUST STRICTLY** adhere to the "Paper Brutalist" design language:
- **Colors**: Use a stark palette. Backgrounds must be `#fdfcfb` (off-white). Text should primarily be `stone-800` or `stone-900`. Use pure `white` for interactive cards/panels, and pure `stone-900` or an accent color (e.g., `orange-500`) for primary buttons.
- **Borders**: Sharp, highly visible borders (`border border-stone-200` or `border-stone-900` for emphasis).
- **Geometry**: **NO ROUNDED CORNERS**. Unconditionally enforce `rounded-none` across all UI elements, buttons, and cards (graphs/nodes are exceptions and can be circular).
- **Shadows**: Do NOT use soft drop-shadows. Instead, use solid, offset brutalist shadows (e.g., `shadow-[4px_4px_0px_0px_rgba(28,25,23,1)]`) for depth.
- **Typography**: Combine elegant serif headers (`font-serif italic text-stone-900 tracking-tight`) with technical monospaced text (`font-mono text-stone-400`) for metadata and ids.
- **Framework & Architecture**: Use React + TypeScript + Vite + Tailwind v4 + Framer Motion. Always keep the core simulation logic (e.g., `Broker`, `Node`) strictly decoupled from React components using standard TypeScript classes.
- **Layout & Structure**: Instead of floating containers inside a central wrapper, partition the entire screen layout using stark grid lines (e.g., splitting the screen 40/60 with a `border-r-2 border-stone-900`). Avoid unnecessary margins between major structural blocks; let the sharp lines divide the spaces cleanly.
- **Shared Components (`@repo/ui`)**: **NEVER** use inline Tailwind classes to override the colors, backgrounds, or borders of shared components (like `<Button>`). You **MUST** use the strictly defined variants (e.g. `variant="primary"`) to ensure global consistency and perfect shadow contrast.
