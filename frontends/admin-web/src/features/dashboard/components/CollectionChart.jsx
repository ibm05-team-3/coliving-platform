import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { formatMan } from '../utils/format'

const MAIN = '#52796F'
const ACCENT = '#D6A56D'
const monthLabel = (m) => `${Number(m.slice(5))}월`

function CollectionChart({ trend }) {
    const data = trend.map((t) => ({
        name: monthLabel(t.month),
        collected: t.collectedAmount,
        unpaid: t.unpaidAmount,
    }))

    return (
        <section className="rounded-lg border border-main/20 bg-white p-5">
            <div className="mb-3 flex items-baseline justify-between">
                <h2 className="text-base font-semibold text-text">월별 수납·미수</h2>
                <span className="text-xs text-text/50">최근 6개월</span>
            </div>
            <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={data} margin={{ top: 10, right: 20, left: 0, bottom: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" vertical={false} />
                        <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                        <YAxis
                            tickFormatter={(v) => `${Math.round(v / 10000).toLocaleString()}`}
                            tick={{ fontSize: 12 }}
                            width={56}
                        />
                        <Tooltip formatter={(v, name) => [formatMan(v), name]} />
                        <Legend />
                        <Bar dataKey="collected" name="수납" fill={MAIN} radius={[3, 3, 0, 0]} />
                        <Bar dataKey="unpaid" name="미수" fill={ACCENT} radius={[3, 3, 0, 0]} />
                    </BarChart>
                </ResponsiveContainer>
            </div>
            <p className="mt-2 text-right text-xs text-text/50">단위: 만원</p>
        </section>
    )
}

export default CollectionChart