import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { api, errorMessage } from '@/lib/api'

const schema = z.object({
  orgName: z.string().min(1, 'Organization name is required'),
  address: z.string(),
  orgContact: z.string(),
  adminName: z.string().min(1, 'Admin name is required'),
  adminEmail: z.email(),
  adminPassword: z.string().min(8, 'Password must be at least 8 characters'),
  confirmPassword: z.string(),
  adminPhone: z.string(),
}).refine((value) => value.adminPassword === value.confirmPassword, {
  path: ['confirmPassword'],
  message: 'Passwords do not match',
})

type Values = z.infer<typeof schema>

export function RegisterOrg() {
  const navigate = useNavigate()
  const form = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: {
      orgName: '', address: '', orgContact: '', adminName: '', adminEmail: '',
      adminPassword: '', confirmPassword: '', adminPhone: '',
    },
  })
  const mutation = useMutation({
    mutationFn: (values: Values) => api.post('/public/orgs/register', {
      orgName: values.orgName,
      address: values.address,
      orgContact: values.orgContact,
      adminName: values.adminName,
      adminEmail: values.adminEmail,
      adminPassword: values.adminPassword,
      adminPhone: values.adminPhone,
    }),
    onSuccess: () => {
      toast.success('Organization registered successfully')
      navigate('/login')
    },
  })
  const fields = [
    ['orgName', 'Organization name', 'text'],
    ['address', 'Address', 'text'],
    ['orgContact', 'Organization contact', 'text'],
    ['adminName', 'Admin name', 'text'],
    ['adminEmail', 'Admin email', 'email'],
    ['adminPhone', 'Admin phone', 'text'],
    ['adminPassword', 'Password', 'password'],
    ['confirmPassword', 'Confirm password', 'password'],
  ] as const
  return (
    <main className="flex min-h-screen items-center justify-center bg-muted/40 px-4 py-10">
      <Card className="w-full max-w-2xl">
        <CardHeader><CardTitle>Register your organization</CardTitle></CardHeader>
        <CardContent>
          <form className="grid gap-4 sm:grid-cols-2" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
            {fields.map(([name, label, type]) => (
              <div className="space-y-2" key={name}>
                <Label htmlFor={name}>{label}</Label>
                <Input id={name} type={type} autoComplete={name.includes('Password') ? 'new-password' : name.includes('Email') ? 'email' : 'off'} {...form.register(name)} />
                {form.formState.errors[name] && <p className="text-sm text-destructive">{form.formState.errors[name]?.message}</p>}
              </div>
            ))}
            {mutation.isError && <p className="sm:col-span-2 text-sm text-destructive">{errorMessage(mutation.error)}</p>}
            <Button className="sm:col-span-2" type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? 'Registering...' : 'Register organization'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </main>
  )
}
