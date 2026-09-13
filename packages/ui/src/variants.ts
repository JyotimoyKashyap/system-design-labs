import { cn } from "./lib/utils.ts";

// --- BUTTON VARIANTS ---

export type ButtonVariant = 
  | "default"
  | "primary" 
  | "secondary" 
  | "tertiary"
  | "dark" 
  | "accent"
  | "orange"
  | "outline" 
  | "ghost";

export type ButtonSize = "default" | "sm" | "lg" | "icon";

export interface ButtonVariantsOptions {
  variant?: ButtonVariant;
  size?: ButtonSize;
  className?: string;
}

/**
 * Standard utility function to generate Paper Brutalist button classes.
 * Can be used in React components or Astro template strings.
 */
export function buttonVariants({
  variant = "default",
  size = "default",
  className,
}: ButtonVariantsOptions = {}): string {
  const baseClasses = 
    "inline-flex items-center justify-center gap-2 rounded-none font-mono font-bold uppercase tracking-wider transition-all duration-150 border-2 border-stone-900 shadow-[3px_3px_0px_0px_rgba(28,25,23,1)] hover:-translate-x-0.5 hover:-translate-y-0.5 hover:shadow-[5px_5px_0px_0px_rgba(28,25,23,1)] active:translate-x-0.5 active:translate-y-0.5 active:shadow-[1px_1px_0px_0px_rgba(28,25,23,1)] disabled:pointer-events-none disabled:opacity-50 select-none cursor-pointer no-underline";

  const sizes: Record<ButtonSize, string> = {
    default: "px-6 py-2.5 text-xs",
    sm: "px-3.5 py-1.5 text-xs",
    lg: "px-8 py-3.5 text-sm",
    icon: "p-2 text-sm",
  };

  const variants: Record<ButtonVariant, string> = {
    default: "bg-white text-stone-900 hover:bg-stone-50",
    primary: "bg-[#FF6B6B] text-stone-950 hover:bg-[#fa5252]",
    secondary: "bg-[#4ECDC4] text-stone-950 hover:bg-[#3dbdb4]",
    tertiary: "bg-white text-stone-900 hover:bg-stone-50",
    dark: "bg-stone-900 text-white hover:bg-stone-800",
    accent: "bg-amber-400 text-stone-950 hover:bg-amber-300",
    orange: "bg-orange-500 text-white hover:bg-orange-600",
    outline: "bg-transparent text-stone-900 hover:bg-stone-100",
    ghost: "border-transparent shadow-none hover:bg-stone-100 hover:shadow-none hover:translate-x-0 hover:translate-y-0 active:translate-x-0 active:translate-y-0",
  };

  return cn(baseClasses, sizes[size], variants[variant], className);
}


// --- CARD VARIANTS ---

export type CardVariant = "default" | "interactive" | "panel" | "subtle";

export interface CardVariantsOptions {
  variant?: CardVariant;
  className?: string;
}

export function cardVariants({
  variant = "default",
  className,
}: CardVariantsOptions = {}): string {
  const variants: Record<CardVariant, string> = {
    default: "rounded-none border-3 border-stone-900 bg-white text-stone-900 shadow-[8px_8px_0px_0px_rgba(28,25,23,1)]",
    interactive: "rounded-none border-3 border-stone-900 bg-white text-stone-900 shadow-[8px_8px_0px_0px_rgba(28,25,23,1)] hover:-translate-x-1 hover:-translate-y-1 hover:shadow-[12px_12px_0px_0px_rgba(28,25,23,1)] transition-all flex flex-col justify-between",
    panel: "rounded-none border-2 border-stone-900 bg-white text-stone-900 shadow-[4px_4px_0px_0px_rgba(28,25,23,1)]",
    subtle: "rounded-none border-2 border-stone-900 bg-stone-50 text-stone-900 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)]",
  };

  return cn(variants[variant], className);
}


// --- BADGE VARIANTS ---

export type BadgeVariant = 
  | "default" 
  | "primary" 
  | "secondary" 
  | "accent" 
  | "dark" 
  | "outline" 
  | "destructive" 
  | "success" 
  | "warning";

export interface BadgeVariantsOptions {
  variant?: BadgeVariant;
  className?: string;
}

export function badgeVariants({
  variant = "default",
  className,
}: BadgeVariantsOptions = {}): string {
  const baseClasses = 
    "inline-flex items-center rounded-none border-2 border-stone-900 px-3 py-1 font-mono text-xs font-black uppercase tracking-wider shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] select-none";

  const variants: Record<BadgeVariant, string> = {
    default: "bg-stone-900 text-white",
    primary: "bg-[#FF6B6B] text-stone-950",
    secondary: "bg-[#4ECDC4] text-stone-950",
    accent: "bg-amber-400 text-stone-950",
    dark: "bg-stone-900 text-white",
    outline: "bg-white text-stone-900",
    destructive: "bg-rose-500 text-white",
    success: "bg-emerald-400 text-stone-950",
    warning: "bg-orange-500 text-stone-950",
  };

  return cn(baseClasses, variants[variant], className);
}
