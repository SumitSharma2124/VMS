import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { Login } from '@/pages/Login'
import { useAuthStore } from '@/store/auth'

const { postMock } = vi.hoisted(() => ({ postMock: vi.fn() }))

vi.mock('@/lib/api', () => ({
  api: { post: postMock },
  errorMessage: (error: { response?: { data?: { message?: string } } }) =>
    error.response?.data?.message ?? 'Something went wrong. Please try again.',
}))

function renderLogin() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/admin" element={<p>Admin page</p>} />
          <Route path="/guard" element={<p>Guard page</p>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('Login', () => {
  afterEach(() => cleanup())

  beforeEach(() => {
    postMock.mockReset()
    useAuthStore.getState().logout()
  })

  it('shows the server error for a wrong password', async () => {
    postMock.mockRejectedValue({ response: { data: { message: 'Invalid credentials' } } })
    const user = userEvent.setup()
    render(
      <QueryClientProvider client={new QueryClient()}>
        <MemoryRouter initialEntries={['/login']}>
          <Routes>
            <Route path="/login" element={<Login />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>,
    )

    await user.type(screen.getByLabelText('Email'), 'admin@example.com')
    await user.type(screen.getByLabelText('Password'), 'wrong')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findByText('Invalid credentials')).toBeInTheDocument()
  })

  it.each([
    ['ADMIN', '/admin', 'Admin page'],
    ['GUARD', '/guard', 'Guard page'],
  ] as const)('redirects a %s after successful login', async (role, _path, page) => {
    postMock.mockResolvedValue({
      data: { token: 'token', role, orgId: 1, name: 'User' },
    })
    const user = userEvent.setup()
    renderLogin()

    await user.type(screen.getByLabelText('Email'), 'user@example.com')
    await user.type(screen.getByLabelText('Password'), 'password')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findByText(page)).toBeInTheDocument()
  })
})
