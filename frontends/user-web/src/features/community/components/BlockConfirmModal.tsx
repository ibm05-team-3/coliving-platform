import { useState } from "react"
import { createPortal } from "react-dom"
import { useTranslation } from "react-i18next"
import { createBlock } from "../api"
import { Button, Icon } from "."

export function BlockConfirmModal({ name, blockedUserId, onClose, onBlocked }: {
  name: string;
  blockedUserId: number;
  onClose: () => void;
  onBlocked: () => void
}) {
  const { t } = useTranslation("community")
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false)
  const handleBlock = async () => {
    setSubmitting(true);
    setError("");
    try {
      await createBlock({ blockedUserId });
      onBlocked()
    } catch (caught) {
      const message = caught instanceof Error ? caught.message : "";
      setError(message.includes("ALREADY_BLOCKED") ? t("blockModal.already") : t("blockModal.failed"))
    } finally {
      setSubmitting(false)
    }
  }
  return createPortal(<div
    className="fixed inset-0 z-50 flex items-center justify-center bg-text/40 p-4"
    onMouseDown={onClose}
  >
    <section
      role="dialog"
      aria-modal="true"
      onMouseDown={(event) => event.stopPropagation()}
      className="w-full max-w-md rounded-lg border border-main/15 bg-white p-6 text-center"
    >
      <span className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-accent/20 text-main">
        <Icon
          name="lock"
          size={22}
        />
      </span>
      <h2 className="mt-4 text-xl font-semibold text-main">
        {t("blockModal.title", { name })}
      </h2>
      <p className="mt-3 text-sm leading-6 text-text/70">
        {t("blockModal.description")}
      </p>
      {error && <p className="mt-3 text-sm font-semibold text-accent">
        {error}
      </p>}
      <footer className="mt-6 flex justify-center gap-2">
        <Button
          variant="secondary"
          onClick={onClose}
        >
          {t("common.cancel")}
        </Button>
        <Button
          variant="danger"
          disabled={submitting}
          onClick={() => void handleBlock()}
        >
          {submitting ? t("common.processing") : t("blockModal.block")}
        </Button>
      </footer>
    </section>
  </div>, document.body)
}
