import { useEffect, useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router'
import { navigation } from '../navigation'
import { useNavBadges } from '../features/community/hooks/useNavBadges'

const DESKTOP_QUERY = '(min-width: 1024px)'

function AdminLayout() {
  const location = useLocation()
  const [menuPath, setMenuPath] = useState(null)
  const [isDesktop, setIsDesktop] = useState(() => window.matchMedia(DESKTOP_QUERY).matches)
  const badgeCounts = useNavBadges()
  const open = menuPath === location.pathname
  const sidebarVisible = isDesktop || open

  useEffect(() => {
    const media = window.matchMedia(DESKTOP_QUERY)
    function update(event) {
      setIsDesktop(event.matches)
    }
    media.addEventListener('change', update)
    return () => media.removeEventListener('change', update)
  }, [])

  useEffect(() => {
    if (!open || isDesktop) {
      document.body.style.overflow = ''
      return undefined
    }

    function onKeyDown(event) {
      if (event.key === 'Escape') {
        setMenuPath(null)
      }
    }

    document.addEventListener('keydown', onKeyDown)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', onKeyDown)
      document.body.style.overflow = ''
    }
  }, [open, isDesktop])

  return (
    <div className="min-h-dvh bg-background text-text lg:grid lg:grid-cols-[16rem_minmax(0,1fr)]">
      {open && !isDesktop && (
        <button
          type="button"
          aria-label="메뉴 닫기"
          className="fixed inset-0 z-30 bg-text/40 lg:hidden"
          onClick={() => setMenuPath(null)}
        />
      )}

      <aside
        id="admin-sidebar"
        inert={!sidebarVisible}
        className={`fixed inset-y-0 left-0 z-40 flex h-dvh w-64 flex-col bg-main text-background transition-transform lg:sticky lg:top-0 lg:z-auto lg:translate-x-0 ${
          sidebarVisible ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="px-5 py-6">
          <p className="text-xs font-semibold text-accent">룸링크</p>
          <p className="mt-1 text-sm text-background/90">관리자</p>
        </div>

        <nav aria-label="관리 메뉴" className="flex-1 overflow-y-auto px-3 pb-6">
          {navigation.map((section) => (
            <div key={section.label ?? 'home'} className={section.label ? 'mt-4' : undefined}>
              {section.label && (
                <p className="px-3 pb-1 text-xs font-semibold tracking-wide text-accent">{section.label}</p>
              )}
              <ul className="grid gap-1">
                {section.items.map((item) => (
                  <li key={item.to}>
                    <NavLink
                      to={item.to}
                      end
                      className={({ isActive }) =>
                        `flex items-center rounded-lg px-3 py-2 text-sm font-medium focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent ${
                          isActive ? 'bg-background text-main' : 'text-background/90 hover:bg-white/10'
                        }`
                      }
                    >
                      <span>{item.label}</span>
                      {badgeCounts[item.to] > 0 && (
                        <span className="ml-auto inline-flex min-w-5 h-5 items-center justify-center rounded-full bg-accent px-1.5 text-xs font-semibold text-text">
                          {badgeCounts[item.to] > 99 ? '99+' : badgeCounts[item.to]}
                        </span>
                      )}
                    </NavLink>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </nav>
      </aside>

      <div className="flex min-w-0 flex-col">
        <header className="flex items-center gap-3 bg-main px-4 py-3 text-background lg:hidden">
          <button
            type="button"
            aria-expanded={open}
            aria-controls="admin-sidebar"
            onClick={() => setMenuPath(location.pathname)}
            className="rounded-md px-2 py-1 text-sm font-semibold hover:bg-white/10 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent"
          >
            메뉴
          </button>
          <p className="text-xs font-semibold text-accent">룸링크</p>
        </header>

        <main className="px-4 py-6 sm:px-8 sm:py-8 md:px-12 md:py-10">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

export default AdminLayout
