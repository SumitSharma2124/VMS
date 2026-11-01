export type Role = 'ADMIN' | 'GUARD'

export type LoginResponse = {
  token: string
  role: Role
  orgId: number
  name: string
}

export type AuthState = {
  token: string | null
  role: Role | null
  orgId: number | null
  name: string | null
}
