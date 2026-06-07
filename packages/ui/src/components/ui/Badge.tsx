import * as React from "react"
import { cn } from "../../lib/utils"

export interface BadgeProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: "default" | "secondary" | "outline" | "destructive" | "success" | "warning";
}

function Badge({ className, variant = "default", ...props }: BadgeProps) {
  
  const baseClasses = "inline-flex items-center rounded-none border-2 border-stone-900 px-2.5 py-0.5 text-xs font-bold transition-colors focus:outline-none focus:ring-2 focus:ring-stone-900 focus:ring-offset-2";
  
  const variants = {
    default: "bg-stone-900 text-white",
    secondary: "bg-stone-100 text-stone-900",
    outline: "text-stone-900",
    destructive: "bg-red-500 text-white",
    success: "bg-emerald-400 text-stone-900",
    warning: "bg-orange-500 text-stone-900"
  }

  return (
    <div className={cn(baseClasses, variants[variant], className)} {...props} />
  )
}

export { Badge }
