import ScreenPage from './pages/ScreenPage'
import CalendarPage from './features/community/pages/CalendarPage'
import FaqBotPage from './features/community/pages/FaqBotPage'
import NotesPage from './features/community/pages/NotesPage'
import ReportsPage from './features/community/pages/ReportsPage'
import ResidentsPage from './features/community/pages/ResidentsPage'
import { getAdminNoteBadgeCount, getAdminReportBadgeCount } from './features/community/api'

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
      { label: '입주자 관리', to: '/operations/residents', page: ResidentsPage },
      { label: '입주·퇴거 달력', to: '/operations/calendar', page: CalendarPage },
      { label: '쪽지함', to: '/operations/messages', page: NotesPage, badge: getAdminNoteBadgeCount },
      { label: '신고 관리', to: '/operations/reports', page: ReportsPage, badge: getAdminReportBadgeCount },
      { label: 'FAQ봇', to: '/operations/faq', page: FaqBotPage },
    ],
  },
  {
    label: '건물 관리',
    items: [{ label: '속성', to: '/buildings/attributes', page: ScreenPage }],
  },
  {
    label: '공용 자원',
    items: [
      { label: '공용 자원 관리', to: '/resources', page: ScreenPage },
      { label: '예약 관리', to: '/resources/reservations', page: ScreenPage },
      { label: '연체 관리', to: '/resources/overdue', page: ScreenPage },
    ],
  },
]

export const screens = navigation.flatMap((section) => section.items)

// 메뉴에 없는 주소. 사이드바에는 나오지 않습니다.
export const notFoundScreen = { label: '페이지를 찾을 수 없습니다', to: '*', page: ScreenPage }
