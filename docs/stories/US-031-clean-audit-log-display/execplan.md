# Exec Plan

## Goal

Return professional, Vietnamese audit log action and object labels without
showing raw numeric IDs in the admin audit-log table fields.

## Scope

In scope:

- Normalize legacy and fallback audit actions during read/write.
- Map known fallback API paths to business actions.
- Map known audit entities to clean display labels using related account,
  profile, job, contract, payment, and wallet data where available.
- Preserve raw entity keys in separate response fields for debugging.
- Add unit coverage for action normalization, fallback path mapping, and clean
  entity display.

Out of scope:

- Rewriting historical database rows.
- Changing audit log table columns.
- Changing frontend table layout.

## Risk Classification

Risk flags:

- Audit/security.
- Public contracts.
- Existing behavior.
- Weak proof.

Hard gates:

- Audit/security.

## Work Phases

1. Discovery of current audit persistence and rendering fields.
2. Design response compatibility and clean display fallback.
3. Implement backend mapping.
4. Add focused unit proof.
5. Compile and run focused tests.
6. Update Harness story, decision, and trace.

## Stop Conditions

Pause for human confirmation if:

- The frontend must remove raw debug fields instead of backend response cleanup.
- A database migration becomes necessary.
- Existing API consumers require raw `entityName` and `entityId` to remain the
  only identity fields.
