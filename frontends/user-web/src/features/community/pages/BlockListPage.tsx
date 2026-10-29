import { useEffect, useState } from "react"
import { useNavigate } from "react-router"
import { useTranslation } from "react-i18next"
import { deleteBlock, getBlocks } from "../api"
import { Avatar, Button, Icon, PageHeader } from "../components"
import { CommunityRestricted } from "../components/CommunityRestricted"
import { useCurrentUser } from "../currentUser"
import type { BlockedUser } from "../types"

const date = (value: string, locale: string) => new Date(value).toLocaleDateString(locale, {
  year: "numeric",
  month: "long",
  day: "numeric"
})
export default function BlockListPage({ title }: { title: string }) {
  const { t, i18n } = useTranslation("community");
  const user = useCurrentUser();
  const navigate = useNavigate();
  const [blocks, setBlocks] = useState<BlockedUser[] | null>(null);
  const [target, setTarget] = useState<BlockedUser | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [toast, setToast] = useState("")
  useEffect(() => {
    document.title = t("blocks.title", { defaultValue: title })
  }, [
    i18n.language,
    t,
    title
  ]);
  useEffect(() => {
    if (user.role === "RESIDENT") void getBlocks().then((response) => setBlocks(response.blocks))
  }, [user.role])
  if (user.role !== "RESIDENT") return <CommunityRestricted />
  const handleUnblock = async () => {
    if (!target || submitting) return;
    setSubmitting(true);
    await deleteBlock(target.blockedUserId);
    setBlocks((items) => items?.filter((item) => item.blockedUserId !== target.blockedUserId) ?? []);
    setTarget(null);
    setSubmitting(false);
    setToast(t("blocks.done"));
    window.setTimeout(() => setToast(""), 2400)
  }
  return <main className="px-4 py-6 sm:px-8 md:px-12">
    <button
      onClick={() => navigate("/community")}
      className="mb-4 inline-flex items-center gap-1 text-sm font-semibold text-main focus-visible:outline-2 focus-visible:outline-accent"
    >
      <Icon
        name="chevronLeft"
        size={17}
      />
      {t("blocks.back")}
    </button>
    <PageHeader
      eyebrow={t("common.eyebrow")}
      title={t("blocks.title")}
      description={t("blocks.description")}
    />


    <section className="overflow-hidden rounded-lg border border-main/15 bg-white">
      <header className="flex items-center justify-between border-b border-main/15 p-5">
        <div>
          <h2 className="font-semibold text-main">
            {t("blocks.listTitle")}
          </h2>
          <p className="mt-1 text-xs text-text/70">
            {t("blocks.listDescription")}
          </p>
        </div>
        <strong className="text-sm text-main">
          {t("blocks.count", { count: blocks?.length ?? 0 })}
        </strong>
      </header>
      <div>
        {blocks?.map((item) => <article
          key={item.blockedUserId}
          className="grid gap-4 border-b border-main/15 p-5 sm:grid-cols-[auto_minmax(0,1fr)_auto_auto] sm:items-center"
        >
          <Avatar name={item.name} />
          <div>
            <strong className="block text-text">
              {item.name}
            </strong>
            <span className="text-xs text-text/70">
              {item.roomNumber ?? t("blocks.movedOut")}
            </span>
          </div>
          <div className="text-sm">
            <span className="block text-xs text-text/70">
              {t("blocks.blockedDate")}
            </span>
            <time>
              {date(item.blockedAt, i18n.resolvedLanguage ?? i18n.language)}
            </time>
          </div>
          <Button
            variant="secondary"
            onClick={() => setTarget(item)}
          >
            {t("blocks.unblock")}
          </Button>
        </article>)}
        {blocks !== null && blocks.length === 0 && <div className="flex min-h-64 flex-col items-center justify-center gap-3 p-8 text-center">
          <span className="rounded-full bg-main/10 p-3 text-main">
            <Icon
              name="users"
              size={26}
            />
          </span>
          <strong className="text-text">
            {t("blocks.empty")}
          </strong>
          <p className="text-sm text-text/70">
            {t("blocks.emptyDescription")}
          </p>
        </div>}
      </div>
      <footer className="flex items-center gap-2 bg-background p-4 text-xs text-text/70">
        <Icon
          name="bell"
          size={15}
        />
        {t("blocks.notice")}
      </footer>
    </section>


    {target && <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-text/40 p-4"
      onMouseDown={() => !submitting && setTarget(null)}
    >
      <section
        role="dialog"
        aria-modal="true"
        onMouseDown={(event) => event.stopPropagation()}
        className="w-full max-w-md rounded-lg border border-main/15 bg-white p-6 text-center"
      >
        <span className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-main/10 text-main">
          <Icon
            name="users"
            size={23}
          />
        </span>
        <h2 className="mt-4 text-xl font-semibold text-main">
          {t("blocks.confirmTitle", { name: target.name })}
        </h2>
        <p className="mt-3 text-sm text-text/70">
          {t("blocks.confirmDescription")}
        </p>
        <footer className="mt-6 flex justify-center gap-2">
          <Button
            variant="secondary"
            onClick={() => setTarget(null)}
          >
            {t("common.cancel")}
          </Button>
          <Button
            disabled={submitting}
            onClick={() => void handleUnblock()}
          >
            {submitting ? t("common.processing") : t("blocks.unblock")}
          </Button>
        </footer>
      </section>
    </div>}


    {toast && <div className="fixed right-6 bottom-20 z-50 rounded-lg bg-main px-5 py-3 text-sm font-semibold text-background">
      {toast}
    </div>}

  </main>
}
