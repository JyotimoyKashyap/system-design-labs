/**
 * Paper Brutalist Design System Tokens
 * Single source of truth for brand colors, shadows, typography, and geometry across all apps and visualizers.
 */

export const colors = {
  // Base Canvas & Neutral Surfaces
  background: "#fdfcfb", // Warm paper grid background
  surface: "#ffffff",    // Card & container surface
  surfaceMuted: "#f5f5f4", // Stone 100
  ink: "#1c1917",        // Stone 900 primary text
  inkMuted: "#57534e",   // Stone 600 secondary text
  border: "#1c1917",     // Heavy brutalist border

  // Core Brand Action Buttons & Accents
  primary: {
    bg: "#FF6B6B",       // Coral Red (Launch App / Primary Actions)
    hover: "#fa5252",
    text: "#0c0a09",     // Stone 950
  },
  secondary: {
    bg: "#4ECDC4",       // Mint / Teal (Read Docs / Secondary Actions)
    hover: "#3dbdb4",
    text: "#0c0a09",
  },
  tertiary: {
    bg: "#ffffff",       // Clean White (Default Paper Brutalist)
    hover: "#f5f5f4",
    text: "#1c1917",
  },
  dark: {
    bg: "#1c1917",       // Pitch Stone (High Contrast)
    hover: "#292524",
    text: "#ffffff",
  },
  accent: {
    amber: "#fbbf24",    // Amber Gold (Navbar CTA / Highlight)
    orange: "#f97316",   // Consensus / Orange
    emerald: "#10b981",  // Success / Active
    rose: "#f43f5e",     // Danger / Stopped
    purple: "#8b5cf6",   // Messaging / Broker
    blue: "#2563eb",     // Low-Level Design / Info
  },
} as const;

export const shadows = {
  sm: "2px 2px 0px 0px rgba(28,25,23,1)",
  default: "4px 4px 0px 0px rgba(28,25,23,1)",
  md: "6px 6px 0px 0px rgba(28,25,23,1)",
  lg: "8px 8px 0px 0px rgba(28,25,23,1)",
  xl: "12px 12px 0px 0px rgba(28,25,23,1)",
} as const;

export const typography = {
  display: "'Silkscreen', monospace",
  body: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
  mono: "'JetBrains Mono', 'Space Mono', ui-monospace, monospace",
} as const;

export const borders = {
  thin: "1px solid #1c1917",
  default: "2px solid #1c1917",
  thick: "3px solid #1c1917",
} as const;

/**
 * Dark Theme Tokens (Dark Blueprint / Obsidian Paper Brutalism)
 * Complementary high-contrast dark palette for dark mode.
 */
export const darkColors = {
  background: "#0c0a09",   // Deep obsidian paper background (Stone 950)
  surface: "#1c1917",      // Card & container surface (Stone 900)
  surfaceMuted: "#292524", // Stone 800 inset panels
  ink: "#fdfcfb",          // High-contrast warm paper-white primary text
  inkMuted: "#a8a29e",     // Stone 400 secondary text
  border: "#44403c",       // Stone 700 brutalist border (or #e7e5e4 for high contrast)
  borderContrast: "#d6d3d1", // Stone 300 high contrast border
  shadow: "#000000",       // Black offset shadow

  // Preserved brand accents with high luminosity in dark mode
  primary: {
    bg: "#FF6B6B",
    hover: "#ff8585",
    text: "#0c0a09",
  },
  secondary: {
    bg: "#4ECDC4",
    hover: "#6ee7de",
    text: "#0c0a09",
  },
  tertiary: {
    bg: "#292524",
    hover: "#3d3835",
    text: "#fdfcfb",
  },
  dark: {
    bg: "#fdfcfb",
    hover: "#e7e5e4",
    text: "#0c0a09",
  },
  accent: {
    amber: "#fbbf24",
    orange: "#f97316",
    emerald: "#34d399",
    rose: "#fb7185",
    purple: "#a78bfa",
    blue: "#60a5fa",
  },
} as const;

export const darkShadows = {
  sm: "2px 2px 0px 0px rgba(0,0,0,1)",
  default: "4px 4px 0px 0px rgba(0,0,0,1)",
  md: "6px 6px 0px 0px rgba(0,0,0,1)",
  lg: "8px 8px 0px 0px rgba(0,0,0,1)",
  xl: "12px 12px 0px 0px rgba(0,0,0,1)",
} as const;

export const themeTokens = {
  light: {
    colors,
    shadows,
  },
  dark: {
    colors: darkColors,
    shadows: darkShadows,
  },
} as const;
