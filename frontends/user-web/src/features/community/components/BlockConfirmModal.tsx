import { useState } from "react"
import { createPortal } from "react-dom"
import { createBlock } from "../api"
import { Button, Icon } from "."

export function BlockConfirmModal({ name, blockedUserId, onClose, onBlocked }: { name: string; blockedUserId: number; onClose: () => void; onBlocked: () => void }) {
  const [error, setError] = useState(""); const [submitting, setSubmitting] = useState(false)
  const block = async () => { setSubmitting(true); setError(""); try { await createBlock({ blockedUserId }); onBlocked() } catch (caught) { const message = caught instanceof Error ? caught.message : "처리하지 못했어요."; setError(message.includes("ALREADY_BLOCKED") ? "이미 차단한 사용자예요." : message.replace(/^[A-Z_]+:\s*/, "")) } finally { setSubmitting(false) } }
  return createPortal(<div className="fixed inset-0 z-50 flex items-center justify-center bg-text/40 p-4" onMouseDown={onClose}><section role="dialog" aria-modal="true" onMouseDown={(event) => event.stopPropagation()} className="w-full max-w-md rounded-lg border border-main/15 bg-white p-6 text-center"><span className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-accent/20 text-main"><Icon name="lock" size={22} /></span><h2 className="mt-4 text-xl font-semibold text-main">{name}님을 차단할까요?</h2><p className="mt-3 text-sm leading-6 text-text/70">차단하면 서로의 글과 댓글이 보이지 않고 채팅을 할 수 없어요. 상대에게는 알리지 않아요. 차단은 커뮤니티 &gt; 차단 관리에서 해제할 수 있어요.</p>{error && <p className="mt-3 text-sm font-semibold text-accent">{error}</p>}<footer className="mt-6 flex justify-center gap-2"><Button variant="secondary" onClick={onClose}>취소</Button><Button variant="danger" disabled={submitting} onClick={() => void block()}>{submitting ? "처리 중" : "차단하기"}</Button></footer></section></div>, document.body)
}
