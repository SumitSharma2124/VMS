import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { Register } from '@/pages/Register'

const { getMock, postMock } = vi.hoisted(() => ({ getMock: vi.fn(), postMock: vi.fn() }))
vi.mock('@/lib/api', () => ({
  api: { get: getMock, post: postMock },
  errorMessage: () => 'Request failed',
}))

function renderPage() {
  return render(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter initialEntries={['/register?org=abc']}>
        <Routes>
          <Route path="/register" element={<Register />} />
          <Route path="/request-sent" element={<p>Request sent page</p>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('Register', () => {
  beforeEach(() => {
    getMock.mockReset()
    postMock.mockReset()
    getMock.mockImplementation((url: string) => {
      if (url.includes('/public/orgs/')) return Promise.resolve({ data: { name: 'Acme' } })
      return Promise.resolve({ data: { found: false } })
    })
  })
  afterEach(() => cleanup())

  it('blocks an empty email', async () => {
    const user = userEvent.setup()
    renderPage()
    await screen.findByText('Acme')
    await user.click(screen.getByRole('button', { name: 'Continue' }))
    expect(await screen.findByText('Invalid email address')).toBeInTheDocument()
    expect(getMock).toHaveBeenCalledTimes(1)
  })

  it('prefills a returning visitor', async () => {
    getMock.mockImplementation((url: string) => {
      if (url.includes('/public/orgs/')) return Promise.resolve({ data: { name: 'Acme' } })
      return Promise.resolve({ data: { found: true, fullName: 'Returning Visitor', contactNumber: '1234567890' } })
    })
    const user = userEvent.setup()
    renderPage()
    await screen.findByText('Acme')
    await user.type(screen.getByLabelText('Email'), 'visitor@example.com')
    await user.click(screen.getByRole('button', { name: 'Continue' }))
    expect(await screen.findByDisplayValue('Returning Visitor')).toBeInTheDocument()
    expect(screen.getByDisplayValue('1234567890')).toBeInTheDocument()
  })

  it('shows an unknown organization message', async () => {
    getMock.mockRejectedValue(new Error('not found'))
    renderPage()
    expect(await screen.findByText('Organization not found')).toBeInTheDocument()
  })
})
