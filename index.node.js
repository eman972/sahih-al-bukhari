// Node ESM entry — auto-loads bukhari.json from disk
// Works in: Node.js ESM (type:module), Next.js API routes, Astro SSR, etc.
// Usage:
//   import Bukhari from 'sahih-al-bukhari'
//   import { Bukhari } from 'sahih-al-bukhari'

import fs   from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { Bukhari } from './index.js';

const __dirname   = path.dirname(fileURLToPath(import.meta.url));
const bukhariData = JSON.parse(
  fs.readFileSync(path.join(__dirname, 'bin', 'bukhari.json'), 'utf8')
);

const bukhari = new Bukhari(bukhariData);

export { Bukhari };
export default bukhari;