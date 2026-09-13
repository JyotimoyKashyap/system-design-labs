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
