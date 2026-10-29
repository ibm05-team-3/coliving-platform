import {useEffect, useState} from 'react'
import {CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis} from 'recharts'
import {fetchMonitoringCharts, fetchMonitoringSummary} from '../api/dashboardApi.js'

// Grafana 주소. 다른 주소를 쓰려면 .env에 VITE_GRAFANA_URL을 지정한다.
const GRAFANA_URL = import.meta.env.VITE_GRAFANA_URL ?? 'http://localhost:3001'
const REFRESH_MS = 15000 // Prometheus 수집 주기(15초)에 맞춰 갱신

// 서비스별 선 스타일. 색뿐 아니라 점선 모양으로도 구분되게 한다.
const LINE_STYLES = [
    {stroke: '#52796F', dash: undefined},
    {stroke: '#D6A56D', dash: '6 4'},
    {stroke: '#2a78d6', dash: '2 3'},
    {stroke: '#eb6834', dash: '8 3 2 3'},
    {stroke: '#7a5aa6', dash: '1 4'},
    {stroke: '#555555', dash: '10 4'},
]

const format = (value, unit) => (value == null ? '-' : `${value.toLocaleString('ko-KR')}${unit}`)

const timeLabel = (t) =>
    new Date(t * 1000).toLocaleTimeString('ko-KR', {hour: '2-digit', minute: '2-digit', hour12: false})

// 서비스별 시계열을 시각 기준 한 줄(행)로 합친다. recharts는 {t, 서비스A: 값, 서비스B: 값} 형태를 쓴다.
function toRows(series) {
    const rows = new Map()
    series.forEach((s) =>
        s.points.forEach((p) => {
            const row = rows.get(p.t) ?? {t: p.t}
            row[s.name] = p.v
            rows.set(p.t, row)
        }),
    )
    return [...rows.values()].sort((a, b) => a.t - b.t)
}

function Card({title, value, note}) {
    return (
        <div className="rounded-lg border border-main/20 bg-white p-5 text-center">
            <p className="text-xs text-text/70">{title}</p>
            <p className="mt-1 text-2xl font-bold text-text sm:text-3xl">{value}</p>
            <p className="mt-1 text-xs text-text/50">{note}</p>
        </div>
    )
}

function ServiceRow({service}) {
    return (
        <div className="flex items-center justify-between rounded-lg border border-main/20 bg-white px-4 py-3">
            <div>
                <p className="text-sm font-medium text-text">{service.name}</p>
                <p className="text-xs text-text/50">{service.instance}</p>
            </div>
            <span
                className={`rounded-md px-2 py-0.5 text-xs ${
                    service.up ? 'bg-main/10 text-main' : 'bg-red-50 text-red-600'
                }`}
            >
                {service.up ? '정상' : '중지'}
            </span>
        </div>
    )
}

function ChartPanel({title, note, unit, panel}) {
    const series = panel?.series ?? []
    const rows = toRows(series)
    const hasData = rows.some((row) => Object.keys(row).some((key) => key !== 't' && row[key] != null))

    return (
        <section className="rounded-lg border border-main/20 bg-white p-5">
            <div className="mb-3 flex items-baseline justify-between">
                <h2 className="text-base font-semibold text-text">{title}</h2>
                <span className="text-xs text-text/50">{note}</span>
            </div>
            <div className="h-64">
                {hasData ? (
                    <ResponsiveContainer width="100%" height="100%">
                        <LineChart data={rows} margin={{top: 10, right: 20, left: 0, bottom: 0}}>
                            <CartesianGrid strokeDasharray="3 3" vertical={false}/>
                            <XAxis
                                dataKey="t"
                                type="number"
                                domain={['dataMin', 'dataMax']}
                                tickFormatter={timeLabel}
                                tick={{fontSize: 12}}
                            />
                            <YAxis domain={[0, 'auto']} tick={{fontSize: 12}} width={48}/>
                            <Tooltip
                                labelFormatter={timeLabel}
                                formatter={(v, name) => [`${v}${unit}`, name]}
                            />
                            <Legend wrapperStyle={{fontSize: 12}}/>
                            {series.map((s, i) => {
                                const style = LINE_STYLES[i % LINE_STYLES.length]
                                return (
                                    <Line
                                        key={s.name}
                                        type="monotone"
                                        dataKey={s.name}
                                        stroke={style.stroke}
                                        strokeDasharray={style.dash}
                                        strokeWidth={2}
                                        dot={false}
                                        connectNulls
                                        isAnimationActive={false}
                                    />
                                )
                            })}
                        </LineChart>
                    </ResponsiveContainer>
                ) : (
                    <div className="flex h-full items-center justify-center text-sm text-text/50">
                        표시할 데이터가 없습니다.
                    </div>
                )}
            </div>
        </section>
    )
}

