import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import PreviewBar from './components/PreviewBar';
import { ApplyProvider } from './context/ApplyContext';
import ApplyFormPage from './pages/ApplyFormPage';
import ApplyRoomPage from './pages/ApplyRoomPage';
import ApplySubmittedPage from './pages/ApplySubmittedPage';
import ContractCompletePage from './pages/ContractCompletePage';
import ContractPayPage from './pages/ContractPayPage';
import ContractSignPage from './pages/ContractSignPage';

export default function App() {
  return (
    <ApplyProvider>
      <Routes>
        <Route element={<Layout />}>
          {/* 신청 전: roomId만 있음 (?roomId=1). 이전 단계로 돌아오면 &applicationNo= 도 붙음 */}
          <Route path="/apply/room" element={<ApplyRoomPage />} />
          {/* 신청 후: 신청번호를 URL에 둔다. 접근 권한은 서버가 JWT로 본인 확인 */}
          <Route path="/apply/:applicationNo/form" element={<ApplyFormPage />} />
          <Route path="/apply/:applicationNo/submitted" element={<ApplySubmittedPage />} />
          <Route path="/contract/:applicationNo/sign" element={<ContractSignPage />} />
          <Route path="/contract/:applicationNo/pay" element={<ContractPayPage />} />
          <Route path="/contract/:applicationNo/complete" element={<ContractCompletePage />} />
          <Route path="*" element={<Navigate to="/apply/room" replace />} />
        </Route>
      </Routes>
      {import.meta.env.VITE_PREVIEW === '1' && <PreviewBar />}
    </ApplyProvider>
  );
}
