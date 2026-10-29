import { ADMIN_NOTES, ADMIN_REPORT_DETAILS, ADMIN_REPORTS, ADMIN_SANCTIONS, CALENDAR_EVENTS, FAQ_ANSWERS_BY_BUILDING, FAQ_DOCUMENTS, FAQ_FALLBACK_ANSWER, FAQ_INDEX_SUMMARY, FAQ_REINDEX_COMPLETED, FAQ_REINDEX_STARTED, RESIDENTS, UPLOADED_FAQ_DOCUMENT } from "../mocks"
import type { AdminReportDetail, AdminReportFilter, AdminReportListResponse, AdminSanction, AdminSanctionFilter, AdminSanctionListResponse, FaqAskRequest, FaqDocumentUploadRequest, NoteParticipant, NoteStatus, ReportActionRequest, ReportActionResponse, ResidentDetail, SanctionType } from "../types"

const copy = <T,>(value: T): T => structuredClone(value)

export async function getResidents() { // TODO: user-service / contract-service 연동
  return copy(RESIDENTS)
}
export async function getResidentDetail(name: string): Promise<ResidentDetail | undefined> { // TODO: user-service / contract-service 연동
  const resident = RESIDENTS.find((item) => item.name === name)
  return resident ? {
    ...copy(resident),
    phone: "010-2847-6931",
    email: "minseo.kim@email.com",
    memo: "주차 1대 등록. 택배는 평일 저녁 수령을 선호합니다."
  } : undefined
}
export async function getMoveSchedules() { // TODO: user-service / contract-service 연동
  return copy(CALENDAR_EVENTS)
}
export async function getAdminNotes() {
  return copy(ADMIN_NOTES)
}
export async function getAdminNoteBadgeCount(): Promise<number> {
  return ADMIN_NOTES.filter((note) => note.status === "UNANSWERED").length
}
export async function updateAdminNoteStatus(noteId: number, status: NoteStatus, processingMemo?: string): Promise<void> {
  const note = ADMIN_NOTES.find((item) => item.id === noteId);
  if (!note) return;
  note.status = status;
  if (processingMemo !== undefined) note.processingMemo = processingMemo
}
export async function searchNoteRecipients(query: string): Promise<NoteParticipant[]> {
  return copy(ADMIN_NOTES.map((note) => note.participant).filter((participant) => participant.id && participant.name.includes(query)))
}

export async function getFaqDocuments() {
  return copy(FAQ_DOCUMENTS)
}
export async function getFaqIndexSummary() {
  return copy(FAQ_INDEX_SUMMARY)
}
export async function uploadFaqDocument(_request: FaqDocumentUploadRequest) {
  return copy(UPLOADED_FAQ_DOCUMENT)
}
export async function deleteFaqDocument(_documentId: number): Promise<void> {
  return Promise.resolve()
}
export async function downloadFaqDocument(documentId: number) {
  const document = FAQ_DOCUMENTS.find((item) => item.documentId === documentId);
  return document ? copy(document) : undefined
}
export async function reindexFaqDocuments() {
  return copy(FAQ_REINDEX_STARTED)
}
export async function getFaqReindexJob(_jobId: string) {
  return copy(FAQ_REINDEX_COMPLETED)
}
export async function askFaqBot(request: FaqAskRequest) {
  const unknown = /수영장|헬스장|택배 보관|문서에 없는/.test(request.question);
  return copy(unknown ? FAQ_FALLBACK_ANSWER : FAQ_ANSWERS_BY_BUILDING[request.buildingId] ?? FAQ_ANSWERS_BY_BUILDING[1])
}

