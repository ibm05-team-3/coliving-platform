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
  const [session, setSession] = useState({ status: 'loading', authenticated: false, nickname: null })

  useEffect(() => {
    const controller = new AbortController()

    async function load() {
      try {
        const response = await fetch(`${BACKEND_API_BASE_URL}/user-service/user/session`, {
          credentials: 'include',
          signal: controller.signal,
        })
        let nickname = null
        if (response.ok) {
          const body = await response.json().catch(() => null)
          const value = body && typeof body.nickname === 'string' ? body.nickname.trim() : ''
          nickname = value || null
        }
        setSession({ status: 'ready', authenticated: response.ok, nickname })
      } catch (error) {
        if (error instanceof DOMException && error.name === 'AbortError') return
        setSession({ status: 'ready', authenticated: false, nickname: null })
      }
    }

    load()
    return () => controller.abort()
  }, [])

  return session
}
