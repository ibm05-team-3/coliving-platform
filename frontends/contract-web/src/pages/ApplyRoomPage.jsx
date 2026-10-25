import { useNavigate } from 'react-router-dom';
import AmountBox from '../components/AmountBox';
import Badge from '../components/Badge';
import Button from '../components/Button';
import Calendar from '../components/Calendar';
import Card from '../components/Card';
import InfoList from '../components/InfoList';
import PageShell from '../components/PageShell';
import Section from '../components/Section';
import StepNav from '../components/StepNav';
import { PERIODS, ROOM, useApply } from '../context/ApplyContext';
import { won, ymdDay } from '../lib/format';

export default function ApplyRoomPage() {
  const navigate = useNavigate();
  const a = useApply();

  return (
    <>
      <StepNav current={1} />
      <PageShell
        main={
          <>
            <Section title="호실 선택">
              <Card className="flex gap-6 p-5">
                <div className="flex h-40 w-56 shrink-0 items-center justify-center rounded-lg bg-soft text-sm text-muted">
                  호실 사진
                </div>
                <div className="flex flex-1 flex-col">
                  <p className="text-sm text-sub">
                    {ROOM.buildingName} · {ROOM.area}
                  </p>
                  <h3 className="mt-1 text-2xl font-bold">
                    {ROOM.roomName} · {ROOM.roomType}
                  </h3>
                  <p className="mt-1 text-sm text-sub">{ROOM.spec}</p>
                  <div className="mt-auto flex items-end justify-between">
                    <Badge tone="ok">입주 가능</Badge>
                    <div className="text-right">
                      <p className="text-xs text-sub">월 이용료</p>
                      <p className="text-2xl font-bold text-primary-strong">{won(ROOM.monthlyFee)}원~</p>
                    </div>
                  </div>
                </div>
              </Card>
            </Section>

            <Section title="계약 기간">
              <div className="grid grid-cols-3 gap-3">
                {PERIODS.map((p) => {
                  const on = p.months === a.months;
                  return (
                    <button
                      type="button"
                      key={p.months}
                      onClick={() => a.setMonths(p.months)}
                      className={`rounded-xl border py-4 text-center transition-colors ${
                        on ? 'border-primary bg-primary-soft' : 'border-line bg-surface hover:bg-soft'
                      }`}
                    >
                      <p className={`font-bold ${on ? 'text-primary-strong' : 'text-ink'}`}>{p.months}개월</p>
                      <div className="mt-1">
                        {p.discount ? <Badge tone="warn">{p.label}</Badge> : <span className="text-xs text-sub">{p.label}</span>}
                      </div>
                    </button>
                  );
                })}
              </div>
              <p className="mt-3 text-sm text-sub">지점별 최소 계약 기간은 3개월이에요. 기간이 길수록 월 이용료가 할인돼요.</p>
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
            <InfoList
              rows={[
                ['계약 기간', `${a.months}개월`],
                ['퇴실 예정일', ymdDay(a.moveOut)],
                [a.period.discount ? `월 이용료 (${a.period.label})` : '월 이용료', `${won(a.monthly)}원`],
                ['보증금', `${won(ROOM.deposit)}원`],
              ]}
            />
            <AmountBox amount={a.firstPayment} />
            <div className="space-y-2 pt-1">
              <Button className="w-full" onClick={() => navigate('/apply/form')}>
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
