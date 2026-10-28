import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AmountBox from '../components/AmountBox';
import Button from '../components/Button';
import InfoList from '../components/InfoList';
import PageShell from '../components/PageShell';
import Section from '../components/Section';
import StepNav from '../components/StepNav';
import { useApplicationNo, useApply } from '../context/ApplyContext';
import { won, ymd, ymdDay } from '../lib/format';

// 결제 기한: 실제로는 contract_application.expires_at (승인 후 1일)과 서버 시각을 받아서 계산.
// 목업은 다른 화면 날짜(결제 일시 2026-10-22)와 맞추려고 고정값 사용
const MOCK_DEADLINE = new Date(2026, 9, 23, 18, 0, 0);
const MOCK_REMAIN_MS = (23 * 3600 + 41 * 60 + 5) * 1000;

const pad = (n) => String(n).padStart(2, '0');
const hms = (ms) => {
  const s = Math.max(0, Math.floor(ms / 1000));
  return `${pad(Math.floor(s / 3600))}:${pad(Math.floor((s % 3600) / 60))}:${pad(s % 60)}`;
};

export default function ContractPayPage() {
  const navigate = useNavigate();
  const a = useApply();
  const ROOM = a.room;
  const applicationNo = useApplicationNo(); // GET /contract-service/apply/applications/{applicationNo}/payment
  const deadline = MOCK_DEADLINE;
  const [now, setNow] = useState(deadline.getTime() - MOCK_REMAIN_MS);

  useEffect(() => {
    const t = setInterval(() => setNow((n) => n + 1000), 1000);
    return () => clearInterval(t);
  }, []);

  return (
    <>
      <StepNav current={3} />
      <PageShell
        title="결제 정보"
        main={
          <>
            <div className="rounded-xl border border-warn-line bg-warn-bg/60 px-5 py-4">
              <p className="font-bold text-warn-fg">결제 기한까지 {hms(deadline - now)} 남았어요</p>
              <p className="mt-1 text-sm text-warn-fg/90">
                {ymdDay(deadline)} {pad(deadline.getHours())}:{pad(deadline.getMinutes())}까지 결제하지 않으면 신청이 자동으로 취소돼요.
              </p>
            </div>

            <Section title="결제 금액">
              <InfoList
                rows={[
                  ['보증금', `${won(ROOM.deposit)}원`],
                  ['첫 달 이용료', `${won(ROOM.monthlyFee)}원`],
                  ['첫 달 관리비', `${won(ROOM.managementFee)}원`],
                ]}
              />
            </Section>

            <Section title="결제 수단">
              <div className="flex h-72 flex-col items-center justify-center rounded-xl border border-dashed border-line bg-surface text-center">
                <p className="text-sm font-bold text-sub">결제위젯 영역</p>
                <p className="mt-1 text-xs leading-relaxed text-muted">
                  토스페이먼츠 SDK가 렌더링 (카드 · 카카오페이 등 간편결제)
                  <br />
                  약관 동의 영역 포함
                </p>
              </div>
            </Section>
          </>
        }
        aside={
          <>
            <InfoList
              rows={[
                ['호실', `${ROOM.buildingName} ${ROOM.roomName}`],
                ['계약 기간', `${ymd(a.moveIn)} ~ ${ymd(a.moveOut)}`],
              ]}
            />
            <AmountBox label="총 결제 금액" amount={a.firstPayment} />
            <Button className="w-full" onClick={() => navigate(`/contract/${applicationNo}/complete`)}>
              {won(a.firstPayment)}원 결제하기
            </Button>
            <p className="text-xs text-sub">결제 금액은 서버에서 계약 정보로 다시 계산해 확인해요.</p>
          </>
        }
      />
    </>
  );
}
