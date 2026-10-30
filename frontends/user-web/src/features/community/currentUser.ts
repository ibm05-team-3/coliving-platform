import { useTranslation } from "react-i18next"
import { isResident } from "../../resident"
import { useSession } from "../../session"
import type { UserRole } from "./types"

export type CurrentUser = {
  role: UserRole;
  userId: number;
  name: string;
  buildingName: string;
  roomNumber: string
}

export function communityDisplayName(nickname: string | null | undefined, anonymous: string) {
  const trimmed = typeof nickname === "string" ? nickname.trim() : ""
  return trimmed || anonymous
}

// TODO: 건물, 호실, 역할은 입주 정보 연동 후 교체
export function useCurrentUser(): CurrentUser {
  const session = useSession()
  const { t } = useTranslation("community")
  return {
    role: isResident() ? "RESIDENT" : "MEMBER",
    userId: 5001,
    name: communityDisplayName(session.status === "ready" ? session.nickname : null, t("common.anonymous")),
    buildingName: "망원 스테이",
    roomNumber: "302호"
  }
}
