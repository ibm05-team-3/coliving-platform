import { createContext, useContext, useMemo, useState } from 'react';
import { moveOutDate } from '../lib/format';

// 화면 확인용 목업 데이터. 실제로는 매물 서비스(gRPC) 조회 결과로 채운다.
export const ROOM = {
  buildingName: '[건물명]',
  area: '서울 성동구',
  roomName: '302호',
  roomType: '1인실',
  spec: '전용 욕실 · 13㎡ · 3층',
  monthlyFee: 780000,
  deposit: 1000000,
};

export const PERIODS = [
  { months: 3, discount: 0, label: '기본가' },
  { months: 6, discount: 0.05, label: '5% 할인' },
  { months: 12, discount: 0.1, label: '10% 할인' },
];

const ApplyContext = createContext(null);

export function ApplyProvider({ children }) {
  const [moveIn, setMoveIn] = useState(new Date(2026, 10, 2));
  const [months, setMonths] = useState(6);

  const value = useMemo(() => {
    const period = PERIODS.find((p) => p.months === months);
    const monthly = Math.round(ROOM.monthlyFee * (1 - period.discount));
    return {
      moveIn,
      setMoveIn,
      months,
      setMonths,
      period,
      monthly,
      moveOut: moveOutDate(moveIn, months),
      firstPayment: monthly + ROOM.deposit,
      applicationNo: 'CT-20261021-0042',
    };
  }, [moveIn, months]);

  return <ApplyContext.Provider value={value}>{children}</ApplyContext.Provider>;
}

export const useApply = () => useContext(ApplyContext);
