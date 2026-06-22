# Execution Plan

## Steps

1. Compare the week4 rejection reason implementation with the current week6
   profile flow.
2. Add a new week6-safe Flyway migration instead of reusing the old duplicate
   `V19` migration.
3. Add entity fields for `rejectionReason`.
4. Update the profile approval controller/service contract.
5. Add focused unit tests for reject reason validation and persistence.
6. Refresh Swagger/OpenAPI and product architecture notes.
7. Run focused test and compile proof.

## Validation

Use focused service tests plus compile proof. Full integration migration proof
requires a PostgreSQL database with Flyway enabled.
