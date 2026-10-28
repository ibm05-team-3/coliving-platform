import type { BlockListResponse, ChatMessageListResponse, ChatRoom, CommunityPostDetail, CommunityPostListResponse, CommunityPostSummary, FaqItem, MySanction, NotificationListResponse, UnreadNotificationSummary, UserNoteListResponse } from "../types"

const PINNED: CommunityPostSummary = {
  postId: 100,
  category: "NOTICE",
  title: "10월 공용 라운지 정기 청소 안내",
  authorName: "망원 스테이 매니저",
  viewCount: 154,
  commentCount: 3,
  createdAt: "2025-10-18T09:00:00"
}
const SUMMARIES: CommunityPostSummary[] = [
  {
    postId: 1,
    category: "FREE",
    title: "이번 주말 한강 피크닉 같이 가실 분?",
    authorId: 5001,
    authorName: "김민서",
    authorRoomNumber: "302호",
    viewCount: 31,
    commentCount: 7,
    createdAt: "2025-10-18T10:00:00",
    isNew: true
  },
  {
    postId: 2,
    category: "SHARE",
    title: "이케아 원목 협탁 무료 나눔합니다",
    authorId: 5002,
    authorName: "박준영",
    authorRoomNumber: "201호",
    viewCount: 87,
    commentCount: 12,
    createdAt: "2025-10-18T09:42:00",
    isNew: true
  },
  {
    postId: 3,
    category: "FREE",
    title: "망원동 조용한 작업 카페 추천해주세요",
    authorId: 5003,
    authorName: "정하은",
    authorRoomNumber: "304호",
    viewCount: 63,
    commentCount: 6,
    createdAt: "2025-10-17T21:15:00",
    isNew: true
  },
  {
    postId: 4,
    category: "MEETUP",
    title: "이번 달 영화 모임은 ‘퍼펙트 데이즈’입니다",
    authorId: 5004,
    authorName: "윤가영",
    authorRoomNumber: "401호",
    viewCount: 104,
    commentCount: 21,
    createdAt: "2025-10-17T19:20:00"
  },
  {
    postId: 5,
    category: "SHARE",
    title: "안 읽는 책 5권, 필요한 분께 드려요",
    authorId: 5001,
    authorName: "김민서",
    authorRoomNumber: "302호",
    viewCount: 42,
    commentCount: 9,
    createdAt: "2025-10-16T18:05:00"
  },
  {
    postId: 6,
    category: "MEETUP",
    title: "목요일 저녁 망원 러닝 크루 모집해요",
    authorId: 5002,
    authorName: "박준영",
    authorRoomNumber: "201호",
    viewCount: 55,
    commentCount: 16,
    createdAt: "2025-10-16T12:30:00"
  },
  {
    postId: 7,
    category: "FREE",
    title: "공용 주방에 두고 간 텀블러 주인 찾습니다",
    authorId: 5003,
    authorName: "정하은",
    authorRoomNumber: "304호",
    viewCount: 28,
    commentCount: 4,
    createdAt: "2025-10-15T20:12:00"
  },
  {
    postId: 8,
    category: "SHARE",
    title: "몬스테라 화분 나눔받으실 분 계실까요?",
    authorId: 5004,
    authorName: "윤가영",
    authorRoomNumber: "401호",
    viewCount: 76,
    commentCount: 11,
    createdAt: "2025-10-15T11:10:00"
  },
]
export const COMMUNITY_POSTS: CommunityPostListResponse = {
  buildingName: "망원 스테이",
  pinned: [PINNED],
  content: SUMMARIES,
  page: 0,
  size: 20,
  totalElements: SUMMARIES.length,
  totalPages: 1
}
const BODIES: Record<number, string> = {
  1: "이번 주 토요일 오후 3시에 망원 한강공원에서 가볍게 피크닉 하려고 해요. 돗자리와 간단한 간식은 제가 준비할게요. 함께 산책하고 이야기 나누실 분은 댓글 남겨주세요!",
  2: "사용감이 조금 있지만 튼튼한 이케아 원목 협탁입니다. 직접 가져가실 수 있는 분께 무료로 나눔할게요.",
  3: "노트북으로 오래 작업해도 괜찮은 조용한 카페를 찾고 있어요.",
  4: "이번 달 영화는 퍼펙트 데이즈예요. 상영 후 라운지에서 짧게 감상도 나눠요.",
  5: "소설과 에세이 다섯 권을 나눔합니다.",
  6: "매주 목요일 저녁 8시에 한강을 따라 천천히 달려요.",
  7: "어제 저녁 공용 주방 창가에서 초록색 텀블러를 발견해 보관 중입니다.",
  8: "건강하게 자란 몬스테라 화분입니다."
}
export const COMMUNITY_POST_DETAILS = Object.fromEntries(SUMMARIES.map((post) => [
  post.postId,
  {
    postId: post.postId,
    category: post.category,
    title: post.title,
    content: BODIES[post.postId],
    authorId: post.authorId ?? null,
    authorName: post.authorName,
    authorRoomNumber: post.authorRoomNumber ?? null,
    viewCount: post.viewCount,
    createdAt: post.createdAt,
    updatedAt: null,
    isMine: post.postId === 1 || post.postId === 5,
    blocked: false,
    comments: []
  }
])) as Record<number, CommunityPostDetail>
COMMUNITY_POST_DETAILS[100] = {
  postId: 100,
  category: "NOTICE",
  title: PINNED.title,
  content: "10월 21일 오전 10시부터 오후 1시까지 공용 라운지 정기 청소가 진행됩니다. 개인 물품은 전날까지 정리해 주세요.",
  authorId: null,
  authorName: PINNED.authorName,
  authorRoomNumber: null,
  viewCount: PINNED.viewCount,
  createdAt: PINNED.createdAt,
  updatedAt: null,
  isMine: false,
  blocked: false,
  comments: []
}
COMMUNITY_POST_DETAILS[1].comments = [
  {
    commentId: 11,
    authorId: 5002,
    authorName: "박준영",
    authorRoomNumber: "201호",
    content: "저 갈래요! 음료 몇 개 챙겨갈게요.",
    status: "ACTIVE",
    blocked: false,
    isMine: false,
    createdAt: "2025-10-18T10:30:00",
    replies: [
      {
        commentId: 12,
        authorId: 5001,
        authorName: "김민서",
        authorRoomNumber: "302호",
        content: "좋아요 :) 토요일에 만나요!",
        status: "ACTIVE",
        blocked: false,
        isMine: true,
        createdAt: "2025-10-18T10:40:00"
      },
      {
        commentId: 13,
        authorId: 5003,
        authorName: "정하은",
        authorRoomNumber: "304호",
        content: "저도 함께할게요.",
        status: "ACTIVE",
        blocked: false,
        isMine: false,
        createdAt: "2025-10-18T10:46:00"
      }
    ]
  },
  {
    commentId: 14,
    authorId: 5001,
    authorName: "김민서",
    authorRoomNumber: "302호",
    content: "비가 오면 라운지에서 보드게임 모임으로 바꿀게요.",
    status: "ACTIVE",
    blocked: false,
    isMine: true,
    createdAt: "2025-10-18T11:02:00",
    replies: [
      {
        commentId: 15,
        authorId: 5002,
        authorName: "박준영",
        authorRoomNumber: "201호",
        content: "확인했습니다!",
        status: "ACTIVE",
        blocked: false,
        isMine: false,
        createdAt: "2025-10-18T11:08:00"
      }
    ]
  },
  {
    commentId: 16,
    authorId: null,
    authorName: null,
    authorRoomNumber: null,
    content: null,
    status: "DELETED",
    blocked: false,
    isMine: false,
    createdAt: "2025-10-18T11:20:00",
    replies: []
  },
  {
    commentId: 17,
    authorId: 5003,
    authorName: "정하은",
    authorRoomNumber: "304호",
    content: "돗자리 하나 더 가져갈게요!",
    status: "ACTIVE",
    blocked: false,
    isMine: false,
    createdAt: "2025-10-18T11:32:00",
    replies: []
  },
]
export const BLOCK_LIST: BlockListResponse = {
  blocks: [
    {
      blockedUserId: 5003,
      name: "정하은",
      roomNumber: "304호",
      profileImageUrl: null,
      blockedAt: "2026-10-20T20:00:00"
    },
    {
      blockedUserId: 5004,
      name: "윤가영",
      roomNumber: null,
      profileImageUrl: null,
      blockedAt: "2026-10-22T18:30:00"
    }
  ]
}
export const INACTIVE_MY_SANCTION: MySanction = {
  active: false,
  sanctionId: null,
  sanctionType: null,
  durationDays: null,
  reason: null,
  startsAt: null,
  endsAt: null
}

