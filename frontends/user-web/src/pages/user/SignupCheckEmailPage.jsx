import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import { useTranslation } from 'react-i18next'
import LocaleSwitch from '../../components/LocaleSwitch'
import { readSignupEmail, storeSignupEmail } from './signupEmailStorage'

function emailFromState(state) {
  const value = state?.email
  return typeof value === 'string' ? value.trim() : ''
}

function SignupCheckEmailPage() {
  const { t, i18n } = useTranslation('signup')
  const { t: tCommon } = useTranslation('common')
  const location = useLocation()
  const navigate = useNavigate()
  const fromState = emailFromState(location.state)
  const [email] = useState(() => fromState || readSignupEmail())

  useEffect(() => {
    if (fromState) storeSignupEmail(fromState)
  }, [fromState])

  useEffect(() => {
    if (!email) navigate('/signup', { replace: true })
  }, [email, navigate])

  useEffect(() => {
    document.title = t('checkEmailDocumentTitle')
  }, [t, i18n.language])

  if (!email) return null

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
        <div className="grid w-full min-w-0 max-w-md gap-4 sm:gap-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h2 className="text-xl font-semibold text-main sm:text-2xl">{t('checkEmailTitle')}</h2>
            <LocaleSwitch />
          </div>
          <p className="min-w-0 text-sm break-all text-text sm:text-base">{t('checkEmailBody', { email })}</p>
          <p className="text-sm text-text/70">{t('checkEmailHint')}</p>
          <Link
            to="/login"
            className="rounded-lg bg-main px-4 py-3 text-center text-sm font-semibold text-background no-underline hover:bg-main/90 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent sm:text-base"
          >
            {tCommon('login')}
          </Link>
        </div>
      </section>
    </main>
  )
}

export default SignupCheckEmailPage
