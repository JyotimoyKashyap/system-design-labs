import * as React from "react"
import { cn } from "../../lib/utils"

export interface InputProps
  extends React.InputHTMLAttributes<HTMLInputElement> {
  variant?: "default" | "error" | "success";
}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, variant = "default", type, ...props }, ref) => {
    
    // Base classes for Paper Brutalism Input
    const baseClasses = "flex w-full rounded-none px-6 py-4 bg-white font-medium text-stone-900 placeholder:text-stone-400 placeholder:font-normal transition-all duration-200 focus:outline-none focus:shadow-[4px_4px_0px_0px_rgba(28,25,23,1)] focus:-translate-y-[2px] focus:-translate-x-[2px] disabled:cursor-not-allowed disabled:opacity-50 disabled:bg-stone-100";
    
    const variants = {
      default: "border-2 border-stone-900",
      error: "border-2 border-rose-500 focus:shadow-[4px_4px_0px_0px_#f43f5e] focus:border-rose-500",
      success: "border-2 border-emerald-500 focus:shadow-[4px_4px_0px_0px_#10b981] focus:border-emerald-500",
    }

    return (
      <input
        type={type}
        className={cn(baseClasses, variants[variant], className)}
        ref={ref}
        {...props}
      />
    )
  }
)
Input.displayName = "Input"

export { Input }
