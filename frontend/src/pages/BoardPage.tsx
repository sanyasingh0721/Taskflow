import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { projectsApi, tasksApi, usersApi } from '../api/endpoints'
import { getErrorMessage, getFieldErrors } from '../api/client'
import { Button } from '../components/Button'
import { Modal } from '../components/Modal'
import { StateMessage } from '../components/StateMessage'
import { SummaryBar } from '../components/SummaryBar'
import { TaskCard } from '../components/TaskCard'
import { TaskForm } from '../components/TaskForm'
import type { Priority, Task, TaskFilters, TaskInput, TaskStatus } from '../types'

const COLUMNS: { status: TaskStatus; label: string }[] = [
  { status: 'TODO', label: 'To Do' },
  { status: 'IN_PROGRESS', label: 'In Progress' },
  { status: 'DONE', label: 'Done' },
]

type Editor = { mode: 'create' } | { mode: 'edit'; task: Task }

export function BoardPage() {
  const projectId = Number(useParams().projectId)
  const queryClient = useQueryClient()
  const [filters, setFilters] = useState<TaskFilters>({})
  const [editor, setEditor] = useState<Editor | null>(null)

  const projectQuery = useQuery({
    queryKey: ['project', projectId],
    queryFn: () => projectsApi.get(projectId),
  })
  const tasksQuery = useQuery({
    queryKey: ['tasks', projectId, filters],
    queryFn: () => tasksApi.list(projectId, { ...filters, size: 100 }),
  })
  const summaryQuery = useQuery({
    queryKey: ['summary', projectId],
    queryFn: () => tasksApi.summary(projectId),
  })
  const usersQuery = useQuery({ queryKey: ['users'], queryFn: usersApi.list })

  // Any change to tasks also changes the counts, so refresh both.
  const refreshBoard = () => {
    void queryClient.invalidateQueries({ queryKey: ['tasks', projectId] })
    void queryClient.invalidateQueries({ queryKey: ['summary', projectId] })
  }

  const saveMutation = useMutation({
    mutationFn: (args: { taskId: number | null; input: TaskInput; currentStatus: TaskStatus }) =>
      args.taskId === null
        ? tasksApi.create(projectId, args.input)
        : tasksApi.update(args.taskId, { ...args.input, status: args.currentStatus }),
    onSuccess: () => {
      refreshBoard()
      setEditor(null)
    },
  })

  const statusMutation = useMutation({
    mutationFn: (args: { taskId: number; status: TaskStatus }) =>
      tasksApi.updateStatus(args.taskId, args.status),
    onSuccess: refreshBoard,
  })

  const deleteMutation = useMutation({
    mutationFn: (taskId: number) => tasksApi.remove(taskId),
    onSuccess: refreshBoard,
  })

  if (projectQuery.isError) {
    return (
      <StateMessage kind="error" onRetry={() => void projectQuery.refetch()}>
        {getErrorMessage(projectQuery.error)}
      </StateMessage>
    )
  }

  const tasks = tasksQuery.data?.content ?? []
  const assignees = usersQuery.data ?? []
  const hasFilters = Object.values(filters).some((value) => value !== undefined)

  function handleDelete(task: Task) {
    if (window.confirm(`Delete "${task.title}"?`)) deleteMutation.mutate(task.id)
  }

  return (
    <>
      <nav className="breadcrumb">
        <Link to="/">Projects</Link> / {projectQuery.data?.name ?? '…'}
      </nav>

      <div className="page-header">
        <h1>{projectQuery.data?.name ?? 'Project board'}</h1>
        <Button onClick={() => setEditor({ mode: 'create' })}>New task</Button>
      </div>

      {summaryQuery.isPending && <p className="muted">Loading summary…</p>}
      {summaryQuery.isError && (
        <StateMessage kind="error" onRetry={() => void summaryQuery.refetch()}>
          {getErrorMessage(summaryQuery.error)}
        </StateMessage>
      )}
      {summaryQuery.isSuccess && <SummaryBar summary={summaryQuery.data} />}

      <div className="filters">
        <label>
          Priority
          <select
            value={filters.priority ?? ''}
            onChange={(e) => setFilters({ ...filters, priority: (e.target.value || undefined) as Priority | undefined })}
          >
            <option value="">All</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>
        </label>

        <label>
          Assignee
          <select
            value={filters.assigneeId ?? ''}
            disabled={!usersQuery.isSuccess}
            onChange={(e) =>
              setFilters({ ...filters, assigneeId: e.target.value ? Number(e.target.value) : undefined })
            }
          >
            <option value="">{usersQuery.isPending ? 'Loading…' : 'Anyone'}</option>
            {assignees.map((u) => (
              <option key={u.id} value={u.id}>{u.name}</option>
            ))}
          </select>
          {usersQuery.isError && <span className="field-error">Could not load team members.</span>}
        </label>

        <label>
          Sort by
          <select
            value={filters.sort ?? ''}
            onChange={(e) => setFilters({ ...filters, sort: (e.target.value || undefined) as TaskFilters['sort'] })}
          >
            <option value="">Newest</option>
            <option value="dueDate">Due date</option>
            <option value="priority">Priority</option>
          </select>
        </label>
      </div>

      {tasksQuery.isPending && <StateMessage>Loading tasks…</StateMessage>}
      {tasksQuery.isError && (
        <StateMessage kind="error" onRetry={() => void tasksQuery.refetch()}>
          {getErrorMessage(tasksQuery.error)}
        </StateMessage>
      )}
      {tasksQuery.isSuccess && tasks.length === 0 && (
        <StateMessage>
          {hasFilters ? 'No tasks match these filters.' : 'No tasks yet. Create the first one.'}
        </StateMessage>
      )}
      {statusMutation.isError && (
        <p role="alert" className="error">
          The status could not be changed. {getErrorMessage(statusMutation.error)}
        </p>
      )}
      {deleteMutation.isError && <p role="alert" className="error">{getErrorMessage(deleteMutation.error)}</p>}

      <div className="board">
        {COLUMNS.map((column) => {
          const columnTasks = tasks.filter((task) => task.status === column.status)
          return (
            <section key={column.status} className="column" aria-label={column.label}>
              <h2>
                {column.label} <span className="count">{columnTasks.length}</span>
              </h2>
              {columnTasks.map((task) => (
                <TaskCard
                  key={task.id}
                  task={task}
                  busy={statusMutation.isPending}
                  onStatusChange={(status) => statusMutation.mutate({ taskId: task.id, status })}
                  onEdit={() => setEditor({ mode: 'edit', task })}
                  onDelete={() => handleDelete(task)}
                />
              ))}
            </section>
          )
        })}
      </div>

      {editor && (
        <Modal title={editor.mode === 'create' ? 'New task' : 'Edit task'} onClose={() => setEditor(null)}>
          {usersQuery.isPending ? (
            <StateMessage>Loading team members…</StateMessage>
          ) : (
            <>
              {usersQuery.isError && (
                <p role="alert" className="error">
                  {getErrorMessage(usersQuery.error)} You can still save the task without an assignee.
                </p>
              )}
              <TaskForm
                initial={editor.mode === 'edit' ? editor.task : undefined}
                users={assignees}
                submitting={saveMutation.isPending}
                serverError={saveMutation.isError ? getErrorMessage(saveMutation.error) : null}
                serverFieldErrors={getFieldErrors(saveMutation.error)}
                onCancel={() => setEditor(null)}
                onSubmit={(input) =>
                  saveMutation.mutate({
                    taskId: editor.mode === 'edit' ? editor.task.id : null,
                    input,
                    currentStatus: editor.mode === 'edit' ? editor.task.status : 'TODO',
                  })
                }
              />
            </>
          )}
        </Modal>
      )}
    </>
  )
}