import { useEffect, useState } from 'react'

const BACKEND_API_BASE_URL = import.meta.env.VITE_BACKEND_API_BASE_URL

export async function logout() {
  const response = await fetch(`${BACKEND_API_BASE_URL}/user-service/user/logout`, {
    method: 'POST',
    credentials: 'include',
  })
  if (!response.ok) {
    throw new Error(String(response.status))
  }
}

export function useSession() {
  const [session, setSession] = useState({ status: 'loading', authenticated: false, forbidden: false })

  useEffect(() => {
    const controller = new AbortController()

    async function load() {
      try {
        const response = await fetch(`${BACKEND_API_BASE_URL}/user-service/admin/session`, {
          credentials: 'include',
          signal: controller.signal,
        })
        setSession({
          status: 'ready',
          authenticated: response.ok,
          forbidden: response.status === 403,
        })
      } catch (error) {
        if (error instanceof DOMException && error.name === 'AbortError') return
        setSession({ status: 'ready', authenticated: false, forbidden: false })
      }
    }

    load()
    return () => controller.abort()
  }, [])

  return session
}
