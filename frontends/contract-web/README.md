# 계약 신청 화면 (CoLiv 스타일)

React + Vite + Tailwind CSS 4 + React Router.

## 실행
npm install
npm run dev          # http://localhost:5173/apply/room

## 화면 경로
/apply/room          1-1 호실 선택 (Step 1)
/apply/form          1-2 신청자 정보 (Step 1)
/apply/submitted     신청 접수 완료 (심사 중)
/contract/sign       계약서 확인·본인인증·서명 (Step 2)
/contract/pay        결제 (Step 3)
/contract/complete   계약 완료 (Step 4)

## 기존 front 프로젝트에 넣을 때
- src/components, src/pages, src/context, src/lib 폴더를 복사
- src/index.css 의 @theme 블록을 front의 CSS에 추가 (색상 토큰)
- App.jsx 의 Route 들을 front 라우터에 추가
- Layout.jsx(헤더·푸터)는 팀 공통 레이아웃이 있으면 그걸 쓰고 이 파일은 빼기

## 참고
- 데이터는 context/ApplyContext.jsx 의 목업 값. 매물 서비스(gRPC)·계약 API 연결 전 상태
- VITE_PREVIEW=1 로 빌드하면 화면 이동 바가 뜸 (확인용)
