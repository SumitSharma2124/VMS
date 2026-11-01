import { useEffect, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { api, errorMessage } from '@/lib/api'

type Org = { name: string; address?: string; contactNumber?: string }
export function OrgSetup() {
  const client = useQueryClient()
  const [form, setForm] = useState<Org>({ name: '', address: '', contactNumber: '' })
  const [qr, setQr] = useState<string>()
  const org = useQuery({ queryKey: ['org'], queryFn: async () => (await api.get<Org>('/admin/org')).data })
  useEffect(() => { if (org.data) setForm(org.data) }, [org.data])
  useEffect(() => {
    let url: string | undefined
    api.get('/admin/registration-qr', { responseType: 'blob' }).then(({ data }) => {
      url = URL.createObjectURL(data)
      setQr(url)
    }).catch(() => toast.error('Unable to load registration QR'))
    return () => { if (url) URL.revokeObjectURL(url) }
  }, [])
  const save = useMutation({
    mutationFn: () => api.put('/admin/org', form),
    onSuccess: () => { toast.success('Organization updated'); client.invalidateQueries({ queryKey: ['org'] }) },
  })
  return <main className="grid gap-6 p-6 lg:grid-cols-2">
    <Card><CardHeader><CardTitle>Organization</CardTitle></CardHeader><CardContent className="space-y-4">
      {(['name', 'address', 'contactNumber'] as const).map((field) => <div className="space-y-2" key={field}>
        <Label htmlFor={field}>{field === 'contactNumber' ? 'Contact number' : field[0].toUpperCase() + field.slice(1)}</Label>
        <Input id={field} value={form[field] ?? ''} onChange={(event) => setForm({ ...form, [field]: event.target.value })} />
      </div>)}
      {save.isError && <p className="text-sm text-destructive">{errorMessage(save.error)}</p>}
      <Button onClick={() => save.mutate()} disabled={save.isPending}>Save changes</Button>
    </CardContent></Card>
    <Card><CardHeader><CardTitle>Registration QR</CardTitle></CardHeader><CardContent className="space-y-4">
      {qr && <img className="mx-auto max-w-xs" src={qr} alt="Organization registration QR" />}
      <div className="flex gap-2">
        <Button onClick={() => { if (qr) { const a = document.createElement('a'); a.href = qr; a.download = 'registration-qr.png'; a.click() } }}>Download</Button>
        <Button onClick={() => window.print()}>Print</Button>
      </div>
    </CardContent></Card>
  </main>
}
