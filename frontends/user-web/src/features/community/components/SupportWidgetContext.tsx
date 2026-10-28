import { createContext, type ReactNode, useContext, useEffect, useState } from "react"
import { getUnreadNotificationSummary, getUserNotifications, readAllNotifications, readAllUserNotes, readChatRoom, readNotification } from "../api"
import { useCurrentUser } from "../currentUser"
import type { UnreadNotificationSummary } from "../types"

export type SupportWidgetTab = "faq" | "inquiry" | "notes" | "chat"
type SupportWidgetContextValue = {
  isOpen: boolean;
  activeTab: SupportWidgetTab;
  selectedChatRoomId: string | null;
  unreadSummary: UnreadNotificationSummary;
  notificationUnreadCount: number;
  openWidget: (tab?: SupportWidgetTab, roomId?: string | null) => void;
  closeWidget: () => void;
  setActiveTab: (tab: SupportWidgetTab) => void;
  selectChatRoom: (roomId: string | null) => void;
  markNotesRead: () => void;
  markChatRead: (roomId: string, count: number) => void;
  markNotificationRead: (notificationId: number) => void;
  markAllRead: () => void
}
const EMPTY_NOTIFICATION_SUMMARY: UnreadNotificationSummary = {
  note: 0,
  chat: 0,
  total: 0
}
const SUPPORT_WIDGET_CONTEXT = createContext<SupportWidgetContextValue | null>(null)

export function SupportWidgetProvider({ children }: { children: ReactNode }) {
  const { role } = useCurrentUser();
  const [isOpen, setIsOpen] = useState(false);
  const [activeTab, setActiveTabState] = useState<SupportWidgetTab>("faq");
  const [selectedChatRoomId, setSelectedChatRoomId] = useState<string | null>(null);
  const [unreadSummary, setUnreadSummary] = useState(EMPTY_NOTIFICATION_SUMMARY);
  const [notificationUnreadCount, setNotificationUnreadCount] = useState(0)
  const refreshNotificationCount = () => {
    void getUserNotifications().then((response) => setNotificationUnreadCount(response.content.filter((item) => !item.read && (role === "RESIDENT" || item.type === "NOTE")).length))
  }
  useEffect(() => {
    void getUnreadNotificationSummary().then((summary) => setUnreadSummary(role === "RESIDENT" ? summary : {
      note: summary.note,
      chat: 0,
      total: summary.note
    }));
    refreshNotificationCount();
    setActiveTabState("faq");
    setSelectedChatRoomId(null)
  }, [role])
  const setActiveTab = (tab: SupportWidgetTab) => {
    setActiveTabState(tab);
    if (tab !== "chat") setSelectedChatRoomId(null)
  }
  const openWidget = (tab: SupportWidgetTab = "faq", roomId: string | null = null) => {
    setIsOpen(true);
    setActiveTabState(tab);
    setSelectedChatRoomId(tab === "chat" ? roomId : null)
  }
  useEffect(() => {
    const handleOpenFromEvent = (event: Event) => {
      const detail = (event as CustomEvent<{ tab?: SupportWidgetTab; roomId?: string }>).detail;
      openWidget(detail?.tab ?? "faq", detail?.roomId ?? null)
    };
    window.addEventListener("roomlink:open-chat", handleOpenFromEvent);
    return () => window.removeEventListener("roomlink:open-chat", handleOpenFromEvent)
  }, [])
  const markNotesRead = () => {
    void readAllUserNotes().then(refreshNotificationCount);
    setUnreadSummary((current) => ({
      note: 0,
      chat: current.chat,
      total: current.chat
    }))
  }
  const markChatRead = (roomId: string, count: number) => {
    void readChatRoom(roomId).then(refreshNotificationCount);
    setUnreadSummary((current) => {
      const chat = Math.max(0, current.chat - count);
      return {
        note: current.note,
        chat,
        total: current.note + chat
      }
    })
  }
  const markNotificationRead = (notificationId: number) => {
    void readNotification(notificationId).then(refreshNotificationCount)
  }
  const markAllRead = () => {
    void readAllNotifications();
    setUnreadSummary(EMPTY_NOTIFICATION_SUMMARY);
    setNotificationUnreadCount(0)
  }
  return <SUPPORT_WIDGET_CONTEXT.Provider value={{
    isOpen,
    activeTab,
    selectedChatRoomId,
    unreadSummary,
    notificationUnreadCount,
    openWidget,
    closeWidget: () => setIsOpen(false),
    setActiveTab,
    selectChatRoom: setSelectedChatRoomId,
    markNotesRead,
    markChatRead,
    markNotificationRead,
    markAllRead
  }}>
    {children}
  </SUPPORT_WIDGET_CONTEXT.Provider>
}
export function useSupportWidget() {
  const value = useContext(SUPPORT_WIDGET_CONTEXT);
  if (!value) throw new Error("useSupportWidget은 SupportWidgetProvider 안에서 사용해야 합니다.");
  return value
}
