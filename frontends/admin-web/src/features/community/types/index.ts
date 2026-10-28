export type ResidentStatus = "입주 예정" | "거주 중" | "퇴거 예정" | "퇴거 완료"
export type Resident = {
  name: string;
  branch: string;
  room: string;
  moveIn: string;
  expires: string;
  status: ResidentStatus;
  payment: string;
  tone: string
}
export type ResidentDetail = Resident & {
  phone: string;
  email: string;
  memo: string
}
export type MoveScheduleEvent = { type: "in" | "out"; label: string }

export type NoteParticipantType = "CANDIDATE" | "RESIDENT" | "MEMBER" | "GUEST_INQUIRY"
export type NoteStatus = "UNANSWERED" | "ANSWERED"
export type NoteMessage = {
  id: number;
  senderType: "USER" | "ADMIN";
  content: string;
  sentAt: string;
  isRead: boolean
}
export type NoteParticipant = {
  id?: number;
  name: string;
  type: NoteParticipantType;
  avatarTone?: string;
  building?: string;
  room?: string;
  phone?: string;
  email?: string;
  privacyConsentAt?: string
}
export type AdminNote = {
  id: number;
  participant: NoteParticipant;
  status: NoteStatus;
  lastContent: string;
  lastSentAt: string;
  unreadCount: number;
  processingMemo: string;
  messages: NoteMessage[]
}

export type FaqDocumentScope = "COMMON" | "BUILDING"
export type FaqIndexStatus = "PENDING" | "PROCESSING" | "COMPLETED" | "FAILED"
export type FaqDocumentFile = {
  documentId: number;
  title: string;
  scope: FaqDocumentScope;
  buildingName: string | null;
  fileName: string;
  fileType: "PDF" | "DOCX" | "TXT" | "MD";
  uploadedAt: string;
  indexStatus: FaqIndexStatus;
  indexError: string | null
}
export type FaqDocumentUploadRequest = {
  file: File;
  title: string;
  scope: FaqDocumentScope;
  buildingId?: number
}
export type FaqDocumentUploadResponse = { documentId: number; indexStatus: FaqIndexStatus }
export type FaqSource = { documentId: number; title: string }
export type FaqAskRequest = { question: string; buildingId: number }
export type FaqAskResponse = {
  answer: string;
  sources: FaqSource[];
  fallback: boolean;
  cached: boolean
}
export type FaqReindexResponse = { jobId: string; status: "PROCESSING" }
export type FaqReindexJobResponse = { status: "PROCESSING" | "COMPLETED" | "FAILED"; processedCount: number }
export type FaqIndexSummary = {
  lastReflectedAt: string;
  reflectedCount: number;
  pendingChanges: number
}

export type ContentStatus = "ACTIVE" | "HIDDEN" | "DELETED"
export type ReportSourceType = "CHAT_ROOM" | "POST"
export type ReportTargetType = "POST" | "COMMENT" | "CHAT_MESSAGE" | "USER"
export type ReportReason = "ABUSE" | "HARASSMENT" | "PRIVACY" | "SPAM" | "INAPPROPRIATE" | "HOUSE_RULE" | "ETC"
export type ReportStatus = "RECEIVED" | "RESOLVED" | "REJECTED"
export type ReportAction = "REJECT" | "HIDE_CONTENT" | "DELETE_CONTENT" | "WARNING" | "RESTRICTION" | "SUSPENSION" | "OFFLINE_RECORD"
export type SanctionType = "WARNING" | "RESTRICTION" | "SUSPENSION"
export type AdminReportSummary = {
  receivedCount: number;
  resolvedCount: number;
  rejectedCount: number
}
export type AdminReportListItem = {
  reportId: number;
  targetType: ReportTargetType;
  targetId: string;
  sourceType: ReportSourceType;
  sourceId: number;
  sourceTitle: string | null;
  targetUserId: number;
  targetUserName: string;
  targetRoomNumber: string;
  buildingId: number;
  buildingName: string;
  contentPreview: string;
  reasonTypes: ReportReason[];
  reportCount: number;
  status: ReportStatus;
  latestReportedAt: string
}
export type AdminReportListResponse = {
  summary: AdminReportSummary;
  content: AdminReportListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number
}
export type AdminSanction = {
  sanctionId: number;
  userId: number;
  userName: string;
  roomNumber: string;
  buildingName: string;
  sanctionType: SanctionType;
  durationDays: number | null;
  reason: string;
  reportId: number | null;
  startsAt: string;
  endsAt: string | null;
  active: boolean;
  createdByName: string;
  revokedAt: string | null;
  revokeReason: string | null
}
export type AdminReportDetail = {
  target: {
    targetType: ReportTargetType;
    targetId: string;
    sourceType: ReportSourceType;
    sourceId: number;
    sourceTitle: string | null;
    snapshot: {
      content: string;
      authorName: string;
      writtenAt: string
    };
    currentStatus: ContentStatus
  };
  targetUser: {
    userId: number;
    name: string;
    roomNumber: string;
    buildingName: string
  };
  reports: {
    reportId: number;
    reasonType: ReportReason;
    reasonDetail: string | null;
    createdAt: string
  }[];
  history: {
    reportedCount: number;
    warningCount: number;
    sanctions: Pick<AdminSanction, "sanctionId" | "sanctionType" | "durationDays" | "reason" | "startsAt" | "endsAt" | "revokedAt">[]
  };
  status: ReportStatus;
  action: ReportAction | null;
  adminMemo: string | null;
  handledByName: string | null;
  handledAt: string | null
}
export type ReportActionRequest = {
  action: ReportAction;
  durationDays?: number | null;
  hideContent?: boolean;
  reason?: string | null;
  adminMemo?: string | null
}
export type ReportActionResponse = {
  reportId: number;
  handledReportCount: number;
  status: ReportStatus;
  action: ReportAction;
  sanctionId: number | null;
  endsAt: string | null;
  handledAt: string
}
export type AdminSanctionListResponse = {
  content: AdminSanction[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number
}
export type AdminReportFilter = {
  status?: ReportStatus;
  sourceType?: ReportSourceType;
  targetType?: ReportTargetType;
  buildingId?: number;
  sort?: "latest" | "count";
  page?: number;
  size?: number
}
export type AdminSanctionFilter = {
  activeOnly?: boolean;
  sanctionType?: SanctionType;
  buildingId?: number;
  keyword?: string;
  page?: number;
  size?: number
}

export const REPORT_REASON_LABELS: Record<ReportReason, string> = {
  ABUSE: "욕설·비방",
  HARASSMENT: "괴롭힘·스토킹",
  PRIVACY: "개인정보 노출",
  SPAM: "광고·도배",
  INAPPROPRIATE: "음란·부적절",
  HOUSE_RULE: "생활 규칙 위반",
  ETC: "기타"
}
export const REPORT_ACTION_LABELS: Record<ReportAction, string> = {
  REJECT: "반려",
  HIDE_CONTENT: "숨김",
  DELETE_CONTENT: "삭제",
  WARNING: "경고",
  RESTRICTION: "이용 제한",
  SUSPENSION: "커뮤니티 정지",
  OFFLINE_RECORD: "면담·계약 검토 기록"
}
export const SANCTION_TYPE_LABELS: Record<SanctionType, string> = {
  WARNING: "경고",
  RESTRICTION: "이용 제한",
  SUSPENSION: "정지"
}
