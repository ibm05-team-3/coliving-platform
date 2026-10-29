import { useEffect, useMemo, useState } from "react"
import { getResidents } from "../api"
import { Avatar, Badge, Button, Icon, PageHeader, SearchField, Select } from "../components"
import type { Resident, ResidentStatus } from "../types"

type Props = { title: string }

const INPUT_CLASS = "w-full rounded-lg border border-main/15 bg-white px-3 py-2 text-sm text-text focus-visible:outline-2 focus-visible:outline-accent"
const statusType = (status: string) => status === "거주 중" ? "success" : status === "입주 예정" ? "info" : status === "퇴거 예정" ? "warning" : "neutral"

export default function ResidentsPage({ title }: Props) {
  const [residents, setResidents] = useState<Resident[]>([])
  const [selected, setSelected] = useState<Resident | null>(null)
  const [query, setQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState("퇴거 완료 제외")
  const [branchFilter, setBranchFilter] = useState("전체 지점")
  const [paymentFilter, setPaymentFilter] = useState("납부 상태")
  const [currentStatus, setCurrentStatus] = useState<ResidentStatus>("거주 중")
  const [moveOutDate, setMoveOutDate] = useState("")
  const [editOpen, setEditOpen] = useState(false)

  useEffect(() => {
    document.title = title
  }, [title])
  useEffect(() => {
    void getResidents().then((data) => {
      setResidents(data);
      setSelected(data[0] ?? null);
      setCurrentStatus(data[0]?.status ?? "거주 중")
    })
  }, [])

  const filtered = useMemo(() => residents.filter((resident) =>
    (resident.name.includes(query) || resident.branch.includes(query))
    && (statusFilter === "전체 상태" || (statusFilter === "퇴거 완료 제외" ? resident.status !== "퇴거 완료" : resident.status === statusFilter))
    && (branchFilter === "전체 지점" || resident.branch === branchFilter)
    && (paymentFilter === "납부 상태" || resident.payment === paymentFilter),
  ), [
    branchFilter,
    paymentFilter,
    query,
    residents,
    statusFilter
  ])

  const handleSelectResident = (resident: Resident) => {
    setSelected(resident);
    setCurrentStatus(resident.status);
    setMoveOutDate("")
  }
  const requiresMoveOutDate = currentStatus === "퇴거 예정" || currentStatus === "퇴거 완료"
  const handleSaveStatus = () => {
    if (!selected || (requiresMoveOutDate && !moveOutDate)) return
    const updated: Resident = {
      ...selected,
      status: currentStatus,
      expires: moveOutDate ? moveOutDate.replace(/-/g, ". ") : selected.expires
    }
    setResidents((items) => items.map((item) => item.name === selected.name ? updated : item));
    setSelected(updated)
  }

  return <div>

    <PageHeader
      eyebrow="RESIDENTS"
      title={title}
      description="전체 지점의 입주 현황과 계약 정보를 관리하세요."
      actions={<Select
        value={branchFilter}
        onChange={setBranchFilter}
      >
        <option>전체 지점</option>
        <option>망원 스테이</option>
        <option>연남 하우스</option>
        <option>성수 로프트</option>
      </Select>}
    />


    <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_320px]">

      <section className="overflow-hidden rounded-lg border border-main/15 bg-white">

        <div className="flex flex-col gap-3 border-b border-main/15 p-4 sm:flex-row sm:items-center sm:justify-between">

          <div className="flex flex-wrap gap-2">
            <Select
              value={statusFilter}
              onChange={setStatusFilter}
            >
              <option>퇴거 완료 제외</option>
              <option>전체 상태</option>
              <option>입주 예정</option>
              <option>거주 중</option>
              <option>퇴거 예정</option>
              <option>퇴거 완료</option>
            </Select>
            <Select
              value={paymentFilter}
              onChange={setPaymentFilter}
            >
              <option>납부 상태</option>
              <option>납부 완료</option>
              <option>미납</option>
            </Select>
          </div>


          <div className="w-full sm:w-64">
            <SearchField
              value={query}
              onChange={setQuery}
              placeholder="이름, 지점 검색"
            />
          </div>

        </div>


        <div className="overflow-x-auto">
          <table className="w-full min-w-[820px] text-left text-sm">
            <thead className="bg-background text-xs text-text/70">
              <tr>
                {[
                  "입주자",
                  "지점 / 호실",
                  "입주일",
                  "만료일",
                  "상태",
                  "납부 상태",
                  ""
                ].map((label) => <th
                  key={label}
                  className="px-4 py-3 font-semibold"
                >
                  {label}
                </th>)}
              </tr>
            </thead>
            <tbody>

              {filtered.map((resident) => <tr
                key={resident.name}
                onClick={() => handleSelectResident(resident)}
                className={`cursor-pointer border-t border-main/15 transition ${selected?.name === resident.name ? "bg-main/10" : "hover:bg-background"}`}
              >

                <td className="px-4 py-4">
                  <div className="flex items-center gap-3">
                    <Avatar
                      name={resident.name}
                      tone={resident.tone}
                      size="sm"
                    />
                    <strong className="text-text">
                      {resident.name}
                    </strong>
                  </div>
                </td>


                <td className="px-4 py-4">
                  <strong className="block text-text">
                    {resident.branch}
                  </strong>
                  <span className="text-xs text-text/70">
                    {resident.room}
                  </span>
                </td>
                <td className="px-4 py-4 text-text/70">
                  {resident.moveIn}
                </td>


                <td className="px-4 py-4">
                  <div className="flex items-center gap-2">
                    <span>
                      {resident.expires}
                    </span>
                    {resident.status === "퇴거 예정" && <Badge type="accent">D-7</Badge>}
                  </div>
                </td>
                <td className="px-4 py-4">
                  <Badge type={statusType(resident.status)}>
                    {resident.status}
                  </Badge>
                </td>
                <td className={`px-4 py-4 font-semibold ${resident.payment === "미납" ? "text-accent" : "text-text/70"}`}>
                  {resident.payment}
                </td>
                <td className="px-4 py-4">
                  <button
                    aria-label="관리"
                    className="rounded-lg p-2 text-text/70 focus-visible:outline-2 focus-visible:outline-accent"
                  >
                    <Icon name="more" />
                  </button>
                </td>

              </tr>)}


              {filtered.length === 0 && <tr>
                <td colSpan={7}>
                  <div className="flex min-h-56 flex-col items-center justify-center gap-3 p-8 text-center">
                    <span className="rounded-full bg-main/10 p-3 text-main">
                      <Icon
                        name="search"
                        size={24}
                      />
                    </span>
                    <strong className="text-text">검색 결과가 없습니다</strong>
                    <p className="text-sm text-text/70">다른 이름이나 지점명으로 다시 검색해보세요.</p>
                  </div>
                </td>
              </tr>}

            </tbody>
          </table>
        </div>


        <footer className="flex items-center justify-between border-t border-main/15 px-4 py-3 text-xs text-text/70">
          <span>총 126명 중 1–
            {filtered.length}
          </span>
          <div className="flex items-center gap-1">
            <PageButton ariaLabel="이전">
              <Icon
                name="chevronLeft"
                size={15}
              />
            </PageButton>
            <PageButton
              active
              ariaLabel="1"
            >1</PageButton>
            <PageButton ariaLabel="2">2</PageButton>
            <PageButton ariaLabel="3">3</PageButton>
            <PageButton ariaLabel="다음">
              <Icon
                name="chevronRight"
                size={15}
              />
            </PageButton>
          </div>
        </footer>

      </section>



      <aside className="h-fit rounded-lg border border-main/15 bg-white">
        {selected ? <>

          <header className="flex items-center justify-between border-b border-main/15 px-5 py-4">
            <span className="text-xs font-semibold tracking-wide text-text/70">입주자 상세</span>
            <button
              aria-label="관리"
              className="rounded-lg p-2 text-text/70 focus-visible:outline-2 focus-visible:outline-accent"
            >
              <Icon name="more" />
            </button>
          </header>


          <div className="flex flex-col items-center border-b border-main/15 p-6">
            <Avatar
              name={selected.name}
              tone={selected.tone}
              size="xl"
            />
            <h2 className="mt-3 text-xl font-semibold text-main">
              {selected.name}
            </h2>
            <Select
              value={currentStatus}
              onChange={(value) => {
                const next = value as ResidentStatus;
                setCurrentStatus(next);
                if (next !== "퇴거 예정" && next !== "퇴거 완료") setMoveOutDate("")
              }}
            >
              <option>입주 예정</option>
              <option>거주 중</option>
              <option>퇴거 예정</option>
              <option>퇴거 완료</option>
            </Select>


            {requiresMoveOutDate && <div className="mt-4 w-full rounded-lg bg-background p-3">
              <label className="text-xs font-semibold text-text/70">퇴거일
                <input
                  type="date"
                  value={moveOutDate}
                  onChange={(event) => setMoveOutDate(event.target.value)}
                  className={`${INPUT_CLASS} mt-2`}
                />
              </label>
              <Button
                className="mt-2 w-full"
                disabled={!moveOutDate}
                onClick={handleSaveStatus}
              >상태 저장</Button>
            </div>}

          </div>


          <DetailBlock title="연락처">
            <InfoLine icon="phone">010-2847-6931</InfoLine>
            <InfoLine icon="mail">minseo.kim@email.com</InfoLine>
          </DetailBlock>


          <DetailBlock title="거주 정보">
            <InfoPair
              label="지점"
              value={selected.branch}
            />
            <InfoPair
              label="호실"
              value={`${selected.room} · 스탠다드`}
            />
            <InfoPair
              label="계약 기간"
              value={`${selected.moveIn} — ${selected.expires}`}
            />
            <InfoPair
              label="월 이용료"
              value="890,000원"
            />
          </DetailBlock>


          <div className="mx-5 mb-5 rounded-lg bg-background p-4">
            <span className="text-xs font-bold tracking-[0.14em] text-accent">MEMO</span>
            <p className="mt-2 text-sm leading-6 text-text">주차 1대 등록. 택배는 평일 저녁 수령을 선호합니다.</p>
          </div>


          <footer className="flex justify-end border-t border-main/15 p-4">
            <Button
              variant="secondary"
              icon="edit"
              onClick={() => setEditOpen(true)}
            >수정</Button>
          </footer>

        </> : <div className="p-8 text-center text-sm text-text/70">입주자를 선택해 주세요.</div>}
      </aside>

    </div>



    {editOpen && selected && <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-text/40 p-4"
      onMouseDown={() => setEditOpen(false)}
    >
      <section
        role="dialog"
        aria-modal="true"
        onMouseDown={(event) => event.stopPropagation()}
        className="w-full max-w-xl rounded-lg border border-main/15 bg-white p-6"
      >
        <header className="mb-5 flex items-start justify-between">
          <div>
            <p className="text-xs font-bold tracking-[0.14em] text-accent">EDIT RESIDENT</p>
            <h2 className="mt-1 text-xl font-semibold text-main">입주 정보 수정</h2>
          </div>
          <button
            aria-label="닫기"
            onClick={() => setEditOpen(false)}
            className="rounded-lg p-2 text-text/70 focus-visible:outline-2 focus-visible:outline-accent"
          >
            <Icon name="close" />
          </button>
        </header>
        <div className="grid gap-4 sm:grid-cols-2">
          <EditField
            label="연락처"
            wide
          >
            <input
              className={INPUT_CLASS}
              defaultValue="010-2847-6931"
            />
          </EditField>
          <EditField
            label="호실"
            wide
          >
            <select
              className={INPUT_CLASS}
              defaultValue={selected.room}
            >
              <option>
                {selected.room}
              </option>
              <option>303호</option>
              <option>401호</option>
            </select>
          </EditField>
          <EditField label="입주일">
            <input
              className={INPUT_CLASS}
              type="date"
              defaultValue="2025-06-12"
            />
          </EditField>
          <EditField label="만료일">
            <input
              className={INPUT_CLASS}
              type="date"
              defaultValue="2026-06-11"
            />
          </EditField>
          <EditField
            label="메모"
            wide
          >
            <textarea
              className={`${INPUT_CLASS} min-h-24 resize-y`}
              defaultValue="주차 1대 등록. 택배는 평일 저녁 수령을 선호합니다."
            />
          </EditField>
        </div>
        <footer className="mt-6 flex justify-end gap-2">
          <Button
            variant="secondary"
            onClick={() => setEditOpen(false)}
          >취소</Button>
          <Button onClick={() => setEditOpen(false)}>저장</Button>
        </footer>
      </section>
    </div>}

  </div>
}

