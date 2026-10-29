// 임시 건물 목록.
export const BUILDINGS = [
    { id: 1, name: '망원 스테이' },
    { id: 2, name: '연남 하우스' },
    { id: 3, name: '성수 로프트' },
]

// 현재 월부터 거꾸로 count개월
export function recentMonths(count = 6) {
    const now = new Date()
    const months = []
    for (let i = 0; i < count; i++) {
        const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
        months.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`)
    }
    return months
}

// 2026-10 → 2026년 10월
export function monthText(month) {
    return `${month.slice(0, 4)}년 ${Number(month.slice(5))}월`
}