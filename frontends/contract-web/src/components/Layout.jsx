import { Link, NavLink, Outlet } from 'react-router-dom';

const NAV = [
  { to: '/rooms', label: '매물 찾기' },
  { to: '/my/applications', label: '나의 신청' },
  { to: '/my', label: '마이페이지' },
];

export default function Layout() {
  return (
    <div className="flex min-h-screen flex-col font-sans">
      <header className="border-b border-line bg-header">
        <div className="mx-auto flex h-14 max-w-[1180px] items-center justify-between px-6">
          <Link to="/" className="text-xl font-bold text-primary">
            CoLiv
          </Link>
          <nav className="flex items-center gap-7 text-sm text-sub">
            {NAV.map((n) => (
              <NavLink key={n.to} to={n.to} className="hover:text-ink">
                {n.label}
              </NavLink>
            ))}
            <button type="button" className="hover:text-ink">
              로그아웃
            </button>
            <button
              type="button"
              className="rounded-full border border-line bg-surface px-3 py-1 text-xs text-sub hover:text-ink"
            >
              KR 한국어 ▾
            </button>
          </nav>
        </div>
      </header>

      <main className="flex-1">
        <Outlet />
      </main>

      <footer className="border-t border-line">
        <p className="mx-auto max-w-[1180px] px-6 py-5 text-xs text-muted">
          [회사명] · 고객센터 [전화번호] · Copyright © 2026
        </p>
      </footer>
    </div>
  );
}
