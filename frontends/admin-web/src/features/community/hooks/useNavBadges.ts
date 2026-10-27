import { useEffect, useState } from "react"
import { navigation } from "../../../navigation"

type BadgeCounts = Record<string, number>

export function useNavBadges(): BadgeCounts {
  const [counts, setCounts] = useState<BadgeCounts>({})

  useEffect(() => {
    let active = true
    const items = navigation.flatMap((section) => section.items).filter((item) => typeof item.badge === "function")
    const refresh = async () => {
      const results = await Promise.allSettled(items.map(async (item) => [item.to, await item.badge!()] as const))
      if (!active) return
      setCounts(Object.fromEntries(results.flatMap((result) => result.status === "fulfilled" ? [result.value] : [])))
    }
    const onRefresh = () => { void refresh() }
    void refresh()
    const interval = window.setInterval(refresh, 30_000)
    window.addEventListener("admin-badge-refresh", onRefresh)
    return () => { active = false; window.clearInterval(interval); window.removeEventListener("admin-badge-refresh", onRefresh) }
  }, [])

  return counts
}
