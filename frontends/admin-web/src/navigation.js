// 로그인(/login)은 이 목록에 넣지 않습니다. 그 화면은 AdminLayout 밖에 둡니다.
export const navigation = [
  {
    items: [{ label: '대시보드', to: '/' }],
  },
  {
    label: '문서',
    items: [
      { label: '계약서', to: '/documents/contracts' },
      { label: '문서 양식', to: '/documents/forms' },
    ],
  },
  {
    label: '운영',
    items: [
      { label: '달력', to: '/operations/calendar' },
      { label: '입주자', to: '/operations/residents' },
      { label: '상담', to: '/operations/consultations' },
      { label: 'FAQ', to: '/operations/faq' },
    ],
  },
  {
    label: '건물 관리',
    items: [{ label: '속성', to: '/buildings/attributes' }],
  },
  {
    label: '공용 자원 예약',
    items: [
      { label: '공용 자원', to: '/resources' },
      { label: '예약 조회', to: '/resources/reservations' },
    ],
  },
]

export const screens = navigation.flatMap((section) => section.items)
