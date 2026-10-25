import { useState } from 'react';
import { DAY_NAMES } from '../lib/format';

export default function Calendar({ value, onChange }) {
  const [view, setView] = useState(new Date(value.getFullYear(), value.getMonth(), 1));
  const y = view.getFullYear();
  const m = view.getMonth();
  const firstDay = new Date(y, m, 1).getDay();
  const days = new Date(y, m + 1, 0).getDate();
  const cells = [...Array(firstDay).fill(null), ...Array.from({ length: days }, (_, i) => i + 1)];
  const isSel = (d) => d && value.getFullYear() === y && value.getMonth() === m && value.getDate() === d;

  return (
    <div className="px-5 pb-4 pt-2">
      <div className="mb-2 flex items-center justify-between">
        <button type="button" className="size-8 rounded-full text-sub hover:bg-soft" onClick={() => setView(new Date(y, m - 1, 1))}>
          ‹
        </button>
        <span className="text-sm font-bold">
          {y}.{String(m + 1).padStart(2, '0')}
        </span>
        <button type="button" className="size-8 rounded-full text-sub hover:bg-soft" onClick={() => setView(new Date(y, m + 1, 1))}>
          ›
        </button>
      </div>
      <div className="grid grid-cols-7 gap-y-1 text-center text-sm">
        {DAY_NAMES.map((d) => (
          <span key={d} className="py-1 text-xs text-muted">
            {d}
          </span>
        ))}
        {cells.map((d, i) =>
          d ? (
            <button
              type="button"
              key={i}
              onClick={() => onChange(new Date(y, m, d))}
              className={`mx-auto size-9 rounded-full ${isSel(d) ? 'bg-primary font-bold text-white' : 'text-ink hover:bg-soft'}`}
            >
              {d}
            </button>
          ) : (
            <span key={i} />
          ),
        )}
      </div>
    </div>
  );
}
