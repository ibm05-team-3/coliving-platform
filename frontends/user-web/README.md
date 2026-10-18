react + vite 테스트 방법: npm run dev


화면은 AI로 Tailwind CSS 적용해서 만들어주세요.

// 요구사항:
// - Tailwind CSS 클래스만 사용할 것 (커스텀 CSS 금지)
// - 반응형 필수 (모바일 우선)
//   - 기본: 모바일
//   - sm: 640px 이상
//   - md: 768px 이상
//   - lg: 1024px 이상
// - 색상은 아래 디자인 토큰을 사용할 것
//   - 메인: #52796F
//   - 배경: #F3F5F1
//   - 텍스트: #26332E
//   - 강조: #D6A56D
// - 컴포넌트는 재사용 가능하게 작성
// - 불필요한 div 중첩 최소화

// coliving에 맞는 스타일로 디자인해주세요.
// 공통 css index.css

// 다국어:
// - 화면에 보이는 문구는 JSX에 직접 쓰지 않고 i18next, react-i18next로 읽을 것
// - 문구 파일은 src/i18n/locales/ko, src/i18n/locales/en 에 둘 것
// - 파일 이름이 네임스페이스다. 예: common.json, login.json
// - ko가 기준이고 en은 같은 키를 가진다. 문구를 추가하면 두 파일에 함께 넣을 것
// - 키는 문장이 아니라 화면 의미로 고정할 것. 예: login.title, login.email
// - 컴포넌트에서는 useTranslation(네임스페이스)와 t('키')를 사용할 것
// - 값이 바뀌는 문장은 {{name}} 자리를 둘 것
// - 날짜, 금액, 숫자는 JSON에 넣지 않고 Intl에 현재 로케일을 넘길 것
// - 기본 언어는 ko. 선택 순서는 localStorage의 locale, 브라우저 언어, ko
// - 언어가 바뀌면 document.documentElement.lang도 바꿀 것
// - 주소에 로케일 경로를 넣지 말 것
// - 서버 응답 원문, 사용자가 쓴 글, 메일 문구는 이 카탈로그에 넣지 말 것
// - API 오류는 에러 코드로 받고 t('errors.코드')로 표시할 것
// - 브랜드명 COLIVING은 그대로 둘 것
// - 버튼과 제목은 글자 수에 맞게 늘어나게 두고 고정 너비로 잠그지 말 것

// 화면 설명:
// React + Tailwind CSS로 [화면 이름]을 만들어줘.
// 화면에 보이는 문구는 src/i18n/locales/ko 와 en에 같은 키로 넣고 t()로 표시해줘.
// [여기에 원하는 화면을 자세히 설명]