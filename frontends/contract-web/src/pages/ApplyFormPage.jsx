import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AmountBox from '../components/AmountBox';
import Button from '../components/Button';
import Card from '../components/Card';
import Checkbox from '../components/Checkbox';
import InfoList from '../components/InfoList';
import PageShell from '../components/PageShell';
import Section from '../components/Section';
import StatusMessage from '../components/StatusMessage';
import StepNav from '../components/StepNav';
import { useApplicationNo, useApply } from '../context/ApplyContext';
import { getApplyForm, submitApplication } from '../lib/api';
import { amountRows, ymdDay } from '../lib/format';

const inputCls =
  'h-11 w-full rounded-lg border border-line bg-surface px-4 text-sm text-ink placeholder:text-muted focus:border-primary focus:outline-none';
const readOnlyCls = 'h-11 w-full rounded-lg border border-line bg-soft px-4 text-sm text-sub';

const TERMS = [
  { id: 'privacy', label: '[필수] 개인정보 수집 및 이용 동의', detail: true },
  { id: 'age', label: '[필수] 만 14세 이상 이용 동의', detail: false },
  { id: 'notice', label: '[필수] 입주 신청 유의사항 확인', detail: true },
];

const LANGS = [
  ['ko', '한국어'],
  ['en', 'English'],
];

