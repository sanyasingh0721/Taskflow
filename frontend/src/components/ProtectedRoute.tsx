import { Link, Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Button } from './Button'

/** Redirects logged-out users to /login. Otherwise shows the header and the page. */
export function ProtectedRoute() {
  const { token, user, logout } = useAuth()
  const location = useLocation()

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return (
    <>
      <header className="app-header">
        <Link to="/" className="brand">TaskFlow</Link>
        <div className="header-right">
          <span className="muted">{user?.name}</span>
          <Button variant="ghost" onClick={logout}>Log out</Button>
        </div>
      </header>
      <main className="container">
        <Outlet />
      </main>
    </>
  )
}
