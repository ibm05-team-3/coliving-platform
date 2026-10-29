import type { ButtonHTMLAttributes, InputHTMLAttributes, ReactNode } from "react"

export type IconName = "arrowRight" | "bell" | "bot" | "chevronDown" | "chevronLeft" | "chevronRight" | "close" | "edit" | "eye" | "flag" | "lock" | "mail" | "message" | "more" | "pin" | "search" | "send" | "trash" | "users"
export function Icon({ name, size = 18 }: { name: IconName; size?: number }) {
  const paths: Record<IconName, ReactNode> = {
    arrowRight: <>
      <path d="M5 12h14" />
      <path d="m13 6 6 6-6 6" />
    </>, bell: <>
      <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" />
      <path d="M10 21h4" />
    </>, bot: <>
      <rect
        x="4"
        y="7"
        width="16"
        height="13"
        rx="4"
      />
      <path d="M12 3v4M8 12h.01M16 12h.01M8 16h8" />
    </>, chevronDown: <path d="m6 9 6 6 6-6" />, chevronLeft: <path d="m15 18-6-6 6-6" />, chevronRight: <path d="m9 18 6-6-6-6" />, close: <path d="m6 6 12 12M18 6 6 18" />, edit: <>
      <path d="M12 20h9" />
      <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L8 18l-4 1 1-4z" />
    </>, eye: <>
      <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12" />
      <circle
        cx="12"
        cy="12"
        r="2.5"
      />
    </>, flag: <>
      <path d="M5 21V4" />
      <path d="M5 4h11l-2 4 2 4H5" />
    </>, lock: <>
      <rect
        x="5"
        y="10"
        width="14"
        height="11"
        rx="2"
      />
      <path d="M8 10V7a4 4 0 0 1 8 0v3" />
    </>, mail: <>
      <rect
        x="3"
        y="5"
        width="18"
        height="14"
        rx="2"
      />
      <path d="m3 7 9 6 9-6" />
    </>, message: <path d="M21 15a4 4 0 0 1-4 4H8l-5 3V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z" />, more: <>
      <circle
        cx="5"
        cy="12"
        r="1"
        fill="currentColor"
        stroke="none"
      />
      <circle
        cx="12"
        cy="12"
        r="1"
        fill="currentColor"
        stroke="none"
      />
      <circle
        cx="19"
        cy="12"
        r="1"
        fill="currentColor"
        stroke="none"
      />
    </>, pin: <>
      <path d="m12 17-5 5" />
      <path d="m15 3 6 6-4 1-5 5-3-3 5-5z" />
    </>, search: <>
      <circle
        cx="11"
        cy="11"
        r="7"
      />
      <path d="m20 20-4-4" />
    </>, send: <>
      <path d="m22 2-7 20-4-9-9-4z" />
      <path d="M22 2 11 13" />
    </>, trash: <>
      <path d="M4 7h16M9 7V4h6v3M7 7l1 14h8l1-14" />
    </>, users: <>
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
      <circle
        cx="9"
        cy="7"
        r="4"
      />
      <path d="M22 21v-2a4 4 0 0 1-3-3.87M16 3.13a4 4 0 0 1 0 7.75" />
    </>,
  }
  return <svg
    width={size}
    height={size}
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="1.8"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
  >
    {paths[name]}
  </svg>
}
export function Button({ children, icon, variant = "primary", className = "", ...props }: ButtonHTMLAttributes<HTMLButtonElement> & {
  children: ReactNode;
  icon?: IconName;
  variant?: "primary" | "secondary" | "ghost" | "danger"
}) {
  const variants = {
    primary: "border-main bg-main text-background hover:bg-main/90",
    secondary: "border-main/15 bg-white text-text hover:bg-background",
    ghost: "border-transparent bg-transparent text-text/70 hover:bg-main/10",
    danger: "border-main/15 bg-accent text-text hover:bg-accent/80"
  };
  return <button
    type="button"
    className={`inline-flex min-h-10 items-center justify-center gap-2 rounded-lg border px-4 text-sm font-semibold transition focus-visible:outline-2 focus-visible:outline-accent disabled:cursor-not-allowed disabled:opacity-50 ${variants[variant]} ${className}`}
    {...props}
  >
    {icon && <Icon
      name={icon}
      size={16}
    />}
    {children}
  </button>
}
export function Badge({ children, type = "neutral" }: { children: ReactNode; type?: string }) {
  const tone = type === "success" ? "bg-main/15 text-main" : type === "warning" || type === "accent" ? "bg-accent/20 text-text" : type === "info" ? "bg-main/10 text-main" : "bg-background text-text/70";
  return <span className={`inline-flex w-fit items-center rounded-full px-2.5 py-1 text-xs font-semibold ${tone}`}>
    {children}
  </span>
}
export function Avatar({ name, size = "md" }: { name: string; size?: "sm" | "md" | "lg" }) {
  const sizes = {
    sm: "h-9 w-9 text-xs",
    md: "h-10 w-10 text-xs",
    lg: "h-12 w-12 text-sm"
  };
  return <span className={`inline-flex shrink-0 items-center justify-center rounded-full bg-main/15 font-bold text-main ${sizes[size]}`}>
    {name.slice(-2)}
  </span>
}
export function SearchField({ value, onChange, placeholder, ...props }: Omit<InputHTMLAttributes<HTMLInputElement>, "onChange"> & {
  value: string;
  onChange: (value: string) => void;
  placeholder: string
}) {
  return <label className="flex h-10 items-center gap-2 rounded-lg border border-main/15 bg-white px-3 text-text/70 focus-within:outline-2 focus-within:outline-accent">
    <Icon
      name="search"
      size={16}
    />
    <input
      className="min-w-0 flex-1 bg-transparent text-sm text-text outline-none placeholder:text-text/70"
      value={value}
      onChange={(event) => onChange(event.target.value)}
      placeholder={placeholder}
      {...props}
    />
  </label>
}
export function PageHeader({ eyebrow, title, description, actions }: {
  eyebrow?: string;
  title: string;
  description?: string;
  actions?: ReactNode
}) {
  return <header className="mb-7 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
    <div>
      {eyebrow && <p className="mb-2 text-xs font-bold tracking-[0.16em] text-accent">
        {eyebrow}
      </p>}
      <h1 className="text-2xl font-semibold text-main sm:text-3xl">
        {title}
      </h1>
      {description && <p className="mt-2 text-sm text-text/70">
        {description}
      </p>}
    </div>
    {actions}
  </header>
}
