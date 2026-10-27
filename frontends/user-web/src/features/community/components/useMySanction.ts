import { useEffect, useState } from "react"
import { getMySanction } from "../api"
import type { MySanction } from "../types"
const empty: MySanction = { active: false, sanctionId: null, sanctionType: null, durationDays: null, reason: null, startsAt: null, endsAt: null }
export function useMySanction() { const [sanction, setSanction] = useState(empty); useEffect(() => { void getMySanction().then(setSanction) }, []); return sanction }
