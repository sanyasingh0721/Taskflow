import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { TOKEN_KEY, UNAUTHORIZED_EVENT, USER_KEY } from '../api/client'
import type { AuthResponse, UserSummary } from '../types'

interface AuthValue {
  token: string | null
  user: UserSummary | null
  sessionExpired: boolean
  login: (auth: AuthResponse) => void
  logout: () => void
}

const AuthContext = createContext<AuthValue | undefined>(undefined)

function readStoredUser(): UserSummary | null {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? (JSON.parse(raw) as UserSummary) : null
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY))
  const [user, setUser] = useState<UserSummary | null>(() => readStoredUser())
  const [sessionExpired, setSessionExpired] = useState(false)

  const clear = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    setToken(null)
    setUser(null)
  }, [])

  // The API client fires this event when the server rejects the token.
  useEffect(() => {
    const onExpired = () => {
      clear()
      setSessionExpired(true)
    }
    window.addEventListener(UNAUTHORIZED_EVENT, onExpired)
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, onExpired)
  }, [clear])

  const login = useCallback((auth: AuthResponse) => {
    localStorage.setItem(TOKEN_KEY, auth.token)
    localStorage.setItem(USER_KEY, JSON.stringify(auth.user))
    setToken(auth.token)
    setUser(auth.user)
    setSessionExpired(false)
  }, [])

  const logout = useCallback(() => {
    clear()
    setSessionExpired(false)
  }, [clear])

  const value = useMemo(
    () => ({ token, user, sessionExpired, login, logout }),
    [token, user, sessionExpired, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
