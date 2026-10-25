const DAY = ['일', '월', '화', '수', '목', '금', '토'];

export const won = (n) => n.toLocaleString('ko-KR');

export const ymd = (d) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

export const ymdDay = (d) => `${ymd(d)} (${DAY[d.getDay()]})`;

// 퇴실 예정일 = 입주일 + 계약 개월 - 1일 (SQL: stay_period '[)' 와 화면 표기 기준)
export const moveOutDate = (moveIn, months) => {
  const d = new Date(moveIn.getFullYear(), moveIn.getMonth() + months, moveIn.getDate());
  d.setDate(d.getDate() - 1);
  return d;
};

export const DAY_NAMES = DAY;
