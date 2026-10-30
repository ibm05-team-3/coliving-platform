import { Route, Routes } from 'react-router'
import AdminLayout from './components/AdminLayout'
import RequireAuth from './components/RequireAuth'
import LoginPage from './pages/LoginPage'
import ScreenPage from './pages/ScreenPage'
import { notFoundScreen, screens } from './navigation'

function App() {
  return (
    // BrowserRouter 는 main.jsx 에 존재합니다.
    // 로그인(/login)은 이 레이아웃 밖에 두어 사이드바를 노출하지 않습니다.
    // 그 밖의 주소는 로그인했고 ADMIN 권한이 있을 때만 엽니다.
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RequireAuth />}>
        <Route element={<AdminLayout />}>
          {screens.map((screen) => {
            const Page = screen.page
            return <Route key={screen.to} path={screen.to} element={<Page title={screen.label} />} />
          })}
          <Route path={notFoundScreen.to} element={<ScreenPage title={notFoundScreen.label} />} />
        </Route>
      </Route>
    </Routes>
  )
}

export default App
