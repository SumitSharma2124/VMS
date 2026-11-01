import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { api, errorMessage } from '@/lib/api'

const initialForm = { fullName: '', email: '', contactNumber: '', hostName: '', purpose: '', expectedStartTime: '', expectedEndTime: '' }
export function Invite() {
  const [form, setForm] = useState(initialForm)
  const create = useMutation({
    mutationFn: () => api.post('/admin/invitations', {
      ...form,
      expectedStartTime: form.expectedStartTime ? new Date(form.expectedStartTime).toISOString() : null,
      expectedEndTime: form.expectedEndTime ? new Date(form.expectedEndTime).toISOString() : null,
    }),
    onSuccess: () => { setForm(initialForm); toast.success('Invitation created') },
  })
  const update = (field: keyof typeof form, value: string) => setForm((current) => ({ ...current, [field]: value }))
  return <main className="p-6"><Card className="max-w-2xl"><CardHeader><CardTitle>Invite visitor</CardTitle></CardHeader><CardContent>
    <form className="grid gap-3 sm:grid-cols-2" onSubmit={(event) => { event.preventDefault(); create.mutate() }}>
      <Input required placeholder="Full name" value={form.fullName} onChange={(event) => update('fullName', event.target.value)} />
      <Input required type="email" placeholder="Email" value={form.email} onChange={(event) => update('email', event.target.value)} />
      <Input required placeholder="Contact number" value={form.contactNumber} onChange={(event) => update('contactNumber', event.target.value)} />
      <Input required placeholder="Host name" value={form.hostName} onChange={(event) => update('hostName', event.target.value)} />
      <Input required className="sm:col-span-2" placeholder="Purpose" value={form.purpose} onChange={(event) => update('purpose', event.target.value)} />
      <Input required type="datetime-local" value={form.expectedStartTime} onChange={(event) => update('expectedStartTime', event.target.value)} />
      <Input required type="datetime-local" value={form.expectedEndTime} onChange={(event) => update('expectedEndTime', event.target.value)} />
      {create.isError && <p className="text-sm text-destructive sm:col-span-2">{errorMessage(create.error)}</p>}
      <Button className="sm:col-span-2" type="submit" disabled={create.isPending}>Create invitation</Button>
    </form>
  </CardContent></Card></main>
}
