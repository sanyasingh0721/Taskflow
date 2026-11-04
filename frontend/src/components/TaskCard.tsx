import type { Task, TaskStatus } from '../types'
import { formatDate } from '../lib/dates'
import { Button } from './Button'

interface TaskCardProps {
  task: Task
  busy?: boolean
  onStatusChange: (status: TaskStatus) => void
  onEdit: () => void
  onDelete: () => void
}

export function TaskCard({ task, busy = false, onStatusChange, onEdit, onDelete }: TaskCardProps) {
  const classes = ['task-card', task.overdue ? 'task-card--overdue' : ''].join(' ').trim()

  return (
    <article className={classes} aria-label={task.title}>
      <header className="task-card__header">
        <h3>{task.title}</h3>
        <span className={`badge badge-${task.priority.toLowerCase()}`}>{task.priority}</span>
      </header>
      {task.description && <p className="muted">{task.description}</p>}
      <dl className="task-card__meta">
        <div>
          <dt>Due</dt>
          <dd>
            {task.dueDate ? formatDate(task.dueDate) : 'No date'}
            {task.overdue && <span className="overdue-tag"> Overdue</span>}
          </dd>
        </div>
        <div>
          <dt>Assignee</dt>
          <dd>{task.assignee?.name ?? 'Unassigned'}</dd>
        </div>
      </dl>
      <div className="task-card__actions">
        <label>
          <span className="sr-only">Status</span>
          <select
            value={task.status}
            disabled={busy}
            onChange={(event) => onStatusChange(event.target.value as TaskStatus)}
          >
            <option value="TODO">To Do</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="DONE">Done</option>
          </select>
        </label>
        <Button variant="ghost" onClick={onEdit}>Edit</Button>
        <Button variant="danger" onClick={onDelete}>Delete</Button>
      </div>
    </article>
  )
}