# Exec Plan

## Goal

Support the third frontend budget option with authoritative backend milestone
allocation and exact whole-VND totals.

## Risk Classification

High risk because the change adds a public contract and performs financial
allocation, even though it is stateless.

## Work Phases

1. Record the API and rounding authority decision.
2. Add request/response DTOs and controller route.
3. Add validation, duplicate protection, deterministic allocation, and tests.
4. Update frontend handoff, Swagger/Postman, architecture, and OpenAPI.
5. Run focused tests, compile, schema assertions, and Harness verification.

## Stop Conditions

Pause if implementation requires persistence, changes the AI estimate range,
or weakens Business final-price authority.
