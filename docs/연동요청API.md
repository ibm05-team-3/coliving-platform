# community-service 연동 요청 API

> 작성: 오시문 · community-service가 **다른 서비스에 요청하는** API와 **수신하는** Kafka 이벤트
> 상태: v1.1 (신고·차단·제재 반영, 1-3 신규) — 각 서비스 담당자와 URL·필드명·통신 방식(REST / gRPC) 확정 필요
> ※ 서비스 간 동기 호출은 팀 스택상 gRPC가 기본이라, 각 API에 대응하는 gRPC 메서드 이름도 함께 적음 (필드는 동일)

---

## 0. 요약

| # | 담당 서비스 | 담당자 | Method / URL | gRPC (대응) | 사용처 |
|---|---|---|---|---|---|
| 1-1 | 유저 서비스 | 조휘재 | `GET /user-service/internal/users` | `UserService.GetUsers` | 게시글·댓글·채팅·쪽지함 목록, 차단 목록, 신고·제재 관리의 이름·프로필 |
| 1-2 | 유저 서비스 | 조휘재 | `GET /user-service/admin/users/search` | `UserService.SearchUsers` | 관리자 쪽지 쓰기 — 받는 사람 검색 |
| 1-3 | 유저 서비스 | 조휘재 | `GET /user-service/internal/admins/{adminId}/buildings` | `UserService.GetAdminBuildings` | **(신규)** 관리자 담당 건물 — 신고·제재·쪽지함 목록 건물 범위 |
| 2-1 | 계약 서비스 | 한성현 | `GET /contract-service/internal/residents/rooms` | `ContractService.GetResidentRooms` | 작성자·채팅 상대 호실, 입주 건물 확인(권한), 신고 대상자·차단 목록 호실 |
| 3-1 | 매물 서비스 | 김정호 | `GET /property-service/buildings/simple` | `PropertyService.GetBuildings` | FAQ 문서 적용 범위·테스트 건물 선택 |
| 4-1 | Kafka (계약 → 커뮤니티) | 한성현 | 토픽 `contract.completed` | — | 쪽지함 유형 후보자 → 입주자 |
| 4-2 | Kafka (유저 → 커뮤니티) | 조휘재 | 토픽 `resident.status-changed` | — | 퇴거 시 채팅방 읽기 전용 |
| 4-3 | Kafka (유저 → 커뮤니티) | 조휘재 | 토픽 `user.withdrawn` | — | 탈퇴 회원 작성자 표시 처리 |

**공통**
- URL 규칙: 일반 `/{서비스명}/{리소스}/**` · 관리자 `/{서비스명}/admin/{리소스}/**` · 서비스 간 `/{서비스명}/internal/{리소스}/**` · 버전 표시 없음
- `/{서비스명}/internal/**` 은 Gateway에 노출하지 않는 서비스 간 전용 경로 (Eureka 서비스 이름으로 호출)
- 오류 응답은 `ProblemDetail`(RFC 9457) 형식 권장 — 404를 받으면 해당 ID는 "알 수 없는 사용자"로 처리
- 일괄 조회는 **목록 화면의 N+1 호출을 막기 위해** ID 여러 개를 한 번에 받음 (최대 100개)
- 없는 ID는 에러 대신 결과에서 빠짐 → community-service가 "알 수 없는 사용자"로 표시

---

## 1. 유저 서비스 (조휘재 팀장님)

### 1-1. 회원 일괄 조회

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /user-service/internal/users` |
| gRPC | `rpc GetUsers(GetUsersRequest) returns (GetUsersResponse)` |
| 호출 시점 | 게시글 목록·상세, 채팅방 목록, 쪽지함 목록을 응답하기 직전 |

**Request** — Query

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| ids | Array\<Long\> | O | 회원 ID 목록 (콤마 구분, 최대 100개) 예: `ids=5001,5002,6001` |

**Response 200**
```json
[
  { "userId": 5001, "name": "김민서", "profileImageUrl": null, "role": "RESIDENT", "residentStatus": "RESIDING" },
  { "userId": 6001, "name": "김예린", "profileImageUrl": null, "role": "CANDIDATE", "residentStatus": null }
]
```

| 필드 | 타입 | 설명 |
|---|---|---|
| [].userId | Long | 회원 ID |
| [].name | String | 이름 |
| [].profileImageUrl | String \| null | 프로필 이미지 URL |
| [].role | Enum | `MEMBER` / `CANDIDATE` / `RESIDENT` / `ADMIN` — 위젯 탭·쪽지함 유형 결정 |
| [].residentStatus | Enum \| null | `MOVE_IN_SCHEDULED` / `RESIDING` / `MOVE_OUT_SCHEDULED` / `MOVED_OUT` — 커뮤니티 접근·채팅 읽기 전용 판단 (입주자가 아니면 null) |

### 1-2. 회원 이름 검색 (관리자)

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /user-service/admin/users/search` |
| gRPC | `rpc SearchUsers(SearchUsersRequest) returns (SearchUsersResponse)` |
| 호출 시점 | 관리자 쪽지함 → 쪽지 쓰기 모달에서 이름 입력 (관리자 화면에서 Gateway 경유로 직접 호출) |

