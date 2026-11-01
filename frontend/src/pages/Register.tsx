import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { api, errorMessage } from '@/lib/api'

const emailSchema = z.object({ email: z.email() })
const visitSchema = z.object({
  email: z.email(),
  fullName: z.string().min(1, 'Name is required').max(100),
  contactNumber: z.string().regex(/^\+?\d{7,15}$/, 'Enter a valid phone number'),
  hostName: z.string().min(1, 'Host name is required'),
  purpose: z.string().min(1, 'Purpose is required').max(255),
  expectedStartTime: z.string().min(1, 'Start time is required'),
  expectedEndTime: z.string().min(1, 'End time is required'),
}).superRefine((values, context) => {
  if (values.expectedStartTime && values.expectedEndTime
      && new Date(values.expectedEndTime) <= new Date(values.expectedStartTime)) {
    context.addIssue({
      code: 'custom',
      path: ['expectedEndTime'],
      message: 'End time must be after the start time',
    })
  }
})

function localDateTimeValue(date: Date) {
  const offset = date.getTimezoneOffset() * 60000
  return new Date(date.getTime() - offset).toISOString().slice(0, 16)
}

type VisitValues = z.infer<typeof visitSchema>
type OrgInfo = { name: string }
type Lookup = { found: boolean; fullName?: string; contactNumber?: string }

export function Register() {
  const [params] = useSearchParams()
  const orgSlug = params.get('org') ?? ''
  const navigate = useNavigate()
  const [step, setStep] = useState<1 | 2>(1)
  const emailForm = useForm<z.infer<typeof emailSchema>>({
    resolver: zodResolver(emailSchema),
    defaultValues: { email: '' },
  })
  const form = useForm<VisitValues>({
    resolver: zodResolver(visitSchema),
    defaultValues: { email: '', fullName: '', contactNumber: '', hostName: '', purpose: '', expectedStartTime: '', expectedEndTime: '' },
  })
  const expectedStartTime = form.watch('expectedStartTime')
  const org = useQuery({
    queryKey: ['public-org', orgSlug],
    queryFn: async () => (await api.get<OrgInfo>(`/public/orgs/${encodeURIComponent(orgSlug)}`)).data,
    enabled: Boolean(orgSlug),
    retry: false,
  })
  const lookup = useMutation({
    mutationFn: async (email: string) =>
      (await api.get<Lookup>('/public/visitors/lookup', { params: { org: orgSlug, email } })).data,
    onSuccess: (data, email) => {
      form.setValue('email', email)
      if (data.found) {
        form.setValue('fullName', data.fullName ?? '')
        form.setValue('contactNumber', data.contactNumber ?? '')
      }
      setStep(2)
    },
  })
  const create = useMutation({
    mutationFn: (values: VisitValues) => api.post('/public/visits', {
      ...values,
      orgSlug,
      expectedStartTime: new Date(values.expectedStartTime).toISOString(),
      expectedEndTime: new Date(values.expectedEndTime).toISOString(),
    }),
    onSuccess: () => navigate('/request-sent', { replace: true }),
  })
  useEffect(() => {
    const fieldErrors = (create.error as { response?: { data?: { fieldErrors?: Record<string, string> } } })?.response?.data?.fieldErrors
    if (fieldErrors) {
      Object.entries(fieldErrors).forEach(([field, message]) => {
        form.setError(field as keyof VisitValues, { message })
      })
    }
  }, [create.error, form])
  useEffect(() => {
    if (org.data?.name && !form.getValues('hostName')) {
      form.setValue('hostName', org.data.name)
    }
  }, [form, org.data?.name])
  useEffect(() => {
    if (step === 2 && !form.getValues('expectedStartTime')) {
      const start = new Date(Date.now() + 60 * 60 * 1000)
      const end = new Date(start.getTime() + 2 * 60 * 60 * 1000)
      form.setValue('expectedStartTime', localDateTimeValue(start))
      form.setValue('expectedEndTime', localDateTimeValue(end))
    }
  }, [form, step])

  if (!orgSlug || org.isError) {
    return <MessageCard message="Organization not found" />
  }
  if (org.isLoading) {
    return <MessageCard message="Loading organization..." />
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-muted/40 px-4 py-8">
      <Card className="w-full max-w-lg">
        <CardHeader>
          <CardTitle>{org.data?.name ?? 'Visitor registration'}</CardTitle>
        </CardHeader>
        <CardContent>
          {step === 1 ? (
            <form className="space-y-4" onSubmit={emailForm.handleSubmit(({ email }) => lookup.mutate(email))}>
              <p className="text-sm text-muted-foreground">Enter your email to continue.</p>
              <div className="space-y-2">
                <Label htmlFor="lookup-email">Email</Label>
                <Input id="lookup-email" type="email" autoComplete="email" {...emailForm.register('email')} />
                {emailForm.formState.errors.email && <p className="text-sm text-destructive">{emailForm.formState.errors.email.message}</p>}
              </div>
              {lookup.isError && <p className="text-sm text-destructive">{errorMessage(lookup.error)}</p>}
              <Button className="w-full" type="submit" disabled={lookup.isPending}>Continue</Button>
            </form>
          ) : (
            <form className="space-y-4" onSubmit={form.handleSubmit((values) => create.mutate(values))}>
              {(['fullName', 'contactNumber', 'hostName', 'purpose'] as const).map((field) => (
                <div className="space-y-2" key={field}>
                  <Label htmlFor={field}>{field === 'fullName' ? 'Full name' : field === 'contactNumber' ? 'Contact number' : field === 'hostName' ? 'Host name' : 'Purpose'}</Label>
                  {field === 'purpose' ? (
                    <textarea id={field} className="min-h-24 w-full rounded-md border border-input bg-background px-3 py-2 text-sm" {...form.register(field)} />
                  ) : (
                    <Input id={field} type={field === 'contactNumber' ? 'tel' : 'text'} autoComplete={field === 'fullName' ? 'name' : 'off'} {...form.register(field)} />
                  )}
                  {form.formState.errors[field] && <p className="text-sm text-destructive">{form.formState.errors[field]?.message}</p>}
                </div>
              ))}
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input id="email" readOnly {...form.register('email')} />
              </div>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="expectedStartTime">Visit from</Label>
                  <Input id="expectedStartTime" type="datetime-local" min={localDateTimeValue(new Date(Date.now() + 60 * 1000))} {...form.register('expectedStartTime')} />
                  {form.formState.errors.expectedStartTime && <p className="text-sm text-destructive">{form.formState.errors.expectedStartTime.message}</p>}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="expectedEndTime">Visit to</Label>
                  <Input id="expectedEndTime" type="datetime-local" min={expectedStartTime || undefined} {...form.register('expectedEndTime')} />
                  {form.formState.errors.expectedEndTime && <p className="text-sm text-destructive">{form.formState.errors.expectedEndTime.message}</p>}
                </div>
              </div>
              {create.isError && <p className="text-sm text-destructive">{errorMessage(create.error)}</p>}
              <Button className="w-full" type="submit" disabled={create.isPending}>
                {create.isPending ? 'Sending request...' : 'Send request'}
              </Button>
            </form>
          )}
        </CardContent>
      </Card>
    </main>
  )
}

function MessageCard({ message }: { message: string }) {
  return <main className="flex min-h-screen items-center justify-center px-4"><Card><CardContent className="p-8"><p>{message}</p></CardContent></Card></main>
}
