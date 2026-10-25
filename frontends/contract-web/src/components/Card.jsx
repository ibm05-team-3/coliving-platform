export default function Card({ className = '', children }) {
  return <div className={`rounded-xl border border-line bg-surface ${className}`}>{children}</div>;
}
