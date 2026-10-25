import { useState } from 'react';
import { NavLink } from 'react-router-dom';

// 발표·확인용 화면 이동 바. VITE_PREVIEW=1 로 빌드할 때만 보인다 (실서비스 미포함)
const SCREENS = [
  ['/apply/room', '1-1 호실 선택'],
  ['/apply/form', '1-2 신청자 정보'],
  ['/apply/submitted', '접수 완료'],
  ['/contract/sign', '2 계약서·서명'],
  ['/contract/pay', '3 결제'],
  ['/contract/complete', '4 계약 완료'],
];

export default function PreviewBar() {
  const [open, setOpen] = useState(true);
  if (!open)
    return (
      <button type="button" onClick={() => setOpen(true)} className="fixed bottom-4 left-4 z-50 rounded-full bg-ink/80 px-3 py-1.5 text-xs text-white">
        화면 목록
      </button>
    );
  return (
    <nav className="fixed bottom-4 left-4 z-50 flex flex-col gap-1 rounded-2xl bg-ink/90 p-1.5 text-xs shadow-lg">
      {SCREENS.map(([to, label]) => (
        <NavLink key={to} to={to} className={({ isActive }) => `rounded-full px-3 py-1.5 ${isActive ? 'bg-white text-ink' : 'text-white/80 hover:text-white'}`}>
          {label}
        </NavLink>
      ))}
      <button type="button" onClick={() => setOpen(false)} className="rounded-full px-3 py-1 text-white/50 hover:text-white">
        숨기기
      </button>
    </nav>
  );
}
