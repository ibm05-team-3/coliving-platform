import type { AdminNote, AdminReportDetail, AdminReportListResponse, AdminSanctionListResponse, FaqAskResponse, FaqDocumentFile, FaqDocumentUploadResponse, FaqIndexSummary, FaqReindexJobResponse, FaqReindexResponse, MoveScheduleEvent, Resident } from "../types"

export const residents: Resident[] = [
  { name: "김민서", branch: "망원 스테이", room: "302호", moveIn: "2025. 06. 12", expires: "2026. 06. 11", status: "거주 중", payment: "납부 완료", tone: "green" },
  { name: "박준영", branch: "연남 하우스", room: "201호", moveIn: "2025. 07. 01", expires: "2026. 06. 30", status: "거주 중", payment: "납부 완료", tone: "blue" },
  { name: "이서현", branch: "성수 로프트", room: "405호", moveIn: "2025. 10. 27", expires: "2026. 04. 26", status: "입주 예정", payment: "납부 완료", tone: "beige" },
  { name: "최도윤", branch: "망원 스테이", room: "204호", moveIn: "2024. 11. 03", expires: "2025. 11. 02", status: "퇴거 예정", payment: "납부 완료", tone: "purple" },
  { name: "정하은", branch: "연남 하우스", room: "304호", moveIn: "2025. 03. 15", expires: "2026. 03. 14", status: "거주 중", payment: "미납", tone: "rose" },
  { name: "한지호", branch: "성수 로프트", room: "202호", moveIn: "2025. 08. 20", expires: "2026. 08. 19", status: "거주 중", payment: "납부 완료", tone: "green" },
]

export const calendarEvents: Record<number, MoveScheduleEvent[]> = {
  2: [{ type: "in", label: "이서현 · 405호" }], 5: [{ type: "out", label: "문소희 · 203호" }], 7: [{ type: "in", label: "강윤재 · 302호" }, { type: "in", label: "권지아 · 106호" }], 11: [{ type: "out", label: "김태호 · 401호" }], 14: [{ type: "in", label: "정유나 · 203호" }], 18: [{ type: "out", label: "박상훈 · 104호" }, { type: "in", label: "송하린 · 305호" }], 23: [{ type: "out", label: "최도윤 · 204호" }], 27: [{ type: "in", label: "한소은 · 301호" }],
}

