import fs from 'node:fs';
import path from 'node:path';

// Locate monorepo root reliably from cwd or parent
function findRootDir() {
  let curr = process.cwd();
  while (curr !== path.dirname(curr)) {
    if (fs.existsSync(path.join(curr, 'package.json'))) {
      try {
        const pkg = JSON.parse(fs.readFileSync(path.join(curr, 'package.json'), 'utf8'));
        if (pkg.name === 'system-design-labs') return curr;
      } catch {}
    }
    curr = path.dirname(curr);
  }
  return process.cwd();
}

const ROOT_DIR = findRootDir();
const WEB_DIR = path.join(ROOT_DIR, 'apps/web');
const LABS_DIR = path.join(WEB_DIR, 'src/labs');
const CONTENT_LABS_DIR = path.join(WEB_DIR, 'src/content/labs');
const CONTENT_NOTES_DIR = path.join(WEB_DIR, 'src/content/notes');
const CONTENT_LLD_DIR = path.join(WEB_DIR, 'src/content/lld');
const REGISTRY_FILE = path.join(LABS_DIR, 'registry.ts');

const IGNORED_DIRS = new Set([
  'apps',
  'packages',
  'node_modules',
  'dist',
  '.git',
  '.github',
  '.gemini',
  '.vscode',
  'deploy-hub',
  'tests',
  'scripts',
  '.astro',
  '.idea',
  '.gradle',
  'public-pages',
]);

const PALETTE = [
  '#f97316',
  '#eab308',
  '#10b981',
  '#8b5cf6',
  '#ef4444',
  '#d97706',
  '#0ea5e9',
  '#ec4899',
];

function getDeterministicColor(str) {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  return PALETTE[Math.abs(hash) % PALETTE.length];
}

function toTitleCase(slug) {
  return slug
    .split(/[-_]+/)
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
    .join(' ');
}

function toIdentifier(str) {
  return str
    .replace(/[^a-zA-Z0-9]/g, ' ')
    .split(' ')
    .filter(Boolean)
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join('');
}

/**
 * Parses YAML frontmatter or HTML <!-- hub-metadata --> comments
 */
