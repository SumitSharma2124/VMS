import { useEffect, useMemo, useState } from 'react'
import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { api, errorMessage } from '@/lib/api'
import { useSearchParams } from 'react-router-dom'

type Row = { visitCode: string; visitorName: string; hostName: string; purpose: string; status: string; requestedAt: string; checkInTime?: string; checkOutTime?: string }
type Page = { content: Row[]; totalPages: number; totalElements: number; number: number }

export function History() {
  const [params, setParams] = useSearchParams()
  const [name, setName] = useState(params.get('name') ?? '')
  const [host, setHost] = useState(params.get('host') ?? '')
  const page = Number(params.get('page') ?? 0)
  const query = useQuery({
    queryKey: ['history', params.toString()],
    queryFn: async () => (await api.get<Page>('/admin/history', { params: Object.fromEntries(params) })).data,
    placeholderData: keepPreviousData,
  })
  useEffect(() => {
    const timer = window.setTimeout(() => {
      const next = new URLSearchParams(params)
      next.set('page', '0')
      name ? next.set('name', name) : next.delete('name')
      host ? next.set('host', host) : next.delete('host')
      setParams(next)
    }, 400)
    return () => window.clearTimeout(timer)
  }, [name, host])
  const rows = useMemo(() => query.data?.content ?? [], [query.data])
  return <main className="space-y-5 p-6">
    <h1 className="text-2xl font-semibold">Visit history</h1>
    <Card><CardContent className="grid gap-3 p-6 md:grid-cols-5">
      <Input placeholder="Visitor name" value={name} onChange={(event) => setName(event.target.value)} />
      <Input placeholder="Host" value={host} onChange={(event) => setHost(event.target.value)} />
      <select className="rounded-md border bg-background px-3" value={params.get('status') ?? ''} onChange={(event) => { const next = new URLSearchParams(params); event.target.value ? next.set('status', event.target.value) : next.delete('status'); next.set('page', '0'); setParams(next) }}>
        <option value="">All statuses</option>{['REQUESTED', 'APPROVED', 'REJECTED', 'CHECKED_IN', 'OVERDUE', 'CHECKED_OUT'].map((status) => <option key={status}>{status}</option>)}
      </select>
      <Input type="date" value={params.get('from') ?? ''} onChange={(event) => { const next = new URLSearchParams(params); event.target.value ? next.set('from', event.target.value) : next.delete('from'); next.set('page', '0'); setParams(next) }} />
      <Input type="date" value={params.get('to') ?? ''} onChange={(event) => { const next = new URLSearchParams(params); event.target.value ? next.set('to', event.target.value) : next.delete('to'); next.set('page', '0'); setParams(next) }} />
    </CardContent></Card>
    <Card><CardHeader><CardTitle>Visits</CardTitle></CardHeader><CardContent>
      {query.isError && <p className="text-sm text-destructive">{errorMessage(query.error)}</p>}
      {!query.isLoading && rows.length === 0 && <p className="text-sm text-muted-foreground">No visits match these filters</p>}
      {rows.length > 0 && <div className="overflow-x-auto"><table className="w-full text-left text-sm"><thead><tr className="border-b">{['Code', 'Visitor', 'Host', 'Purpose', 'Status', 'Requested', 'Check in', 'Check out'].map((heading) => <th className="p-2" key={heading}>{heading}</th>)}</tr></thead><tbody>
        {rows.map((row) => <tr className="border-b" key={`${row.visitCode}-${row.requestedAt}`}><td className="p-2">{row.visitCode}</td><td className="p-2">{row.visitorName}</td><td className="p-2">{row.hostName}</td><td className="p-2">{row.purpose}</td><td className="p-2"><span className="rounded border px-2 py-1 text-xs">{row.status}</span></td><td className="p-2">{new Date(row.requestedAt).toLocaleString()}</td><td className="p-2">{row.checkInTime ? new Date(row.checkInTime).toLocaleString() : '-'}</td><td className="p-2">{row.checkOutTime ? new Date(row.checkOutTime).toLocaleString() : '-'}</td></tr>)}
      </tbody></table></div>}
      <div className="mt-4 flex items-center justify-between"><span className="text-sm text-muted-foreground">Page {page + 1} of {query.data?.totalPages ?? 1}</span><div className="space-x-2"><button className="rounded border px-3 py-1" disabled={page === 0} onClick={() => { const next = new URLSearchParams(params); next.set('page', String(page - 1)); setParams(next) }}>Previous</button><button className="rounded border px-3 py-1" disabled={page + 1 >= (query.data?.totalPages ?? 1)} onClick={() => { const next = new URLSearchParams(params); next.set('page', String(page + 1)); setParams(next) }}>Next</button></div></div>
    </CardContent></Card>
  </main>
}
