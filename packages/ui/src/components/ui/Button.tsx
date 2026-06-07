import * as React from "react"
import { cn } from "../../lib/utils"

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "default" | "primary" | "secondary" | "dark" | "outline" | "ghost";
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "default", ...props }, ref) => {
    
    // Base classes for Paper Brutalism
    const baseClasses = "inline-flex items-center justify-center gap-2 px-8 py-4 rounded-none font-bold transition-all border-2 border-stone-900 shadow-[4px_4px_0px_0px_rgba(28,25,23,1)] hover:translate-y-[2px] hover:shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] active:translate-y-[4px] active:shadow-none disabled:pointer-events-none disabled:opacity-50";
    
    const variants = {
      default: "bg-white text-stone-900 hover:bg-stone-50",
      primary: "bg-orange-500 text-white hover:bg-orange-600",
      dark: "bg-stone-900 text-white hover:bg-stone-800",
      secondary: "bg-stone-200 text-stone-900 hover:bg-stone-300",
      outline: "bg-transparent text-stone-900 hover:bg-stone-100",
      ghost: "border-transparent shadow-none hover:bg-stone-100 hover:shadow-none active:translate-y-0",
    }

    return (
      <button
        className={cn(baseClasses, variants[variant], className)}
        ref={ref}
        {...props}
      />
    )
  }
)
Button.displayName = "Button"

export { Button }
