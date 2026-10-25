const STEPS = ['신청서 작성', '계약서 확인·서명', '결제', '계약 완료'];

// current: 현재 단계(1~4), done: 완료한 단계 수
export default function StepNav({ current, done = current - 1 }) {
  return (
    <div className="border-b border-line bg-surface">
      <ol className="mx-auto flex max-w-[1180px] px-6">
        {STEPS.map((label, i) => {
          const n = i + 1;
          const isCurrent = n === current;
          const isDone = n <= done;
          return (
            <li
              key={label}
              className={`-mb-px flex-1 border-b-2 py-3.5 ${isCurrent ? 'border-primary' : 'border-transparent'}`}
            >
              <span className={`text-xs ${isDone || isCurrent ? 'text-primary' : 'text-muted'}`}>
                Step {n}
                {isDone && ' ✓'}
              </span>
              <p className={`text-sm ${isCurrent ? 'font-bold text-ink' : isDone ? 'text-sub' : 'text-muted'}`}>
                {label}
              </p>
            </li>
          );
        })}
      </ol>
    </div>
  );
}
