import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/auth'
import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api'

const items = [
  ['Dashboard', '/admin'],
  ['Approvals', '/admin/approvals'],
  ['Invite', '/admin/invite'],
  ['Guards', '/admin/guards'],
  ['History', '/admin/history'],
  ['Reports', '/admin/reports'],
  ['Organization', '/admin/organization'],
] as const

export function AdminLayout() {
  const navigate = useNavigate()
  const { name, logout } = useAuthStore()
  const unread = useQuery({
    queryKey: ['notifications-unread'],
    queryFn: async () => (await api.get<{ count: number }>('/admin/notifications/unread-count')).data.count,
    refetchInterval: 60000,
  })
  return (
    <div className="flex min-h-screen bg-muted/30">
      <aside className="w-60 border-r bg-background p-4">
        <h1 className="mb-6 text-xl font-bold">VMS</h1>
        <nav className="space-y-1">
          {items.map(([label, path]) => (
            <NavLink
              key={path}
              to={path}
              end={path === '/admin'}
              className={({ isActive }) =>
                `block rounded-md px-3 py-2 text-sm ${isActive ? 'bg-primary text-primary-foreground' : 'hover:bg-muted'}`
              }
            >
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <section className="min-w-0 flex-1">
        <header className="flex h-16 items-center justify-between border-b bg-background px-6">
          <span className="text-sm text-muted-foreground">Organization admin</span>
          <div className="flex items-center gap-4">
            <span aria-label="Unread notifications" className="text-sm">Notifications {unread.data ? `(${unread.data})` : ''}</span>
            <span className="text-sm">{name}</span>
            <button
              className="text-sm text-muted-foreground hover:text-foreground"
              onClick={() => { logout(); navigate('/login', { replace: true }) }}
            >
              Log out
            </button>
          </div>
        </header>
        <Outlet />
      </section>
    </div>
  )
}
