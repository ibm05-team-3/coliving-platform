import { useEffect, useLayoutEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { NavLink, useLocation, useNavigate } from 'react-router'
import LocaleSwitch from './LocaleSwitch'
import { isResident } from '../resident'
import { useSession } from '../session'
import { NotificationPanel } from '../features/community/components/NotificationPanel'
import { useSupportWidget } from '../features/community/components/SupportWidgetContext'

const RESIDENT_LINKS = [
  { to: '/community', label: 'community' },
  { to: '/facilities', label: 'facilityReservation' },
]

const LOCALE_LABEL = {
  ko: 'localeKo',
  en: 'localeEn',
}

const MY_LINKS = [
  { to: '/my/profile', label: 'myProfile' },
  { to: '/my/application', label: 'myApplication' },
  { to: '/my/reservations', label: 'myReservations' },
  { to: '/my/settings', label: 'accountSettings' },
]

function linkClass(isActive) {
  return `rounded-lg px-2 py-1.5 text-sm font-medium focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent sm:px-3 ${
    isActive ? 'bg-main text-background' : 'text-text hover:bg-main/10'
  }`
}

function MenuLink({ to, label }) {
  const { t } = useTranslation('common')

  return (
    <NavLink to={to} className={({ isActive }) => linkClass(isActive)}>
      {t(label)}
    </NavLink>
  )
}

function LanguageMenu() {
  const { t, i18n } = useTranslation('common')
  const currentLocale = i18n.resolvedLanguage === 'en' ? 'en' : 'ko'
  const location = useLocation()
  const [open, setOpen] = useState(false)
  const rootRef = useRef(null)

  useEffect(() => {
    setOpen(false)
  }, [location.pathname])

  useEffect(() => {
    if (!open) return undefined

    function onPointerDown(event) {
      if (!rootRef.current?.contains(event.target)) setOpen(false)
    }

    function onKeyDown(event) {
      if (event.key === 'Escape') setOpen(false)
    }

    document.addEventListener('pointerdown', onPointerDown)
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.removeEventListener('pointerdown', onPointerDown)
      document.removeEventListener('keydown', onKeyDown)
    }
  }, [open])

  return (
    <div ref={rootRef} className="relative">
      <button
        type="button"
        aria-expanded={open}
        aria-controls="language-menu"
        onClick={() => setOpen((value) => !value)}
        className={`${linkClass(open)} inline-flex items-center gap-1.5 whitespace-nowrap`}
      >
        <svg viewBox="0 0 24 24" aria-hidden="true" className="h-4 w-4">
          <circle cx="12" cy="12" r="8.25" fill="none" stroke="currentColor" strokeWidth="1.8" />
          <ellipse cx="12" cy="12" rx="3.5" ry="8.25" fill="none" stroke="currentColor" strokeWidth="1.8" />
          <path d="M4 12h16" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
        </svg>
        {t(LOCALE_LABEL[currentLocale])}
        <svg
          viewBox="0 0 12 8"
          aria-hidden="true"
          className={`h-2 w-2.5 fill-current ${open ? 'rotate-180' : ''}`}
        >
          <path d="M1.1.9h9.8L6 7.1Z" />
        </svg>
      </button>
      {open && (
        <div
          id="language-menu"
          className="absolute top-full left-0 z-20 mt-2 w-max min-w-full rounded-lg border border-main/15 bg-white p-1"
          onClick={() => setOpen(false)}
        >
          <LocaleSwitch layout="menu" />
        </div>
      )}
    </div>
  )
}

