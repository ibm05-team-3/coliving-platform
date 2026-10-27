import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import enCommon from './locales/en/common.json'
import enLogin from './locales/en/login.json'
import enCommunity from './locales/en/community.json'
import koCommon from './locales/ko/common.json'
import koLogin from './locales/ko/login.json'
import koCommunity from './locales/ko/community.json'

export const LOCALES = ['ko', 'en']

const STORAGE_KEY = 'locale'
const BOUND = Symbol.for('coliving.localeBound')

export function detectLocale() {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (LOCALES.includes(stored)) return stored

  const browser = navigator.language?.toLowerCase() ?? ''
  return LOCALES.find((locale) => browser.startsWith(locale)) ?? 'ko'
}

function applyDocumentLocale(locale) {
  document.documentElement.lang = LOCALES.includes(locale) ? locale : 'ko'
}

function persistLocale(locale) {
  const next = LOCALES.includes(locale) ? locale : 'ko'
  localStorage.setItem(STORAGE_KEY, next)
  applyDocumentLocale(next)
}

export async function initI18n() {
  const locale = detectLocale()
  applyDocumentLocale(locale)

  if (!i18n[BOUND]) {
    i18n[BOUND] = true
    i18n.on('languageChanged', persistLocale)
  }

  if (i18n.isInitialized) {
    if (i18n.resolvedLanguage !== locale) await i18n.changeLanguage(locale)
    return i18n
  }

  await i18n.use(initReactI18next).init({
    resources: {
      ko: { common: koCommon, login: koLogin, community: koCommunity },
      en: { common: enCommon, login: enLogin, community: enCommunity },
    },
    lng: locale,
    fallbackLng: 'ko',
    supportedLngs: LOCALES,
    ns: ['common', 'login', 'community'],
    defaultNS: 'common',
    interpolation: { escapeValue: false },
    react: { useSuspense: false },
  })

  return i18n
}

export default i18n
