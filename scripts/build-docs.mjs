import fs from 'node:fs';
import path from 'node:path';
import { execSync } from 'node:child_process';

const ROOT_DIR = process.cwd();
const DEPLOY_HUB_DIR = path.join(ROOT_DIR, 'deploy-hub');
const DATA_JSON_PATH = path.join(DEPLOY_HUB_DIR, 'data.json');
const GITIGNORE_PATH = path.join(ROOT_DIR, '.gitignore');

// Preset palette for tags if not explicitly specified
const PALETTE = ['#2563eb', '#ec4899', '#10b981', '#f97316', '#8b5cf6', '#d97706', '#0ea5e9', '#eab308'];

function getDeterministicColor(str) {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  return PALETTE[Math.abs(hash) % PALETTE.length];
}

/**
 * Parses invisible metadata from HTML comments:
 * <!-- hub-metadata
 * type: blog | visualizer
 * tag: Low-Level Design
 * tagColor: #2563eb
 * title: My Project Title
 * description: A short description
 * appLink: ./my-app-web/
 * targetDir: my-app
 * enabled: true
 * -->
 */
function parseHubMetadata(content) {
  const match = content.match(/<!--\s*(?:hub-metadata|deploy-hub)\s*\n([\s\S]*?)-->/i);
  if (!match) return null;

  const rawMeta = match[1];
  const metadata = {};

  for (const line of rawMeta.split('\n')) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) continue;
    const colonIdx = trimmed.indexOf(':');
    if (colonIdx > 0) {
      const key = trimmed.slice(0, colonIdx).trim();
      let val = trimmed.slice(colonIdx + 1).trim();
      if ((val.startsWith('"') && val.endsWith('"')) || (val.startsWith("'") && val.endsWith("'"))) {
        val = val.slice(1, -1);
      }
      if (val === 'true') val = true;
      else if (val === 'false') val = false;
      metadata[key] = val;
    }
  }

  return metadata;
}

/**
 * Extracts a fallback title from the first H1 header in markdown.
 */
