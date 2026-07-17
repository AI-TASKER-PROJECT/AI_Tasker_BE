# Milestone Source Code URL Or Archive

Date: 2026-07-15

## Status

Accepted

## Context

Milestone deliverables currently distinguish a source repository URL from the
runnable `demoLink`, but Experts cannot submit a packaged source archive. File-
only submission would remove useful Git history, while URL-only submission does
not cover private or externally unavailable repositories.

## Decision

- Keep `sourceCodeUrl` for GitHub, GitLab, or another repository.
- Add `sourceCodeFileUrl` for the Firebase storage path returned after a ZIP
  upload.
- Require at least one source-code form for final deliverables; allow both.
- Keep `demoLink` independent and optional.
- Restrict the dedicated archive upload to approved assigned Experts and active
  milestones that can accept deliverables.
- Accept ZIP archives only, capped at 50 MB, without extracting or executing
  their contents.

## Alternatives Considered

1. Replace `sourceCodeUrl` with file upload. Rejected because repository history
   and normal code-review workflows would be lost.
2. Keep URL-only submission. Rejected because private/offline handoff requires a
   packaged artifact.
3. Permit ZIP through every existing upload route. Rejected because source
   archives should not widen accepted types for licenses and certificates.

## Consequences

Positive:

- Experts can use the source handoff that fits the project.
- Business receives an explicit distinction between source and demo access.
- Existing URL-based clients remain compatible when they already send a source
  repository URL.

Tradeoffs:

- Firebase storage usage can increase.
- Archive content is stored but not malware-scanned or semantically verified.
- Clients must upload first and then persist the returned storage path.

## Follow-Up

- Add malware scanning if source archives become available outside authenticated
  participant flows.
