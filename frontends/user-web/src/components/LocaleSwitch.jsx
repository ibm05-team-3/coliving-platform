import { useTranslation } from 'react-i18next'
import { LOCALES } from '../i18n'

const LOCALE_LABEL = {
  ko: 'localeKo',
  en: 'localeEn',
}

function LocaleSwitch() {
  const { t, i18n } = useTranslation('common')
  const current = i18n.resolvedLanguage

  return (
    <div className="flex flex-wrap gap-2">
      {LOCALES.map((locale) => {
        const selected = current === locale
        return (
          <button
            key={locale}
            type="button"
            aria-pressed={selected}
            onClick={() => i18n.changeLanguage(locale)}
            className={
              selected
                ? 'rounded-lg bg-main px-3 py-1.5 text-sm font-semibold text-background outline-none focus:ring-2 focus:ring-accent'
                : 'rounded-lg px-3 py-1.5 text-sm font-medium text-text/70 outline-none focus:ring-2 focus:ring-accent'
            }
          >
            {t(LOCALE_LABEL[locale])}
          </button>
        )
      })}
    </div>
  )
}

export default LocaleSwitch