export const FAQ_ITEMS: FaqItem[] = [
  {
    id: 1,
    category: "비용",
    question: "보증금·월 이용료",
    answer: "보증금과 월 이용료는 지점 및 객실 유형에 따라 달라집니다. 상세 페이지에서 정확한 금액을 확인해 주세요.",
    branch: "전체 공통",
    enabled: true
  },
  {
    id: 2,
    category: "입주",
    question: "입주 가능일",
    answer: "객실별 입주 가능일은 실시간으로 달라질 수 있으며, 계약 완료 후 확정됩니다.",
    branch: "전체 공통",
    enabled: true
  },
  {
    id: 3,
    category: "계약",
    question: "최소 계약 기간",
    answer: "최소 계약 기간은 3개월이며 지점별 프로모션에 따라 달라질 수 있습니다.",
    branch: "전체 공통",
    enabled: true
  },
  {
    id: 4,
    category: "시설",
    question: "공용 시설",
    answer: "라운지, 공용 주방, 세탁실 등 지점별로 다양한 공용 시설을 이용할 수 있습니다.",
    branch: "전체 공통",
    enabled: true
  },
  {
    id: 5,
    category: "계약",
    question: "계약 절차",
    answer: "상담 신청, 객실 확인, 전자 계약서 작성, 보증금 납부 순서로 진행됩니다.",
    branch: "전체 공통",
    enabled: true
  },
]
export const USER_NOTES: UserNoteListResponse = {
  unreadCount: 1,
  content: [
    {
      noteId: 76,
      senderType: "USER",
      senderName: "김민서",
      content: "11월 초 입주 가능한 방이 있을까요?",
      createdAt: "2025-10-18T13:10:00",
      readAt: "2025-10-18T13:11:00"
    },
    {
      noteId: 77,
      senderType: "ADMIN",
      senderName: "망원 스테이 매니저",
      content: "11월 3일 입주 가능합니다. 원하시면 계약 절차도 안내해 드릴게요.",
      createdAt: "2025-10-18T13:11:00",
      readAt: null
    }
  ],
  page: 0,
  size: 30,
  totalElements: 2,
  totalPages: 1
}
export const CHAT_ROOMS: ChatRoom[] = [
  {
    roomId: "r_501",
    partner: {
      userId: 5002,
      name: "박준영",
      profileImageUrl: null,
      roomNumber: "201호"
    },
    lastMessage: "내일 라운지에서 봬요!",
    lastMessageAt: "2025-10-18T21:03:00",
    unreadCount: 2,
    readOnly: false
  },
  {
    roomId: "r_502",
    partner: {
      userId: 5003,
      name: "정하은",
      profileImageUrl: null,
      roomNumber: "304호"
    },
    lastMessage: "책 잘 읽을게요. 고마워요 :) ",
    lastMessageAt: "2025-10-17T19:42:00",
    unreadCount: 0,
    readOnly: false
  },
  {
    roomId: "r_503",
    partner: {
      userId: 5004,
      name: "윤가영",
      profileImageUrl: null,
      roomNumber: "401호 · 퇴거"
    },
    lastMessage: "그동안 즐거웠어요!",
    lastMessageAt: "2025-10-16T18:20:00",
    unreadCount: 1,
    readOnly: true
  },
]
export const CHAT_MESSAGES: Record<string, ChatMessageListResponse> = {
  r_501: {
    messages: [
      {
        messageId: "m_8999",
        senderId: 5001,
        content: "내일 저녁에 라운지 모임 오세요?",
        sentAt: "2025-10-18T20:58:00"
      },
      {
        messageId: "m_9001",
        senderId: 5002,
        content: "네! 내일 라운지에서 봬요!",
        sentAt: "2025-10-18T21:03:00"
      }
    ],
    hasMore: false
  },
  r_502: {
    messages: [
      {
        messageId: "m_9010",
        senderId: 5001,
        content: "책 문 앞에 두었어요.",
        sentAt: "2025-10-17T19:38:00"
      },
      {
        messageId: "m_9011",
        senderId: 5003,
        content: "책 잘 읽을게요. 고마워요 :) ",
        sentAt: "2025-10-17T19:42:00"
      }
    ],
    hasMore: false
  },
  r_503: {
    messages: [
      {
        messageId: "m_9020",
        senderId: 5004,
        content: "오늘 퇴거했어요. 그동안 즐거웠어요!",
        sentAt: "2025-10-16T18:20:00"
      }
    ],
    hasMore: false
  },
}
export const USER_NOTIFICATIONS: NotificationListResponse = {
  content: [
    {
      notificationId: 301,
      type: "NOTE",
      title: "새 쪽지",
      message: "망원 스테이 매니저가 쪽지를 보냈어요",
      targetType: "NOTE",
      targetId: "77",
      createdAt: "2025-10-18T13:11:00",
      read: false
    },
    {
      notificationId: 302,
      type: "CHAT",
      title: "새 메시지",
      message: "박준영님: 내일 라운지에서 봬요!",
      targetType: "CHAT_ROOM",
      targetId: "r_501",
      createdAt: "2025-10-18T21:03:00",
      read: false
    },
    {
      notificationId: 303,
      type: "COMMENT",
      title: "새 댓글",
      message: "한지호님이 회원님의 게시글에 댓글을 남겼어요",
      targetType: "POST",
      targetId: "1",
      createdAt: "2025-10-18T20:20:00",
      read: true
    }
  ],
  page: 0,
  size: 20,
  totalElements: 3,
  totalPages: 1
}
export const UNREAD_NOTIFICATION_SUMMARY: UnreadNotificationSummary = {
  note: 1,
  chat: 3,
  total: 4
}
