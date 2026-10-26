react + vite 테스트 방법:
```
npm run dev
```

화면은 Tailwind CSS 적용하도록 AI에게 만들어 달라고 해주세요.
admin-web은 다국어 지원이 필요없습니다.

프롬프트 예시:
```
React + Tailwind CSS로 [화면 이름]을 구현해줘.

디자인:
- 기존 프로젝트의 기술 스택과 Tailwind CSS 버전을 유지할 것.
- Tailwind CSS 유틸리티 클래스만 사용하고 커스텀 CSS는 추가하지 말 것.
- coliving 서비스에 어울리는 디자인을 적용할 것.
- 모바일 우선 반응형으로 제작할 것. sm 640px, md 768px, lg 1024px.
- 디자인 토큰:
  main: #52796F
  background: #F3F5F1
  text: #26332E
  accent: #D6A56D
- 사이드바와 상단 「메뉴」는 만들지 말고, AdminLayout 안의 본문만 구현할 것.
- 1024px 이상에서는 왼쪽 16rem이 사이드바므로 본문은 남은 너비 안에 둘 것. 1024px 미만에서는 본문이 화면 가로를 모두 쓸 것.
- fixed inset-0, h-screen, w-screen으로 화면 전체를 덮지 말 것.
- 가로로 긴 내용은 overflow-x-auto로 페이지 안에서 스크롤할 것.


화면 요구사항:
[화면 목적, 구성 요소, 주요 동작 및 상세 설명]
```

## 메뉴 추가

메뉴를 추가할 때는 `src/navigation.js`만 수정합니다. 사이드바 컴포넌트나 라우트 파일은 수정하지 않습니다.

이 파일의 배열 순서가 사이드바에 보이는 순서입니다. 각 항목은 화면 이름(`label`), 주소(`to`), 페이지(`page`)입니다. 페이지가 아직 없으면 `page`에 `ScreenPage`를 넣습니다.

운영 메뉴에 공지를 넣는다면 `items` 안에 한 줄을 추가합니다.

```js
{
  label: '운영',
  items: [
    { label: '달력', to: '/operations/calendar', page: ScreenPage },
    { label: '입주자', to: '/operations/residents', page: ScreenPage },
    { label: '상담', to: '/operations/consultations', page: ScreenPage },
    { label: 'FAQ', to: '/operations/faq', page: ScreenPage },
    { label: '공지', to: '/operations/notices', page: ScreenPage },
  ],
},
```

저장하면 사이드바에 공지가 생기고, `/operations/notices`는 `ScreenPage`가 열립니다. 제목은 공지이고, 본문에는 "표시할 내용이 없습니다."라고 표시됩니다. 라우트는 `App.jsx`가 이 목록의 `page`로 만듭니다.

문서, 운영 같은 새 묶음이 필요하면 객체를 하나 더 추가합니다.

```js
{
  label: '정산',
  items: [{ label: '청구', to: '/billing/invoices', page: ScreenPage }],
},
```

- `to`는 기존 메뉴와 다르게 적습니다.
- `/login`은 이 목록에 넣지 않습니다. 로그인 화면에는 사이드바가 없습니다.
- 빈 화면을 실제 화면으로 바꿀 때는 그 항목의 `page`만 바꿉니다. `App.jsx`는 수정하지 않습니다.

```js
{ label: '공지', to: '/operations/notices', page: NoticesPage },
```