// "2026-11-02" → 로컬 날짜 (new Date("2026-11-02")는 UTC 기준이라 하루 밀릴 수 있음)
const parseYmd = (s) => {
  const [y, m, d] = s.split('-').map(Number);
  return new Date(y, m - 1, d);
};

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
  const { setRoom, setMoveIn, setMonths } = useApply();
  const applicationNo = useApplicationNo();

  // GET /contract-service/apply/applications/{applicationNo}/form
  const [status, setStatus] = useState('loading'); // loading → ok | error
  const [loadError, setLoadError] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);
  const [form, setForm] = useState(null);

  // 사용자가 고칠 수 있는 값
  const [email, setEmail] = useState('');
  const [address, setAddress] = useState('');
  const [lang, setLang] = useState('ko');
  // 동의는 사용자가 직접 체크해야 하므로 처음엔 모두 해제
  const [agreed, setAgreed] = useState({ privacy: false, age: false, notice: false });
  const allAgreed = TERMS.every((t) => agreed[t.id]);
  const setAll = (v) => setAgreed(Object.fromEntries(TERMS.map((t) => [t.id, v])));

  // POST .../submit 상태
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);

  useEffect(() => {
    let ignore = false;
    setStatus('loading');
    getApplyForm(applicationNo)
      .then((f) => {
        if (ignore) return;
        setForm(f);
        setEmail(f.email ?? '');
        setAddress(f.address ?? '');
        setLang(f.contractLanguage ?? 'ko');
        // 다음 화면(접수 완료 등)이 같은 값을 보여주도록 context도 서버 값으로 맞춤
        setRoom((prev) => ({
          ...prev,
          roomId: f.roomId,
          buildingName: f.buildingName,
          region: f.region,
          roomName: f.roomName,
          roomType: f.roomType,
          monthlyFee: f.monthlyFee,
          managementFee: f.managementFee,
          deposit: f.deposit,
        }));
        setMoveIn(parseYmd(f.moveInDate));
        setMonths(f.months);
        setStatus('ok');
      })
      .catch((e) => {
        if (ignore) return;
        setLoadError(e);
        setStatus('error');
      });
    return () => {
      ignore = true;
    };
  }, [applicationNo, reloadKey, setRoom, setMoveIn, setMonths]);

  if (status === 'loading') {
    return (
      <>
        <StepNav current={1} />
        <StatusMessage title="신청서를 불러오는 중이에요" />
      </>
    );
  }

  if (status === 'error') {
    const s = loadError?.status;
    const retryable = s === 0 || s >= 500;
    const title =
      s === 401
        ? '로그인이 필요해요'
        : s === 403
          ? '본인의 신청서만 볼 수 있어요'
          : s === 404
            ? '신청서를 찾을 수 없어요'
            : s === 409
              ? '이미 제출했거나 작성할 수 없는 신청서예요'
              : '신청서를 불러오지 못했어요';
    return (
      <>
        <StepNav current={1} />
        <StatusMessage
          title={title}
          description={loadError?.message}
          action={
            retryable ? (
              <Button onClick={() => setReloadKey((k) => k + 1)}>다시 시도</Button>
            ) : s === 409 ? (
              <Button variant="outline" onClick={() => navigate(`/apply/${applicationNo}/submitted`)}>
                신청 현황 보기
              </Button>
            ) : (
              <Button variant="outline" onClick={() => navigate('/')}>
                홈으로
              </Button>
            )
          }
        />
      </>
    );
  }

  const submit = async () => {
    if (submitting) return;
    if (!email.trim()) {
      setSubmitError('이메일을 입력해주세요.');
      return;
    }
    setSubmitting(true);
    setSubmitError(null);
    try {
      await submitApplication(applicationNo, {
        email: email.trim(),
        address: address.trim() || null,
        contractLang: lang,
        termsVersion: form.termsVersion, // 화면에 보였던 약관 버전을 그대로 돌려보냄
      });
      navigate(`/apply/${applicationNo}/submitted`);
    } catch (e) {
      setSubmitError(e.message);
      setSubmitting(false);
    }
  };

  return (
    <>
      <StepNav current={1} />
      <PageShell
        main={
          <>
            <Section title="신청자 정보">
              <Card className="space-y-5 p-6">
                <Field label="이름" hint="회원 정보에 등록된 이름이에요. 마이페이지에서 바꿀 수 있어요.">
                  <input className={readOnlyCls} value={form.name ?? ''} readOnly />
                </Field>
                <Field label="이메일" hint="로그인 계정 정보가 자동으로 입력돼요. 영수증이 이 주소로 발송돼요.">
                  <input
                    className={inputCls}
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="example@email.com"
                  />
                </Field>
                <Field label="휴대폰 번호" hint="본인인증에 쓰는 번호라 여기서는 바꿀 수 없어요.">
                  <input className={readOnlyCls} value={form.phone ?? ''} readOnly />
                </Field>
                <Field label="주소">
                  <div className="flex gap-2">
                    <input
                      className={inputCls}
                      value={address}
                      onChange={(e) => setAddress(e.target.value)}
                      placeholder="주소 검색"
                    />
                    <Button variant="outline" className="shrink-0">
                      주소 찾기
                    </Button>
                  </div>
                </Field>
              </Card>
            </Section>

            <Section title="계약서 언어">
              <div className="grid grid-cols-2 gap-3">
                {LANGS.map(([v, label]) => (
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
                ['호실', `${form.buildingName} ${form.roomName}`],
                ['입주일', ymdDay(parseYmd(form.moveInDate))],
                ['계약 기간', `${form.months}개월`],
                ['퇴실 예정일', ymdDay(parseYmd(form.moveOutDate))],
                ...amountRows(form),
              ]}
            />
            {/* 서버가 계산한 값 (Contract.firstPayment). 1-1 화면 계산식과 다르면 정책 확인 필요 */}
            <AmountBox amount={form.firstPaymentAmount} />
            <p className="rounded-xl border border-warn-line bg-warn-bg/60 px-5 py-3.5 text-sm leading-relaxed text-ink">
              신청서를 제출하면 관리자 심사가 시작돼요. 승인 후 계약서 확인과 결제를 진행해요.
            </p>
            <div className="space-y-2 pt-1">
              {submitError && (
                <p role="alert" className="rounded-lg bg-warn-bg/60 px-4 py-3 text-sm text-warn-fg">
                  {submitError}
                </p>
              )}
              <Button className="w-full" disabled={!allAgreed || submitting} onClick={submit}>
                {submitting ? '제출 중…' : '신청서 제출'}
              </Button>
              <Button
                variant="outline"
                className="w-full"
                onClick={() => navigate(`/apply/room?roomId=${form.roomId}&applicationNo=${applicationNo}`)}
              >
                ‹ 이전
              </Button>
            </div>
          </>
        }
      />
    </>
  );
}
