import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom'
import { ProtectedRoute } from '@/components/ProtectedRoute'
import { Login } from '@/pages/Login'
import { RegisterOrg } from '@/pages/RegisterOrg'
import { AdminLayout } from '@/layouts/AdminLayout'
import { OrgSetup } from '@/pages/admin/OrgSetup'
import { Guards } from '@/pages/admin/Guards'
import { Approvals } from '@/pages/admin/Approvals'
import { Invite } from '@/pages/admin/Invite'
import { Register } from '@/pages/Register'
import { RequestSent } from '@/pages/RequestSent'
import { AdminHome, NotFound } from '@/pages/Placeholders'
import { Dashboard } from '@/pages/admin/Dashboard'
import { useAuthStore } from '@/store/auth'
import { GuardLayout } from '@/layouts/GuardLayout'
import { Scan } from '@/pages/guard/Scan'
import { Overview } from '@/pages/guard/Overview'
import { History } from '@/pages/admin/History'
import { Reports } from '@/pages/admin/Reports'
import { PublicPass } from '@/pages/PublicPass'

function RootRedirect() {
  const role = useAuthStore((state) => state.role)
  return <Navigate to={role === 'ADMIN' ? '/admin' : role === 'GUARD' ? '/guard' : '/login'} replace />
}

const router = createBrowserRouter([
  { path: '/login', element: <Login /> },
  { path: '/register-org', element: <RegisterOrg /> },
  { path: '/register', element: <Register /> },
  { path: '/request-sent', element: <RequestSent /> },
  { path: '/pass', element: <PublicPass /> },
  {
    path: '/admin',
    element: <ProtectedRoute role="ADMIN"><AdminLayout /></ProtectedRoute>,
    children: [
      { index: true, element: <Dashboard /> },
      { path: 'approvals', element: <Approvals /> },
      { path: 'invite', element: <Invite /> },
      { path: 'organization', element: <OrgSetup /> },
      { path: 'guards', element: <Guards /> },
      { path: 'history', element: <History /> },
      { path: 'reports', element: <Reports /> },
      { path: '*', element: <AdminHome /> },
    ],
  },
  {
    path: '/guard',
    element: <ProtectedRoute role="GUARD"><GuardLayout /></ProtectedRoute>,
    children: [
      { index: true, element: <Scan /> },
      { path: 'overview', element: <Overview /> },
    ],
  },
  { path: '/', element: <RootRedirect /> },
  { path: '*', element: <NotFound /> },
])

export function AppRouter() {
  return <RouterProvider router={router} />
}
