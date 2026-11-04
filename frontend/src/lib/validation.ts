import { parseDisplayDate, todayIso } from './dates'

// Client-side checks give instant feedback. The backend still validates everything.

export function validateRegister(values: { name: string; email: string; password: string }) {
  const errors: Record<string, string> = {}
  if (!values.name.trim()) errors.name = 'Name is required'
  if (!/^\S+@\S+\.\S+$/.test(values.email)) errors.email = 'Enter a valid email address'
  if (values.password.length < 8) errors.password = 'Password must be at least 8 characters'
  return errors
}

/** dueDate is what the user typed, in dd-mm-yyyy. */
export function validateTask(values: { title: string; dueDate: string }, mode: 'create' | 'edit') {
  const errors: Record<string, string> = {}
  const title = values.title.trim()
  if (!title) errors.title = 'Title is required'
  else if (title.length > 120) errors.title = 'Title must be 120 characters or fewer'

  const dueDate = values.dueDate.trim()
  if (dueDate) {
    const iso = parseDisplayDate(dueDate)
    if (!iso) {
      errors.dueDate = 'Use the format DD-MM-YYYY'
    } else if (mode === 'create' && iso < todayIso()) {
      errors.dueDate = 'Due date cannot be in the past'
    }
  }
  return errors
}