import { Navigate, Outlet } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuthStore } from '@/store/auth'
import type { Role } from '@/types/auth'

type ProtectedRouteProps = {
  role: Role
  children?: ReactNode
}

export function ProtectedRoute({ role, children }: ProtectedRouteProps) {
  const { token, role: currentRole } = useAuthStore()

  if (!token) {
    return <Navigate to="/login" replace />
  }
  if (currentRole !== role) {
    return <Navigate to={currentRole === 'ADMIN' ? '/admin' : '/guard'} replace />
  }
  return children ?? <Outlet />
}
