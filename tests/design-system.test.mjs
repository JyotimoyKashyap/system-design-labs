import test from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

// Import tokens and variants from pure TypeScript source files
import { colors, darkColors, darkShadows, themeTokens, typography, shadows, borders } from "../packages/ui/src/tokens.ts";
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

test("Tokens: Dark mode palette defines obsidian, stone surfaces, and crisp ink", () => {
  assert.equal(darkColors.background, "#0c0a09", "Dark canvas background must be obsidian #0c0a09");
  assert.equal(darkColors.surface, "#1c1917", "Dark surface must be Stone 900 #1c1917");
  assert.equal(darkColors.surfaceMuted, "#292524", "Dark muted surface must be Stone 800 #292524");
  assert.equal(darkColors.ink, "#fdfcfb", "Dark ink must be crisp paper #fdfcfb");
  assert.equal(darkColors.border, "#44403c", "Dark border must be Stone 700 #44403c");
  assert.equal(darkColors.shadow, "#000000", "Dark shadow must be pure black #000000");
  assert.equal(themeTokens.dark.colors.background, "#0c0a09");
  assert.equal(themeTokens.light.colors.background, "#fdfcfb");
});

test("Tokens: Shadows enforce solid offset brutalist depth", () => {
  for (const [key, shadowVal] of Object.entries(shadows)) {
    assert.match(
      shadowVal,
      /rgba\(28,25,23,1\)|rgba\(28, 25, 23, 1\)|rgba\(0,0,0,1\)/,
      `Shadow token '${key}' must use solid, unblurred brutalist offset shadows (not soft blur)`
    );
  }
  for (const [key, shadowVal] of Object.entries(darkShadows)) {
    assert.match(
      shadowVal,
      /rgba\(0,0,0,1\)|#000000/,
      `Dark shadow token '${key}' must use solid black offset shadows`
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

test("Variants: Components provide dark mode class adaptations", () => {
  const primaryBtn = buttonVariants({ variant: "primary" });
  assert.match(primaryBtn, /dark:border-stone-100/, "Button must use bright border in dark mode");
  assert.match(primaryBtn, /dark:shadow-\[3px_3px_0px_0px_rgba\(0,0,0,1\)\]/, "Button must use solid black shadow in dark mode");

  const darkBtn = buttonVariants({ variant: "dark" });
  assert.match(darkBtn, /dark:bg-stone-100/, "Dark button in dark mode must invert to stone-100");
  assert.match(darkBtn, /dark:text-stone-900/, "Dark button in dark mode must invert text to stone-900");

  const interactiveCard = cardVariants({ variant: "interactive" });
  assert.match(interactiveCard, /dark:bg-stone-900/, "Card must use stone-900 surface in dark mode");
  assert.match(interactiveCard, /dark:border-stone-700/, "Card must use stone-700 border in dark mode");
  assert.match(interactiveCard, /dark:shadow-\[8px_8px_0px_0px_rgba\(0,0,0,1\)\]/, "Card must use pitch black brutalist shadow in dark mode");

  const defaultBadge = badgeVariants({ variant: "default" });
  assert.match(defaultBadge, /dark:bg-stone-100/, "Default badge must invert to stone-100 in dark mode");
  assert.match(defaultBadge, /dark:text-stone-900/, "Default badge must invert text to stone-900 in dark mode");
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

test("Codebase Linter: Global CSS configures Tailwind v4 dark variant and canvas rules", () => {
  const globalCss = path.join(webSrcDir, "styles/global.css");
  const content = fs.readFileSync(globalCss, "utf8");

  assert.match(
    content,
    /@variant\s+dark\s+\(&:where\(\.dark,\s*\.dark\s+\*\)\);/,
    "global.css must register Tailwind v4 dark class selector variant"
  );
  assert.match(
    content,
    /html\.dark\s+body\s*\{[^}]*background-color:\s*#0c0a09/s,
    "global.css must define dark canvas background #0c0a09"
  );
});

test("Codebase Linter: ThemeToggle component is mounted on right side of Navbar", () => {
  const navbarFile = path.join(webSrcDir, "components/Navbar.astro");
  assert.ok(fs.existsSync(navbarFile), "Navbar.astro must exist");

  const content = fs.readFileSync(navbarFile, "utf8");
  assert.match(content, /import\s+ThemeToggle\s+from\s+["']\.\/ThemeToggle\.astro["']/, "Navbar.astro must import ThemeToggle");
  assert.match(content, /<ThemeToggle\s*\/>/, "Navbar.astro must mount ThemeToggle component");

  const themeToggleFile = path.join(webSrcDir, "components/ThemeToggle.astro");
  assert.ok(fs.existsSync(themeToggleFile), "ThemeToggle.astro component must exist");

  const toggleContent = fs.readFileSync(themeToggleFile, "utf8");
  assert.match(toggleContent, /theme-toggle/, "ThemeToggle must include trigger button");
  assert.match(toggleContent, /localStorage\.setItem\(['"]theme['"]/, "ThemeToggle must persist preference to localStorage");
  assert.match(toggleContent, /prefers-color-scheme:\s*dark/, "ThemeToggle must support OS system preference listener");
});

test("Codebase Linter: BaseLayout contains zero-FOUC theme bootstrapper", () => {
  const baseLayoutFile = path.join(webSrcDir, "layouts/BaseLayout.astro");
  const content = fs.readFileSync(baseLayoutFile, "utf8");

  assert.match(
    content,
    /localStorage\.getItem\(['"]theme['"]\)/,
    "BaseLayout must check localStorage theme before render to eliminate FOUC"
  );
  assert.match(
    content,
    /prefers-color-scheme:\s*dark/,
    "BaseLayout must inspect OS prefers-color-scheme in early head script"
  );
  assert.match(
    content,
    /classList\.(?:add|toggle)\(['"]dark['"]/,
    "BaseLayout early script must apply .dark class synchronously"
  );
});

test("Codebase Linter: Social pills define dark mode background and contrast styling", () => {
  const indexPage = path.join(webSrcDir, "pages/index.astro");
  const content = fs.readFileSync(indexPage, "utf8");

  const socialPillMatches = [...content.matchAll(/<SocialPill\b([^>]*?)(?:\/>|>.*?<\/SocialPill>)/gs)];
  assert.ok(socialPillMatches.length >= 5, "index.astro must have at least 5 SocialPill components");

  for (const match of socialPillMatches) {
    const pillAttrs = match[1];
    assert.match(
      pillAttrs,
      /dark:bg-/,
      "Each SocialPill on index.astro must include dark:bg- styling for dark mode visibility"
    );
  }
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

