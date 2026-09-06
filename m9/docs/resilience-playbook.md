# Platform Resilience Playbook

## Purpose

This playbook defines how our API gateway, billing service, and analytics pipeline handle failures while keeping customer impact low.

## Service roles

- **API Gateway**: Receives user traffic and orchestrates calls to downstream services.
- **Billing Service**: Charges cards and writes payment records.
- **Analytics Pipeline**: Ingests usage events and builds reports.

## Dependency map

- API Gateway depends on Billing Service for paid actions.
- API Gateway depends on Analytics Pipeline only for non-blocking event logging.
- Billing Service depends on **PaymentProviderX** (external API).
- Analytics Pipeline depends on **Kafka** and **DataWarehouseY**.

## Retry and timeout policy by path

### API Gateway -> Billing Service

- Timeout: **1500 ms**
- Retries: **2** retries with exponential backoff (**200 ms**, **400 ms**)
- Circuit breaker opens after **5 failures in 60 seconds**
- When circuit is open, paid actions return `503 BILLING_TEMPORARILY_UNAVAILABLE`

### Billing Service -> PaymentProviderX

- Timeout: **2500 ms**
- Retries: **3** retries with jittered exponential backoff (**500 ms**, **1 s**, **2 s** +/- 20%)
- Idempotency key is required on every charge attempt
- Do not retry on `402`, `403`, or validation errors

### API Gateway -> Analytics Pipeline

- Timeout: **300 ms**
- Retries: **0**
- If analytics write fails, request still succeeds and logs `ANALYTICS_DROPPED_EVENT`

## Degraded mode behavior

- If Billing circuit is open:
  - Existing subscribers can access previously purchased features.
  - New purchases are blocked.
  - Gateway emits alert `ALERT-BILLING-CB-OPEN`.
- If Analytics is down:
  - Core user requests are unaffected.
  - Daily KPI report can be delayed by up to **24 hours**.

## Alert thresholds

- Page on-call when:
  - Billing 5xx error rate > **8%** for **10 minutes**.
  - PaymentProviderX timeout rate > **12%** for **5 minutes**.
  - API Gateway p95 latency > **1200 ms** for **15 minutes**.
- Slack-only alert when:
  - Analytics dropped events > **2%** for **30 minutes**.

## Incident response rules

1. If PaymentProviderX is the root cause, enable `billingReadOnlyMode=true`.
2. In read-only mode:
   - Renewal retries continue for up to **6 hours**.
   - New checkout endpoints return `503` with message `Try again later`.
3. If incident exceeds **45 minutes**, post customer status-page update.

## Data consistency guarantees

- Billing writes are **strongly consistent** in the primary region.
- Analytics reports are **eventually consistent** (up to 24 hours delay).
- If charge succeeds but analytics event fails, finance data is still treated as source of truth.

## Change management note

Any change to retry counts, timeout values, or breaker thresholds requires:

1. An ADR update.
2. A load-test run in staging.
3. Sign-off from SRE and Billing owners.