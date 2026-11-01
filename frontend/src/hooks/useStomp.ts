import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'
import { useCallback, useEffect, useRef, useState } from 'react'
import { useAuthStore } from '@/store/auth'

export function useStomp() {
  const token = useAuthStore((state) => state.token)
  const [connected, setConnected] = useState(false)
  const clientRef = useRef<Client | null>(null)
  const subscriptions = useRef<StompSubscription[]>([])
  const subscriptionSpecs = useRef<{ topic: string; handler: (message: IMessage) => void }[]>([])

  useEffect(() => {
    if (!token) return
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const client = new Client({
      brokerURL: `${protocol}//${window.location.host}/ws`,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 3000,
      onConnect: () => {
        setConnected(true)
        subscriptions.current.forEach((subscription) => subscription.unsubscribe())
        subscriptions.current = subscriptionSpecs.current.map(({ topic, handler }) => client.subscribe(topic, handler))
      },
      onDisconnect: () => setConnected(false),
      onWebSocketClose: () => setConnected(false),
    })
    client.activate()
    clientRef.current = client
    return () => {
      subscriptions.current.forEach((subscription) => subscription.unsubscribe())
      subscriptions.current = []
      subscriptionSpecs.current = []
      void client.deactivate()
      clientRef.current = null
      setConnected(false)
    }
  }, [token])

  const subscribe = useCallback((topic: string, handler: (message: IMessage) => void) => {
    const client = clientRef.current
    const spec = { topic, handler }
    subscriptionSpecs.current.push(spec)
    const subscription = client?.connected ? client.subscribe(topic, handler) : undefined
    if (subscription) subscriptions.current.push(subscription)
    return () => {
      subscription?.unsubscribe()
      subscriptionSpecs.current = subscriptionSpecs.current.filter((item) => item !== spec)
      subscriptions.current = subscriptions.current.filter((item) => item !== subscription)
    }
  }, [])

  return { connected, subscribe }
}
