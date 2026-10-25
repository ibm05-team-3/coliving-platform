import Card from './Card';

// 라벨-값 행 목록 (신청 정보 요약 카드)
export default function InfoList({ rows, className = '' }) {
  return (
    <Card className={className}>
      <dl className="divide-y divide-line">
        {rows.map(([label, value]) => (
          <div key={label} className="flex items-center justify-between gap-4 px-5 py-3.5 text-sm">
            <dt className="text-sub">{label}</dt>
            <dd className="text-right font-medium text-ink">{value}</dd>
          </div>
        ))}
      </dl>
    </Card>
  );
}
