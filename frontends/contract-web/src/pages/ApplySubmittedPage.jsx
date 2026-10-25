import { useNavigate } from 'react-router-dom';
import Badge from '../components/Badge';
import Button from '../components/Button';
import InfoList from '../components/InfoList';
import StatusIcon from '../components/StatusIcon';
import StepNav from '../components/StepNav';
import { ROOM, useApply } from '../context/ApplyContext';
import { ymd } from '../lib/format';

export default function ApplySubmittedPage() {
  const navigate = useNavigate();
  const a = useApply();

  return (
    <>
      <StepNav current={0} done={1} />
      <div className="mx-auto max-w-[680px] px-6 py-14">
        <StatusIcon type="clock" />
        <h1 className="mt-5 text-center text-2xl font-bold">신청이 접수됐어요</h1>
        <p className="mt-2 text-center text-sm leading-relaxed text-sub">
          관리자가 신청서를 확인하고 있어요.
          <br />
          승인되면 알림과 이메일로 알려드릴게요.
        </p>

        <InfoList
          className="mt-8"
          rows={[
            ['신청 번호', a.applicationNo],
            ['호실', `${ROOM.buildingName} ${ROOM.roomName} · ${ROOM.roomType}`],
            ['입주일 · 기간', `${ymd(a.moveIn)} · ${a.months}개월`],
            ['진행 상태', <Badge tone="warn">심사 중</Badge>],
          ]}
        />

        <div className="mt-4 rounded-xl border border-warn-line bg-warn-bg/60 px-5 py-4 text-sm">
          <p className="text-xs font-bold text-warn-fg">반려된 경우 표시 예시</p>
          <p className="mt-1.5">
            <b>신청이 반려됐어요.</b> <span className="text-sub">사유: 희망 입주일에 해당 호실을 이용할 수 없어요.</span>
          </p>
          <button type="button" onClick={() => navigate('/apply/room')} className="mt-2 font-bold text-primary-strong hover:underline">
            다른 날짜로 다시 신청하기 ›
          </button>
        </div>

        <div className="mt-6 grid grid-cols-3 gap-3">
          <Button variant="outline" onClick={() => navigate('/')}>
            홈으로
          </Button>
          <Button onClick={() => navigate('/my/applications')}>나의 신청 보기</Button>
          <Button variant="outline">취소하기</Button>
        </div>
      </div>
    </>
  );
}
