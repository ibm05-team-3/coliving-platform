# community-service API 명세서

> 담당: 오시문 · 서비스: community-service (쪽지 · 비회원 문의 · 1:1 채팅 · 커뮤니티 게시판 · 알림 · FAQ 봇 · 신고·차단·제재)
> 기준: DB `community-service_schema.sql` · 상태: 최종본 v1.0 (신고·차단·제재, Gemini RAG 반영)
> ※ 입주자 관리 · 입주-퇴거 달력 API는 조휘재 팀장님 담당 (화면만 오시문)

---

## 0. 공통

### 0-1. 기본 규칙

| 항목 | 내용 |
|---|---|
| URL 규칙 | 일반 사용자 `/community-service/{리소스}/**` · 관리자 `/community-service/admin/{리소스}/**` · 서비스 간 `/community-service/internal/{리소스}/**` |
| 버전 | URL에 버전 표시(`v1` 등) 없음 |
| Gateway | `/community-service/**` → community-service 로 라우팅 (`/internal/**`은 외부 노출 안 함) |
| 인증 | `Authorization: Bearer {JWT}` → Gateway 검증 후 `X-User: {user_id}` 헤더로 전달 |
| 권한 표기 | 비회원 / 회원(후보자 포함) / 입주자(거주 중) / 관리자 |
| Content-Type | `application/json; charset=UTF-8` (파일 업로드만 `multipart/form-data`) |
| 날짜 형식 | 날짜 `YYYY-MM-DD` · 일시 `YYYY-MM-DDTHH:mm:ss` (예: `2025-10-18T13:11:00`) — 한국 시간, 시차 표기 없음 (팀 규칙: DB `TIMESTAMP` + Java `LocalDateTime`) |
| 건물 범위 | 게시글·채팅은 요청자의 **입주 건물** 기준 (서버가 X-User로 계약 서비스 조회, 요청에 건물 ID 받지 않음) |

### 0-2. 페이지 요청 · 응답

| Query | 타입 | 필수 | 설명 |
|---|---|---|---|
| page | Integer | X | 0부터 시작, 기본 0 |
| size | Integer | X | 기본 20 |

