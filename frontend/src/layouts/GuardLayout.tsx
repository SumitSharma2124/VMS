import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/auth'

export function GuardLayout() {
  const navigate = useNavigate()
  const { logout } = useAuthStore()
  return <div className="min-h-screen bg-muted/30 pb-20">
    <header className="flex h-16 items-center justify-between border-b bg-background px-5">
      <span className="font-semibold">VMS Gate</span>
      <button className="text-sm text-muted-foreground" onClick={() => { logout(); navigate('/login', { replace: true }) }}>Log out</button>
    </header>
    <Outlet />
    <nav className="fixed inset-x-0 bottom-0 flex h-16 border-t bg-background">
      <NavLink className="flex flex-1 items-center justify-center text-sm" to="/guard">Scan</NavLink>
      <NavLink className="flex flex-1 items-center justify-center text-sm" to="/guard/overview">Overview</NavLink>
    </nav>
  </div>
}
