import axios from 'axios'
import type { ApiError } from '../types'

export const TOKEN_KEY = 'taskflow.token'
export const USER_KEY = 'taskflow.user'
export const UNAUTHORIZED_EVENT = 'taskflow:unauthorized'

/** One axios instance for the whole app. All API calls go through it. */
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080',
})

// Attach the JWT to every request.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// If the token is rejected, clear the session and tell the app to send the user to login.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthCall = String(error.config?.url ?? '').startsWith('/api/auth/')
    if (error.response?.status === 401 && !isAuthCall) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
      window.dispatchEvent(new Event(UNAUTHORIZED_EVENT))
    }
    return Promise.reject(error)
  },
)

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError<ApiError>(error)) {
    if (!error.response) return 'Cannot reach the server. Is the backend running?'
    return error.response.data?.message ?? 'Something went wrong'
  }
  return 'Something went wrong'
}

export function getFieldErrors(error: unknown): Record<string, string> {
  if (axios.isAxiosError<ApiError>(error)) {
    return error.response?.data?.fieldErrors ?? {}
  }
  return {}
}
