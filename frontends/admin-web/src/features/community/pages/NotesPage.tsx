import { type FormEvent, useEffect, useMemo, useState } from "react"
import { getAdminNotes, searchNoteRecipients, updateAdminNoteStatus } from "../api"
import { Avatar, Badge, Button, Icon, SearchField, Select } from "../components"
import type { AdminNote, NoteParticipant, NoteParticipantType, NoteStatus } from "../types"

const TYPE_LABELS: Record<NoteParticipantType, string> = {
  CANDIDATE: "후보자",
  RESIDENT: "입주자",
  MEMBER: "회원",
  GUEST_INQUIRY: "비회원 문의"
}
const TYPE_TABS = [
  "전체",
  "후보자",
  "입주자",
  "회원",
  "비회원 문의"
]
const FIELD_CLASS = "w-full rounded-lg border border-main/15 bg-white px-3 py-2.5 text-sm text-text outline-none focus-visible:outline-2 focus-visible:outline-accent"

export default function NotesPage({ title }: { title: string }) {
  const [notes, setNotes] = useState<AdminNote[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [typeTab, setTypeTab] = useState("전체")
  const [statusFilter, setStatusFilter] = useState("전체")
  const [query, setQuery] = useState("")
  const [reply, setReply] = useState("")
  const [processingStatus, setProcessingStatus] = useState<NoteStatus>("UNANSWERED")
  const [processingMemo, setProcessingMemo] = useState("")
  const [writeOpen, setWriteOpen] = useState(false)
  const [recipientQuery, setRecipientQuery] = useState("")
  const [recipientResults, setRecipientResults] = useState<NoteParticipant[]>([])
  const [recipient, setRecipient] = useState<NoteParticipant | null>(null)
  const [newContent, setNewContent] = useState("")

  useEffect(() => {
    document.title = title
  }, [title])
  useEffect(() => {
    getAdminNotes().then((data) => {
      const first = data[0];
      setNotes(data);
      setSelectedId(first?.id ?? null);
      if (first) {
        setProcessingStatus(first.status);
        setProcessingMemo(first.processingMemo)
      }
    })
  }, [])
  useEffect(() => {
    searchNoteRecipients(recipientQuery).then(setRecipientResults)
  }, [recipientQuery])

  const selected = notes.find((note) => note.id === selectedId) ?? null
  const handleSelectNote = (note: AdminNote) => {
    setSelectedId(note.id);
    setProcessingStatus(note.status);
    setProcessingMemo(note.processingMemo)
  }
  const list = useMemo(() => notes.filter((note) => {
    const matchesType = typeTab === "전체" || TYPE_LABELS[note.participant.type] === typeTab
    const matchesStatus = statusFilter === "전체" || (statusFilter === "미답변" ? note.status === "UNANSWERED" : note.status === "ANSWERED")
    return matchesType && matchesStatus && (note.participant.name.includes(query) || note.lastContent.includes(query))
  }), [
    notes,
    query,
    statusFilter,
    typeTab
  ])

  const handleSendReply = async (event: FormEvent) => {
    event.preventDefault()
    if (!selected || !reply.trim()) return
    await updateAdminNoteStatus(selected.id, "ANSWERED")
    const message = {
      id: Date.now(),
      senderType: "ADMIN" as const,
      content: reply.trim(),
      sentAt: "방금",
      isRead: false
    }
    setNotes((items) => items.map((note) => note.id === selected.id ? {
      ...note,
      status: "ANSWERED",
      lastContent: message.content,
      lastSentAt: "방금",
      messages: [...note.messages, message]
    } : note))
    setProcessingStatus("ANSWERED")
    setReply("")
    window.dispatchEvent(new Event("admin-badge-refresh"))
  }
  const handleSaveProcessing = async () => {
    if (!selected) return;
    await updateAdminNoteStatus(selected.id, processingStatus, processingMemo);
    setNotes((items) => items.map((note) => note.id === selected.id ? {
      ...note,
      status: processingStatus,
      processingMemo
    } : note));
    window.dispatchEvent(new Event("admin-badge-refresh"))
  }
  const handleSendNewNote = (event: FormEvent) => {
    event.preventDefault()
    if (!recipient || !newContent.trim()) return
    const note: AdminNote = {
      id: Math.max(0, ...notes.map((item) => item.id)) + 1,
      participant: recipient,
      status: "ANSWERED",
      lastContent: newContent.trim(),
      lastSentAt: "방금",
      unreadCount: 0,
      processingMemo: "",
      messages: [{
        id: Date.now(),
        senderType: "ADMIN",
        content: newContent.trim(),
        sentAt: "방금",
        isRead: false
      }]
    }
    setNotes((items) => [note, ...items]);
    handleSelectNote(note);
    setWriteOpen(false);
    setRecipient(null);
    setRecipientQuery("");
    setNewContent("")
  }

  return <div className="mx-auto max-w-[1500px] p-4 sm:p-6 lg:p-8">

    <header className="mb-7 flex items-end justify-between gap-4">
      <div>
        <p className="mb-2 text-xs font-bold tracking-[0.16em] text-accent">NOTES</p>
        <h1 className="text-2xl font-semibold text-main sm:text-3xl">
          {title}
        </h1>
      </div>
      <Button
        icon="edit"
        onClick={() => setWriteOpen(true)}
      >쪽지 쓰기</Button>
    </header>


    <div className="grid min-h-[680px] overflow-hidden rounded-lg border border-main/15 bg-white shadow-sm lg:grid-cols-[340px_minmax(420px,1fr)_280px]">

      <aside className="flex min-h-0 flex-col border-main/15 lg:border-r">

        <div className="space-y-3 border-b border-main/15 p-4">
          <SearchField
            value={query}
            onChange={setQuery}
            placeholder="이름, 내용 검색"
          />
          <div className="flex flex-wrap gap-1">
            {TYPE_TABS.map((item) => <button
              key={item}
              className={`rounded-lg px-2.5 py-1.5 text-xs font-semibold focus-visible:outline-2 focus-visible:outline-accent ${typeTab === item ? "bg-main text-white" : "bg-background text-text/70"}`}
              onClick={() => setTypeTab(item)}
            >
              {item}
            </button>)}
          </div>
          <Select
            label="처리 상태"
            value={statusFilter}
            onChange={setStatusFilter}
          >
            <option>전체</option>
            <option>미답변</option>
            <option>답변 완료</option>
          </Select>
        </div>


        <div className="min-h-0 flex-1 overflow-y-auto">
          {list.map((note) => <button
            className={`flex w-full items-center gap-3 border-b border-main/15 p-4 text-left focus-visible:outline-2 focus-visible:outline-accent ${selectedId === note.id ? "bg-background" : "bg-white"}`}
            key={note.id}
            onClick={() => handleSelectNote(note)}
          >
            {note.participant.type === "GUEST_INQUIRY" ? <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-background text-main">
              <Icon name="user" />
            </span> : <Avatar name={note.participant.name} />}
            <span className="min-w-0 flex-1">
              <span className="flex items-center gap-2">
                <strong className="text-sm text-text">
                  {note.participant.name}
                </strong>
                <small className="text-xs text-text/70">
                  {TYPE_LABELS[note.participant.type]}
                </small>
                <time className="ml-auto text-xs text-text/70">
                  {note.lastSentAt}
                </time>
              </span>
              <span className="mt-1 block truncate text-xs text-text/70">
                {note.lastContent}
              </span>
            </span>
            {note.unreadCount > 0 && <b className="flex h-5 min-w-5 items-center justify-center rounded-full bg-accent px-1 text-xs text-text">
              {note.unreadCount}
            </b>}
          </button>)}
          {list.length === 0 && <EmptyState />}
        </div>

      </aside>


      {!selected || list.length === 0 ? <section className="hidden items-center justify-center lg:flex">
        <EmptyState select />
      </section> : <section className="flex min-h-[560px] min-w-0 flex-col border-main/15 lg:border-r">
        <header className="flex items-center gap-3 border-b border-main/15 p-4">
          <Avatar name={selected.participant.name} />
          <div className="min-w-0 flex-1">
            <strong className="block text-sm text-text">
              {selected.participant.name}
            </strong>
            <span className="text-xs text-text/70">
              {TYPE_LABELS[selected.participant.type]}
            </span>
          </div>
          <Badge type={selected.status === "UNANSWERED" ? "warning" : "success"}>
            {selected.status === "UNANSWERED" ? "미답변" : "답변 완료"}
          </Badge>
        </header>
        <div className="min-h-0 flex-1 space-y-3 overflow-y-auto bg-background p-5">
          {selected.messages.map((message) => <article
            className={`max-w-[85%] rounded-lg border border-main/15 p-4 ${message.senderType === "ADMIN" ? "ml-auto bg-main text-white" : "bg-white text-text"}`}
            key={message.id}
          >
            <div className="mb-2 flex items-center justify-between gap-3">
              <strong className="text-xs">
                {message.senderType === "ADMIN" ? "관리자" : "사용자"}
              </strong>
              <span className="text-xs opacity-70">
                {message.senderType === "ADMIN" ? "보낸 쪽지" : "받은 쪽지"}
              </span>
            </div>
            <p className="text-sm leading-6">
              {message.content}
            </p>
            <footer className="mt-3 flex justify-between text-xs opacity-70">
              <time>
                {message.sentAt}
              </time>
              <span>
                {message.isRead ? "읽음" : "안 읽음"}
              </span>
            </footer>
          </article>)}
        </div>
        {selected.participant.type === "GUEST_INQUIRY" ? <div className="flex items-center justify-center gap-2 border-t border-main/15 bg-background p-4 text-sm text-text/70">
          <Icon
            name="phone"
            size={17}
          />
          비회원 문의는 전화 또는 이메일로 답변해 주세요</div> : <form
            className="flex gap-3 border-t border-main/15 p-4"
            onSubmit={handleSendReply}
          >
          <textarea
            className={`${FIELD_CLASS} min-h-20 flex-1 resize-none`}
            value={reply}
            onChange={(event) => setReply(event.target.value)}
            placeholder="답장 내용을 입력하세요"
          />
          <Button
            type="submit"
            icon="send"
          >답장 보내기</Button>
        </form>}
      </section>}


      {selected && list.length > 0 && <aside className="hidden min-w-0 lg:block">
        <div className="flex flex-col items-center border-b border-main/15 p-6 text-center">
          <Avatar
            name={selected.participant.name}
            size="xl"
          />
          <h2 className="mt-3 text-lg font-semibold text-text">
            {selected.participant.name}
          </h2>
          <div className="mt-2">
            <Badge type={selected.participant.type === "GUEST_INQUIRY" ? "warning" : "info"}>
              {TYPE_LABELS[selected.participant.type]}
            </Badge>
          </div>
        </div>
        <div className="space-y-3 border-b border-main/15 p-5 text-sm">
          {selected.participant.type === "GUEST_INQUIRY" ? <>
            <Info
              label="전화번호"
              value={selected.participant.phone}
            />
            <Info
              label="이메일"
              value={selected.participant.email}
            />
            <Info
              label="개인정보 동의"
              value={selected.participant.privacyConsentAt}
            />
          </> : <>
            <Info
              label="유형"
              value={TYPE_LABELS[selected.participant.type]}
            />
            <Info
              label="건물"
              value={selected.participant.building}
            />
            <Info
              label="호실"
              value={selected.participant.room}
            />
          </>}
        </div>
        <div className="space-y-4 p-5">
          <label className="block text-xs font-semibold text-text/70">처리 상태
            <select
              className={`${FIELD_CLASS} mt-2`}
              value={processingStatus}
              onChange={(event) => setProcessingStatus(event.target.value as NoteStatus)}
            >
              <option value="UNANSWERED">미답변</option>
              <option value="ANSWERED">답변 완료</option>
            </select>
          </label>
          <label className="block text-xs font-semibold text-text/70">처리 메모
            <textarea
              className={`${FIELD_CLASS} mt-2 min-h-24 resize-none`}
              value={processingMemo}
              onChange={(event) => setProcessingMemo(event.target.value)}
              placeholder="처리 내용을 기록하세요"
            />
          </label>
          <Button
            className="w-full"
            onClick={handleSaveProcessing}
          >저장</Button>
        </div>
      </aside>}

    </div>


    {writeOpen && <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-main/15 p-4"
      onMouseDown={() => setWriteOpen(false)}
    >
      <form
        className="w-full max-w-lg overflow-hidden rounded-lg border border-main/15 bg-white shadow-xl"
        onSubmit={handleSendNewNote}
        onMouseDown={(event) => event.stopPropagation()}
      >
        <header className="flex items-start justify-between border-b border-main/15 p-5">
          <div>
            <p className="mb-1 text-xs font-bold tracking-[0.16em] text-accent">NEW NOTE</p>
            <h2 className="text-lg font-semibold text-main">쪽지 쓰기</h2>
          </div>
          <button
            type="button"
            className="rounded-lg p-2 text-text/70 focus-visible:outline-2 focus-visible:outline-accent"
            onClick={() => setWriteOpen(false)}
            aria-label="닫기"
          >
            <Icon name="close" />
          </button>
        </header>
        <div className="space-y-4 p-5">
          <label className="block text-xs font-semibold text-text/70">받는 사람 검색
            <input
              className={`${FIELD_CLASS} mt-2`}
              value={recipientQuery}
              onChange={(event) => {
                setRecipientQuery(event.target.value);
                setRecipient(null)
              }}
              placeholder="회원 이름을 입력하세요"
            />
          </label>
          {recipientQuery && !recipient && <div className="max-h-44 overflow-y-auto rounded-lg border border-main/15">
            {recipientResults.map((item) => <button
              type="button"
              className="flex w-full items-center gap-3 border-b border-main/15 p-3 text-left focus-visible:outline-2 focus-visible:outline-accent"
              key={item.id}
              onClick={() => {
                setRecipient(item);
                setRecipientQuery(item.name)
              }}
            >
              <Avatar
                name={item.name}
                size="sm"
              />
              <span>
                <strong className="block text-sm text-text">
                  {item.name}
                </strong>
                <small className="text-xs text-text/70">
                  {TYPE_LABELS[item.type]}
                  ·
                  {item.building}
                </small>
              </span>
            </button>)}
          </div>}
          <label className="block text-xs font-semibold text-text/70">내용
            <textarea
              required
              className={`${FIELD_CLASS} mt-2 min-h-28 resize-none`}
              value={newContent}
              onChange={(event) => setNewContent(event.target.value)}
              placeholder="쪽지 내용을 입력하세요"
            />
          </label>
        </div>
        <footer className="flex justify-end gap-2 border-t border-main/15 bg-background p-4">
          <Button
            variant="secondary"
            onClick={() => setWriteOpen(false)}
          >취소</Button>
          <Button type="submit">보내기</Button>
        </footer>
      </form>
    </div>}

  </div>
}

function Info({ label, value }: { label: string; value?: string }) {
  return <div className="flex items-start justify-between gap-3">
    <span className="text-text/70">
      {label}
    </span>
    <strong className="text-right text-text">
      {value ?? "-"}
    </strong>
  </div>
}
function EmptyState({ select = false }: { select?: boolean }) {
  return <div className="flex min-h-64 flex-col items-center justify-center p-6 text-center text-text/70">
    <span className="mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-background text-main">
      <Icon
        name="mail"
        size={24}
      />
    </span>
    <strong className="text-sm text-text">
      {select ? "쪽지를 선택해 주세요" : "쪽지가 없습니다"}
    </strong>
    <p className="mt-1 text-xs">
      {select ? "목록에서 확인할 쪽지를 선택하세요." : "필터 조건을 변경해 보세요."}
    </p>
  </div>
}
