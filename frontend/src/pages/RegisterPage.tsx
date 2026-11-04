import { useState, type ChangeEvent, type FormEvent } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../api/endpoints'
import { getErrorMessage, getFieldErrors } from '../api/client'
import { Button } from '../components/Button'
import { useAuth } from '../context/AuthContext'
import { validateRegister } from '../lib/validation'

export function RegisterPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [clientErrors, setClientErrors] = useState<Record<string, string>>({})

  const mutation = useMutation({
    mutationFn: authApi.register,
    onSuccess: (auth) => {
      login(auth)
      navigate('/', { replace: true })
    },
  })

  const errors = { ...getFieldErrors(mutation.error), ...clientErrors }
  const update = (field: keyof typeof form) => (event: ChangeEvent<HTMLInputElement>) =>
    setForm((current) => ({ ...current, [field]: event.target.value }))

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const found = validateRegister(form)
    setClientErrors(found)
    if (Object.keys(found).length > 0) return
    mutation.mutate(form)
  }

  return (
    <section className="auth-card">
      <h1>Create account</h1>

      <form className="stack" onSubmit={handleSubmit} noValidate>
        <label>
          Name
          <input value={form.name} onChange={update('name')} aria-invalid={!!errors.name} />
          {errors.name && <span className="field-error">{errors.name}</span>}
        </label>
        <label>
          Email
          <input type="email" value={form.email} onChange={update('email')} aria-invalid={!!errors.email} />
          {errors.email && <span className="field-error">{errors.email}</span>}
        </label>
        <label>
          Password
          <input type="password" value={form.password} onChange={update('password')} aria-invalid={!!errors.password} />
          {errors.password && <span className="field-error">{errors.password}</span>}
        </label>

        {mutation.isError && <p role="alert" className="error">{getErrorMessage(mutation.error)}</p>}

        <Button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? 'Creating account…' : 'Register'}
        </Button>
      </form>

      <p className="muted">
        Already have an account? <Link to="/login">Log in</Link>
      </p>
    </section>
  )
}
