import { useEffect, useState } from "react"
import { useNavigate } from "react-router"
import { getMoveSchedules } from "../api"
import { Badge, Button, Icon, PageHeader, Select } from "../components"
import type { MoveScheduleEvent } from "../types"

type Props = { title: string }
type CalendarDay = { day: number; current: boolean }

const CALENDAR_DAYS: CalendarDay[] = [
  28,
  29,
  30
].map((day) => ({ day, current: false }))
  .concat(Array.from({ length: 31 }, (_, index) => ({ day: index + 1, current: true })))
  .concat([{ day: 1, current: false }])

export default function CalendarPage({ title }: Props) {
  const navigate = useNavigate()
  const [events, setEvents] = useState<Record<number, MoveScheduleEvent[]>>({})
  const [selectedDay, setSelectedDay] = useState(18)
  const [branch, setBranch] = useState("전체 지점")

  useEffect(() => {
    document.title = title
  }, [title])
  useEffect(() => {
    void getMoveSchedules().then(setEvents)
  }, [])
  const selectedEvents = events[selectedDay] ?? []

  return <div>

    <PageHeader
      eyebrow="SCHEDULE"
      title={title}
      description="입주와 퇴거 일정을 한눈에 확인하세요."
      actions={<Select
        value={branch}
        onChange={setBranch}
      >
        <option>전체 지점</option>
        <option>망원 스테이</option>
        <option>연남 하우스</option>
        <option>성수 로프트</option>
      </Select>}
    />


    <div className="mb-4 flex flex-wrap items-center gap-3">

      <button
        aria-label="이전 달"
        className="rounded-lg border border-main/15 bg-white p-2 text-main focus-visible:outline-2 focus-visible:outline-accent"
      >
        <Icon name="chevronLeft" />
      </button>
      <h2 className="text-lg font-semibold text-main">2025년 10월</h2>
      <button
        aria-label="다음 달"
        className="rounded-lg border border-main/15 bg-white p-2 text-main focus-visible:outline-2 focus-visible:outline-accent"
      >
        <Icon name="chevronRight" />
      </button>
      <Button variant="secondary">오늘</Button>


      <div className="ml-auto flex items-center gap-4 text-xs font-semibold text-text/70">
        <span className="flex items-center gap-1.5">
          <i className="h-2.5 w-2.5 rounded-full bg-main" />
          입주</span>
        <span className="flex items-center gap-1.5">
          <i className="h-2.5 w-2.5 rounded-full bg-accent" />
          퇴거</span>
      </div>

    </div>


    <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_300px]">

      <section className="overflow-hidden rounded-lg border border-main/15 bg-white">

        <div className="grid grid-cols-7 border-b border-main/15 bg-background text-center text-xs font-semibold text-text/70">
          {[
            "일",
            "월",
            "화",
            "수",
            "목",
            "금",
            "토"
          ].map((day) => <span
            key={day}
            className="py-3"
          >
            {day}
          </span>)}
        </div>


        <div className="grid grid-cols-7">
          {CALENDAR_DAYS.map(({ day, current }, index) => {
            const dayEvents = current ? events[day] ?? [] : []
            const selected = current && selectedDay === day
            return <button
              key={`${day}-${index}`}
              disabled={!current}
              onClick={() => setSelectedDay(day)}
              className={`min-h-28 border-r border-b border-main/15 p-2 text-left align-top transition focus-visible:z-10 focus-visible:outline-2 focus-visible:outline-accent sm:min-h-32 ${current ? "bg-white hover:bg-background" : "bg-background text-text/70"} ${selected ? "bg-main/10" : ""}`}
            >

              <span className={`inline-flex h-7 w-7 items-center justify-center rounded-full text-xs font-semibold ${current && day === 18 ? "bg-main text-white" : "text-text/70"}`}>
                {day}
              </span>
              <span className="mt-1 block space-y-1">
                {dayEvents.map((event, eventIndex) => <span
                  key={eventIndex}
                  className={`block truncate rounded px-2 py-1 text-[11px] font-semibold ${event.type === "in" ? "bg-main/15 text-main" : "bg-accent/20 text-text"}`}
                >
                  {event.label}
                </span>)}
              </span>

            </button>
          })}
        </div>

      </section>


      <aside className="space-y-4">

        <section className="rounded-lg border border-main/15 bg-white p-5">
          <h3 className="text-xs font-semibold text-text/70">10월 일정</h3>
          <div className="mt-4 grid grid-cols-2 gap-3">
            <Summary
              label="입주"
              count={12}
              tone="main"
            />
            <Summary
              label="퇴거"
              count={7}
              tone="accent"
            />
          </div>
        </section>


        <section className="rounded-lg border border-main/15 bg-white">
          <header className="flex items-end justify-between border-b border-main/15 p-5">
            <div>
              <p className="text-xs font-semibold text-text/70">선택한 날짜</p>
              <h3 className="mt-1 text-lg font-semibold text-main">10월
                {selectedDay}
                일</h3>
            </div>
            <strong className="text-sm text-main">
              {selectedEvents.length}
              건</strong>
          </header>


          <div className="p-5">
            {selectedEvents.length ? <div className="space-y-5">
              {selectedEvents.map((event, index) => {
                const [name, room] = event.label.split(" · ");
                return <article
                  key={index}
                  className="relative border-l-2 border-main/15 pl-4"
                >
                  <Badge type={event.type === "in" ? "success" : "warning"}>
                    {event.type === "in" ? "입주" : "퇴거"}
                  </Badge>
                  <strong className="mt-2 block text-sm text-text">
                    {name}
                  </strong>
                  <p className="mt-1 text-xs text-text/70">망원 스테이 ·
                    {room}
                  </p>
                  <span className="mt-2 block text-xs text-text/70">2025.10.18 ~ 2026.10.17</span>
                  <button
                    onClick={() => navigate("/operations/residents")}
                    className="mt-3 inline-flex items-center gap-1 text-xs font-semibold text-main focus-visible:outline-2 focus-visible:outline-accent"
                  >입주자 정보 보기
                    <Icon
                      name="chevronRight"
                      size={12}
                    />
                  </button>
                </article>
              })}
            </div> : <div className="flex min-h-44 flex-col items-center justify-center gap-3 text-center text-text/70">
              <span className="rounded-full bg-main/10 p-3 text-main">
                <Icon
                  name="calendar"
                  size={28}
                />
              </span>
              <p className="text-sm">예정된 일정이 없습니다.</p>
            </div>}
          </div>

        </section>

      </aside>

    </div>

  </div>
}

function Summary({ label, count, tone }: {
  label: string;
  count: number;
  tone: "main" | "accent"
}) {
  return <div className="rounded-lg bg-background p-3">
    <span className={`text-xs font-semibold ${tone === "main" ? "text-main" : "text-accent"}`}>
      {label}
    </span>
    <strong className="mt-2 block text-2xl text-text">
      {count}
      <small className="ml-1 text-xs font-normal text-text/70">건</small>
    </strong>
  </div>
}
