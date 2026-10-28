import { createContext, useContext, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { moveOutDate } from '../lib/format';

// 화면 확인용 목업. 필드 이름은 RoomApplyResponse와 맞춤
// (GET /contract-service/apply/rooms/{roomId} 연결 시 이 값을 응답으로 교체)
export const ROOM = {
  roomId: 1,
  buildingName: '[건물명]',
  region: '서울 성동구',
  roomName: '302호',
  roomType: '1인실',
  thumbnailUrl: null,
  roomFacilities: ['전용 욕실'],
  area: 13.5,
  floorNumber: 3,
  monthlyFee: 780000,
  managementFee: 50000, // ⚠ RoomApplyResponse에 managementFee 추가 필요
  deposit: 1000000,
  available: true,
};

// 10/27 할인 제외 → 개월 수만 선택
export const PERIODS = [3, 6, 12];

// 목업 신청번호 (PreviewBar 등에서 사용). 실제로는 POST /apply/applications 응답의 applicationNo
export const MOCK_APPLICATION_NO = 'CT-20261021-0042';

// 첫 결제 금액 = 첫 달 이용료 + 첫 달 관리비 + 보증금 (표시용, 결제 시 서버가 다시 계산)
export const firstPaymentOf = (room) => room.monthlyFee + room.managementFee + room.deposit;

const ApplyContext = createContext(null);

export function ApplyProvider({ children }) {
  const [moveIn, setMoveIn] = useState(new Date(2026, 10, 2));
  const [months, setMonths] = useState(6);

  const value = useMemo(
    () => ({
      room: ROOM,
      moveIn,
      setMoveIn,
      months,
      setMonths,
      moveOut: moveOutDate(moveIn, months),
      firstPayment: firstPaymentOf(ROOM),
    }),
    [moveIn, months],
  );

  return <ApplyContext.Provider value={value}>{children}</ApplyContext.Provider>;
}

export const useApply = () => useContext(ApplyContext);

// 신청번호는 Context가 아니라 URL에서 읽는다 → 새로고침해도 유지
export const useApplicationNo = () => useParams().applicationNo;
