import * as React from "react"
import {
  buttonVariants,
  type ButtonVariant,
  type ButtonSize,
  type ButtonVariantsOptions,
} from "../../variants"

export {
  buttonVariants,
  type ButtonVariant,
  type ButtonSize,
  type ButtonVariantsOptions,
};

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "default", size = "default", ...props }, ref) => {
    return (
      <button
        className={buttonVariants({ variant, size, className })}
        ref={ref}
        {...props}
      />
    );
  }
);
Button.displayName = "Button";

export { Button };