function NotificationMenu() {
  const { t } = useTranslation('common')
  const { notificationUnreadCount } = useSupportWidget()
  const location = useLocation()
  const [open, setOpen] = useState(false)
  const rootRef = useRef(null)

  useEffect(() => {
    setOpen(false)
  }, [location.pathname])

  useEffect(() => {
    if (!open) return undefined

    function onPointerDown(event) {
      if (!rootRef.current?.contains(event.target)) setOpen(false)
    }

    function onKeyDown(event) {
      if (event.key === 'Escape') setOpen(false)
    }

    document.addEventListener('pointerdown', onPointerDown)
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.removeEventListener('pointerdown', onPointerDown)
      document.removeEventListener('keydown', onKeyDown)
    }
  }, [open])

  return (
    <div ref={rootRef} className="relative">
      <button
        type="button"
        aria-label={t('notifications')}
        aria-expanded={open}
        aria-controls="notification-menu"
        onClick={() => setOpen((value) => !value)}
        className={`${linkClass(open)} relative inline-flex items-center`}
      >
        <svg viewBox="0 0 24 24" aria-hidden="true" className="h-5 w-5 fill-current">
          <path d="M12 2.8a1.15 1.15 0 0 1 .9 1.45C16.2 5 18.2 7.6 18.2 10.6V13.2c0 .55.16 1.08.46 1.54l.72 1.08a1.15 1.15 0 0 1-.96 1.78H5.58a1.15 1.15 0 0 1-.96-1.78l.72-1.08c.3-.46.46-.99.46-1.54V10.6C5.8 7.6 7.8 5 11.1 4.25A1.15 1.15 0 0 1 12 2.8Zm-2.4 15.4a2.4 2.4 0 0 0 4.8 0Z" />
        </svg>
        {notificationUnreadCount > 0 && <span className="absolute top-1 right-1 h-2.5 w-2.5 rounded-full bg-red-500" />}
      </button>
      {open && (
        <div
          id="notification-menu"
          className="absolute top-full right-0 z-20 mt-2 w-64 max-w-[calc(100vw-2rem)] rounded-lg border border-main/15 bg-white p-3"
        >
          <NotificationPanel onClose={() => setOpen(false)} />
        </div>
      )}
    </div>
  )
}

function MyMenu() {
  const { t } = useTranslation('common')
  const navigate = useNavigate()
  const location = useLocation()
  const [open, setOpen] = useState(false)
  const rootRef = useRef(null)
  const sectionActive = location.pathname.startsWith('/my')

  useEffect(() => {
    setOpen(false)
  }, [location.pathname])

  useEffect(() => {
    if (!open) return undefined

    function onPointerDown(event) {
      if (!rootRef.current?.contains(event.target)) setOpen(false)
    }

    function onKeyDown(event) {
      if (event.key === 'Escape') setOpen(false)
    }

    document.addEventListener('pointerdown', onPointerDown)
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.removeEventListener('pointerdown', onPointerDown)
      document.removeEventListener('keydown', onKeyDown)
    }
  }, [open])

  return (
    <div ref={rootRef} className="relative">
      <button
        type="button"
        aria-expanded={open}
        aria-controls="my-menu"
        onClick={() => setOpen((value) => !value)}
        className={`grid h-9 w-9 place-items-center rounded-full text-xs font-semibold focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent ${
          open || sectionActive ? 'bg-main text-background' : 'bg-main/15 text-main hover:bg-main/25'
        }`}
      >
        MY
      </button>
      {open && (
        <div
          id="my-menu"
          className="absolute top-full right-0 z-20 mt-2 grid min-w-max gap-1 rounded-lg border border-main/15 bg-white p-2"
          onClick={() => setOpen(false)}
        >
          {MY_LINKS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `rounded-lg px-3 py-2 text-left text-sm font-medium whitespace-nowrap focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent ${
                  isActive ? 'bg-main text-background' : 'text-text hover:bg-main/10'
                }`
              }
            >
              {t(item.label)}
            </NavLink>
          ))}
          <button
            type="button"
            onClick={() =>
              navigate('/login', {
                state: { from: `${location.pathname}${location.search}` },
              })
            }
            className="rounded-lg px-3 py-2 text-left text-sm font-medium whitespace-nowrap text-text hover:bg-main/10 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent"
          >
            {t('logout')}
          </button>
        </div>
      )}
    </div>
  )
}