function extractTitle(content, fallback) {
  const h1Match = content.match(/^#\s+(.+)$/m);
  if (h1Match) {
    return h1Match[1].replace(/\[.*?\]\(.*?\)/g, '').replace(/!\[.*?\]\(.*?\)/g, '').trim();
  }
  return fallback;
}

/**
 * Extracts a fallback description from the first substantive paragraph.
 */
function extractDescription(content, fallback = '') {
  const lines = content.split('\n');
  for (const rawLine of lines) {
    const line = rawLine.trim();
    if (!line || line.startsWith('#') || line.startsWith('<!--') || line.startsWith('![') || line.startsWith('```') || line.startsWith('|')) {
      continue;
    }
    const clean = line.replace(/\[([^\]]+)\]\([^\)]+\)/g, '$1').replace(/[*_`]/g, '').trim();
    if (clean.length > 20) {
      return clean.length > 180 ? `${clean.slice(0, 177)}...` : clean;
    }
  }
  return fallback;
}

/**
 * Recursively scans directory for markdown files
 */
function scanMarkdownFiles(dir, fileList = []) {
  const IGNORED = ['node_modules', '.git', '.gemini', 'deploy-hub', 'build', 'dist', 'scratch', 'public-pages'];
  const entries = fs.readdirSync(dir, { withFileTypes: true });

  for (const entry of entries) {
    if (IGNORED.includes(entry.name)) continue;
    const fullPath = path.join(dir, entry.name);

    if (entry.isDirectory()) {
      scanMarkdownFiles(fullPath, fileList);
    } else if (entry.isFile() && entry.name.endsWith('.md')) {
      fileList.push(fullPath);
    }
  }
  return fileList;
}

function findFolder(name, dir = '.') {
  const IGNORED = ['node_modules', '.git', '.gemini', 'deploy-hub', 'build', 'dist', 'scratch', 'public-pages'];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    if (entry.isDirectory() && !IGNORED.includes(entry.name)) {
      const full = path.join(dir, entry.name);
      if (entry.name === name) return full;
      const found = findFolder(name, full);
      if (found) return found;
    }
  }
  return null;
}

function resolveLegacyReadme(item) {
  if (item.readme && fs.existsSync(path.join(ROOT_DIR, item.readme))) {
    return path.join(ROOT_DIR, item.readme);
  }

  const targetDir = item.docsLink.replace(/^\.\//, '').replace(/\/docs\.html$/, '');

  // 1. Direct path in root
  if (fs.existsSync(path.join(ROOT_DIR, targetDir, 'README.md'))) {
    return path.join(ROOT_DIR, targetDir, 'README.md');
  }

  // 2. Find targetDir directory anywhere in monorepo
  const targetFolder = findFolder(targetDir, ROOT_DIR);
  if (targetFolder && fs.existsSync(path.join(targetFolder, 'README.md'))) {
    return path.join(targetFolder, 'README.md');
  }

  // 3. Find item.id directory
  const idFolder = findFolder(item.id, ROOT_DIR);
  if (idFolder && fs.existsSync(path.join(idFolder, 'README.md'))) {
    return path.join(idFolder, 'README.md');
  }

  // 4. Check blogs directory
  const blogCandidate = path.join(ROOT_DIR, 'blogs', `${item.id}.md`);
  if (fs.existsSync(blogCandidate)) return blogCandidate;

  return null;
}

const HTML_TEMPLATE = (content, backLink = '../') => `<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/5.5.0/github-markdown-light.min.css">
<style>
  body { 
    background-color: #fdfcfb; 
    padding: 40px 20px; 
    font-family: 'Inter', sans-serif;
  }
  .markdown-container {
    box-sizing: border-box;
    max-width: 900px;
    margin: 0 auto;
    padding: 45px;
    background-color: white;
    border: 3px solid #1c1917;
    box-shadow: 8px 8px 0px 0px rgba(28,25,23,1);
  }
  .markdown-body img {
    display: inline-block;
    vertical-align: middle;
    max-width: 100%;
  }
  .back-btn {
    display: inline-block;
    padding: 8px 16px;
    margin-bottom: 30px;
    background: #e7e5e4;
    color: #1c1917;
    border: 2px solid #1c1917;
    text-decoration: none;
    font-size: 0.85rem;
    font-weight: 800;
    text-transform: uppercase;
    box-shadow: 4px 4px 0px 0px rgba(28,25,23,1);
    transition: all 0.15s ease;
  }
  .back-btn:hover {
    transform: translate(2px, 2px);
    box-shadow: 2px 2px 0px 0px rgba(28,25,23,1);
  }
</style>
</head>
<body>
  <div class="markdown-container markdown-body">
    <a href="${backLink}" class="back-btn">← Back to Hub</a>
${content}
  </div>
  <!-- Syntax Highlighting -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github.min.css">
  <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
  <script>hljs.highlightAll();</script>
  
  <!-- Mermaid -->
  <script type="module">
    import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.esm.min.mjs';
    mermaid.initialize({ startOnLoad: false, theme: 'default' });
    
    document.querySelectorAll('code.language-mermaid').forEach(async (block, index) => {
      const pre = block.parentElement;
      const graphDef = block.textContent;
      try {
        const { svg } = await mermaid.render(\`mermaid-graph-\${index}\`, graphDef);
        const div = document.createElement('div');
        div.style.textAlign = 'center';
        div.style.margin = '20px 0';
        div.innerHTML = svg;
        pre.replaceWith(div);
      } catch (e) {
        console.error('Mermaid parsing error', e);
      }
    });
  </script>
</body>
</html>
`;

function compileMarkdownToHtml(sourceFile, outDir) {
  fs.mkdirSync(outDir, { recursive: true });
  const outFile = path.join(outDir, 'docs.html');

  // Strip invisible metadata comment from the HTML output
  const rawMarkdown = fs.readFileSync(sourceFile, 'utf8');
  const cleanMarkdown = rawMarkdown
    .replace(/<!--\s*(?:hub-metadata|deploy-hub)\s*\n[\s\S]*?-->/i, '')
    .trim();

  const tempFile = path.join(outDir, '.temp-render.md');
  fs.writeFileSync(tempFile, cleanMarkdown, 'utf8');

  try {
    const renderedHtml = execSync(`npx marked "${tempFile}"`, {
      encoding: 'utf8',
      maxBuffer: 10 * 1024 * 1024
    });
    const fullHtml = HTML_TEMPLATE(renderedHtml);
    fs.writeFileSync(outFile, fullHtml, 'utf8');
  } finally {
    if (fs.existsSync(tempFile)) fs.unlinkSync(tempFile);
  }
}

export async function buildDocs() {
  console.log('🔍 Scanning repository for marked documentation and metadata tags...\n');

  let existingData = [];
  if (fs.existsSync(DATA_JSON_PATH)) {
    try {
      existingData = JSON.parse(fs.readFileSync(DATA_JSON_PATH, 'utf8'));
    } catch (e) {
      console.warn('⚠️ Could not parse existing deploy-hub/data.json');
    }
  }

  const existingMap = new Map(existingData.map(item => [item.id, item]));
  const allMdFiles = scanMarkdownFiles(ROOT_DIR);
  const taggedItems = [];
  const processedIds = new Set();
  const generatedFolders = new Set();

  // Phase 1: Auto-discover files with <!-- hub-metadata ... --> tags
  for (const mdPath of allMdFiles) {
    const content = fs.readFileSync(mdPath, 'utf8');
    const meta = parseHubMetadata(content);
    if (!meta) continue;

    if (meta.enabled === false) {
      console.log(`⏸️ Skipping disabled doc: ${path.relative(ROOT_DIR, mdPath)}`);
      continue;
    }

    const relPath = path.relative(ROOT_DIR, mdPath);
    const parentDir = path.dirname(relPath);
    const folderName = path.basename(parentDir === '.' ? mdPath : parentDir);
    const fileSlug = path.basename(mdPath, '.md');

    const id = meta.id || (fileSlug !== 'README' ? fileSlug : folderName);
    const type = meta.type || (folderName.endsWith('-web') ? 'visualizer' : 'blog');
    const tag = meta.tag || (type === 'blog' ? 'System Design LLD' : 'Interactive Lab');
    const tagColor = meta.tagColor || getDeterministicColor(tag);
    const tagTextColor = meta.tagTextColor || '#fff';
    const title = meta.title || extractTitle(content, id);
    const description = meta.description || extractDescription(content, 'Documentation and interactive guide.');

    const outSubDir = meta.targetDir || folderName;
    const docsLink = `./${outSubDir}/docs.html`;
    const appLink = meta.appLink !== undefined ? meta.appLink : (type === 'visualizer' ? `./${folderName}/` : '');

    const hubEntry = {
      id,
      tag,
      tagColor,
      tagTextColor,
      title,
      description,
      appLink,
      docsLink,
      ...(type === 'blog' ? { type: 'blog' } : {})
    };

    taggedItems.push({
      entry: hubEntry,
      sourceFile: mdPath,
      outSubDir
    });
    processedIds.add(id);
  }

  // Compile tagged docs and update existingMap
  for (const { entry, sourceFile, outSubDir } of taggedItems) {
    existingMap.set(entry.id, entry);
    generatedFolders.add(`deploy-hub/${outSubDir}/`);

    const outDir = path.join(DEPLOY_HUB_DIR, outSubDir);
    const relSource = path.relative(ROOT_DIR, sourceFile);
    console.log(`🏷️  [Tagged] Compiling: ${relSource} -> deploy-hub/${outSubDir}/docs.html`);
    compileMarkdownToHtml(sourceFile, outDir);
  }

  // Phase 2: Fallback compile for existing data.json entries not yet tagged
  for (const [id, item] of existingMap.entries()) {
    if (processedIds.has(id)) continue;
    if (!item.docsLink) continue;

    const targetDir = item.docsLink.replace(/^\.\//, '').replace(/\/docs\.html$/, '');
    const outDir = path.join(DEPLOY_HUB_DIR, targetDir);
    generatedFolders.add(`deploy-hub/${targetDir}/`);

    const sourceFile = resolveLegacyReadme(item);
    if (!sourceFile) {
      console.warn(`⚠️ Warning: No README found for untagged item [${id}]`);
      continue;
    }

    const relSource = path.relative(ROOT_DIR, sourceFile);
    console.log(`📌 [Existing] Compiling: ${relSource} -> deploy-hub/${targetDir}/docs.html`);
    compileMarkdownToHtml(sourceFile, outDir);
  }

  // Write updated data.json
  const finalEntries = Array.from(existingMap.values());
  fs.writeFileSync(DATA_JSON_PATH, JSON.stringify(finalEntries, null, 2) + '\n', 'utf8');
  console.log(`\n💾 Saved ${finalEntries.length} entries to deploy-hub/data.json`);

  // Ensure gitignore covers all output folders
  if (fs.existsSync(GITIGNORE_PATH)) {
    let gitignore = fs.readFileSync(GITIGNORE_PATH, 'utf8');
    if (!gitignore.includes('deploy-hub/*/')) {
      let added = false;
      for (const folder of generatedFolders) {
        if (!gitignore.includes(folder)) {
          gitignore += `\n${folder}`;
          added = true;
        }
      }
      if (added) {
        fs.writeFileSync(GITIGNORE_PATH, gitignore, 'utf8');
        console.log('📝 Updated .gitignore with generated documentation directories.');
      }
    }
  }

  console.log('\n✨ Documentation generation successfully completed!');
}

if (process.argv[1] === new URL(import.meta.url).pathname) {
  buildDocs().catch(err => {
    console.error('Build error:', err);
    process.exit(1);
  });
}
