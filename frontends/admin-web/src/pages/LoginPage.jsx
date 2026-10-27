import { useEffect, useState } from 'react'

const BACKEND_API_BASE_URL = import.meta.env.VITE_BACKEND_API_BASE_URL

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

function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [result, setResult] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    document.title = '로그인'
  }, [])

  async function handleSubmit(event) {
    event.preventDefault()
    setSubmitting(true)
    try {
      const response = await fetch(`${BACKEND_API_BASE_URL}/user-service/user/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ email, password }),
      })
      const body = await response.text()
      setResult(body ? `${response.status}\n${body}` : String(response.status))
    } catch (error) {
      setResult(error instanceof Error ? error.message : String(error))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="grid min-h-dvh bg-background text-text lg:grid-cols-2">
      <header className="bg-main px-4 py-8 text-background sm:px-8 sm:py-12 md:px-12 lg:flex lg:flex-col lg:justify-end lg:px-16 lg:py-20">
        <p className="text-xs font-semibold text-accent sm:text-sm">룸링크</p>
        <h1 className="mt-3 text-2xl font-semibold sm:text-3xl md:text-4xl lg:text-5xl">함께 사는 집</h1>
        <p className="mt-2 max-w-md text-sm text-background/85 sm:text-base md:mt-4">공유 주거 공간에 다시 들어오세요.</p>
      </header>

      <section className="flex min-w-0 items-center px-4 py-8 sm:px-8 sm:py-12 md:px-12 lg:px-16">
        <form onSubmit={handleSubmit} className="grid w-full min-w-0 max-w-md gap-4 sm:gap-5">
          <h2 className="text-xl font-semibold text-main sm:text-2xl">로그인</h2>
          <TextField
            id="email"
            label="이메일"
            type="email"
            autoComplete="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
          <TextField
            id="password"
            label="비밀번호"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
          <button
            type="submit"
            disabled={submitting}
            className="mt-1 rounded-lg bg-main px-4 py-3 text-sm font-semibold text-background hover:bg-main/90 disabled:opacity-60 sm:text-base"
          >
            {submitting ? '로그인 중' : '로그인'}
          </button>
          {result !== '' && (
            <p className="max-h-[40dvh] min-w-0 overflow-auto whitespace-pre-wrap break-all rounded-lg border border-main/20 bg-white p-3 text-sm">
              {result}
            </p>
          )}
        </form>
      </section>
    </main>
  )
}

export default LoginPage
