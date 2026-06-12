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

## 3. Update the Documentation Generator
The `scripts/build-docs.sh` script converts markdown into styled HTML pages.
1. Add the new visualizer's directory to the `mkdir -p` command at the top of the script so the output directory is created.
   ```bash
   mkdir -p ... deploy-hub/concept-name-web
   ```
2. Add the `npx marked` command to compile the `README.md` you verified in step 1 into the `deploy-hub`.
   ```bash
   npx marked concept-name/concept-name-web/README.md | cat template.html - <(echo "  </div></body></html>") > deploy-hub/concept-name-web/docs.html
   ```

## 4. Update the Landing Page (Hub)
The `deploy-hub/index.html` file serves as the unified landing page. It is fully data-driven using Vanilla JS.
- Open `deploy-hub/data.json` and append a new JSON object for the visualizer.
  ```json
  {
    "id": "concept-name",
    "tag": "Distributed Systems",
    "tagColor": "#10b981",
    "tagTextColor": "#fff",
    "title": "Concept Name Visualizer",
    "description": "Short description of what the lab visualizes.",
    "appLink": "./concept-name-web/",
    "docsLink": "./concept-name-web/docs.html"
  }
  ```
- You do **not** need to touch `deploy-hub/index.html`. It will automatically fetch and render the new card based on the JSON.

## 5. Update the Root README
The main repository `README.md` tracks all available labs.
- Add a new numbered entry under the `## 📂 Projects Overview` section.
- Provide a clear, one-sentence description and link to the correct subdirectory.

## 6. Verify and Commit
1. Verify the pipeline locally by running `docker build -t test-deploy .` at the root of the repository. Ensure all steps complete successfully.
2. Commit the changes. Once pushed to the `main` branch, the GitHub Action will automatically run the build, extract the generated `deploy-hub` assets, and publish them to GitHub Pages.
