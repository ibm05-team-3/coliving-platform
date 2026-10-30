import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  return {
    plugins: [react(), tailwindcss()],
    base: '/',
    server: {
      // 개발 서버(5173)에서 /contract-service/** 요청을 gateway(8081)로 넘김 → 브라우저 CORS 문제 없음
      // gateway 없이 contract-service(8089)에 바로 붙이려면 .env.local에
      //   VITE_API_TARGET=http://localhost:8089 + VITE_API_STRIP=1  (gateway 대신 /contract-service를 떼어줌)
      proxy: {
        '/contract-service': {
          target: env.VITE_API_TARGET || 'http://localhost:8081',
          changeOrigin: true,
          ...(env.VITE_API_STRIP === '1' && {
            rewrite: (path) => path.replace(/^\/contract-service/, ''),
          }),
        },
      },
    },
  };
});
