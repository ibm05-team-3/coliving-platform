export function formatMan(amount) {
    if (amount == null) return '-'
    return `${Math.round(amount / 10000).toLocaleString('ko-KR')}만원`
}

export function formatEok(amount) {
    if (amount == null) return '-'
    if (amount >= 100000000) return `${(amount / 100000000).toFixed(1)}억원`
    return `${Math.round(amount / 10000).toLocaleString('ko-KR')}만원`
}