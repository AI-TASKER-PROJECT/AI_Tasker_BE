# Exec Plan

## Goal

Restore Business progress-report feedback as a first-class API while preserving
the current acknowledgement gate that blocks later progress reports until the
latest report is handled by Business.

## Scope

In scope:

- Re-map existing `milestone_progress_reports` feedback columns in JPA.
- Restore `ProgressReportFeedbackRequest`.
- Restore `POST .../progress-reports/{progressReportId}/feedback`.
- Make feedback acknowledge a pending report.
- Restore Expert notification for feedback.
- Update product/spec/API docs and focused tests.

Out of scope:

- Financial settlement changes.
- Dispute routing changes.
- New tables or data deletion.

## Risk Classification

Risk flags:

- Authorization.
- Data model.
- Public contracts.
- Existing behavior.
- Weak proof.

Hard gates:

- Authorization.
- Public API behavior.

## Work Phases

1. Discovery of current and historical progress-report code.
2. Story/design/validation packet creation.
3. Entity, DTO, service, controller, and notification restoration.
4. Test updates for feedback and acknowledgement interaction.
5. Spec/product/Swagger doc sync.
6. Compile, focused tests, full suite where available.
7. Harness evidence update and trace.

## Stop Conditions

Pause for human confirmation if:

- Restoring feedback requires deleting or rewriting existing migrations.
- Feedback conflicts with financial/dispute settlement rules.
- Validation must be weakened below focused service tests and compile proof.
