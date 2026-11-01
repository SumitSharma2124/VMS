import { Scanner } from '@yudiel/react-qr-scanner'
import { useRef, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { api, errorMessage } from '@/lib/api'

type ScanResult = {
  decision: 'CHECKED_IN' | 'CHECKED_OUT' | 'DENIED' | 'INVALID'
  message: string
  visitorName?: string
  hostName?: string
  visitCode?: string
  insideCount: number
}

const colors = {
  CHECKED_IN: 'border-green-500 bg-green-50',
  CHECKED_OUT: 'border-blue-500 bg-blue-50',
  DENIED: 'border-red-500 bg-red-50',
  INVALID: 'border-red-500 bg-red-50',
}

export function Scan() {
  const [manualToken, setManualToken] = useState('')
  const [result, setResult] = useState<ScanResult | null>(null)
  const [cameraError, setCameraError] = useState(false)
  const [cameraErrorMessage, setCameraErrorMessage] = useState('')
  const lastToken = useRef<string | undefined>(undefined)
  const scan = useMutation({
    mutationFn: async (qrToken: string) => (await api.post<ScanResult>('/guard/scan', { qrToken })).data,
    onSuccess: (data) => {
      setResult(data)
      window.setTimeout(() => {
        setResult(null)
        lastToken.current = undefined
      }, 3000)
    },
    onError: () => {
      lastToken.current = undefined
    },
  })
  const submit = (token: string) => {
    const trimmed = token.trim()
    if (!trimmed || scan.isPending || lastToken.current === trimmed) return
    lastToken.current = trimmed
    scan.mutate(trimmed)
  }
  return <main className="mx-auto max-w-lg space-y-5 p-5">
    <Card>
      <CardHeader><CardTitle>Scan visitor pass</CardTitle></CardHeader>
      <CardContent className="space-y-4">
        {!cameraError && !result ? <Scanner
          constraints={{ facingMode: { ideal: 'environment' } }}
          formats={['qr_code']}
          paused={Boolean(result)}
          onScan={(codes) => codes[0]?.rawValue && submit(codes[0].rawValue)}
          onError={(error) => {
            setCameraErrorMessage(`${error.kind}: ${error.message}`)
            setCameraError(['permission-denied', 'no-camera', 'in-use', 'overconstrained',
              'insecure-context', 'unsupported', 'security', 'type-error'].includes(error.kind))
          }}
        /> : <div className="space-y-3 rounded-md border p-5 text-sm">
          <p>Camera access failed or the QR code could not be read.</p>
          {cameraErrorMessage && <p className="text-destructive">{cameraErrorMessage}</p>}
          {!window.isSecureContext && <p className="text-destructive">Open this page using HTTPS. Camera access is blocked on plain HTTP.</p>}
          <Button onClick={() => { setCameraErrorMessage(''); setCameraError(false) }}>Retry camera</Button>
        </div>}
        <form className="flex gap-2" onSubmit={(event) => { event.preventDefault(); submit(manualToken) }}>
          <Input value={manualToken} maxLength={64} placeholder="Paste QR token" onChange={(event) => setManualToken(event.target.value)} />
          <Button type="submit" disabled={scan.isPending}>Submit</Button>
        </form>
        {scan.isError && <p className="text-sm text-destructive">{errorMessage(scan.error)}</p>}
      </CardContent>
    </Card>
    {result && <button className={`w-full rounded-xl border-2 p-6 text-left ${colors[result.decision]}`} onClick={() => { setResult(null); lastToken.current = undefined }}>
      <p className="text-xl font-semibold">{result.message}</p>
      {result.visitorName && <p className="mt-3">Visitor: {result.visitorName}</p>}
      {result.hostName && <p>Host: {result.hostName}</p>}
      {result.visitCode && <p>Visit code: {result.visitCode}</p>}
      <p className="mt-3 text-sm">Inside now: {result.insideCount}</p>
    </button>}
  </main>
}
