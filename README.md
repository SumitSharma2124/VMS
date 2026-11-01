# VMS

## Local stack

1. Copy `.env.example` to `.env` and set `JWT_SECRET`, database credentials, and SMTP values.
2. Start the complete stack:

   ```powershell
   docker compose up -d --build
   ```

3. Open the application at `http://localhost`, Mailpit at `http://localhost:8025`,
   and Grafana at `http://localhost:3000`.

Prometheus scrapes the backend internally. The backend is not published directly;
nginx serves the frontend and proxies `/api/` and `/ws`.

For phone testing, set `FRONTEND_URL` to the machine's LAN URL. Camera access
normally requires HTTPS. Generate local certificates with `mkcert`, place them
under `infra/nginx/certs/`, and extend the nginx configuration for port 443.
Certificates are intentionally ignored by Git.

The local profile is the only profile that runs `DataSeeder`. Container
deployments use the `container` profile and require an explicit non-default
32-byte JWT secret.
