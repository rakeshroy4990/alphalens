# Health API

`GET /api/health`

Public. No authentication. No request body or query parameters.

## Success

`200 OK` when the API process is up and PostgreSQL answers `SELECT 1`.

```json
{
  "status": "UP",
  "database": "UP",
  "service": "alphalens-backend",
  "timestamp": "2026-09-13T09:30:00Z"
}
```

## Database unavailable

`503 Service Unavailable` when the API is running but the database probe fails.

```json
{
  "status": "DOWN",
  "database": "DOWN",
  "service": "alphalens-backend",
  "timestamp": "2026-09-13T09:30:00Z"
}
```

JSON keys are camelCase. `timestamp` is UTC ISO-8601.

## Validation

This endpoint has no user input. Invalid methods receive the framework default error. Other APIs use the shared validation error body:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": ["field: must not be blank"],
  "timestamp": "2026-09-13T09:30:00Z"
}
```
