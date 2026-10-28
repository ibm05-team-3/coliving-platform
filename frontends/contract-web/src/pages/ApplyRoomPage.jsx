import { useNavigate, useSearchParams } from 'react-router-dom';
import AmountBox from '../components/AmountBox';
import Badge from '../components/Badge';
import Button from '../components/Button';
import Calendar from '../components/Calendar';
import Card from '../components/Card';
import InfoList from '../components/InfoList';
import PageShell from '../components/PageShell';
import Section from '../components/Section';
import StepNav from '../components/StepNav';
import { MOCK_APPLICATION_NO, PERIODS, useApply } from '../context/ApplyContext';
import { amountRows, roomSpec, won, ymdDay } from '../lib/format';

export default function ApplyRoomPage() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const a = useApply();
  const r = a.room;

  // ?roomId=1 : 호실 상세에서 넘어온 값 → GET /contract-service/internel/apply/rooms/{roomId}
  // ?applicationNo=... : 신청자 정보에서 "이전"으로 돌아온 경우 → 기존 DRAFT를 수정
  const roomId = params.get('roomId') ?? r.roomId; // POST body의 roomId
  const draftNo = params.get('applicationNo');

  const goNext = () => {
    // 실제 연결 시
    //   draftNo 없음 → POST  /contract-service/apply/applications            { roomId, moveInDate, months }
    //   draftNo 있음 → PATCH /contract-service/apply/applications/{draftNo}  { moveInDate, months }
    // 응답의 applicationNo로 이동
    const applicationNo = draftNo ?? MOCK_APPLICATION_NO;
    navigate(`/apply/${applicationNo}/form`);
  };

  return (
    <>
      <StepNav current={1} />
      <PageShell
        main={
          <>
            <Section title="호실 선택">
              <Card className="flex gap-6 p-5">
                {r.thumbnailUrl ? (
                  <img src={r.thumbnailUrl} alt={r.roomName} className="h-40 w-56 shrink-0 rounded-lg object-cover" />
                ) : (
                  <div className="flex h-40 w-56 shrink-0 items-center justify-center rounded-lg bg-soft text-sm text-muted">
                    호실 사진
                  </div>
                )}
                <div className="flex flex-1 flex-col">
                  <p className="text-sm text-sub">
                    {r.buildingName} · {r.region}
                  </p>
                  <h3 className="mt-1 text-2xl font-bold">
                    {r.roomName} · {r.roomType}
                  </h3>
                  <p className="mt-1 text-sm text-sub">{roomSpec(r)}</p>
                  <div className="mt-auto flex items-end justify-between">
                    <Badge tone={r.available ? 'ok' : 'warn'}>{r.available ? '입주 가능' : '입주 불가'}</Badge>
                    <div className="text-right">
                      <p className="text-xs text-sub">월 이용료</p>
                      <p className="text-2xl font-bold text-primary-strong">{won(r.monthlyFee)}원</p>
                      <p className="text-xs text-sub">관리비 월 {won(r.managementFee)}원 별도</p>
                    </div>
                  </div>
                </div>
              </Card>
            </Section>

            <Section title="계약 기간">
              <div className="grid grid-cols-3 gap-3">
                {PERIODS.map((m) => {
                  const on = m === a.months;
                  return (
                    <button
                      type="button"
                      key={m}
                      onClick={() => a.setMonths(m)}
                      className={`rounded-xl border py-4 text-center transition-colors ${
                        on ? 'border-primary bg-primary-soft' : 'border-line bg-surface hover:bg-soft'
                      }`}
                    >
                      <p className={`font-bold ${on ? 'text-primary-strong' : 'text-ink'}`}>{m}개월</p>
                    </button>
                  );
                })}
              </div>
              <p className="mt-3 text-sm text-sub">지점별 최소 계약 기간은 3개월이에요.</p>
            </Section>
          </>
        }
        aside={
          <>
            <Card>
              <div className="flex items-center justify-between border-b border-line px-5 py-3.5 text-sm">
                <span className="text-sub">입주일</span>
                <span className="font-medium">{ymdDay(a.moveIn)}</span>
              </div>
              <Calendar value={a.moveIn} onChange={a.setMoveIn} />
            </Card>
            <InfoList rows={[['계약 기간', `${a.months}개월`], ['퇴실 예정일', ymdDay(a.moveOut)], ...amountRows(r)]} />
            <AmountBox amount={a.firstPayment} />
            <div className="space-y-2 pt-1">
              <Button className="w-full" disabled={!r.available} onClick={goNext}>
                다음 단계 ›
              </Button>
              <Button variant="outline" className="w-full">
                상담 신청 ›
              </Button>
            </div>
          </>
        }
      />
    </>
  );
}
