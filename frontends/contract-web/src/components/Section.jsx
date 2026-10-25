export default function Section({ title, children, className = '' }) {
  return (
    <section className={className}>
      <h2 className="mb-3 text-[17px] font-bold text-ink">{title}</h2>
      {children}
    </section>
  );
}