export const adminNotes: AdminNote[] = [
  { id: 1, participant: { id: 101, name: "김예린", type: "CANDIDATE", avatarTone: "rose", building: "망원 스테이", room: "스탠다드" }, status: "UNANSWERED", lastContent: "계약 진행 전에 필요한 서류가 궁금합니다.", lastSentAt: "오전 10:24", unreadCount: 2, processingMemo: "", messages: [{ id: 1, senderType: "USER", content: "안녕하세요. 계약 진행 전에 필요한 서류가 궁금합니다.", sentAt: "2025. 10. 18. 오전 10:24", isRead: true }] },
  { id: 2, participant: { id: 102, name: "노지훈", type: "CANDIDATE", avatarTone: "green", building: "망원 스테이", room: "컴팩트" }, status: "ANSWERED", lastContent: "확인했습니다. 감사합니다.", lastSentAt: "어제", unreadCount: 0, processingMemo: "계약 안내 완료", messages: [{ id: 1, senderType: "USER", content: "최소 계약 기간을 알고 싶습니다.", sentAt: "2025. 10. 17. 오후 2:10", isRead: true }, { id: 2, senderType: "ADMIN", content: "최소 계약 기간은 3개월입니다.", sentAt: "2025. 10. 17. 오후 3:02", isRead: true }] },
  { id: 3, participant: { id: 201, name: "박준영", type: "RESIDENT", avatarTone: "blue", building: "연남 하우스", room: "201호" }, status: "UNANSWERED", lastContent: "공용 라운지 예약을 변경하고 싶어요.", lastSentAt: "어제", unreadCount: 1, processingMemo: "", messages: [{ id: 1, senderType: "USER", content: "공용 라운지 예약을 토요일로 변경하고 싶어요.", sentAt: "2025. 10. 17. 오후 5:40", isRead: false }] },
  { id: 4, participant: { id: 202, name: "정하은", type: "RESIDENT", avatarTone: "purple", building: "연남 하우스", room: "304호" }, status: "ANSWERED", lastContent: "처리해 드렸습니다.", lastSentAt: "10. 16", unreadCount: 0, processingMemo: "방문 차량 등록 완료", messages: [{ id: 1, senderType: "USER", content: "방문 차량 등록 부탁드립니다.", sentAt: "2025. 10. 16. 오전 9:12", isRead: true }, { id: 2, senderType: "ADMIN", content: "차량 등록을 처리해 드렸습니다.", sentAt: "2025. 10. 16. 오전 10:05", isRead: true }] },
  { id: 5, participant: { id: 301, name: "한지호", type: "MEMBER", avatarTone: "beige", building: "성수 로프트", room: "-" }, status: "ANSWERED", lastContent: "투어 예약이 확정되었습니다.", lastSentAt: "10. 15", unreadCount: 0, processingMemo: "투어 예약", messages: [{ id: 1, senderType: "USER", content: "지점 투어를 신청하고 싶습니다.", sentAt: "2025. 10. 15. 오전 11:20", isRead: true }, { id: 2, senderType: "ADMIN", content: "토요일 오후 2시 투어 예약이 확정되었습니다.", sentAt: "2025. 10. 15. 오후 1:10", isRead: true }] },
  { id: 6, participant: { name: "이수빈", type: "GUEST_INQUIRY", phone: "010-5832-1940", email: "subin.lee@email.com", privacyConsentAt: "2025. 10. 18. 오전 9:31" }, status: "UNANSWERED", lastContent: "망원 지점에 주차가 가능한가요?", lastSentAt: "오전 9:31", unreadCount: 1, processingMemo: "", messages: [{ id: 1, senderType: "USER", content: "망원 지점에 주차가 가능한가요?", sentAt: "2025. 10. 18. 오전 9:31", isRead: false }] },
]

export const faqDocuments: FaqDocumentFile[] = [
  { documentId: 1, title: "공통 이용 약관", scope: "COMMON", buildingName: null, fileName: "roomlink-terms.pdf", fileType: "PDF", uploadedAt: "2026-10-20T10:30:00+09:00", indexStatus: "COMPLETED", indexError: null },
  { documentId: 2, title: "계약·환불 규정", scope: "COMMON", buildingName: null, fileName: "contract-refund.docx", fileType: "DOCX", uploadedAt: "2026-10-21T14:12:00+09:00", indexStatus: "COMPLETED", indexError: null },
  { documentId: 3, title: "망원 스테이 이용 규정", scope: "BUILDING", buildingName: "망원 스테이", fileName: "mangwon-guide.pdf", fileType: "PDF", uploadedAt: "2026-10-22T09:05:00+09:00", indexStatus: "COMPLETED", indexError: null },
  { documentId: 4, title: "연남 하우스 이용 규정", scope: "BUILDING", buildingName: "연남 하우스", fileName: "yeonnam-guide.md", fileType: "MD", uploadedAt: "2026-10-22T11:40:00+09:00", indexStatus: "COMPLETED", indexError: null },
  { documentId: 5, title: "공용 시설 사용 안내", scope: "COMMON", buildingName: null, fileName: "shared-facilities.txt", fileType: "TXT", uploadedAt: "2026-10-25T16:20:00+09:00", indexStatus: "PENDING", indexError: null },
]
export const faqIndexSummary: FaqIndexSummary = { lastReflectedAt: "2026-10-24T18:30:00+09:00", reflectedCount: 4, pendingChanges: 2 }
export const uploadedFaqDocument: FaqDocumentUploadResponse = { documentId: 6, indexStatus: "PENDING" }
export const faqReindexStarted: FaqReindexResponse = { jobId: "j_10", status: "PROCESSING" }
export const faqReindexCompleted: FaqReindexJobResponse = { status: "COMPLETED", processedCount: 5 }
export const faqAnswersByBuilding: Record<number, FaqAskResponse> = {
  1: { answer: "망원 스테이는 최소 3개월부터 계약할 수 있어요. 중도 해지 시에는 계약·환불 규정에 따라 위약금이 발생할 수 있습니다.", sources: [{ documentId: 2, title: "계약·환불 규정" }, { documentId: 3, title: "망원 스테이 이용 규정" }], fallback: false, cached: false },
  2: { answer: "연남 하우스는 최소 3개월부터 계약할 수 있어요. 계약 변경과 환불 기준은 공통 계약·환불 규정을 따릅니다.", sources: [{ documentId: 2, title: "계약·환불 규정" }, { documentId: 4, title: "연남 하우스 이용 규정" }], fallback: false, cached: false },
  3: { answer: "최소 계약 기간은 3개월이며, 계약 변경과 환불 기준은 공통 계약·환불 규정을 따라요.", sources: [{ documentId: 2, title: "계약·환불 규정" }], fallback: false, cached: false },
}
export const faqFallbackAnswer: FaqAskResponse = { answer: "문서에서 답을 찾지 못했어요.", sources: [], fallback: true, cached: false }