```json
{ "content": [], "page": 0, "size": 20, "totalElements": 42, "totalPages": 3 }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| content | Array | 목록 데이터 |
| page | Integer | 현재 페이지 |
| size | Integer | 페이지 크기 |
| totalElements | Long | 전체 건수 |
| totalPages | Integer | 전체 페이지 수 |

### 0-3. 에러 응답 (ProblemDetail, RFC 9457)

Spring `ProblemDetail` 형식 · `Content-Type: application/problem+json`

```java
throw new ErrorResponseException(HttpStatus.NOT_FOUND,
    ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."), null);
```

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "게시글을 찾을 수 없습니다.",
  "instance": "/community-service/posts/999",
  "code": "POST_NOT_FOUND"
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| type | String | 오류 유형 URI (기본 `about:blank`) |
| title | String | HTTP 상태 이름 |
| status | Integer | HTTP 상태 코드 |
| detail | String | 사용자에게 보여줄 오류 설명 (한국어) |
| instance | String | 요청 경로 |
| code | String | (선택) 화면 분기용 오류 코드 — `problemDetail.setProperty("code", "POST_NOT_FOUND")` |

| HTTP | code 예시 | 상황 |
|---|---|---|
| 400 | INVALID_REQUEST, REPLY_DEPTH_EXCEEDED, GUEST_REPLY_NOT_ALLOWED, INVALID_DURATION | 필수값 누락, 대댓글의 대댓글, 비회원 문의 답장, 이용 제한 기간이 1·3·7·30일이 아님 |
| 401 | UNAUTHORIZED | 토큰 없음·만료 |
| 403 | NOT_RESIDENT, NOT_OWNER, OTHER_BUILDING | 입주자 아님, 본인 글 아님, 다른 건물 |
| 403 | SANCTIONED | 이용 제한·정지 중 글·댓글·채팅 작성 (0-6 참고) |
| 403 | CHAT_NOT_AVAILABLE | 차단 관계 등으로 채팅 불가 (차단 사실은 알리지 않음) |
| 403 | CANNOT_REPORT_SELF, CANNOT_BLOCK_SELF | 본인 신고·차단 |
| 404 | POST_NOT_FOUND, COMMENT_NOT_FOUND, THREAD_NOT_FOUND, ROOM_NOT_FOUND, DOCUMENT_NOT_FOUND, MESSAGE_NOT_FOUND, USER_NOT_FOUND, REPORT_NOT_FOUND, SANCTION_NOT_FOUND, BLOCK_NOT_FOUND, NOTIFICATION_NOT_FOUND | 대상 없음 |
| 409 | REINDEX_IN_PROGRESS | 반영 작업 진행 중 |
| 413 | FILE_TOO_LARGE | FAQ 문서 30MB 초과 |
| 409 | ALREADY_REPORTED, ALREADY_BLOCKED, ALREADY_HANDLED, SANCTION_ALREADY_REVOKED | 같은 대상 중복 신고, 이미 차단, 이미 처리된 신고, 이미 해제된 제재 |
| 429 | TOO_MANY_REQUESTS | 비회원 문의·FAQ 질문 횟수 초과 |

### 0-4. Enum

| Enum | 값 |
|---|---|
| SenderType | `USER` (사용자), `ADMIN` (관리자) |
| ThreadType | `CANDIDATE` (후보자), `RESIDENT` (입주자), `MEMBER` (회원), `GUEST` (비회원 문의) |
| ThreadStatus | `UNANSWERED` (미답변), `ANSWERED` (답변 완료) |
| PostCategory | `NOTICE` (건물 공지·관리자만), `FREE` (자유), `SHARE` (나눔), `MEETUP` (모임) |
| ContentStatus | `ACTIVE` (정상), `HIDDEN` (관리자 숨김), `DELETED` (삭제) |
| ChatSourceType | `POST` (게시글에서 시작), `PROFILE` (프로필에서 시작) |
| NotificationType | `NOTE` (쪽지), `CHAT` (채팅), `COMMENT` (댓글), `REPLY` (대댓글), `SANCTION` (경고·이용 제한·해제), `REPORT` (관리자: 새 신고) |
| NotificationTarget | `NOTE_THREAD`, `CHAT_ROOM`, `POST`, `SANCTION` (내 이용 제한 안내), `REPORT` (관리자 신고 상세) |
| FaqScope | `COMMON` (공통), `BUILDING` (건물) |
| FaqIndexStatus | `PENDING` (대기), `PROCESSING` (처리 중), `COMPLETED` (완료), `FAILED` (실패) |
| ReportSourceType | `CHAT_ROOM` (채팅방), `POST` (게시글) — 신고가 발생한 위치 |
| ReportTargetType | `POST` (게시글), `COMMENT` (댓글), `CHAT_MESSAGE` (채팅 메시지), `USER` (사용자) |
| ReportReason | `ABUSE` (욕설·비방), `HARASSMENT` (괴롭힘·스토킹), `PRIVACY` (개인정보 노출), `SPAM` (광고·도배), `INAPPROPRIATE` (음란·부적절), `HOUSE_RULE` (생활 규칙 위반), `ETC` (기타) |
| ReportStatus | `RECEIVED` (접수), `RESOLVED` (처리 완료), `REJECTED` (반려) |
| ReportAction | `REJECT` (반려), `HIDE_CONTENT` (숨김), `DELETE_CONTENT` (삭제), `WARNING` (경고), `RESTRICTION` (이용 제한), `SUSPENSION` (커뮤니티 정지), `OFFLINE_RECORD` (매니저 면담·계약 검토 기록) |
| SanctionType | `WARNING` (경고), `RESTRICTION` (기간 이용 제한 1·3·7·30일), `SUSPENSION` (해제 전까지 정지) |

### 0-5. 엔드포인트 목록

| # | Method | URL | 권한 | 설명 |
|---|---|---|---|---|
| 1-1 | GET | `/community-service/notes` | 회원 | 내 쪽지 목록 |
| 1-2 | POST | `/community-service/notes` | 회원 | 관리자에게 쪽지 보내기 |
| 1-3 | PATCH | `/community-service/notes/read` | 회원 | 받은 쪽지 읽음 처리 |
| 2-1 | POST | `/community-service/guest-inquiries` | 비회원 | 비회원 문의 접수 |
| 3-1 | GET | `/community-service/admin/note-threads` | 관리자 | 쪽지함 목록 |
| 3-2 | GET | `/community-service/admin/note-threads/{threadId}` | 관리자 | 쪽지함 상세 |
| 3-3 | POST | `/community-service/admin/note-threads/{threadId}/notes` | 관리자 | 답장 보내기 |
| 3-4 | PATCH | `/community-service/admin/note-threads/{threadId}` | 관리자 | 처리 상태·메모 변경 |
| 3-5 | POST | `/community-service/admin/notes` | 관리자 | 새 쪽지 쓰기 |
| 4-1 | GET | `/community-service/chat/rooms` | 입주자 | 채팅방 목록 |
| 4-2 | POST | `/community-service/chat/rooms` | 입주자 | 채팅방 열기(없으면 생성) |
| 4-3 | GET | `/community-service/chat/rooms/{roomId}/messages` | 입주자 | 메시지 목록 |
| 4-4 | PATCH | `/community-service/chat/rooms/{roomId}/read` | 입주자 | 읽음 처리 |
| 4-5 | WS | `/community-service/ws` (STOMP) | 회원 | 실시간 채팅·알림 |
| 5-1 | GET | `/community-service/posts` | 입주자 | 게시글 목록 |
| 5-2 | GET | `/community-service/posts/{postId}` | 입주자 | 게시글 상세 (조회수 증가) |
| 5-3 | POST | `/community-service/posts` | 입주자·관리자 | 게시글 작성 |
| 5-4 | PUT | `/community-service/posts/{postId}` | 작성자 | 게시글 수정 |
| 5-5 | DELETE | `/community-service/posts/{postId}` | 작성자 | 게시글 삭제 |
| 5-6 | POST | `/community-service/posts/{postId}/comments` | 입주자 | 댓글·답글 작성 |
| 5-7 | PUT | `/community-service/comments/{commentId}` | 작성자 | 댓글 수정 |
| 5-8 | DELETE | `/community-service/comments/{commentId}` | 작성자 | 댓글 삭제 |
| 5-9 | GET | `/community-service/me/summary` | 입주자 | 내 커뮤니티 정보·인기글 |
| 6-1 | GET | `/community-service/notifications` | 회원·관리자 | 알림 목록 |
| 6-2 | GET | `/community-service/notifications/unread-summary` | 회원·관리자 | 안 읽은 수 |
| 6-3 | PATCH | `/community-service/notifications/{notificationId}/read` | 회원·관리자 | 알림 읽음 |
| 6-4 | PATCH | `/community-service/notifications/read-all` | 회원·관리자 | 모두 읽음 |
| 7-1 | POST | `/community-service/faq-bot/ask` | 전체(비회원 포함) | FAQ 봇 질문 |
| 7-2 | GET | `/community-service/admin/faq-documents` | 관리자 | 문서 목록·반영 상태 |
| 7-3 | POST | `/community-service/admin/faq-documents` | 관리자 | 문서 업로드 |
| 7-4 | DELETE | `/community-service/admin/faq-documents/{documentId}` | 관리자 | 문서 삭제 |
| 7-5 | GET | `/community-service/admin/faq-documents/{documentId}/file` | 관리자 | 원본 파일 다운로드 |
| 7-6 | POST | `/community-service/admin/faq-documents/reindex` | 관리자 | 업데이트 반영 시작 |
| 7-7 | GET | `/community-service/admin/faq-documents/reindex/{jobId}` | 관리자 | 반영 진행 상태 |
| 7-8 | GET | `/community-service/admin/faq-bot/logs` | 관리자 | 질문 기록 (답 못 한 질문) |
| 8-1 | POST | `/community-service/reports` | 입주자 | 신고하기 |
| 8-2 | GET | `/community-service/blocks` | 입주자 | 차단 목록 |
| 8-3 | POST | `/community-service/blocks` | 입주자 | 차단하기 |
| 8-4 | DELETE | `/community-service/blocks/{blockedUserId}` | 입주자 | 차단 해제 |
| 8-5 | GET | `/community-service/sanctions/me` | 입주자 | 내 이용 제한 상태 |
| 9-1 | GET | `/community-service/admin/reports` | 관리자 | 신고 목록 (대상별 묶음) |
| 9-2 | GET | `/community-service/admin/reports/{reportId}` | 관리자 | 신고 상세 |
| 9-3 | POST | `/community-service/admin/reports/{reportId}/actions` | 관리자 | 신고 처리 (조치) |
| 9-4 | GET | `/community-service/admin/sanctions` | 관리자 | 제재 목록 |
| 9-5 | PATCH | `/community-service/admin/sanctions/{sanctionId}/revoke` | 관리자 | 제재 해제 |

### 0-6. 제재 · 차단 공통 처리

| 구분 | 적용 API | 처리 |
|---|---|---|
| 이용 제한·정지 중 (`RESTRICTION` 기간 내, `SUSPENSION` 해제 전) | 5-3 게시글 작성, 5-4 수정, 5-6 댓글 작성, 5-7 수정, 4-2 채팅방 생성, 4-5 메시지 전송, 8-1 신고 | **403 SANCTIONED** (아래 예시). 읽기·쪽지(관리자 문의)·FAQ 봇·차단 관리는 계속 가능 |
| 경고 (`WARNING`) | — | 기능 제한 없음, 알림만 발송 |
| 차단 (내가 상대를 차단) | 5-1 목록, 5-2 상세, 4-1 채팅방 목록 | 상대 게시글 목록에서 제외, 상대 댓글은 `blocked = true`·내용 null (접힘 표시), 상대와의 채팅방 숨김 |
| 차단 (상대가 나를 차단) | 4-2 채팅방 생성, 4-5 메시지 전송 | **403 CHAT_NOT_AVAILABLE** — 일반 문구만, 차단 사실은 알리지 않음 |
| 공통 | 관리자 공지(NOTICE)·쪽지 | 차단·제재와 관계없이 항상 노출 |
| 제재 상태 확인 | 서버 내부 | Redis `sanction:active:{userId}` 캐시 → 없으면 `user_sanctions` 조회 |

```json
{
  "type": "about:blank",
  "title": "Forbidden",
  "status": 403,
  "detail": "커뮤니티 이용이 제한된 상태입니다. (2026-11-03 10:00 해제)",
  "instance": "/community-service/posts",
  "code": "SANCTIONED",
  "sanctionType": "RESTRICTION",
  "endsAt": "2026-11-03T10:00:00"
}
```

| 추가 필드 | 타입 | 설명 |
|---|---|---|
| sanctionType | SanctionType | `RESTRICTION` / `SUSPENSION` |
| endsAt | DateTime \| null | 해제 일시 (정지는 null) — `problemDetail.setProperty("endsAt", ...)` |

---

## 1. 쪽지 — 사용자

### 1-1. 내 쪽지 목록 조회

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/notes` |
| 권한 | 회원 (후보자·입주자 포함) |
| 설명 | 관리자와 주고받은 쪽지를 시간순으로 조회 (위젯 쪽지 탭) |

**Request** — Query: `page`, `size` (0-2 참고, size 기본 30)

**Response 200**
```json
{
  "unreadCount": 1,
  "content": [
    { "noteId": 77, "senderType": "ADMIN", "senderName": "망원 스테이 매니저", "content": "11월 3일 입주 가능합니다.", "createdAt": "2025-10-18T13:11:00", "readAt": null },
    { "noteId": 76, "senderType": "USER", "senderName": "김예린", "content": "11월 초 입주 가능한 방이 있을까요?", "createdAt": "2025-10-18T13:10:00", "readAt": "2025-10-18T13:11:00" }
  ],
  "page": 0, "size": 30, "totalElements": 2, "totalPages": 1
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| unreadCount | Integer | 안 읽은 받은 쪽지 수 |
| content[].noteId | Long | 쪽지 ID |
| content[].senderType | SenderType | 보낸 쪽 |
| content[].senderName | String | 표시 이름 (관리자는 "건물명 매니저") |
| content[].content | String | 쪽지 내용 |
| content[].createdAt | DateTime | 보낸 일시 |
| content[].readAt | DateTime \| null | 상대가 읽은 일시 (null이면 안 읽음) |

### 1-2. 쪽지 보내기 (관리자에게)

| 항목 | 내용 |
|---|---|
| Method / URL | `POST /community-service/notes` |
| 권한 | 회원 |
| 설명 | 쪽지함이 없으면 생성(유형은 역할 기준) 후 저장, 관리자에게 알림 |

**Request**
```json
{ "content": "계약 기간 관련 문의드립니다." }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| content | String | O | 쪽지 내용 (1~1,000자) |

**Response 201**
```json
{ "noteId": 78, "createdAt": "2025-10-18T13:20:00" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| noteId | Long | 생성된 쪽지 ID |
| createdAt | DateTime | 보낸 일시 |

### 1-3. 받은 쪽지 읽음 처리

| 항목 | 내용 |
|---|---|
| Method / URL | `PATCH /community-service/notes/read` |
| 권한 | 회원 |
| 설명 | 받은 쪽지 전체 읽음, Redis 안 읽은 수 0 |

**Request** — Body 없음
**Response 204** — Body 없음

---

## 2. 비회원 문의

### 2-1. 비회원 문의 접수

| 항목 | 내용 |
|---|---|
| Method / URL | `POST /community-service/guest-inquiries` |
| 권한 | 비회원 (인증 불필요) |
| 설명 | 연락처 암호화 저장 + GUEST 쪽지함 생성 → 관리자 쪽지함에 표시. 같은 연락처 10분 내 재제출 시 429 |

**Request**
```json
{ "name": "홍길동", "phone": "010-1234-5678", "email": "gildong@email.com", "content": "반려동물 동반 입주 가능한가요?", "privacyAgreed": true }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| name | String | O | 이름 (최대 50자) |
| phone | String | O | 휴대전화 (`010-0000-0000` 형식) |
| email | String | O | 이메일 |
| content | String | O | 문의 내용 (최대 1,000자) |
| privacyAgreed | Boolean | O | 개인정보 수집·이용 동의 (false면 400) |

**Response 201**
```json
{ "inquiryId": 15, "createdAt": "2025-10-18T14:00:00" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| inquiryId | Long | 문의 ID |
| createdAt | DateTime | 접수 일시 |

---

## 3. 쪽지함 — 관리자

### 3-1. 쪽지함 목록

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/admin/note-threads` |
| 권한 | 관리자 |

**Request** — Query

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| type | ThreadType | X | 유형 탭 (없으면 전체) |
| status | ThreadStatus | X | 처리 상태 (없으면 전체) |
| keyword | String | X | 이름·내용 검색 |
| page, size | Integer | X | 0-2 참고 |

**Response 200**
```json
{
  "content": [
    { "threadId": 12, "type": "CANDIDATE", "userId": 6001, "name": "김예린", "lastContent": "토요일 오후 2시 가능합니다!", "lastAt": "2025-10-18T10:24:00", "unreadCount": 2, "status": "UNANSWERED" },
    { "threadId": 15, "type": "GUEST", "userId": null, "name": "홍길동", "lastContent": "반려동물 동반 입주 가능한가요?", "lastAt": "2025-10-18T14:00:00", "unreadCount": 1, "status": "UNANSWERED" }
  ],
  "page": 0, "size": 20, "totalElements": 2, "totalPages": 1
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| content[].threadId | Long | 쪽지함 ID |
| content[].type | ThreadType | 유형 |
| content[].userId | Long \| null | 회원 ID (비회원은 null) |
| content[].name | String | 이름 (회원: 유저 서비스 / 비회원: 문의 이름) |
| content[].lastContent | String | 마지막 쪽지 내용 |
| content[].lastAt | DateTime | 마지막 쪽지 일시 |
| content[].unreadCount | Integer | 관리자가 안 읽은 쪽지 수 |
| content[].status | ThreadStatus | 처리 상태 |

### 3-2. 쪽지함 상세

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/admin/note-threads/{threadId}` |
| 권한 | 관리자 |
| 설명 | 조회 시 사용자가 보낸 쪽지 읽음 처리 |

**Request** — Path: `threadId` (Long, 필수)

**Response 200 (비회원 문의 예)**
```json
{
  "threadId": 15, "type": "GUEST", "status": "UNANSWERED", "memo": null,
  "user": null,
  "guest": { "name": "홍길동", "phone": "010-1234-5678", "email": "gildong@email.com", "privacyAgreedAt": "2025-10-18T14:00:00" },
  "notes": [
    { "noteId": 90, "senderType": "USER", "content": "반려동물 동반 입주 가능한가요?", "createdAt": "2025-10-18T14:00:00", "readAt": "2025-10-18T14:05:00" }
  ]
}
```
회원이면 `guest: null`, `user: { "userId": 6001, "name": "김예린", "buildingName": "망원 스테이", "roomNumber": "302호" }`

| 필드 | 타입 | 설명 |
|---|---|---|
| threadId | Long | 쪽지함 ID |
| type | ThreadType | 유형 |
| status | ThreadStatus | 처리 상태 |
| memo | String \| null | 처리 메모 |
| user | Object \| null | 회원 정보 (비회원이면 null) |
| user.userId | Long | 회원 ID |
| user.name | String | 이름 (유저 서비스) |
| user.buildingName | String \| null | 건물명 (계약 서비스, 후보자는 희망 건물) |
| user.roomNumber | String \| null | 호실 |
| guest | Object \| null | 비회원 연락처 (회원이면 null) |
| guest.name / phone / email | String | 이름 / 전화 / 이메일 (복호화) |
| guest.privacyAgreedAt | DateTime | 개인정보 동의 일시 |
| notes[] | Array | 쪽지 목록 (1-1 content 항목과 같은 구조, senderName 제외) |

### 3-3. 답장 보내기

| 항목 | 내용 |
|---|---|
| Method / URL | `POST /community-service/admin/note-threads/{threadId}/notes` |
| 권한 | 관리자 |
| 설명 | 저장 후 처리 상태 `ANSWERED`, 사용자에게 알림. **비회원 문의는 400 (GUEST_REPLY_NOT_ALLOWED)** |

**Request**
```json
{ "content": "네, 11월 3일 입주 가능합니다." }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| content | String | O | 답장 내용 (1~1,000자) |

**Response 201**
```json
{ "noteId": 79, "createdAt": "2025-10-18T14:10:00", "threadStatus": "ANSWERED" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| noteId | Long | 쪽지 ID |
| createdAt | DateTime | 보낸 일시 |
| threadStatus | ThreadStatus | 변경된 처리 상태 |

### 3-4. 처리 상태 · 메모 변경

| 항목 | 내용 |
|---|---|
| Method / URL | `PATCH /community-service/admin/note-threads/{threadId}` |
| 권한 | 관리자 |

**Request** (보낸 필드만 변경)
```json
{ "status": "ANSWERED", "memo": "10/18 전화 안내 완료" }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| status | ThreadStatus | X | 처리 상태 |
| memo | String | X | 처리 메모 (최대 1,000자) |

**Response 200**
```json
{ "threadId": 15, "status": "ANSWERED", "memo": "10/18 전화 안내 완료", "updatedAt": "2025-10-18T15:00:00" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| threadId | Long | 쪽지함 ID |
| status | ThreadStatus | 처리 상태 |
| memo | String \| null | 처리 메모 |
| updatedAt | DateTime | 수정 일시 |

### 3-5. 새 쪽지 쓰기 (관리자가 먼저)

| 항목 | 내용 |
|---|---|
| Method / URL | `POST /community-service/admin/notes` |
| 권한 | 관리자 |
| 설명 | 받는 사람 검색은 유저 서비스 회원 검색 API 사용 → 선택한 userId로 전송. 쪽지함이 없으면 생성 |

**Request**
```json
{ "recipientUserId": 5001, "content": "다음 주 정기 점검 일정 안내드립니다." }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| recipientUserId | Long | O | 받는 회원 ID |
| content | String | O | 쪽지 내용 (1~1,000자) |

**Response 201**
```json
{ "threadId": 21, "noteId": 95, "createdAt": "2025-10-18T16:00:00" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| threadId | Long | 쪽지함 ID (기존 또는 새로 생성) |
| noteId | Long | 쪽지 ID |
| createdAt | DateTime | 보낸 일시 |

---

## 4. 1:1 채팅 (입주자 전용)

### 4-1. 채팅방 목록

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/chat/rooms` |
| 권한 | 입주자 (퇴거자는 기존 방 읽기만) |

**Request** — 없음

**Response 200**
```json
[
  { "roomId": 501, "partner": { "userId": 5002, "name": "박준영", "profileImageUrl": null, "roomNumber": "201호" }, "lastMessage": "내일 라운지에서 봬요!", "lastMessageAt": "2025-10-18T21:03:00", "unreadCount": 2, "readOnly": false }
]
```

| 필드 | 타입 | 설명 |
|---|---|---|
| [].roomId | Long | 채팅방 ID |
| [].partner.userId | Long | 상대 ID |
| [].partner.name | String | 상대 이름 (유저 서비스) |
| [].partner.profileImageUrl | String \| null | 프로필 이미지 |
| [].partner.roomNumber | String | 상대 호실 (계약 서비스) |
| [].lastMessage | String \| null | 마지막 메시지 미리보기 |
| [].lastMessageAt | DateTime \| null | 마지막 메시지 일시 (정렬 기준) |
| [].unreadCount | Integer | 안 읽은 메시지 수 |
| [].readOnly | Boolean | 읽기 전용 (상대·본인 퇴거 시 true) |

### 4-2. 채팅방 열기 (없으면 생성)

| 항목 | 내용 |
|---|---|
| Method / URL | `POST /community-service/chat/rooms` |
| 권한 | 입주자 |
| 설명 | 같은 상대와의 방이 있으면 기존 방 반환. 다른 건물·퇴거자·본인과는 403. 상대가 나를 차단했으면 403 CHAT_NOT_AVAILABLE, 제재 중이면 403 SANCTIONED |

**Request**
```json
{ "partnerUserId": 5002, "sourceType": "POST", "sourcePostId": 2 }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| partnerUserId | Long | O | 상대 회원 ID |
| sourceType | ChatSourceType | O | 시작 위치 |
| sourcePostId | Long | △ | `sourceType = POST`일 때 필수 |

**Response 200** (기존 방) / **201** (새 방)
```json
{ "roomId": 501, "created": false }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| roomId | Long | 채팅방 ID |
| created | Boolean | 새로 생성됐는지 |

### 4-3. 메시지 목록 (위로 스크롤 시 이전 메시지)

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/chat/rooms/{roomId}/messages` |
| 권한 | 해당 방 참여자 |

**Request** — Path: `roomId` (Long) / Query

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| before | String | X | 이 메시지 ID보다 이전 메시지 (없으면 최신부터) |
| size | Integer | X | 기본 30 |

**Response 200**
```json
{
  "messages": [
    { "messageId": "6712f0a1c3e5b2a1f0000001", "senderId": 5002, "content": "내일 라운지에서 봬요!", "status": "ACTIVE", "sentAt": "2025-10-18T21:03:00" }
  ],
  "hasMore": true
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| messages[].messageId | String | 메시지 ID (MongoDB ObjectId) |
| messages[].senderId | Long | 보낸 사람 ID |
| messages[].content | String \| null | 내용 (숨김·삭제면 null) |
| messages[].status | ContentStatus | 상태 |
| messages[].sentAt | DateTime | 보낸 일시 |
| hasMore | Boolean | 이전 메시지가 더 있는지 |

### 4-4. 읽음 처리

| 항목 | 내용 |
|---|---|
| Method / URL | `PATCH /community-service/chat/rooms/{roomId}/read` |
| 권한 | 해당 방 참여자 |

**Request**
```json
{ "lastReadMessageId": "6712f0a1c3e5b2a1f0000001" }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| lastReadMessageId | String | O | 마지막으로 읽은 메시지 ID |

**Response 204** — Body 없음

### 4-5. 실시간 (WebSocket + STOMP)

| 구분 | 경로 | 설명 |
|---|---|---|
| 연결 | `/community-service/ws` | CONNECT 헤더 `Authorization: Bearer {JWT}` |
| 메시지 전송 | SEND `/app/chat/{roomId}` | Body `{ "content": "안녕하세요" }` (content: String, 필수, 1~1,000자) |
| 메시지 수신 | SUBSCRIBE `/topic/chat/{roomId}` | 4-3 `messages[]` 항목과 같은 구조 |
| 알림 수신 | SUBSCRIBE `/user/queue/notifications` | 6-1 알림 항목과 같은 구조 (쪽지·채팅·댓글) |
| 오류 수신 | SUBSCRIBE `/user/queue/errors` | `{ "code": "CHAT_READ_ONLY", "message": "..." }` — code: `CHAT_READ_ONLY`, `SANCTIONED`, `CHAT_NOT_AVAILABLE` |

---

## 5. 커뮤니티 게시판 (입주자 전용, 건물별)

### 5-1. 게시글 목록

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/posts` |
| 권한 | 입주자 (아니면 403 NOT_RESIDENT) |
| 설명 | 내 입주 건물 게시글만, 내가 차단한 사용자의 글 제외 (공지는 항상 노출) |

**Request** — Query

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| category | PostCategory | X | 카테고리 (없으면 전체, NOTICE 제외) |
| keyword | String | X | 제목·내용 검색 |
| page, size | Integer | X | 0-2 참고 |

**Response 200**
```json
{
  "buildingId": 1,
  "buildingName": "망원 스테이",
  "pinned": [
    { "postId": 100, "category": "NOTICE", "title": "10월 공용 라운지 정기 청소 안내", "authorName": "망원 스테이 매니저", "viewCount": 54, "commentCount": 3, "createdAt": "2025-10-18T09:00:00" }
  ],
  "content": [
    { "postId": 1, "category": "FREE", "title": "이번 주말 한강 피크닉 같이 가실 분?", "authorId": 5001, "authorName": "김민서", "authorRoomNumber": "302호", "viewCount": 31, "commentCount": 7, "createdAt": "2025-10-18T10:00:00", "isNew": true }
  ],
  "page": 0, "size": 20, "totalElements": 42, "totalPages": 3
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| buildingId / buildingName | Long / String | 내 입주 건물 |
| pinned[] | Array | 상단 고정 공지 (postId, category, title, authorName, viewCount, commentCount, createdAt) |
| content[].postId | Long | 게시글 ID |
| content[].category | PostCategory | 카테고리 |
| content[].title | String | 제목 |
| content[].authorId | Long | 작성자 ID |
| content[].authorName | String | 작성자 이름 (유저 서비스 일괄 조회) |
| content[].authorRoomNumber | String | 작성자 호실 (계약 서비스 일괄 조회) |
| content[].viewCount | Integer | 조회수 |
| content[].commentCount | Integer | 댓글 수 |
| content[].createdAt | DateTime | 작성 일시 |
| content[].isNew | Boolean | 24시간 이내 작성 |

### 5-2. 게시글 상세 (조회수 증가)

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/posts/{postId}` |
| 권한 | 같은 건물 입주자 |
| 설명 | Redis `post:view:{postId}:{userId}`(24시간) 키가 없을 때만 조회수 +1 |

**Request** — Path: `postId` (Long, 필수)

**Response 200**
```json
{
  "postId": 1, "category": "FREE", "title": "이번 주말 한강 피크닉 같이 가실 분?",
  "content": "토요일 오후 3시에 망원 한강공원에서...",
  "authorId": 5001, "authorName": "김민서", "authorRoomNumber": "302호",
  "viewCount": 32, "createdAt": "2025-10-18T10:00:00", "updatedAt": null,
  "isMine": true,
  "comments": [
    {
      "commentId": 11, "authorId": 5002, "authorName": "박준영", "authorRoomNumber": "201호",
      "content": "저 갈래요!", "status": "ACTIVE", "blocked": false, "isMine": false, "createdAt": "2025-10-18T10:30:00",
      "replies": [
        { "commentId": 12, "authorId": 5001, "authorName": "김민서", "authorRoomNumber": "302호", "content": "좋아요 :)", "status": "ACTIVE", "blocked": false, "isMine": true, "createdAt": "2025-10-18T10:40:00" }
      ]
    },
    { "commentId": 16, "authorId": null, "authorName": null, "authorRoomNumber": null, "content": null, "status": "DELETED", "blocked": false, "isMine": false, "createdAt": "2025-10-18T11:20:00", "replies": [] }
  ]
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| postId | Long | 게시글 ID |
| category | PostCategory | 카테고리 |
| title / content | String | 제목 / 본문 |
| authorId / authorName / authorRoomNumber | Long / String / String | 작성자 |
| viewCount | Integer | 조회수 (증가 반영 후) |
| createdAt / updatedAt | DateTime / DateTime \| null | 작성 / 수정 일시 |
| isMine | Boolean | 내 글 여부 (수정·삭제 버튼) |
| comments[] | Array | 원댓글 목록 (작성순) |
| comments[].commentId | Long | 댓글 ID |
| comments[].authorId / authorName / authorRoomNumber | Long / String / String \| null | 작성자 (삭제된 댓글은 null) |
| comments[].content | String \| null | 내용 (DELETED면 null → "삭제된 댓글입니다") |
| comments[].status | ContentStatus | 상태 |
| comments[].blocked | Boolean | 내가 차단한 사용자의 댓글 (true면 content·작성자 null → "차단한 사용자의 댓글입니다") |
| comments[].isMine | Boolean | 내 댓글 여부 |
| comments[].createdAt | DateTime | 작성 일시 |
| comments[].replies[] | Array | 대댓글 (같은 구조, replies 없음) |

### 5-3. 게시글 작성

| 항목 | 내용 |
|---|---|
| Method / URL | `POST /community-service/posts` |
| 권한 | 입주자 · 관리자 (`NOTICE`는 관리자만) |
| 설명 | 건물 ID·작성자 ID는 서버가 토큰 기준으로 설정. 제재 중이면 403 SANCTIONED |

**Request**
```json
{ "category": "FREE", "title": "이번 주 금요일 라운지 보드게임 하실 분", "content": "저녁 8시에 라운지에서..." }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| category | PostCategory | O | 카테고리 (입주자는 NOTICE 불가 → 403) |
| title | String | O | 제목 (1~100자) |
| content | String | O | 본문 (1~5,000자) |
| buildingId | Long | △ | **관리자 공지 작성 시에만 필수** (입주자는 보내도 무시하고 입주 건물로 저장) |

> 관리자는 `NOTICE`만 작성 가능, 공지는 자동으로 상단 고정(`pinned`)

**Response 201**
```json
{ "postId": 43, "createdAt": "2025-10-19T09:00:00" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| postId | Long | 게시글 ID (상세 화면으로 이동) |
| createdAt | DateTime | 작성 일시 |

### 5-4. 게시글 수정

| 항목 | 내용 |
|---|---|
| Method / URL | `PUT /community-service/posts/{postId}` |
| 권한 | 작성자 본인 (아니면 403 NOT_OWNER) |

**Request** — Path: `postId` / Body는 5-3과 같음

**Response 200**
```json
{ "postId": 43, "updatedAt": "2025-10-19T09:30:00" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| postId | Long | 게시글 ID |
| updatedAt | DateTime | 수정 일시 |

### 5-5. 게시글 삭제

| 항목 | 내용 |
|---|---|
| Method / URL | `DELETE /community-service/posts/{postId}` |
| 권한 | 작성자 본인 |
| 설명 | 실제 삭제하지 않고 `status = DELETED`, `deleted_at` 기록 |

**Request** — Path: `postId`
**Response 204** — Body 없음

### 5-6. 댓글 · 답글 작성

| 항목 | 내용 |
|---|---|
| Method / URL | `POST /community-service/posts/{postId}/comments` |
| 권한 | 같은 건물 입주자 |
| 설명 | 게시글 댓글 수 +1, 글 작성자(답글이면 원댓글 작성자)에게 알림(상대가 나를 차단했으면 알림 생략). 대댓글의 대댓글은 400 REPLY_DEPTH_EXCEEDED, 제재 중이면 403 SANCTIONED |

**Request**
```json
{ "content": "저도 갈게요!", "parentCommentId": 11 }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| content | String | O | 내용 (1~1,000자) |
| parentCommentId | Long | X | 원댓글 ID (답글일 때만) |

**Response 201**
```json
{ "commentId": 18, "parentCommentId": 11, "createdAt": "2025-10-18T12:00:00" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| commentId | Long | 댓글 ID |
| parentCommentId | Long \| null | 원댓글 ID |
| createdAt | DateTime | 작성 일시 |

### 5-7. 댓글 수정

| 항목 | 내용 |
|---|---|
| Method / URL | `PUT /community-service/comments/{commentId}` |
| 권한 | 작성자 본인 |

**Request**
```json
{ "content": "수정한 댓글 내용" }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| content | String | O | 내용 (1~1,000자) |

**Response 200**
```json
{ "commentId": 18, "updatedAt": "2025-10-18T12:10:00" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| commentId | Long | 댓글 ID |
| updatedAt | DateTime | 수정 일시 |

### 5-8. 댓글 삭제

| 항목 | 내용 |
|---|---|
| Method / URL | `DELETE /community-service/comments/{commentId}` |
| 권한 | 작성자 본인 |
| 설명 | `status = DELETED` (달린 대댓글은 유지), 게시글 댓글 수 -1 |

**Request** — Path: `commentId`
**Response 204** — Body 없음

### 5-9. 내 커뮤니티 정보 · 인기글

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/me/summary` |
| 권한 | 입주자 |
| 설명 | 목록 화면 오른쪽 패널 |

**Request** — 없음

**Response 200**
```json
{
  "name": "김민서", "buildingName": "망원 스테이", "roomNumber": "302호",
  "postCount": 2, "receivedCommentCount": 18,
  "sanction": null,
  "popularPosts": [ { "postId": 4, "title": "이번 달 영화 모임은 ‘퍼펙트 데이즈’입니다", "viewCount": 104, "commentCount": 21 } ]
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| name | String | 내 이름 (유저 서비스) |
| buildingName / roomNumber | String | 내 건물 / 호실 (계약 서비스) |
| postCount | Integer | 작성한 글 수 |
| receivedCommentCount | Integer | 받은 댓글 수 |
| popularPosts[] | Array | 이번 주 조회수 상위 3개 (postId, title, viewCount, commentCount) |
| sanction | Object \| null | 진행 중인 이용 제한·정지 (8-5와 같은 구조, 없으면 null) → 글쓰기 버튼 비활성·안내 배너 |

---

## 6. 알림

### 6-1. 알림 목록

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/notifications` |
| 권한 | 회원 · 관리자 |
| 설명 | 헤더 종 아이콘 드롭다운. 30일 보관 |

**Request** — Query

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| unreadOnly | Boolean | X | 안 읽은 알림만 (기본 false) |
| page, size | Integer | X | 0-2 참고 |

**Response 200**
```json
{
  "content": [
    { "notificationId": 301, "type": "NOTE", "title": "새 쪽지", "message": "망원 스테이 매니저가 쪽지를 보냈어요", "targetType": "NOTE_THREAD", "targetId": "12", "read": false, "createdAt": "2025-10-18T13:11:00" },
    { "notificationId": 302, "type": "CHAT", "title": "새 메시지", "message": "박준영님: 내일 라운지에서 봬요!", "targetType": "CHAT_ROOM", "targetId": "501", "read": false, "createdAt": "2025-10-18T21:03:00" }
  ],
  "page": 0, "size": 20, "totalElements": 2, "totalPages": 1
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| content[].notificationId | Long | 알림 ID |
| content[].type | NotificationType | 알림 종류 (아이콘) |
| content[].title | String | 제목 |
| content[].message | String | 내용 |
| content[].targetType | NotificationTarget | 이동 대상 종류 |
| content[].targetId | String | 이동 대상 ID (클릭 시 위젯 쪽지 탭·채팅방·게시글 열기) |
| content[].read | Boolean | 읽음 여부 |
| content[].createdAt | DateTime | 생성 일시 |

### 6-2. 안 읽은 수

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/notifications/unread-summary` |
| 권한 | 회원 · 관리자 |
| 설명 | 위젯 버튼 빨간 점, 탭 배지, 관리자 사이드바 배지. 사용자 `note` = 안 읽은 받은 쪽지 수 / 관리자 `note` = 미답변 쪽지함 수 |

**Request** — 없음

**Response 200**
```json
{ "note": 1, "chat": 2, "notification": 3, "total": 3 }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| note | Integer | 안 읽은 쪽지 수 |
| chat | Integer | 안 읽은 채팅 수 (입주자만, 나머지 0) |
| notification | Integer | 안 읽은 알림 수 (헤더 종) |
| total | Integer | 위젯 빨간 점 기준 (note + chat) |

### 6-3. 알림 읽음 · 6-4. 모두 읽음

| 항목 | 내용 |
|---|---|
| Method / URL | `PATCH /community-service/notifications/{notificationId}/read` · `PATCH /community-service/notifications/read-all` |
| 권한 | 회원 · 관리자 (본인 알림만) |

**Request** — Path: `notificationId` (6-3만) / Body 없음
**Response 204** — Body 없음

---

## 7. FAQ 봇 (RAG)

> 구성 (예정): Spring AI 2.0 + PostgreSQL pgvector · 임베딩 `gemini-embedding-2`(768차원) · 답변 `gemini-3.5-flash-lite`
> 검색 결과가 유사도 기준에 못 미치면 **LLM을 호출하지 않고** fallback 응답 · 근거 문서 제목은 검색된 문서의 메타데이터로 응답

### 7-1. FAQ 봇 질문

| 항목 | 내용 |
|---|---|
| Method / URL | `POST /community-service/faq-bot/ask` |
| 권한 | 전체 (비회원 포함) · 관리자 미리보기도 같은 API |
| 설명 | 공통 문서 + 해당 건물 문서에서 검색 → 답변. 비회원 분당 5회, 회원 분당 10회 초과 시 429 (제재와 관계없이 사용 가능) |

**Request**
```json
{ "question": "최소 계약 기간이 어떻게 되나요?", "buildingId": 1 }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| question | String | O | 질문 (1~300자) |
| buildingId | Long | X | 건물 ID (건물 상세 페이지에서 질문 / 관리자 테스트 건물). 입주자는 생략 시 입주 건물 |

**Response 200 (답변 성공)**
```json
{
  "answered": true,
  "answer": "망원 스테이는 최소 3개월부터 계약할 수 있어요. 6개월 이상 계약 시 월 이용료가 할인돼요.",
  "sources": [ { "documentId": 3, "title": "망원 스테이 이용 규정" } ],
  "actions": [],
  "cached": false
}
```

**Response 200 (답을 못 찾음)**
```json
{
  "answered": false,
  "answer": "해당 내용은 규정에서 찾지 못했어요. 매니저에게 문의해 주세요.",
  "sources": [],
  "actions": ["INQUIRY"],
  "cached": false
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| answered | Boolean | 문서 근거로 답했는지 |
| answer | String | 답변 (한국어) |
| sources[].documentId | Long | 근거 문서 ID |
| sources[].title | String | 근거 문서 제목 ("출처:" 표시) |
| actions[] | Array\<String\> | 다음 행동 버튼: `INQUIRY`(비회원 문의 남기기) / `NOTE`(회원 쪽지로 문의) |
| cached | Boolean | Redis 캐시 답변 여부 (같은 질문·건물 10분 캐시, 화면 표시는 안 함) |

> 처리 순서: 횟수 제한 확인 → 캐시 조회 → 벡터 검색(topK 5, 유사도 0.6 이상, `scope = COMMON` 또는 해당 건물) → 결과 없으면 fallback(LLM 미호출) → Gemini 답변 생성 → 캐시 저장 → `faq_question_logs` 기록
> 일일 호출 상한(Redis `faq:daily-calls:{yyyyMMdd}`) 초과 시에도 fallback 응답 · 화면에 "개인정보를 입력하지 마세요" 안내

### 7-2. 문서 목록 · 반영 상태

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/admin/faq-documents` |
| 권한 | 관리자 |

**Request** — Query

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| scope | FaqScope | X | 적용 범위 필터 |
| buildingId | Long | X | 건물 필터 |

**Response 200**
```json
{
  "summary": { "lastIndexedAt": "2026-10-24T18:30:00", "indexedCount": 4, "pendingCount": 1 },
  "documents": [
    { "documentId": 3, "title": "망원 스테이 이용 규정", "scope": "BUILDING", "buildingId": 1, "buildingName": "망원 스테이", "fileName": "mangwon-guide.pdf", "fileType": "PDF", "fileSize": 245760, "uploadedAt": "2026-10-22T10:00:00", "indexStatus": "COMPLETED", "indexedAt": "2026-10-24T18:30:00", "indexError": null }
  ]
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| summary.lastIndexedAt | DateTime \| null | 마지막 반영 일시 |
| summary.indexedCount | Integer | 반영 완료 문서 수 |
| summary.pendingCount | Integer | 반영 안 된 변경 수 (대기·실패) |
| documents[].documentId | Long | 문서 ID |
| documents[].title | String | 문서 제목 |
| documents[].scope | FaqScope | 적용 범위 |
| documents[].buildingId / buildingName | Long \| null / String \| null | 건물 (공통이면 null) |
| documents[].fileName / fileType / fileSize | String / String / Long | 원본 파일명 / 형식 / 크기(byte) |
| documents[].uploadedAt | DateTime | 업로드 일시 |
| documents[].indexStatus | FaqIndexStatus | 반영 상태 |
| documents[].indexedAt | DateTime \| null | 반영 일시 |
| documents[].indexError | String \| null | 실패 사유 (`FAILED`일 때, 예: "텍스트를 추출할 수 없는 파일입니다" — 스캔 PDF) |

### 7-3. 문서 업로드

| 항목 | 내용 |
|---|---|
| Method / URL | `POST /community-service/admin/faq-documents` |
| 권한 | 관리자 |
| Content-Type | `multipart/form-data` |
| 설명 | 원본 파일을 DB에 저장 후 `PENDING` (업데이트 반영 전까지 봇에 미적용). Spring 설정 `spring.servlet.multipart.max-file-size=30MB`, `max-request-size=31MB` · Gateway·Ingress 업로드 크기 제한도 30MB 이상으로 |

**Request** — Form

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| file | File | O | PDF · DOCX · TXT · MD, 최대 30MB (초과 시 413 FILE_TOO_LARGE) |
| title | String | O | 문서 제목 (최대 100자) |
| scope | FaqScope | O | 적용 범위 |
| buildingId | Long | △ | `scope = BUILDING`일 때 필수 |

**Response 201**
```json
{ "documentId": 6, "indexStatus": "PENDING", "uploadedAt": "2026-10-26T10:00:00" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| documentId | Long | 문서 ID |
| indexStatus | FaqIndexStatus | 반영 상태 (`PENDING`) |
| uploadedAt | DateTime | 업로드 일시 |

### 7-4. 문서 삭제

| 항목 | 내용 |
|---|---|
| Method / URL | `DELETE /community-service/admin/faq-documents/{documentId}` |
| 권한 | 관리자 |
| 설명 | 문서(원본 파일 포함)와 문서 조각(임베딩) 함께 삭제 |

**Request** — Path: `documentId`
**Response 204** — Body 없음

### 7-5. 원본 파일 다운로드

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/admin/faq-documents/{documentId}/file` |
| 권한 | 관리자 |

**Request** — Path: `documentId`
**Response 200** — 파일 바이너리 (`Content-Disposition: attachment; filename="mangwon-guide.pdf"`)

### 7-6. 업데이트 반영 시작

| 항목 | 내용 |
|---|---|
| Method / URL | `POST /community-service/admin/faq-documents/reindex` |
| 권한 | 관리자 |
| 설명 | 대기·실패 문서를 비동기로 분할 → 임베딩 → 저장. 이미 진행 중이면 409 |

**Request** (생략 시 대기·실패 문서 전체)
```json
{ "documentIds": [6] }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| documentIds | Array\<Long\> | X | 반영할 문서 ID 목록 |

**Response 202**
```json
{ "jobId": 10, "status": "PROCESSING", "startedAt": "2026-10-26T10:05:00" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| jobId | Long | 반영 작업 ID |
| status | FaqIndexStatus | 작업 상태 |
| startedAt | DateTime | 시작 일시 |

### 7-7. 반영 진행 상태

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/admin/faq-documents/reindex/{jobId}` |
| 권한 | 관리자 |
| 설명 | 화면에서 2~3초 간격 조회 → 완료 시 목록 새로고침 |

**Request** — Path: `jobId`

**Response 200**
```json
{ "jobId": 10, "status": "COMPLETED", "processedCount": 1, "totalCount": 1, "errorMessage": null, "startedAt": "2026-10-26T10:05:00", "finishedAt": "2026-10-26T10:05:12" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| jobId | Long | 작업 ID |
| status | FaqIndexStatus | 작업 상태 |
| processedCount / totalCount | Integer | 처리한 문서 수 / 전체 문서 수 |
| errorMessage | String \| null | 실패 사유 |
| startedAt / finishedAt | DateTime / DateTime \| null | 시작 / 종료 일시 |

### 7-8. 질문 기록 (답 못 한 질문)

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/admin/faq-bot/logs` |
| 권한 | 관리자 |
| 설명 | 봇이 답하지 못한 질문을 보고 FAQ 문서를 보완. `faq_question_logs` 30일 보관 |

**Request** — Query

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| answered | Boolean | X | `false`면 답 못 한 질문만 (기본 전체) |
| buildingId | Long | X | 건물 필터 |
| from / to | Date | X | 기간 (기본 최근 7일) |
| page, size | Integer | X | 0-2 참고 |

**Response 200**
```json
{
  "summary": { "totalCount": 128, "unansweredCount": 9, "cachedCount": 41, "avgLatencyMs": 1240 },
  "content": [
    { "logId": 901, "question": "택배는 어디서 받나요?", "buildingId": 1, "buildingName": "망원 스테이", "answered": false, "topScore": 0.42, "sources": [], "cached": false, "latencyMs": 380, "createdAt": "2026-10-26T14:02:00" }
  ],
  "page": 0, "size": 20, "totalElements": 9, "totalPages": 1
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| summary.totalCount | Integer | 기간 내 전체 질문 수 |
| summary.unansweredCount | Integer | 답 못 한 질문 수 |
| summary.cachedCount | Integer | 캐시로 답한 수 |
| summary.avgLatencyMs | Integer | 평균 응답 시간(ms) |
| content[].logId | Long | 기록 ID |
| content[].question | String | 질문 |
| content[].buildingId / buildingName | Long \| null / String \| null | 질문한 건물 |
| content[].answered | Boolean | 답변 여부 |
| content[].topScore | Float \| null | 최고 유사도 (0~1) |
| content[].sources[] | Array | 근거 문서 (documentId, title) |
| content[].cached | Boolean | 캐시 답변 여부 |
| content[].latencyMs | Integer | 응답 시간(ms) |
| content[].createdAt | DateTime | 질문 일시 |

> 질문한 회원 ID는 저장하지만 화면에는 표시하지 않음 (개인정보 최소화)

---

## 8. 신고 · 차단 — 사용자 (입주자)

> 신고자는 익명 (상대·다른 입주자에게 공개 안 함) · 신고만으로 자동 숨김 없음 — **관리자만 처리**
> 차단은 한쪽 방향 · 상대에게 알리지 않음 · 커뮤니티 제재는 주거 계약과 별개

### 8-1. 신고하기

| 항목 | 내용 |
|---|---|
| Method / URL | `POST /community-service/reports` |
| 권한 | 같은 건물 입주자 |
| 설명 | 신고 시점 내용을 `content_snapshot`으로 저장 (작성자가 수정·삭제해도 관리자는 원본 확인). 같은 대상 중복 신고 409, 본인 신고 403 |

**Request**
```json
{ "sourceType": "POST", "sourceId": 2, "targetType": "COMMENT", "targetId": "11", "reasonType": "ABUSE", "reasonDetail": "욕설이 포함되어 있어요." }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| sourceType | ReportSourceType | O | 신고 위치 (게시글 / 채팅방) |
| sourceId | Long | O | 게시글 ID 또는 채팅방 ID |
| targetType | ReportTargetType | O | 신고 대상 종류 |
| targetId | String | O | 대상 ID (게시글·댓글 ID, 메시지 ObjectId, 사용자 ID) |
| reasonType | ReportReason | O | 신고 사유 |
| reasonDetail | String | △ | 상세 내용 (최대 500자, `ETC`면 필수) |

**Response 201**
```json
{ "reportId": 77, "status": "RECEIVED", "createdAt": "2026-10-26T15:00:00" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| reportId | Long | 신고 ID |
| status | ReportStatus | `RECEIVED` |
| createdAt | DateTime | 신고 일시 |

> 화면: 신고 완료 후 "이 사용자를 차단할까요?" 안내 → 8-3 호출 (선택)

### 8-2. 차단 목록

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/blocks` |
| 권한 | 입주자 |
| 설명 | 커뮤니티 > 차단 관리 화면 |

**Request** — 없음

**Response 200**
```json
{
  "blocks": [
    { "blockedUserId": 5003, "name": "이도윤", "roomNumber": "405호", "profileImageUrl": null, "blockedAt": "2026-10-20T20:00:00" }
  ]
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| blocks[].blockedUserId | Long | 차단한 사용자 ID |
| blocks[].name / roomNumber | String / String \| null | 이름 / 호실 (퇴거했으면 null) |
| blocks[].profileImageUrl | String \| null | 프로필 이미지 |
| blocks[].blockedAt | DateTime | 차단 일시 |

### 8-3. 차단하기

| 항목 | 내용 |
|---|---|
| Method / URL | `POST /community-service/blocks` |
| 권한 | 입주자 |
| 설명 | 게시글·댓글·채팅방 메뉴에서 호출. 이미 차단 409, 본인 403 |

**Request**
```json
{ "blockedUserId": 5003 }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| blockedUserId | Long | O | 차단할 사용자 ID |

**Response 201**
```json
{ "blockedUserId": 5003, "blockedAt": "2026-10-26T15:01:00" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| blockedUserId | Long | 차단한 사용자 ID |
| blockedAt | DateTime | 차단 일시 |

### 8-4. 차단 해제

| 항목 | 내용 |
|---|---|
| Method / URL | `DELETE /community-service/blocks/{blockedUserId}` |
| 권한 | 입주자 (본인 차단만) |
| 설명 | 해제 후 상대 글·댓글 다시 표시. 기존 채팅방은 목록에 다시 보임 |

**Request** — Path: `blockedUserId` (Long)
**Response 204** — Body 없음 (차단 내역 없으면 404 BLOCK_NOT_FOUND)

### 8-5. 내 이용 제한 상태

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/sanctions/me` |
| 권한 | 입주자 |
| 설명 | 진행 중인 제한·정지가 없으면 `active = false`. 커뮤니티 상단 안내 배너, 글쓰기·채팅 입력 비활성에 사용 |

**Request** — 없음

**Response 200**
```json
{ "active": true, "sanctionId": 31, "sanctionType": "RESTRICTION", "durationDays": 7, "reason": "게시판 욕설", "startsAt": "2026-10-27T10:00:00", "endsAt": "2026-11-03T10:00:00" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| active | Boolean | 제한·정지 진행 중 여부 |
| sanctionId | Long \| null | 제재 ID |
| sanctionType | SanctionType \| null | `RESTRICTION` / `SUSPENSION` |
| durationDays | Integer \| null | 제한 기간 (1·3·7·30, 정지는 null) |
| reason | String \| null | 사유 (사용자에게 보이는 문구) |
| startsAt / endsAt | DateTime \| null | 시작 / 해제 일시 (정지는 endsAt null) |

---

## 9. 신고 · 제재 — 관리자

> 관리자 탭 `/admin/reports` (ReportsPage) · 사이드바 배지는 9-1 `summary.receivedCount`
> 관리자는 담당 건물 신고만 조회 (건물 범위는 유저 서비스 관리자 정보 기준)

### 9-1. 신고 목록 (대상별 묶음)

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/admin/reports` |
| 권한 | 관리자 |
| 설명 | 같은 대상에 대한 여러 신고를 한 줄로 묶어서 표시 (신고 수 많은 순 정렬 가능) |

**Request** — Query

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| status | ReportStatus | X | 기본 `RECEIVED` |
| sourceType | ReportSourceType | X | 신고 위치 필터 |
| targetType | ReportTargetType | X | 대상 종류 필터 |
| buildingId | Long | X | 건물 필터 |
| sort | String | X | `latest`(기본) / `count` |
| page, size | Integer | X | 0-2 참고 |

**Response 200**
```json
{
  "summary": { "receivedCount": 4, "resolvedCount": 12, "rejectedCount": 3 },
  "content": [
    {
      "reportId": 77, "targetType": "COMMENT", "targetId": "11",
      "sourceType": "POST", "sourceId": 2, "sourceTitle": "이번 주말 한강 피크닉 같이 가실 분?",
      "targetUserId": 5003, "targetUserName": "이도윤", "targetRoomNumber": "405호",
      "buildingId": 1, "buildingName": "망원 스테이",
      "contentPreview": "○○ 진짜 ...", "reasonTypes": ["ABUSE", "HARASSMENT"],
      "reportCount": 3, "status": "RECEIVED", "latestReportedAt": "2026-10-26T15:00:00"
    }
  ],
  "page": 0, "size": 20, "totalElements": 4, "totalPages": 1
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| summary.receivedCount | Integer | 처리 대기 신고 대상 수 (사이드바 배지) |
| summary.resolvedCount / rejectedCount | Integer | 처리 완료 / 반려 수 |
| content[].reportId | Long | 대표 신고 ID (가장 최근 신고, 상세 조회용) |
| content[].targetType / targetId | ReportTargetType / String | 신고 대상 |
| content[].sourceType / sourceId | ReportSourceType / Long | 신고 위치 |
| content[].sourceTitle | String \| null | 게시글 제목 (채팅방이면 null) |
| content[].targetUserId / targetUserName / targetRoomNumber | Long / String / String | 신고된 사용자 |
| content[].buildingId / buildingName | Long / String | 건물 |
| content[].contentPreview | String | 신고 당시 내용 미리보기 (50자) |
| content[].reasonTypes | Array\<ReportReason\> | 접수된 사유 종류 |
| content[].reportCount | Integer | 이 대상의 신고 수 |
| content[].status | ReportStatus | 처리 상태 |
| content[].latestReportedAt | DateTime | 최근 신고 일시 |

### 9-2. 신고 상세

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/admin/reports/{reportId}` |
| 권한 | 관리자 |
| 설명 | 같은 대상의 신고 전체 + 신고 당시 원본 + 현재 상태 + 신고된 사용자의 과거 신고·제재 이력 |

**Request** — Path: `reportId` (Long)

**Response 200**
```json
{
  "target": {
    "targetType": "COMMENT", "targetId": "11", "sourceType": "POST", "sourceId": 2,
    "sourceTitle": "이번 주말 한강 피크닉 같이 가실 분?",
    "snapshot": { "content": "○○ 진짜 ...", "authorName": "이도윤", "writtenAt": "2026-10-26T14:50:00" },
    "currentStatus": "ACTIVE"
  },
  "targetUser": { "userId": 5003, "name": "이도윤", "roomNumber": "405호", "buildingName": "망원 스테이" },
  "reports": [
    { "reportId": 77, "reasonType": "ABUSE", "reasonDetail": "욕설이 포함되어 있어요.", "createdAt": "2026-10-26T15:00:00" },
    { "reportId": 75, "reasonType": "HARASSMENT", "reasonDetail": null, "createdAt": "2026-10-26T14:55:00" }
  ],
  "history": {
    "reportedCount": 5, "warningCount": 1,
    "sanctions": [ { "sanctionId": 20, "sanctionType": "WARNING", "durationDays": null, "reason": "도배", "startsAt": "2026-09-30T11:00:00", "endsAt": null, "revokedAt": null } ]
  },
  "status": "RECEIVED", "action": null, "adminMemo": null, "handledByName": null, "handledAt": null
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| target.targetType / targetId / sourceType / sourceId / sourceTitle | — | 9-1과 같음 |
| target.snapshot | Object | 신고 당시 저장된 내용 (content, authorName, writtenAt) |
| target.currentStatus | ContentStatus | 현재 상태 (이미 숨김·삭제됐는지) |
| targetUser | Object | 신고된 사용자 (userId, name, roomNumber, buildingName) |
| reports[] | Array | 같은 대상 신고 목록 (신고자 정보는 **미포함**, 익명) |
| reports[].reasonType / reasonDetail | ReportReason / String \| null | 사유 / 상세 |
| history.reportedCount | Integer | 이 사용자가 받은 전체 신고 수 |
| history.warningCount | Integer | 받은 경고 수 |
| history.sanctions[] | Array | 과거 제재 (9-4 항목과 같은 구조 일부) |
| status / action | ReportStatus / ReportAction \| null | 처리 상태 / 조치 |
| adminMemo | String \| null | 관리자 메모 |
| handledByName / handledAt | String \| null / DateTime \| null | 처리자 / 처리 일시 |

### 9-3. 신고 처리 (조치)

| 항목 | 내용 |
|---|---|
| Method / URL | `POST /community-service/admin/reports/{reportId}/actions` |
| 권한 | 관리자 |
| 설명 | 같은 대상의 `RECEIVED` 신고를 한 번에 처리. 조치에 따라 콘텐츠 상태 변경·`user_sanctions` 생성·알림 발송. 이미 처리됐으면 409 ALREADY_HANDLED |

**Request**
```json
{ "action": "RESTRICTION", "durationDays": 7, "hideContent": true, "reason": "게시판 욕설", "adminMemo": "3건 신고, 이전 경고 1회" }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| action | ReportAction | O | 조치 |
| durationDays | Integer | △ | `RESTRICTION`일 때 필수, 1·3·7·30 중 하나 (아니면 400 INVALID_DURATION) |
| hideContent | Boolean | X | 경고·제한·정지와 함께 대상 콘텐츠도 숨김 (기본 false) |
| reason | String | △ | 사용자에게 보낼 사유 (최대 500자) — `WARNING`·`RESTRICTION`·`SUSPENSION`이면 필수 |
| adminMemo | String | X | 내부 메모 (`OFFLINE_RECORD`면 면담·계약 검토 내용 기록) |

| action | 처리 내용 |
|---|---|
| `REJECT` | 신고 `REJECTED`, 콘텐츠 변화 없음 |
| `HIDE_CONTENT` / `DELETE_CONTENT` | 게시글·댓글·메시지 `HIDDEN` / `DELETED` (대상이 USER면 400) |
| `WARNING` | `user_sanctions`(WARNING) 생성 + 대상자에게 경고 알림 |
| `RESTRICTION` | `user_sanctions`(RESTRICTION, ends_at = 지금 + 기간) 생성 + Redis 캐시 + 알림 |
| `SUSPENSION` | `user_sanctions`(SUSPENSION, ends_at 없음) 생성 + 알림 |
| `OFFLINE_RECORD` | 기록만 남김 (매니저 면담·계약 검토는 오프라인 처리) |

**Response 200**
```json
{ "reportId": 77, "handledReportCount": 3, "status": "RESOLVED", "action": "RESTRICTION", "sanctionId": 31, "endsAt": "2026-11-03T10:00:00", "handledAt": "2026-10-27T10:00:00" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| reportId | Long | 신고 ID |
| handledReportCount | Integer | 함께 처리된 신고 수 |
| status | ReportStatus | `RESOLVED` / `REJECTED` |
| action | ReportAction | 조치 |
| sanctionId | Long \| null | 생성된 제재 ID |
| endsAt | DateTime \| null | 제한 해제 일시 |
| handledAt | DateTime | 처리 일시 |

### 9-4. 제재 목록

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /community-service/admin/sanctions` |
| 권한 | 관리자 |
| 설명 | ReportsPage의 "제재 내역" 탭 |

**Request** — Query

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| activeOnly | Boolean | X | 진행 중만 (기본 true) |
| sanctionType | SanctionType | X | 종류 필터 |
| buildingId | Long | X | 건물 필터 |
| keyword | String | X | 이름 검색 |
| page, size | Integer | X | 0-2 참고 |

**Response 200**
```json
{
  "content": [
    { "sanctionId": 31, "userId": 5003, "userName": "이도윤", "roomNumber": "405호", "buildingName": "망원 스테이", "sanctionType": "RESTRICTION", "durationDays": 7, "reason": "게시판 욕설", "reportId": 77, "startsAt": "2026-10-27T10:00:00", "endsAt": "2026-11-03T10:00:00", "active": true, "createdByName": "망원 스테이 매니저", "revokedAt": null, "revokeReason": null }
  ],
  "page": 0, "size": 20, "totalElements": 1, "totalPages": 1
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| content[].sanctionId | Long | 제재 ID |
| content[].userId / userName / roomNumber / buildingName | Long / String / String / String | 대상자 |
| content[].sanctionType | SanctionType | 종류 |
| content[].durationDays | Integer \| null | 제한 기간 |
| content[].reason | String | 사유 |
| content[].reportId | Long \| null | 연결된 신고 (직접 제재면 null) |
| content[].startsAt / endsAt | DateTime / DateTime \| null | 시작 / 해제 일시 |
| content[].active | Boolean | 진행 중 여부 (해제 안 됨 + 기간 남음) |
| content[].createdByName | String | 처리 관리자 |
| content[].revokedAt / revokeReason | DateTime \| null / String \| null | 조기 해제 일시 / 사유 |

### 9-5. 제재 해제

| 항목 | 내용 |
|---|---|
| Method / URL | `PATCH /community-service/admin/sanctions/{sanctionId}/revoke` |
| 권한 | 관리자 |
| 설명 | 기간 전 조기 해제 (잘못된 처리, 면담 후 해제 등). Redis 캐시 삭제 + 대상자 알림. 이미 해제·만료면 409 SANCTION_ALREADY_REVOKED |

**Request**
```json
{ "revokeReason": "면담 후 조기 해제" }
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| revokeReason | String | O | 해제 사유 (최대 500자) |

**Response 200**
```json
{ "sanctionId": 31, "revokedAt": "2026-10-29T11:00:00" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| sanctionId | Long | 제재 ID |
| revokedAt | DateTime | 해제 일시 |

---

> 다른 서비스(유저·계약·매물)에 요청하는 API와 Kafka 이벤트는 별도 문서 **「community-service 연동 요청 API」** 참고
