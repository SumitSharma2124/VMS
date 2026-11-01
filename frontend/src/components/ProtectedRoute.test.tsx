import { render, screen } from '@testing-library/react'
import { describe, expect, it, beforeEach } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from '@/components/ProtectedRoute'
import { useAuthStore } from '@/store/auth'

function renderRoute() {
  return render(
    <MemoryRouter initialEntries={['/admin']}>
      <Routes>
        <Route path="/login" element={<p>Login page</p>} />
        <Route path="/admin" element={<ProtectedRoute role="ADMIN"><p>Admin page</p></ProtectedRoute>} />
        <Route path="/guard" element={<ProtectedRoute role="GUARD"><p>Guard page</p></ProtectedRoute>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('ProtectedRoute', () => {
  beforeEach(() => useAuthStore.getState().logout())

  it('redirects an unauthenticated user to login', () => {
    renderRoute()
    expect(screen.getByText('Login page')).toBeInTheDocument()
  })

  it('redirects a guard away from an admin route', () => {
    useAuthStore.getState().setSession({
      token: 'token',
      role: 'GUARD',
      orgId: 1,
      name: 'Guard',
    })
    renderRoute()
    expect(screen.getByText('Guard page')).toBeInTheDocument()
  })
})
