import { formatMan } from '../utils/format'

function Diff({ value, unit, label, goodWhenUp = true }) {
    if (value == null) return <p className="mt-1 text-xs text-text/50">{label} 비교 데이터 없음</p>
    if (value === 0) return <p className="mt-1 text-xs text-text/70">변동 없음 · {label}</p>

    const up = value > 0
    const good = up === goodWhenUp
    return (
        <p className={`mt-1 text-xs ${good ? 'text-main' : 'text-red-600'}`}>
            {up ? '▲' : '▼'} {Math.abs(value)}
            {unit} <span className="text-text/50">{label}</span>
        </p>
    )
}

function Card({ title, value, children }) {
    return (
        <div className="rounded-lg border border-main/20 bg-white p-5 text-center">
            <p className="text-xs text-text/70">{title}</p>
            <p className="mt-1 text-2xl font-bold text-text sm:text-3xl">{value}</p>
            {children}
        </div>
    )
}

function SummaryCards({ cards }) {
    return (
        <div className="grid grid-cols-2 gap-3 sm:gap-4 lg:grid-cols-4">
            <Card title="입주율" value={`${cards.occupancyRate}%`}>
                <Diff value={cards.occupancyRateDiff} unit="%p" label="지난달 대비" />
            </Card>
            <Card title="이번 달 수납액" value={formatMan(cards.monthCollectedAmount)}>
                <Diff value={cards.collectedChangeRate} unit="%" label="지난달 대비" />
            </Card>
            <Card title="승인 대기 신청" value={`${cards.pendingApprovalCount}건`}>
                <p className="mt-1 text-xs text-text/50">처리 대기 중인 신청</p>
            </Card>
            <Card title="연체 계약" value={`${cards.overdueCount}건`}>
                <Diff value={cards.overdueCountDiff} unit="건" label="지난주 대비" goodWhenUp={false} />
            </Card>
        </div>
    )
}

export default SummaryCards