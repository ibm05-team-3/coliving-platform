import { SupportWidget } from '../features/community/components/SupportWidget'
import { useSupportWidget } from '../features/community/components/SupportWidgetContext'

function ChatButton() {
  const { isOpen, unreadSummary, openWidget, closeWidget } = useSupportWidget()
  return (
    <><button
      type="button"
      aria-label="채팅"
      onClick={() => isOpen ? closeWidget() : openWidget()}
      className="fixed right-4 bottom-4 z-30 grid h-12 w-12 place-items-center rounded-full bg-main text-background shadow-sm focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent sm:right-6 sm:bottom-6"
    >
      <svg viewBox="0 0 24 24" aria-hidden="true" className="h-6 w-6 fill-current">
        <path d="M5 4.5h14A2.5 2.5 0 0 1 21.5 7v7a2.5 2.5 0 0 1-2.5 2.5H10l-4.2 3.2c-.7.5-1.8 0-1.8-.8V7A2.5 2.5 0 0 1 5 4.5Z" />
      </svg>
      {unreadSummary.total > 0 && <span className="absolute top-0 right-0 h-2.5 w-2.5 rounded-full bg-red-500" />}
    </button><SupportWidget /></>
  )
}

export default ChatButton
