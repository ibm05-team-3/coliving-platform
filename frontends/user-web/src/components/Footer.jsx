import { useTranslation } from 'react-i18next'

function Footer() {
  const { t, i18n } = useTranslation('common')
  const year = new Intl.DateTimeFormat(i18n.language, { year: 'numeric' })
    .formatToParts(new Date())
    .find((part) => part.type === 'year').value

  return (
    <footer className="border-t border-main/15 px-4 py-4 sm:px-8 md:px-12">
      <p className="text-sm font-semibold text-main">{t('serviceName')}</p>
      <p className="mt-0.5 text-xs font-medium text-main/80">{t('serviceNameEn')}</p>
      <p className="mt-1 text-xs text-text/70 sm:text-sm">{t('serviceDescription')}</p>
      <p className="mt-3 text-xs text-text/60">{t('copyright', { year })}</p>
    </footer>
  )
}

export default Footer
