import { useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { api, errorMessage } from '@/lib/api'

type Pass = {
  organizationName: string
  visitorName: string
  hostName: string
  purpose: string
  visitCode: string
  expectedEndTime: string
}

export function PublicPass() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const pass = useQuery({
    queryKey: ['public-pass', token],
    enabled: Boolean(token),
    queryFn: async () => (await api.get<Pass>(`/public/passes/${encodeURIComponent(token ?? '')}`)).data,
  })
  if (!token) return <Message text="This pass link is missing its token." />
  if (pass.isLoading) return <Message text="Loading your visitor pass…" />
  if (pass.isError) return <Message text={errorMessage(pass.error)} />
  const data = pass.data
  if (!data) return <Message text="Pass not found." />
  const qrUrl = `/api/public/passes/${encodeURIComponent(token)}/qr`
  return <main className="flex min-h-screen items-center justify-center bg-slate-100 p-4">
    <Card className="w-full max-w-2xl overflow-hidden border-0 shadow-xl">
      <div className="flex flex-col sm:flex-row">
        <CardContent className="order-2 flex-1 p-6 sm:order-1">
          <p className="text-sm font-semibold uppercase tracking-wide text-blue-600">{data.organizationName}</p>
          <CardHeader className="px-0"><CardTitle>Visitor pass</CardTitle></CardHeader>
          <dl className="space-y-3 text-sm">
            <div><dt className="text-muted-foreground">Visitor</dt><dd className="font-semibold">{data.visitorName}</dd></div>
            <div><dt className="text-muted-foreground">Host</dt><dd>{data.hostName}</dd></div>
            <div><dt className="text-muted-foreground">Purpose</dt><dd>{data.purpose}</dd></div>
            <div><dt className="text-muted-foreground">Visit code</dt><dd className="font-mono font-semibold">{data.visitCode}</dd></div>
            <div><dt className="text-muted-foreground">Expected end</dt><dd>{new Date(data.expectedEndTime).toLocaleString()}</dd></div>
          </dl>
        </CardContent>
        <div className="order-1 flex flex-col items-center justify-center border-b bg-white p-6 sm:order-2 sm:w-56 sm:border-b-0 sm:border-l">
          <img className="h-44 w-44" src={qrUrl} alt="Visitor pass QR code" />
          <p className="mt-3 text-center text-xs text-muted-foreground">Show this QR to the guard at the gate.</p>
        </div>
      </div>
    </Card>
  </main>
}

function Message({ text }: { text: string }) {
  return <main className="flex min-h-screen items-center justify-center p-4"><Card><CardContent className="p-6 text-center">{text}</CardContent></Card></main>
}
