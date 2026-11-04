import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { TaskCard } from './TaskCard'
import type { Task } from '../types'

const baseTask: Task = {
  id: 1,
  projectId: 1,
  title: 'Write docs',
  description: 'Update the README',
  status: 'TODO',
  priority: 'HIGH',
  dueDate: '2099-01-01',
  assignee: null,
  overdue: false,
  createdAt: '2025-01-01T00:00:00Z',
  updatedAt: '2025-01-01T00:00:00Z',
}

function renderCard(task: Task, onStatusChange = jest.fn()) {
  render(
    <TaskCard
      task={task}
      onStatusChange={onStatusChange}
      onEdit={jest.fn()}
      onDelete={jest.fn()}
    />,
  )
  return { onStatusChange }
}

describe('TaskCard', () => {
  it('shows the Overdue tag only for overdue tasks', () => {
    renderCard({ ...baseTask, overdue: true, dueDate: '2020-01-01' })
    expect(screen.getByText('Overdue')).toBeInTheDocument()
  })

  it('does not show the Overdue tag for on-time tasks', () => {
    renderCard(baseTask)
    expect(screen.queryByText('Overdue')).not.toBeInTheDocument()
  })

  it('shows the due date as DD-MM-YYYY', () => {
    renderCard({ ...baseTask, dueDate: '2099-10-31' })
    expect(screen.getByText('31-10-2099')).toBeInTheDocument()
  })

  it('reports the new status when the dropdown changes', async () => {
    const { onStatusChange } = renderCard(baseTask)

    await userEvent.selectOptions(screen.getByLabelText('Status'), 'DONE')

    expect(onStatusChange).toHaveBeenCalledWith('DONE')
  })
})