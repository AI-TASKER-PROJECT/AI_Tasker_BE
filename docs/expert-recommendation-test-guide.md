# Huong Dan Test Expert Candidate Va AI Recommendation

Tai lieu nay huong dan test thu cong bang Postman/curl cho flow:

1. Lay SoW cua Job Posting.
2. Extract keyword/skills/domains tu SoW.
3. Loc va sort Top 20 Expert Candidate tu bang `portfolios`.
4. Goi AI chon Top 5 Expert Recommendation.
5. Luu va doc lai recommendation tu bang `expert_recommendations`.

## 1. Chuan Bi Moi Truong

Chay database:

```powershell
docker compose up -d
```

Chay backend:

```powershell
.\mvnw.cmd spring-boot:run
```

Base URL:

```text
http://localhost:8080
```

Health check:

```http
GET {{baseUrl}}/api/health
```

Expected:

```json
{
  "success": true
}
```

Neu local DB da co migration `V24` nhung repo hien tai chua co file V24, Flyway co the bao:

```text
Detected applied migration not resolved locally: 24
```

Day la van de migration history cua local DB. Co the dung database moi sach hoac restore file migration V24 tu branch da tao no.

## 2. Bien Moi Truong Postman

Tao Postman environment:

| Key | Value |
| --- | --- |
| `baseUrl` | `http://localhost:8080` |
| `businessToken` | token cua business |
| `jobPostingId` | id job can test |

Header mac dinh neu can token:

```text
Authorization: Bearer {{businessToken}}
Content-Type: application/json
```

Hai API moi hien tai khong yeu cau body.

## 3. Chuan Bi Du Lieu Job Co SoW

Can co mot job da luu SoW trong bang `sow`, kem milestone neu muon test day du.

Neu dung API tao job, body mau:

```http
POST {{baseUrl}}/api/v1/jobs
Authorization: Bearer {{businessToken}}
Content-Type: application/json
```

```json
{
  "title": "Xay dung tro ly AI cham soc khach hang",
  "rawRequirements": "Can chatbot AI tra loi cau hoi san pham, tra cuu don hang, tich hop API CRM va trien khai production.",
  "budget": 90000000,
  "plannedDurationValue": 6,
  "plannedDurationUnit": "WEEK",
  "sow": {
    "title": "Tro ly AI cham soc khach hang",
    "overview": "Xay dung chatbot AI su dung RAG va knowledge base de ho tro customer support.",
    "objectives": [
      "Tu dong tra loi cau hoi khach hang",
      "Tra cuu don hang va chuyen ticket sang CRM"
    ],
    "scopeOfWork": [
      "Thiet ke RAG knowledge base",
      "Tich hop API don hang va CRM",
      "Kiem thu va trien khai production"
    ],
    "deliverables": [
      "Chatbot API",
      "Tai lieu deployment",
      "Bao cao testing"
    ],
    "assumptions": [
      "Business cung cap FAQ va API don hang"
    ],
    "outOfScope": [
      "Khong xay mobile app"
    ]
  },
  "milestones": [
    {
      "name": "Discovery va Solution Design",
      "description": "Phan tich yeu cau chatbot, CRM va order management.",
      "budget": 30000000,
      "orderIndex": 1,
      "status": "Pending"
    },
    {
      "name": "Build RAG Chatbot",
      "description": "Xay dung chatbot AI, API integration va NLP flow.",
      "budget": 40000000,
      "orderIndex": 2,
      "status": "Pending"
    },
    {
      "name": "Testing va Deployment",
      "description": "Kiem thu, viet tai lieu va trien khai production.",
      "budget": 20000000,
      "orderIndex": 3,
      "status": "Pending"
    }
  ]
}
```

Lay `data.jobId` tu response va gan vao `jobPostingId`.

Neu test bang seed data, co the thu `jobPostingId = 1`, mien la job do co SoW/structured SoW va portfolio seed co domain/skill lien quan.

