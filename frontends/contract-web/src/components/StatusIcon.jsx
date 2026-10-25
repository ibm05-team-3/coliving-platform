const ICONS = {
  clock: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5l3 2" />
    </>
  ),
  check: <path d="M5 12.5l4.5 4.5L19 7.5" />,
};

export default function StatusIcon({ type }) {
  return (
    <div className="mx-auto flex size-16 items-center justify-center rounded-full bg-primary-soft">
      <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-primary-strong">
        {ICONS[type]}
      </svg>
    </div>
  );
}
