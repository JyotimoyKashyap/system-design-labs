import * as React from "react"
import { cn } from "../../lib/utils"
import {
  badgeVariants,
  type BadgeVariant,
  type BadgeVariantsOptions,
} from "../../variants"

export {
  badgeVariants,
  type BadgeVariant,
  type BadgeVariantsOptions,
};

export interface BadgeProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: BadgeVariant;
}

function Badge({ className, variant = "default", ...props }: BadgeProps) {
  return (
    <div className={badgeVariants({ variant, className })} {...props} />
  );
}

export { Badge };
