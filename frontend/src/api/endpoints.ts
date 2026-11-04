import { api } from './client'
import type {
  AuthResponse,
  Page,
  Project,
  Task,
  TaskFilters,
  TaskInput,
  TaskStatus,
  TaskSummary,
  UserSummary,
} from '../types'

export const authApi = {
  register: (body: { name: string; email: string; password: string }) =>
    api.post<AuthResponse>('/api/auth/register', body).then((r) => r.data),
  login: (body: { email: string; password: string }) =>
    api.post<AuthResponse>('/api/auth/login', body).then((r) => r.data),
}

export const projectsApi = {
  list: () => api.get<Project[]>('/api/projects').then((r) => r.data),
  get: (id: number) => api.get<Project>(`/api/projects/${id}`).then((r) => r.data),
  create: (body: { name: string; description: string | null }) =>
    api.post<Project>('/api/projects', body).then((r) => r.data),
}

export const tasksApi = {
  list: (projectId: number, filters: TaskFilters & { page?: number; size?: number }) =>
    api.get<Page<Task>>(`/api/projects/${projectId}/tasks`, { params: filters }).then((r) => r.data),
  summary: (projectId: number) =>
    api.get<TaskSummary>(`/api/projects/${projectId}/summary`).then((r) => r.data),
  create: (projectId: number, input: TaskInput) =>
    api.post<Task>(`/api/projects/${projectId}/tasks`, input).then((r) => r.data),
  update: (taskId: number, input: TaskInput & { status: TaskStatus }) =>
    api.put<Task>(`/api/tasks/${taskId}`, input).then((r) => r.data),
  updateStatus: (taskId: number, status: TaskStatus) =>
    api.patch<Task>(`/api/tasks/${taskId}/status`, { status }).then((r) => r.data),
  remove: (taskId: number) => api.delete(`/api/tasks/${taskId}`).then(() => undefined),
}

export const usersApi = {
  list: () => api.get<UserSummary[]>('/api/users').then((r) => r.data),
}
