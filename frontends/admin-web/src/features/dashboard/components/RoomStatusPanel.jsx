
const STATUS = {
    OCCUPIED: { label: '입주', className: 'bg-main text-background' },
    VACANT: { label: '공실', className: 'bg-text/10 text-text/60' },
    MOVING_OUT: { label: '퇴거 예정', className: 'bg-accent text-text' },
    APPLYING: { label: '신청 진행 중', className: 'border border-main bg-background text-main' },
}

function groupByFloor(rooms) {
    const byFloor = new Map()

    for (const room of rooms) {
        const key = room.floor ?? '층 미정'
        if (!byFloor.has(key)) byFloor.set(key, [])
        byFloor.get(key).push(room)
    }

    return Array.from(byFloor.entries())
}

function RoomStatusPanel({ rooms }) {
    const floors = groupByFloor(rooms)

    return (
        <section className="rounded-lg border border-main/20 bg-white p-4">
            <h2 className="mb-3 text-center text-base font-semibold text-text">
                호실 현황
            </h2>

            <div className="space-y-2">
                {floors.map(([floor, list]) => (
                    <div key={floor} className="flex justify-center">
                        <div className="flex w-fit max-w-full items-start gap-3">
                            <span className="w-12 shrink-0 pt-1 text-right text-sm text-text/70">
                                {typeof floor === 'number' ? `${floor}층` : floor}
                            </span>

                            <div className="flex min-w-0 flex-wrap justify-center gap-1.5">
                                {list.map((room) => {
                                    const style = STATUS[room.status] ?? STATUS.VACANT

                                    return (
                                        <span
                                            key={room.roomId}
                                            className={`box-border inline-flex h-6 w-14 items-center justify-center rounded-md text-xs font-medium ${style.className}`}
                                        >
                                            {room.roomName}
                                        </span>
                                    )
                                })}
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            <div className="mt-3 flex flex-wrap justify-center gap-x-4 gap-y-1 text-xs text-text/70">
                {Object.entries(STATUS).map(([key, { label, className }]) => (
                    <span key={key} className="flex items-center gap-1.5">
                        <span
                            className={`inline-block h-3 w-3 rounded-sm ${className}`}
                        />
                        {label}
                    </span>
                ))}
            </div>
        </section>
    )
}

export default RoomStatusPanel
