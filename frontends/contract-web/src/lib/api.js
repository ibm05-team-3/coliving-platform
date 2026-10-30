import { MOCK_APPLICATION_NO, ROOM } from '../context/ApplyContext';

// ---------------------------------------------------------------
// 계약 서비스 API 호출은 모두 이 파일을 거친다.
// 화면(pages)은 fetch를 직접 쓰지 않고 여기 함수만 부른다.
//
// 목업 / 실제 전환
//   .env.local 에 VITE_USE_MOCK=1 → 서버 없이 목업 값 반환
//   미리보기 빌드(VITE_PREVIEW=1)도 서버가 없으므로 목업
//   그 외 → /contract-service/** 로 실제 호출 (vite.config.js 프록시가 넘김)
//
// 로그인 사용자
//   실제로는 gateway가 로그인 토큰을 확인하고 X-User 헤더를 넣어준다.
//   gateway 없이 8089로 직접 붙는 개발 중에는 .env.local 의 VITE_DEV_USER_ID 값을 X-User로 직접 보낸다.
//   (gateway를 거치면 gateway가 이 값을 덮어쓰거나 지우므로 운영에서는 의미 없음)
// ---------------------------------------------------------------
const USE_MOCK = import.meta.env.VITE_USE_MOCK === '1' || import.meta.env.VITE_PREVIEW === '1';
const DEV_USER_ID = import.meta.env.VITE_DEV_USER_ID;
const BASE = '/contract-service';

// 서버 에러를 화면에서 구분하기 위한 에러 (status로 404 / 409 / 503 등을 나눈다)
export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

async function request(path, { method = 'GET', body, headers } = {}) {
  let res;
  try {
    res = await fetch(BASE + path, {
      method,
      credentials: 'include', // 로그인 쿠키 → gateway가 토큰으로 바꿔서 X-User 헤더를 붙임
      headers: {
        'Content-Type': 'application/json',
        ...(DEV_USER_ID && { 'X-User': DEV_USER_ID }),
        ...headers,
      },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    // 서버(gateway)가 아예 꺼져 있거나 네트워크가 끊긴 경우
    throw new ApiError(0, '서버에 연결할 수 없어요. 잠시 후 다시 시도해주세요.');
  }

  if (!res.ok) {
    // CustomControllerAdvice의 buildError는 { message } JSON, RuntimeException 핸들러는 일반 문자열
    let message = `요청에 실패했어요. (${res.status})`;
    try {
      const text = await res.text();
      try {
        message = JSON.parse(text).message ?? message;
      } catch {
        if (text) message = text;
      }
    } catch {
      // 본문을 못 읽으면 기본 메시지 사용
    }
    throw new ApiError(res.status, message);
  }
  return res.json();
}

// 목업 응답에도 약간의 지연을 줘서 로딩 화면을 확인할 수 있게 함
const mock = (value, ms = 300) => new Promise((resolve) => setTimeout(() => resolve(value), ms));

// ---------------------------------------------------------------
// [1-1 호실 선택] → RoomApplyResponse
// 현재 백엔드 경로에 맞춤. 경로가 /apply/rooms/{roomId}로 바뀌면 이 줄만 수정
const ROOM_PATH = (roomId) => `/apply/room/${encodeURIComponent(roomId)}`;

export function getRoom(roomId) {
  if (USE_MOCK) return mock({ ...ROOM, roomId: Number(roomId) });
  return request(ROOM_PATH(roomId));
}

// ---------------------------------------------------------------
// [1-1 → 다음 단계] POST /apply/applications → ApplicationDraftResponse
// body: { roomId, moveInDate: "2026-11-02", months }
// 같은 호실로 작성 중인 신청서가 있으면 서버가 그걸 수정해서 같은 신청번호를 돌려준다
export function createApplication({ roomId, moveInDate, months }) {
  if (USE_MOCK) return mock({ applicationNo: MOCK_APPLICATION_NO, status: 'DRAFT' });
  return request('/apply/applications', {
    method: 'POST',
    body: { roomId: Number(roomId), moveInDate, months },
  });
}

// ---------------------------------------------------------------
// [1-2 신청자 정보] GET /apply/applications/{applicationNo}/form → ApplyFormResponse
export function getApplyForm(applicationNo) {
  if (USE_MOCK) {
    return mock({
      applicationNo,
      roomId: ROOM.roomId,
      buildingName: ROOM.buildingName,
      region: ROOM.region,
      roomName: ROOM.roomName,
      roomType: ROOM.roomType,
      moveInDate: '2026-11-02',
      moveOutDate: '2027-05-01',
      months: 6,
      monthlyFee: ROOM.monthlyFee,
      managementFee: ROOM.managementFee,
      deposit: ROOM.deposit,
      firstPaymentAmount: ROOM.monthlyFee + ROOM.deposit,
      name: '홍길동',
      phone: '010-1234-5678',
      email: 'gildong@email.com',
      address: null,
      contractLanguage: null,
      termsVersion: '1',
    });
  }
  return request(`/apply/applications/${encodeURIComponent(applicationNo)}/form`);
}

// ---------------------------------------------------------------
// [1-2 신청서 제출] POST /apply/applications/{applicationNo}/submit → ApplicationSubmitResponse
// body: { email, address, contractLang: "ko" | "en", termsVersion }
export function submitApplication(applicationNo, { email, address, contractLang, termsVersion }) {
  if (USE_MOCK) return mock({ applicationNo, status: 'SUBMITTED' });
  return request(`/apply/applications/${encodeURIComponent(applicationNo)}/submit`, {
    method: 'POST',
    body: { email, address, contractLang, termsVersion },
  });
}