export const adminReports: AdminReportListResponse = { summary: { receivedCount: 4, resolvedCount: 1, rejectedCount: 1 }, content: [
  { reportId: 77, targetType: "COMMENT", targetId: "11", sourceType: "POST", sourceId: 1, sourceTitle: "이번 주말 한강 피크닉 같이 가실 분?", targetUserId: 5002, targetUserName: "박준영", targetRoomNumber: "201호", buildingId: 2, buildingName: "연남 하우스", contentPreview: "저 갈래요! 음료 몇 개 챙겨갈게요.", reasonTypes: ["ABUSE", "HARASSMENT"], reportCount: 3, status: "RECEIVED", latestReportedAt: "2026-10-26T15:00:00+09:00" },
  { reportId: 78, targetType: "POST", targetId: "7", sourceType: "POST", sourceId: 7, sourceTitle: "공용 주방에 두고 간 텀블러 주인 찾습니다", targetUserId: 5003, targetUserName: "정하은", targetRoomNumber: "304호", buildingId: 2, buildingName: "연남 하우스", contentPreview: "공용 주방에 두고 간 텀블러를 찾습니다.", reasonTypes: ["PRIVACY"], reportCount: 1, status: "RECEIVED", latestReportedAt: "2026-10-26T14:20:00+09:00" },
  { reportId: 79, targetType: "COMMENT", targetId: "17", sourceType: "POST", sourceId: 1, sourceTitle: "이번 주말 한강 피크닉 같이 가실 분?", targetUserId: 5003, targetUserName: "정하은", targetRoomNumber: "304호", buildingId: 2, buildingName: "연남 하우스", contentPreview: "돗자리 하나 더 가져갈게요!", reasonTypes: ["SPAM"], reportCount: 1, status: "RECEIVED", latestReportedAt: "2026-10-26T13:40:00+09:00" },
  { reportId: 80, targetType: "CHAT_MESSAGE", targetId: "m_9001", sourceType: "CHAT_ROOM", sourceId: 501, sourceTitle: null, targetUserId: 5002, targetUserName: "박준영", targetRoomNumber: "201호", buildingId: 2, buildingName: "연남 하우스", contentPreview: "내일 라운지에서 봬요!", reasonTypes: ["HARASSMENT"], reportCount: 1, status: "RECEIVED", latestReportedAt: "2026-10-26T12:10:00+09:00" },
  { reportId: 70, targetType: "POST", targetId: "3", sourceType: "POST", sourceId: 3, sourceTitle: "중고 가구 판매합니다", targetUserId: 5004, targetUserName: "이서윤", targetRoomNumber: "405호", buildingId: 3, buildingName: "성수 로프트", contentPreview: "침대 프레임 판매합니다. 연락 주세요.", reasonTypes: ["SPAM"], reportCount: 2, status: "RESOLVED", latestReportedAt: "2026-10-24T11:30:00+09:00" },
  { reportId: 69, targetType: "USER", targetId: "5005", sourceType: "CHAT_ROOM", sourceId: 488, sourceTitle: null, targetUserId: 5005, targetUserName: "최도윤", targetRoomNumber: "204호", buildingId: 1, buildingName: "망원 스테이", contentPreview: "사용자에 대한 신고", reasonTypes: ["ETC"], reportCount: 1, status: "REJECTED", latestReportedAt: "2026-10-23T09:20:00+09:00" },
], page: 0, size: 20, totalElements: 6, totalPages: 1 }
export const adminReportDetails: Record<number, AdminReportDetail> = { 77: { target: { targetType: "COMMENT", targetId: "11", sourceType: "POST", sourceId: 1, sourceTitle: "이번 주말 한강 피크닉 같이 가실 분?", snapshot: { content: "저 갈래요! 음료 몇 개 챙겨갈게요.", authorName: "박준영", writtenAt: "2026-10-26T14:50:00+09:00" }, currentStatus: "ACTIVE" }, targetUser: { userId: 5002, name: "박준영", roomNumber: "201호", buildingName: "연남 하우스" }, reports: [
  { reportId: 77, reasonType: "ABUSE", reasonDetail: "욕설과 비방이 포함되어 있어요.", createdAt: "2026-10-26T15:00:00+09:00" },
  { reportId: 76, reasonType: "HARASSMENT", reasonDetail: "계속 불편한 댓글을 남겨요.", createdAt: "2026-10-26T14:58:00+09:00" },
  { reportId: 75, reasonType: "ABUSE", reasonDetail: null, createdAt: "2026-10-26T14:55:00+09:00" },
], history: { reportedCount: 5, warningCount: 1, sanctions: [{ sanctionId: 24, sanctionType: "WARNING", durationDays: null, reason: "부적절한 댓글 작성", startsAt: "2026-09-14T10:00:00+09:00", endsAt: null, revokedAt: null }] }, status: "RECEIVED", action: null, adminMemo: null, handledByName: null, handledAt: null } }
export const adminSanctions: AdminSanctionListResponse = { content: [
  { sanctionId: 31, userId: 5003, userName: "정하은", roomNumber: "304호", buildingName: "연남 하우스", sanctionType: "RESTRICTION", durationDays: 7, reason: "게시판 욕설", reportId: 77, startsAt: "2026-10-27T10:00:00+09:00", endsAt: "2026-11-03T10:00:00+09:00", active: true, createdByName: "연남 하우스 매니저", revokedAt: null, revokeReason: null },
  { sanctionId: 30, userId: 5002, userName: "박준영", roomNumber: "201호", buildingName: "연남 하우스", sanctionType: "SUSPENSION", durationDays: null, reason: "반복적인 괴롭힘", reportId: 74, startsAt: "2026-10-25T16:00:00+09:00", endsAt: null, active: true, createdByName: "연남 하우스 매니저", revokedAt: null, revokeReason: null },
  { sanctionId: 29, userId: 5005, userName: "최도윤", roomNumber: "204호", buildingName: "망원 스테이", sanctionType: "RESTRICTION", durationDays: 3, reason: "생활 규칙 반복 위반", reportId: 68, startsAt: "2026-10-20T09:00:00+09:00", endsAt: "2026-10-23T09:00:00+09:00", active: false, createdByName: "망원 스테이 매니저", revokedAt: "2026-10-21T18:00:00+09:00", revokeReason: "당사자 확인 후 조기 해제" },
], page: 0, size: 20, totalElements: 3, totalPages: 1 }
