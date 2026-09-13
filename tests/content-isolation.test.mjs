import test from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const rootDir = path.resolve(__dirname, "..");
const webSrcDir = path.resolve(rootDir, "apps/web/src");

test("Content Isolation: Notes pages strictly link only to notes hierarchy", () => {
  const notesSlugPage = path.join(webSrcDir, "pages/notes/[slug].astro");
  assert.ok(fs.existsSync(notesSlugPage), "notes/[slug].astro must exist");

  const content = fs.readFileSync(notesSlugPage, "utf8");
  assert.match(content, /backLink="\/notes"/i, "Notes page backLink must point to /notes");
  assert.match(content, /backLabel="Back to All Notes"/i, "Notes page backLabel must be 'Back to All Notes'");
  assert.ok(!content.includes("/labs/"), "Notes page must not inject forced lab links");
  assert.ok(!content.includes("/lld/"), "Notes page must not inject forced LLD links");
});

test("Content Isolation: LLD pages strictly link only to LLD hierarchy", () => {
  const lldSlugPage = path.join(webSrcDir, "pages/lld/[slug].astro");
  assert.ok(fs.existsSync(lldSlugPage), "lld/[slug].astro must exist");

  const content = fs.readFileSync(lldSlugPage, "utf8");
  assert.match(content, /backLink="\/lld"/i, "LLD page backLink must point to /lld");
  assert.match(content, /backLabel="Back to All LLD"/i, "LLD page backLabel must be 'Back to All LLD'");
  assert.ok(!content.includes("/labs/"), "LLD page must not inject forced lab links");
  assert.ok(!content.includes("/notes/"), "LLD page must not inject forced note links");
});

test("Content Isolation: ContentLayout shell does not contain hardcoded cross-content links", () => {
  const layoutPath = path.join(webSrcDir, "layouts/ContentLayout.astro");
  const content = fs.readFileSync(layoutPath, "utf8");

  assert.ok(!content.includes("Related Visualizer"), "ContentLayout must not inject forced visualizer cards");
  assert.ok(!content.includes("Related Low-Level Design"), "ContentLayout must not inject forced LLD cards");
  assert.ok(!content.includes("Accompanying Note"), "ContentLayout must not inject forced note cards");
});
