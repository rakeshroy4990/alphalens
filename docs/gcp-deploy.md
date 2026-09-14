# Google Cloud image

Google Cloud project: **alphalens-508509**  
Dashboard: https://console.cloud.google.com/home/dashboard?project=alphalens-508509

Firebase Hosting is a different project (`alphalens-a3cce`). Use this GCP project for Artifact Registry, Cloud Build, Cloud Run, and Cloud SQL.

This is **deploy packaging** for the completed mock research slice. It is not Phase 28. The image still uses `MockMarketDataProvider`.

The Cloud image is one container: Vue static files behind nginx, Spring Boot on localhost, nginx serving `$PORT` (Cloud Run default **8080**). The browser never talks to a market-data vendor.

```text
Internet → Cloud Run / GCE :8080 (nginx)
                ├─ /        → Vue SPA
                └─ /api/*   → Java 127.0.0.1:8088
                                  ↓
                           PostgreSQL (Cloud SQL, Auth Proxy, or existing hosted Postgres)
```

## 1. Build the image locally

```bash
./scripts/gcp-build-image.sh
```

Apple Silicon / Colima produces **linux/arm64**. Cloud Run and most GCE VMs need **linux/amd64**. Use Cloud Build for the image you deploy, or:

```bash
PLATFORM=linux/amd64 ./scripts/gcp-build-image.sh
```

Smoke-test with local Postgres:

```bash
docker-compose -f infrastructure/docker-compose.cloud.yml up --build
# or: docker compose -f infrastructure/docker-compose.cloud.yml up --build
```

Open http://127.0.0.1:8080 — health is `GET /api/health`. Demo symbols: `DEMA`, `DEMB`, `DEMC`.

## 2. Push to Artifact Registry

```bash
./scripts/gcp-use-project.sh
gcloud services enable artifactregistry.googleapis.com run.googleapis.com cloudbuild.googleapis.com compute.googleapis.com

gcloud artifacts repositories create alphalens \
  --repository-format=docker \
  --location=asia-south1 \
  --description="AlphaLens container images"

# From your machine (defaults to alphalens-508509)
PUSH=1 ./scripts/gcp-build-image.sh

# Cloud Build (amd64) — builds, pushes, and rolls Cloud Run
gcloud builds submit --project=alphalens-508509 --config=cloudbuild.yaml
```

That is the usual release command. Cloud Run does not pick up a new `:latest` image by itself; `cloudbuild.yaml` now deploys the revision after the push. Existing env and Secret Manager bindings stay on the service.

Image:

```text
asia-south1-docker.pkg.dev/alphalens-508509/alphalens/alphalens:latest
```

Change `_LOCATION` if the project is not in `asia-south1`.

## 3. Database

The image does **not** embed Postgres or secrets.

Pick one:

| Option | How |
| --- | --- |
| Cloud SQL (recommended on GCP) | Create Postgres 16. On GCE, run Cloud SQL Auth Proxy on `127.0.0.1:5432`. On Cloud Run, attach the instance and use a JDBC URL your team has verified, or keep using an existing hosted Postgres URL. |
| Existing hosted Postgres | Set `SPRING_DATASOURCE_URL` as `postgresql://postgres:{{password}}@db.<ref>.supabase.co:5432/postgres`. Do not convert env files to `jdbc:`. |

Never bake passwords into the image. Use Secret Manager or the VM/Cloud Run env (encrypted).

Placeholder env: `infrastructure/gcp/env.example`.

## 4. Deploy Cloud Run

Code changes: `gcloud builds submit` (section 2) already rolls Cloud Run.

Env or secret changes only — no image rebuild:

```bash
gcloud run deploy alphalens \
  --project=alphalens-508509 \
  --region=asia-south1 \
  --image=asia-south1-docker.pkg.dev/alphalens-508509/alphalens/alphalens:latest \
  --env-vars-file=infrastructure/gcp/cloud-run-env.yaml \
  --set-secrets=SPRING_DATASOURCE_PASSWORD=SPRING_DATASOURCE_PASSWORD:latest
```

`--set-env-vars` splits on commas, so CORS values belong in `cloud-run-env.yaml`.

One-time Cloud Build IAM (if the deploy step fails with permission denied):

```bash
PROJECT_NUMBER=113523778150
gcloud projects add-iam-policy-binding alphalens-508509 \
  --member="serviceAccount:${PROJECT_NUMBER}@cloudbuild.gserviceaccount.com" \
  --role=roles/run.admin
gcloud iam service-accounts add-iam-policy-binding \
  ${PROJECT_NUMBER}-compute@developer.gserviceaccount.com \
  --member="serviceAccount:${PROJECT_NUMBER}@cloudbuild.gserviceaccount.com" \
  --role=roles/iam.serviceAccountUser \
  --project=alphalens-508509
```

Startup probe must be **HTTP** `GET /api/health` (not TCP on 8080). Nginx binds `$PORT` immediately; Java needs ~35s on 8088 plus Flyway. A TCP probe marks the revision ready, Cloud Run throttles CPU, and `/api/*` returns nginx **502**.

The all-in-one image builds Vue with `VITE_API_BASE_URL=/api` so the browser stays on the Cloud Run origin and nginx proxies `/api` to Java. Do not bake a `localhost` or Firebase-only URL into that image.

Flyway **V5** (RLS) skips `flyway_schema_history` and uses an 8s lock timeout so a blocked `ALTER` cannot hang past the startup probe. After V5 has applied on Supabase, do not change that file.

`cloudbuild.yaml` sets `--no-cpu-throttling` and the HTTP probe on each roll.

## 5. Deploy Compute Engine (container)

Container-Optimized OS pulls the same image — no custom disk image required:

```bash
gcloud compute instances create-with-container alphalens \
  --zone=asia-south1-a \
  --machine-type=e2-medium \
  --project=alphalens-508509 \
  --container-image=asia-south1-docker.pkg.dev/alphalens-508509/alphalens/alphalens:latest \
  --container-env=SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:5432/alphalens,SPRING_DATASOURCE_USERNAME=alphalens,SPRING_DATASOURCE_PASSWORD=FROM_SECRET \
  --tags=alphalens-http \
  --scopes=cloud-platform

gcloud compute firewall-rules create alphalens-http \
  --allow=tcp:8080 \
  --target-tags=alphalens-http \
  --description="AlphaLens nginx"
```

Then http://VM_EXTERNAL_IP:8080

## 6. Split images (optional)

`backend/Dockerfile` and `frontend/Dockerfile` exist if you later split Cloud Run services. The all-in-one image is the default.

## 7. What this image does not do

- Live NSE/BSE data (Phase 28+; license first)
- Kite, OpenAI, billing
- Redis / Timescale (not required for the mock slice)

This is research/education software, not investment advice.
