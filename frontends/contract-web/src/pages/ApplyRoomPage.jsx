import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import AmountBox from '../components/AmountBox';
import Badge from '../components/Badge';
import Button from '../components/Button';
import Calendar from '../components/Calendar';
import Card from '../components/Card';
import InfoList from '../components/InfoList';
import PageShell from '../components/PageShell';
import Section from '../components/Section';
import StatusMessage from '../components/StatusMessage';
import StepNav from '../components/StepNav';
import { PERIODS, ROOM, useApply } from '../context/ApplyContext';
import { createApplication, getRoom } from '../lib/api';
import { amountRows, roomSpec, won, ymd, ymdDay } from '../lib/format';

export default function ApplyRoomPage() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const a = useApply();
  const r = a.room;
  const { setRoom } = a;

  // ?roomId=1 : 호실 상세에서 넘어온 값 → GET /contract-service/internel/apply/room/{roomId} (lib/api.js ROOM_PATH)
  // ?applicationNo=... : 신청자 정보에서 "이전"으로 돌아온 경우.
  //   따로 읽지 않는다. "다음 단계"를 누르면 서버가 같은 호실의 작성 중 신청서를 찾아 수정한다
  // roomId 없이 들어오면 개발 편의상 목업 호실 번호(1)로 조회
  const roomId = params.get('roomId') ?? ROOM.roomId; // POST body의 roomId

  // 호실 조회 상태: loading → ok | error
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState(null);
  const [reloadKey, setReloadKey] = useState(0); // "다시 시도" 누르면 1 올려서 재조회

  // "다음 단계" 요청 상태 (중복 클릭 방지 + 에러 문구)
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState(null);

  useEffect(() => {
    let ignore = false; // 응답 전에 roomId가 바뀌거나 화면을 떠나면 늦게 온 응답은 버림
    setStatus('loading');
    getRoom(roomId)
      .then((room) => {
        if (ignore) return;
        setRoom(room);
        setStatus('ok');
      })
      .catch((e) => {
        if (ignore) return;
        setError(e);
        setStatus('error');
      });
    return () => {
      ignore = true;
    };
  }, [roomId, reloadKey, setRoom]);

  if (status === 'loading') {
    return (
      <>
        <StepNav current={1} />
        <StatusMessage title="호실 정보를 불러오는 중이에요" />
      </>
    );
  }

  if (status === 'error') {
    // 404 = 없는/삭제된 호실 → 다시 시도해도 같음. 그 외(503, 502, 연결 실패) → 다시 시도 가능
    const notFound = error?.status === 404;
    return (
      <>
        <StepNav current={1} />
        <StatusMessage
          title={notFound ? '신청할 수 없는 호실이에요' : '호실 정보를 불러오지 못했어요'}
          description={error?.message}
          action={
            notFound ? (
              <Button variant="outline" onClick={() => navigate(-1)}>
                이전 화면으로
              </Button>
            ) : (
              <Button onClick={() => setReloadKey((k) => k + 1)}>다시 시도</Button>
            )
          }
        />
      </>
    );
  }

  // POST /contract-service/apply/applications { roomId, moveInDate, months }
  // "이전"으로 돌아온 경우(?applicationNo=...)에도 같은 API를 부른다.
  // 서버가 같은 회원·같은 호실의 작성 중 신청서를 찾아 수정하고 같은 신청번호를 돌려준다.
  const goNext = async () => {
    if (saving) return;
    setSaving(true);
    setSaveError(null);
    try {
      const res = await createApplication({ roomId, moveInDate: ymd(a.moveIn), months: a.months });
      navigate(`/apply/${res.applicationNo}/form`);
    } catch (e) {
      setSaveError(e.status === 401 ? '로그인 후 신청할 수 있어요.' : e.message);
      setSaving(false);
    }
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
              {saveError && (
                <p role="alert" className="rounded-lg bg-warn-bg/60 px-4 py-3 text-sm text-warn-fg">
                  {saveError}
                </p>
              )}
              <Button className="w-full" disabled={!r.available || saving} onClick={goNext}>
                {saving ? '저장 중…' : '다음 단계 ›'}
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