const reportSummary = () => ({
  receivedCount: ADMIN_REPORTS.content.filter((item) => item.status === "RECEIVED").length,
  resolvedCount: ADMIN_REPORTS.content.filter((item) => item.status === "RESOLVED").length,
  rejectedCount: ADMIN_REPORTS.content.filter((item) => item.status === "REJECTED").length
})
export async function getAdminReportBadgeCount(): Promise<number> {
  return reportSummary().receivedCount
}
export async function getAdminReports(filter: AdminReportFilter = {}): Promise<AdminReportListResponse> {
  let content = ADMIN_REPORTS.content.filter((item) => item.status === (filter.status ?? "RECEIVED"))
  if (filter.sourceType) content = content.filter((item) => item.sourceType === filter.sourceType)
  if (filter.targetType) content = content.filter((item) => item.targetType === filter.targetType)
  if (filter.buildingId) content = content.filter((item) => item.buildingId === filter.buildingId)
  content.sort((a, b) => filter.sort === "count" ? b.reportCount - a.reportCount : b.latestReportedAt.localeCompare(a.latestReportedAt))
  const page = filter.page ?? 0
  const size = filter.size ?? 20
  return copy({
    ...ADMIN_REPORTS,
    summary: reportSummary(),
    content: content.slice(page * size, (page + 1) * size),
    page,
    size,
    totalElements: content.length,
    totalPages: content.length ? Math.ceil(content.length / size) : 0
  })
}
export async function getAdminReportDetail(reportId: number): Promise<AdminReportDetail | undefined> {
  const saved = ADMIN_REPORT_DETAILS[reportId]
  if (saved) return copy(saved)
  const item = ADMIN_REPORTS.content.find((report) => report.reportId === reportId)
  if (!item) return undefined
  return copy({
    target: {
      targetType: item.targetType,
      targetId: item.targetId,
      sourceType: item.sourceType,
      sourceId: item.sourceId,
      sourceTitle: item.sourceTitle,
      snapshot: {
        content: item.contentPreview,
        authorName: item.targetUserName,
        writtenAt: item.latestReportedAt
      },
      currentStatus: item.status === "RESOLVED" ? "HIDDEN" : "ACTIVE"
    },
    targetUser: {
      userId: item.targetUserId,
      name: item.targetUserName,
      roomNumber: item.targetRoomNumber,
      buildingName: item.buildingName
    },
    reports: [
      {
        reportId: item.reportId,
        reasonType: item.reasonTypes[0],
        reasonDetail: null,
        createdAt: item.latestReportedAt
      }
    ],
    history: {
      reportedCount: item.reportCount,
      warningCount: 0,
      sanctions: []
    },
    status: item.status,
    action: item.status === "RESOLVED" ? "HIDE_CONTENT" : item.status === "REJECTED" ? "REJECT" : null,
    adminMemo: item.status === "RECEIVED" ? null : "프로토타입 처리 기록",
    handledByName: item.status === "RECEIVED" ? null : "커뮤니티 매니저",
    handledAt: item.status === "RECEIVED" ? null : item.latestReportedAt
  })
}
export async function handleReport(reportId: number, request: ReportActionRequest): Promise<ReportActionResponse> {
  const report = ADMIN_REPORTS.content.find((item) => item.reportId === reportId)
  if (!report) throw new Error("REPORT_NOT_FOUND: 신고를 찾을 수 없습니다.")
  if (report.status !== "RECEIVED") throw new Error("ALREADY_HANDLED: 이미 처리된 신고입니다.")
  if (request.action === "RESTRICTION" && ![
    1,
    3,
    7,
    30
  ].includes(request.durationDays ?? 0)) throw new Error("INVALID_DURATION: 이용 제한 기간은 1·3·7·30일 중 하나여야 합니다.")
  const handledAt = new Date().toISOString()
  report.status = request.action === "REJECT" ? "REJECTED" : "RESOLVED"
  const detail = await getAdminReportDetail(reportId)
  if (detail) ADMIN_REPORT_DETAILS[reportId] = {
    ...detail,
    status: report.status,
    action: request.action,
    adminMemo: request.adminMemo ?? null,
    handledByName: "커뮤니티 매니저",
    handledAt,
    target: {
      ...detail.target,
      currentStatus: request.action === "DELETE_CONTENT" ? "DELETED" : request.action === "HIDE_CONTENT" || request.hideContent ? "HIDDEN" : detail.target.currentStatus
    }
  }
  let sanctionId: number | null = null
  let endsAt: string | null = null
  if ([
    "WARNING",
    "RESTRICTION",
    "SUSPENSION"
  ].includes(request.action)) {
    sanctionId = Math.max(0, ...ADMIN_SANCTIONS.content.map((item) => item.sanctionId)) + 1
    endsAt = request.action === "RESTRICTION" ? new Date(Date.now() + (request.durationDays ?? 0) * 86_400_000).toISOString() : null
    const sanctionType: SanctionType = request.action === "WARNING" ? "WARNING" : request.action === "RESTRICTION" ? "RESTRICTION" : "SUSPENSION"
    const sanction: AdminSanction = {
      sanctionId,
      userId: report.targetUserId,
      userName: report.targetUserName,
      roomNumber: report.targetRoomNumber,
      buildingName: report.buildingName,
      sanctionType,
      durationDays: request.action === "RESTRICTION" ? request.durationDays ?? null : null,
      reason: request.reason ?? "관리자 조치",
      reportId,
      startsAt: handledAt,
      endsAt,
      active: true,
      createdByName: "커뮤니티 매니저",
      revokedAt: null,
      revokeReason: null
    }
    ADMIN_SANCTIONS.content.unshift(sanction)
  }
  return {
    reportId,
    handledReportCount: report.reportCount,
    status: report.status,
    action: request.action,
    sanctionId,
    endsAt,
    handledAt
  }
}
export async function getAdminSanctions(filter: AdminSanctionFilter = {}): Promise<AdminSanctionListResponse> {
  let content = ADMIN_SANCTIONS.content.filter((item) => filter.activeOnly === false || item.active)
  if (filter.sanctionType) content = content.filter((item) => item.sanctionType === filter.sanctionType)
  if (filter.keyword?.trim()) content = content.filter((item) => item.userName.includes(filter.keyword!.trim()))
  return copy({
    ...ADMIN_SANCTIONS,
    content,
    totalElements: content.length
  })
}
export async function revokeSanction(sanctionId: number, revokeReason: string) {
  const item = ADMIN_SANCTIONS.content.find((sanction) => sanction.sanctionId === sanctionId);
  if (!item) throw new Error("SANCTION_NOT_FOUND: 제재를 찾을 수 없습니다.");
  const revokedAt = new Date().toISOString();
  item.active = false;
  item.revokedAt = revokedAt;
  item.revokeReason = revokeReason;
  return { sanctionId, revokedAt }
}
