// 좌측 본문 + 우측 신청 정보(sticky) 2단 레이아웃
export default function PageShell({ main, aside, title = '신청 정보' }) {
  return (
    <div className="mx-auto grid max-w-[1180px] gap-8 px-6 py-8 lg:grid-cols-[1fr_360px]">
      <div className="space-y-8">{main}</div>
      <aside className="space-y-3 self-start lg:sticky lg:top-6">
        <h2 className="text-[17px] font-bold text-ink">{title}</h2>
        {aside}
      </aside>
    </div>
  );
}
