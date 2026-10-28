const TYPE = {
    CONTRACT: { label: '계약', className: 'bg-main/15 text-main' },
    RESERVATION: { label: '예약', className: 'bg-accent/30 text-text' },
    MOVE_IN: { label: '입주', className: 'bg-main text-background' },
    PAYMENT: { label: '결제', className: 'bg-text/10 text-text' },
}

const STATUS = {
    COMPLETED: { label: '완료', className: 'text-main' },
    PENDING: { label: '대기', className: 'text-accent' },
    PROCESSING: { label: '처리중', className: 'text-text/60' },
}

function formatTime(iso) {
    return new Date(iso).toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
    })
}

function ActivityLogPanel({ logs }) {
    return (
        <section className="rounded-lg border border-main/20 bg-white p-5">
            <h2 className="mb-4 text-base font-semibold text-text">최근 활동 로그</h2>

            {logs.length === 0 ? (
                <p className="py-6 text-center text-sm text-text/50">최근 활동이 없습니다.</p>
            ) : (
                <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                        <thead>
                        <tr className="border-b border-main/10 text-left text-xs text-text/50">
                            <th className="py-2 pr-3 font-normal">시간</th>
                            <th className="py-2 pr-3 font-normal">유형</th>
                            <th className="py-2 pr-3 font-normal">내용</th>
                            <th className="py-2 text-right font-normal">상태</th>
                        </tr>
                        </thead>
                        <tbody>
                        {logs.map((log) => {
                            const type = TYPE[log.activityType] ?? { label: log.activityType, className: 'bg-text/10 text-text' }
                            const status = STATUS[log.status] ?? { label: log.status, className: 'text-text/60' }
                            return (
                                <tr key={log.id} className="border-b border-main/5">
                                    <td className="whitespace-nowrap py-2 pr-3 text-text/70">{formatTime(log.updatedAt)}</td>
                                    <td className="whitespace-nowrap py-2 pr-3">
                                        <span className={`rounded px-2 py-0.5 text-xs ${type.className}`}>{type.label}</span>
                                    </td>
                                    <td className="min-w-48 py-2 pr-3 text-text">{log.content}</td>
                                    <td className={`whitespace-nowrap py-2 text-right font-medium ${status.className}`}>
                                        {status.label}
                                    </td>
                                </tr>
                            )
                        })}
                        </tbody>
                    </table>
                </div>
            )}
        </section>
    )
}

export default ActivityLogPanel