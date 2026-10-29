import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'

const MAIN = '#52796F'
const monthLabel = (m) => `${Number(m.slice(5))}월`

function OccupancyTrendChart({ trend }) {
    const data = trend.map((t) => ({ name: monthLabel(t.month), rate: t.occupancyRate }))

    return (
        <section className="rounded-lg border border-main/20 bg-white p-5">
            <div className="mb-3 flex items-baseline justify-between">
                <h2 className="text-base font-semibold text-text">입주율 추이</h2>
                <span className="text-xs text-text/50">최근 6개월</span>
            </div>
            <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={data} margin={{ top: 10, right: 20, left: 0, bottom: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" vertical={false} />
                        <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                        <YAxis
                            domain={['dataMin - 3', 'dataMax + 3']}
                            tickFormatter={(v) => `${Math.round(v)}%`}
                            tick={{ fontSize: 12 }}
                            width={44}
                        />
                        <Tooltip formatter={(v) => [`${v}%`, '입주율']} />
                        <Line type="monotone" dataKey="rate" stroke={MAIN} strokeWidth={2} dot={{ r: 4 }} connectNulls />
                    </LineChart>
                </ResponsiveContainer>
            </div>
        </section>
    )
}

export default OccupancyTrendChart