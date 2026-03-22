#!/usr/bin/env node
/**
 * scripts/copy-data-java.mjs
 *
 * NOTE: You normally do NOT need to run this script.
 * Both pom.xml and build.gradle are configured to read bukhari.json.gz
 * directly from ../data/ at build time — no manual copy required.
 *
 * This script is only useful if you want to pre-stage the file for an IDE
 * that does not run the Maven/Gradle resource pipeline (e.g. running tests
 * directly from IntelliJ without a Maven run configuration).
 *
 * Usage:  node java/scripts/copy-data-java.mjs
 *   (run from the monorepo root)
 */

import { copyFileSync, mkdirSync, existsSync } from 'fs';
import { join, dirname }                        from 'path';
import { fileURLToPath }                        from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
// This script lives at java/scripts/ — go up two levels to reach repo root
const repoRoot  = join(__dirname, '..', '..');
const srcGz     = join(repoRoot, 'data', 'bukhari.json.gz');
const srcJson   = join(repoRoot, 'data', 'bukhari.json');
const destDir   = join(__dirname, '..', 'src', 'main', 'resources');

mkdirSync(destDir, { recursive: true });

if (existsSync(srcGz)) {
  copyFileSync(srcGz, join(destDir, 'bukhari.json.gz'));
  console.log('[copy-data-java] Copied bukhari.json.gz ->', join(destDir, 'bukhari.json.gz'));
} else if (existsSync(srcJson)) {
  copyFileSync(srcJson, join(destDir, 'bukhari.json'));
  console.log('[copy-data-java] Copied bukhari.json ->', join(destDir, 'bukhari.json'));
} else {
  console.error('[copy-data-java] ERROR: No data file found.');
  console.error('  Expected:', srcGz);
  console.error('  Run `node scripts/build.mjs` first to generate the data file.');
  process.exit(1);
}