## 4. Kiem Tra Top 20 Expert Candidate

Endpoint:

```http
GET {{baseUrl}}/api/jobs/{{jobPostingId}}/expert-candidates
```

Expected response shape:

```json
{
  "jobPostingId": 1,
  "keywords": {
    "skills": ["AI", "Chatbot", "RAG / Knowledge Base", "API Integration", "Testing", "Deployment"],
    "domains": ["Customer Support", "Order Management", "CRM"],
    "keywords": ["AI", "Chatbot", "RAG / Knowledge Base"]
  },
  "candidates": [
    {
      "expertId": 1,
      "portfolioId": 1,
      "matchScore": 83.5,
      "matchedSkills": ["RAG / Knowledge Base", "API Integration"],
      "matchedDomains": ["Customer Support"],
      "yearsExperience": 5,
      "certificates": "...",
      "selfDescription": "..."
    }
  ]
}
```

Pass criteria:

- API tra `200`.
- `keywords.skills`, `keywords.domains`, `keywords.keywords` khong null.
- `candidates` la array, co the rong neu khong co portfolio match.
- Neu co candidate, danh sach da sort giam dan theo `matchScore`.
- Moi candidate phai co it nhat mot match trong `matchedSkills` hoac `matchedDomains`.
- Khong crash neu SoW thieu field.

## 5. Generate AI Expert Recommendation Top 5

Endpoint:

```http
POST {{baseUrl}}/api/jobs/{{jobPostingId}}/expert-recommendations
```

Body: khong can.

Expected response shape:

```json
{
  "success": true,
  "message": "GENERATE EXPERT RECOMMENDATIONS SUCCESS",
  "data": {
    "jobPostingId": 1,
    "recommendations": [
      {
        "expertId": 1,
        "portfolioId": 1,
        "rankPosition": 1,
        "matchScore": 92.5,
        "matchedSkills": ["AI", "Chatbot", "RAG / Knowledge Base"],
        "matchedDomains": ["Customer Support"],
        "reason": "Expert phu hop vi co kinh nghiem xay dung chatbot AI cham soc khach hang."
      }
    ],
    "generatedByAi": true,
    "message": "AI recommendations generated successfully."
  }
}
```

Pass criteria:

- API tra `200`.
- `data.recommendations.length <= 5`.
- `rankPosition` bat dau tu `1` va tang dan.
- Moi `expertId` phai nam trong danh sach Top 20 candidate tu endpoint `/expert-candidates`.
- `matchScore` nam trong khoang `0-100`.
- Ket qua duoc luu vao bang `expert_recommendations`.
- Neu goi lai POST cung job, recommendation cu bi xoa va tao lai.

## 6. Get Saved Expert Recommendation

Endpoint:

```http
GET {{baseUrl}}/api/jobs/{{jobPostingId}}/expert-recommendations
```

Expected:

```json
{
  "success": true,
  "message": "GET EXPERT RECOMMENDATIONS SUCCESS",
  "data": {
    "jobPostingId": 1,
    "recommendations": [
      {
        "expertId": 1,
        "portfolioId": 1,
        "rankPosition": 1,
        "matchScore": 92.5,
        "matchedSkills": ["AI"],
        "matchedDomains": ["Customer Support"],
        "reason": "..."
      }
    ],
    "generatedByAi": null,
    "message": "Saved expert recommendations loaded."
  }
}
```

Pass criteria:

- GET khong goi AI.
- Tra recommendation da luu trong DB.
- Sort theo `rankPosition` tang dan.
- Neu chua generate lan nao, `recommendations = []`.

## 7. Test Fallback Khi Khong Co OPENAI_API_KEY

Dung mot trong hai cach:

1. Xoa hoac de trong `OPENAI_API_KEY` trong `.env`.
2. Chay backend voi env `OPENAI_API_KEY=`.

Sau do goi:

```http
POST {{baseUrl}}/api/jobs/{{jobPostingId}}/expert-recommendations
```

