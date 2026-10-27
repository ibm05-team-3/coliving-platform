import { Outlet } from 'react-router'
import ChatButton from './ChatButton'
import Footer from './Footer'
import Header from './Header'

function UserLayout() {
  return (
    <div className="flex min-h-dvh flex-col bg-background text-text">
      <Header />
      <main className="flex-1">
        <Outlet />
      </main>
      <Footer />
      <ChatButton />
    </div>
  )
}

export default UserLayout
