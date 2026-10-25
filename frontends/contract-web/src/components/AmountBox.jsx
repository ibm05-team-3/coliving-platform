import { won } from '../lib/format';

export default function AmountBox({ label = '첫 결제 금액', amount }) {
  return (
    <div className="flex items-center justify-between rounded-xl bg-soft px-5 py-4">
      <span className="text-sm font-bold text-ink">{label}</span>
      <span className="text-2xl font-bold text-primary-strong">
        {won(amount)}
        <span className="ml-1 text-sm font-medium text-sub">원</span>
      </span>
    </div>
  );
}