Expected:

```json
{
  "data": {
    "generatedByAi": false,
    "message": "AI recommendation failed, fallback to rule-based ranking.",
    "recommendations": [
      {
        "rankPosition": 1,
        "reason": "Duoc de xuat dua tren diem match tu ky nang, linh vuc, kinh nghiem va mo ta portfolio."
      }
    ]
  }
}
```

Pass criteria:

- API khong crash.
- Lay Top 5 dau tien tu danh sach candidate da sort.
- Van luu ket qua fallback vao `expert_recommendations`.

## 8. Test AI Response Validation

Neu mock OpenAI hoac test bang unit test, can cover cac case:

- AI tra hon 5 recommendation: backend chi nhan toi da 5.
- AI tra `expertId` khong nam trong candidate list: backend bo item do.
- AI tra `matchScore < 0`: backend clamp ve `0`.
- AI tra `matchScore > 100`: backend clamp ve `100`.
- AI thieu `rankPosition`: backend tu danh rank theo thu tu.
- AI tra invalid JSON: backend fallback Top 5 rule-based.
- AI timeout/500: backend fallback Top 5 rule-based.

## 9. Kiem Tra Database

Neu dung Docker Postgres:

```powershell
docker exec aitasker-postgres psql -U aitasker -d aitasker_db -c "select id, job_posting_id, expert_id, portfolio_id, rank_position, match_score, ai_reason, matched_skills, matched_domains, created_at from expert_recommendations where job_posting_id = 1 order by rank_position;"
```

Expected:

- Co toi da 5 rows cho mot `job_posting_id`.
- `rank_position` la `1..5`.
- `matched_skills` va `matched_domains` luu dang JSON text, vi du `["AI","Chatbot"]`.

Kiem tra generate lai co xoa du lieu cu:

```powershell
docker exec aitasker-postgres psql -U aitasker -d aitasker_db -c "select count(*) from expert_recommendations where job_posting_id = 1;"
```

Expected sau moi lan POST:

```text
count <= 5
```

## 10. Negative Test

### Job khong ton tai

```http
POST {{baseUrl}}/api/jobs/999999/expert-recommendations
```

Expected:

- `404` hoac response loi theo `GlobalExceptionHandler`.
- Khong insert DB.

### Job co SoW nhung khong co portfolio match

Expected:

```json
{
  "data": {
    "recommendations": [],
    "generatedByAi": false,
    "message": "No expert candidates found."
  }
}
```

### GET khi chua generate

```http
GET {{baseUrl}}/api/jobs/{{jobPostingId}}/expert-recommendations
```

Expected:

```json
{
  "data": {
    "recommendations": [],
    "generatedByAi": null,
    "message": "No saved expert recommendations found."
  }
}
```

## 11. Automated Test Commands

Compile:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

Chay test lien quan flow candidate va recommendation:

```powershell
.\mvnw.cmd -q "-Dtest=SowKeywordExtractionServiceTest,ExpertCandidateRankingServiceTest,ExpertRecommendationServiceTest" test
```

Full test suite:

```powershell
.\mvnw.cmd -q test
```

Luu y: neu full suite fail vi local DB co migration da apply nhung repo thieu file migration tuong ung, can sua migration history/local DB truoc khi danh gia ket qua full suite.

## 12. Checklist Pass Cuoi Cung

- `GET /api/jobs/{jobPostingId}/expert-candidates` tra Top 20 candidate dung keyword va sorted score.
- `POST /api/jobs/{jobPostingId}/expert-recommendations` tra Top 5 va luu DB.
- `GET /api/jobs/{jobPostingId}/expert-recommendations` doc lai dung Top 5 da luu.
- Khi OpenAI loi/thieu key, API fallback Top 5 rule-based va khong crash.
- Khong co expertId nao ngoai candidate list duoc luu.
- Khong gui toan bo expert database len AI.
- Khong sua bang `portfolios`.
