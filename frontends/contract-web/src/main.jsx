import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, HashRouter } from 'react-router-dom';
import App from './App';
import './index.css';

// 미리보기 빌드는 정적 호스팅이라 HashRouter, 실제 프로젝트는 BrowserRouter
const Router = import.meta.env.VITE_PREVIEW === '1' ? HashRouter : BrowserRouter;

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <Router>
      <App />
    </Router>
  </StrictMode>,
);
