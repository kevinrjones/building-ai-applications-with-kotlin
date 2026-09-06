# Retry Policy

Our services retry failed outbound requests at most **3 times**, using exponential
backoff starting at 500 ms (500 ms, 1 s, 2 s). Only idempotent operations are retried.
Retries are never attempted after a response body has started streaming.

Requests that still fail after the third attempt are surfaced to the caller with a
clear error and logged with the correlation id.
