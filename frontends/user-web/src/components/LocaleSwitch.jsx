import { useTranslation } from 'react-i18next'
import { LOCALES } from '../i18n'

const LOCALE_LABEL = {
  ko: 'localeKo',
  en: 'localeEn',
}

function LocaleSwitch({ layout = 'inline' }) {
  const { t, i18n } = useTranslation('common')
  const current = i18n.resolvedLanguage
  const menu = layout === 'menu'

  return (
    <div className={menu ? 'grid gap-1' : 'flex flex-wrap gap-2'}>
      {LOCALES.map((locale) => {
        const selected = current === locale
        return (
          <button
            key={locale}
            type="button"
            aria-pressed={selected}
            onClick={() => i18n.changeLanguage(locale)}
            className={
              menu
                ? `w-full rounded-lg px-3 py-1.5 text-center text-sm whitespace-nowrap focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent ${
                    selected ? 'bg-main font-semibold text-background' : 'font-medium text-text hover:bg-main/10'
                  }`
                : `rounded-lg px-3 py-1.5 text-sm outline-none focus:ring-2 focus:ring-accent ${
                    selected
                      ? 'bg-main font-semibold text-background'
                      : 'font-medium text-text/70'
                  }`
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
