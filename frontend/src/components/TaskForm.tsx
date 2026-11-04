import { useState, type FormEvent } from 'react'
import { formatDate, parseDisplayDate } from '../lib/dates'
import { validateTask } from '../lib/validation'
import type { Priority, Task, TaskInput, UserSummary } from '../types'
import { Button } from './Button'

interface TaskFormProps {
  initial?: Task
  users: UserSummary[]
  submitting: boolean
  serverError: string | null
  serverFieldErrors: Record<string, string>
  onSubmit: (input: TaskInput) => void
  onCancel: () => void
}

export function TaskForm({
  initial,
  users,
  submitting,
  serverError,
  serverFieldErrors,
  onSubmit,
  onCancel,
}: TaskFormProps) {
  const mode = initial ? 'edit' : 'create'
  const [title, setTitle] = useState(initial?.title ?? '')
  const [description, setDescription] = useState(initial?.description ?? '')
  const [priority, setPriority] = useState<Priority>(initial?.priority ?? 'MEDIUM')
  const [dueDate, setDueDate] = useState(formatDate(initial?.dueDate ?? null))
  const [assigneeId, setAssigneeId] = useState(initial?.assignee ? String(initial.assignee.id) : '')
  const [clientErrors, setClientErrors] = useState<Record<string, string>>({})

  const errors = { ...serverFieldErrors, ...clientErrors }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const found = validateTask({ title, dueDate }, mode)
    setClientErrors(found)
    if (Object.keys(found).length > 0) return

    onSubmit({
      title: title.trim(),
      description: description.trim() || null,
      priority,
      dueDate: dueDate.trim() ? parseDisplayDate(dueDate) : null,
      assigneeId: assigneeId ? Number(assigneeId) : null,
    })
  }

  return (
    <form className="stack" onSubmit={handleSubmit} noValidate>
      <label>
        Title
        <input value={title} onChange={(e) => setTitle(e.target.value)} aria-invalid={!!errors.title} />
        {errors.title && <span className="field-error">{errors.title}</span>}
      </label>

      <label>
        Description
        <textarea rows={3} value={description} onChange={(e) => setDescription(e.target.value)} />
      </label>

      <div className="form-row">
        <label>
          Priority
          <select value={priority} onChange={(e) => setPriority(e.target.value as Priority)}>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
          </select>
        </label>

        <label>
          Due date
          <input
            type="text"
            inputMode="numeric"
            placeholder="DD-MM-YYYY"
            maxLength={10}
            value={dueDate}
            onChange={(e) => setDueDate(e.target.value)}
            aria-invalid={!!errors.dueDate}
          />
          {errors.dueDate && <span className="field-error">{errors.dueDate}</span>}
        </label>
      </div>

      <label>
        Assignee
        <select value={assigneeId} onChange={(e) => setAssigneeId(e.target.value)}>
          <option value="">Unassigned</option>
          {users.map((u) => (
            <option key={u.id} value={u.id}>{u.name}</option>
          ))}
        </select>
      </label>

      {serverError && <p role="alert" className="error">{serverError}</p>}

      <div className="form-actions">
        <Button variant="secondary" onClick={onCancel}>Cancel</Button>
        <Button type="submit" disabled={submitting}>
          {submitting ? 'Saving…' : mode === 'edit' ? 'Save changes' : 'Create task'}
        </Button>
      </div>
    </form>
  )
}