function AuthButtons() {
  const { t } = useTranslation('common')
  const location = useLocation()

  return (
    <>
      <NavLink to="/signup" className={({ isActive }) => `${linkClass(isActive)} whitespace-nowrap`}>
        {t('signup')}
      </NavLink>
      <NavLink
        to="/login"
        state={{ from: `${location.pathname}${location.search}` }}
        className="rounded-lg bg-main px-2 py-1.5 text-sm font-semibold whitespace-nowrap text-background hover:bg-main/90 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent sm:px-3"
      >
        {t('login')}
      </NavLink>
    </>
  )
}

function ResidentLinks() {
  return (
    <div className="flex w-max items-center gap-1 whitespace-nowrap">
      {RESIDENT_LINKS.map((item) => (
        <MenuLink key={item.to} to={item.to} label={item.label} />
      ))}
    </div>
  )
}

function Header() {
  const { t, i18n } = useTranslation('common')
  const resident = isResident()
  const session = useSession()
  const headerRef = useRef(null)
  const findRef = useRef(null)
  const residentRef = useRef(null)
  const utilityRef = useRef(null)
  const [besideFind, setBesideFind] = useState(true)
  const [wrapUtilities, setWrapUtilities] = useState(false)

  useLayoutEffect(() => {
    const header = headerRef.current
    const find = findRef.current
    const utilities = utilityRef.current
    if (!header || !find || !utilities) return undefined

    function update() {
      const grid = header.firstElementChild
      const gridBox = grid.getBoundingClientRect()
      const findBox = find.getBoundingClientRect()
      const residentWidth = residentRef.current?.offsetWidth ?? 0
      const paddingRight = Number.parseFloat(getComputedStyle(grid).paddingRight) || 0
      const contentRight = gridBox.right - paddingRight
      const utilitiesStart = contentRight - utilities.offsetWidth
      const clusterEnd = findBox.right + (residentWidth > 0 ? 8 + residentWidth : 0)
      const fitsBeside = residentWidth === 0 || (clusterEnd <= contentRight && clusterEnd + 12 <= utilitiesStart)
      const rowEnd = fitsBeside ? clusterEnd : findBox.right
      setBesideFind(residentWidth > 0 && fitsBeside)
      setWrapUtilities(rowEnd + 12 > utilitiesStart)
    }

    update()
    const observer = new ResizeObserver(update)
    observer.observe(header)
    return () => observer.disconnect()
  }, [i18n.language, resident])

  return (
    <header ref={headerRef} className="border-b border-main/15">
      <div className="grid grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] items-center gap-x-3 px-4 py-3 sm:px-8 md:px-12">
        <NavLink
          to="/"
          end
          className="justify-self-start text-sm font-semibold text-main focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent"
        >
          {t('serviceName')}
        </NavLink>
        <nav aria-label={t('mainMenu')} className="contents">
          <div ref={findRef} className="relative">
            <MenuLink to="/find" label="find" />
            {resident && besideFind && (
              <div ref={residentRef} className="absolute top-1/2 left-full ml-2 -translate-y-1/2">
                <ResidentLinks />
              </div>
            )}
          </div>
          {resident && !besideFind && (
            <div className="col-span-3 mt-2 flex justify-center">
              <div ref={residentRef}>
                <ResidentLinks />
              </div>
            </div>
          )}
          <div
            ref={utilityRef}
            className={
              wrapUtilities
                ? 'col-span-3 mt-2 flex w-max items-center justify-end gap-1 justify-self-end'
                : 'col-start-3 row-start-1 flex w-max items-center justify-end gap-1 justify-self-end'
            }
          >
            <LanguageMenu />
            <NotificationMenu />
            {session.status === 'ready' && (session.authenticated ? <MyMenu /> : <AuthButtons />)}
          </div>
        </nav>
      </div>
    </header>
  )
}

export default Header
