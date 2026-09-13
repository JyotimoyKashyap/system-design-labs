import test from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const rootDir = path.resolve(__dirname, "..");
const webSrcDir = path.resolve(rootDir, "apps/web/src");

const IGNORED_TOP_LEVEL_DIRS = new Set([
  "apps",
  "packages",
  "node_modules",
  "dist",
  ".git",
  ".github",
  ".gemini",
  ".agents",
  ".vscode",
  "deploy-hub",
  "tests",
  "scripts",
  ".astro",
  ".idea",
  ".gradle",
  "public-pages",
  "docs",
]);

/**
 * Extracts metadata from either:
 * 1. YAML frontmatter: --- \n title: ... \n ---
 * 2. HTML comments: <!-- hub-metadata \n title: ... \n -->
 */
function extractMetadata(content) {
  const metadata = {};

  // Check YAML frontmatter
  const yamlMatch = content.match(/^---\r?\n([\s\S]*?)\r?\n---/);
  if (yamlMatch) {
    for (const line of yamlMatch[1].split("\n")) {
      const trimmed = line.trim();
      const colonIdx = trimmed.indexOf(":");
      if (colonIdx > 0 && !trimmed.startsWith("#")) {
        const key = trimmed.slice(0, colonIdx).trim();
        const val = trimmed.slice(colonIdx + 1).trim().replace(/^["']|["']$/g, "");
        metadata[key] = val;
      }
    }
  }

  // Check HTML comment <!-- hub-metadata -->
  const hubMatch = content.match(/<!--\s*(?:hub-metadata|deploy-hub)\s*\n([\s\S]*?)-->/i);
  if (hubMatch) {
    for (const line of hubMatch[1].split("\n")) {
      const trimmed = line.trim();
      const colonIdx = trimmed.indexOf(":");
      if (colonIdx > 0 && !trimmed.startsWith("#")) {
        const key = trimmed.slice(0, colonIdx).trim();
        const val = trimmed.slice(colonIdx + 1).trim().replace(/^["']|["']$/g, "");
        metadata[key] = val;
      }
    }
  }

  return metadata;
}

// ============================================================================
// 1. PROJECT README METADATA LINTER
// ============================================================================

test("Metadata Linter: All project root directories must contain a README.md with valid metadata", () => {
  const rootEntries = fs.readdirSync(rootDir, { withFileTypes: true });
  const missingMetadataDirs = [];

  for (const entry of rootEntries) {
    if (!entry.isDirectory()) continue;
    if (entry.name.startsWith(".") || entry.name.startsWith("test-") || entry.name.startsWith("tmp-") || IGNORED_TOP_LEVEL_DIRS.has(entry.name)) continue;

    const dirPath = path.join(rootDir, entry.name);
    
    // Find README.md in root of dir or in subfolder *-web
    const directReadme = path.join(dirPath, "README.md");
    let readmePath = null;

    if (fs.existsSync(directReadme)) {
      readmePath = directReadme;
    } else {
      // Check subdirectories (e.g. consistent-hashing/consistent-hashing-web/README.md)
      const subEntries = fs.readdirSync(dirPath, { withFileTypes: true });
      for (const sub of subEntries) {
        if (sub.isDirectory()) {
          const subReadme = path.join(dirPath, sub.name, "README.md");
          if (fs.existsSync(subReadme)) {
            readmePath = subReadme;
            break;
          }
        }
      }
    }

    assert.ok(
      readmePath,
      `Project directory '${entry.name}' is missing a README.md! Every project directory must have documentation.`
    );

    const content = fs.readFileSync(readmePath, "utf8");
    const meta = extractMetadata(content);

    const hasTitle = Boolean(meta.title && meta.title.length > 0);
    const hasDescription = Boolean(meta.description && meta.description.length > 0);

    if (!hasTitle || !hasDescription) {
      missingMetadataDirs.push({
        dir: entry.name,
        file: path.relative(rootDir, readmePath),
        missingTitle: !hasTitle,
        missingDescription: !hasDescription,
      });
    }
  }

  assert.equal(
    missingMetadataDirs.length,
    0,
    `The following project READMEs are missing required metadata (title, description):\n` +
      missingMetadataDirs
        .map(
          (m) =>
            `  • ${m.file} (missing: ${[m.missingTitle ? "title" : null, m.missingDescription ? "description" : null].filter(Boolean).join(", ")})`
        )
        .join("\n") +
      `\n\nPlease add a <!-- hub-metadata --> or YAML frontmatter block to the top of each file:\n` +
      `<!-- hub-metadata\n` +
      `type: lab | visualizer | lld | note\n` +
      `title: Your Project Title\n` +
      `description: A short one or two sentence summary\n` +
      `-->\n`
  );
});

// ============================================================================
// 2. CONTENT COLLECTIONS SCHEMA VALIDATION
// ============================================================================

test("Content Linter: All lab content files adhere strictly to Astro schema", () => {
  const labsDir = path.join(webSrcDir, "content/labs");
  assert.ok(fs.existsSync(labsDir), "apps/web/src/content/labs must exist");

  const labFiles = fs.readdirSync(labsDir).filter((f) => f.endsWith(".md") || f.endsWith(".mdx"));
  assert.ok(labFiles.length >= 6, "Labs collection must contain at least 6 foundational labs");

  for (const file of labFiles) {
    const filePath = path.join(labsDir, file);
    const content = fs.readFileSync(filePath, "utf8");
    const meta = extractMetadata(content);

    assert.ok(meta.id, `Lab ${file} must have an 'id' field`);
    assert.ok(meta.title, `Lab ${file} must have a 'title' field`);
    assert.ok(meta.description, `Lab ${file} must have a 'description' field`);
    assert.ok(meta.tag, `Lab ${file} must have a 'tag' field`);
    assert.ok(meta.tagColor, `Lab ${file} must have a 'tagColor' field`);
    assert.ok(meta.category, `Lab ${file} must have a 'category' field`);
    assert.match(
      meta.status || "Interactive Simulation",
      /^(?:Interactive Simulation|Beta|Experimental)$/,
      `Lab ${file} status must be 'Interactive Simulation', 'Beta', or 'Experimental'`
    );
  }
});

test("Content Linter: All notes content files adhere strictly to Astro schema", () => {
  const notesDir = path.join(webSrcDir, "content/notes");
  assert.ok(fs.existsSync(notesDir), "apps/web/src/content/notes must exist");

  const noteFiles = fs.readdirSync(notesDir).filter((f) => f.endsWith(".md") || f.endsWith(".mdx"));
  assert.ok(noteFiles.length >= 1, "Notes collection must contain at least 1 note");

  for (const file of noteFiles) {
    const filePath = path.join(notesDir, file);
    const content = fs.readFileSync(filePath, "utf8");
    const meta = extractMetadata(content);

    assert.ok(meta.title, `Note ${file} must have a 'title' field`);
    assert.ok(meta.description, `Note ${file} must have a 'description' field`);
    assert.ok(meta.publishedAt, `Note ${file} must have a 'publishedAt' date`);
    assert.ok(meta.category, `Note ${file} must have a 'category' field`);
  }
});

test("Content Linter: All LLD content files adhere strictly to Astro schema", () => {
  const lldDir = path.join(webSrcDir, "content/lld");
  assert.ok(fs.existsSync(lldDir), "apps/web/src/content/lld must exist");

  const lldFiles = fs.readdirSync(lldDir).filter((f) => f.endsWith(".md") || f.endsWith(".mdx"));
  assert.ok(lldFiles.length >= 2, "LLD collection must contain at least 2 LLD entries");

  for (const file of lldFiles) {
    const filePath = path.join(lldDir, file);
    const content = fs.readFileSync(filePath, "utf8");
    const meta = extractMetadata(content);

    assert.ok(meta.title, `LLD ${file} must have a 'title' field`);
    assert.ok(meta.description, `LLD ${file} must have a 'description' field`);
    assert.ok(meta.publishedAt, `LLD ${file} must have a 'publishedAt' date`);
    assert.match(
      meta.difficulty || "Medium",
      /^(?:Medium|Hard)$/,
      `LLD ${file} difficulty must be either 'Medium' or 'Hard'`
    );
  }
});
