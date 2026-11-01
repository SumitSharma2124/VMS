import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { api, errorMessage } from '@/lib/api'

type Report = { totalVisitors: number; byStatus: Record<string, number>; peakHours: number[]; dailyTrend: { date: string; count: number }[] }
const dateValue = (date: Date) => date.toISOString().slice(0, 10)

export function Reports() {
  const today = new Date()
  const [to, setTo] = useState(dateValue(today))
  const [from, setFrom] = useState(dateValue(new Date(today.getTime() - 29 * 86400000)))
  const report = useQuery({
    queryKey: ['reports', from, to],
    queryFn: async () => (await api.get<Report>('/admin/reports/summary', { params: { from, to } })).data,
  })
  const max = Math.max(...(report.data?.peakHours ?? [0]), 1)
  return <main className="space-y-5 p-6">
    <div className="flex flex-wrap items-end justify-between gap-3"><h1 className="text-2xl font-semibold">Reports</h1><div className="flex gap-2"><Input type="date" value={from} onChange={(event) => setFrom(event.target.value)} /><Input type="date" value={to} onChange={(event) => setTo(event.target.value)} /></div></div>
    {report.isError && <p className="text-sm text-destructive">{errorMessage(report.error)}</p>}
    <div className="grid gap-4 md:grid-cols-2"><Card><CardHeader><CardTitle>Total visitors</CardTitle></CardHeader><CardContent className="text-3xl font-bold">{report.data?.totalVisitors ?? 0}</CardContent></Card><Card><CardHeader><CardTitle>Overdue</CardTitle></CardHeader><CardContent className="text-3xl font-bold">{report.data?.byStatus.OVERDUE ?? 0}</CardContent></Card></div>
    <Card><CardHeader><CardTitle>Peak hours</CardTitle></CardHeader><CardContent><div className="flex h-56 items-end gap-1">{(report.data?.peakHours ?? Array(24).fill(0)).map((count, hour) => <div className="flex flex-1 flex-col items-center gap-1" key={hour}><div className="w-full bg-primary" style={{ height: `${Math.max((count / max) * 180, count ? 4 : 0)}px` }} title={`${hour}:00 — ${count}`} /><span className="text-[10px]">{hour}</span></div>)}</div></CardContent></Card>
    <Card><CardHeader><CardTitle>Daily trend</CardTitle></CardHeader><CardContent><div className="flex h-40 items-end gap-1">{(report.data?.dailyTrend ?? []).map((day) => { const maxDay = Math.max(...(report.data?.dailyTrend.map((item) => item.count) ?? [1]), 1); return <div className="flex flex-1 flex-col items-center gap-1" key={day.date}><div className="w-full bg-secondary" style={{ height: `${Math.max((day.count / maxDay) * 130, day.count ? 4 : 0)}px` }} title={`${day.date} — ${day.count}`} /><span className="max-w-8 truncate text-[9px]">{day.date.slice(5)}</span></div> })}</div></CardContent></Card>
  </main>
}
