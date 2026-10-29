import { Routes, Route } from 'react-router'
import UserLayout from './components/UserLayout'
import HomePage from './pages/user/HomePage'
import LoginPage from './pages/user/LoginPage'
import SectionPage from './pages/user/SectionPage'
import BlockListPage from './features/community/pages/BlockListPage'
import CommunityPage from './features/community/pages/CommunityPage'
import PostEditorView from './features/community/pages/PostEditorView'

function App() {
  return (
    // BrowserRouter 는 main.jsx 에 존재합니다.
    // 로그인(/login)은 이 레이아웃 밖에 두어 헤더와 풋터를 노출하지 않습니다.
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<UserLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/find" element={<SectionPage titleKey="find" />} />
        <Route path="/community" element={<CommunityPage title="커뮤니티" />} />
        <Route path="/community/posts/:postId" element={<PostEditorView mode="view" title="게시글" />} />
        <Route path="/community/write" element={<PostEditorView mode="create" title="새 글 작성" />} />
        <Route path="/community/posts/:postId/edit" element={<PostEditorView mode="edit" title="게시글 수정" />} />
        <Route path="/community/blocks" element={<BlockListPage title="차단 관리" />} />
        <Route path="/facilities" element={<SectionPage titleKey="facilityReservation" />} />
        <Route path="/my/profile" element={<SectionPage titleKey="myProfile" />} />
        <Route path="/my/application" element={<SectionPage titleKey="myApplication" />} />
        <Route path="/my/reservations" element={<SectionPage titleKey="myReservations" />} />
        <Route path="/my/settings" element={<SectionPage titleKey="accountSettings" />} />
        <Route path="/signup" element={<SectionPage titleKey="signup" />} />
      </Route>
    </Routes>
  )
}

export default App
