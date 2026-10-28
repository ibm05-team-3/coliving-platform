import { type FormEvent, useEffect, useMemo, useState } from "react"
import { getAdminReportDetail, getAdminReports, getAdminSanctions, handleReport, revokeSanction } from "../api"
import { Avatar, Badge, Button, Icon, PageHeader, SearchField, Select } from "../components"
import { REPORT_ACTION_LABELS, REPORT_REASON_LABELS, SANCTION_TYPE_LABELS } from "../types"
import type { AdminReportDetail, AdminReportListItem, AdminReportSummary, AdminSanction, ReportAction, ReportSourceType, ReportStatus, ReportTargetType, SanctionType } from "../types"

type Props = { title: string }
type MainTab = "reports" | "sanctions"

const STATUS_LABELS: Record<ReportStatus, string> = {
  RECEIVED: "처리 대기",
  RESOLVED: "처리 완료",
  REJECTED: "반려"
}
const TARGET_LABELS: Record<ReportTargetType, string> = {
  POST: "게시글",
  COMMENT: "댓글",
  CHAT_MESSAGE: "채팅 메시지",
  USER: "사용자"
}
const ACTION_DESCRIPTIONS: Record<ReportAction, string> = {
  REJECT: "신고를 반려하고 콘텐츠는 그대로 유지해요.",
  HIDE_CONTENT: "대상 콘텐츠를 숨겨 일반 사용자에게 보이지 않게 해요.",
  DELETE_CONTENT: "대상 콘텐츠를 삭제 상태로 변경해요.",
  WARNING: "대상자에게 경고 알림을 보내요.",
  RESTRICTION: "기간 동안 글·댓글·채팅 작성 불가, 읽기·쪽지·FAQ는 가능해요.",
  SUSPENSION: "관리자가 해제하기 전까지 커뮤니티 이용을 정지해요.",
  OFFLINE_RECORD: "면담·계약 검토 결과를 내부 기록으로 남겨요.",
}
const ACTION_ORDER: ReportAction[] = [
  "REJECT",
  "HIDE_CONTENT",
  "DELETE_CONTENT",
  "WARNING",
  "RESTRICTION",
  "SUSPENSION",
  "OFFLINE_RECORD"
]
const EMPTY_SUMMARY: AdminReportSummary = {
  receivedCount: 0,
  resolvedCount: 0,
  rejectedCount: 0
}
const INPUT_CLASS = "w-full rounded-lg border border-main/15 bg-white px-3 py-2 text-sm text-text focus-visible:outline-2 focus-visible:outline-accent"
const dateTime = (value: string | null) => value ? new Date(value).toLocaleString("ko-KR", {
  month: "long",
  day: "numeric",
  hour: "2-digit",
  minute: "2-digit"
}) : "-"
const shortDate = (value: string | null) => value ? new Date(value).toLocaleDateString("ko-KR") : "-"

