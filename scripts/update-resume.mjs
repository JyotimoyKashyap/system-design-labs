#!/usr/bin/env node

import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import readline from 'node:readline/promises';
import { execSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const rootDir = path.resolve(__dirname, '..');
const targetFile = path.join(rootDir, 'apps/web/public/resume.pdf');

function formatBytes(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

function validatePdf(filePath) {
  if (!fs.existsSync(filePath)) {
    throw new Error(`File does not exist: ${filePath}`);
  }
  const stat = fs.statSync(filePath);
  if (!stat.isFile()) {
    throw new Error(`Path is not a regular file: ${filePath}`);
  }
  if (stat.size < 100) {
    throw new Error(`File is too small (${stat.size} bytes). Likely empty or corrupted.`);
  }
  if (stat.size > 50 * 1024 * 1024) {
    throw new Error(`File is too large (${formatBytes(stat.size)}). Max allowed size is 50 MB.`);
  }

  // Check magic bytes %PDF
  const buffer = Buffer.alloc(4);
  const fd = fs.openSync(filePath, 'r');
  fs.readSync(fd, buffer, 0, 4, 0);
  fs.closeSync(fd);

  if (buffer.toString('utf8') !== '%PDF') {
    throw new Error(`Invalid PDF header: "${buffer.toString('utf8')}". File must be a valid PDF document.`);
  }

  return stat;
}

function findLatestDownloadPdf() {
  const downloadsDir = path.join(os.homedir(), 'Downloads');
  if (!fs.existsSync(downloadsDir)) {
    return null;
  }

  let files = [];
  try {
    files = fs.readdirSync(downloadsDir);
  } catch {
    return null;
  }

  const pdfs = [];

  for (const file of files) {
    if (file.toLowerCase().endsWith('.pdf') && !file.startsWith('.')) {
      const fullPath = path.join(downloadsDir, file);
      try {
        const stat = fs.statSync(fullPath);
        if (stat.isFile()) {
          pdfs.push({ path: fullPath, name: file, mtimeMs: stat.mtimeMs, size: stat.size });
        }
      } catch {
        // Skip unreadable files
      }
    }
  }

  if (pdfs.length === 0) {
    return null;
  }

  // Sort newest first
  pdfs.sort((a, b) => b.mtimeMs - a.mtimeMs);
  return pdfs[0];
}

async function main() {
  const args = process.argv.slice(2);

  if (args.includes('--help') || args.includes('-h')) {
    console.log(`
📄 Resume Update Utility
-----------------------------------------------------------
Usage:
  npm run update-resume                     Auto-pick newest PDF from ~/Downloads
  npm run update-resume <path-to-pdf>       Use specified PDF file
  npm run update-resume -- -y               Auto-confirm prompts
  npm run update-resume -- --dry-run        Validate without modifying files
  npm run update-resume -- --help           Show this help message
-----------------------------------------------------------
Destination:
  apps/web/public/resume.pdf
URLs:
  https://jyotimoykashyap.me/resume.pdf
  https://jyotimoykashyap.me/resume
`);
    process.exit(0);
  }

  const isDryRun = args.includes('--dry-run');
  const isAutoYes = args.includes('-y') || args.includes('--yes');
  const filePathArg = args.find((arg) => !arg.startsWith('-'));

  let sourceFile = filePathArg ? path.resolve(process.cwd(), filePathArg) : null;

  if (!sourceFile) {
    console.log('🔍 No file specified. Searching ~/Downloads for the newest PDF...');
    const latest = findLatestDownloadPdf();
    if (!latest) {
      console.error('❌ No PDF files found in ~/Downloads.');
      console.error('👉 Usage: npm run update-resume [path/to/resume.pdf]');
      process.exit(1);
    }
    sourceFile = latest.path;
  }

  console.log(`\n📄 Candidate PDF: ${sourceFile}`);

  let stat;
  try {
    stat = validatePdf(sourceFile);
    console.log(`✅ Validation passed: ${formatBytes(stat.size)} (Valid %PDF header)`);
  } catch (err) {
    console.error(`❌ Validation failed: ${err.message}`);
    process.exit(1);
  }

  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
  });

  try {
    if (isDryRun) {
      console.log(`\n🔎 [DRY RUN] PDF is valid. Would replace apps/web/public/resume.pdf with ${sourceFile}.`);
      process.exit(0);
    }

    if (!isAutoYes) {
      const answer = await rl.question(`\n🎯 Replace apps/web/public/resume.pdf with this file? [Y/n] `);
      if (answer.trim().toLowerCase() === 'n' || answer.trim().toLowerCase() === 'no') {
        console.log('Aborted.');
        process.exit(0);
      }
    }

    // Ensure target dir exists
    fs.mkdirSync(path.dirname(targetFile), { recursive: true });
    fs.copyFileSync(sourceFile, targetFile);
    console.log(`\n🎉 Successfully updated: apps/web/public/resume.pdf (${formatBytes(stat.size)})`);

    let pushAnswer = 'y';
    if (!isAutoYes) {
      pushAnswer = await rl.question('\n🚀 Commit and push changes to trigger GitHub Pages deploy? [Y/n] ');
    }

    if (pushAnswer.trim().toLowerCase() !== 'n' && pushAnswer.trim().toLowerCase() !== 'no') {
      console.log('\n📦 Running git commands...');
      execSync(`git add apps/web/public/resume.pdf`, { stdio: 'inherit', cwd: rootDir });
      execSync(`git commit -m "chore: update resume.pdf"`, { stdio: 'inherit', cwd: rootDir });
      execSync(`git push`, { stdio: 'inherit', cwd: rootDir });
      console.log('✅ Changes pushed! GitHub Actions will deploy your new resume in ~45 seconds.');
    } else {
      console.log('ℹ️  File updated locally. Remember to git commit and push when ready.');
    }
  } finally {
    rl.close();
  }
}

main().catch((err) => {
  console.error('Unexpected error:', err);
  process.exit(1);
});
