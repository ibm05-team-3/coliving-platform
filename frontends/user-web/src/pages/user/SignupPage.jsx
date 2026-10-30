import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import { useTranslation } from 'react-i18next'
import LocaleSwitch from '../../components/LocaleSwitch'
import naverIcon from '../../assets/auth/naver-icon.png'
import { storeSignupEmail } from './signupEmailStorage'
import '../../assets/auth/google-sign-in.css'

const BACKEND_API_BASE_URL = import.meta.env.VITE_BACKEND_API_BASE_URL

function socialLoginUrl(registrationId, from) {
  const url = `${BACKEND_API_BASE_URL}/user-service/user/oauth2/authorization/${registrationId}`
  const path = returnPath(from)
  if (path === '/') {
    return url
  }
  return `${url}?return_to=${encodeURIComponent(path)}`
}

function returnPath(from) {
  const safe =
    typeof from === 'string' &&
    from.startsWith('/') &&
    !from.startsWith('//') &&
    !from.startsWith('/login') &&
    !from.startsWith('/signup')
  return safe ? from : '/'
}

function GoogleMark() {
  return (
    <svg viewBox="0 0 48 48" aria-hidden="true" style={{ display: 'block' }}>
      <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z" />
      <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z" />
      <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z" />
      <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z" />
      <path fill="none" d="M0 0h48v48H0z" />
    </svg>
  )
}

function TextField({ id, label, type, value, onChange, autoComplete }) {
  return (
    <label htmlFor={id} className="grid gap-1.5 text-sm font-medium text-text sm:text-base">
      {label}
      <input
        id={id}
        name={id}
        type={type}
        value={value}
        autoComplete={autoComplete}
        required
        onChange={onChange}
        className="rounded-lg border border-main/25 bg-white px-3 py-2.5 text-base font-normal outline-none focus:border-main focus:ring-2 focus:ring-accent sm:px-4 sm:py-3"
      />
    </label>
  )
}

function SignupPage() {
  const { t, i18n } = useTranslation('signup')
  const { t: tCommon } = useTranslation('common')
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [result, setResult] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    document.title = t('documentTitle')
  }, [t, i18n.language])

  async function handleSubmit(event) {
    event.preventDefault()
    setResult('')
    if (password !== passwordConfirm) {
      setResult(t('passwordMismatch'))
      return
    }
    setSubmitting(true)
    try {
      const response = await fetch(`${BACKEND_API_BASE_URL}/user-service/user/signup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ email, password }),
      })
      if (response.status === 201) {
        const sentTo = email.trim()
        storeSignupEmail(sentTo)
        navigate('/signup/check-email', { replace: true, state: { email: sentTo } })
        return
      }
      setResult(response.status === 409 ? t('emailTaken') : t('failed'))
    } catch {
      setResult(t('failed'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="grid min-h-dvh bg-background text-text lg:grid-cols-2">
      <header className="bg-main px-4 py-8 text-background sm:px-8 sm:py-12 md:px-12 lg:flex lg:flex-col lg:justify-end lg:px-16 lg:py-20">
        <Link to="/" className="block max-w-md text-inherit no-underline focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-accent">
          <p className="text-xs font-semibold text-accent sm:text-sm">{tCommon('serviceName')}</p>
          <p className="mt-0.5 text-xs text-background/80">{tCommon('serviceNameEn')}</p>
          <h1 className="mt-3 text-2xl font-semibold sm:text-3xl md:text-4xl lg:text-5xl">{t('headline')}</h1>
          <p className="mt-2 text-sm text-background/85 sm:text-base md:mt-4">{t('description')}</p>
        </Link>
      </header>

      <section className="flex min-w-0 items-center px-4 py-8 sm:px-8 sm:py-12 md:px-12 lg:px-16">
        <form onSubmit={handleSubmit} className="grid w-full min-w-0 max-w-md gap-4 sm:gap-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h2 className="text-xl font-semibold text-main sm:text-2xl">{t('title')}</h2>
            <LocaleSwitch />
          </div>
          <TextField
            id="email"
            label={t('email')}
            type="email"
            autoComplete="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
          <TextField
            id="password"
            label={t('password')}
            type="password"
            autoComplete="new-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
          <TextField
            id="passwordConfirm"
            label={t('passwordConfirm')}
            type="password"
            autoComplete="new-password"
            value={passwordConfirm}
            onChange={(event) => setPasswordConfirm(event.target.value)}
          />
          <div className="mt-1 grid gap-2">
            <button
              type="submit"
              disabled={submitting}
              className="rounded-lg bg-main px-4 py-3 text-sm font-semibold text-background hover:bg-main/90 disabled:opacity-60 sm:text-base"
            >
              {submitting ? t('submitting') : t('submit')}
            </button>
            <p className="flex flex-wrap items-baseline justify-center gap-x-1.5 text-sm text-text/70">
              <span>{t('hasAccount')}</span>
              <Link
                to="/login"
                className="font-semibold text-main focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent"
              >
                {tCommon('login')}
              </Link>
            </p>
          </div>
          <div className="flex items-center gap-3">
            <span className="h-px flex-1 bg-main/20" />
            <span className="text-sm text-text/70">{t('or')}</span>
            <span className="h-px flex-1 bg-main/20" />
          </div>
          <a
            className="gsi-material-button block no-underline"
            style={{ width: '100%', maxWidth: 'none' }}
            href={socialLoginUrl('google', location.state?.from)}
          >
            <span className="gsi-material-button-content-wrapper">
              <span className="gsi-material-button-icon">
                <GoogleMark />
              </span>
              <span className="gsi-material-button-contents">{t('googleSignIn')}</span>
            </span>
          </a>
          <a
            className="flex h-10 w-full items-center rounded-md no-underline"
            style={{ backgroundColor: '#03A94D' }}
            href={socialLoginUrl('naver', location.state?.from)}
          >
            <img src={naverIcon} alt="" className="ml-5 h-5 w-5 shrink-0" />
            <span className="flex-1 text-center text-sm font-medium text-white">{t('naverSignIn')}</span>
          </a>
          {result !== '' && (
            <p className="min-w-0 rounded-lg border border-main/20 bg-white p-3 text-sm">{result}</p>
          )}
        </form>
      </section>
    </main>
  )
}

export default SignupPage