**Request** — Query

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| keyword | String | O | 이름 검색어 (2자 이상) |
| role | Enum | X | 역할 필터 (`CANDIDATE` / `RESIDENT` / `MEMBER`) |
| size | Integer | X | 최대 결과 수 (기본 10) |

**Response 200**
```json
[
  { "userId": 5001, "name": "김민서", "role": "RESIDENT", "buildingName": "망원 스테이", "roomNumber": "302호" }
]
```

| 필드 | 타입 | 설명 |
|---|---|---|
| [].userId | Long | 회원 ID (쪽지 받는 사람) |
| [].name | String | 이름 |
| [].role | Enum | 역할 |
| [].buildingName | String \| null | 건물명 (동명이인 구분용, 입주자·후보자만) |
| [].roomNumber | String \| null | 호실 |

---

### 1-3. 관리자 담당 건물 조회 (신규)

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /user-service/internal/admins/{adminId}/buildings` |
| gRPC | `rpc GetAdminBuildings(GetAdminBuildingsRequest) returns (GetAdminBuildingsResponse)` |
| 호출 시점 | 관리자 신고 목록·신고 처리·제재 목록·쪽지함 목록 요청 시 (담당 건물만 조회·처리 가능하게) |

**Request** — Path: `adminId` (Long, 필수 — Gateway가 넘긴 `X-User`)

**Response 200**
```json
{ "adminId": 9001, "allBuildings": false, "buildingIds": [1, 3] }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| adminId | Long | 관리자 ID |
| allBuildings | Boolean | 전체 건물 관리자(본사)면 true → buildingIds 무시 |
| buildingIds | Array\<Long\> | 담당 건물 ID 목록 |

> 대안: 담당 건물 ID를 JWT 클레임에 넣어주면 이 API 없이 처리 가능 (5. 확인 요청 4번과 같이 결정)

---

## 2. 계약 서비스 (한성현 님)

