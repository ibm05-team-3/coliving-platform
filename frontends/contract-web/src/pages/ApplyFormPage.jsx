import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AmountBox from '../components/AmountBox';
import Button from '../components/Button';
import Card from '../components/Card';
import Checkbox from '../components/Checkbox';
import InfoList from '../components/InfoList';
import PageShell from '../components/PageShell';
import Section from '../components/Section';
import StepNav from '../components/StepNav';
import { useApplicationNo, useApply } from '../context/ApplyContext';
import { amountRows, won, ymdDay } from '../lib/format';

const inputCls =
  'h-11 w-full rounded-lg border border-line bg-surface px-4 text-sm text-ink placeholder:text-muted focus:border-primary focus:outline-none';

const TERMS = [
  { id: 'privacy', label: '[필수] 개인정보 수집 및 이용 동의', detail: true },
  { id: 'age', label: '[필수] 만 14세 이상 이용 동의', detail: false },
  { id: 'notice', label: '[필수] 입주 신청 유의사항 확인', detail: true },
];

function Field({ label, children, hint }) {
  return (
    <div>
      <label className="mb-1.5 block text-sm font-medium text-ink">
        {label} <span className="text-warn-fg">*</span>
      </label>
      {children}
      {hint && <p className="mt-1.5 text-xs text-sub">{hint}</p>}
    </div>
  );
}

export default function ApplyFormPage() {
  const navigate = useNavigate();
  const a = useApply();
  const ROOM = a.room;
  const applicationNo = useApplicationNo(); // GET /contract-service/apply/applications/{applicationNo}/form
  const [lang, setLang] = useState('ko');
  const [agreed, setAgreed] = useState({ privacy: true, age: true, notice: true });
  const allAgreed = TERMS.every((t) => agreed[t.id]);
  const setAll = (v) => setAgreed(Object.fromEntries(TERMS.map((t) => [t.id, v])));

  return (
    <>
      <StepNav current={1} />
      <PageShell
        main={
          <>
            <Section title="신청자 정보">
              <Card className="space-y-5 p-6">
                <Field label="이름">
                  <input className={inputCls} defaultValue="홍길동" />
                </Field>
                <Field label="이메일" hint="로그인 계정 정보가 자동으로 입력돼요. 영수증이 이 주소로 발송돼요.">
                  <input className={inputCls} defaultValue="gildong@email.com" />
                </Field>
                <Field label="휴대폰 번호">
                  <div className="flex gap-2">
                    <select className={`${inputCls} w-44!`}>
                      <option>+82 (대한민국)</option>
                    </select>
                    <input className={inputCls} placeholder="숫자만 입력하세요" />
                  </div>
                </Field>
                <Field label="주소">
                  <div className="flex gap-2">
                    <input className={inputCls} placeholder="주소 검색" />
                    <Button variant="outline" className="shrink-0">
                      주소 찾기
                    </Button>
                  </div>
                </Field>
              </Card>
            </Section>

            <Section title="계약서 언어">
              <div className="grid grid-cols-2 gap-3">
                {[
                  ['ko', '한국어'],
                  ['en', 'English'],
                ].map(([v, label]) => (
                  <button
                    type="button"
                    key={v}
                    onClick={() => setLang(v)}
                    className={`h-12 rounded-xl border text-sm font-medium transition-colors ${
                      lang === v ? 'border-primary bg-primary-soft text-primary-strong' : 'border-line bg-surface text-ink hover:bg-soft'
                    }`}
                  >
                    {label}
                  </button>
                ))}
              </div>
            </Section>

            <Section title="약관 동의">
              <Card>
                <div className="border-b border-line px-5 py-4">
                  <Checkbox checked={allAgreed} onChange={setAll} bold>
                    전체 동의
                  </Checkbox>
                </div>
                <ul className="space-y-3.5 px-5 py-4">
                  {TERMS.map((t) => (
                    <li key={t.id} className="flex items-center justify-between">
                      <Checkbox checked={agreed[t.id]} onChange={(v) => setAgreed({ ...agreed, [t.id]: v })}>
                        {t.label}
                      </Checkbox>
                      {t.detail && (
                        <button type="button" className="text-xs text-sub hover:text-ink">
                          전문보기 ›
                        </button>
                      )}
                    </li>
                  ))}
                </ul>
              </Card>
            </Section>
          </>
        }
        aside={
          <>
            <InfoList
              rows={[
                ['호실', `${ROOM.buildingName} ${ROOM.roomName}`],
                ['입주일', ymdDay(a.moveIn)],
                ['계약 기간', `${a.months}개월`],
                ...amountRows(ROOM),
              ]}
            />
            <AmountBox amount={a.firstPayment} />
            <p className="rounded-xl border border-warn-line bg-warn-bg/60 px-5 py-3.5 text-sm leading-relaxed text-ink">
              신청서를 제출하면 관리자 심사가 시작돼요. 승인 후 계약서 확인과 결제를 진행해요.
            </p>
            <div className="space-y-2 pt-1">
              <Button className="w-full" disabled={!allAgreed} onClick={() => navigate(`/apply/${applicationNo}/submitted`)}>
                신청서 제출
              </Button>
              <Button variant="outline" className="w-full" onClick={() => navigate(`/apply/room?roomId=${ROOM.roomId}&applicationNo=${applicationNo}`)}>
                ‹ 이전
              </Button>
            </div>
          </>
        }
      />
    </>
  );
}
