import test from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

// Import tokens and variants from pure TypeScript source files
import { colors, typography, shadows, borders } from "../packages/ui/src/tokens.ts";
import { buttonVariants, cardVariants, badgeVariants } from "../packages/ui/src/variants.ts";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const rootDir = path.resolve(__dirname, "..");
const webSrcDir = path.resolve(rootDir, "apps/web/src");
const uiSrcDir = path.resolve(rootDir, "packages/ui/src");

// Helper to recursively collect files matching extensions
function getFilesRecursively(dir, extensions = [".astro", ".tsx", ".ts", ".jsx", ".js"]) {
  const results = [];
  if (!fs.existsSync(dir)) return results;

  const entries = fs.readdirSync(dir, { withFileTypes: true });
  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      if (entry.name !== "node_modules" && entry.name !== ".astro" && entry.name !== "dist") {
        results.push(...getFilesRecursively(fullPath, extensions));
      }
    } else if (entry.isFile() && extensions.some(ext => entry.name.endsWith(ext))) {
      results.push(fullPath);
    }
  }
  return results;
}


// ============================================================================
// 1. DESIGN TOKENS INTEGRITY SUITE
// ============================================================================

test("Tokens: Core brand palette defines all canonical colors", () => {
  assert.equal(colors.primary.bg, "#FF6B6B", "Primary color must be Coral Pink #FF6B6B");
  assert.equal(colors.secondary.bg, "#4ECDC4", "Secondary color must be Mint Teal #4ECDC4");
  assert.equal(colors.background, "#fdfcfb", "Canvas background must be Paper #fdfcfb");
  assert.equal(colors.dark.bg, "#1c1917", "Dark pitch color must be Stone 900 #1c1917");
  assert.equal(colors.surface, "#ffffff", "Surface must be Pure White #ffffff");

  assert.ok(colors.accent.amber, "Accent palette must include amber");
  assert.ok(colors.accent.orange, "Accent palette must include orange");
  assert.ok(colors.accent.emerald, "Accent palette must include emerald");
});

test("Tokens: Typography follows Paper Brutalist hierarchy", () => {
  assert.match(typography.display, /Silkscreen/i, "Display font must use Silkscreen");
  assert.match(typography.body, /Inter/i, "Body font must use Inter");
  assert.match(typography.mono, /JetBrains Mono|Space Mono/i, "Mono font must use JetBrains or Space Mono");
});

test("Tokens: Shadows enforce solid offset brutalist depth", () => {
  for (const [key, shadowVal] of Object.entries(shadows)) {
    assert.match(
      shadowVal,
      /rgba\(28,25,23,1\)|rgba\(28, 25, 23, 1\)|rgba\(0,0,0,1\)/,
      `Shadow token '${key}' must use solid, unblurred brutalist offset shadows (not soft blur)`
    );
  }
});


// ============================================================================
// 2. COMPONENT VARIANTS & MICRO-INTERACTIONS SUITE
// ============================================================================

