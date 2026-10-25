export default function Checkbox({ checked, onChange, children, bold = false }) {
  return (
    <label className="flex cursor-pointer items-center gap-3 text-sm">
      <input
        type="checkbox"
        checked={checked}
        onChange={(e) => onChange(e.target.checked)}
        className="size-[18px] cursor-pointer accent-primary"
      />
      <span className={bold ? 'font-bold text-ink' : 'text-ink'}>{children}</span>
    </label>
  );
}
