import { useState, type FormEvent } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../api/endpoints'
import { getErrorMessage } from '../api/client'
import { Button } from '../components/Button'
import { useAuth } from '../context/AuthContext'

const DEMO_PASSWORD = 'password123'
const DEMO_ACCOUNTS = [
  { name: 'Demo User', email: 'demo@taskflow.dev' },
  { name: 'Priya Sharma', email: 'priya@taskflow.dev' },
  { name: 'Arjun Mehta', email: 'arjun@taskflow.dev' },
  { name: 'Neha Verma', email: 'neha@taskflow.dev' },
  { name: 'Rahul Iyer', email: 'rahul@taskflow.dev' },
]

export function LoginPage() {
  const { login, sessionExpired } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showDemo, setShowDemo] = useState(false)

  const mutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: (auth) => {
      login(auth)
      navigate('/', { replace: true })
    },
  })

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    mutation.mutate({ email, password })
  }

  function fillIn(accountEmail: string) {
    setEmail(accountEmail)
    setPassword(DEMO_PASSWORD)
    setShowDemo(false)
  }

  return (
    <section className="auth-card">
      <div className="auth-card__header">
        <div>
          <h1>Welcome User</h1>
          <p className="muted">Log in to manage your projects and tasks.</p>
        </div>
        <Button
          variant="secondary"
          className="demo-toggle"
          aria-expanded={showDemo}
          aria-controls="demo-panel"
          onClick={() => setShowDemo((open) => !open)}
        >
          {showDemo ? 'Close' : 'User Details'}
        </Button>
      </div>

      {showDemo && (
        <div id="demo-panel" className="demo-panel" role="region" aria-label="Demo accounts">
          <p className="demo-box__title">
            Demo accounts <span className="muted">· password: {DEMO_PASSWORD}</span>
          </p>
          <ul className="demo-box__list">
            {DEMO_ACCOUNTS.map((account) => (
              <li key={account.email}>
                <span>
                  <strong>{account.name}</strong>
                  <span className="muted"> {account.email}</span>
                </span>
                <Button variant="ghost" onClick={() => fillIn(account.email)}>Fill in</Button>
              </li>
            ))}
          </ul>
        </div>
      )}

      {sessionExpired && <p className="notice">Your session expired. Please log in again.</p>}

      <form className="stack" onSubmit={handleSubmit}>
        <label>
          Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoComplete="email" />
        </label>
        <label>
          Password
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            autoComplete="current-password"
          />
        </label>

        {mutation.isError && <p role="alert" className="error">{getErrorMessage(mutation.error)}</p>}

        <Button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? 'Logging in…' : 'Log in'}
        </Button>
      </form>

      <p className="muted auth-card__footer">
        New to TaskFlow? <Link to="/register">Create an account</Link>
      </p>
    </section>
  )
}