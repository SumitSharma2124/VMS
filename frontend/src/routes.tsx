import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom'
import { ProtectedRoute } from '@/components/ProtectedRoute'
import { Login } from '@/pages/Login'
import { RegisterOrg } from '@/pages/RegisterOrg'
import { AdminLayout } from '@/layouts/AdminLayout'
import { OrgSetup } from '@/pages/admin/OrgSetup'
import { Guards } from '@/pages/admin/Guards'
import { AdminHome, GuardHome, NotFound } from '@/pages/Placeholders'
import { useAuthStore } from '@/store/auth'

function RootRedirect() {
  const role = useAuthStore((state) => state.role)
  return <Navigate to={role === 'ADMIN' ? '/admin' : role === 'GUARD' ? '/guard' : '/login'} replace />
}

const router = createBrowserRouter([
  { path: '/login', element: <Login /> },
  { path: '/register-org', element: <RegisterOrg /> },
  {
    path: '/admin',
    element: <ProtectedRoute role="ADMIN"><AdminLayout /></ProtectedRoute>,
    children: [
      { index: true, element: <AdminHome /> },
      { path: 'organization', element: <OrgSetup /> },
      { path: 'guards', element: <Guards /> },
      { path: '*', element: <AdminHome /> },
    ],
  },
  {
    path: '/guard/*',
    element: <ProtectedRoute role="GUARD"><GuardHome /></ProtectedRoute>,
  },
  { path: '/', element: <RootRedirect /> },
  { path: '*', element: <NotFound /> },
])

export function AppRouter() {
  return <RouterProvider router={router} />
}
