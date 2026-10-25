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
          <Route path="/apply/room" element={<ApplyRoomPage />} />
          <Route path="/apply/form" element={<ApplyFormPage />} />
          <Route path="/apply/submitted" element={<ApplySubmittedPage />} />
          <Route path="/contract/sign" element={<ContractSignPage />} />
          <Route path="/contract/pay" element={<ContractPayPage />} />
          <Route path="/contract/complete" element={<ContractCompletePage />} />
          <Route path="*" element={<Navigate to="/apply/room" replace />} />
        </Route>
      </Routes>
      {import.meta.env.VITE_PREVIEW === '1' && <PreviewBar />}
    </ApplyProvider>
  );
}
