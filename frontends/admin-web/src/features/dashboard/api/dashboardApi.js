// 대시보드 서비스 API 호출.
const BASE = '/api/dashboard'

async function get(path, params) {
    const query = new URLSearchParams(params).toString()
    const res = await fetch(`${BASE}${path}?${query}`)
    if (!res.ok) {
        const error = new Error(`요청 실패 (${res.status})`)
        error.status = res.status // 상태 코드 구분
        throw error
    }
    return res.json()
}

export const fetchRooms = (buildingId) => get('/rooms', { buildingId })

export const fetchActivityLogs = (buildingId, size = 10) =>
    get('/activity-logs', { buildingId, size })

export const fetchSummary = (buildingId, month) =>
    get('/summary', month ? { buildingId, month } : { buildingId })