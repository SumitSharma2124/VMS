import { useQuery } from '@tanstack/react-query'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { api, errorMessage } from '@/lib/api'

type OverviewData = {
  insideCount: number
  overdue: { visitorName: string; hostName: string; expectedEndTime: string }[]
}

export function Overview() {
  const overview = useQuery({
    queryKey: ['guard-overview'],
    queryFn: async () => (await api.get<OverviewData>('/guard/overview')).data,
    refetchInterval: 15000,
  })
  return <main className="mx-auto max-w-lg space-y-5 p-5">
    <Card><CardHeader><CardTitle>Inside now</CardTitle></CardHeader><CardContent><p className="text-4xl font-bold">{overview.data?.insideCount ?? 0}</p></CardContent></Card>
    <Card><CardHeader><CardTitle>Overdue</CardTitle></CardHeader><CardContent>
      {overview.isError && <p className="text-sm text-destructive">{errorMessage(overview.error)}</p>}
      {!overview.isLoading && !overview.data?.overdue.length && <p className="text-sm text-muted-foreground">No overdue visitors</p>}
      <ul className="space-y-3">{overview.data?.overdue.map((visit) => <li className="border-b pb-2 text-sm" key={`${visit.visitorName}-${visit.expectedEndTime}`}><p className="font-medium">{visit.visitorName}</p><p>{visit.hostName}</p><p className="text-muted-foreground">Expected out: {new Date(visit.expectedEndTime).toLocaleString()}</p></li>)}</ul>
    </CardContent></Card>
  </main>
}
