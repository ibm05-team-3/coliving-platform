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

## API 연결
- API 호출은 모두 src/lib/api.js 를 거친다 (화면에서 fetch 직접 사용 금지)
- 연결된 API
  - 1-1 호실 조회 `GET /internel/apply/room/{roomId}`
  - 1-1 다음 단계 `POST /apply/applications` (같은 호실 작성 중 신청서가 있으면 서버가 재사용)
  - 1-2 신청자 정보 `GET /apply/applications/{applicationNo}/form`
  - 1-2 제출 `POST /apply/applications/{applicationNo}/submit`
  - 접수 완료 이후 화면은 아직 목업
- 서버 없이 화면만 볼 때: .env.local 에 VITE_USE_MOCK=1
- 개발 서버는 /contract-service/** 를 gateway(8081)로 프록시. 바로 8089로 붙이는 방법은 .env.example 참고
- 회원 번호는 X-User 헤더. gateway 없이 테스트할 때는 VITE_DEV_USER_ID 값을 보냄
- contract-service를 dev,dummy-room,dummy-user 프로필로 켜면 매물·회원 서비스 없이 테스트 가능
  - roomId=1 사진 없음 / 2 사진 2장 / 3 삭제된 호실(404) / 503 매물 서비스 꺼짐(503)
  - X-User=1 정상 회원 / 2 주소 없음 / 503 회원 서비스 꺼짐 / 그 외 없는 회원(404)
  - 예: http://localhost:5173/apply/room?roomId=1

## 참고
- 접수 완료 이후 화면 데이터는 context/ApplyContext.jsx 의 값 (1-2에서 받은 서버 값으로 채워짐)
- VITE_PREVIEW=1 로 빌드하면 화면 이동 바가 뜸 (확인용)
