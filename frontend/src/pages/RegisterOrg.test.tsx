import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { RegisterOrg } from '@/pages/RegisterOrg'

const { postMock } = vi.hoisted(() => ({ postMock: vi.fn() }))
vi.mock('@/lib/api', () => ({
  api: { post: postMock },
  errorMessage: () => 'Registration failed',
}))

function renderPage() {
  return render(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter initialEntries={['/register-org']}>
        <Routes>
          <Route path="/register-org" element={<RegisterOrg />} />
          <Route path="/login" element={<p>Login page</p>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('RegisterOrg', () => {
  beforeEach(() => postMock.mockReset())
  afterEach(() => cleanup())

  it('shows a password mismatch error', async () => {
    const user = userEvent.setup()
    renderPage()
    await user.type(screen.getByLabelText('Organization name'), 'Acme')
    await user.type(screen.getByLabelText('Admin name'), 'Admin')
    await user.type(screen.getByLabelText('Admin email'), 'admin@example.com')
    await user.type(screen.getByLabelText('Password'), 'password123')
    await user.type(screen.getByLabelText('Confirm password'), 'different')
    await user.click(screen.getByRole('button', { name: 'Register organization' }))
    expect(await screen.findByText('Passwords do not match')).toBeInTheDocument()
    expect(postMock).not.toHaveBeenCalled()
  })

  it('redirects to login after successful registration', async () => {
    postMock.mockResolvedValue({ data: { orgId: 1, slug: 'abc' } })
    const user = userEvent.setup()
    renderPage()
    await user.type(screen.getByLabelText('Organization name'), 'Acme')
    await user.type(screen.getByLabelText('Admin name'), 'Admin')
    await user.type(screen.getByLabelText('Admin email'), 'admin@example.com')
    await user.type(screen.getByLabelText('Password'), 'password123')
    await user.type(screen.getByLabelText('Confirm password'), 'password123')
    await user.click(screen.getByRole('button', { name: 'Register organization' }))
    expect(await screen.findByText('Login page')).toBeInTheDocument()
  })
})
