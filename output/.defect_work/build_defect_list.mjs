import fs from "node:fs/promises";
import path from "node:path";
import { FileBlob, SpreadsheetFile, Workbook } from "@oai/artifact-tool";

const repo = process.cwd();
const outDir = path.join(repo, "output");
const storyRoot = path.join(repo, "docs", "stories");

const clean = (s = "") => s
  .replace(/```[\s\S]*?```/g, " ")
  .replace(/[`*_>#|]/g, " ")
  .replace(/\[[^\]]+\]\([^\)]+\)/g, m => m.replace(/\]\([^\)]+\)/, ""))
  .replace(/^[-+\d.)\s]+/gm, "")
  .replace(/\s+/g, " ")
  .trim();

const needsEnglishTranslation = (value) =>
  typeof value === "string" && /[^\u0000-\u007f]/.test(value);

async function translateStringsToEnglish(values) {
  const unique = [...new Set(values.filter(needsEnglishTranslation))];
  const translated = new Map();
  const chunks = [];
  let current = [];
  let currentLength = 0;

  for (const value of unique) {
    const addition = value.length + 32;
    if (current.length && currentLength + addition > 2800) {
      chunks.push(current);
      current = [];
      currentLength = 0;
    }
    current.push(value);
    currentLength += addition;
  }
  if (current.length) chunks.push(current);

  for (const chunk of chunks) {
    const marked = chunk
      .map((value, index) => `ZXQ${String(index + 1).padStart(5, "0")}ZXQ\n${value}`)
      .join("\n") + "\nZXQENDZXQ";
    const url = new URL("https://translate.googleapis.com/translate_a/single");
    url.searchParams.set("client", "gtx");
    url.searchParams.set("sl", "vi");
    url.searchParams.set("tl", "en");
    url.searchParams.set("dt", "t");
    url.searchParams.set("q", marked);

    let response;
    for (let attempt = 1; attempt <= 3; attempt++) {
      response = await fetch(url);
      if (response.ok) break;
      if (attempt === 3) throw new Error(`Translation request failed: ${response.status}`);
      await new Promise(resolve => setTimeout(resolve, 600 * attempt));
    }
    const payload = await response.json();
    const output = (payload[0] || []).map(part => part[0] || "").join("");
    for (let index = 0; index < chunk.length; index++) {
      const marker = `ZXQ${String(index + 1).padStart(5, "0")}ZXQ`;
      const nextMarker = index + 1 < chunk.length
        ? `ZXQ${String(index + 2).padStart(5, "0")}ZXQ`
        : "ZXQENDZXQ";
      const start = output.indexOf(marker);
      const end = output.indexOf(nextMarker, start + marker.length);
      if (start < 0 || end < 0) throw new Error(`Translation marker lost: ${marker}`);
      const english = output
        .slice(start + marker.length, end)
        .replace(/^\s+|\s+$/g, "");
      translated.set(chunk[index], english);
    }
  }
  return translated;
}

function section(text, names) {
  for (const name of names) {
    const re = new RegExp(`^##\\s+${name}\\s*$([\\s\\S]*?)(?=^##\\s+|\\Z)`, "im");
    const m = text.match(re);
    if (m && clean(m[1])) return clean(m[1]).slice(0, 900);
  }
  return "Acceptance criteria không được tách thành mục riêng; đã đối chiếu overview/design/validation của story.";
}

