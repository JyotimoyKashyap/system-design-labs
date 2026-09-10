---
name: system-design-deployer
description: Best practices and rules for deploying a new web visualizer to the system-design-labs monorepo.
---

# System Design Labs Deployment Guide 🚀

This skill provides the strict, step-by-step process for deploying a new interactive web visualizer to GitHub Pages and the GitHub Container Registry (GHCR) within the `system-design-labs` monorepo.

> **CRITICAL ARCHITECTURE NOTE**
> The repository uses a unified Docker pipeline to compile all visualizers, but it uses a GitHub Action (`.github/workflows/deploy-pages.yml`) to specifically extract the compiled static HTML/JS/CSS assets from the Docker image and deploy them to GitHub Pages.

When instructed to deploy or wire up a new visualizer, follow these exact steps in order:

## 1. Prerequisites: Documentation Check
Before wiring up deployment, the visualizer **must** have a comprehensive `README.md` file located inside its web project directory (e.g., `concept-name/concept-name-web/README.md`). 
- If it uses the default Vite README, **replace it** with a detailed overview of the system design concept, the UI features, and the low-level technical design. 
- The `scripts/build-docs.sh` script relies on this `README.md` to generate the documentation page for the Hub.

## 2. Update the Dockerfile
The `Dockerfile` is the single source of truth for the CI build.
1. In **Stage 1 (builder)**, add the Vite build command for the new visualizer. Always use `--base=./` to ensure relative asset loading on GitHub Pages.
   ```dockerfile
   RUN cd concept-name/concept-name-web && npx vite build --base=./
   ```
2. In **Stage 2 (nginx)**, add a `COPY` command to pull the built `dist` folder into the Nginx `html` directory.
   ```dockerfile
   COPY --from=builder /app/concept-name/concept-name-web/dist /usr/share/nginx/html/concept-name-web
   ```

## 3. Configure Hub Documentation (Automated Metadata Tag)
The documentation build process is fully automated. You do **not** need to manually write `npx marked` commands in shell scripts.
Add an invisible `<!-- hub-metadata -->` HTML comment block at the top of your visualizer's or blog's `README.md`. It is completely invisible in Markdown previews on GitHub:

```markdown
<!-- hub-metadata
type: visualizer
tag: Distributed Systems
tagColor: #10b981
title: Concept Name Visualizer
description: Short description of what the lab visualizes.
appLink: ./concept-name-web/
-->
```

For standalone **blogs** or LLD projects:
```markdown
<!-- hub-metadata
type: blog
tag: System Design LLD
tagColor: #2563eb
title: Project Title LLD
description: Short description of the architecture.
-->
```

## 4. Generate Documentation & Sync Hub
Run the automated docs generator:
```bash
npm run build:docs
```
This script will:
- Auto-discover all tagged `README.md` and Markdown files.
- Automatically compile styled HTML documentation with GitHub markdown CSS, highlight.js, and interactive Mermaid.js.
- Automatically synchronize and register the card inside `deploy-hub/data.json`.
- Automatically manage `.gitignore` for generated static assets.

## 5. Update the Root README
The main repository `README.md` tracks all available labs.
- Add a new numbered entry under the `## 📂 Projects Overview` section.
- Provide a clear, one-sentence description and link to the correct subdirectory.

## 6. Verify and Commit
1. Verify the pipeline locally by running `docker build -t test-deploy .` at the root of the repository. Ensure all steps complete successfully.
2. Commit the changes. Once pushed to the `main` branch, the GitHub Action will automatically run the build, extract the generated `deploy-hub` assets, and publish them to GitHub Pages.
