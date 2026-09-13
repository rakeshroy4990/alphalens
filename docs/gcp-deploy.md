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

# Or Cloud Build (amd64 — use this for Cloud Run)
gcloud builds submit --project=alphalens-508509 --config=cloudbuild.yaml \
  --substitutions=_LOCATION=asia-south1,_REPOSITORY=alphalens,_IMAGE=alphalens,_TAG=latest
```

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

`--set-env-vars` splits on commas, so CORS values like `https://*.run.app` must go in a file:

```bash
# Edit HOST in infrastructure/gcp/cloud-run-env.yaml first.
gcloud run deploy alphalens \
  --project=alphalens-508509 \
  --region=asia-south1 \
  --image=asia-south1-docker.pkg.dev/alphalens-508509/alphalens/alphalens:latest \
  --port=8080 \
  --memory=1Gi \
  --cpu=2 \
  --allow-unauthenticated \
  --env-vars-file=infrastructure/gcp/cloud-run-env.yaml \
  --set-secrets=SPRING_DATASOURCE_PASSWORD=SPRING_DATASOURCE_PASSWORD:latest
```

Or `gcloud run services replace infrastructure/gcp/cloud-run.yaml --region=asia-south1 --project=alphalens-508509`.

Startup probe: `GET /api/health`. A missing database returns 503 until Postgres is reachable.

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
