import type { ButtonHTMLAttributes, InputHTMLAttributes, ReactNode } from "react"

export type IconName = "search" | "edit" | "chevronDown" | "chevronLeft" | "user" | "mail" | "phone" | "send" | "close" | "resource" | "bot" | "chevronRight" | "plus" | "flag" | "lock" | "arrowRight" | "more" | "calendar"

export function Icon({ name, size = 18 }: { name: IconName; size?: number }) {
  const paths: Record<IconName, ReactNode> = {
    search: <>
      <circle
        cx="11"
        cy="11"
        r="7"
      />
      <path d="m20 20-4-4" />
    </>,
    edit: <>
      <path d="M12 20h9" />
      <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L8 18l-4 1 1-4z" />
    </>,
    chevronDown: <path d="m6 9 6 6 6-6" />,
    chevronLeft: <path d="m15 18-6-6 6-6" />,
    chevronRight: <path d="m9 18 6-6-6-6" />,
    user: <>
      <circle
        cx="12"
        cy="8"
        r="4"
      />
      <path d="M4 21a8 8 0 0 1 16 0" />
    </>,
    mail: <>
      <rect
        x="3"
        y="5"
        width="18"
        height="14"
        rx="2"
      />
      <path d="m3 7 9 6 9-6" />
    </>,
    phone: <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.8 19.8 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.12 4.2 2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.12.9.34 1.78.65 2.61a2 2 0 0 1-.45 2.11L8.04 9.7a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.83.31 1.71.53 2.61.65A2 2 0 0 1 22 16.92z" />,
    send: <>
      <path d="m22 2-7 20-4-9-9-4z" />
      <path d="M22 2 11 13" />
    </>,
    close: <path d="m6 6 12 12M18 6 6 18" />,
    resource: <>
      <rect
        x="3"
        y="3"
        width="18"
        height="18"
        rx="3"
      />
      <path d="M8 3v18M8 8h13M8 16h13" />
    </>,
    bot: <>
      <rect
        x="4"
        y="7"
        width="16"
        height="13"
        rx="4"
      />
      <path d="M12 3v4M8 12h.01M16 12h.01M8 16h8" />
    </>,
    plus: <path d="M12 5v14M5 12h14" />,
    flag: <>
      <path d="M5 21V4" />
      <path d="M5 4h11l-2 4 2 4H5" />
    </>,
    lock: <>
      <rect
        x="5"
        y="10"
        width="14"
        height="11"
        rx="2"
      />
      <path d="M8 10V7a4 4 0 0 1 8 0v3" />
    </>,
    arrowRight: <>
      <path d="M5 12h14" />
      <path d="m13 6 6 6-6 6" />
    </>,
    more: <>
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
    </>,
    calendar: <>
      <rect
        x="3"
        y="5"
        width="18"
        height="16"
        rx="2"
      />
      <path d="M16 3v4M8 3v4M3 10h18" />
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
  variant?: "primary" | "secondary" | "ghost"
}) {
  const variants = {
    primary: "border-main bg-main text-white hover:bg-main/90",
    secondary: "border-main/15 bg-white text-text hover:bg-background",
    ghost: "border-transparent bg-transparent text-text/70 hover:bg-background"
  }
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
  const tone = type === "success" ? "bg-main/15 text-main" : type === "warning" || type === "accent" ? "bg-accent/20 text-text" : type === "info" ? "bg-main/10 text-main" : "bg-background text-text/70"
  return <span className={`inline-flex w-fit items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ${tone}`}>
    <span className="h-1.5 w-1.5 rounded-full bg-current" />
    {children}
  </span>
}

export function Avatar({ name, size = "md" }: {
  name: string;
  tone?: string;
  size?: "sm" | "md" | "lg" | "xl"
}) {
  const sizes = {
    sm: "h-9 w-9 text-xs",
    md: "h-10 w-10 text-xs",
    lg: "h-12 w-12 text-sm",
    xl: "h-20 w-20 text-lg"
  }
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

export function Select({ children, value, onChange, label }: {
  children: ReactNode;
  value: string | number;
  onChange: (value: string) => void;
  label?: string
}) {
  return <label className="flex items-center gap-2 text-xs font-semibold text-text/70">
    {label && <span>
      {label}
    </span>}
    <span className="relative">
      <select
        className="h-10 appearance-none rounded-lg border border-main/15 bg-white py-0 pr-8 pl-3 text-sm text-text focus-visible:outline-2 focus-visible:outline-accent"
        value={value}
        onChange={(event) => onChange(event.target.value)}
      >
        {children}
      </select>
      <span className="pointer-events-none absolute top-1/2 right-2 -translate-y-1/2">
        <Icon
          name="chevronDown"
          size={14}
        />
      </span>
    </span>
  </label>
}

export function PageHeader({ eyebrow, title, description, actions }: {
  eyebrow?: string;
  title: string;
  description?: string;
  actions?: ReactNode
}) {
  return <header className="mb-7 flex items-end justify-between gap-6">
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
    {actions && <div className="flex items-center gap-2">
      {actions}
    </div>}
  </header>
}
