# AI SoW Budget Assessment

Checklist triển khai frontend: [frontend-ai-sow-budget-change-guide.md](frontend-ai-sow-budget-change-guide.md).

## Product Rule

`POST /api/jobs/generate-sow` returns an advisory AI price range for the full
generated scope. The estimate never overwrites the amount entered by Business.
The pricing prompt does not contain the Business-entered amount or any sample
price. AI derives the range from project scope, duration, required roles,
integrations, data, testing, security, infrastructure, deployment, and risk.
The recommendation is read-only. Business may keep the entered amount or enter
a different custom amount before creating the draft job. When the entered
amount is above the full AI range (`status=HIGH`), frontend hides the advisory
card and keeps the Business amount without requiring a second confirmation.

The estimate is response-only in this release. No estimate or confirmation
choice is stored in the database.

## Request Contract

The existing `GenerateSowRequest` is unchanged. `budget` remains required and
means "amount currently proposed by Business", not an authoritative market
price.

| Field | Type | Required | Frontend meaning |
| --- | --- | --- | --- |
| `projectTitle` | string | yes | Project title. |
| `rawRequirement` | string | yes | Full raw requirement used to generate scope and pricing factors. |
| `budget` | number | yes | Current amount entered by Business. Must be greater than zero. |
| `duration` | integer | yes | Planned total duration. |
| `durationUnit` | string | yes | Duration unit supplied to SoW generation. |
| `supportFields` | string[] | no | Selected support domains. |
| `requiredSkills` | string[] | no | Selected skills. |
| `clarificationAlreadyAsked` | boolean | no | Suppress a second user-facing question cycle when true. |

## Response Contract For Frontend

### `budgetAssessment`

| Field | Type | Always returned | UI usage |
| --- | --- | --- | --- |
| `currency` | string | yes | Display currency; currently always `VND`. |
| `businessBudget` | number | yes | Echo of the Business-entered amount; never silently replace it. |
| `estimatedMin` | number | yes | Advisory minimum for the full generated scope. |
| `recommendedBudget` | number | yes | Main AI recommendation for the full generated scope. |
| `estimatedMax` | number | yes | Advisory upper estimate for the full generated scope. |
| `status` | enum | yes | Drives badge, warning color, and default CTA copy. |
| `gapToMinimum` | number | yes | Amount missing to reach `estimatedMin`; otherwise `0`. |
| `confidence` | enum | yes | `LOW`, `MEDIUM`, or `HIGH`; display as estimate confidence. |
| `source` | enum | yes | Shows whether the top-level AI estimate or the AI milestone total produced the range. |
| `requiresBusinessConfirmation` | boolean | yes | `false` for `HIGH`; otherwise `true`. |
| `message` | string | yes | Backend-owned Vietnamese explanation for the current status. |
| `factors` | string[] | yes | Short list of price drivers such as integrations, data work, or deployment. |

### `milestones[]`

| Field | Meaning when creating the Job |
| --- | --- |
| `budget` | Allocation whose total equals `budgetAssessment.businessBudget`. Use when Business keeps the entered amount. |
| `recommendedBudget` | Advisory allocation whose total equals `budgetAssessment.recommendedBudget`; use only as the proportional reference for custom reallocation. |

`recommendedBudget` is advisory and is used as a proportional reference for
custom reallocation. It is not assigned directly as the authoritative Job
budget by the Create Job UI.

## VND Scale Normalization

All returned money fields are full, whole VND amounts. The prompt uses only
integer type constraints and deliberately contains no sample price. Text
amounts returned by an untrusted provider are still parsed into full VND.

OpenAI output is untrusted. If it returns a bare whole-number recommendation
from `1` through `10000`, the backend treats it as abbreviated millions before
range comparison. For example, `estimatedMin=80`,
`recommendedBudget=100`, and `estimatedMax=130` become `80000000`,
`100000000`, and `130000000`. Confidence is lowered to `LOW` because the
backend repaired the provider's unit scale.

## Custom Budget Reallocation API

When Business enters a third custom amount, frontend must call:

`POST /api/jobs/reallocate-sow-budget`

This endpoint performs a stateless calculation. It does not call OpenAI, change
the advisory range, create a Job, or write to the database.

Request fields:

| Field | Type | Required | Frontend mapping |
| --- | --- | --- | --- |
| `selectedBudget` | whole-VND number | yes | Custom final amount entered and confirmed by Business. |
| `milestones` | array | yes | One item for every generated milestone, maximum 50. |
| `milestones[].milestoneIndex` | non-negative integer | yes | Zero-based index in the generated `milestones[]` array; indexes must be unique. |
| `milestones[].referenceBudget` | positive whole-VND number | yes | Copy the matching generated `milestones[].recommendedBudget`. |