function PageButton({ children, active = false, ariaLabel }: {
  children: React.ReactNode;
  active?: boolean;
  ariaLabel: string
}) {
  return <button
    aria-label={ariaLabel}
    className={`flex h-8 min-w-8 items-center justify-center rounded-lg px-2 font-semibold focus-visible:outline-2 focus-visible:outline-accent ${active ? "bg-main text-white" : "text-text/70 hover:bg-background"}`}
  >
    {children}
  </button>
}
function DetailBlock({ title, children }: { title: string; children: React.ReactNode }) {
  return <section className="border-b border-main/15 p-5">
    <h3 className="mb-3 text-xs font-semibold text-text/70">
      {title}
    </h3>
    <div className="space-y-3">
      {children}
    </div>
  </section>
}
function InfoLine({ icon, children }: { icon: "phone" | "mail"; children: React.ReactNode }) {
  return <div className="flex items-center gap-3 text-sm text-text">
    <span className="text-main">
      <Icon
        name={icon}
        size={17}
      />
    </span>
    {children}
  </div>
}
function InfoPair({ label, value }: { label: string; value: string }) {
  return <div className="grid grid-cols-[72px_1fr] gap-3 text-sm">
    <span className="text-text/70">
      {label}
    </span>
    <strong className="text-right font-semibold text-text">
      {value}
    </strong>
  </div>
}
function EditField({ label, wide = false, children }: {
  label: string;
  wide?: boolean;
  children: React.ReactNode
}) {
  return <label className={`text-sm font-semibold text-text ${wide ? "sm:col-span-2" : ""}`}>
    <span className="mb-2 block">
      {label}
    </span>
    {children}
  </label>
}