export default function ReportsPage({ title }: Props) {
  const [tab, setTab] = useState<MainTab>("reports")
  const [status, setStatus] = useState<ReportStatus>("RECEIVED")
  const [sourceType, setSourceType] = useState<ReportSourceType | "">("")
  const [targetType, setTargetType] = useState<ReportTargetType | "">("")
  const [sort, setSort] = useState<"latest" | "count">("latest")
  const [summary, setSummary] = useState(EMPTY_SUMMARY)
  const [reports, setReports] = useState<AdminReportListItem[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [detail, setDetail] = useState<AdminReportDetail | null>(null)
  const [actionOpen, setActionOpen] = useState(false)
  const [action, setAction] = useState<ReportAction | null>(null)
  const [durationDays, setDurationDays] = useState<number | null>(null)
  const [hideContent, setHideContent] = useState(false)
  const [reason, setReason] = useState("")
  const [adminMemo, setAdminMemo] = useState("")
  const [actionStartedAt, setActionStartedAt] = useState(0)
  const [toast, setToast] = useState("")
  const [activeOnly, setActiveOnly] = useState(true)
  const [sanctionType, setSanctionType] = useState<SanctionType | "">("")
  const [query, setQuery] = useState("")
  const [sanctions, setSanctions] = useState<AdminSanction[]>([])
  const [revokeTarget, setRevokeTarget] = useState<AdminSanction | null>(null)
  const [revokeReason, setRevokeReason] = useState("")

  useEffect(() => {
    document.title = title
  }, [title])
  const loadReports = async () => {
    const response = await getAdminReports({
      status,
      sourceType: sourceType || undefined,
      targetType: targetType || undefined,
      sort
    })
    setSummary(response.summary);
    setReports(response.content)
    setSelectedId((current) => response.content.some((item) => item.reportId === current) ? current : response.content[0]?.reportId ?? null)
  }
  const loadSanctions = async () => {
    const response = await getAdminSanctions({
      activeOnly,
      sanctionType: sanctionType || undefined,
      keyword: query
    });
    setSanctions(response.content)
  }
  useEffect(() => {
    void loadReports()
  }, [
    status,
    sourceType,
    targetType,
    sort
  ])
  useEffect(() => {
    if (selectedId === null) setDetail(null); else void getAdminReportDetail(selectedId).then((value) => setDetail(value ?? null))
  }, [selectedId])
  useEffect(() => {
    if (tab === "sanctions") void loadSanctions()
  }, [
    tab,
    activeOnly,
    sanctionType,
    query
  ])

  const showToast = (message: string) => {
    setToast(message);
    window.setTimeout(() => setToast(""), 2600)
  }
  const restrictionEndsAt = useMemo(() => durationDays ? new Date(actionStartedAt + durationDays * 86_400_000).toISOString() : null, [actionStartedAt, durationDays])
  const actionValid = Boolean(action) && (action !== "RESTRICTION" || durationDays !== null) && (!action || ![
    "WARNING",
    "RESTRICTION",
    "SUSPENSION"
  ].includes(action) || Boolean(reason.trim())) && (action !== "OFFLINE_RECORD" || Boolean(adminMemo.trim()))
  const handleOpenAction = () => {
    setAction(null);
    setDurationDays(null);
    setHideContent(false);
    setReason("");
    setAdminMemo("");
    setActionStartedAt(Date.now());
    setActionOpen(true)
  }
  const handleSubmitAction = async (event: FormEvent) => {
    event.preventDefault()
    if (!selectedId || !action || !actionValid) return
    const response = await handleReport(selectedId, {
      action,
      durationDays,
      hideContent,
      reason: reason.trim() || null,
      adminMemo: adminMemo.trim() || null
    })
    window.dispatchEvent(new Event("admin-badge-refresh"))
    setActionOpen(false);
    showToast(`신고 ${response.handledReportCount}건을 처리했어요`);
    setStatus(response.status);
    await loadReports();
    setDetail((await getAdminReportDetail(selectedId)) ?? null)
  }
  const handleSubmitRevoke = async (event: FormEvent) => {
    event.preventDefault()
    if (!revokeTarget || !revokeReason.trim()) return
    const name = revokeTarget.userName
    await revokeSanction(revokeTarget.sanctionId, revokeReason.trim());
    setRevokeTarget(null);
    setRevokeReason("");
    showToast(`${name}님의 제재를 해제했어요`);
    await loadSanctions()
  }

  return <div>

    <PageHeader
      eyebrow="COMMUNITY SAFETY"
      title={title}
      description="입주자 신고를 확인하고 조치해요. 신고만으로 자동 숨김되지 않아요."
    />


    <div
      className="mb-5 flex gap-2 border-b border-main/15"
      role="tablist"
    >

      {([["reports", "신고 목록"], ["sanctions", "제재 내역"]] as const).map(([value, label]) => <button
        key={value}
        className={`border-b-2 px-4 py-3 text-sm font-semibold focus-visible:outline-2 focus-visible:outline-accent ${tab === value ? "border-main text-main" : "border-transparent text-text/70"}`}
        onClick={() => setTab(value)}
      >
        {label}
      </button>)}

    </div>



    {tab === "reports" ? <>

      <section className="mb-4 rounded-lg border border-main/15 bg-white p-4">

        <div className="mb-4 flex flex-wrap gap-2">
          {([
            "RECEIVED",
            "RESOLVED",
            "REJECTED"
          ] as ReportStatus[]).map((value) => <button
            key={value}
            onClick={() => setStatus(value)}
            className={`rounded-lg px-3 py-2 text-sm font-semibold focus-visible:outline-2 focus-visible:outline-accent ${status === value ? "bg-main text-white" : "bg-background text-text/70"}`}
          >
            {STATUS_LABELS[value]}

            <span className="ml-1">
              {value === "RECEIVED" ? summary.receivedCount : value === "RESOLVED" ? summary.resolvedCount : summary.rejectedCount}
            </span>
          </button>)}
        </div>


        <div className="flex flex-wrap gap-3">

          <Select
            label="신고 위치"
            value={sourceType}
            onChange={(value) => setSourceType(value as ReportSourceType | "")}
          >
            <option value="">전체</option>
            <option value="POST">게시글</option>
            <option value="CHAT_ROOM">채팅방</option>
          </Select>


          <Select
            label="대상 종류"
            value={targetType}
            onChange={(value) => setTargetType(value as ReportTargetType | "")}
          >
            <option value="">전체</option>
            <option value="POST">게시글</option>
            <option value="COMMENT">댓글</option>
            <option value="CHAT_MESSAGE">채팅 메시지</option>
            <option value="USER">사용자</option>
          </Select>


          <Select
            label="정렬"
            value={sort}
            onChange={(value) => setSort(value as "latest" | "count")}
          >
            <option value="latest">최신순</option>
            <option value="count">신고 많은 순</option>
          </Select>

        </div>

      </section>


      <div className="grid min-h-[660px] gap-4 xl:grid-cols-[minmax(320px,0.85fr)_minmax(480px,1.4fr)]">

        <section className="overflow-hidden rounded-lg border border-main/15 bg-white">

          <header className="flex items-center justify-between border-b border-main/15 p-4">
            <div>
              <h2 className="font-semibold text-main">
                {STATUS_LABELS[status]}
                신고</h2>
              <p className="mt-1 text-xs text-text/70">대상별로 묶인 신고를 확인하세요.</p>
            </div>
            <strong className="text-sm text-main">
              {reports.length}
              건</strong>
          </header>


          <div className="max-h-[590px] overflow-y-auto">
            {reports.map((report) => <button
              key={report.reportId}
              onClick={() => setSelectedId(report.reportId)}
              className={`block w-full border-b border-main/15 p-4 text-left transition focus-visible:outline-2 focus-visible:outline-accent ${selectedId === report.reportId ? "bg-main/10" : "hover:bg-background"}`}
            >

              <div className="flex items-center justify-between">
                <Badge type="info">
                  {TARGET_LABELS[report.targetType]}
                </Badge>
                <time className="text-xs text-text/70">
                  {dateTime(report.latestReportedAt)}
                </time>
              </div>


              <strong className="mt-3 block truncate text-sm text-text">
                {report.contentPreview}
              </strong>
              <p className="mt-1 truncate text-xs text-text/70">
                {report.sourceType === "POST" ? `게시글 · ${report.sourceTitle}` : "채팅방"}
              </p>


              <div className="mt-3 flex flex-wrap items-center gap-2 text-xs text-text/70">
                <span>
                  {report.targetUserName}
                  ·
                  {report.targetRoomNumber}
                </span>
                <span className="rounded-full bg-accent/20 px-2 py-1">
                  {REPORT_REASON_LABELS[report.reasonTypes[0]]}
                  {report.reasonTypes.length > 1 && ` 외 ${report.reasonTypes.length - 1}`}
                </span>
                {report.reportCount > 1 && <strong className="text-accent">
                  {report.reportCount}
                  건</strong>}
              </div>

            </button>)}
            {reports.length === 0 && <Empty
              icon="flag"
              title="처리할 신고가 없어요"
            />}
          </div>

        </section>


        <ReportDetail
          detail={detail}
          selectedId={selectedId}
          onAction={handleOpenAction}
        />

      </div>

    </> : <section className="rounded-lg border border-main/15 bg-white">

      <div className="flex flex-wrap items-center gap-3 border-b border-main/15 p-4">
        <label className="flex items-center gap-2 text-sm font-semibold text-text">
          <input
            type="checkbox"
            checked={activeOnly}
            onChange={(event) => setActiveOnly(event.target.checked)}
            className="accent-main"
          />
          진행 중만</label>
        <Select
          label="종류"
          value={sanctionType}
          onChange={(value) => setSanctionType(value as SanctionType | "")}
        >
          <option value="">전체</option>
          <option value="WARNING">경고</option>
          <option value="RESTRICTION">이용 제한</option>
          <option value="SUSPENSION">정지</option>
        </Select>
        <div className="w-full sm:ml-auto sm:w-64">
          <SearchField
            value={query}
            onChange={setQuery}
            placeholder="이름 검색"
          />
        </div>
      </div>


      <div className="overflow-x-auto">
        <table className="w-full min-w-[980px] text-left text-sm">
          <thead className="bg-background text-xs text-text/70">
            <tr>
              {[
                "대상자",
                "종류",
                "기간",
                "사유",
                "시작일",
                "해제 예정일",
                "상태",
                "처리 관리자",
                ""
              ].map((label) => <th
                key={label}
                className="px-4 py-3 font-semibold"
              >
                {label}
              </th>)}
            </tr>
          </thead>
          <tbody>
            {sanctions.map((item) => {
              const state = item.revokedAt ? "조기 해제" : item.active ? "진행 중" : "만료";
              return <tr
                key={item.sanctionId}
                className="border-t border-main/15 align-top"
              >
                <td className="px-4 py-4">
                  <strong className="block text-text">
                    {item.userName}
                  </strong>
                  <span className="text-xs text-text/70">
                    {item.roomNumber}
                    ·
                    {item.buildingName}
                  </span>
                </td>
                <td className="px-4 py-4">
                  <Badge>
                    {SANCTION_TYPE_LABELS[item.sanctionType]}
                  </Badge>
                </td>
                <td className="px-4 py-4">
                  {item.durationDays ? `${item.durationDays}일` : "-"}
                </td>
                <td className="max-w-48 px-4 py-4">
                  {item.reason}
                </td>
                <td className="px-4 py-4">
                  {shortDate(item.startsAt)}
                </td>
                <td className="px-4 py-4">
                  {item.sanctionType === "SUSPENSION" ? "해제 시까지" : shortDate(item.endsAt)}
                </td>
                <td className="px-4 py-4">
                  <Badge type={item.active ? "success" : "neutral"}>
                    {state}
                  </Badge>
                  {item.revokedAt && <small className="mt-1 block text-text/70">
                    {item.revokeReason}
                  </small>}
                </td>
                <td className="px-4 py-4">
                  {item.createdByName}
                </td>
                <td className="px-4 py-4">
                  {item.active && item.sanctionType !== "WARNING" && <Button
                    variant="secondary"
                    className="min-h-8 px-3 py-1"
                    onClick={() => {
                      setRevokeTarget(item);
                      setRevokeReason("")
                    }}
                  >해제</Button>}
                </td>
              </tr>
            })}
          </tbody>
        </table>
        {sanctions.length === 0 && <Empty
          icon="lock"
          title="제재 내역이 없어요"
        />}
      </div>

    </section>}



    {actionOpen && detail && <ActionModal
      detail={detail}
      action={action}
      setAction={setAction}
      durationDays={durationDays}
      setDurationDays={setDurationDays}
      hideContent={hideContent}
      setHideContent={setHideContent}
      reason={reason}
      setReason={setReason}
      adminMemo={adminMemo}
      setAdminMemo={setAdminMemo}
      restrictionEndsAt={restrictionEndsAt}
      valid={actionValid}
      onClose={() => setActionOpen(false)}
      onSubmit={handleSubmitAction}
    />}


    {revokeTarget && <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-text/40 p-4"
      onMouseDown={() => setRevokeTarget(null)}
    >
      <form
        role="dialog"
        aria-modal="true"
        onSubmit={handleSubmitRevoke}
        onMouseDown={(event) => event.stopPropagation()}
        className="w-full max-w-lg rounded-lg border border-main/15 bg-white p-6"
      >
        <ModalHead
          eyebrow="REVOKE SANCTION"
          title="제재 해제"
          onClose={() => setRevokeTarget(null)}
        />
        <div className="mb-4 rounded-lg bg-background p-4">
          <strong className="text-main">
            {revokeTarget.userName}
          </strong>
          <p className="mt-1 text-sm text-text/70">
            {SANCTION_TYPE_LABELS[revokeTarget.sanctionType]}
            ·
            {revokeTarget.reason}
          </p>
        </div>
        <label className="block text-sm font-semibold text-text">해제 사유 · 필수
          <textarea
            maxLength={500}
            className={`${INPUT_CLASS} mt-2 min-h-28 resize-y`}
            value={revokeReason}
            onChange={(event) => setRevokeReason(event.target.value)}
            placeholder="조기 해제 사유를 입력하세요."
          />
        </label>
        <div className="mt-6 flex justify-end gap-2">
          <Button
            variant="secondary"
            onClick={() => setRevokeTarget(null)}
          >취소</Button>
          <Button
            type="submit"
            disabled={!revokeReason.trim()}
          >해제하기</Button>
        </div>
      </form>
    </div>}


    {toast && <div className="fixed right-6 bottom-6 z-50 flex items-center gap-3 rounded-lg bg-main px-5 py-4 text-sm font-semibold text-white">
      <Icon name="flag" />
      {toast}
    </div>}

  </div>
}

function Empty({ icon, title }: { icon: "flag" | "lock"; title: string }) {
  return <div className="flex min-h-48 flex-col items-center justify-center gap-3 p-8 text-center text-text/70">
    <span className="rounded-full bg-main/10 p-3 text-main">
      <Icon
        name={icon}
        size={24}
      />
    </span>
    <strong className="text-text">
      {title}
    </strong>
    <p className="text-xs">필터 조건을 변경해 보세요.</p>
  </div>
}

function ReportDetail({ detail, selectedId, onAction }: {
  detail: AdminReportDetail | null;
  selectedId: number | null;
  onAction: () => void
}) {
  if (!detail) return <section className="rounded-lg border border-main/15 bg-white">
    <Empty
      icon="flag"
      title="신고를 선택해 주세요"
    />
  </section>
  return <section className="overflow-hidden rounded-lg border border-main/15 bg-white">
    <header className="flex items-center justify-between border-b border-main/15 p-5">
      <div>
        <span className="text-xs text-text/70">신고 #
          {selectedId}
        </span>
        <h2 className="mt-1 font-semibold text-main">
          {TARGET_LABELS[detail.target.targetType]}
          신고 상세</h2>
      </div>
      <Badge type={detail.status === "RECEIVED" ? "warning" : detail.status === "RESOLVED" ? "success" : "neutral"}>
        {STATUS_LABELS[detail.status]}
      </Badge>
    </header>
    <div className="max-h-[520px] space-y-6 overflow-y-auto p-5">

      <DetailSection title="신고 당시 내용">
        <article className="rounded-lg border border-main/15 bg-background p-4">
          <div className="mb-2 flex justify-end">
            {detail.target.currentStatus !== "ACTIVE" && <Badge type="warning">현재
              {detail.target.currentStatus === "HIDDEN" ? "숨김됨" : "삭제됨"}
            </Badge>}
          </div>
          <p className="text-sm leading-6 text-text">
            {detail.target.snapshot.content}
          </p>
          <footer className="mt-3 flex justify-between text-xs text-text/70">
            <span>
              {detail.target.snapshot.authorName}
            </span>
            <time>
              {dateTime(detail.target.snapshot.writtenAt)}
            </time>
          </footer>
        </article>
        <button
          type="button"
          className="mt-3 flex items-center gap-1 text-sm font-semibold text-main focus-visible:outline-2 focus-visible:outline-accent"
        >
          <Icon
            name="arrowRight"
            size={14}
          />
          신고 위치 ·
          {detail.target.sourceType === "POST" ? detail.target.sourceTitle : "채팅방"}
        </button>
      </DetailSection>


      <DetailSection title={`접수된 신고 ${detail.reports.length}건`}>
        <div className="space-y-2">
          {detail.reports.map((report, index) => <article
            key={report.reportId}
            className="rounded-lg border border-main/15 p-4"
          >
            <header className="flex flex-wrap items-center gap-2">
              <strong className="text-sm text-text">익명 신고
                {index + 1}
              </strong>
              <Badge type="accent">
                {REPORT_REASON_LABELS[report.reasonType]}
              </Badge>
              <time className="ml-auto text-xs text-text/70">
                {dateTime(report.createdAt)}
              </time>
            </header>
            <p className="mt-2 text-sm text-text/70">
              {report.reasonDetail || "상세 내용이 없습니다."}
            </p>
          </article>)}
        </div>
      </DetailSection>


      <DetailSection title="신고된 사용자">
        <article className="rounded-lg border border-main/15 p-4">
          <div className="flex items-center gap-3">
            <Avatar name={detail.targetUser.name} />
            <div>
              <strong className="text-text">
                {detail.targetUser.name}
              </strong>
              <p className="text-xs text-text/70">
                {detail.targetUser.roomNumber}
                ·
                {detail.targetUser.buildingName}
              </p>
            </div>
          </div>
          <div className="mt-4 grid grid-cols-2 gap-2 rounded-lg bg-background p-3 text-sm">
            <span>받은 신고
              <strong className="text-main">
                {detail.history.reportedCount}
                건</strong>
            </span>
            <span>경고
              <strong className="text-main">
                {detail.history.warningCount}
                회</strong>
            </span>
          </div>
          <div className="mt-4">
            <span className="text-xs font-semibold text-text/70">과거 제재 이력</span>
            {detail.history.sanctions.length === 0 ? <p className="mt-2 text-sm text-text/70">이력 없음</p> : detail.history.sanctions.map((item) => <div
              key={item.sanctionId}
              className="mt-2 flex flex-wrap items-center gap-2 text-sm"
            >
              <Badge>
                {SANCTION_TYPE_LABELS[item.sanctionType]}
              </Badge>
              <span>
                {item.reason}
              </span>
              <time className="ml-auto text-xs text-text/70">
                {shortDate(item.startsAt)}
              </time>
            </div>)}
          </div>
        </article>
      </DetailSection>


      {detail.status !== "RECEIVED" && <DetailSection title="조치 결과">
        <dl className="grid grid-cols-[90px_1fr] gap-2 rounded-lg bg-background p-4 text-sm">
          <dt className="text-text/70">조치</dt>
          <dd>
            {detail.action ? REPORT_ACTION_LABELS[detail.action] : "-"}
          </dd>
          <dt className="text-text/70">처리자</dt>
          <dd>
            {detail.handledByName || "-"}
          </dd>
          <dt className="text-text/70">처리 시각</dt>
          <dd>
            {dateTime(detail.handledAt)}
          </dd>
          <dt className="text-text/70">관리자 메모</dt>
          <dd>
            {detail.adminMemo || "없음"}
          </dd>
        </dl>
      </DetailSection>}

    </div>
    {detail.status === "RECEIVED" && <footer className="flex justify-end border-t border-main/15 p-4">
      <Button onClick={onAction}>조치하기</Button>
    </footer>}
  </section>
}

function DetailSection({ title, children }: { title: string; children: React.ReactNode }) {
  return <section>
    <h3 className="mb-3 text-sm font-semibold text-main">
      {title}
    </h3>
    {children}
  </section>
}
function ModalHead({ eyebrow, title, onClose }: {
  eyebrow: string;
  title: string;
  onClose: () => void
}) {
  return <header className="mb-5 flex items-start justify-between">
    <div>
      <p className="text-xs font-bold tracking-[0.14em] text-accent">
        {eyebrow}
      </p>
      <h2 className="mt-1 text-xl font-semibold text-main">
        {title}
      </h2>
    </div>
    <button
      type="button"
      aria-label="닫기"
      onClick={onClose}
      className="rounded-lg p-2 text-text/70 focus-visible:outline-2 focus-visible:outline-accent"
    >
      <Icon name="close" />
    </button>
  </header>
}

type ActionModalProps = {
  detail: AdminReportDetail;
  action: ReportAction | null;
  setAction: (value: ReportAction) => void;
  durationDays: number | null;
  setDurationDays: (value: number) => void;
  hideContent: boolean;
  setHideContent: (value: boolean) => void;
  reason: string;
  setReason: (value: string) => void;
  adminMemo: string;
  setAdminMemo: (value: string) => void;
  restrictionEndsAt: string | null;
  valid: boolean;
  onClose: () => void;
  onSubmit: (event: FormEvent) => void
}
function ActionModal(props: ActionModalProps) {
  return <div
    className="fixed inset-0 z-50 flex items-center justify-center bg-text/40 p-4"
    onMouseDown={props.onClose}
  >
    <form
      role="dialog"
      aria-modal="true"
      onSubmit={props.onSubmit}
      onMouseDown={(event) => event.stopPropagation()}
      className="max-h-[92vh] w-full max-w-3xl overflow-y-auto rounded-lg border border-main/15 bg-white p-6"
    >
      <ModalHead
        eyebrow="REPORT ACTION"
        title="신고 조치하기"
        onClose={props.onClose}
      />
      <fieldset>
        <legend className="mb-3 text-sm font-semibold text-text">조치 선택</legend>
        <div className="grid gap-2 sm:grid-cols-2">
          {ACTION_ORDER.map((item) => {
            const disabled = props.detail.target.targetType === "USER" && (item === "HIDE_CONTENT" || item === "DELETE_CONTENT");
            return <label
              key={item}
              className={`flex gap-3 rounded-lg border p-3 ${props.action === item ? "border-main bg-main/10" : "border-main/15"} ${disabled ? "cursor-not-allowed opacity-50" : "cursor-pointer"}`}
            >
              <input
                type="radio"
                name="action"
                checked={props.action === item}
                disabled={disabled}
                onChange={() => props.setAction(item)}
                className="mt-1 accent-main"
              />
              <span>
                <strong className="block text-sm text-main">
                  {REPORT_ACTION_LABELS[item]}
                </strong>
                <small className="mt-1 block leading-5 text-text/70">
                  {ACTION_DESCRIPTIONS[item]}
                </small>
              </span>
            </label>
          })}
        </div>
      </fieldset>


      {props.action === "RESTRICTION" && <div className="mt-5 rounded-lg bg-background p-4">
        <p className="mb-3 text-sm font-semibold text-text">이용 제한 기간 · 필수</p>
        <div className="flex gap-2">
          {[
            1,
            3,
            7,
            30
          ].map((days) => <button
            type="button"
            key={days}
            onClick={() => props.setDurationDays(days)}
            className={`rounded-lg px-3 py-2 text-sm font-semibold focus-visible:outline-2 focus-visible:outline-accent ${props.durationDays === days ? "bg-main text-white" : "bg-white text-text"}`}
          >
            {days}
            일</button>)}
        </div>
        {props.restrictionEndsAt && <p className="mt-3 text-sm text-main">해제 예정:
          {dateTime(props.restrictionEndsAt)}
        </p>}
      </div>}


      {props.action && [
        "WARNING",
        "RESTRICTION",
        "SUSPENSION"
      ].includes(props.action) && <div className="mt-5 space-y-4">
          <label className="flex items-center gap-2 text-sm text-text">
            <input
              type="checkbox"
              checked={props.hideContent}
              onChange={(event) => props.setHideContent(event.target.checked)}
              className="accent-main"
            />
            콘텐츠도 숨기기</label>
          <label className="block text-sm font-semibold text-text">사용자에게 보낼 사유 · 필수
            <textarea
              maxLength={500}
              value={props.reason}
              onChange={(event) => props.setReason(event.target.value)}
              className={`${INPUT_CLASS} mt-2 min-h-24 resize-y`}
              placeholder="사용자에게 안내할 사유를 입력하세요."
            />
            <small className="mt-1 block text-right font-normal text-text/70">
              {props.reason.length}
              /500</small>
          </label>
          {props.action === "SUSPENSION" && <p className="rounded-lg bg-accent/20 p-3 text-sm text-text">관리자가 해제하기 전까지 커뮤니티 이용이 정지돼요.</p>}
        </div>}


      {props.action === "OFFLINE_RECORD" && <p className="mt-5 rounded-lg bg-background p-4 text-sm text-text">오프라인으로 처리하고 기록만 남겨요. 관리자 메모를 반드시 작성해 주세요.</p>}


      <label className="mt-5 block text-sm font-semibold text-text">관리자 메모 ·
        {props.action === "OFFLINE_RECORD" ? "필수" : "선택"}

        <span className="font-normal text-text/70">(내부용)</span>
        <textarea
          value={props.adminMemo}
          onChange={(event) => props.setAdminMemo(event.target.value)}
          className={`${INPUT_CLASS} mt-2 min-h-24 resize-y`}
          placeholder="처리 판단과 참고 내용을 기록하세요."
        />
      </label>
      <div className="mt-6 flex justify-end gap-2">
        <Button
          variant="secondary"
          onClick={props.onClose}
        >취소</Button>
        <Button
          type="submit"
          disabled={!props.valid}
        >처리하기</Button>
      </div>
    </form>
  </div>
}
