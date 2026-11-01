# VMS

## Local stack

1. Copy `.env.example` to `.env` and set `JWT_SECRET`, database credentials, and SMTP values.
2. Start the complete stack:

   ```powershell
   docker compose up -d --build
   ```

3. Generate local TLS certificates before starting nginx:

   ```powershell
   mkcert -install
   mkcert -cert-file infra/nginx/certs/localhost.pem `
     -key-file infra/nginx/certs/localhost-key.pem localhost 127.0.0.1
   ```

   Install mkcert's root CA on any phone used for camera testing. Then open the
   application at `https://localhost`, Mailpit at `http://localhost:8025`,
   and Grafana at `http://localhost:3000`.

Prometheus scrapes the backend internally. The backend is not published directly;
nginx serves the frontend and proxies `/api/` and `/ws`.

For phone testing, set `FRONTEND_URL` to the machine's LAN URL. Camera access
normally requires HTTPS. For phone testing, replace `localhost` in the mkcert
command and `FRONTEND_URL` with the machine's LAN hostname or IP, then
regenerate the entrance QR. Certificates are intentionally ignored by Git.

The local profile is the only profile that runs `DataSeeder`. Container
deployments use the `container` profile and require an explicit non-default
32-byte JWT secret.
