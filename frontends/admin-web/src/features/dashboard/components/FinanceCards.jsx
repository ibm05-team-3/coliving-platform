import { formatEok, formatMan } from '../utils/format'

function Card({ title, value, sub }) {
    return (
        <div className="rounded-lg border border-main/20 bg-white p-5 text-center">
            <p className="text-xs text-text/70">{title}</p>
            <p className="mt-1 text-2xl font-bold text-text sm:text-3xl">{value}</p>
            <p className="mt-1 text-xs text-text/50">{sub}</p>
        </div>
    )
}

function FinanceCards({ finance }) {
    return (
        <div className="grid grid-cols-1 gap-3 sm:gap-4 md:grid-cols-3">
            <Card title="미수금" value={formatMan(finance.totalUnpaidAmount)} sub={`연체 ${finance.overdueCount}건`} />
            <Card title="보증금 보관액" value={formatEok(finance.depositHeldAmount)} sub={`입주자 ${finance.residentCount}명`} />
            <Card title="환불 예정" value={formatMan(finance.refundScheduledAmount)} sub={`퇴거 ${finance.refundScheduledCount}건`} />
        </div>
    )
}

export default FinanceCards