import { useEffect, useState } from 'react'

const BACKEND_API_BASE_URL = import.meta.env.VITE_BACKEND_API_BASE_URL

export function useSession() {
  const [session, setSession] = useState({ status: 'loading', authenticated: false })

  useEffect(() => {
    const controller = new AbortController()

    async function load() {
      try {
        const response = await fetch(`${BACKEND_API_BASE_URL}/user-service/user/session`, {
          credentials: 'include',
          signal: controller.signal,
        })
        setSession({ status: 'ready', authenticated: response.ok })
      } catch (error) {
        if (error instanceof DOMException && error.name === 'AbortError') return
        setSession({ status: 'ready', authenticated: false })
      }
    }

    load()
    return () => controller.abort()
  }, [])

  return session
}
