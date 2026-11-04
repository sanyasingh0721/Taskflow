import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { projectsApi } from '../api/endpoints'
import { getErrorMessage, getFieldErrors } from '../api/client'
import { Button } from '../components/Button'
import { Modal } from '../components/Modal'
import { StateMessage } from '../components/StateMessage'

export function ProjectsPage() {
  const queryClient = useQueryClient()
  const [creating, setCreating] = useState(false)

  const projectsQuery = useQuery({ queryKey: ['projects'], queryFn: projectsApi.list })

  const createMutation = useMutation({
    mutationFn: projectsApi.create,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['projects'] })
      setCreating(false)
    },
  })

  return (
    <>
      <div className="page-header">
        <h1>Your projects</h1>
        <Button onClick={() => setCreating(true)}>New project</Button>
      </div>

      {projectsQuery.isPending && <StateMessage>Loading projects…</StateMessage>}
      {projectsQuery.isError && (
        <StateMessage kind="error" onRetry={() => void projectsQuery.refetch()}>
          {getErrorMessage(projectsQuery.error)}
        </StateMessage>
      )}
      {projectsQuery.isSuccess && projectsQuery.data.length === 0 && (
        <StateMessage>No projects yet. Create your first one.</StateMessage>
      )}

      <ul className="project-list">
        {projectsQuery.data?.map((project) => (
          <li key={project.id}>
            <Link to={`/projects/${project.id}`} className="project-card">
              <h2>{project.name}</h2>
              <p className="muted">{project.description ?? 'No description'}</p>
            </Link>
          </li>
        ))}
      </ul>

      {creating && (
        <Modal title="New project" onClose={() => setCreating(false)}>
          <NewProjectForm
            submitting={createMutation.isPending}
            errorMessage={createMutation.isError ? getErrorMessage(createMutation.error) : null}
            fieldErrors={getFieldErrors(createMutation.error)}
            onSubmit={(body) => createMutation.mutate(body)}
            onCancel={() => setCreating(false)}
          />
        </Modal>
      )}
    </>
  )
}

interface NewProjectFormProps {
  submitting: boolean
  errorMessage: string | null
  fieldErrors: Record<string, string>
  onSubmit: (body: { name: string; description: string | null }) => void
  onCancel: () => void
}

function NewProjectForm({ submitting, errorMessage, fieldErrors, onSubmit, onCancel }: NewProjectFormProps) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [nameError, setNameError] = useState<string | null>(null)

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!name.trim()) {
      setNameError('Name is required')
      return
    }
    setNameError(null)
    onSubmit({ name: name.trim(), description: description.trim() || null })
  }

  return (
    <form className="stack" onSubmit={handleSubmit} noValidate>
      <label>
        Name
        <input value={name} onChange={(e) => setName(e.target.value)} aria-invalid={!!(nameError || fieldErrors.name)} />
        {(nameError || fieldErrors.name) && <span className="field-error">{nameError ?? fieldErrors.name}</span>}
      </label>
      <label>
        Description
        <textarea rows={3} value={description} onChange={(e) => setDescription(e.target.value)} />
      </label>
      {errorMessage && <p role="alert" className="error">{errorMessage}</p>}
      <div className="form-actions">
        <Button variant="secondary" onClick={onCancel}>Cancel</Button>
        <Button type="submit" disabled={submitting}>{submitting ? 'Creating…' : 'Create project'}</Button>
      </div>
    </form>
  )
}
