import type { ReactNode } from 'react'
import { Button } from './Button'

interface StateMessageProps {
  kind?: 'info' | 'error'
  onRetry?: () => void
  children: ReactNode
}

/** Shared message box for loading, empty and error states. */
export function StateMessage({ kind = 'info', onRetry, children }: StateMessageProps) {
  return (
    <div className={`state-message state-${kind}`} role={kind === 'error' ? 'alert' : 'status'}>
      <p>{children}</p>
      {onRetry && (
        <Button variant="secondary" onClick={onRetry}>
          Try again
        </Button>
      )}
    </div>
  )
}
