import { type FormEvent, useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { createGuestInquiry, createUserNote, getChatRoomMessages, getChatRooms, getFaqItems, getUserNotes } from "../api"
import { useCurrentUser } from "../currentUser"
import type { ChatMessage, ChatRoom, FaqItem, GuestInquiryRequest, ReportTargetType, UserNote } from "../types"
import { Avatar, Icon } from "."
import { BlockConfirmModal } from "./BlockConfirmModal"
import { ReportModal } from "./ReportModal"
import { useMySanction } from "./useMySanction"
import { useSupportWidget, type SupportWidgetTab } from "./SupportWidgetContext"

const ROLE_TABS: Record<"RESIDENT" | "MEMBER", SupportWidgetTab[]> = {
  MEMBER: ["faq", "notes"],
  RESIDENT: [
    "faq",
    "notes",
    "chat"
  ]
}
const EMPTY_INQUIRY: GuestInquiryRequest = {
  name: "",
  phone: "",
  email: "",
  content: "",
  privacyAgreed: false
}
const displayTime = (value: string, locale: string) => new Date(value).toLocaleTimeString(locale, { hour: "numeric", minute: "2-digit" })
const FIELD = "w-full rounded-lg border border-main/15 bg-white px-3 py-2 text-sm text-text focus-visible:outline-2 focus-visible:outline-accent disabled:bg-background"

export function SupportWidget() {
  const { t, i18n } = useTranslation("community")
  const locale = i18n.resolvedLanguage ?? i18n.language
  const user = useCurrentUser()
  const sanction = useMySanction()
  const { isOpen, activeTab, selectedChatRoomId, unreadSummary, closeWidget, setActiveTab, selectChatRoom, markNotesRead, markChatRead } = useSupportWidget()
  const [faqs, setFaqs] = useState<FaqItem[]>([])
  const [selectedFaq, setSelectedFaq] = useState<FaqItem | null>(null)
  const [notes, setNotes] = useState<UserNote[]>([])
  const [noteText, setNoteText] = useState("")
  const [rooms, setRooms] = useState<ChatRoom[]>([])
  const [messages, setMessages] = useState<Record<string, ChatMessage[]>>({})
  const [chatText, setChatText] = useState("")
  const [inquiry, setInquiry] = useState(EMPTY_INQUIRY)
  const [inquirySent, setInquirySent] = useState(false)
  const [privacyOpen, setPrivacyOpen] = useState(false)
  const [chatMenu, setChatMenu] = useState(false)
  const [reportTarget, setReportTarget] = useState<{
    targetType: ReportTargetType;
    targetId: string;
    targetName: string
  } | null>(null)
  const [blockTarget, setBlockTarget] = useState<{ userId: number; name: string } | null>(null)
  const scrollRef = useRef<HTMLDivElement>(null)
  const tabs = ROLE_TABS[user.role]
  const room = rooms.find((item) => item.roomId === selectedChatRoomId) ?? null
  const roomMessages = selectedChatRoomId ? messages[selectedChatRoomId] ?? [] : []
  const inquiryValid = inquiry.name.trim() && inquiry.phone.trim() && inquiry.email.trim() && inquiry.content.trim() && inquiry.privacyAgreed

  useEffect(() => {
    void Promise.all([
      getFaqItems(),
      getUserNotes(),
      getChatRooms()
    ]).then(([faqData, noteData, roomData]) => {
      const enabled = faqData.filter((item) => item.enabled);
      setFaqs(enabled);
      setSelectedFaq(enabled.find((item) => item.question === "최소 계약 기간") ?? enabled[0] ?? null);
      setNotes(noteData.content);
      setRooms(roomData)
    })
  }, [])
  useEffect(() => {
    if (!tabs.includes(activeTab)) setActiveTab("faq")
  }, [
    activeTab,
    setActiveTab,
    tabs
  ])
  useEffect(() => {
    if (isOpen && activeTab === "notes") {
      markNotesRead();
      setNotes((items) => items.map((item) => item.senderType === "ADMIN" ? { ...item, readAt: item.readAt ?? new Date().toISOString() } : item))
    }
  }, [
    activeTab,
    isOpen,
    markNotesRead
  ])
  useEffect(() => {
    if (!room) return;
    if (room.unreadCount > 0) {
      markChatRead(room.roomId, room.unreadCount);
      setRooms((items) => items.map((item) => item.roomId === room.roomId ? { ...item, unreadCount: 0 } : item))
    }
    if (!messages[room.roomId]) void getChatRoomMessages(room.roomId).then((response) => setMessages((items) => ({ ...items, [room.roomId]: response.messages })))
  }, [
    markChatRead,
    messages,
    room
  ])
  useEffect(() => {
    if (scrollRef.current) scrollRef.current.scrollTop = scrollRef.current.scrollHeight
  }, [
    activeTab,
    notes.length,
    roomMessages.length,
    selectedFaq
  ])
  const sourceTitle = useMemo(() => t("widget.branchSource", { branch: selectedFaq?.branch === "전체 공통" ? "망원 스테이" : selectedFaq?.branch }), [selectedFaq, t])
  if (!isOpen) return null

  const handleSendNote = async (event: FormEvent) => {
    event.preventDefault();
    const content = noteText.trim();
    if (!content) return;
    const response = await createUserNote({ content });
    setNotes((items) => [
      ...items,
      {
        noteId: response.noteId,
        senderType: "USER",
        senderName: user.name,
        content,
        createdAt: response.createdAt,
        readAt: null
      }
    ]);
    setNoteText("")
  }
  const handleSendChat = (event: FormEvent) => {
    event.preventDefault();
    const content = chatText.trim();
    if (!content || !selectedChatRoomId || sanction.active) return;
    const sentAt = new Date().toISOString();
    setMessages((items) => ({
      ...items,
      [selectedChatRoomId]: [
        ...(items[selectedChatRoomId] ?? []),
        {
          messageId: `local_${sentAt}`,
          senderId: user.userId,
          content,
          sentAt
        }
      ]
    }));
    setRooms((items) => items.map((item) => item.roomId === selectedChatRoomId ? {
      ...item,
      lastMessage: content,
      lastMessageAt: sentAt
    } : item));
    setChatText("")
  }
  const handleSubmitInquiry = async (event: FormEvent) => {
    event.preventDefault();
    if (!inquiryValid) return;
    await createGuestInquiry(inquiry);
    setInquirySent(true)
  }
  const handleBlocked = (userId: number) => {
    setRooms((items) => items.filter((item) => item.partner.userId !== userId));
    setBlockTarget(null);
    setChatMenu(false);
    selectChatRoom(null)
  }
  const sourceId = room ? Number(room.roomId.replace(/\D/g, "")) : 0

  return <section
    aria-label={t("widget.ariaLabel")}
    className="fixed right-4 bottom-20 z-40 flex h-[min(650px,calc(100dvh-7rem))] w-[min(390px,calc(100vw-2rem))] flex-col overflow-hidden rounded-lg border border-main/15 bg-white shadow-xl sm:right-6"
  >

    <div
      className="grid bg-background"
      style={{ gridTemplateColumns: `repeat(${tabs.length}, minmax(0, 1fr))` }}
    >
      {tabs.map((tab) => {
        const count = tab === "notes" ? unreadSummary.note : tab === "chat" ? unreadSummary.chat : 0;
        return <button
          key={tab}
          onClick={() => setActiveTab(tab)}
          className={`relative border-b-2 px-2 py-3 text-xs font-semibold focus-visible:outline-2 focus-visible:outline-accent ${activeTab === tab ? "border-main text-main" : "border-transparent text-text/70"}`}
        >
          {t(`widget.tabs.${tab}`)}
          {count > 0 && <span className="ml-1 rounded-full bg-accent px-1.5 py-0.5 text-[10px] text-text">
            {count}
          </span>}
        </button>
      })}
    </div>


    <header className="flex min-h-16 items-center gap-3 border-b border-main/15 px-4">

      {activeTab === "chat" && room ? <>
        <button
          aria-label={t("widget.backToChats")}
          onClick={() => selectChatRoom(null)}
          className="rounded p-1 text-main focus-visible:outline-2 focus-visible:outline-accent"
        >
          <Icon name="chevronLeft" />
        </button>
        <Avatar
          name={room.partner.name}
          size="sm"
        />
        <div className="min-w-0 flex-1">
          <strong className="block text-sm text-text">
            {room.partner.name}
          </strong>
          <span className="text-xs text-text/70">
            {room.partner.roomNumber}
          </span>
        </div>
        <div className="relative">
          <button
            aria-label={t("widget.roomMenu")}
            onClick={() => setChatMenu(!chatMenu)}
            className="rounded p-2 text-text/70"
          >
            <Icon name="more" />
          </button>
          {chatMenu && <div className="absolute top-full right-0 z-10 w-40 rounded-lg border border-main/15 bg-white p-1">
            <button
              onClick={() => {
                setReportTarget({
                  targetType: "USER",
                  targetId: String(room.partner.userId),
                  targetName: room.partner.name
                });
                setChatMenu(false)
              }}
              className="flex w-full items-center gap-2 rounded p-2 text-xs hover:bg-background"
            >
              <Icon
                name="flag"
                size={13}
              />
              {t("widget.reportUser")}
            </button>
            <button
              onClick={() => {
                setBlockTarget({ userId: room.partner.userId, name: room.partner.name });
                setChatMenu(false)
              }}
              className="flex w-full items-center gap-2 rounded p-2 text-xs hover:bg-background"
            >
              <Icon
                name="lock"
                size={13}
              />
              {t("widget.block")}
            </button>
          </div>}
        </div>
      </> : <>
        <span className="rounded-full bg-main/10 p-2 text-main">
          <Icon
            name={activeTab === "faq" ? "bot" : activeTab === "chat" ? "message" : "mail"}
            size={20}
          />
        </span>
        <div className="min-w-0 flex-1">
          <strong className="block text-sm text-main">
            {t(activeTab === "faq" ? "widget.helper" : activeTab === "inquiry" ? "widget.tabs.inquiry" : activeTab === "notes" ? "widget.managerNotes" : "widget.residentChat")}
          </strong>
          <span className="text-xs text-text/70">
            {t(activeTab === "faq" ? "widget.autoReply" : activeTab === "notes" ? "widget.notesSubtitle" : activeTab === "chat" ? "widget.chatSubtitle" : "widget.inquirySubtitle")}
          </span>
        </div>
      </>}


      <button
        aria-label={t("widget.close")}
        onClick={closeWidget}
        className="rounded p-2 text-text/70 focus-visible:outline-2 focus-visible:outline-accent"
      >
        <Icon name="close" />
      </button>

    </header>



    {activeTab === "faq" && <>
      <div
        ref={scrollRef}
        className="flex-1 space-y-3 overflow-y-auto bg-background p-4"
      >
        <Bubble>
          {t("widget.faqHello")}
        </Bubble>
        {selectedFaq ? <>
          <Bubble outgoing>
            {selectedFaq.question}
          </Bubble>
          <Bubble>
            {selectedFaq.answer}
          </Bubble>
          <p className="text-xs text-text/70">
            {t("widget.source", { source: sourceTitle })}
          </p>
          <div className="flex gap-2">
            <SmallButton onClick={() => setSelectedFaq(null)}>
              {t("widget.otherQuestion")}
            </SmallButton>
            <SmallButton onClick={() => setActiveTab("notes")}>
              {t("widget.askByNote")}
            </SmallButton>
          </div>
        </> : <div className="space-y-2">
          {faqs.map((faq) => <button
            key={faq.id}
            onClick={() => setSelectedFaq(faq)}
            className="flex w-full items-center justify-between rounded-lg border border-main/15 bg-white p-3 text-left text-sm text-text focus-visible:outline-2 focus-visible:outline-accent"
          >
            {faq.question}
            <Icon
              name="chevronRight"
              size={13}
            />
          </button>)}
        </div>}
      </div>
      <div className="flex items-center justify-center gap-2 border-t border-main/15 p-3 text-xs text-text/70">
        <Icon
          name="bot"
          size={15}
        />
        {t("widget.selectQuestion")}
      </div>
    </>}


    {activeTab === "inquiry" && <div
      ref={scrollRef}
      className="flex-1 overflow-y-auto p-4"
    >
      {inquirySent ? <div className="flex h-full flex-col items-center justify-center text-center">
        <span className="rounded-full bg-main/10 p-3 text-main">
          <Icon
            name="mail"
            size={24}
          />
        </span>
        <strong className="mt-4 text-text">
          {t("widget.inquiryDone")}
        </strong>
        <p className="mt-1 text-sm text-text/70">
          {t("widget.inquiryReply")}
        </p>
        <SmallButton onClick={() => {
          setInquiry(EMPTY_INQUIRY);
          setInquirySent(false)
        }}>
          {t("widget.newInquiry")}
        </SmallButton>
      </div> : <form
        onSubmit={handleSubmitInquiry}
        className="space-y-3"
      >
        <p className="rounded-lg bg-background p-3 text-xs text-text/70">
          {t("widget.inquiryGuide")}
        </p>
        <WidgetField label={t("widget.name")}>
          <input
            className={FIELD}
            value={inquiry.name}
            onChange={(event) => setInquiry({ ...inquiry, name: event.target.value })}
          />
        </WidgetField>
        <WidgetField label={t("widget.phone")}>
          <input
            className={FIELD}
            value={inquiry.phone}
            onChange={(event) => setInquiry({ ...inquiry, phone: event.target.value })}
            placeholder="010-0000-0000"
          />
        </WidgetField>
        <WidgetField label={t("widget.email")}>
          <input
            className={FIELD}
            type="email"
            value={inquiry.email}
            onChange={(event) => setInquiry({ ...inquiry, email: event.target.value })}
          />
        </WidgetField>
        <WidgetField label={t("widget.inquiryContent")}>
          <textarea
            className={`${FIELD} min-h-20 resize-y`}
            value={inquiry.content}
            onChange={(event) => setInquiry({ ...inquiry, content: event.target.value })}
          />
        </WidgetField>
        <label className="flex items-center gap-2 text-xs text-text">
          <input
            type="checkbox"
            className="accent-main"
            checked={inquiry.privacyAgreed}
            onChange={(event) => setInquiry({ ...inquiry, privacyAgreed: event.target.checked })}
          />
          {t("widget.privacyConsent")}
        </label>
        <button
          type="button"
          onClick={() => setPrivacyOpen(!privacyOpen)}
          className="text-xs font-semibold text-main"
        >
          {t("widget.details")}

          <Icon
            name="chevronDown"
            size={12}
          />
        </button>
        {privacyOpen && <p className="rounded-lg bg-background p-3 text-xs text-text/70">
          {t("widget.privacyDetail")}
        </p>}
        <button
          disabled={!inquiryValid}
          className="w-full rounded-lg bg-main py-2.5 text-sm font-semibold text-background disabled:opacity-50"
        >
          {t("widget.submitInquiry")}
        </button>
      </form>}
    </div>}


    {activeTab === "notes" && <>
      <div
        ref={scrollRef}
        className="flex-1 space-y-3 overflow-y-auto bg-background p-4"
      >
        <p className="text-center text-xs text-text/70">망원 스테이 매니저</p>
        {notes.map((note) => <Bubble
          key={note.noteId}
          outgoing={note.senderType === "USER"}
        >
          {note.content}
          <time className="mt-1 block text-[10px] opacity-70">
            {displayTime(note.createdAt, locale)}
            {note.senderType === "USER" && ` · ${t(note.readAt ? "widget.read" : "widget.sent")}`}
          </time>
        </Bubble>)}
      </div>
      <Composer
        value={noteText}
        onValueChange={setNoteText}
        onSubmit={handleSendNote}
        placeholder={t("widget.notePlaceholder")}
      />
    </>}


    {activeTab === "chat" && !room && <div className="flex-1 overflow-y-auto">
      {rooms.length === 0 ? <div className="flex h-full flex-col items-center justify-center gap-3 text-center text-sm text-text/70">
        <Icon
          name="message"
          size={26}
        />
        {t("widget.emptyChat")}
      </div> : rooms.map((item) => <button
        key={item.roomId}
        onClick={() => selectChatRoom(item.roomId)}
        className="flex w-full items-center gap-3 border-b border-main/15 p-4 text-left hover:bg-background focus-visible:outline-2 focus-visible:outline-accent"
      >
        <Avatar name={item.partner.name} />
        <span className="min-w-0 flex-1">
          <strong className="block text-sm text-text">
            {item.partner.name}

            <small className="text-text/70">
              {item.partner.roomNumber}
            </small>
          </strong>
          <span className="block truncate text-xs text-text/70">
            {item.lastMessage}
          </span>
        </span>
        <span className="text-right text-[10px] text-text/70">
          {displayTime(item.lastMessageAt, locale)}
          {item.unreadCount > 0 && <b className="ml-auto mt-1 block w-fit rounded-full bg-accent px-1.5 text-text">
            {item.unreadCount}
          </b>}
        </span>
      </button>)}
    </div>}


    {activeTab === "chat" && room && <>
      <div
        ref={scrollRef}
        className="flex-1 space-y-3 overflow-y-auto bg-background p-4"
      >
        {roomMessages.map((message) => {
          const incoming = message.senderId !== user.userId;
          return <div
            key={message.messageId}
            className={`group flex items-end gap-2 ${incoming ? "justify-start" : "justify-end"}`}
          >
            <Bubble outgoing={!incoming}>
              {message.content}
              <time className="mt-1 block text-[10px] opacity-70">
                {displayTime(message.sentAt, locale)}
              </time>
            </Bubble>
            {incoming && <button
              onClick={() => setReportTarget({
                targetType: "CHAT_MESSAGE",
                targetId: message.messageId,
                targetName: room.partner.name
              })}
              className="text-[10px] text-text/70 opacity-0 group-hover:opacity-100"
            >
              {t("widget.reportMessage")}
            </button>}
          </div>
        })}
      </div>
      {room.readOnly ? <div className="flex items-center justify-center gap-2 border-t border-main/15 p-3 text-xs text-text/70">
        <Icon
          name="message"
          size={15}
        />
        {t("widget.readOnly")}
      </div> : <>
        {sanction.active && <div className="border-t border-main/15 bg-accent/20 p-2 text-center text-xs text-text">
          <Icon
            name="lock"
            size={13}
          />

          {t("widget.sanctionGuide")}
        </div>}
        <Composer
          value={chatText}
          onValueChange={setChatText}
          onSubmit={handleSendChat}
          placeholder={t(sanction.active ? "widget.restrictedPlaceholder" : "widget.messagePlaceholder")}
          disabled={sanction.active}
        />
      </>}
    </>}


    {reportTarget && room && <ReportModal
      sourceType="CHAT_ROOM"
      sourceId={sourceId}
      targetType={reportTarget.targetType}
      targetId={reportTarget.targetId}
      targetName={reportTarget.targetName}
      targetUserId={room.partner.userId}
      onClose={() => setReportTarget(null)}
      onReported={(didBlock) => {
        const id = room.partner.userId;
        setReportTarget(null);
        if (didBlock) handleBlocked(id)
      }}
    />}


    {blockTarget && <BlockConfirmModal
      name={blockTarget.name}
      blockedUserId={blockTarget.userId}
      onClose={() => setBlockTarget(null)}
      onBlocked={() => handleBlocked(blockTarget.userId)}
    />}

  </section>
}

function Bubble({ children, outgoing = false }: { children: React.ReactNode; outgoing?: boolean }) {
  return <div className={`w-fit max-w-[82%] rounded-lg border border-main/15 px-3 py-2 text-sm leading-5 ${outgoing ? "ml-auto bg-main text-background" : "bg-white text-text"}`}>
    {children}
  </div>
}
function SmallButton({ children, onClick }: { children: React.ReactNode; onClick: () => void }) {
  return <button
    onClick={onClick}
    className="mt-3 rounded-lg border border-main/15 bg-white px-3 py-2 text-xs font-semibold text-main focus-visible:outline-2 focus-visible:outline-accent"
  >
    {children}
  </button>
}
function WidgetField({ label, children }: { label: string; children: React.ReactNode }) {
  return <label className="block text-xs font-semibold text-text">
    {label}

    <b className="text-accent">*</b>
    <span className="mt-1 block">
      {children}
    </span>
  </label>
}
function Composer({ value, onValueChange, onSubmit, placeholder, disabled = false }: {
  value: string;
  onValueChange: (value: string) => void;
  onSubmit: (event: FormEvent) => void;
  placeholder: string;
  disabled?: boolean
}) {
  const { t } = useTranslation("community");
  return <form
    onSubmit={onSubmit}
    className="flex gap-2 border-t border-main/15 p-3"
  >
    <input
      disabled={disabled}
      value={value}
      onChange={(event) => onValueChange(event.target.value)}
      placeholder={placeholder}
      className={FIELD}
    />
    <button
      disabled={disabled || !value.trim()}
      aria-label={t("widget.send")}
      className="rounded-lg bg-main p-2 text-background disabled:opacity-50"
    >
      <Icon
        name="send"
        size={18}
      />
    </button>
  </form>
}
