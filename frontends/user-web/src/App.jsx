import { Routes, Route } from 'react-router'
import LoginPage from './pages/user/LoginPage'

function App() {
  return (
    // BrowserRouter 는 main.jsx 에 존재합니다.
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route path="/login" element={<LoginPage />} />
    </Routes>
  )
}

export default App
