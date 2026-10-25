import AmountBox from '../components/AmountBox';
import Button from '../components/Button';
import InfoList from '../components/InfoList';
import StatusIcon from '../components/StatusIcon';
import StepNav from '../components/StepNav';
import { ROOM, useApply } from '../context/ApplyContext';
import { ymd } from '../lib/format';
import { useNavigate } from 'react-router-dom';

export default function ContractCompletePage() {
  const navigate = useNavigate();
  const a = useApply();

  return (
    <>
      <StepNav current={4} />
      <div className="mx-auto max-w-[680px] px-6 py-14">
        <StatusIcon type="check" />
        <h1 className="mt-5 text-center text-2xl font-bold">계약이 완료됐어요</h1>
        <p className="mt-2 text-center text-sm leading-relaxed text-sub">
          영수증을 gil***@email.com 으로 보냈어요.
          <br />
          입주 3일 전에 체크인 안내를 알림으로 보내드릴게요.
        </p>

        <InfoList
          className="mt-8"
          rows={[
            ['계약 번호', a.applicationNo],
            ['호실', `${ROOM.buildingName} ${ROOM.roomName} · ${ROOM.roomType}`],
            ['계약 기간', `${ymd(a.moveIn)} ~ ${ymd(a.moveOut)} (${a.months}개월)`],
            ['결제 수단', '카카오페이'],
            ['결제 일시', '2026-10-22 14:05'],
          ]}
        />
        <div className="mt-3">
          <AmountBox label="결제 금액" amount={a.firstPayment} />
        </div>

        <div className="mt-6 grid grid-cols-3 gap-3">
          <Button variant="outline">영수증 보기</Button>
          <Button variant="outline">계약서 다운로드</Button>
          <Button onClick={() => navigate('/my/applications')}>나의 신청으로</Button>
        </div>
      </div>
    </>
  );
}
