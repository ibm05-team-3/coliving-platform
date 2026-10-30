import { Navigate, Outlet } from 'react-router'
import { useSession } from '../session'

function RequireAuth() {
  const session = useSession()

  if (session.status === 'loading') {
    return <main className="min-h-dvh bg-background" />
  }

  if (!session.authenticated) {
    return (
      <Navigate
        to="/login"
        replace
        state={session.forbidden ? { adminRequired: true } : null}
      />
    )
  }

  return <Outlet />
}

export default RequireAuth