function MonitoringPage({title}) {
    const [summary, setSummary] = useState(null)
    const [charts, setCharts] = useState(null)
    const [error, setError] = useState(null)

    useEffect(() => {
        document.title = title
    }, [title])

    useEffect(() => {
        let cancelled = false
        const load = () =>
            Promise.all([fetchMonitoringSummary(), fetchMonitoringCharts()])
                .then(([summaryData, chartData]) => {
                    if (cancelled) return
                    setSummary(summaryData)
                    setCharts(chartData)
                    setError(null)
                })
                .catch((e) => !cancelled && setError(e.message))

        load()
        const timer = setInterval(load, REFRESH_MS)
        return () => {
            cancelled = true
            clearInterval(timer)
        }
    }, [])

    const cards = summary?.cards

    return (
        <div className="w-full max-w-[68rem]">
            <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
                <div>
                    <h1 className="text-2xl font-semibold text-main sm:text-3xl">{title}</h1>
                    <p className="mt-1 text-sm text-text/70">서비스 상태와 요청 지표 요약</p>
                </div>
                <a
                    href={GRAFANA_URL}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="rounded-lg border border-main/30 bg-white px-3 py-2 text-sm text-main hover:bg-main/5"
                >
                    Grafana에서 자세히 보기 ↗
                </a>
            </div>

            {error && <p className="mb-4 text-sm text-red-600">{error}</p>}

            {summary && !summary.prometheusAvailable && (
                <div className="rounded-lg border border-dashed border-main/30 bg-white p-8 text-center text-sm text-text/50">
                    Prometheus에 연결할 수 없습니다. prometheus 컨테이너가 실행 중인지 확인하세요.
                </div>
            )}

            {cards && (
                <div className="space-y-5">
                    <div className="grid grid-cols-2 gap-3 sm:gap-4 lg:grid-cols-4">
                        <Card
                            title="서비스 상태"
                            value={`${cards.upCount} / ${cards.totalCount}`}
                            note="정상 / 수집 대상"
                        />
                        <Card title="평균 응답 시간" value={format(cards.avgResponseMs, 'ms')} note="최근 5분 기준"/>
                        <Card title="분당 요청" value={format(cards.requestsPerMinute, '건')} note="최근 5분 기준"/>
                        <Card title="오류율" value={format(cards.errorRatePercent, '%')} note="5xx 응답, 최근 5분"/>
                    </div>

                    <div className="grid gap-3 sm:grid-cols-2">
                        {summary.services.map((service) => (
                            <ServiceRow key={service.instance} service={service}/>
                        ))}
                    </div>

                    {charts && (
                        <div className="grid gap-5 lg:grid-cols-2">
                            <ChartPanel title="HTTP 요청 수" note="초당 요청 · 최근 1시간" unit=" req/s" panel={charts.requests}/>
                            <ChartPanel title="응답 시간 p95" note="밀리초 · 최근 1시간" unit=" ms" panel={charts.p95}/>
                            <ChartPanel title="JVM 힙 메모리" note="MB · 최근 1시간" unit=" MB" panel={charts.heap}/>
                            <ChartPanel title="CPU 사용률" note="퍼센트 · 최근 1시간" unit="%" panel={charts.cpu}/>
                        </div>
                    )}

                    <p className="text-xs text-text/50">
                        마지막 조회 {new Date(summary.checkedAt).toLocaleTimeString('ko-KR')} · 15초마다 자동 갱신
                    </p>
                </div>
            )}
        </div>
    )
}

export default MonitoringPage
