import ScreenPage from './pages/ScreenPage'

// 로그인(/login)은 이 목록에 넣지 않습니다. 그 화면은 AdminLayout 밖에 둡니다.
// page는 그 주소의 화면입니다. 아직 화면이 없으면 ScreenPage를 넣습니다.
export const navigation = [
  {
    items: [{ label: '대시보드', to: '/', page: ScreenPage }],
  },
  {
    label: '문서',
    items: [
      { label: '계약서', to: '/documents/contracts', page: ScreenPage },
      { label: '문서 양식', to: '/documents/forms', page: ScreenPage },
    ],
  },
  {
    label: '운영',
    items: [
      { label: '달력', to: '/operations/calendar', page: ScreenPage },
      { label: '입주자', to: '/operations/residents', page: ScreenPage },
      { label: '상담', to: '/operations/consultations', page: ScreenPage },
      { label: 'FAQ', to: '/operations/faq', page: ScreenPage },
    ],
  },
  {
    label: '건물 관리',
    items: [{ label: '속성', to: '/buildings/attributes', page: ScreenPage }],
  },
  {
    label: '공용 자원 예약',
    items: [
      { label: '공용 자원', to: '/resources', page: ScreenPage },
      { label: '예약 조회', to: '/resources/reservations', page: ScreenPage },
    ],
  },
]

export const screens = navigation.flatMap((section) => section.items)

// 메뉴에 없는 주소. 사이드바에는 나오지 않습니다.
export const notFoundScreen = { label: '페이지를 찾을 수 없습니다', to: '*', page: ScreenPage }
