import {useEffect, useState} from 'react'
import {fetchActivityLogs, fetchRooms, fetchSummary} from '../api/dashboardApi.js'
import ActivityLogPanel from '../components/ActivityLogPanel.jsx'
import CollectionChart from '../components/CollectionChart.jsx'
import FilterBar from '../components/FilterBar.jsx'
import FinanceCards from '../components/FinanceCards.jsx'
import OccupancyTrendChart from '../components/OccupancyTrendChart.jsx'
import RoomStatusPanel from '../components/RoomStatusPanel.jsx'
import SummaryCards from '../components/SummaryCards.jsx'
import {BUILDINGS, recentMonths} from '../utils/buildings.js'

function DashboardPage({title}) {
    const [buildingId, setBuildingId] = useState(BUILDINGS[0].id)
    const [month, setMonth] = useState(recentMonths(1)[0]) // 기본값: 이번 달

    const [summary, setSummary] = useState(null)
    const [summaryEmpty, setSummaryEmpty] = useState(false) // 해당 월 집계 데이터 없음(404)
    const [rooms, setRooms] = useState([])
    const [logs, setLogs] = useState([])
    const [error, setError] = useState(null)

    useEffect(() => {
        document.title = title
    }, [title])

    // 호실 현황, 활동 로그: 건물이 바뀔 때만 다시 조회
    useEffect(() => {
        let cancelled = false
        Promise.all([fetchRooms(buildingId), fetchActivityLogs(buildingId)])
            .then(([roomData, logData]) => {
                if (cancelled) return
                setRooms(roomData)
                setLogs(logData)
                setError(null)
            })
            .catch((e) => !cancelled && setError(e.message))
        return () => {
            cancelled = true
        }
    }, [buildingId])

    // 지표 카드, 재무 요약, 추이: 건물이나 월이 바뀌면 다시 조회
    useEffect(() => {
        let cancelled = false
        fetchSummary(buildingId, month)
            .then((data) => {
                if (cancelled) return
                setSummary(data)
                setSummaryEmpty(false)
                setError(null)
            })
            .catch((e) => {
                if (cancelled) return
                if (e.status === 404) {
                    setSummary(null)
                    setSummaryEmpty(true)
                } else {
                    setError(e.message)
                }
            })
        return () => {
            cancelled = true
        }
    }, [buildingId, month])

    return (
        <div className="w-full max-w-[68rem]">
            <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
                <div>
                    <h1 className="text-2xl font-semibold text-main sm:text-3xl">{title}</h1>
                    <p className="mt-1 text-sm text-text/70">입주 현황과 수납 상태를 한 화면에서 확인합니다.</p>
                </div>
                <FilterBar
                    buildingId={buildingId}
                    month={month}
                    onBuildingChange={setBuildingId}
                    onMonthChange={setMonth}
                />
            </div>

            {error && <p className="mb-4 text-sm text-red-600">{error}</p>}

            <div className="space-y-5">
                {summaryEmpty && (
                    <div
                        className="rounded-lg border border-dashed border-main/30 bg-white p-8 text-center text-sm text-text/50">
                        선택한 월의 집계 데이터가 없습니다.
                    </div>
                )}

                {summary && <SummaryCards cards={summary.cards}/>}

                <div className="grid gap-5 lg:grid-cols-2">
                    <RoomStatusPanel rooms={rooms}/>
                    <ActivityLogPanel logs={logs}/>
                </div>

                {summary && <FinanceCards finance={summary.finance}/>}

                {summary && (
                    <div className="grid gap-5 lg:grid-cols-2">
                        <OccupancyTrendChart trend={summary.trend}/>
                        <CollectionChart trend={summary.trend}/>
                    </div>
                )}
            </div>
        </div>
    )
}

export default DashboardPage