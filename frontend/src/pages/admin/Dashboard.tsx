import { useEffect, useMemo, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { api, errorMessage } from '@/lib/api'
import { useAuthStore } from '@/store/auth'
import { useStomp } from '@/hooks/useStomp'

type ActiveVisitor = {
  visitId: number
  visitCode: string
  visitorName: string
  hostName: string
  checkInTime: string
  expectedEndTime: string
  status: 'CHECKED_IN' | 'OVERDUE'
}

type ActiveEvent = { type: 'ACTIVE'; count: number; list: ActiveVisitor[] }
type NotificationEvent = { type: 'NEW_REQUEST' | 'OVERDUE'; visitorName?: string; hostName?: string }

export function Dashboard() {
  const orgId = useAuthStore((state) => state.orgId)
  const client = useQueryClient()
  const { connected, subscribe } = useStomp()
  const [overdueMessage, setOverdueMessage] = useState<string | null>(null)
  const active = useQuery({
    queryKey: ['active-visitors'],
    queryFn: async () => (await api.get<ActiveVisitor[]>('/admin/active')).data,
    refetchInterval: 60000,
  })
  const approvals = useQuery({
    queryKey: ['approvals'],
    queryFn: async () => (await api.get<{ content: unknown[] }>('/admin/visits', { params: { status: 'REQUESTED' } })).data,
    refetchInterval: 60000,
  })

  useEffect(() => {
    if (!orgId) return
    return subscribe(`/topic/org/${orgId}/active`, (message) => {
      const event = JSON.parse(message.body) as ActiveEvent
      if (event.type === 'ACTIVE') client.setQueryData(['active-visitors'], event.list)
    })
  }, [client, connected, orgId, subscribe])

  useEffect(() => {
    if (!orgId) return
    return subscribe(`/topic/org/${orgId}/notifications`, (message) => {
      const event = JSON.parse(message.body) as NotificationEvent
      if (event.type === 'NEW_REQUEST') {
        toast.info(`New visit request from ${event.visitorName ?? 'visitor'}`)
        void client.invalidateQueries({ queryKey: ['approvals'] })
      } else if (event.type === 'OVERDUE') {
        setOverdueMessage(`${event.visitorName ?? 'A visitor'} is overdue`)
      }
    })
  }, [client, connected, orgId, subscribe])

  const overdue = useMemo(() => (active.data ?? []).filter((visitor) => visitor.status === 'OVERDUE').length, [active.data])
  return <main className="space-y-6 p-6">
    <div className="flex items-center justify-between">
      <h1 className="text-2xl font-semibold">Dashboard</h1>
      <span className="text-sm text-muted-foreground">{connected ? 'Live' : 'Reconnecting…'}</span>
    </div>
    {overdueMessage && <button className="w-full rounded-md bg-destructive p-3 text-left text-destructive-foreground" onClick={() => setOverdueMessage(null)}>{overdueMessage}</button>}
    {active.isError && <p className="text-sm text-destructive">{errorMessage(active.error)}</p>}
    <div className="grid gap-4 md:grid-cols-3">
      <Card><CardHeader><CardTitle>Inside now</CardTitle></CardHeader><CardContent className="text-3xl font-bold">{active.data?.length ?? 0}</CardContent></Card>
      <Card><CardHeader><CardTitle>Overdue</CardTitle></CardHeader><CardContent className="text-3xl font-bold">{overdue}</CardContent></Card>
      <Card><CardHeader><CardTitle>Pending requests</CardTitle></CardHeader><CardContent className="text-3xl font-bold">{approvals.data?.content.length ?? 0}</CardContent></Card>
    </div>
    <Card><CardHeader><CardTitle>Active visitors</CardTitle></CardHeader><CardContent>
      {!active.data?.length && <p className="text-sm text-muted-foreground">No visitors inside</p>}
      {!!active.data?.length && <div className="overflow-x-auto"><table className="w-full text-left text-sm"><thead><tr className="border-b"><th className="p-2">Visitor</th><th className="p-2">Host</th><th className="p-2">Visit</th><th className="p-2">Checked in</th><th className="p-2">Status</th></tr></thead><tbody>
        {active.data.map((visitor) => <tr className="border-b" key={visitor.visitId}><td className="p-2">{visitor.visitorName}</td><td className="p-2">{visitor.hostName}</td><td className="p-2">{visitor.visitCode}</td><td className="p-2">{new Date(visitor.checkInTime).toLocaleString()}</td><td className="p-2">{visitor.status === 'OVERDUE' ? <span className="rounded bg-destructive px-2 py-1 text-xs text-destructive-foreground">Overdue</span> : 'Inside'}</td></tr>)}
      </tbody></table></div>}
    </CardContent></Card>
  </main>
}
