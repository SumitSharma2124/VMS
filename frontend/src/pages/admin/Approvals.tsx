import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { api, errorMessage } from '@/lib/api'

type Visit = {
  visitId: number
  visitorName: string
  visitorEmail: string
  contact: string
  hostName: string
  purpose: string
  requestedAt: string
}

export function Approvals() {
  const client = useQueryClient()
  const visits = useQuery({
    queryKey: ['approvals'],
    queryFn: async () => (await api.get<{ content: Visit[] }>('/admin/visits', { params: { status: 'REQUESTED' } })).data,
    refetchInterval: 15000,
  })
  const action = useMutation({
    mutationFn: ({ id, type, body }: { id: number; type: 'approve' | 'reject'; body: object }) =>
      api.post(`/admin/visits/${id}/${type}`, body),
    onSuccess: () => {
      client.invalidateQueries({ queryKey: ['approvals'] })
      toast.success('Request updated')
    },
    onError: (error) => {
      toast.error(errorMessage(error))
      client.invalidateQueries({ queryKey: ['approvals'] })
    },
  })
  const content = visits.data?.content ?? []
  return <main className="p-6"><Card><CardHeader><CardTitle>Approvals</CardTitle></CardHeader><CardContent>
    {visits.isError && <p className="text-sm text-destructive">{errorMessage(visits.error)}</p>}
    {!visits.isLoading && content.length === 0 && <p className="text-sm text-muted-foreground">No pending requests</p>}
    {content.length > 0 && <div className="overflow-x-auto"><table className="w-full text-left text-sm"><thead><tr className="border-b">
      <th className="p-2">Visitor</th><th className="p-2">Contact</th><th className="p-2">Host</th><th className="p-2">Purpose</th><th className="p-2">Requested</th><th className="p-2">Actions</th>
    </tr></thead><tbody>{content.map((visit) => <tr className="border-b" key={visit.visitId}>
      <td className="p-2">{visit.visitorName}<br /><span className="text-muted-foreground">{visit.visitorEmail}</span></td>
      <td className="p-2">{visit.contact}</td><td className="p-2">{visit.hostName}</td><td className="p-2">{visit.purpose}</td><td className="p-2">{new Date(visit.requestedAt).toLocaleString()}</td>
      <td className="space-x-2 p-2"><Button disabled={action.isPending} onClick={() => action.mutate({ id: visit.visitId, type: 'approve', body: {} })}>Approve</Button><Button disabled={action.isPending} onClick={() => action.mutate({ id: visit.visitId, type: 'reject', body: {} })}>Reject</Button>
      </td>
    </tr>)}</tbody></table></div>}
  </CardContent></Card></main>
}