function titleFrom(primaryText, base) {
  const h1 = primaryText.match(/^#\s+(.+)$/m)?.[1]?.trim() || "";
  if (h1 && !/^overview$/i.test(h1) && !/^overview\s*[—-]/i.test(h1)) {
    return clean(h1.replace(/^US-[A-Z0-9-]+\s*[:—-]?\s*/i, ""));
  }
  return base.replace(/^US-\d{3}-/, "").split("-").map(x => x ? x[0].toUpperCase() + x.slice(1) : x).join(" ");
}

function moduleFor(title) {
  const t = title.toLowerCase();
  if (/auth|login|password|token/.test(t)) return "Authentication & Security";
  if (/wallet|payment|payos|revenue|credit|membership|withdraw/.test(t)) return "Payment & Wallet";
  if (/dispute|settlement|termination/.test(t)) return "Dispute & Settlement";
  if (/milestone|contract|deliverable|progress report|escrow/.test(t)) return "Contract & Milestone";
  if (/recommend|sow|ai /.test(t)) return "AI & Recommendation";
  if (/profile|kyb|kyc|portfolio/.test(t)) return "Profile & Verification";
  if (/admin|audit|setting|swagger|api/.test(t)) return "Admin & Platform";
  if (/job|proposal|marketplace/.test(t)) return "Marketplace";
  if (/notification|realtime/.test(t)) return "Notification";
  return "Cross-cutting";
}

async function loadStories() {
  const entries = await fs.readdir(storyRoot, { withFileTypes: true });
  const records = [];
  for (const entry of entries) {
    if (entry.isFile() && /^US-\d{3}.*\.md$/i.test(entry.name)) {
      const p = path.join(storyRoot, entry.name);
      const txt = await fs.readFile(p, "utf8");
      const id = entry.name.match(/US-\d{3}/i)[0].toUpperCase();
      const title = titleFrom(txt, entry.name.replace(/\.md$/i, ""));
      records.push({ id, title, module: moduleFor(title), ac: section(txt, ["Acceptance Criteria", "Product Contract", "Target Behavior", "Goal"]), source: path.relative(repo, p).replaceAll("\\", "/") });
    } else if (entry.isDirectory() && /^US-\d{3}/i.test(entry.name)) {
      const dir = path.join(storyRoot, entry.name);
      const names = await fs.readdir(dir);
      const primaryName = names.includes("overview.md") ? "overview.md" : (names.includes("story.md") ? "story.md" : names.find(n => n.endsWith(".md")));
      if (!primaryName) continue;
      const primaryPath = path.join(dir, primaryName);
      const primary = await fs.readFile(primaryPath, "utf8");
      let packet = primary;
      for (const n of names.filter(n => n.endsWith(".md") && n !== primaryName)) packet += "\n" + await fs.readFile(path.join(dir, n), "utf8");
      const id = entry.name.match(/US-\d{3}/i)[0].toUpperCase();
      const title = titleFrom(primary, entry.name);
      records.push({ id, title, module: moduleFor(title), ac: section(packet, ["Acceptance Criteria", "Target Behavior", "Product Contract", "Goal"]), source: path.relative(repo, primaryPath).replaceAll("\\", "/") });
    }
  }
  records.push({ id: "US-024", title: "Prevent Duplicated Milestone Guidance In Generated SoW", module: "AI & Recommendation", ac: "AI-generated SoW fields must not repeat the recommended milestone section; prompt and post-parse cleanup preserve the actual milestone list once.", source: "Harness story table; trace #32; commit 6bf0120" });
  const auth = [
    ["US-AUTH-001", "Temporary Lockout After 5 Wrong Passwords"],
    ["US-AUTH-002", "Security Lock After Second 5-attempt Cycle"],
    ["US-AUTH-003", "Forgot-password Sends Reset Link Via Email"],
    ["US-AUTH-004", "Reset Password With Token Validation"],
    ["US-AUTH-005", "Login Resets Failed Counters On Success"],
  ];
  for (const [id, title] of auth) records.push({ id, title, module: "Authentication & Security", ac: "Theo gói high-risk auth: kiểm soát brute-force, reset password an toàn, token dùng một lần và phục hồi trạng thái khóa/counter đúng quy tắc.", source: "docs/stories/high-risk-auth-lockout-reset/execplan.md" });
  if (process.env.DEFECT_STORY_CUTOFF_20260721 === "1") {
    const addedAfterWorkbookCreation = [
      "docs/stories/US-066-transparent-wallet-history-scope-split/",
      "docs/stories/US-067-structured-final-deliverable-rejection-feedback/",
      "docs/stories/US-068-editable-marketplace-and-contract-change-requests/",
      "docs/stories/US-069-ai-sow-budget-assessment/",
      "docs/stories/US-070-custom-sow-budget-reallocation/",
    ];
    for (let index = records.length - 1; index >= 0; index--) {
      if (addedAfterWorkbookCreation.some(prefix => records[index].source.startsWith(prefix))) {
        records.splice(index, 1);
      }
    }
  }
  records.sort((a, b) => a.id.localeCompare(b.id) || a.title.localeCompare(b.title));
  const counts = new Map();
  for (const r of records) counts.set(r.id, (counts.get(r.id) || 0) + 1);
  for (const r of records) r.note = counts.get(r.id) > 1 ? "ID được tái sử dụng cho nhiều story definition; giữ riêng từng dòng để không mất coverage." : "Đã đọc và đối chiếu nguồn trong project.";
  return records;
}

const D = (id, us, module, summary, description, steps, expected, actual, severity, priority, status, assigned, fixed, tc, level, evidence, ref, note = "") => ({
  id, us, module, summary, description, steps, expected, actual, severity, priority, status,
  detectedBy: "Unknown", assigned, detected: null, fixed: fixed ? new Date(`${fixed}T00:00:00`) : null,
  tc, level, evidence, ref, note
});

const defects = [
  D("DEF-001","US-058","Admin account management","Tạo tài khoản Staff không tự tạo Staff profile","Luồng Admin tạo account role STAFF từng để account tồn tại nhưng thiếu bản ghi Staff tương ứng, làm các luồng gán/routing Staff không có profile để sử dụng.","1. Đăng nhập Admin.\n2. Tạo account với role STAFF.\n3. Truy vấn Staff profile của account vừa tạo.","Account STAFF và Staff profile liên kết phải được tạo trong cùng luồng.","Account được tạo nhưng Staff profile không tồn tại.","High","High","Closed","Nguyễn Thiên Trường","2026-06-07","AdminServiceTest.createAccount_shouldCreateStaffProfileWhenRoleIsStaff","Confirmed","src/test/java/com/aitasker/be/service/core/AdminServiceTest.java:168; commit fc325ac","fc325ac","Commit có regression test và đã sửa luồng."),
  D("DEF-002","US-002","Contract lifecycle","Hoàn tất milestone không đóng đúng contract/job","Backend cũ kích hoạt/hoàn tất contract với trạng thái job không đúng; hoàn thành milestone cuối chưa đảm bảo contract COMPLETED và job CLOSED.","1. Tạo contract đang ACTIVE với milestone cuối.\n2. Hoàn tất milestone cuối.\n3. Đọc trạng thái contract và job.","Contract chuyển COMPLETED và job chuyển CLOSED.","Contract/job có thể giữ trạng thái cũ hoặc job bị đóng sai thời điểm.","Critical","Urgent","Closed","Nguyen Trong Hieu","2026-06-19","ContractExecutionServiceTest.completeMilestone_shouldCompleteContractAndCloseJobWhenAllMilestonesCompleted","Confirmed","docs/stories/US-002-contract-management-flow/overview.md:7; docs/decisions/0008-contract-lifecycle-statuses.md:12; ContractExecutionServiceTest:183","578cda9","Đã có test hồi quy cho completion path."),
  D("DEF-003","US-005","Payment/contract status","Status mixed-case và lifecycle cũ gây sai luồng","Schema, service, test và docs từng dùng lẫn status mixed-case/obsolete; contract vẫn có thể đi vào negotiation/change-request lifecycle đã bỏ.","1. Thực hiện payment/contract flow trên dữ liệu status cũ.\n2. Chuyển trạng thái contract/job/milestone.\n3. Đối chiếu constraint và API response.","Chỉ dùng bộ enum chuẩn UPPER_CASE đã quy định trong US-005.","Status không đồng nhất hoặc luồng obsolete vẫn được chấp nhận.","High","High","Closed","Nguyễn Thiên Trường","2026-06-20","Full Maven suite 55 tests; ContractExecutionServiceTest invalid-status cases","Confirmed","docs/stories/US-005-payment-contract-status-alignment/overview.md:5; docs/stories/US-005-payment-contract-status-alignment/validation.md:36; V31__status_enum_constraint_alignment.sql","595427a","Lần test đầu fail, sau sửa service toàn bộ 55 test pass."),
  D("DEF-004","US-006","SLA auto approval","SLA auto-approve milestone cuối không hoàn tất contract/job","Nhánh auto-approve chỉ hoàn tất milestone quá SLA nhưng không dùng cùng helper finalization như Business approval.","1. Chuẩn bị contract ACTIVE chỉ còn milestone cuối UNDER_REVIEW quá SLA.\n2. Gọi SLA auto-approve.\n3. Kiểm tra contract và job.","Milestone COMPLETED, contract COMPLETED, job CLOSED.","Milestone hoàn tất nhưng contract/job không được hoàn tất tương ứng.","High","High","Closed","Nguyen Trong Hieu","2026-06-20","ContractExecutionServiceTest.runSlaAutoApprove_shouldCompleteContractAndCloseJobWhenFinalMilestoneApproved","Confirmed","docs/stories/US-006-sla-auto-approve-contract-completion.md:13-20; ContractExecutionServiceTest.java:231","e68990a","Focused 12 tests và full 56 tests pass."),
  D("DEF-005","US-008","Membership entitlement","Mua gói thấp hơn làm mất Premium còn hạn","Premium access từng dựa vào display flag/badge expiry nên giao dịch Standard/Plus có thể ghi đè flag Premium.","1. Mua Premium và xác nhận còn hạn.\n2. Mua thêm Standard hoặc Plus trước khi Premium hết hạn.\n3. Truy cập recommendation/quota.","Premium vẫn active đến premium_expired_at và không bị rút ngắn.","Quyền Premium bị mất ngay sau khi mua gói thấp hơn.","High","Urgent","Closed","Nguyễn Thiên Trường","2026-06-21","PaymentWalletServiceTest.purchaseMembership_shouldKeepPremiumActiveWhenLowerTierIsBoughtBeforePremiumExpires","Confirmed","docs/stories/US-008-premium-entitlement-expiration/overview.md:5; docs/decisions/0011-premium-entitlement-expiration.md:11-13; PaymentWalletServiceTest.java:343","9cb4e06","Đã đổi source of truth sang premium_expired_at."),
  D("DEF-006","US-011","Public marketplace","Guest bị 401 khi xem milestone của job OPEN","Service cho phép đọc milestone của job OPEN nhưng Spring Security chặn request anonymous trước khi tới service.","1. Không gửi JWT.\n2. Gọi GET /api/v1/jobs/{openJobId}/milestones.\n3. Quan sát HTTP status.","Guest nhận 200 và danh sách milestone của job OPEN.","Request bị chặn 401/403.","Medium","High","Fixed","Nguyen Trong Hieu","2026-06-21","PublicJobRouteMatcherTest.jobMilestonesMatcher_matchesNumericJobIdOnly","Confirmed","docs/stories/US-011-public-job-milestones/overview.md:5; docs/decisions/0012-public-open-job-milestones.md:14; PublicJobRouteMatcherTest.java:30","aef2aac","Compile/story verify pass; chưa có runtime anonymous E2E trong story."),
  D("DEF-007","US-017","Public profile","Guest bị chặn khi xem Business public profile","Hai lớp gate route/service từng yêu cầu role khiến endpoint public business profile không dùng được cho guest.","1. Không gửi JWT.\n2. Gọi GET /api/v1/profiles/business/{businessId}.\n3. Quan sát response.","Guest đọc được public profile bằng businessId; /me vẫn protected.","Guest nhận 401/403 dù đây là public profile.","Medium","High","Fixed","Nguyen Trong Hieu","2026-06-22","BusinessProfileRouteMatcherTest.matches_numericBusinessId; ProfileServiceTest.businessProfileById_shouldNotRequireRoleForGuestAccess","Confirmed","docs/decisions/0015-business-public-profile-guest-access.md:16; docs/stories/US-017-public-business-profile/validation.md:19; ProfileServiceTest.java:251","0f96197","Unit/route proof có; story ghi runtime anonymous 200 còn hạn chế."),
  D("DEF-008","US-018","Public profile by job","Guest bị chặn khi xem Business của job OPEN","Endpoint business-by-job có service rule cho OPEN job nhưng Security từng chặn anonymous ở route.","1. Không gửi JWT.\n2. Gọi GET /api/v1/profiles/business/by-job/{openJobId}.\n3. Quan sát response.","Guest nhận Business profile khi job OPEN; job không OPEN vẫn bị gate.","Anonymous bị 401/403 trước khi kiểm tra trạng thái job.","Medium","High","Fixed","Nguyen Trong Hieu","2026-06-22","BusinessProfileRouteMatcherTest.byJobMatcher_matchesNumericJobId; ProfileServiceTest.businessProfileByJob_shouldReturnProfileWhenJobOpen","Confirmed","docs/decisions/0016-public-business-by-job-guest-access.md:17; ProfileServiceTest.java:267","0f96197","Route matcher và service test đã có."),
  D("DEF-009","US-012","Contract milestone view","Danh sách contract milestone hiển thị status snapshot đã cũ","API từng trả ContractMilestoneEntity trực tiếp nên status hiển thị không theo live milestones sau submit/complete/SLA.","1. Tạo contract milestone.\n2. Submit deliverable để live milestone thành UNDER_REVIEW.\n3. Gọi GET /api/v1/contracts/{id}/milestones.","Status hiển thị lấy từ live milestone; nếu link mất thì fallback snapshot và không crash.","API trả status snapshot cũ hoặc có thể lỗi khi linked milestone thiếu.","High","High","Closed","Nguyen Trong Hieu","2026-06-22","ContractExecutionServiceTest.listMilestonesByContract_shouldUseLiveMilestoneStatusAfterDeliverableSubmission","Confirmed","docs/stories/US-012-contract-milestone-live-status.md:13-34; ContractExecutionServiceTest.java:380,469","c7fb789","Focused test pass."),
  D("DEF-010","US-013","Contract milestone snapshot","Duration contract milestone không ổn định theo snapshot","Contract view thiếu duration snapshot hoặc có thể phản ánh duration nguồn sau khi contract đã ký.","1. Tạo contract từ milestone có duration.\n2. Sửa duration milestone nguồn.\n3. Đọc contract milestones.","Duration trong contract giữ nguyên giá trị snapshot lúc tạo.","Giá trị duration thiếu hoặc thay đổi theo milestone nguồn.","Medium","Medium","Closed","Nguyen Trong Hieu","2026-06-22","ContractExecutionServiceTest.listMilestonesByContract_shouldKeepDurationSnapshotStableWhenMilestoneDurationEdited","Confirmed","docs/stories/US-013-milestone-duration.md; ContractExecutionServiceTest.java:689,1155","c7fb789","Regression test khóa snapshot stability."),
  D("DEF-011","US-014","Notification routing","Thông báo deliverable không mở đúng workspace/milestone","Notification cũ thiếu metadata và targetUrl chính xác tới contract workspace, khiến FE không xác định đúng milestone/deliverable.","1. Expert submit deliverable.\n2. Business nhận notification.\n3. Mở targetUrl và kiểm tra metadata.","targetUrl=/contracts/{contractId}/workspace?milestoneId={milestoneId}; metadata có contractId, milestoneId, deliverableId.","Route/metadata thiếu hoặc không đủ context để FE điều hướng chính xác.","Medium","High","Closed","Nguyễn Thiên Trường","2026-06-22","NotificationServiceTest.notifyDeliverableSubmitted_shouldBuildTargetUrlWithContractAndMilestone","Confirmed","docs/stories/US-014-deliverable-notification-metadata.md:13-31; NotificationServiceTest.java:83,102","bed3504","Focused ContractExecutionServiceTest 23 tests pass."),
  D("DEF-012","US-015","Contract acceptance snapshot","Tiêu chí/đầu ra contract thay đổi theo milestone nguồn","Contract milestone chưa lưu criteria_snapshot và deliverable_expectation nên nội dung đã thống nhất có thể trôi theo dữ liệu nguồn.","1. Tạo contract từ milestone có criteria/description.\n2. Sửa milestone hoặc acceptance criteria nguồn.\n3. Đọc contract milestone.","Snapshot criteria và deliverable expectation giữ nguyên sau khi contract tạo.","Snapshot thiếu hoặc thay đổi theo source milestone.","High","High","Closed","Nguyen Trong Hieu","2026-06-22","ContractExecutionServiceTest.listMilestonesByContract_shouldKeepSnapshotStableWhenMilestoneDescriptionChanges","Confirmed","docs/stories/US-015-contract-milestone-snapshot-fields.md:13-35; ContractExecutionServiceTest.java:497,536,571","c7fb789","Migration nullable và focused test 25/25 pass."),
  D("DEF-013","US-022","Draft job editing","Sửa draft làm SoW không được lưu và publish báo JOB_MUST_HAVE_AI_SOW","Không có draft update path persist lại job+SoW+milestones; client chỉnh sửa nhưng server vẫn giữ/thiếu SoW.","1. Tạo draft job có SoW.\n2. Chỉnh nội dung SoW/milestones.\n3. Publish draft.","Update upsert SoW và milestones trong một transaction; publish thành công khi SoW thực sự tồn tại.","Publish có thể fail JOB_MUST_HAVE_AI_SOW hoặc dùng dữ liệu create-time cũ.","High","Urgent","Closed","Nguyen Trong Hieu","2026-06-23","MarketplaceServiceTest.updateDraftJob_shouldUpsertSowAndReplaceMilestonesForDraftJob","Confirmed","docs/stories/US-022-preserve-sow-on-draft-edit/overview.md:5-25; MarketplaceServiceTest.java:112,152","4d5e3a3","Commit message xác nhận lỗi không đăng được bài khi update."),
  D("DEF-014","US-024","AI SoW generation","SoW sinh dư và lặp lại phần milestone guidance","Model có thể chèn Recommended milestones vào các trường SoW trong khi response đã có milestones riêng.","1. Gọi AI tạo SoW.\n2. Cho model trả thêm khối Recommended milestones trong overview/scope.\n3. Kiểm tra response.","Milestone chỉ xuất hiện ở cấu trúc milestones; text SoW không lặp guidance.","Overview/scope chứa thêm khối milestone trùng lặp.","Medium","High","Closed","Nguyen Trong Hieu","2026-06-23","AiSowGenerationServiceTest.parseAiResponse_shouldStripRecommendedMilestonesBlockFromAllSowFields","Confirmed","trace #32; AiSowGenerationServiceTest.java:149,203,280,328; commit message 'sửa lỗi gen lỗi dư milestone sow'","6bf0120; trace #32","Prompt guard và post-parse strip đều có test."),
  D("DEF-015","US-AUTH-001, US-AUTH-002","Authentication security","Login cho phép thử sai mật khẩu không giới hạn","Backend trước V43 không theo dõi failed attempts/locked_until/lock_reason nên không có brute-force protection.","1. Gọi login liên tục với mật khẩu sai.\n2. Lặp quá 5 lần và thêm chu kỳ thứ hai.\n3. Kiểm tra trạng thái account.","Khóa tạm sau 5 lần; khóa bảo mật sau chu kỳ thứ hai; login bị chặn đúng thời gian.","Có thể thử sai không giới hạn, account không bị lock.","Critical","Urgent","Closed","Nguyen Trong Hieu","2026-06-26","AuthServiceImplTest.fifthWrongPassword_shouldTriggerTemporaryLockout; secondWrongPasswordCycle_shouldSecurityLockAccount","Confirmed","docs/decisions/0018-auth-lockout-password-reset.md:11-18; AuthServiceImplTest.java:118,171,192","e819580; e359638","Full auth validation 23 focused và 147 full tests pass."),
  D("DEF-016","US-AUTH-003, US-AUTH-004","Password recovery","Người dùng quên mật khẩu không có self-service recovery","Không có forgot/reset password flow; người dùng bị khóa/quên mật khẩu không tự khôi phục được.","1. Tại login chọn quên mật khẩu.\n2. Yêu cầu reset bằng email.\n3. Thử dùng token đổi mật khẩu.","Email reset được gửi an toàn; token có hạn/dùng một lần; reset thành công và chỉ mở khóa TOO_MANY_FAILED.","Không có endpoint/token recovery; user phải nhờ hỗ trợ.","High","High","Closed","Nguyen Trong Hieu","2026-06-26","AuthServiceImplTest.forgotPassword_withExistingEmail_shouldReturnSuccess; resetPassword_withValidToken_shouldSucceed","Confirmed","docs/decisions/0018-auth-lockout-password-reset.md:11; AuthServiceImplTest.java:318,374,392,514","e359638","Có test token concurrency/rollback/email failure."),
  D("DEF-017","US-030","Realtime notification","Thiếu thông báo realtime cho finance/quota/account events","Một số sự kiện wallet top-up, quota, withdrawal và account không tạo notification realtime nên người dùng/Admin không thấy cập nhật.","1. Thực hiện các finance/account event thuộc US-030.\n2. Theo dõi websocket/notification inbox.\n3. Đối chiếu từng event.","Mỗi event được yêu cầu tạo notification đúng recipient, target và tiếng Việt có dấu.","Không có notification cho một hoặc nhiều event.","Medium","High","Closed","Nguyễn Thiên Trường","2026-06-26","NotificationServiceTest.financeAndAccountNotifications_shouldUseVietnameseDiacritics","Confirmed","docs/stories/US-030-missing-realtime-notifications/story.md; NotificationServiceTest.java:160-185","7b5175f","Focused và full suite được ghi nhận pass trong story."),
  D("DEF-018","US-031","Audit log display","Audit log Admin lộ path kỹ thuật và raw numeric ID","Response hiển thị /api/... #id, table names và action không dấu thay vì đối tượng nghiệp vụ dễ hiểu.","1. Tạo audit cho profile/job/proposal/wallet.\n2. Admin gọi API audit list.\n3. Quan sát entityName/entityId/action.","UI fields là tiếng Việt và tên business/expert/job/contract; raw fields chỉ dành debug.","Admin thấy path kỹ thuật, table name và numeric IDs.","Medium","High","Closed","Nguyễn Thiên Trường","2026-06-26","AuditLogServiceTest.listForAdmin_shouldRenderBusinessProfileWithoutRawId","Confirmed","docs/stories/US-031-clean-audit-log-display/overview.md:5-17; AuditLogServiceTest.java:89,116,149","b1b4567","Focused clean test và full suite pass."),
  D("DEF-019","US-033","Wallet history","Lịch sử ví chỉ trả raw ledger code, thiếu context giao dịch","API trả WalletTransactionEntity nên FE không biết ai nạp/rút, contract/job nào, bank account hoặc lý do fail.","1. Tạo deposit/withdraw/contract ledger entries.\n2. Gọi GET /api/wallet/transactions.\n3. Đọc title/description/context.","DTO hiển thị sự kiện tiếng Việt, đủ actor/contract/job/bank/reason và vẫn giữ raw code để debug.","Chỉ có code/reference raw, không thể giải thích giao dịch cho user.","Medium","High","Closed","Nguyễn Thiên Trường","2026-06-26","PaymentWalletServiceTest.listCurrentWalletTransactions_shouldReturnTransparentContractDepositHistory","Confirmed","docs/stories/US-033-transparent-wallet-transaction-history/overview.md:5-21; PaymentWalletServiceTest.java:647","95f2552","Focused 49 và full 137 tests pass."),
  D("DEF-020","US-041","Public API contract","Swagger/public controller chứa route alias trùng và endpoint legacy/support","Cùng nghiệp vụ contract/dispute/escrow được expose bằng nhiều alias cùng route nội bộ, làm API surface mơ hồ và dễ gọi nhầm.","1. Mở runtime /v3/api-docs.\n2. Lọc Contract Execution Flow.\n3. So sánh các action tương đương/legacy/support.","Mỗi public action chỉ có một v1 route; support/system routes không nằm trong public flow.","Swagger có duplicate aliases và legacy routes.","Medium","Medium","Closed","Nguyễn Thiên Trường","2026-07-04","OpenAPI route inventory + ContractExecutionServiceTest full regression","Confirmed","docs/stories/US-041-contract-api-surface-cleanup/overview.md:5-16; validation.md:56","f10e54a","Full 195 tests pass và runtime OpenAPI đã loại route trùng."),
  D("DEF-021","US-042","Withdrawal history","Một withdrawal hiển thị nhiều dòng ledger trùng lặp","DEBIT/HOLD/RELEASE/CREDIT nội bộ từng được trả thẳng nên một yêu cầu rút tiền xuất hiện nhiều lần và gây hiểu nhầm.","1. Tạo withdrawal PENDING.\n2. Gọi GET /api/wallet/transactions.\n3. Approve/Reject rồi gọi lại.","Mỗi withdrawalId chỉ có một dòng đại diện theo status hiện tại.","Một withdrawal xuất hiện nhiều raw ledger rows.","Medium","High","Closed","Nguyễn Thiên Trường","2026-07-03","PaymentWalletServiceTest.listCurrentWalletTransactions_shouldCollapsePendingWithdrawalLedgerEntries","Confirmed","docs/stories/US-042-withdrawal-history-dedup.md:13-40,79-95; PaymentWalletServiceTest.java:770,825,890","433f674; trace #58","Focused 18 và full 194 tests pass; trace #57 bị supersede sau regression."),
  D("DEF-022","US-046","KYB tax verification","Resubmit MST của cùng account bị báo duplicate","Duplicate check trước đây không loại trừ chính account đang cập nhật, nên cùng một Business resubmit MST hợp lệ cũng bị conflict.","1. Business đã có MST hợp lệ.\n2. Resubmit/update profile với đúng MST cũ.\n3. Quan sát validation.","Cùng account được resubmit; chỉ MST thuộc account khác mới bị duplicate.","Hệ thống báo duplicate MST cho chính chủ sở hữu.","Medium","High","Closed","Nguyen Trong Hieu","2026-07-10","ProfileServiceTest.upsertBusiness_shouldAllowResubmitSameAccount","Confirmed","docs/flows/tax-code-verification-flow.md:40; docs/stories/US-046-kyb-tax-code-verification-hardening/validation.md:20; ProfileServiceTest.java:681","4def411","AC7 pass."),
  D("DEF-023","US-049","Admin analytics","openDisputes luôn sai do lọc status legacy","AdminService.analyticsOverview dùng 'Open'/'UnderReview' không khớp constants active của DisputeEntity.","1. Tạo dispute PENDING_SELF_RESOLVE/ESCALATION_REQUESTED/STAFF_REVIEWING.\n2. Gọi Admin analytics overview.\n3. Kiểm tra openDisputes.","Đếm tất cả active dispute constants theo domain model.","Active disputes bị bỏ sót, số openDisputes thấp/sai.","High","High","Closed","hieunt1504","2026-07-11","AdminDashboardServiceTest.disputes_shouldReturnOpenAndOverdueSlaCounts","Confirmed","docs/stories/US-049-admin-dispute-settlement-dashboard/overview.md:13-17; docs/decisions/0021-admin-dispute-dashboard.md:38-39; AdminDashboardServiceTest.java:167","efb706f","Full Maven suite 252 tests pass theo flow doc."),
  D("DEF-024","US-052","Progress reporting","Feedback báo cáo tiến độ bị mất khỏi API/entity","DB còn cột feedback từ V51 nhưng Java entity/API chỉ acknowledge và không cho Business lưu feedback có cấu trúc.","1. Expert submit progress report.\n2. Business acknowledge và muốn gửi feedback.\n3. Kiểm tra request/response và DB.","Feedback text/category/severity/DoD/adjustment được lưu; đồng thời ack report nếu pending.","API/entity không expose feedback dù schema đã có.","Medium","High","Closed","Nguyễn Thiên Trường","2026-07-11","ContractExecutionServiceTest.feedbackProgressReport_shouldStoreFeedbackAcknowledgeAndNotify","Confirmed","docs/stories/US-052-restore-business-progress-report-feedback/overview.md:5-25; ContractExecutionServiceTest.java:1944,2009","64417af","Chức năng được khôi phục và regression tests pass."),
  D("DEF-025","US-049, US-054","Settlement notification","Admin settlement report có thể gửi trước commit hoặc bị duplicate khi replay/race","Notification settlement thiếu AFTER_COMMIT/idempotency durable nên rollback có thể vẫn báo, replay/race có thể tạo bản ghi trùng.","1. Thực hiện settlement rồi rollback hoặc replay event.\n2. Mô phỏng duplicate-key race cho một Admin.\n3. Kiểm tra notifications của tất cả Admin.","Chỉ gửi sau commit; mỗi dispute/Admin đúng một notification; duplicate một recipient không chặn recipient khác.","Rollback vẫn có thể phát notification hoặc replay tạo duplicate/chặn fan-out.","High","Urgent","Closed","hieunt1504","2026-07-12","DisputeSettlementNotificationTest.rolledBackSettlement_shouldNotNotifyAdmins; NotificationServiceTest.notifyAdminDisputeSettlementReported_replayedEventShouldNotPersistOrPushAgain","Confirmed","docs/stories/US-054-post-commit-dispute-settlement-report/overview.md:5-23; DisputeSettlementNotificationTest.java:38-65; NotificationServiceTest.java:34-58","01d1005","Focused 83, story 3, integration 4 và full 287 tests pass."),
  D("DEF-026","US-055","Wallet ledger","Escrow/deposit thiếu operation identity và history bị nhân đôi","Financial writers không có operationKey/operationLeg; retry và same-wallet multi-leg thiếu idempotency, history hiển thị từng leg.","1. Thực hiện escrow/deposit nhiều leg.\n2. Retry cùng logical operation.\n3. Gọi user wallet history.","Writer retry-safe, atomic; user history một logical item; Admin vẫn thấy raw ledger.","Có nguy cơ duplicate leg và user thấy nhiều dòng cho một action.","Critical","Urgent","Closed","hieunt1504","2026-07-12","WalletLedgerServiceTest.holdEscrowFromAvailable_shouldReturnExistingOperationWithoutMutatingWallet; PaymentWalletServiceTest.listCurrentWalletTransactions_shouldCollapseOperationKeyEscrowDepositIntoSingleHistoryRow","Confirmed","docs/stories/US-055-wallet-ledger-idempotency-and-escrow-history/overview.md:5-17; WalletLedgerServiceTest.java:70; PaymentWalletServiceTest.java:718","01d1005","Migration/unique operation semantics và full regression đã được ghi nhận."),
  D("DEF-027","US-060","Expert recommendation","Regenerate recommendation ném unique-constraint exception","Lần generate thứ hai insert trước khi loại dữ liệu cũ/đồng bộ persistence, vi phạm uq_expert_recommendations_job_expert.","1. Generate recommendation cho một job.\n2. Thay candidate/rating rồi generate lại.\n3. Quan sát persistence.","Regeneration cập nhật Top 5, không vi phạm unique constraint và có thể thêm expert phù hợp hơn.","DataIntegrityViolationException ở lần regeneration thứ hai.","High","Urgent","Closed","Hoàng Tiến Dũng","2026-07-16","ExpertRecommendationRegenerationIntegrationTest.regenerate_shouldAddNewHigherCompatibilityExpertToTopRecommendations","Confirmed","Harness trace #120 tái hiện DataIntegrityViolationException; ExpertRecommendationRegenerationIntegrationTest.java:66; commit sửa danh sách duplicated","fdc064f; trace #120-#122","PostgreSQL integration test pass sau sửa."),
  D("DEF-028","US-060","Recommendation authority","LLM có thể ghi đè rank/score/evidence và regeneration reset lựa chọn Business","Current behavior cho phép output AI thay identity/ranking fields và reset business_selected, trái backend-authoritative contract.","1. Tạo backend ranking và chọn một expert.\n2. Cho AI trả rank/score/expert giả hoặc generate lại.\n3. Đọc persisted recommendations.","Backend giữ expertId/portfolio/rank/score/evidence; AI chỉ thêm reason; lựa chọn hợp lệ được giữ.","AI output có thể thay dữ liệu quyết định hoặc business_selected về false.","Critical","Urgent","Closed","Nguyen Trong Hieu","2026-07-14","ExpertRecommendationServiceTest.generateRecommendations_whenAiReturnsJson_shouldKeepBackendIdentityScoreRankAndEvidence; generateRecommendations_shouldPinAndPreserveExistingBusinessSelection","Confirmed","docs/stories/US-060-ai-expert-recommendation-relevance-hardening/overview.md:5-13; ExpertRecommendationServiceTest.java:106,237,265","a11d1f2; trace #106","Focused/integration/full fresh-DB proof đã pass."),
  D("DEF-029","US-060","AI structured output fallback","Fallback AI recommendation có thể lỗi do response schema không chặt","Request fallback/parse từng không ép schema phù hợp nên provider hoặc parser trả shape sai và generation fail thay vì dùng rule-based ranking.","1. Bật OpenAI recommendation.\n2. Cho provider trả expertId dạng string/blank reason/shape không hợp lệ.\n3. Generate recommendations.","Strict json_schema; backend validate và fallback rule-based Top 5 khi AI invalid.","AI generation lỗi hoặc dữ liệu invalid đi qua parser.","High","High","Closed","Nguyen Trong Hieu","2026-07-16","ExpertRecommendationServiceTest.generateRecommendations_whenAiReturnsExpertIdAsString_shouldUseBackendFallback","Confirmed","ExpertRecommendationServiceTest.java:155-157,187,212; commit 'ep schema AI recommend khong gen loi'","3b02572","Có test strict json_schema và invalid AI fallback."),
  D("DEF-030","US-062","Staff dispute routing","Dispute lặp lại dồn vào một Staff và có race workload","Routing cũ ưu tiên chuyên môn nhưng không cân workload/capacity và không khóa candidate rows, nên các request đồng thời chọn từ workload stale.","1. Chuẩn bị hai Staff đủ điều kiện ngang nhau.\n2. Route 6 dispute giống nhau hoặc route đồng thời.\n3. Đếm assignment/capacity.","Phân phối 3/3, không vượt capacity, lock theo thứ tự xác định; không có người thì giữ ESCALATION_REQUESTED.","Nhiều dispute dồn cùng Staff hoặc vượt capacity do stale read.","High","High","Closed","Nguyen Trong Hieu","2026-07-15","StaffDisputeInboxRoutingTest.routeDispute_balancesRepeatedAssignmentsAcrossEquallyQualifiedStaff","Confirmed","docs/stories/US-062-balanced-staff-dispute-routing/overview.md:9-25; StaffDisputeInboxRoutingTest.java:216,261","da39ee0","Focused 96 và full 351 tests pass; PostgreSQL indexes verified."),
  D("DEF-031","US-063","Milestone deadline","Expert vẫn submit deliverable/source ZIP sau deadline","Luồng cũ chấp nhận submit/resubmit khi milestone IN_PROGRESS hoặc OVERDUE và phụ thuộc Admin đánh dấu overdue.","1. Tạo contract milestone đã quá in_progress_started_at + duration.\n2. Giữ status IN_PROGRESS hoặc OVERDUE.\n3. Submit deliverable/upload ZIP.","Reject cả final deliverable và ZIP sau deadline; không tạo side effect.","Submission vẫn được chấp nhận sau hạn.","Critical","Urgent","Closed","Nguyen Trong Hieu","2026-07-15","ContractExecutionServiceTest.submitDeliverable_shouldRejectLateResubmissionEvenWhenStatusIsStillInProgress; uploadMilestoneSourceCode_shouldRejectAfterDeadlineWithoutUploading","Confirmed","docs/decisions/0034-block-overdue-milestone-deliverables.md:12-27; ContractExecutionServiceTest.java:988,1026,1120","88f9c6d; trace #117-#118","Focused 79, full 354 và story verify pass."),
  D("DEF-032","US-064","Platform revenue","Admin totalRevenue bỏ sót membership và retail-credit revenue","Purchase debit user và cấp quyền nhưng không credit platform; syncWallet chỉ cộng commission contract.","1. Mua membership/job-post/proposal credits thành công.\n2. Chạy SystemWalletService.syncWallet.\n3. Đọc Admin totalRevenue.","Platform ledger có revenue credit replay-safe và totalRevenue gồm commission + package + retail credits.","Package/credit revenue không xuất hiện trong platform wallet/totalRevenue.","Critical","Urgent","Closed","hieunt1504","2026-07-17","SystemWalletServiceTest.syncWallet_shouldIncludeCommissionMembershipAndCreditRevenue","Confirmed","docs/stories/US-064-platform-purchase-revenue-ledger/overview.md:5-20; docs/decisions/0035-platform-purchase-revenue-ledger.md:12-14; SystemWalletServiceTest.java:47","6533970; trace #126","Focused 36, integration aggregate và full 365 tests pass."),
  D("DEF-033","US-065","Profile review state","Profile Approved/Pending có thể resubmit và quay lại Pending","Upsert route dùng chung cho submit/update nên Approved bị reopen review, Pending nhận submit lặp; Staff có thể quyết định lại profile đã final.","1. Submit profile đến Pending hoặc approve profile.\n2. Gọi upsert lại; Staff gọi approve/reject lần nữa.\n3. Kiểm tra reviewStatus.","Pending immutable; Rejected được resubmit; Approved update không reopen; Staff chỉ review Pending.","Approved có thể về Pending và final review bị lặp.","High","Urgent","Closed","Nguyễn Thiên Trường","2026-07-17","ProfileServiceTest.upsertBusiness_shouldRejectResubmitWhilePending; upsertBusiness_shouldUpdateApprovedProfileWithoutReopeningReview; approveProfile_shouldRejectAlreadyApprovedBusinessProfile","Confirmed","docs/stories/US-065-profile-audit-settings-hardening/overview.md:5-15; ProfileServiceTest.java:729,756,796","0a4593d","Focused 46 và full 370 tests pass."),
  D("DEF-034","US-065","Payment audit","Thiếu audit terminal top-up và PayOS sync fallback gây log spam","Top-up success/failure không có product audit rõ; fallback filter ghi lặp mỗi sync dù payment state không đổi.","1. Sync cùng PayOS order nhiều lần, không đổi state.\n2. Cho order chuyển terminal success/failure.\n3. Đọc audit logs.","Chỉ ghi một audit theo terminal status transition; route sync lặp không spam fallback.","Không có terminal event rõ hoặc nhiều audit sync trùng.","Medium","High","Closed","Nguyễn Thiên Trường","2026-07-17","AuditLogServiceTest regression suite; PayOS sync audit transition tests in profile/audit hardening proof","Confirmed","docs/decisions/0036-profile-audit-settings-hardening.md:11-15; docs/stories/US-065-profile-audit-settings-hardening/overview.md:5-15","0a4593d","Full suite 370/370 pass theo story evidence."),
  D("DEF-035","US-065","Admin settings","Admin settings liệt kê key seed/demo không điều khiển backend","Settings API hiển thị các key demo/seed không được flow runtime sử dụng, gây hiểu nhầm là operational controls.","1. Admin gọi list settings.\n2. Đối chiếu từng key với code sử dụng.\n3. Thay key demo và quan sát runtime.","Chỉ expose allowlist operational keys thực sự được backend consume.","Key inactive/demo vẫn hiện và thay đổi không có tác dụng.","Low","Medium","Closed","Nguyễn Thiên Trường","2026-07-17","AdminServiceTest.createSetting_shouldRejectUnsupportedSettingKey","Confirmed","docs/decisions/0036-profile-audit-settings-hardening.md:11-15; AdminServiceTest.java:142; V63__profile_audit_settings_hardening.sql","0a4593d","Obsolete seed settings được soft-disable."),
  D("DEF-036","US-061","Source ZIP upload","Giới hạn multipart 10MB mâu thuẫn contract ZIP 50MB","Service/docs cho phép ZIP đến 50MB nhưng Spring multipart mặc định max-file/max-request vẫn 10MB nếu env không override; request 10-50MB có thể bị chặn trước validation service.","1. Không set MAX_UPLOAD_FILE_SIZE/MAX_UPLOAD_REQUEST_SIZE.\n2. Upload ZIP hợp lệ 20MB.\n3. Quan sát request trước FirebaseStorageService.","ZIP hợp lệ đến 50MB đi qua multipart và được service kiểm tra.","Có khả năng request bị reject ở 10MB mặc dù docs/service công bố 50MB.","Medium","High","Open","Unknown",null,"Chưa có test tích hợp multipart 10-50MB","Potential","src/main/resources/application.properties:22-23 default 10MB; FirebaseStorageService.java:29,71 cap 50MB; docs/product/contract-management.md:81; Harness trace #119","trace #119","Potential vì chưa có log/test upload 10-50MB trong repo; cần runtime reproduction."),
];

const P = (id, us, module, summary, description, steps, expected, risk, severity, priority, tc, evidence, note = "Suy luận từ Acceptance Criteria/source boundary; chưa có failure hoặc runtime reproduction được lưu trong project.") =>
  D(id, us, module, summary, description, steps, expected, risk, severity, priority, "Open", "Unknown", null, tc, "Potential", evidence, "", note);

defects.push(
  P("DEF-037","US-003","PayOS payment sync","Webhook và sync cùng xác nhận một payment order","Story yêu cầu bỏ public webhook và chỉ dùng active sync; nếu một phiên bản cũ vẫn expose cả hai đường xác nhận, cùng order có thể được xử lý hai lần.","1. Tạo PayOS top-up order.\n2. Gửi webhook provider và gọi sync gần đồng thời.\n3. Kiểm tra payment/wallet ledger.","Một order chỉ được xác nhận và cộng ví đúng một lần qua đường sync có kiểm soát.","Chưa có reproduction; rủi ro là hai confirmation path cùng ghi trạng thái hoặc cộng tiền lặp.","Critical","Urgent","POT-TC-001 (chưa chạy)","docs/stories/US-003-payos-sync-payment-flow/overview.md; docs/stories/US-003-payos-sync-payment-flow/design.md"),
  P("DEF-038","US-007","Contract API compatibility","Client hoặc tài liệu vẫn gọi endpoint change-request đã vô hiệu","US-007 loại bỏ endpoint/DTO change-request. Không có bằng chứng toàn bộ client ngoài repo đã bỏ route cũ.","1. Dùng client/spec cũ.\n2. Gọi POST /api/v1/contracts/change-requests.\n3. So sánh response với tài liệu/runtime hiện tại.","Client và tài liệu không còn phụ thuộc endpoint đã vô hiệu; API trả lỗi deprecation/404 nhất quán.","Chưa có client inventory; có thể còn caller nhận 404 hoặc hiểu sai lifecycle.","Medium","Medium","POT-TC-002 (chưa chạy)","docs/stories/US-007-remove-disabled-change-request-endpoint.md; git history removal df5deff"),
  P("DEF-039","US-009, US-010","Public profile privacy","Public profile có thể lộ contact field hoặc áp role gate không nhất quán","Các story public profile thay đổi authorization/DTO qua nhiều đợt; chưa có E2E privacy matrix cho guest trên toàn bộ Business/Expert fields.","1. Không gửi JWT.\n2. Gọi public Business và Expert profile endpoints.\n3. Kiểm tra email/phone/license/rejectionReason và HTTP status.","Chỉ public fields được expose; protected fields và /me vẫn yêu cầu auth; route Business/Expert nhất quán.","Chưa có full E2E matrix; có nguy cơ lộ contact nhạy cảm hoặc chặn nhầm guest.","High","High","POT-TC-003 (chưa chạy)","docs/stories/US-009-business-public-profile.md; docs/stories/US-010-expert-public-profile.md; docs/stories/US-017-public-business-profile/validation.md"),
  P("DEF-040","US-016","Regression coverage","Thiếu regression có thể tái xuất hiện status drift và FE routing sai","US-016 được tạo để khóa các fix US-012 đến US-015, cho thấy trước đó có khoảng trống test ở contract milestone/notification.","1. Thay đổi mapping contract milestone hoặc notification.\n2. Chạy focused tests thiếu các boundary liên quan.\n3. Kiểm tra live status, snapshot và targetUrl.","Regression suite phát hiện mọi status drift, snapshot mutation và route notification sai.","Chưa có failure mới; rủi ro là boundary chưa được test đầy đủ bị tái phát.","Medium","Medium","POT-TC-004 (test-gap review)","docs/stories/US-016-regression-tests-contract-milestone-notification.md:13-15,93-103"),
  P("DEF-041","US-020","Job and milestone validation","Duration invalid hoặc tổng milestone vượt duration job vẫn được lưu","Story yêu cầu validate duration/unit và tổng duration. Không có artifact failure lịch sử chứng minh mọi API mutation đều áp cùng rule.","1. Tạo job với duration thiếu unit/âm.\n2. Cho tổng duration milestones lớn hơn job.\n3. Thử create và update.","Reject input invalid ở mọi create/update path, không persist dữ liệu một phần.","Chưa có reproduction; có thể một mutation path bỏ qua validation và lưu schedule không hợp lệ.","High","High","POT-TC-005 (chưa chạy toàn API)","docs/stories/US-020-job-and-milestone-duration-validation/overview.md; MarketplaceServiceTest.java:249-292; ContractExecutionServiceTest.java:638-672"),
  P("DEF-042","US-021","Profile review notification","Profile submission có thể thông báo nhầm toàn bộ Staff","Story yêu cầu notification cho Staff phù hợp; routing domain PROFILE_REVIEW được bổ sung sau đó nên phiên bản trung gian có thể broadcast hoặc bỏ sót reviewer.","1. Tạo Staff có/không có PROFILE_REVIEW domain.\n2. Submit Business/Expert profile.\n3. Kiểm tra notification recipients.","Chỉ approved Staff có specialization PROFILE_REVIEW nhận thông báo.","Chưa có production evidence; rủi ro broadcast cho Staff không liên quan hoặc không gửi cho reviewer hợp lệ.","Medium","High","POT-TC-006 (chưa chạy integration)","docs/stories/US-021-profile-verification-staff-notifications-and-recommendation-seed-data/overview.md; ProfileServiceTest.java:90,143"),
  P("DEF-043","US-023","Membership pricing","Giá membership trong seed DB và fallback code có thể không đồng nhất","US-023 cập nhật giá bằng migration; các môi trường bỏ migration hoặc dùng fallback cũ có thể hiển thị/trừ số credit khác nhau.","1. Khởi tạo DB cũ và DB fresh.\n2. Đọc membership package prices.\n3. Mua package khi setting/row thiếu để kích hoạt fallback.","Giá hiển thị, số tiền debit và fallback đều khớp bảng giá US-023.","Chưa có cross-environment reproduction; có thể lệch giá giữa DB, fallback và docs.","High","High","POT-TC-007 (chưa chạy multi-version DB)","docs/stories/US-023-update-membership-package-prices.md; V38__update_membership_package_prices.sql"),
  P("DEF-044","US-025","OpenAPI security contract","Swagger security metadata có thể lệch SecurityConfig/runtime","US-025 đồng bộ inventory và public/protected routes, nhưng static OpenAPI có thể drift sau controller/security changes.","1. Lấy runtime /v3/api-docs.\n2. So với docs/openapi/openapi-v1.json và SecurityConfig.\n3. Kiểm tra security requirement từng route.","Runtime, static spec và SecurityConfig thống nhất public/protected behavior.","Chưa có drift scan tại mọi commit; client có thể gửi/không gửi JWT sai theo Swagger.","Medium","Medium","POT-TC-008 (contract drift scan)","docs/stories/US-025-sync-swagger-api-inventory-and-db-reference.md; docs/openapi/openapi-v1.json; SecurityConfig.java"),
  P("DEF-045","US-026","Retail credit pricing","Retail credit setting và fallback price có thể trừ tiền khác nhau","US-026 nhấn mạnh runtime setting phải khớp code fallback; nếu setting thiếu/stale, giao dịch có thể dùng giá khác tài liệu.","1. Xóa/deactivate retail price setting.\n2. Mua job-post/proposal credits.\n3. So sánh debit với price được công bố.","Fallback arithmetic và runtime setting cho cùng đơn giá.","Chưa có environment failure; có khả năng giá fallback khác DB setting.","High","High","POT-TC-009 (chưa chạy config-missing integration)","docs/stories/US-026-update-retail-credit-prices.md:18,57; PaymentWalletServiceTest.java:213-235"),
  P("DEF-046","US-027","Recommendation demo data","Seed demo cũ làm sai kết quả đánh giá recommendation","US-027 dọn recommendation seed, cho thấy dữ liệu demo cũ có thể tạo candidate/job/profile không còn hợp lệ và làm benchmark/manual QA sai lệch.","1. Chạy migrations trên DB đã seed nhiều phiên bản.\n2. Generate recommendation cho demo jobs.\n3. So sánh candidate với fresh DB.","Seed idempotent, chỉ chứa dữ liệu hợp lệ và không làm sai ranking/eligibility.","Chưa có failure cụ thể; long-lived DB có thể giữ duplicate/stale demo records.","Medium","Medium","POT-TC-010 (fresh-vs-long-lived DB)","docs/stories/US-027-clean-recommendation-demo-seed-data/overview.md; V39__seed_recommendation_demo_profiles_jobs_proposals.sql"),
  P("DEF-047","US-028","Swagger documentation","Thứ tự Swagger và test guide có thể lệch runtime business flow","Swagger tag order, overview và test guide được sinh/cập nhật ở nhiều commit; không có đảm bảo tự động mọi lần route thay đổi.","1. Chạy app và lấy runtime OpenAPI.\n2. So tag/operation order với overview/test guide.\n3. Tìm route thiếu/thừa/sai thứ tự.","Cả ba nguồn có cùng inventory và business-flow ordering.","Chưa có failure hiện tại; static docs có thể drift sau feature mới.","Low","Medium","POT-TC-011 (inventory parity scan)","docs/stories/US-028-swagger-flow-order-and-response-guide/overview.md; docs/swagger-api-overview.md; docs/swagger-api-test-guide.md"),
  P("DEF-048","US-032","Multi-domain recommendation","Expert phù hợp domain phụ có thể bị loại khỏi candidate set","Broad keyword/single-domain retrieval có thể bỏ expert chỉ match domain/technology thứ hai hoặc taxonomy ID chưa được resolve.","1. Gán job nhiều domain/skill.\n2. Tạo expert chỉ match domain thứ hai nhưng đủ mandatory skills.\n3. Generate recommendation.","Candidate retrieval xét toàn bộ domain/skill/technology IDs và giữ expert hợp lệ.","Chưa có reproduction ngoài benchmark; có nguy cơ recall thấp cho multi-domain job.","High","High","POT-TC-012 (multi-domain integration)","docs/stories/US-032-multi-domain-recommendation-experts.md; JobRequirementResolverTest.java; ExpertCandidateRepositoryIntegrationTest.java"),
  P("DEF-049","US-032, US-047","AI SoW clarification","AI có thể trả question-only hoặc lặp clarification nhiều chu kỳ","Model response không ổn định có thể bỏ SoW/milestones, trả nhiều câu hỏi hoặc tiếp tục hỏi sau khi đã clarification.","1. Cho AI trả needMoreInfo/question-only.\n2. Gửi clarification rồi gọi lại.\n3. Cho recovery response tiếp tục thiếu draft.","Giữ draft hiện có, tối đa một batch ba câu hỏi; retry nội bộ một lần rồi dừng rõ lỗi.","Chưa có provider log cụ thể; có nguy cơ loop, mất draft hoặc UX bị chặn.","Medium","High","POT-TC-013 (provider adversarial E2E)","docs/stories/US-032-non-blocking-ai-sow-drafts-with-assumptions.md:39-51; docs/stories/US-047-limit-sow-clarification-to-one-cycle.md; AiSowGenerationServiceTest.java:482-809"),
  P("DEF-050","US-033","Milestone acceptance criteria","Sửa acceptance criteria có thể ảnh hưởng milestone/contract khác","Story chuyển từ fixed platform catalog sang criteria thuộc milestone; phiên bản/flow cũ có thể tái sử dụng row shared hoặc cho mutation sau contract.","1. Tạo hai milestones/contract dùng criteria tương tự.\n2. Sửa/xóa criterion của một milestone.\n3. Kiểm tra milestone còn lại và contract snapshot.","Criteria ownership độc lập theo milestone; contract snapshot bất biến; mutation sau contract bị chặn.","Chưa có failure lưu lại; có nguy cơ cross-milestone mutation hoặc thay đổi điều khoản đã ký.","Critical","Urgent","POT-TC-014 (ownership integration)","docs/stories/US-033-ai-milestone-acceptance-criteria/overview.md; docs/decisions/0019-milestone-owned-acceptance-criteria.md; ContractExecutionServiceTest.java:727-797"),
  P("DEF-051","US-034","Refresh token","Refresh có thể chấp nhận access token, token stale hoặc account đã lock","Refresh flow phải phân biệt token type/version/account status; thiếu một gate có thể kéo dài session không hợp lệ.","1. Gọi /api/auth/refresh bằng access token.\n2. Dùng refresh token cũ sau token-version change.\n3. Lock account rồi refresh.","Tất cả trường hợp invalid/stale/non-refresh/locked bị từ chối; valid refresh cấp access token mới.","Chưa có security incident; rủi ro session bị gia hạn trái phép nếu một gate không chạy ở runtime.","Critical","Urgent","POT-TC-015 (security integration)","docs/stories/US-034-refresh-token-session-extension/overview.md; AuthServiceImplTest.java:202-255"),
  P("DEF-052","US-035","Platform wallet history","Platform wallet history có thể thiếu event hoặc hiển thị hai ledger legs","Admin history phải project nhiều finance event và lọc duplicate transfer legs; event type mới có thể rơi vào raw/duplicate path.","1. Tạo membership, credit, deposit, withdrawal, top-up events.\n2. Gọi platform wallet history.\n3. Đếm logical events và kiểm tra title/description.","Mỗi business event hiển thị một dòng đầy đủ; technical IDs vẫn có cho reconciliation.","Chưa có failure cụ thể; event type mới có thể thiếu translation hoặc bị nhân đôi.","Medium","High","POT-TC-016 (event-family matrix)","docs/stories/US-035-platform-wallet-history/overview.md; validation.md:13,55; PaymentWalletServiceTest.java:947"),
  P("DEF-053","US-036","Profile read DTO","Contact fields có thể thiếu hoặc khác nhau giữa các profile read endpoints","US-036 thêm contact fields cho nhiều DTO/path; mapper mới hoặc nhánh not-found/role-specific có thể bỏ email/phone.","1. Tạo Business/Expert có contact.\n2. Gọi public, current và staff-list endpoints.\n3. So sánh field theo privacy contract.","Mỗi endpoint trả đúng contact field được phép và không lộ field protected.","Chưa có full endpoint matrix; có thể thiếu dữ liệu hoặc privacy inconsistency.","Medium","Medium","POT-TC-017 (DTO parity matrix)","docs/stories/US-036-profile-contact-fields/overview.md; ProfileServiceTest.java:206-458"),
  P("DEF-054","US-037","Milestone escrow","Retry deposit có thể hold escrow hai lần hoặc chuyển state sai","Escrow foundation gồm ledger và state machine; thiếu operation id/state guard ở một path có thể double-hold khi client retry.","1. Gọi deposit milestone escrow hai lần cùng milestone.\n2. Mô phỏng concurrent retry.\n3. Kiểm tra wallet balance, ledger và milestone status.","Chỉ một hold được tạo; retry idempotent; transition PENDING→IN_PROGRESS hợp lệ.","Chưa có incident lưu lại; rủi ro double hold/duplicate ledger hoặc state nhảy sai.","Critical","Urgent","POT-TC-018 (concurrent escrow retry)","docs/stories/US-037-milestone-escrow-foundation/overview.md; ContractExecutionServiceTest.java:1190; WalletLedgerServiceTest.java:70"),
  P("DEF-055","US-038","Dispute authorization","Participant có thể spoof initiator hoặc gọi action sai dispute state","Dispute flow có nhiều actor/state gates; thiếu ownership check trước mutation có thể đổi milestone hoặc tạo dispute trái phép.","1. Dùng account không thuộc contract hoặc spoof initiatorId.\n2. Gọi initiate/cancel/escalate/decide.\n3. Kiểm tra dispute và milestone.","Reject trước mọi mutation; chỉ participant/assigned Staff đúng state được thao tác.","Chưa có exploit evidence; rủi ro unauthorized state change hoặc milestone bị DISPUTED.","Critical","Urgent","POT-TC-019 (authorization E2E)","docs/stories/US-038-dispute-flow/overview.md; ContractExecutionServiceTest.java:1281-1308,1696-1728"),
  P("DEF-056","US-039","Contract termination","Termination có thể refund sai escrow của milestone đã hoàn tất","Termination phải phân biệt active, approved/completed và deposit legs; mapping sai có thể hoàn tiền đã payout hoặc bỏ sót tiền participant.","1. Contract có milestone completed và active.\n2. Execute termination.\n3. Đối chiếu escrow/refund/payout từng milestone.","Chỉ escrow còn khóa được refund/split; completed/approved không bị hoàn lại lần hai.","Chưa có financial incident; rủi ro double refund, refund sai bên hoặc thiếu ledger leg.","Critical","Urgent","POT-TC-020 (mixed-state settlement integration)","docs/stories/US-039-contract-termination/overview.md; ContractExecutionServiceTest.java:1588-1620,1774"),
  P("DEF-057","US-043, US-044","Flow 4-5 specification","Spec SLA/reject/termination mâu thuẫn có thể dẫn tới implementation sai","US-043/044 sửa lại authority, reject-resubmit và dual-deposit rules; branch/consumer còn dùng spec cũ có thể triển khai hành vi trái nhau.","1. So SPEC v2.1/v2.2 với code hiện tại.\n2. Test reject, resubmit, Staff decision và termination.\n3. So trạng thái/ledger với spec mới.","Một nguồn contract duy nhất; code/test/docs thống nhất rule mới.","Chưa có runtime failure cụ thể; rủi ro branch hoặc client dùng rule đã supersede.","High","High","POT-TC-021 (spec conformance review)","docs/stories/US-043-flow45-spec-v21/overview.md; docs/stories/US-044-flow45-reject-resubmit-dual-deposit/overview.md:5; SPEC-MILESTONE-DISPUTER.md"),
  P("DEF-058","US-045","Reject/resubmit flow","Business reject có thể tạo dispute hoặc resubmit quay sai state","Spec cũ từng gắn rejection với dispute; flow đúng phải lưu feedback/reject count và trả milestone về IN_PROGRESS mà không mở dispute tự động.","1. Submit deliverable.\n2. Business reject với feedback.\n3. Expert resubmit và kiểm tra dispute/state/count.","Reject lưu feedback, tăng count, UNDER_REVIEW→IN_PROGRESS; dispute chỉ khi participant chủ động initiate.","Chưa có failure hiện tại; rủi ro rejection tạo dispute hoặc không cho resubmit đúng cách.","High","High","POT-TC-022 (reject/resubmit E2E)","docs/stories/US-045-flow45-v22-backend-implementation/validation.md:69; ContractExecutionServiceTest.java:941,1249"),
  P("DEF-059","US-048","Report gates and refunds","Progress-report gate hoặc automatic refund có thể chạy sai thứ tự","US-048 thêm ACK gate, Staff routing và auto-refund trong flow nhiều transaction; guard order sai có thể cho submit tiếp khi report chưa ack hoặc hoàn tiền trước trạng thái hợp lệ.","1. Để progress report pending Business ack.\n2. Submit/request report tiếp hoặc kích hoạt termination/refund.\n3. Kiểm tra state, notification và wallet.","Pending ack chặn đúng action; refund chỉ sau state/authority hợp lệ và idempotent.","Chưa có failure trực tiếp ngoài review fixes; rủi ro bypass gate hoặc refund có side effect sớm.","Critical","Urgent","POT-TC-023 (transaction-order integration)","docs/stories/US-048-flow45-v23-routing-and-report-gates/overview.md; validation.md:31-52; commit 2ea059c review fix"),
  P("DEF-060","US-050","Staff dispute authorization","Staff không được gán có thể xem hoặc quyết định dispute","Inbox/routing cần filter assigned Staff và specialization; thiếu gate ở list/detail/decision endpoint có thể lộ evidence hoặc cho payout trái quyền.","1. Tạo hai Staff khác domain/assignment.\n2. Staff không được gán gọi list/detail/decide.\n3. Kiểm tra response và settlement.","Chỉ assigned/eligible Staff xem và quyết định; Admin read-only theo contract.","Chưa có security incident; rủi ro data exposure hoặc unauthorized payout decision.","Critical","Urgent","POT-TC-024 (role/domain authorization E2E)","docs/stories/US-050-staff-dispute-inbox-specialization-routing/overview.md; StaffDisputeAuthTest.java:110; ContractExecutionServiceTest.java:2146"),
  P("DEF-061","US-051","Profile rating","averageRating có thể thiếu, null sai hoặc tính cả review không hợp lệ","Field rating được bổ sung vào response; mapper/query có thể xử lý sai profile chưa có review hoặc review ngoài phạm vi.","1. Expert không có review.\n2. Expert có nhiều review/rating biên.\n3. Gọi tất cả profile response endpoints.","Không review trả contract value nhất quán; có review trả average chính xác và cùng kiểu dữ liệu.","Chưa có reproduction; có thể null/0 không nhất quán hoặc average sai.","Medium","Medium","POT-TC-025 (rating aggregation matrix)","docs/stories/US-051-profile-average-rating-response.md; ProfileServiceTest.java focused proof"),
  P("DEF-062","US-053","Milestone execution","Deposit thành công nhưng milestone vẫn DEPOSITED, Expert phải start thủ công","Story đổi contract sang auto-start sau escrow. Client cũ/mutation path khác có thể vẫn để DEPOSITED và không đặt in_progress_started_at.","1. Business deposit escrow milestone PENDING.\n2. Đọc milestone ngay sau commit.\n3. Thử progress report/deliverable không gọi start.","Deposit thành công tự chuyển IN_PROGRESS và đặt start timestamp; start endpoint idempotent.","Chưa có failure log; rủi ro milestone kẹt DEPOSITED và timeline/SLA chưa chạy.","High","High","POT-TC-026 (deposit runtime E2E)","docs/stories/US-053-auto-start-milestone-after-escrow/overview.md; ContractExecutionServiceTest.java:1190,1817"),
  P("DEF-063","US-056","Session security","Access/refresh token cũ có thể vẫn hợp lệ sau login mới","Single active session dựa tokenVersion; nếu filter hoặc refresh path không kiểm tra version nhất quán, session cũ chưa bị revoke.","1. Login lấy token A.\n2. Login lại lấy token B.\n3. Dùng access/refresh A trên protected/auth endpoints.","Token A bị 401; token B hoạt động; refresh stale cũng bị reject.","Chưa có incident; rủi ro stale session tiếp tục truy cập.","Critical","Urgent","POT-TC-027 (multi-session integration)","docs/stories/US-056-single-session-staff-catalog-sla/design.md:28-35; JwtAuthenticationFilterTest.java:38-70; AuthServiceImplTest.java:222"),
  P("DEF-064","US-057","Profile review routing","Staff ngoài PROFILE_REVIEW domain có thể nhận/xem hồ sơ","Internal domain routing được thêm sau; thiếu mapping/config hoặc gate ở một list endpoint có thể cho Staff không liên quan xem dữ liệu KYB/KYC.","1. Tạo Staff không có PROFILE_REVIEW.\n2. Submit profile và gọi staff profile lists/review.\n3. Kiểm tra notification/authorization.","Staff ngoài domain không nhận notification, không list/review; missing config fail an toàn.","Chưa có production evidence; rủi ro privacy leak hoặc hồ sơ không có reviewer.","High","High","POT-TC-028 (routing configuration E2E)","docs/stories/US-057-internal-profile-review-domain-routing/overview.md; ProfileServiceTest.java:333-346,521"),
  P("DEF-065","US-058","Admin configuration CRUD","Xóa catalog/package đang được tham chiếu có thể gây FK hoặc mất dữ liệu","Story yêu cầu soft delete; một endpoint/delete path mới dùng physical delete có thể phá reference hoặc làm lịch sử không đọc được.","1. Tạo domain/skill/package đang được profile/job/purchase tham chiếu.\n2. Admin gọi delete.\n3. Đọc dữ liệu lịch sử và kiểm tra FK.","Delete chỉ deactivate; reference/history còn nguyên; unsupported setting key bị reject.","Chưa có failure; rủi ro FK exception hoặc mất lookup label nếu physical delete.","High","High","POT-TC-029 (referenced-delete integration)","docs/stories/US-058-admin-configuration-crud-apis/overview.md; execplan.md:19; PaymentWalletServiceTest.java:154; AdminServiceTest.java:122"),
  P("DEF-066","US-059","Admin dashboard analytics","Date range/pagination invalid có thể bị âm thầm default và làm aggregate sai","Dashboard có nhiều aggregate/filter endpoints; flow doc ghi pagination trước đây dùng default khi input invalid.","1. Gửi page/size âm, from>to hoặc timezone boundary.\n2. Gọi dashboard aggregates.\n3. So với query trực tiếp DB.","Input invalid trả validation error; valid range cho tổng/trend/breakdown nhất quán.","Chưa có failure hiện tại; rủi ro silently default, double count hoặc lệch ngày.","High","High","POT-TC-030 (boundary/query reconciliation)","docs/stories/US-059-admin-dashboard-aggregate-apis/overview.md; docs/flows/admin-dispute-dashboard-flow.md:110; AdminDashboardServiceTest.java:109-185"),
  P("DEF-067","US-061","Source ZIP security","ZIP giả mạo MIME/signature hoặc path ownership có thể được chấp nhận","Upload phải kiểm tra extension, MIME, ZIP signature và expert folder ownership; unit mocks chưa thay thế multipart/storage E2E.","1. Upload file không phải ZIP nhưng đổi extension/MIME.\n2. Dùng URL file thuộc expert khác.\n3. Submit deliverable.","Reject file giả/path không thuộc expert trước persist/audit; ZIP hợp lệ được nhận.","Chưa có exploit evidence; rủi ro bypass content validation hoặc gắn source archive của người khác.","Critical","Urgent","POT-TC-031 (multipart/storage security E2E)","docs/stories/US-061-milestone-source-code-file-upload/validation.md:56-58; FirebaseStorageService.java:66-82; ContractExecutionServiceTest.java:1061,1079"),
  P("DEF-068","US-AUTH-004","Password reset security","Reset token có thể dùng lại, bị race hoặc mở khóa account ADMIN_LOCKED","Token reset cần atomic claim/restore khi rollback và chỉ unlock TOO_MANY_FAILED. Sai transaction ordering có thể cho hai caller reset hoặc mở admin lock.","1. Gửi hai reset request đồng thời cùng token.\n2. Gây DB rollback sau claim.\n3. Thử reset account ADMIN_LOCKED.","Chỉ một caller thành công; rollback phục hồi token; ADMIN_LOCKED không bị mở.","Chưa có incident; rủi ro token reuse, mất token hợp lệ hoặc privilege unlock.","Critical","Urgent","POT-TC-032 (concurrency/transaction integration)","docs/stories/high-risk-auth-lockout-reset/design.md:99-110; AuthServiceImplTest.java:392-539")
);

for (const d of defects) {
  for (const k of ["summary", "expected", "actual", "evidence"]) if (!d[k]) throw new Error(`${d.id} missing ${k}`);
}

const stories = await loadStories();
const defectTextFields = ["summary", "description", "steps", "expected", "actual", "tc", "evidence", "note"];
const storyTextFields = ["title", "ac", "note"];
const translationInputs = [
  ...defects.flatMap(d => defectTextFields.map(field => d[field])),
  ...stories.flatMap(s => storyTextFields.map(field => s[field])),
  "Chưa có reproduction trực tiếp trong repo.",
  "Bằng chứng khớp defect và fix/retest.",
];
const englishTranslations = await translateStringsToEnglish(translationInputs);
for (const d of defects) {
  for (const field of defectTextFields) {
    if (englishTranslations.has(d[field])) d[field] = englishTranslations.get(d[field]);
  }
}
for (const s of stories) {
  for (const field of storyTextFields) {
    if (englishTranslations.has(s[field])) s[field] = englishTranslations.get(s[field]);
  }
}
const normalizeEnglishText = value => typeof value === "string"
  ? value
      .replace(/\u200b/g, "")
      .replaceAll("→", " to ")
      .replaceAll("✅", "Pass")
      .replaceAll(
        "commit 'ep schema AI recommend khong gen loi'",
        "commit 'enforce AI recommendation schema without generation errors'",
      )
  : value;
for (const d of defects) {
  for (const field of defectTextFields) d[field] = normalizeEnglishText(d[field]);
}
for (const s of stories) {
  for (const field of storyTextFields) s[field] = normalizeEnglishText(s[field]);
}
const kybCoverageStory = stories.find(s =>
  s.id === "US-046" && /KYB Tax Code Verification/i.test(s.title),
);
if (kybCoverageStory) {
  kybCoverageStory.ac = "AC1: A blank tax code is rejected as not provided. AC2: A tax code that is not 10 or 13 digits is rejected as invalid. AC3: A tax code already used by another account is rejected as a duplicate. AC4: A tax code not found in VietQR returns NotFoundException. AC5: An unavailable VietQR API returns BadGatewayException. AC6: A valid tax code stores the company name, address, and verified representative from VietQR. AC7: The same account may resubmit its existing tax code without a duplicate error. AC8: Staff notifications and audit logging remain functional.";
}
const sourceNoteStory = stories.find(s =>
  s.id === "US-029" && /Backfill Legacy Source Notes/i.test(s.title),
);
if (sourceNoteStory) {
  sourceNoteStory.ac = "Every Java file in src/main/java that lacks a NOTE FILE section receives a note consistent with the established style. New notes explain only the file, annotations, fields, and important methods; they do not change business logic. The source still compiles after the note backfill.";
}
const evidencePotentialNote = englishTranslations.get("Chưa có reproduction trực tiếp trong repo.")
  || "No direct reproduction is available in the repository.";
const evidenceConfirmedNote = englishTranslations.get("Bằng chứng khớp defect và fix/retest.")
  || "The evidence matches the defect and its fix/retest.";
const related = new Map();
for (const s of stories) related.set(`${s.id}|${s.title}`, []);
const duplicateStoryTitleHint = new Map([
  ["DEF-009", "Contract Milestone"],
  ["DEF-010", "Duration"],
  ["DEF-019", "Wallet Transaction"],
  ["DEF-050", "AI-Generated"],
]);
for (const s of stories) {
  for (const d of defects) {
    if (!d.us.split(",").map(x => x.trim()).includes(s.id)) continue;
    const hint = duplicateStoryTitleHint.get(d.id);
    if (hint && !s.title.toLowerCase().includes(hint.toLowerCase())) continue;
    related.get(`${s.id}|${s.title}`).push(d.id);
  }
}

const defectHeaders = ["Defect ID","User Story ID","Module/Function","Defect Summary","Defect Description","Steps to Reproduce","Expected Result","Actual Result","Severity","Priority","Status","Detected By","Assigned To","Detected Date","Fixed Date","Test Case ID","Evidence Level","Evidence Source","Commit/Issue Reference","Note"];
const defectRows = defects.map(d => [d.id,d.us,d.module,d.summary,d.description,d.steps,d.expected,d.actual,d.severity,d.priority,d.status,d.detectedBy,d.assigned,d.detected,d.fixed,d.tc,d.level,d.evidence,d.ref,d.note]);
const coverageHeaders = ["User Story ID","User Story Title","Module","Acceptance Criteria Summary","Number of Confirmed Defects","Number of Potential Defects","Related Defect IDs","Evidence Source","Note"];
const coverageRows = stories.map(s => {
  const ids = related.get(`${s.id}|${s.title}`) || [];
  const linked = defects.filter(d => ids.includes(d.id));
  return [s.id,s.title,s.module,s.ac,linked.filter(d => d.level === "Confirmed").length,linked.filter(d => d.level === "Potential").length,ids.join(", "),s.source,s.note];
});

const evidenceHeaders = ["Evidence ID","Defect ID","Evidence Type","File/Commit/Issue","Location","Evidence Description","Confidence","Note"];
const evidenceRows = [];
let ev = 1;
for (const d of defects) {
  const items = d.evidence.split(";").map(x => x.trim()).filter(Boolean);
  if (d.ref) items.push(...d.ref.split(";").map(x => `Commit/Trace ${x.trim()}`));
  for (const item of [...new Set(items)]) {
    const low = item.toLowerCase();
    const type = low.includes("commit") ? "Commit" : low.includes("trace") ? "Harness Trace" : low.includes("test") || low.includes("test.java") ? "Test Case" : low.includes(".md") ? "Document" : low.includes(".java") || low.includes(".sql") || low.includes(".properties") ? "Source Code" : "Project Evidence";
    evidenceRows.push([`EVD-${String(ev++).padStart(3,"0")}`,d.id,type,item,item,d.summary,d.level === "Confirmed" ? "High" : "Medium",d.level === "Potential" ? evidencePotentialNote : evidenceConfirmedNote]);
  }
}

const wb = Workbook.create();
const defectSheet = wb.worksheets.add("Defect List");
const coverageSheet = wb.worksheets.add("User Story Coverage");
const evidenceSheet = wb.worksheets.add("Evidence");

function writeSheet(sheet, headers, rows, tableName) {
  sheet.showGridLines = false;
  const lastCol = String.fromCharCode(64 + headers.length);
  sheet.getRange(`A1:${lastCol}${rows.length + 1}`).values = [headers, ...rows];
  const table = sheet.tables.add(`A1:${lastCol}${rows.length + 1}`, true, tableName);
  table.style = "TableStyleMedium2";
  table.showFilterButton = true;
  sheet.freezePanes.freezeRows(1);
  sheet.getRange(`A1:${lastCol}1`).format = { fill: "#1F4E78", font: { bold: true, color: "#FFFFFF" }, verticalAlignment: "center", horizontalAlignment: "center", wrapText: true, borders: { preset: "all", style: "thin", color: "#17365D" } };
  sheet.getRange(`A2:${lastCol}${rows.length + 1}`).format = { verticalAlignment: "top", wrapText: true, borders: { insideHorizontal: { style: "thin", color: "#D9E2F3" } } };
  sheet.getRange(`A1:${lastCol}1`).format.rowHeight = 34;
}

writeSheet(defectSheet, defectHeaders, defectRows, "DefectListTable");
writeSheet(coverageSheet, coverageHeaders, coverageRows, "StoryCoverageTable");
writeSheet(evidenceSheet, evidenceHeaders, evidenceRows, "EvidenceTable");

const dEnd = defects.length + 1;
const sEnd = stories.length + 1;
const eEnd = evidenceRows.length + 1;

defectSheet.getRange(`I2:I${dEnd}`).dataValidation = { rule: { type: "list", values: ["Critical","High","Medium","Low"] } };
defectSheet.getRange(`J2:J${dEnd}`).dataValidation = { rule: { type: "list", values: ["Urgent","High","Medium","Low"] } };
defectSheet.getRange(`K2:K${dEnd}`).dataValidation = { rule: { type: "list", values: ["Open","In Progress","Fixed","Retest","Closed","Rejected"] } };
defectSheet.getRange(`Q2:Q${dEnd}`).dataValidation = { rule: { type: "list", values: ["Potential","Confirmed"] } };
evidenceSheet.getRange(`G2:G${eEnd}`).dataValidation = { rule: { type: "list", values: ["High","Medium","Low"] } };

function textColor(range, text, fill, font = "#000000") { range.conditionalFormats.add("containsText", { text, format: { fill, font: { color: font, bold: true } } }); }
textColor(defectSheet.getRange(`I2:I${dEnd}`),"Critical","#9C0006","#FFFFFF");
textColor(defectSheet.getRange(`I2:I${dEnd}`),"High","#FFC7CE","#9C0006");
textColor(defectSheet.getRange(`I2:I${dEnd}`),"Medium","#FFEB9C","#7F6000");
textColor(defectSheet.getRange(`I2:I${dEnd}`),"Low","#C6EFCE","#006100");
textColor(defectSheet.getRange(`J2:J${dEnd}`),"Urgent","#9C0006","#FFFFFF");
textColor(defectSheet.getRange(`J2:J${dEnd}`),"High","#FFC7CE","#9C0006");
textColor(defectSheet.getRange(`J2:J${dEnd}`),"Medium","#FFEB9C","#7F6000");
textColor(defectSheet.getRange(`J2:J${dEnd}`),"Low","#C6EFCE","#006100");
textColor(defectSheet.getRange(`K2:K${dEnd}`),"Open","#F4CCCC","#9C0006");
textColor(defectSheet.getRange(`K2:K${dEnd}`),"In Progress","#FFE599","#7F6000");
textColor(defectSheet.getRange(`K2:K${dEnd}`),"Fixed","#9FC5E8","#073763");
textColor(defectSheet.getRange(`K2:K${dEnd}`),"Retest","#FCE5CD","#783F04");
textColor(defectSheet.getRange(`K2:K${dEnd}`),"Closed","#B6D7A8","#274E13");
textColor(defectSheet.getRange(`K2:K${dEnd}`),"Rejected","#D9D9D9","#595959");
textColor(defectSheet.getRange(`Q2:Q${dEnd}`),"Potential","#FFF2CC","#7F6000");
textColor(defectSheet.getRange(`Q2:Q${dEnd}`),"Confirmed","#E2F0D9","#375623");

defectSheet.getRange(`N2:O${dEnd}`).format.numberFormat = "yyyy-mm-dd";
coverageSheet.getRange(`E2:F${sEnd}`).format.numberFormat = "0";
defectSheet.getRange(`A2:A${dEnd}`).format.font = { bold: true, color: "#1F4E78" };
coverageSheet.getRange(`A2:A${sEnd}`).format.font = { bold: true, color: "#1F4E78" };
evidenceSheet.getRange(`A2:B${eEnd}`).format.font = { bold: true, color: "#1F4E78" };

const dw = [13,18,24,38,55,48,44,44,12,12,14,16,20,13,13,45,15,60,28,38];
dw.forEach((w,i)=>defectSheet.getRangeByIndexes(0,i,dEnd,1).format.columnWidth=w);
const sw = [18,38,25,70,16,16,28,55,42];
sw.forEach((w,i)=>coverageSheet.getRangeByIndexes(0,i,sEnd,1).format.columnWidth=w);
const ew = [14,14,18,55,55,48,14,38];
ew.forEach((w,i)=>evidenceSheet.getRangeByIndexes(0,i,eEnd,1).format.columnWidth=w);
defectSheet.getRange(`A2:T${dEnd}`).format.rowHeight = 88;
coverageSheet.getRange(`A2:I${sEnd}`).format.rowHeight = 120;
evidenceSheet.getRange(`A2:H${eEnd}`).format.rowHeight = 48;

// Apply panes last because later sheet-format mutations can reset the sheet view in this runtime.
defectSheet.freezePanes.freezeRows(1);
coverageSheet.freezePanes.freezeRows(1);
evidenceSheet.freezePanes.freezeRows(1);
defectSheet.freezePanes.freezeColumns(1);
coverageSheet.freezePanes.freezeColumns(1);
evidenceSheet.freezePanes.freezeColumns(1);

await fs.mkdir(outDir, { recursive: true });
const xlsx = await SpreadsheetFile.exportXlsx(wb);
const finalPath = path.join(outDir, process.env.DEFECT_OUTPUT_NAME || "Defect_List.xlsx");
await xlsx.save(finalPath);

// Re-open the exported artifact so QA checks the actual .xlsx, not only the in-memory workbook.
const qaWb = await SpreadsheetFile.importXlsx(await FileBlob.load(finalPath));

const inspectSheets = await qaWb.inspect({ kind: "sheet", include: "id,name", maxChars: 3000 });
const inspectDefects = await qaWb.inspect({ kind: "table", sheetId: "Defect List", range: `A1:T${Math.min(dEnd,8)}`, include: "values,formulas", tableMaxRows: 8, tableMaxCols: 20, maxChars: 10000 });
const inspectCoverage = await qaWb.inspect({ kind: "table", sheetId: "User Story Coverage", range: `A1:I${Math.min(sEnd,8)}`, include: "values,formulas", tableMaxRows: 8, tableMaxCols: 9, maxChars: 8000 });
const inspectEvidence = await qaWb.inspect({ kind: "table", sheetId: "Evidence", range: `A1:H${Math.min(eEnd,8)}`, include: "values,formulas", tableMaxRows: 8, tableMaxCols: 8, maxChars: 8000 });
const headerStyle = await qaWb.inspect({ kind: "computedStyle", sheetId: "Defect List", range: "A1:T1", maxChars: 3000 });
const formulaErrors = await qaWb.inspect({ kind: "match", searchTerm: "#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A", options: { useRegex: true, maxResults: 300 }, summary: "final formula error scan", maxChars: 5000 });
const qaMeta = {
  sheetNames: [0,1,2].map(i => qaWb.worksheets.getItemAt(i).name),
  tableCounts: ["Defect List","User Story Coverage","Evidence"].map(n => qaWb.worksheets.getItem(n).tables.items.length),
  validations: {
    severity: qaWb.worksheets.getItem("Defect List").getRange(`I2:I${dEnd}`).dataValidation,
    priority: qaWb.worksheets.getItem("Defect List").getRange(`J2:J${dEnd}`).dataValidation,
    status: qaWb.worksheets.getItem("Defect List").getRange(`K2:K${dEnd}`).dataValidation,
    evidenceLevel: qaWb.worksheets.getItem("Defect List").getRange(`Q2:Q${dEnd}`).dataValidation,
  }
};
await fs.writeFile(path.join(outDir, ".defect_work", "qa_inspect.txt"), [JSON.stringify(qaMeta), inspectSheets.ndjson, inspectDefects.ndjson, inspectCoverage.ndjson, inspectEvidence.ndjson, headerStyle.ndjson, formulaErrors.ndjson].join("\n"));

for (const [name, range, file] of [
  ["Defect List", `A1:T${Math.min(dEnd,12)}`, "preview_defects.png"],
  ["User Story Coverage", `A1:I${Math.min(sEnd,14)}`, "preview_coverage.png"],
  ["Evidence", `A1:H${Math.min(eEnd,14)}`, "preview_evidence.png"],
]) {
  const img = await qaWb.render({ sheetName: name, range, scale: 0.8, format: "png" });
  await fs.writeFile(path.join(outDir, ".defect_work", file), new Uint8Array(await img.arrayBuffer()));
}

await fs.rm(`${finalPath}.inspect.ndjson`, { force: true });

console.log(JSON.stringify({ output: finalPath, stories: stories.length, confirmed: defects.filter(d=>d.level==="Confirmed").length, potential: defects.filter(d=>d.level==="Potential").length, evidenceRows: evidenceRows.length, sheets: ["Defect List","User Story Coverage","Evidence"] }));
