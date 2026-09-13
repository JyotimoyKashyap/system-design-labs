import test from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { autoDiscoverAndSync } from "../scripts/auto-discover-content.mjs";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const rootDir = path.resolve(__dirname, "..");
const webSrcDir = path.resolve(rootDir, "apps/web/src");
const labsDir = path.resolve(webSrcDir, "labs");

// ============================================================================
// 1. REGISTRY & VISUALIZER COMPONENT INTEGRITY
// ============================================================================

test("Auto-Discovery: Registry exports visualizerRegistry and helper functions", () => {
  const registryPath = path.join(labsDir, "registry.ts");
  assert.ok(fs.existsSync(registryPath), "registry.ts must exist");

  const content = fs.readFileSync(registryPath, "utf8");
  assert.match(content, /export const visualizerRegistry/i, "Must export visualizerRegistry");
  assert.match(content, /export function hasVisualizer/i, "Must export hasVisualizer");
  assert.match(content, /export const registeredLabSlugs/i, "Must export registeredLabSlugs");
});

test("Auto-Discovery: All registered visualizers resolve to valid component files on disk", () => {
  const registryPath = path.join(labsDir, "registry.ts");
  const content = fs.readFileSync(registryPath, "utf8");

  // Extract all import statements
  const importLines = content
    .split("\n")
    .filter((l) => l.startsWith("import ") && l.includes("from '"));

  assert.ok(importLines.length >= 6, "Must have at least 6 imported visualizer components");

  for (const line of importLines) {
    const match = line.match(/import\s+(\w+)\s+from\s+'([^']+)';/);
    if (!match) continue;
    const [, componentName, relImportPath] = match;

    // Resolve relative to apps/web/src/labs/
    let resolvedPath = path.resolve(labsDir, relImportPath);
    if (!fs.existsSync(resolvedPath)) {
      // Try with extensions
      if (fs.existsSync(resolvedPath + ".tsx")) resolvedPath += ".tsx";
      else if (fs.existsSync(resolvedPath + ".ts")) resolvedPath += ".ts";
      else if (fs.existsSync(resolvedPath + ".jsx")) resolvedPath += ".jsx";
      else if (fs.existsSync(resolvedPath + ".js")) resolvedPath += ".js";
      else if (fs.existsSync(path.join(resolvedPath, "index.tsx"))) resolvedPath = path.join(resolvedPath, "index.tsx");
    }

    assert.ok(
      fs.existsSync(resolvedPath),
      `Imported component '${componentName}' from '${relImportPath}' does not exist on disk!`
    );
  }
});

test("Auto-Discovery: All foundational labs are present in registry and content", () => {
  const foundationalSlugs = [
    "raft-consensus",
    "consistent-hashing",
    "bloom-filter",
    "rabbitmq",
    "apache-kafka",
    "api-rate-limiter",
  ];

  const registryContent = fs.readFileSync(path.join(labsDir, "registry.ts"), "utf8");
  const labsContentDir = path.join(webSrcDir, "content/labs");

  for (const slug of foundationalSlugs) {
    assert.ok(
      registryContent.includes(`'${slug}':`),
      `Foundational lab '${slug}' must be registered in visualizerRegistry`
    );

    const labDoc = path.join(labsContentDir, `${slug}.md`);
    assert.ok(
      fs.existsSync(labDoc),
      `Foundational lab markdown '${slug}.md' must exist in apps/web/src/content/labs/`
    );
  }
});

// ============================================================================
// 2. DYNAMIC AUTO-DISCOVERY LIFECYCLE (MOCK FOLDER TEST)
// ============================================================================

test("Auto-Discovery: New root directory dynamically registers visualizer, notes, and LLD without commands", () => {
  const testDirName = "test-discovery-suite";
  const testDirPath = path.join(rootDir, testDirName);

  try {
    // 1. Create test folder
    fs.mkdirSync(testDirPath, { recursive: true });
    
    // 2. Add visualizer
    fs.writeFileSync(
      path.join(testDirPath, "test-visualizer.tsx"),
      `import React from 'react'; export default function TestVisualizer() { return <div>Test</div>; }`,
      "utf8"
    );

    // 3. Add notes
    fs.writeFileSync(
      path.join(testDirPath, "notes.md"),
      `# Test Architectural Notes\n\nDeep dive into distributed systems testing.`,
      "utf8"
    );

    // 4. Add LLD
    fs.writeFileSync(
      path.join(testDirPath, "lld.md"),
      `# Test Object Pool LLD\n\nLow level design using Strategy Pattern.`,
      "utf8"
    );

    // 5. Add README with valid metadata
    fs.writeFileSync(
      path.join(testDirPath, "README.md"),
      `<!-- hub-metadata\ntype: lab\ntitle: Test Discovery Suite\ndescription: Interactive simulation testing auto-discovery.\n-->\n\n# Test Lab Simulation\n\nInteractive simulation testing auto-discovery.`,
      "utf8"
    );

    // Run discovery
    autoDiscoverAndSync();

    // Assert Lab was registered
    const registryContent = fs.readFileSync(path.join(labsDir, "registry.ts"), "utf8");
    assert.ok(
      registryContent.includes(`'${testDirName}':`),
      "New test lab must be dynamically added to visualizerRegistry"
    );

    // Assert Lab doc was generated
    const labDoc = path.join(webSrcDir, `content/labs/${testDirName}.md`);
    assert.ok(fs.existsSync(labDoc), "Test lab markdown must be generated in content/labs");

    // Assert Note was generated
    const noteDoc = path.join(webSrcDir, `content/notes/${testDirName}.md`);
    assert.ok(fs.existsSync(noteDoc), "Test note markdown must be generated in content/notes");

    // Assert LLD was generated
    const lldDoc = path.join(webSrcDir, `content/lld/${testDirName}.md`);
    assert.ok(fs.existsSync(lldDoc), "Test LLD markdown must be generated in content/lld");
  } finally {
    // Cleanup temporary files
    fs.rmSync(testDirPath, { recursive: true, force: true });
    fs.rmSync(path.join(webSrcDir, `content/labs/${testDirName}.md`), { force: true });
    fs.rmSync(path.join(webSrcDir, `content/notes/${testDirName}.md`), { force: true });
    fs.rmSync(path.join(webSrcDir, `content/notes/${testDirName}.mdx`), { force: true });
    fs.rmSync(path.join(webSrcDir, `content/lld/${testDirName}.md`), { force: true });
    fs.rmSync(path.join(webSrcDir, `content/lld/${testDirName}.mdx`), { force: true });

    // Restore canonical registry
    autoDiscoverAndSync();
  }
});
