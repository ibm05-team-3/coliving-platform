import { isResident } from "../../resident"
import type { UserRole } from "./types"

export type CurrentUser = {
  role: UserRole;
  userId: number;
  name: string;
  buildingName: string;
  roomNumber: string
}

// TODO: 로그인 연동 후 실제 사용자 정보로 교체
export function useCurrentUser(): CurrentUser {
  return {
    role: isResident() ? "RESIDENT" : "MEMBER",
    userId: 5001,
    name: "김민서",
    buildingName: "망원 스테이",
    roomNumber: "302호"
  }
}