function extractMetadata(rawContent) {
  let metadata = {};
  let content = rawContent;

  // 1. Check for YAML frontmatter
  const yamlMatch = content.match(/^---\r?\n([\s\S]*?)\r?\n---\r?\n([\s\S]*)$/);
  if (yamlMatch) {
    const yamlStr = yamlMatch[1];
    content = yamlMatch[2];
    for (const line of yamlStr.split('\n')) {
      const trimmed = line.trim();
      if (!trimmed || trimmed.startsWith('#')) continue;
      const colonIdx = trimmed.indexOf(':');
      if (colonIdx > 0) {
        const key = trimmed.slice(0, colonIdx).trim();
        let val = trimmed.slice(colonIdx + 1).trim();
        if (val.startsWith('[') && val.endsWith(']')) {
          try {
            metadata[key] = JSON.parse(val);
            continue;
          } catch {
            metadata[key] = val
              .slice(1, -1)
              .split(',')
              .map((s) => s.trim().replace(/^["']|["']$/g, ''))
              .filter(Boolean);
            continue;
          }
        }
        if ((val.startsWith('"') && val.endsWith('"')) || (val.startsWith("'") && val.endsWith("'"))) {
          val = val.slice(1, -1);
        }
        if (val === 'true') metadata[key] = true;
        else if (val === 'false') metadata[key] = false;
        else if (!isNaN(Number(val)) && val !== '') metadata[key] = Number(val);
        else metadata[key] = val;
      }
    }
  }

  // 2. Check for <!-- hub-metadata --> or <!-- deploy-hub --> comments
  const hubMatch = content.match(/<!--\s*(?:hub-metadata|deploy-hub)\s*\n([\s\S]*?)-->/i);
  if (hubMatch) {
    const hubStr = hubMatch[1];
    content = content.replace(hubMatch[0], '').trim();
    for (const line of hubStr.split('\n')) {
      const trimmed = line.trim();
      if (!trimmed || trimmed.startsWith('#')) continue;
      const colonIdx = trimmed.indexOf(':');
      if (colonIdx > 0) {
        const key = trimmed.slice(0, colonIdx).trim();
        let val = trimmed.slice(colonIdx + 1).trim();
        if ((val.startsWith('"') && val.endsWith('"')) || (val.startsWith("'") && val.endsWith("'"))) {
          val = val.slice(1, -1);
        }
        if (val === 'true') metadata[key] = true;
        else if (val === 'false') metadata[key] = false;
        else if (!isNaN(Number(val)) && val !== '') metadata[key] = Number(val);
        else metadata[key] = val;
      }
    }
  }

  return { metadata, content: content.trim() };
}

function extractHeading(content) {
  const m = content.match(/^#\s+(.+)$/m);
  return m ? m[1].replace(/^[^\w]+/, '').trim() : null;
}

function extractParagraph(content) {
  const clean = content
    .replace(/^#+.*$/gm, '')
    .replace(/!\[.*?\]\(.*?\)/g, '')
    .trim();
  const paras = clean.split(/\n\s*\n/).map((p) => p.trim()).filter(Boolean);
  if (paras.length > 0) {
    const first = paras[0].replace(/\n/g, ' ').slice(0, 160).trim();
    return first.endsWith('.') ? first : first + '...';
  }
  return null;
}

function detectDesignPatterns(content) {
  const KNOWN_PATTERNS = [
    'Strategy Pattern',
    'Observer Pattern',
    'Repository Pattern',
    'Factory Pattern',
    'Builder Pattern',
    'Singleton Pattern',
    'Decorator Pattern',
    'Adapter Pattern',
    'Lock Striping',
    'Event Sourcing',
    'Domain-Driven Design',
    'DAO Pattern',
    'Competing Consumers Pattern',
    'Append-Only Log',
    'Token Bucket',
    'State Pattern',
  ];
  const found = [];
  for (const pat of KNOWN_PATTERNS) {
    if (new RegExp(`\\b${pat.replace(' Pattern', '')}`, 'i').test(content)) {
      found.push(pat);
    }
  }
  return found.length > 0 ? found.slice(0, 4) : ['Object-Oriented Design', 'High-Performance'];
}

function rewriteRelativeImages(text, dirName) {
  return text.replace(/!\[(.*?)\]\(\.?\/?(?:assets|animations)\/(.*?)\)/g, (match, alt, assetPath) => {
    return `![${alt}](https://raw.githubusercontent.com/JyotimoyKashyap/system-design-labs/main/${dirName}/assets/${assetPath})`;
  });
}

function detectLanguage(dirPath) {
  if (
    fs.existsSync(path.join(dirPath, 'pom.xml')) ||
    fs.existsSync(path.join(dirPath, 'build.gradle')) ||
    fs.existsSync(path.join(dirPath, 'build.gradle.kts'))
  ) {
    return 'Java 21';
  }
  if (fs.existsSync(path.join(dirPath, 'go.mod'))) {
    return 'Go';
  }
  if (fs.existsSync(path.join(dirPath, 'Cargo.toml'))) {
    return 'Rust';
  }
  if (
    fs.existsSync(path.join(dirPath, 'package.json')) ||
    fs.existsSync(path.join(dirPath, 'tsconfig.json'))
  ) {
    return 'TypeScript';
  }
  if (
    fs.existsSync(path.join(dirPath, 'requirements.txt')) ||
    fs.existsSync(path.join(dirPath, 'pyproject.toml'))
  ) {
    return 'Python 3';
  }
  return 'Java / TypeScript';
}

// Curated Foundational Labs Configuration
const FOUNDATIONAL_LABS = [
  {
    id: 'raft-consensus',
    readme: 'leader-election-web/README.md',
    title: 'Raft Consensus Visualizer',
    description:
      'Interactive simulation of the Raft leader election algorithm featuring asynchronous network delays, split votes, term changes, and event-loop state machines.',
    tag: 'Consensus / State',
    tagColor: '#f97316',
    tagTextColor: '#fff',
    category: 'Consensus',
    featured: true,
    status: 'Interactive Simulation',
    githubRepo:
      'https://github.com/JyotimoyKashyap/system-design-labs/tree/main/leader-election-web',
    importPath: './raft-consensus/RaftVisualizer',
    componentVar: 'RaftVisualizer',
  },
  {
    id: 'consistent-hashing',
    readme: 'consistent-hashing/consistent-hashing-web/README.md',
    title: 'Consistent Hashing Visualizer',
    description:
      'Interactive demonstration of hash space partitioning. Watch how adding or removing nodes elegantly minimizes data redistribution across the ring topology!',
    tag: 'Distributed Systems',
    tagColor: '#eab308',
    tagTextColor: '#000',
    category: 'Distributed Systems',
    featured: true,
    status: 'Interactive Simulation',
    githubRepo:
      'https://github.com/JyotimoyKashyap/system-design-labs/tree/main/consistent-hashing/consistent-hashing-web',
    importPath: './consistent-hashing/ConsistentHashingVisualizer',
    componentVar: 'ConsistentHashingVisualizer',
  },
  {
    id: 'bloom-filter',
    readme: 'bloom-filter-web/README.md',
    title: 'Bloom Filter Visualizer',
    description:
      'Interactive implementation and visualization of probabilistic data structures used for high-performance set membership testing.',
    tag: 'Data Structures',
    tagColor: '#10b981',
    tagTextColor: '#fff',
    category: 'Data Structures',
    featured: true,
    status: 'Interactive Simulation',
    githubRepo:
      'https://github.com/JyotimoyKashyap/system-design-labs/tree/main/bloom-filter-web',
    importPath: './bloom-filter/BloomFilterVisualizer',
    componentVar: 'BloomFilterVisualizer',
  },
  {
    id: 'rabbitmq',
    readme: 'rabbitmq-lab/rabbitmq-web/README.md',
    title: 'RabbitMQ Visualizer',
    description:
      'A beautifully animated demonstration of the Competing Consumers Pattern. Spawn producers and workers dynamically to watch horizontal scaling in action!',
    tag: 'Message Broker',
    tagColor: '#8b5cf6',
    tagTextColor: '#fff',
    category: 'Messaging',
    featured: true,
    status: 'Interactive Simulation',
    githubRepo:
      'https://github.com/JyotimoyKashyap/system-design-labs/tree/main/rabbitmq-lab/rabbitmq-web',
    importPath: './rabbitmq/RabbitMQVisualizer',
    componentVar: 'RabbitMQVisualizer',
  },
  {
    id: 'apache-kafka',
    readme: 'apache-kafka/kafka-web/README.md',
    title: 'Apache Kafka Visualizer',
    description:
      'Simulating an Append-Only Log with Consumer Groups. Watch messages append immutably to partitions while Consumer Groups rebalance partition ownership dynamically!',
    tag: 'Event Streaming',
    tagColor: '#ef4444',
    tagTextColor: '#fff',
    category: 'Streaming',
    featured: true,
    status: 'Interactive Simulation',
    githubRepo:
      'https://github.com/JyotimoyKashyap/system-design-labs/tree/main/apache-kafka/kafka-web',
    importPath: './apache-kafka/KafkaVisualizer',
    componentVar: 'KafkaVisualizer',
  },
  {
    id: 'api-rate-limiter',
    readme: 'api-rate-limiter/api-rate-limiter-web/README.md',
    title: 'API Rate Limiter',
    description:
      'An interactive simulation of a Distributed Token Bucket algorithm. Watch how an API Gateway throttles requests across a Redis cluster using sliding windows!',
    tag: 'API Gateway',
    tagColor: '#d97706',
    tagTextColor: '#fff',
    category: 'Infrastructure',
    featured: true,
    status: 'Interactive Simulation',
    githubRepo:
      'https://github.com/JyotimoyKashyap/system-design-labs/tree/main/api-rate-limiter/api-rate-limiter-web',
    importPath: './api-rate-limiter/RateLimiterVisualizer',
    componentVar: 'RateLimiterVisualizer',
  },
];

const FOUNDATIONAL_DIR_MAPPING = {
  'leader-election-web': 'raft-consensus',
  'bloom-filter-web': 'bloom-filter',
  'rabbitmq-lab': 'rabbitmq',
  'apache-kafka': 'apache-kafka',
  'consistent-hashing': 'consistent-hashing',
  'api-rate-limiter': 'api-rate-limiter',
};

export function autoDiscoverAndSync() {
  console.log('🔍 Running Auto-Discovery across monorepo...');
  console.log(`📁 Workspace root: ${ROOT_DIR}\n`);

  fs.mkdirSync(CONTENT_LABS_DIR, { recursive: true });
  fs.mkdirSync(CONTENT_NOTES_DIR, { recursive: true });
  fs.mkdirSync(CONTENT_LLD_DIR, { recursive: true });
  fs.mkdirSync(LABS_DIR, { recursive: true });

  const registryImports = [];
  const registryMap = [];

  // Track discovered IDs to prevent duplicates
  const processedLabs = new Set();
  const processedNotes = new Set();
  const processedLLD = new Set();

  // 1. Process Foundational Labs
  console.log('⚡ Registering Foundational Labs...');
  for (const lab of FOUNDATIONAL_LABS) {
    processedLabs.add(lab.id);
    registryImports.push(`import ${lab.componentVar} from '${lab.importPath}';`);
    registryMap.push(`  '${lab.id}': ${lab.componentVar},`);

    // Sync Docs
    const readmePath = path.join(ROOT_DIR, lab.readme);
    let markdownBody = '';
    if (fs.existsSync(readmePath)) {
      const raw = fs.readFileSync(readmePath, 'utf8');
      const { content } = extractMetadata(raw);
      markdownBody = content;
    } else {
      markdownBody = `# ${lab.title}\n\n${lab.description}`;
    }

    const frontmatter = `---
id: "${lab.id}"
title: "${lab.title.replace(/"/g, '\\"')}"
description: "${lab.description.replace(/"/g, '\\"')}"
tag: "${lab.tag}"
tagColor: "${lab.tagColor}"
tagTextColor: "${lab.tagTextColor}"
category: "${lab.category}"
featured: ${lab.featured}
status: "${lab.status}"
githubRepo: "${lab.githubRepo}"
---

${markdownBody}
`;
    const targetPath = path.join(CONTENT_LABS_DIR, `${lab.id}.md`);
    fs.writeFileSync(targetPath, frontmatter, 'utf8');
    console.log(`  ✓ Lab: ${lab.id} -> ${path.relative(ROOT_DIR, targetPath)}`);
  }

  // 2. Scan Monorepo Root Folders
  const rootEntries = fs.readdirSync(ROOT_DIR, { withFileTypes: true });

  for (const entry of rootEntries) {
    if (!entry.isDirectory()) continue;
    if (entry.name.startsWith('.') || IGNORED_DIRS.has(entry.name)) continue;

    const dirName = entry.name;
    const dirPath = path.join(ROOT_DIR, dirName);

    // Recursively look for visualizers, notes, and LLDs inside this top-level directory
    discoverContentInDir(dirName, dirPath, {
      processedLabs,
      processedNotes,
      processedLLD,
      registryImports,
      registryMap,
    });
  }

  // 3. Write Visualizer Registry file: apps/web/src/labs/registry.ts
  const registryCode = `// AUTO-GENERATED by scripts/auto-discover-content.mjs
// Do not edit manually. Generated automatically on predev / prebuild.
import type { ComponentType } from 'react';

${registryImports.join('\n')}

export const visualizerRegistry: Record<string, ComponentType<any>> = {
${registryMap.join('\n')}
};

export function hasVisualizer(slug: string): boolean {
  return Boolean(visualizerRegistry[slug]);
}

export const registeredLabSlugs = Object.keys(visualizerRegistry);
`;

  fs.writeFileSync(REGISTRY_FILE, registryCode, 'utf8');
  console.log(`\n📋 Visualizer Registry compiled with ${registryMap.length} labs!`);
  console.log(`  -> ${path.relative(ROOT_DIR, REGISTRY_FILE)}`);
  console.log('✅ Auto-discovery completed successfully!\n');
}

/**
 * Searches a root subdirectory for:
 * 1. Visualizers (*visualizer*.tsx, *Visualizer*.tsx)
 * 2. Notes (notes.md, note.md, architecture.md, or markdown with type: note/blog)
 * 3. LLD (lld.md, design.md, or dirName ends with -lld, or type: lld)
 * 4. Lab docs (README.md if visualizer present)
 */
function discoverContentInDir(
  dirName,
  dirPath,
  { processedLabs, processedNotes, processedLLD, registryImports, registryMap }
) {
  // Collect all files in this directory (up to 3 levels deep)
  const allFiles = findFilesRecursive(dirPath, 3);

  const labSlug = dirName.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');

  // A. Check for Visualizer Component (skip foundational dirs)
  const isFoundational = Boolean(FOUNDATIONAL_DIR_MAPPING[dirName]);
  const visualizerFile = isFoundational
    ? null
    : allFiles.find((f) => {
        const base = path.basename(f);
        return (
          (base.toLowerCase().includes('visualizer') && (base.endsWith('.tsx') || base.endsWith('.jsx'))) ||
          (base === 'App.tsx' && f.includes('-web'))
        );
      });

  if (visualizerFile && !processedLabs.has(labSlug)) {
    processedLabs.add(labSlug);
    const varName = toIdentifier(`${labSlug}-visualizer`);
    // Path relative to apps/web/src/labs/registry.ts
    const relToLabs = path.relative(LABS_DIR, visualizerFile).replace(/\\/g, '/');
    const importPath = relToLabs.startsWith('.') ? relToLabs.replace(/\.(tsx|jsx|ts|js)$/, '') : `./${relToLabs.replace(/\.(tsx|jsx|ts|js)$/, '')}`;

    registryImports.push(`import ${varName} from '${importPath}';`);
    registryMap.push(`  '${labSlug}': ${varName},`);

    // Check for accompanying README for lab documentation
    const readmeFile =
      allFiles.find((f) => path.basename(f).toLowerCase() === 'readme.md') ||
      path.join(dirPath, 'README.md');

    let labTitle = toTitleCase(labSlug) + ' Visualizer';
    let labDesc = `Interactive visualizer and architectural simulation for ${toTitleCase(labSlug)}.`;
    let labTag = 'Distributed Systems';
    let labColor = getDeterministicColor(labSlug);
    let labCategory = 'Distributed Systems';
    let docBody = '';

    if (fs.existsSync(readmeFile)) {
      const raw = fs.readFileSync(readmeFile, 'utf8');
      const { metadata, content } = extractMetadata(raw);
      labTitle = metadata.title || extractHeading(content) || labTitle;
      labDesc = metadata.description || extractParagraph(content) || labDesc;
      labTag = metadata.tag || labTag;
      labColor = metadata.tagColor || labColor;
      labCategory = metadata.category || labCategory;
      docBody = content;
    } else {
      docBody = `# ${labTitle}\n\n${labDesc}\n\nInteractive simulation is ready for exploration.`;
    }
    docBody = rewriteRelativeImages(docBody, dirName);

    const labFrontmatter = `---
id: "${labSlug}"
title: "${labTitle.replace(/"/g, '\\"')}"
description: "${labDesc.replace(/"/g, '\\"')}"
tag: "${labTag}"
tagColor: "${labColor}"
tagTextColor: "#fff"
category: "${labCategory}"
featured: false
status: "Interactive Simulation"
githubRepo: "https://github.com/JyotimoyKashyap/system-design-labs/tree/main/${dirName}"
---

${docBody}
`;
    const targetLabPath = path.join(CONTENT_LABS_DIR, `${labSlug}.md`);
    fs.writeFileSync(targetLabPath, labFrontmatter, 'utf8');
    console.log(`  ✓ Discovered Lab: ${labSlug} -> ${path.relative(ROOT_DIR, targetLabPath)}`);
  }

  // B. Check for Notes (notes.md, note.md, architecture.md, or files in notes/ folder)
  for (const f of allFiles) {
    const base = path.basename(f).toLowerCase();
    if ((dirName === 'blogs' || dirName === 'notes') && base === 'readme.md') continue;
    const isNoteFile =
      base === 'notes.md' ||
      base === 'note.md' ||
      base === 'notes.mdx' ||
      base === 'note.mdx' ||
      base === 'architecture.md' ||
      base === 'hld.md' ||
      (f.includes('/notes/') && (base.endsWith('.md') || base.endsWith('.mdx')));

    let isMarkedNote = false;
    let fileContent = '';
    let parsedMeta = {};
    let cleanContent = '';

    if (base.endsWith('.md') || base.endsWith('.mdx')) {
      try {
        fileContent = fs.readFileSync(f, 'utf8');
        const parsed = extractMetadata(fileContent);
        parsedMeta = parsed.metadata;
        cleanContent = parsed.content;
        if (parsedMeta.type === 'note' || parsedMeta.type === 'blog') {
          // If it is explicitly tagged as LLD, skip note processing
          if (
            dirName.endsWith('-lld') ||
            (parsedMeta.tag && parsedMeta.tag.toLowerCase().includes('lld'))
          ) {
            isMarkedNote = false;
          } else {
            isMarkedNote = true;
          }
        }
      } catch {}
    }

    if (isNoteFile || isMarkedNote) {
      let noteSlug = labSlug;
      if (dirName === 'blogs' || dirName === 'notes') {
        noteSlug = path.basename(f).replace(/\.(md|mdx)$/, '');
      } else if (isMarkedNote && parsedMeta.targetDir) {
        noteSlug = parsedMeta.targetDir;
      }

      if (!processedNotes.has(noteSlug)) {
        processedNotes.add(noteSlug);

        const title = parsedMeta.title || extractHeading(cleanContent) || toTitleCase(noteSlug);
        const description =
          parsedMeta.description ||
          extractParagraph(cleanContent) ||
          `Technical architectural notes on ${title}.`;
        const category = parsedMeta.category || 'Architecture';
        const publishedAt =
          parsedMeta.publishedAt ||
          new Date().toISOString().split('T')[0];
        const tags = Array.isArray(parsedMeta.tags)
          ? parsedMeta.tags
          : parsedMeta.tag
            ? [parsedMeta.tag]
            : ['Architecture', 'System Design'];
        const wordCount = cleanContent.split(/\s+/).filter(Boolean).length;
        const readTimeMinutes =
          typeof parsedMeta.readTimeMinutes === 'number'
            ? parsedMeta.readTimeMinutes
            : Math.max(1, Math.ceil(wordCount / 200));

        const noteDoc = `---
title: "${title.replace(/"/g, '\\"')}"
description: "${description.replace(/"/g, '\\"')}"
publishedAt: "${publishedAt}"
category: "${category}"
tags: ${JSON.stringify(tags)}
readTimeMinutes: ${readTimeMinutes}
featured: ${Boolean(parsedMeta.featured)}
---

${rewriteRelativeImages(cleanContent, dirName)}
`;
        const targetNotePath = path.join(CONTENT_NOTES_DIR, `${noteSlug}.md`);
        const legacyMdx = path.join(CONTENT_NOTES_DIR, `${noteSlug}.mdx`);
        if (fs.existsSync(legacyMdx)) fs.rmSync(legacyMdx);
        fs.writeFileSync(targetNotePath, noteDoc, 'utf8');
        console.log(`  ✓ Discovered Note: ${noteSlug} -> ${path.relative(ROOT_DIR, targetNotePath)}`);
      }
    }
  }

  // C. Check for LLD (lld.md, design.md, *-lld folders, or tag contains LLD)
  for (const f of allFiles) {
    const base = path.basename(f).toLowerCase();
    const isLldFile =
      base === 'lld.md' ||
      base === 'lld.mdx' ||
      base === 'design.md' ||
      base === 'low-level-design.md' ||
      base === 'system-design.md' ||
      (f.includes('/lld/') && (base.endsWith('.md') || base.endsWith('.mdx')));

    let isMarkedLld = false;
    let fileContent = '';
    let parsedMeta = {};
    let cleanContent = '';

    if (base.endsWith('.md') || base.endsWith('.mdx')) {
      try {
        fileContent = fs.readFileSync(f, 'utf8');
        const parsed = extractMetadata(fileContent);
        parsedMeta = parsed.metadata;
        cleanContent = parsed.content;

        if (
          parsedMeta.type === 'lld' ||
          dirName.endsWith('-lld') ||
          (parsedMeta.tag && parsedMeta.tag.toLowerCase().includes('lld'))
        ) {
          isMarkedLld = true;
        }
      } catch {}
    }

    if (isLldFile || isMarkedLld) {
      const lldSlug = dirName.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');

      if (!processedLLD.has(lldSlug)) {
        processedLLD.add(lldSlug);

        const title = parsedMeta.title || extractHeading(cleanContent) || toTitleCase(lldSlug);
        const description =
          parsedMeta.description ||
          extractParagraph(cleanContent) ||
          `Low-Level Design and architectural blueprint for ${title}.`;
        const publishedAt =
          parsedMeta.publishedAt ||
          '2026-07-20';
        const designPatterns = Array.isArray(parsedMeta.designPatterns)
          ? parsedMeta.designPatterns
          : detectDesignPatterns(cleanContent);
        const language = parsedMeta.language || detectLanguage(dirPath);
        const difficulty = parsedMeta.difficulty === 'Hard' ? 'Hard' : 'Medium';
        const githubRepo =
          parsedMeta.githubRepo ||
          `https://github.com/JyotimoyKashyap/system-design-labs/tree/main/${dirName}`;

        const lldDoc = `---
title: "${title.replace(/"/g, '\\"')}"
description: "${description.replace(/"/g, '\\"')}"
publishedAt: "${publishedAt}"
designPatterns: ${JSON.stringify(designPatterns)}
language: "${language}"
githubRepo: "${githubRepo}"
difficulty: "${difficulty}"
---

${rewriteRelativeImages(cleanContent, dirName)}
`;
        const targetLldPath = path.join(CONTENT_LLD_DIR, `${lldSlug}.md`);
        const legacyMdx = path.join(CONTENT_LLD_DIR, `${lldSlug}.mdx`);
        if (fs.existsSync(legacyMdx)) fs.rmSync(legacyMdx);
        fs.writeFileSync(targetLldPath, lldDoc, 'utf8');
        console.log(`  ✓ Discovered LLD: ${lldSlug} -> ${path.relative(ROOT_DIR, targetLldPath)}`);
      }
    }
  }
}

function findFilesRecursive(dir, maxDepth = 3, currentDepth = 0) {
  const results = [];
  if (currentDepth > maxDepth || !fs.existsSync(dir)) return results;

  let entries = [];
  try {
    entries = fs.readdirSync(dir, { withFileTypes: true });
  } catch {
    return results;
  }

  for (const entry of entries) {
    if (entry.name.startsWith('.') || IGNORED_DIRS.has(entry.name)) continue;
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      results.push(...findFilesRecursive(fullPath, maxDepth, currentDepth + 1));
    } else if (entry.isFile()) {
      results.push(fullPath);
    }
  }
  return results;
}

// Self-executing runner
if (
  process.argv[1] &&
  (process.argv[1].endsWith('auto-discover-content.mjs') ||
    process.argv[1].endsWith('auto-discover-content.js'))
) {
  autoDiscoverAndSync();
}
