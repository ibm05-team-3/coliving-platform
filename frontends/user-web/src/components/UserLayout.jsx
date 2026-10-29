import { Outlet } from 'react-router'
import ChatButton from './ChatButton'
import Footer from './Footer'
import Header from './Header'
import { SupportWidgetProvider } from '../features/community/components/SupportWidgetContext'

function UserLayout() {
  return (
    <SupportWidgetProvider><div className="flex min-h-dvh flex-col bg-background text-text">
      <Header />
      <main className="flex-1">
        <Outlet />
      </main>
      <Footer />
      <ChatButton />
    </div></SupportWidgetProvider>
  )
}

export default UserLayout
