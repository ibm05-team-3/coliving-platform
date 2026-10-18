react + vite 테스트 방법: npm run dev


화면은 AI로 Tailwind CSS 적용해서 만들어주세요.

// 디자인:
// - Tailwind CSS 클래스만 사용 (커스텀 CSS 금지) coliving에 맞는 스타일로 디자인.
// - 모바일 우선으로 반응형 으로 제작. sm 640, md 768, lg 1024
// - 색상은 아래 디자인 토큰을 사용할 것
//   --color-main: #52796F;
//   --color-background #F3F5F1;
//   --color-text: #26332E;
//   --color-accent: #D6A56D;

// 다국어:
// - 화면에 보이는 문구는 JSX에 직접 쓰지 않고 i18next, react-i18next로 읽을 것
// - 문구 파일은 src/i18n/locales/ko, src/i18n/locales/en 에 둘 것
// - 파일 이름이 네임스페이스다. 예: common.json, login.json
// - ko가 기준이고 en은 같은 키를 가진다. 문구를 추가하면 두 파일에 함께 넣을 것
// - 키는 문장이 아니라 화면 의미로 고정할 것. 예: login.title, login.email
// - 컴포넌트에서는 useTranslation(네임스페이스)와 t('키')를 사용할 것
// - 값이 바뀌는 문장은 {{name}} 자리를 둘 것
// - 날짜, 금액, 숫자 표기는 Intl에서 가져올 것
// - 현재 언어는 document.documentElement.lang 에서 가져올 것
// - 버튼과 제목은 글자 수에 맞게 늘어나게 두고 고정 너비로 잠그지 말 것

// 화면 설명:
// React + Tailwind CSS로 다국어 고려해서 [화면 이름]을 만들어줘.
// [여기에 원하는 화면을 자세히 설명]
