# Exec Plan

## Goal

Add a draft-job update flow that persists `jobs` + `sow` + `milestones`
together so editing draft milestones does not break the persisted SoW
requirement for publish.

## Scope

In scope:

- New endpoint `PUT /api/v1/jobs/{jobId}` for draft update.
- New service method `MarketplaceService.updateDraftJob(jobId, request)`.
- Upsert `sow` by `jobId` (update if exists, create if missing).
- Replace draft milestones for the same job while no contract exists.
- Update job core fields (`title`, `rawRequirements`, `budget`).
- Unit tests for the new flow.
- Docs sync.

Out of scope:

- Editing non-DRAFT jobs.
- Editing milestones after contract creation.
- Changing publish quota or AI generation.
- Changing catalog (domains/skills/technologies) update flow.

## Risk Classification

Risk flags:

- Data model
- Existing behavior
- Public contracts
- Authorization

Hard gates:

- Data model (sow upsert, milestone replace)
- Authorization (ownership + BUSINESS role)

4 flags + 2 hard gates -> high-risk.

## Work Phases

1. Discovery (read MarketplaceController/Service, SowRepository, SowEntity,
   MilestoneEntity, existing tests). -> DONE, Case B confirmed.
2. Design (endpoint shape, upsert sow, replace milestones, guards).
3. Validation planning (unit tests for update + publish still works).
4. Implementation (controller + service + upsert + replace).
5. Verification (unit tests + compile).
6. Harness update (story proof, decision, trace, docs sync).

## Stop Conditions

Pause for human confirmation if:

- The existing `saveSow`/`saveMilestones` create-time logic cannot be safely
  reused for upsert (it can: `saveSow` needs an upsert variant;
  `saveMilestones` needs a replace variant).
- A draft job is found to already have a contract (editing must be blocked).
- Validation requirements need to be weakened.
