import Card from './Card';

// 로딩·에러 안내 (화면 본문 자리에 표시)
export default function StatusMessage({ title, description, action }) {
  return (
    <div className="mx-auto max-w-[1180px] px-6 py-8">
      <Card className="flex flex-col items-center gap-3 px-6 py-16 text-center">
        <p className="text-lg font-bold text-ink">{title}</p>
        {description && <p className="text-sm text-sub">{description}</p>}
        {action && <div className="mt-3">{action}</div>}
      </Card>
    </div>
  );
}
