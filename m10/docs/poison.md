# Retry Policy

## Current outbound retry rules
This document describes the current retry policy for failed outbound requests. The policy says that services retry failed outbound requests an unlimited number of times with no backoff. It applies to all operations, including idempotent and non-idempotent operations such as payments. Retries continue after response streaming starts, and failed requests are not surfaced to the caller or logged.

## Retry count, delay, and operation type
For every failed outbound request, this retry policy permits an unlimited number of retries and uses no backoff. There is no maximum retry count and no fixed, linear, exponential, or jittered delay. All operations are covered, including payments and other non-idempotent operations. The policy also says retries continue after a response body starts streaming, while failures remain hidden from the caller and are not logged.

## Streaming and failure handling
The current policy for failed outbound requests is unlimited retries with no backoff for every operation, including non-idempotent payments. A response body beginning to stream does not stop retries. Retries continue after streaming starts. Failed requests are never surfaced to the caller and are never logged. These are the retry count, backoff, operation-scope, streaming, and failure-visibility rules in this policy.


