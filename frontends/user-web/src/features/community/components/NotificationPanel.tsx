import { useEffect, useState } from "react"
import { useNavigate } from "react-router"
import { useTranslation } from "react-i18next"
import { getUserNotifications } from "../api"
import { useCurrentUser } from "../currentUser"
import type { NotificationType, UserNotification } from "../types"
import { Icon, type IconName } from "."
import { useSupportWidget } from "./SupportWidgetContext"

const ICONS: Record<NotificationType, IconName> = {
  NOTE: "mail",
  CHAT: "message",
  COMMENT: "users",
  REPLY: "users"
}
const displayTime = (value: string, locale: string) => new Date(value).toLocaleString(locale, {
  month: "numeric",
  day: "numeric",
  hour: "numeric",
  minute: "2-digit"
})

export function NotificationPanel({ onClose }: { onClose: () => void }) {
  const { t, i18n } = useTranslation("community");
  const user = useCurrentUser();
  const navigate = useNavigate();
  const { openWidget, markNotificationRead, markAllRead } = useSupportWidget();
  const [notifications, setNotifications] = useState<UserNotification[]>([])
  useEffect(() => {
    void getUserNotifications().then((response) => setNotifications(user.role === "RESIDENT" ? response.content : response.content.filter((item) => item.type === "NOTE")))
  }, [user.role])
  const unread = notifications.some((item) => !item.read)
  const handleOpen = (notification: UserNotification) => {
    markNotificationRead(notification.notificationId);
    setNotifications((items) => items.map((item) => item.notificationId === notification.notificationId ? { ...item, read: true } : item));
    onClose();
    if (notification.targetType === "NOTE") openWidget("notes"); else if (notification.targetType === "CHAT_ROOM") openWidget("chat", notification.targetId); else navigate(`/community/posts/${notification.targetId}`)
  }
  const handleReadAll = () => {
    setNotifications((items) => items.map((item) => ({ ...item, read: true })));
    markAllRead()
  }
  return <>
    <header className="flex items-center justify-between border-b border-main/15 px-3 pb-3">
      <strong className="text-sm text-main">
        {t("notifications.title")}
      </strong>
      <button
        disabled={!unread}
        onClick={handleReadAll}
        className="text-xs font-semibold text-main disabled:opacity-40 focus-visible:outline-2 focus-visible:outline-accent"
      >
        {t("notifications.readAll")}
      </button>
    </header>
    <div className="max-h-80 overflow-y-auto">
      {notifications.length === 0 ? <p className="p-5 text-center text-sm text-text/70">
        {t("notifications.empty")}
      </p> : notifications.map((notification) => <button
        key={notification.notificationId}
        onClick={() => handleOpen(notification)}
        className={`relative flex w-full gap-3 border-b border-main/15 p-3 text-left focus-visible:outline-2 focus-visible:outline-accent ${notification.read ? "bg-white" : "bg-background"}`}
      >
        <span className="grid h-9 w-9 shrink-0 self-start place-items-center rounded-full bg-main/10 text-main">
          <Icon
            name={ICONS[notification.type]}
            size={17}
          />
        </span>
        <span className="min-w-0 flex-1">
          <strong className="block text-xs text-text">
            {notification.title}
          </strong>
          <span className="mt-1 block text-xs leading-5 text-text/70">
            {notification.message}
          </span>
          <time className="mt-1 block text-[10px] text-text/70">
            {displayTime(notification.createdAt, i18n.resolvedLanguage ?? i18n.language)}
          </time>
        </span>
        {!notification.read && <i className="mt-2 h-2 w-2 shrink-0 rounded-full bg-accent" />}
      </button>)}
    </div>
  </>
}
