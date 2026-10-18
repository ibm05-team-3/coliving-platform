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
- 토큰은 프로젝트 설정에 맞게 등록하고 재사용할 것.

화면 요구사항:
[화면 목적, 구성 요소, 주요 동작 및 상세 설명]
```
