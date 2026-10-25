import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AmountBox from '../components/AmountBox';
import Badge from '../components/Badge';
import Button from '../components/Button';
import Card from '../components/Card';
import Checkbox from '../components/Checkbox';
import InfoList from '../components/InfoList';
import PageShell from '../components/PageShell';
import Section from '../components/Section';
import SignaturePad from '../components/SignaturePad';
import StepNav from '../components/StepNav';
import { ROOM, useApply } from '../context/ApplyContext';
import { ymd } from '../lib/format';

export default function ContractSignPage() {
  const navigate = useNavigate();
  const a = useApply();
  const [read, setRead] = useState(false);
  const [signed, setSigned] = useState(false);
  const [clearSignal, setClearSignal] = useState(0);
  const verified = true; // 포트원 본인인증 결과 (목업)
  const ready = read && verified && signed;

  return (
    <>
      <StepNav current={2} />
      <PageShell
        main={
          <>
            <Section title="계약서 확인">
              <div className="flex h-80 flex-col items-center justify-center rounded-xl border border-dashed border-line bg-surface text-center">
                <p className="text-sm font-bold text-sub">계약서 뷰어</p>
                <p className="mt-1 text-xs text-muted">
                  문서 담당 공통 컴포넌트 영역
                  <br />
                  (승인된 계약서 내용 · PDF 미리보기)
                </p>
              </div>
              <Card className="mt-3 px-5 py-4">
                <Checkbox checked={read} onChange={setRead}>
                  계약서 내용을 모두 확인했어요
                </Checkbox>
              </Card>
            </Section>

            <Section title="본인 인증">
              <Card className="flex items-center justify-between px-5 py-4">
                <div>
                  <p className="text-sm font-bold">휴대폰 본인 인증</p>
                  <p className="mt-0.5 text-xs text-sub">서명 전에 본인 확인이 필요해요.</p>
                </div>
                <Badge tone="ok">인증 완료</Badge>
              </Card>
            </Section>

            <Section title="전자 서명">
              <SignaturePad onChange={setSigned} clearSignal={clearSignal} />
              <div className="mt-3">
                <Button variant="outline" size="sm" onClick={() => setClearSignal((n) => n + 1)}>
                  지우기
                </Button>
              </div>
            </Section>
          </>
        }
        aside={
          <>
            <InfoList
              rows={[
                ['신청 번호', a.applicationNo],
                ['호실', `${ROOM.buildingName} ${ROOM.roomName}`],
                ['계약 기간', `${ymd(a.moveIn)} ~ ${ymd(a.moveOut)}`],
                ['심사 결과', <Badge tone="ok">승인</Badge>],
              ]}
            />
            <AmountBox amount={a.firstPayment} />
            <Button className="w-full" disabled={!ready} onClick={() => navigate('/contract/pay')}>
              서명 완료하고 결제하기 ›
            </Button>
            <p className="text-xs text-sub">계약서 확인, 본인 인증, 서명을 모두 마쳐야 버튼이 활성화돼요.</p>
          </>
        }
      />
    </>
  );
}
