import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { api, errorMessage } from '@/lib/api'

type Guard = { id: number; fullName: string; email: string; phone?: string; status: 'ACTIVE' | 'INACTIVE' }
export function Guards() {
  const client = useQueryClient()
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState({ fullName: '', email: '', password: '', phone: '' })
  const guards = useQuery({ queryKey: ['guards'], queryFn: async () => (await api.get<Guard[]>('/admin/guards')).data })
  const status = useMutation({
    mutationFn: ({ id, active }: { id: number; active: boolean }) => api.patch(`/admin/guards/${id}/status`, { active }),
    onSuccess: () => client.invalidateQueries({ queryKey: ['guards'] }),
  })
  const create = useMutation({
    mutationFn: () => api.post('/admin/guards', form),
    onSuccess: () => { setOpen(false); setForm({ fullName: '', email: '', password: '', phone: '' }); client.invalidateQueries({ queryKey: ['guards'] }); toast.success('Guard created. Share credentials securely.') },
  })
  return <main className="p-6"><Card><CardHeader className="flex-row items-center justify-between"><CardTitle>Guards</CardTitle><Button onClick={() => setOpen(!open)}>Add guard</Button></CardHeader>
    <CardContent>
      {open && <form className="mb-6 grid gap-3 sm:grid-cols-2" onSubmit={(event) => { event.preventDefault(); create.mutate() }}>
        {Object.keys(form).map((field) => <Input key={field} required={field !== 'phone'} type={field === 'password' ? 'password' : field === 'email' ? 'email' : 'text'} placeholder={field} value={form[field as keyof typeof form]} onChange={(event) => setForm({ ...form, [field]: event.target.value })} />)}
        <Button type="submit" disabled={create.isPending}>Create guard</Button>
        {create.isError && <p className="text-sm text-destructive">{errorMessage(create.error)}</p>}
      </form>}
      <p className="mb-3 text-sm text-muted-foreground">After creation, copy credentials and share them out of band.</p>
      <div className="overflow-x-auto"><table className="w-full text-left text-sm"><thead><tr className="border-b"><th className="p-2">Name</th><th className="p-2">Email</th><th className="p-2">Status</th><th className="p-2">Active</th></tr></thead><tbody>
        {guards.data?.map((guard) => <tr className="border-b" key={guard.id}><td className="p-2">{guard.fullName}</td><td className="p-2">{guard.email}</td><td className="p-2">{guard.status}</td><td className="p-2"><input type="checkbox" checked={guard.status === 'ACTIVE'} onChange={(event) => status.mutate({ id: guard.id, active: event.target.checked })} /></td></tr>)}
      </tbody></table></div>
    </CardContent>
  </Card></main>
}
