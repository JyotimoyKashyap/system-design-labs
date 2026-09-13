/**
 * Resolves a site-relative path against the configured Astro base URL.
 * Usage: url('/labs') → '/system-design-labs/labs' (in production)
 *        url('/labs') → '/labs' (when base is '/')
 */
export function url(path: string): string {
  if (!path) return import.meta.env.BASE_URL || '/';
  if (path.startsWith('http://') || path.startsWith('https://') || path.startsWith('mailto:') || path.startsWith('#')) {
    return path;
  }
  const base = (import.meta.env.BASE_URL || '/').replace(/\/$/, '');
  if (path === '/') {
    return base ? `${base}/` : '/';
  }
  const cleanPath = path.startsWith('/') ? path : `/${path}`;
  return `${base}${cleanPath}`;
}
