import { Route, Routes } from 'react-router'
import AdminLayout from './components/AdminLayout'
import LoginPage from './pages/LoginPage'
import ScreenPage from './pages/ScreenPage'
import { screens } from './navigation'

function App() {
  return (
    // BrowserRouter 는 main.jsx 에 존재합니다.
    // 로그인(/login)은 이 레이아웃 밖에 두어 사이드바를 노출하지 않습니다.
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<AdminLayout />}>
        {screens.map((screen) => (
          <Route key={screen.to} path={screen.to} element={<ScreenPage title={screen.label} />} />
        ))}
      </Route>
    </Routes>
  )
}

export default App
