import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

export function RequestSent() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-muted/40 px-4">
      <Card className="w-full max-w-md text-center">
        <CardHeader><CardTitle>Request sent</CardTitle></CardHeader>
        <CardContent><p className="text-muted-foreground">You&apos;ll get an email once it&apos;s approved.</p></CardContent>
      </Card>
    </main>
  )
}
