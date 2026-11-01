import axios from 'axios'
import { useAuthStore } from '@/store/auth'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
})

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (
      axios.isAxiosError(error) &&
      error.response?.status === 401 &&
      !error.config?.url?.endsWith('/auth/login')
    ) {
      useAuthStore.getState().logout()
      window.location.assign('/login')
    }
    return Promise.reject(error)
  },
)

export function errorMessage(error: unknown): string {
  if (axios.isAxiosError<{ message?: string; error?: string }>(error)) {
    if (error.response?.data?.message) return error.response.data.message
    if (error.response?.data?.error) return error.response.data.error
    if (error.response) return `Request failed (${error.response.status})`
    if (error.code === 'ERR_NETWORK') return 'Cannot reach the backend. Check that Spring Boot is running.'
  }
  return 'Something went wrong. Please try again.'
}
