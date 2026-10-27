import { useEffect } from 'react'

function ScreenPage({ title }) {
  useEffect(() => {
    document.title = title
  }, [title])

  return (
    <>
      <h1 className="text-2xl font-semibold text-main sm:text-3xl">{title}</h1>
      <section className="mt-6 flex min-h-64 items-center justify-center rounded-lg border border-dashed border-main/30 bg-white px-4 py-16 text-center sm:min-h-80 sm:py-24">
        <p className="text-sm text-text/70 sm:text-base">표시할 내용이 없습니다.</p>
      </section>
    </>
  )
}

export default ScreenPage