test("buttonVariants: Generates Paper Brutalist kinetic micro-interactions", () => {
  const primaryBtn = buttonVariants({ variant: "primary" });
  
  // Color contract
  assert.match(primaryBtn, /#FF6B6B/, "Primary button must have #FF6B6B background");
  
  // Kinetic micro-interactions
  assert.match(primaryBtn, /hover:-translate-x-0\.5/, "Button must lift up-left on hover (-x)");
  assert.match(primaryBtn, /hover:-translate-y-0\.5/, "Button must lift up-left on hover (-y)");
  assert.match(primaryBtn, /hover:shadow-\[5px_5px_0px_0px/, "Button must expand shadow on hover");
  assert.match(primaryBtn, /active:translate-x-0\.5/, "Button must press down on click");
  
  // Geometry
  assert.match(primaryBtn, /rounded-none/, "Button must have rounded-none geometry");
  assert.match(primaryBtn, /border-2/, "Button must have solid border-2");
  assert.match(primaryBtn, /font-mono/, "Button text must use monospace");
});

test("buttonVariants: Generates secondary and accent variants correctly", () => {
  const secBtn = buttonVariants({ variant: "secondary" });
  assert.match(secBtn, /#4ECDC4/, "Secondary button must have #4ECDC4 background");

  const accentBtn = buttonVariants({ variant: "accent" });
  assert.match(accentBtn, /amber-400/, "Accent button must use amber background");

  const darkBtn = buttonVariants({ variant: "dark" });
  assert.match(darkBtn, /stone-900/, "Dark button must use stone-900 background");
});

test("cardVariants: Generates heavy brutalist borders and hover elevations", () => {
  const interactiveCard = cardVariants({ variant: "interactive" });
  
  assert.match(interactiveCard, /border-3 border-stone-900/, "Interactive card must have heavy border-3");
  assert.match(interactiveCard, /shadow-\[8px_8px_0px_0px/, "Interactive card must start with 8px brutalist shadow");
  assert.match(interactiveCard, /hover:-translate-x-1 hover:-translate-y-1/, "Interactive card must lift on hover");
  assert.match(interactiveCard, /hover:shadow-\[12px_12px_0px_0px/, "Interactive card must expand shadow to 12px on hover");
  assert.match(interactiveCard, /rounded-none/, "Cards must strictly be rounded-none");
});

test("badgeVariants: Generates sharp brutalist label tags", () => {
  const badge = badgeVariants({ variant: "primary" });
  assert.match(badge, /rounded-none/, "Badges must strictly be rounded-none");
  assert.match(badge, /border-2 border-stone-900/, "Badges must have border-2");
  assert.match(badge, /shadow-\[2px_2px_0px_0px/, "Badges must have 2px brutalist shadow");
  assert.match(badge, /#FF6B6B/, "Primary badge must have #FF6B6B background");
});


// ============================================================================
// 3. CODEBASE LINTER & STATIC ENFORCEMENT SUITE
// ============================================================================

test("Codebase Linter: Prohibition of Serif Fonts (Playfair Display / font-serif)", () => {
  const files = [
    ...getFilesRecursively(webSrcDir),
    ...getFilesRecursively(uiSrcDir),
  ];

  const forbiddenSerifRegex = /\bfont-serif\b|Playfair\s*Display|playfair-display/i;
  const violations = [];

  for (const file of files) {
    const content = fs.readFileSync(file, "utf8");
    if (forbiddenSerifRegex.test(content)) {
      violations.push(path.relative(rootDir, file));
    }
  }

  assert.equal(
    violations.length,
    0,
    `Prohibited serif fonts found in files:\n  ${violations.join("\n  ")}\nAll headers must be font-silkscreen not-italic, font-sans, or font-mono.`
  );
});

test("Codebase Linter: Prohibition of arbitrary rounded corners on cards and buttons", () => {
  const files = [
    ...getFilesRecursively(path.join(webSrcDir, "pages")),
    ...getFilesRecursively(path.join(webSrcDir, "components")),
    ...getFilesRecursively(path.join(uiSrcDir, "components")),
  ];

  // We strictly forbid soft curves: rounded-sm, rounded-md, rounded-lg, rounded-xl, rounded-2xl, rounded-3xl
  const forbiddenRoundedRegex = /\brounded-(?:sm|md|lg|xl|2xl|3xl)\b/;
  const violations = [];

  for (const file of files) {
    const content = fs.readFileSync(file, "utf8");
    if (forbiddenRoundedRegex.test(content)) {
      violations.push(path.relative(rootDir, file));
    }
  }

  assert.equal(
    violations.length,
    0,
    `Prohibited rounded corner classes (rounded-md/lg/xl) found in:\n  ${violations.join("\n  ")}\nPaper Brutalism strictly requires rounded-none.`
  );
});

test("Codebase Linter: Standard pages consume buttonVariants instead of ad-hoc classes", () => {
  const pagesToCheck = [
    path.join(webSrcDir, "pages/index.astro"),
    path.join(webSrcDir, "pages/labs/index.astro"),
    path.join(webSrcDir, "pages/lld/index.astro"),
    path.join(webSrcDir, "pages/notes/index.astro"),
  ];

  for (const pagePath of pagesToCheck) {
    const content = fs.readFileSync(pagePath, "utf8");
    assert.ok(
      content.includes("buttonVariants"),
      `Page ${path.basename(pagePath)} must import and use buttonVariants from @repo/ui`
    );
    assert.ok(
      content.includes("cardVariants"),
      `Page ${path.basename(pagePath)} must import and use cardVariants from @repo/ui`
    );
  }
});

test("Codebase Linter: Living Design Showcase page exists and embeds DesignShowcase", () => {
  const showcasePage = path.join(webSrcDir, "pages/design.astro");
  assert.ok(fs.existsSync(showcasePage), "Living design showcase page /design (design.astro) must exist");

  const content = fs.readFileSync(showcasePage, "utf8");
  assert.match(content, /<DesignShowcase/, "design.astro must mount <DesignShowcase /> component");
});

test("Codebase Linter: Global CSS defines Paper Brutalist theme variables", () => {
  const globalCss = path.join(webSrcDir, "styles/global.css");
  assert.ok(fs.existsSync(globalCss), "global.css must exist");

  const content = fs.readFileSync(globalCss, "utf8");
  assert.match(content, /--color-brand-primary:\s*#FF6B6B/i, "global.css must register brand-primary as #FF6B6B");
  assert.match(content, /--color-brand-secondary:\s*#4ECDC4/i, "global.css must register brand-secondary as #4ECDC4");
  assert.match(content, /--color-brand-paper:\s*#FDFCFB/i, "global.css must register brand-paper as #FDFCFB");
});

// ============================================================================
// 4. AUTO-DISCOVERY & CONTENT ISOLATION SUITE
// ============================================================================

test("Auto-Discovery: Visualizer registry and DynamicVisualizer are properly configured", () => {
  const registryPath = path.join(webSrcDir, "labs/registry.ts");
  assert.ok(fs.existsSync(registryPath), "apps/web/src/labs/registry.ts must exist");

  const registryContent = fs.readFileSync(registryPath, "utf8");
  assert.match(registryContent, /export const visualizerRegistry/i, "registry.ts must export visualizerRegistry");
  assert.match(registryContent, /export function hasVisualizer/i, "registry.ts must export hasVisualizer");

  const dynamicVisualizerPath = path.join(webSrcDir, "components/DynamicVisualizer.tsx");
  assert.ok(fs.existsSync(dynamicVisualizerPath), "DynamicVisualizer.tsx must exist");

  const slugPage = path.join(webSrcDir, "pages/labs/[slug].astro");
  const slugContent = fs.readFileSync(slugPage, "utf8");
  assert.match(slugContent, /<DynamicVisualizer/i, "labs/[slug].astro must mount <DynamicVisualizer />");
});

test("Auto-Discovery: Discovered content adheres to strict collection schemas", () => {
  const labsDir = path.join(webSrcDir, "content/labs");
  const notesDir = path.join(webSrcDir, "content/notes");
  const lldDir = path.join(webSrcDir, "content/lld");

  assert.ok(fs.existsSync(labsDir) && fs.readdirSync(labsDir).length >= 6, "Labs collection must contain foundational labs");
  assert.ok(fs.existsSync(notesDir) && fs.readdirSync(notesDir).length >= 1, "Notes collection must contain notes");
  assert.ok(fs.existsSync(lldDir) && fs.readdirSync(lldDir).length >= 2, "LLD collection must contain LLD entries");

  // Verify all labs frontmatter
  for (const file of fs.readdirSync(labsDir)) {
    if (!file.endsWith(".md") && !file.endsWith(".mdx")) continue;
    const content = fs.readFileSync(path.join(labsDir, file), "utf8");
    assert.match(content, /^---\r?\n/m, `Lab ${file} must have frontmatter`);
    assert.match(content, /id:\s*"[^"]+"/m, `Lab ${file} must specify id`);
    assert.match(content, /title:\s*"[^"]+"/m, `Lab ${file} must specify title`);
    assert.match(content, /description:\s*"[^"]+"/m, `Lab ${file} must specify description`);
  }

  // Verify all notes frontmatter
  for (const file of fs.readdirSync(notesDir)) {
    if (!file.endsWith(".md") && !file.endsWith(".mdx")) continue;
    const content = fs.readFileSync(path.join(notesDir, file), "utf8");
    assert.match(content, /^---\r?\n/m, `Note ${file} must have frontmatter`);
    assert.match(content, /title:\s*"[^"]+"/m, `Note ${file} must specify title`);
    assert.match(content, /publishedAt:\s*"[^"]+"/m, `Note ${file} must specify publishedAt`);
  }

  // Verify all LLD frontmatter
  for (const file of fs.readdirSync(lldDir)) {
    if (!file.endsWith(".md") && !file.endsWith(".mdx")) continue;
    const content = fs.readFileSync(path.join(lldDir, file), "utf8");
    assert.match(content, /^---\r?\n/m, `LLD ${file} must have frontmatter`);
    assert.match(content, /title:\s*"[^"]+"/m, `LLD ${file} must specify title`);
    assert.match(content, /difficulty:\s*"(?:Medium|Hard)"/m, `LLD ${file} must specify valid difficulty`);
  }
});

test("Content Isolation: Dedicated sections preserve location independence without forced cross-linking", () => {
  const contentLayoutPath = path.join(webSrcDir, "layouts/ContentLayout.astro");
  const layoutContent = fs.readFileSync(contentLayoutPath, "utf8");

  // ContentLayout should NOT have hardcoded cross-link injection between notes and labs
  assert.ok(!layoutContent.includes("Accompanying Visualizer"), "ContentLayout must not force cross-links into notes");
  assert.ok(!layoutContent.includes("Related LLD Design"), "ContentLayout must not force cross-links into notes");
});

