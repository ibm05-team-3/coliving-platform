import { useEffect } from 'react'
import { useTranslation } from 'react-i18next'

function SectionPage({ titleKey }) {
  const { t, i18n } = useTranslation('common')

  useEffect(() => {
    document.title = t(titleKey)
  }, [t, i18n.language, titleKey])

  return (
    <div className="px-4 py-6 sm:px-8 sm:py-8 md:px-12 md:py-10">
      <h1 className="text-2xl font-semibold text-main sm:text-3xl">{t(titleKey)}</h1>
    </div>
  )
}

export default SectionPage
