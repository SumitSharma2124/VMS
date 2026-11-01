import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { AuthState, LoginResponse } from '@/types/auth'

type AuthActions = {
  setSession: (session: LoginResponse) => void
  logout: () => void
}

const initialState: AuthState = {
  token: null,
  role: null,
  orgId: null,
  name: null,
}

export const useAuthStore = create<AuthState & AuthActions>()(
  persist(
    (set) => ({
      ...initialState,
      setSession: (session) => set(session),
      logout: () => set(initialState),
    }),
    { name: 'vms-auth' },
  ),
)
