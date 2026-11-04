import type { TaskSummary } from '../types'

export function SummaryBar({ summary }: { summary: TaskSummary }) {
  return (
    <div className="summary-bar" role="region" aria-label="Task summary">
      <span><strong>{summary.total}</strong> total</span>
      <span><strong>{summary.byStatus.TODO}</strong> to do</span>
      <span><strong>{summary.byStatus.IN_PROGRESS}</strong> in progress</span>
      <span><strong>{summary.byStatus.DONE}</strong> done</span>
      <span className={summary.overdue > 0 ? 'overdue-text' : undefined}>
        <strong>{summary.overdue}</strong> overdue
      </span>
    </div>
  )
}