Example request:

```json
{
  "selectedBudget": 110000000,
  "milestones": [
    { "milestoneIndex": 0, "referenceBudget": 40000000 },
    { "milestoneIndex": 1, "referenceBudget": 100000000 }
  ]
}
```

Example response:

```json
{
  "currency": "VND",
  "selectedBudget": 110000000,
  "allocationTotal": 110000000,
  "allocations": [
    {
      "milestoneIndex": 0,
      "referenceBudget": 40000000,
      "fundsAllocated": 31428571
    },
    {
      "milestoneIndex": 1,
      "referenceBudget": 100000000,
      "fundsAllocated": 78571429
    }
  ]
}
```

Frontend maps each returned `fundsAllocated` back to the generated milestone by
`milestoneIndex`. Do not calculate or round the custom allocation again.

## Status Table

| Status | Condition | Suggested UI |
| --- | --- | --- |
| `TOO_LOW` | `businessBudget < estimatedMin` | Red warning; show `gapToMinimum`; emphasize "Use AI recommendation" but keep both choices enabled. |
| `LOW` | `estimatedMin <= businessBudget < recommendedBudget` | Amber warning; explain the amount is inside the range but below recommendation. |
| `SUITABLE` | `recommendedBudget <= businessBudget <= estimatedMax` | Green/neutral state; allow either choice without warning. |
| `HIGH` | `businessBudget > estimatedMax` | Hide the advisory card and keep the higher Business amount. |

## Source Table

| Source | Meaning | Frontend treatment |
| --- | --- | --- |
| `AI_ADVISORY` | OpenAI returned a positive recommendation; backend normalized the range. | Display returned confidence. |
| `AI_MILESTONE_FALLBACK` | Top-level recommendation was missing, so backend used the valid raw AI milestone total. | Display low-confidence notice. |

If neither the top-level AI recommendation nor the AI milestone total is
usable, generation fails with an invalid-AI-budget error. The backend never
turns the Business-entered amount into an AI estimate.

## Confirmation Flow

```text
Generate SoW
  -> if status=HIGH: hide AI range and keep Business amount
  -> otherwise show Business amount and AI range
  -> Business selects one option
       KEEP_BUSINESS_BUDGET
       USE_CUSTOM_BUDGET
         -> enter custom amount
         -> call POST /api/jobs/reallocate-sow-budget
  -> build POST /api/v1/jobs payload
  -> Business can still edit the draft before publishing
```

Frontend mapping:

| Business action | `jobs.budget` | `milestones[].fundsAllocated` |
| --- | --- | --- |
| Keep entered amount | `budgetAssessment.businessBudget` | Copy each `milestones[].budget`. |
| Use custom amount | `reallocation.selectedBudget` | Map each `reallocation.allocations[].fundsAllocated` by `milestoneIndex`. |
| `HIGH` implicit keep | `budgetAssessment.businessBudget` | Copy each `milestones[].budget`; no advisory card or second confirmation. |

Before calling `POST /api/v1/jobs`, frontend must verify that the selected
milestone allocation total exactly equals the selected Job budget.

The AI `estimatedMin`, `recommendedBudget`, and `estimatedMax` remain read-only.
If the custom amount is below `estimatedMin`, show a warning but keep the final
confirmation action enabled because Business remains the price authority.

## Example Response

```json
{
  "needMoreInfo": false,
  "questions": [],
  "budgetAssessment": {
    "currency": "VND",
    "businessBudget": 50000000,
    "estimatedMin": 120000000,
    "recommendedBudget": 140000000,
    "estimatedMax": 160000000,
    "status": "TOO_LOW",
    "gapToMinimum": 70000000,
    "confidence": "MEDIUM",
    "source": "AI_ADVISORY",
    "requiresBusinessConfirmation": true,
    "message": "Ngân sách Business nhập thấp hơn mức tối thiểu AI ước tính cho toàn bộ phạm vi.",
    "factors": [
      "Hai hệ thống tích hợp",
      "Triển khai production"
    ]
  },
  "sow": {},
  "milestones": [
    {
      "name": "Discovery",
      "budget": 14000000,
      "recommendedBudget": 40000000
    },
    {
      "name": "Build",
      "budget": 36000000,
      "recommendedBudget": 100000000
    }
  ]
}
```

## Authority After Job Creation

- `jobs.budget` is the amount selected by Business.
- `proposals.bid_amount` is the amount proposed by Expert.
- The accepted proposal and its milestone budgets remain the authority used to
  create the contract total.
- The AI assessment is advisory only and does not create or update a Job.
