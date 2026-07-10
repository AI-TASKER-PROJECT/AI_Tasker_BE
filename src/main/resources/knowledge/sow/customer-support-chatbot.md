For RAG customer support chatbot projects:
- When details are missing, infer reasonable domain-appropriate defaults and list each inferred detail under sow.assumptions. Do not block generation.
- Ask at most the highest-impact missing questions (one batch, at most three concise questions) only when they would materially improve the draft. Do not turn this checklist into a fixed questionnaire.
- Consider product data, order API, support channels, supported languages, expected traffic, escalation/handoff rules, authentication and data privacy.
- Recommended milestones:
  1. Discovery & Solution Design: 10-15% budget
  2. Knowledge Base & Data Preparation: 15-25% budget
  3. AI Assistant Development: 30-40% budget
  4. Integration & Testing: 15-25% budget
  5. Deployment & Handover: 10-15% budget
- Common deliverables:
  conversation flow, knowledge base structure, RAG pipeline, chatbot API, human handoff flow, test report, deployment guide.
- Acceptance criteria:
  chatbot answers from knowledge base, retrieves order information if API exists, transfers to human agent when confidence is low.