### 2-1. 입주 건물 · 호실 일괄 조회

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /contract-service/internal/residents/rooms` |
| gRPC | `rpc GetResidentRooms(GetResidentRoomsRequest) returns (GetResidentRoomsResponse)` |
| 호출 시점 | ① 커뮤니티·채팅 요청마다 **본인 입주 건물 확인**(권한) ② 목록의 작성자·상대 호실 표시 ③ 쪽지함 상세의 회원 건물·호실 |

**Request** — Query

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| userIds | Array\<Long\> | O | 회원 ID 목록 (최대 100개) |

**Response 200**
```json
[
  { "userId": 5001, "contractId": 9001, "buildingId": 1, "buildingName": "망원 스테이", "roomNumber": "302호", "contractStatus": "ACTIVE", "moveInDate": "2025-06-12", "contractEndDate": "2026-06-11" },
  { "userId": 6001, "contractId": 9101, "buildingId": 1, "buildingName": "망원 스테이", "roomNumber": "302호", "contractStatus": "UNDER_REVIEW", "moveInDate": "2025-11-03", "contractEndDate": null }
]
```

| 필드 | 타입 | 설명 |
|---|---|---|
| [].userId | Long | 회원 ID |
| [].contractId | Long | 현재(최근) 계약 ID |
| [].buildingId | Long | 건물 ID — 게시글·채팅 건물 범위 판단 |
| [].buildingName | String | 건물명 |
| [].roomNumber | String \| null | 호실 (후보자는 희망 호실) |
| [].contractStatus | Enum | 계약 단계 (예: `UNDER_REVIEW` / `APPROVED_PAYMENT_PENDING` / `ACTIVE` / `ENDED`) — 계약 서비스 상태값 기준으로 확정 |
| [].moveInDate | Date \| null | 입주일 |
| [].contractEndDate | Date \| null | 계약 종료일 |

> 커뮤니티 권한 확인에 **매 요청 호출**되므로, 본인 건물 ID를 JWT 클레임에 넣을 수 있다면 그 방식도 검토 (회의 안건)

---

## 3. 매물 서비스 (김정호 님)

### 3-1. 건물 목록 (간단)

| 항목 | 내용 |
|---|---|
| Method / URL | `GET /property-service/buildings/simple` |
| gRPC | `rpc GetBuildings(GetBuildingsRequest) returns (GetBuildingsResponse)` |
| 호출 시점 | FAQ 봇 관리 — 문서 적용 범위·테스트 건물 선택 (입주자 관리·달력 지점 필터도 같은 API 사용) |

**Request** — 없음

**Response 200**
```json
[
  { "buildingId": 1, "buildingName": "망원 스테이" },
  { "buildingId": 2, "buildingName": "연남 하우스" }
]
```

| 필드 | 타입 | 설명 |
|---|---|---|
| [].buildingId | Long | 건물 ID |
| [].buildingName | String | 건물명 |

---

## 4. Kafka 이벤트 (community-service가 수신)

### 4-1. 계약 완료

| 항목 | 내용 |
|---|---|
| 토픽 | `contract.completed` |
| 발행 | 계약 서비스 (결제 완료로 계약 확정 시) |
| community-service 처리 | 해당 회원 쪽지함 `thread_type` → `RESIDENT`, `building_id` 갱신 |

**메시지**
```json
{ "eventId": "c7b1e2f0-1d2a-4c55-9a10-7b9e0f1a2b3c", "occurredAt": "2025-10-20T10:00:00+09:00", "userId": 6001, "contractId": 9101, "buildingId": 1, "roomNumber": "302호", "moveInDate": "2025-11-03", "contractEndDate": "2026-11-02" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| eventId | String (UUID) | 이벤트 ID (중복 처리 방지) |
| occurredAt | DateTime | 발생 일시 |
| userId | Long | 회원 ID |
| contractId | Long | 계약 ID |
| buildingId | Long | 건물 ID |
| roomNumber | String | 호실 |
| moveInDate | Date | 입주일 |
| contractEndDate | Date | 계약 종료일 |

### 4-2. 입주 상태 변경

| 항목 | 내용 |
|---|---|
| 토픽 | `resident.status-changed` |
| 발행 | 유저 서비스 (입주 예정 → 거주 중 → 퇴거 예정 → 퇴거 완료) |
| community-service 처리 | `MOVED_OUT`이면 그 회원이 참여한 채팅방 `read_only = TRUE`, 커뮤니티 접근 차단 (신고·제재 기록은 보관, 재입주 시 이력 유지) |

**메시지**
```json
{ "eventId": "0f8e...", "occurredAt": "2026-06-11T00:00:00+09:00", "userId": 5001, "buildingId": 1, "previousStatus": "MOVE_OUT_SCHEDULED", "currentStatus": "MOVED_OUT" }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| eventId | String (UUID) | 이벤트 ID |
| occurredAt | DateTime | 발생 일시 |
| userId | Long | 회원 ID |
| buildingId | Long | 건물 ID |
| previousStatus | Enum | 이전 입주 상태 |
| currentStatus | Enum | 변경된 입주 상태 |

### 4-3. 회원 탈퇴

| 항목 | 내용 |
|---|---|
| 토픽 | `user.withdrawn` |
| 발행 | 유저 서비스 |
| community-service 처리 | 게시글·댓글 작성자 표시를 "탈퇴한 회원"으로, 참여 채팅방 읽기 전용, 쪽지함 보관, 그 회원이 걸었거나 걸린 차단(`user_blocks`) 삭제, 진행 중 제재 종료 |

**메시지**
```json
{ "eventId": "9a1c...", "occurredAt": "2026-07-01T12:00:00+09:00", "userId": 5003 }
```

| 필드 | 타입 | 설명 |
|---|---|---|
| eventId | String (UUID) | 이벤트 ID |
| occurredAt | DateTime | 발생 일시 |
| userId | Long | 탈퇴한 회원 ID |

---

## 5. 확인 요청 사항

| # | 대상 | 내용 |
|---|---|---|
| 1 | 전체 | 서비스 간 동기 호출을 REST(`/{서비스명}/internal/**`)로 할지 gRPC로 할지, proto 파일 공유 위치 |
| 2 | 조휘재 | `role`·`residentStatus` 값 이름, 회원 검색 API를 프론트에서 직접 호출해도 되는지 |
| 3 | 한성현 | `contractStatus` 값 이름, 후보자(계약 전)도 희망 건물·호실을 같은 API로 줄 수 있는지 |
| 4 | 조휘재·한성현 | 입주 건물 ID·관리자 담당 건물 ID를 JWT 클레임에 넣을지 (권한 확인 호출 줄이기, 넣으면 1-3 불필요) |
| 5 | 김정호 | 건물 목록 API URL (입주자 관리·달력 지점 필터와 공용) |
| 6 | 전체 | Kafka 토픽 이름 규칙, 메시지 공통 필드(eventId, occurredAt) 통일 |
| 7 | 조휘재 | 관리자 권한 구분 — 지점 매니저(담당 건물만) / 본사 관리자(전체) 둘로 나뉘는지 |
| 8 | 조휘재 | (참고) FAQ 봇 원본 파일은 community-service DB에 저장(최대 30MB) — Gateway·Ingress 업로드 크기 제한 30MB 이상으로 설정 요청 |
