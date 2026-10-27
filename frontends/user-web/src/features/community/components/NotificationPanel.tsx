import { useEffect, useState } from "react"
import { useNavigate } from "react-router"
import { getUserNotifications } from "../api"
import { useCurrentUser } from "../currentUser"
import type { NotificationType, UserNotification } from "../types"
import { Icon, type IconName } from "."
import { useSupportWidget } from "./SupportWidgetContext"

const icons: Record<NotificationType, IconName> = { NOTE: "mail", CHAT: "message", COMMENT: "users", REPLY: "users" }
const displayTime = (value: string) => new Date(value).toLocaleString("ko-KR", { month: "numeric", day: "numeric", hour: "numeric", minute: "2-digit" })

export function NotificationPanel({ onClose }: { onClose: () => void }) {
  const user = useCurrentUser(); const navigate = useNavigate(); const { openWidget, markNotificationRead, markAllRead } = useSupportWidget(); const [notifications, setNotifications] = useState<UserNotification[]>([])
  useEffect(() => { void getUserNotifications().then((response) => setNotifications(user.role === "RESIDENT" ? response.content : response.content.filter((item) => item.type === "NOTE"))) }, [user.role])
  const unread = notifications.some((item) => !item.read)
  const open = (notification: UserNotification) => { markNotificationRead(notification.notificationId); setNotifications((items) => items.map((item) => item.notificationId === notification.notificationId ? { ...item, read: true } : item)); onClose(); if (notification.targetType === "NOTE") openWidget("notes"); else if (notification.targetType === "CHAT_ROOM") openWidget("chat", notification.targetId); else navigate(`/community/posts/${notification.targetId}`) }
  const readAll = () => { setNotifications((items) => items.map((item) => ({ ...item, read: true }))); markAllRead() }
  return <><header className="flex items-center justify-between border-b border-main/15 px-3 pb-3"><strong className="text-sm text-main">알림</strong><button disabled={!unread} onClick={readAll} className="text-xs font-semibold text-main disabled:opacity-40 focus-visible:outline-2 focus-visible:outline-accent">모두 읽음</button></header><div className="max-h-80 overflow-y-auto">{notifications.length === 0 ? <p className="p-5 text-center text-sm text-text/70">새 알림이 없어요.</p> : notifications.map((notification) => <button key={notification.notificationId} onClick={() => open(notification)} className={`relative flex w-full gap-3 border-b border-main/15 p-3 text-left focus-visible:outline-2 focus-visible:outline-accent ${notification.read ? "bg-white" : "bg-background"}`}><span className="rounded-full bg-main/10 p-2 text-main"><Icon name={icons[notification.type]} size={17} /></span><span className="min-w-0 flex-1"><strong className="block text-xs text-text">{notification.title}</strong><span className="mt-1 block text-xs leading-5 text-text/70">{notification.message}</span><time className="mt-1 block text-[10px] text-text/70">{displayTime(notification.createdAt)}</time></span>{!notification.read && <i className="mt-2 h-2 w-2 shrink-0 rounded-full bg-accent" />}</button>)}</div></>
}
