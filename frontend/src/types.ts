// These mirror the backend DTOs. Keep them in sync when the API changes.

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE'
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH'

export interface UserSummary {
  id: number
  name: string
  email: string
}

export interface AuthResponse {
  token: string
  user: UserSummary
}

export interface Project {
  id: number
  name: string
  description: string | null
  createdAt: string
}

export interface Task {
  id: number
  projectId: number
  title: string
  description: string | null
  status: TaskStatus
  priority: Priority
  dueDate: string | null
  assignee: UserSummary | null
  overdue: boolean
  createdAt: string
  updatedAt: string
}

export interface TaskInput {
  title: string
  description: string | null
  priority: Priority
  dueDate: string | null
  assigneeId: number | null
}

export interface TaskFilters {
  status?: TaskStatus
  priority?: Priority
  assigneeId?: number
  sort?: 'dueDate' | 'priority'
}

export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface TaskSummary {
  total: number
  byStatus: Record<TaskStatus, number>
  overdue: number
}

export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  fieldErrors: Record<string, string>
}
