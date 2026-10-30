-- =====================================================
-- community-service 개발용 스키마 (schema-dev.sql)
-- 대상: coliving-pgvector 컨테이너 (localhost:5433, postgres / admin)
-- 1) DBeaver에서 Data Source를 postgres로 두고 아래 두 줄만 먼저 실행
CREATE DATABASE community_service ENCODING 'UTF8';
ALTER DATABASE community_service SET timezone TO 'Asia/Seoul';   -- DEFAULT NOW() 도 한국 시간으로
-- 2) Schema를 community_service로 전환한 뒤 "\c community_service;"를 제외한 아래쪽 전체 실행
\c community_service;
-- =====================================================

-- =====================================================
-- Community Service Schema (오시문)
-- 쪽지 · 비회원 문의 · 1:1 채팅 · 커뮤니티 게시판 · 알림 · FAQ 봇(RAG, Gemini) · 신고 · 차단 · 제재
-- 최종본 (2026-10-27)
-- =====================================================
-- ※ user_id, building_id, admin_id 는 다른 서비스(유저·매물·계약)의 ID만 저장 (물리 FK 없음)
-- ※ 게시글·댓글은 실제 삭제하지 않고 status 로 처리 (소프트 삭제)
-- ※ 시간 컬럼은 팀 규칙대로 TIMESTAMP(한국 시간) · created_at/updated_at 은 엔티티 BaseTimeEntity 가 자동 기록
-- ※ 제약조건 이름: fk_ / uq_ / chk_ 접두사
-- ※ 채팅 메시지는 MongoDB, 조회수 중복·안 읽은 수·요청 제한은 Redis (파일 맨 아래 참고)

-- =====================================================
-- Enum
-- =====================================================
CREATE TYPE note_thread_type   AS ENUM ('CANDIDATE', 'RESIDENT', 'MEMBER', 'GUEST');   -- 쪽지함 유형
CREATE TYPE note_thread_status AS ENUM ('UNANSWERED', 'ANSWERED');                    -- 처리 상태
CREATE TYPE sender_type        AS ENUM ('USER', 'ADMIN');                             -- 보낸 쪽 / 작성자 유형
CREATE TYPE post_category      AS ENUM ('NOTICE', 'FREE', 'SHARE', 'MEETUP');         -- 공지 / 자유 / 나눔 / 모임
CREATE TYPE content_status     AS ENUM ('ACTIVE', 'HIDDEN', 'DELETED');               -- 정상 / 관리자 숨김 / 삭제
CREATE TYPE chat_source_type   AS ENUM ('POST', 'PROFILE');                           -- 채팅 시작 위치
CREATE TYPE notification_type  AS ENUM ('NOTE', 'CHAT', 'COMMENT', 'REPLY', 'SANCTION', 'REPORT'); -- 알림 종류 (SANCTION: 경고·제한·해제 / REPORT: 관리자에게 새 신고)
CREATE TYPE notification_target AS ENUM ('NOTE_THREAD', 'CHAT_ROOM', 'POST', 'SANCTION', 'REPORT'); -- 알림 클릭 시 이동 대상
CREATE TYPE faq_scope          AS ENUM ('COMMON', 'BUILDING');                        -- 문서 적용 범위
CREATE TYPE faq_file_type      AS ENUM ('PDF', 'DOCX', 'TXT', 'MD');
CREATE TYPE faq_index_status   AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED');
CREATE TYPE report_source_type AS ENUM ('CHAT_ROOM', 'POST');                         -- 신고한 위치
CREATE TYPE report_target_type AS ENUM ('POST', 'COMMENT', 'CHAT_MESSAGE', 'USER');   -- 신고 대상
CREATE TYPE report_reason      AS ENUM ('ABUSE', 'HARASSMENT', 'PRIVACY', 'SPAM',
                                        'INAPPROPRIATE', 'HOUSE_RULE', 'ETC');        -- 욕설·비하 / 괴롭힘·스토킹 / 사생활 노출 / 스팸·홍보 / 부적절 / 생활 규칙 위반 / 기타
CREATE TYPE report_status      AS ENUM ('RECEIVED', 'RESOLVED', 'REJECTED');          -- 접수 / 처리 완료 / 반려
CREATE TYPE report_action      AS ENUM ('REJECT', 'HIDE_CONTENT', 'DELETE_CONTENT',
                                        'WARNING', 'RESTRICTION', 'SUSPENSION',
                                        'OFFLINE_RECORD');                            -- 반려 / 숨김 / 삭제 / 경고 / 이용 제한 / 이용 정지 / 매니저 면담 기록
CREATE TYPE sanction_type      AS ENUM ('WARNING', 'RESTRICTION', 'SUSPENSION');      -- 경고 / 이용 제한(기간) / 커뮤니티 이용 정지

-- =====================================================
-- 1. 비회원 문의 (Guest Inquiry)
-- =====================================================
CREATE TABLE guest_inquiries (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                VARCHAR(50)  NOT NULL,
    phone_enc           VARCHAR(255) NOT NULL,                 -- AES 암호화
    email_enc           VARCHAR(255) NOT NULL,                 -- AES 암호화
    content             TEXT         NOT NULL,
    privacy_agreed_at   TIMESTAMP    NOT NULL,                 -- 동의 없으면 저장 불가
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE guest_inquiries IS '비회원 문의 (연락처는 회원 데이터와 분리 보관)';
COMMENT ON COLUMN guest_inquiries.id IS '문의 ID';
COMMENT ON COLUMN guest_inquiries.name IS '문의자 이름';
COMMENT ON COLUMN guest_inquiries.phone_enc IS '휴대전화 (AES 암호화)';
COMMENT ON COLUMN guest_inquiries.email_enc IS '이메일 (AES 암호화)';
COMMENT ON COLUMN guest_inquiries.content IS '문의 내용';
COMMENT ON COLUMN guest_inquiries.privacy_agreed_at IS '개인정보 수집·이용 동의 일시';
COMMENT ON COLUMN guest_inquiries.created_at IS '접수 일시';

CREATE INDEX idx_guest_inquiries_created_at ON guest_inquiries (created_at DESC);

-- =====================================================
-- 2. 쪽지함 (Note Thread) — 회원 1명 또는 비회원 문의 1건당 1개
-- =====================================================
CREATE TABLE note_threads (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    thread_type         note_thread_type   NOT NULL,
    user_id             BIGINT,                                -- 회원 ID (유저 서비스)
    guest_inquiry_id    BIGINT,
    building_id         BIGINT,                                -- 관련 건물 (매물 서비스)
    status              note_thread_status NOT NULL DEFAULT 'UNANSWERED',
    memo                TEXT,
    last_note_at        TIMESTAMP,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_note_threads_guest_inquiry FOREIGN KEY (guest_inquiry_id) REFERENCES guest_inquiries (id),
    CONSTRAINT uq_note_threads_guest_inquiry UNIQUE (guest_inquiry_id),
    CONSTRAINT chk_note_threads_owner
        CHECK ((user_id IS NOT NULL) <> (guest_inquiry_id IS NOT NULL))   -- 회원 / 비회원 중 하나만
);
COMMENT ON TABLE note_threads IS '쪽지함 (관리자 ↔ 사용자)';
COMMENT ON COLUMN note_threads.id IS '쪽지함 ID';
COMMENT ON COLUMN note_threads.thread_type IS '유형 (후보자/입주자/회원/비회원)';
COMMENT ON COLUMN note_threads.user_id IS '회원 ID';
COMMENT ON COLUMN note_threads.guest_inquiry_id IS '비회원 문의 ID';
COMMENT ON COLUMN note_threads.building_id IS '관련 건물 ID';
COMMENT ON COLUMN note_threads.status IS '처리 상태 (미답변/답변 완료)';
COMMENT ON COLUMN note_threads.memo IS '관리자 처리 메모';
COMMENT ON COLUMN note_threads.last_note_at IS '마지막 쪽지 일시 (목록 정렬용)';

CREATE UNIQUE INDEX uq_note_threads_user ON note_threads (user_id) WHERE user_id IS NOT NULL;   -- 회원당 1개
CREATE INDEX idx_note_threads_list ON note_threads (thread_type, status, last_note_at DESC);

-- =====================================================
-- 3. 쪽지 (Note)
-- =====================================================
CREATE TABLE notes (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    note_thread_id      BIGINT      NOT NULL,
    sender_type         sender_type NOT NULL,
    sender_id           BIGINT,                                -- 회원 또는 관리자 ID (비회원 문의 원문은 NULL)
    content             TEXT        NOT NULL,
    read_at             TIMESTAMP,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_notes_note_thread FOREIGN KEY (note_thread_id) REFERENCES note_threads (id)
);
COMMENT ON TABLE notes IS '쪽지 (실시간 아님)';
COMMENT ON COLUMN notes.id IS '쪽지 ID';
COMMENT ON COLUMN notes.note_thread_id IS '쪽지함 ID';
COMMENT ON COLUMN notes.sender_type IS '보낸 쪽 (사용자/관리자)';
COMMENT ON COLUMN notes.sender_id IS '보낸 사람 ID';
COMMENT ON COLUMN notes.content IS '쪽지 내용';
COMMENT ON COLUMN notes.read_at IS '상대가 읽은 시각';
COMMENT ON COLUMN notes.created_at IS '보낸 시각';

CREATE INDEX idx_notes_thread_created ON notes (note_thread_id, created_at);

-- =====================================================
-- 4. 게시글 (Post) — 입주자 전용, 건물별 분리
-- =====================================================
CREATE TABLE posts (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    building_id         BIGINT         NOT NULL,
    author_id           BIGINT         NOT NULL,
    author_type         sender_type    NOT NULL DEFAULT 'USER',
    category            post_category  NOT NULL,
    title               VARCHAR(100)   NOT NULL,
    content             TEXT           NOT NULL,
    view_count          INT            NOT NULL DEFAULT 0,      -- 반정규화
    comment_count       INT            NOT NULL DEFAULT 0,      -- 반정규화
    pinned              BOOLEAN        NOT NULL DEFAULT FALSE,
    status              content_status NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP      NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP,
    CONSTRAINT chk_posts_view_count CHECK (view_count >= 0),
    CONSTRAINT chk_posts_comment_count CHECK (comment_count >= 0),
    CONSTRAINT chk_posts_notice_admin
        CHECK (category <> 'NOTICE' OR author_type = 'ADMIN')          -- 공지는 관리자만
);
COMMENT ON TABLE posts IS '커뮤니티 게시글';
COMMENT ON COLUMN posts.id IS '게시글 ID';
COMMENT ON COLUMN posts.building_id IS '건물 ID (같은 건물 입주자만 조회)';
COMMENT ON COLUMN posts.author_id IS '작성자 ID';
COMMENT ON COLUMN posts.author_type IS '작성자 유형 (입주자/관리자)';
COMMENT ON COLUMN posts.category IS '카테고리 (공지/자유/나눔/모임)';
COMMENT ON COLUMN posts.title IS '제목';
COMMENT ON COLUMN posts.content IS '내용';
COMMENT ON COLUMN posts.view_count IS '조회수 (Redis 중복 방지 후 반영)';
COMMENT ON COLUMN posts.comment_count IS '댓글 수';
COMMENT ON COLUMN posts.pinned IS '상단 고정 여부';
COMMENT ON COLUMN posts.status IS '상태 (정상/숨김/삭제)';
COMMENT ON COLUMN posts.updated_at IS '최종 수정 일시';
COMMENT ON COLUMN posts.deleted_at IS '삭제 일시';

CREATE INDEX idx_posts_list ON posts (building_id, status, pinned DESC, created_at DESC);
CREATE INDEX idx_posts_category ON posts (building_id, category, created_at DESC);
CREATE INDEX idx_posts_author ON posts (author_id);

-- =====================================================
-- 5. 댓글 · 대댓글 (Comment) — 대댓글은 1단계까지 (서비스에서 검증)
-- =====================================================
CREATE TABLE comments (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    post_id             BIGINT         NOT NULL,
    parent_comment_id   BIGINT,
    author_id           BIGINT         NOT NULL,
    content             TEXT           NOT NULL,
    status              content_status NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP      NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP,
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES comments (id)
);
COMMENT ON TABLE comments IS '댓글 · 대댓글';
COMMENT ON COLUMN comments.id IS '댓글 ID';
COMMENT ON COLUMN comments.post_id IS '게시글 ID';
COMMENT ON COLUMN comments.parent_comment_id IS '원댓글 ID (대댓글일 때)';
COMMENT ON COLUMN comments.author_id IS '작성자 ID';
COMMENT ON COLUMN comments.content IS '내용 (삭제 시 화면에 "삭제된 댓글입니다")';
COMMENT ON COLUMN comments.status IS '상태 (정상/숨김/삭제)';

CREATE INDEX idx_comments_post_created ON comments (post_id, created_at);
CREATE INDEX idx_comments_parent ON comments (parent_comment_id);

-- =====================================================
-- 6. 1:1 채팅방 (Chat Room) — 메시지는 MongoDB
-- =====================================================
CREATE TABLE chat_rooms (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    building_id             BIGINT           NOT NULL,
    pair_key                VARCHAR(50)      NOT NULL,             -- '작은ID_큰ID' → 같은 상대와 방 중복 생성 방지
    source_type             chat_source_type NOT NULL,
    source_post_id          BIGINT,
    read_only               BOOLEAN          NOT NULL DEFAULT FALSE,
    last_message_preview    VARCHAR(100),                          -- 반정규화
    last_message_at         TIMESTAMP,                           -- 반정규화
    closed_at               TIMESTAMP,                           -- 종료 일시 (누군가 나가면 종료, pair_key 뒤에 #방ID)
    created_at              TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP        NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_chat_rooms_pair_key UNIQUE (pair_key),
    CONSTRAINT fk_chat_rooms_source_post FOREIGN KEY (source_post_id) REFERENCES posts (id)
);
COMMENT ON TABLE chat_rooms IS '입주자 1:1 채팅방';
COMMENT ON COLUMN chat_rooms.id IS '채팅방 ID';
COMMENT ON COLUMN chat_rooms.building_id IS '건물 ID (같은 건물 입주자끼리만)';
COMMENT ON COLUMN chat_rooms.pair_key IS '참여자 쌍 키 (종료된 방은 뒤에 #방ID)';
COMMENT ON COLUMN chat_rooms.source_type IS '시작 위치 (게시글/프로필)';
COMMENT ON COLUMN chat_rooms.source_post_id IS '시작한 게시글 ID';
COMMENT ON COLUMN chat_rooms.read_only IS '읽기 전용 (상대 퇴거 시 TRUE)';
COMMENT ON COLUMN chat_rooms.last_message_preview IS '마지막 메시지 미리보기';
COMMENT ON COLUMN chat_rooms.last_message_at IS '마지막 메시지 일시';
COMMENT ON COLUMN chat_rooms.closed_at IS '종료 일시 (한 명이라도 나가면 종료, 다시 시작하면 새 방)';

-- =====================================================
-- 7. 채팅 참여자 (Chat Room Member)
-- =====================================================
CREATE TABLE chat_room_members (
    chat_room_id            BIGINT      NOT NULL,
    user_id                 BIGINT      NOT NULL,
    last_read_message_id    VARCHAR(30),                           -- MongoDB 메시지 ID
    left_at                 TIMESTAMP,                             -- 나간 일시 (나가면 내 목록에서 숨김)
    created_at              TIMESTAMP   NOT NULL DEFAULT NOW(),       -- 참여 일시
    updated_at              TIMESTAMP   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (chat_room_id, user_id),
    CONSTRAINT fk_chat_room_members_chat_room FOREIGN KEY (chat_room_id) REFERENCES chat_rooms (id)
);
COMMENT ON TABLE chat_room_members IS '채팅방 참여자';
COMMENT ON COLUMN chat_room_members.chat_room_id IS '채팅방 ID';
COMMENT ON COLUMN chat_room_members.user_id IS '참여자 ID';
COMMENT ON COLUMN chat_room_members.last_read_message_id IS '마지막으로 읽은 메시지 ID';
COMMENT ON COLUMN chat_room_members.created_at IS '참여 일시';
COMMENT ON COLUMN chat_room_members.left_at IS '나간 일시 (다시 들어올 수 없음)';

CREATE INDEX idx_chat_room_members_user ON chat_room_members (user_id);

-- =====================================================
-- 8. 알림 (Notification)
-- =====================================================
CREATE TABLE notifications (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    receiver_id         BIGINT              NOT NULL,
    receiver_type       sender_type         NOT NULL,
    type                notification_type   NOT NULL,
    title               VARCHAR(50)         NOT NULL,
    message             VARCHAR(200)        NOT NULL,
    target_type         notification_target NOT NULL,
    target_id           VARCHAR(50)         NOT NULL,
    is_read             BOOLEAN             NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP           NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE notifications IS '알림 (30일 보관)';
COMMENT ON COLUMN notifications.id IS '알림 ID';
COMMENT ON COLUMN notifications.receiver_id IS '받는 사람 ID';
COMMENT ON COLUMN notifications.receiver_type IS '받는 사람 유형 (사용자/관리자)';
COMMENT ON COLUMN notifications.type IS '알림 종류 (쪽지/채팅/댓글/대댓글)';
COMMENT ON COLUMN notifications.title IS '제목';
COMMENT ON COLUMN notifications.message IS '내용';
COMMENT ON COLUMN notifications.target_type IS '이동 대상 종류';
COMMENT ON COLUMN notifications.target_id IS '이동 대상 ID (클릭 시 위젯 탭·채팅방 열기)';
COMMENT ON COLUMN notifications.is_read IS '읽음 여부';

CREATE INDEX idx_notifications_receiver ON notifications (receiver_type, receiver_id, is_read, created_at DESC);

-- =====================================================
-- 9. FAQ 봇 문서 (FAQ Document) — 원본 파일은 DB(BYTEA) 저장, 최대 30MB
-- =====================================================
CREATE TABLE faq_documents (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title               VARCHAR(100)     NOT NULL,
    scope               faq_scope        NOT NULL,
    building_id         BIGINT,
    file_name           VARCHAR(255)     NOT NULL,
    file_type           faq_file_type    NOT NULL,
    file_data           BYTEA            NOT NULL,                 -- 원본 파일 (DB 저장, 목록 조회 시 SELECT 제외)
    file_size           BIGINT           NOT NULL,
    index_status        faq_index_status NOT NULL DEFAULT 'PENDING',
    index_error         VARCHAR(500),                              -- 반영 실패 사유 (예: 텍스트 추출 불가 스캔 PDF)
    uploaded_by         BIGINT           NOT NULL,
    indexed_at          TIMESTAMP,
    created_at          TIMESTAMP        NOT NULL DEFAULT NOW(),     -- 업로드 일시
    updated_at          TIMESTAMP        NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_faq_documents_scope
        CHECK (scope = 'COMMON' OR building_id IS NOT NULL),           -- 건물 범위면 건물 ID 필수
    CONSTRAINT chk_faq_documents_file_size
        CHECK (file_size > 0 AND file_size <= 31457280)                -- 최대 30MB
);
COMMENT ON TABLE faq_documents IS 'FAQ 봇 참고 문서';
COMMENT ON COLUMN faq_documents.id IS '문서 ID';
COMMENT ON COLUMN faq_documents.title IS '문서 제목';
COMMENT ON COLUMN faq_documents.scope IS '적용 범위 (공통/건물)';
COMMENT ON COLUMN faq_documents.building_id IS '건물 ID';
COMMENT ON COLUMN faq_documents.file_name IS '원본 파일명';
COMMENT ON COLUMN faq_documents.file_type IS '파일 형식';
COMMENT ON COLUMN faq_documents.file_data IS '원본 파일 바이너리 (최대 30MB)';
COMMENT ON COLUMN faq_documents.file_size IS '파일 크기 (byte)';
COMMENT ON COLUMN faq_documents.index_error IS '반영 실패 사유';
COMMENT ON COLUMN faq_documents.index_status IS '반영 상태 (대기/처리 중/완료/실패)';
COMMENT ON COLUMN faq_documents.uploaded_by IS '업로드한 관리자 ID';
COMMENT ON COLUMN faq_documents.indexed_at IS '마지막 반영 일시';

CREATE INDEX idx_faq_documents_scope ON faq_documents (scope, building_id);

-- =====================================================
-- 10. FAQ 문서 조각 (FAQ Chunk) — Spring AI PgVectorStore 호환 구조
--     임베딩: Gemini gemini-embedding-2 (768차원) / pgvector 0.8.6 이상 (이미지 pgvector/pgvector:pg17)
-- =====================================================
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE faq_chunks (
    id                  UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    content             TEXT,                                      -- "[문서 제목 > 조항] 조각 원문"
    metadata            JSON,                                      -- {document_id, title, scope, building_id, chunk_no, embedding_model}
    embedding           VECTOR(768)                                -- gemini-embedding-2, 768차원
);
COMMENT ON TABLE faq_chunks IS 'FAQ 문서 조각 + 임베딩 (Spring AI PgVectorStore가 저장·검색)';
COMMENT ON COLUMN faq_chunks.id IS '조각 ID (UUID)';
COMMENT ON COLUMN faq_chunks.content IS '조각 원문 (답변 근거)';
COMMENT ON COLUMN faq_chunks.metadata IS '문서 ID·제목·적용 범위·건물 ID·순서·임베딩 모델 (검색 필터: scope == COMMON || building_id == ?)';
COMMENT ON COLUMN faq_chunks.embedding IS '임베딩 벡터 (768차원)';

CREATE INDEX idx_faq_chunks_embedding ON faq_chunks
    USING hnsw (embedding vector_cosine_ops) WITH (m = 16, ef_construction = 64);
-- 문서 재반영·삭제 시: vectorStore.delete("document_id == '{id}'") 로 해당 문서 조각 삭제 (faq_documents와 FK 대신 metadata로 연결)

-- =====================================================
-- 11. FAQ 반영 작업 (FAQ Index Job)
-- =====================================================
CREATE TABLE faq_index_jobs (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    status              faq_index_status NOT NULL DEFAULT 'PROCESSING',
    requested_by        BIGINT           NOT NULL,
    processed_count     INT              NOT NULL DEFAULT 0,
    error_message       TEXT,
    finished_at         TIMESTAMP,
    created_at          TIMESTAMP        NOT NULL DEFAULT NOW(),     -- 시작 일시
    updated_at          TIMESTAMP        NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE faq_index_jobs IS 'FAQ 업데이트 반영 실행 기록';
COMMENT ON COLUMN faq_index_jobs.id IS '작업 ID';
COMMENT ON COLUMN faq_index_jobs.status IS '작업 상태';
COMMENT ON COLUMN faq_index_jobs.requested_by IS '요청한 관리자 ID';
COMMENT ON COLUMN faq_index_jobs.processed_count IS '처리한 문서 수';
COMMENT ON COLUMN faq_index_jobs.error_message IS '실패 사유';

CREATE INDEX idx_faq_index_jobs_created ON faq_index_jobs (created_at DESC);

-- =====================================================
-- 11-1. FAQ 질문 기록 (FAQ Question Log) — 유사도 기준 조정 · 답 못 한 질문 확인
-- =====================================================
CREATE TABLE faq_question_logs (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    question            VARCHAR(300) NOT NULL,
    building_id         BIGINT,
    user_id             BIGINT,                                    -- 비회원은 NULL
    answered            BOOLEAN      NOT NULL,                     -- 문서 근거로 답했는지
    top_score           REAL,                                      -- 가장 높은 유사도 (0~1)
    source_doc_ids      BIGINT[],                                  -- 근거 문서 ID 목록
    cached              BOOLEAN      NOT NULL DEFAULT FALSE,       -- Redis 캐시 응답 여부
    latency_ms          INT,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE faq_question_logs IS 'FAQ 봇 질문 기록 (90일 보관)';
COMMENT ON COLUMN faq_question_logs.question IS '질문 (개인정보 입력 금지 안내)';
COMMENT ON COLUMN faq_question_logs.answered IS '답변 성공 여부 (false = 문의 버튼 안내)';
COMMENT ON COLUMN faq_question_logs.top_score IS '최고 유사도 점수';
COMMENT ON COLUMN faq_question_logs.source_doc_ids IS '근거 문서 ID';
COMMENT ON COLUMN faq_question_logs.cached IS '캐시 응답 여부';
COMMENT ON COLUMN faq_question_logs.latency_ms IS '응답 시간(ms)';

CREATE INDEX idx_faq_question_logs_created ON faq_question_logs (created_at DESC);
CREATE INDEX idx_faq_question_logs_unanswered ON faq_question_logs (created_at DESC) WHERE answered = FALSE;

-- =====================================================
-- 12. 차단 (User Block) — 한 방향 숨김 (상대에게 알리지 않음)
-- =====================================================
CREATE TABLE user_blocks (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    blocker_id          BIGINT      NOT NULL,
    blocked_id          BIGINT      NOT NULL,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_blocks_pair UNIQUE (blocker_id, blocked_id),
    CONSTRAINT chk_user_blocks_not_self CHECK (blocker_id <> blocked_id)
);
COMMENT ON TABLE user_blocks IS '사용자 차단 (차단한 사람 화면에서 상대 글 숨김·댓글 접힘, 서로 채팅 불가, 관리자 공지는 영향 없음)';
COMMENT ON COLUMN user_blocks.created_at IS '차단 일시';
COMMENT ON COLUMN user_blocks.blocker_id IS '차단한 사람 ID';
COMMENT ON COLUMN user_blocks.blocked_id IS '차단당한 사람 ID';

CREATE INDEX idx_user_blocks_blocked ON user_blocks (blocked_id);

-- =====================================================
-- 13. 신고 (Report) — 관리자만 처리 (자동 숨김 없음)
-- =====================================================
CREATE TABLE reports (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reporter_id         BIGINT             NOT NULL,
    source_type         report_source_type NOT NULL,               -- 신고한 위치 (채팅방 / 게시글)
    source_id           BIGINT             NOT NULL,               -- 채팅방 ID 또는 게시글 ID
    target_type         report_target_type NOT NULL,
    target_id           VARCHAR(50)        NOT NULL,               -- 게시글·댓글 ID 또는 MongoDB 메시지 ID, 사용자 신고는 user_id
    target_user_id      BIGINT             NOT NULL,               -- 신고당한 사람
    building_id         BIGINT             NOT NULL,
    reason_type         report_reason      NOT NULL,
    reason_detail       VARCHAR(500),
    content_snapshot    JSONB              NOT NULL,               -- 신고 시점 내용 (채팅은 앞뒤 5개 메시지 포함)
    status              report_status      NOT NULL DEFAULT 'RECEIVED',
    action              report_action,
    admin_memo          TEXT,                                      -- 처리 메모 (매니저 면담 기록 포함)
    handled_by          BIGINT,
    handled_at          TIMESTAMP,
    created_at          TIMESTAMP          NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP          NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_reports_reporter_target UNIQUE (reporter_id, target_type, target_id),   -- 같은 사람의 중복 신고 방지
    CONSTRAINT chk_reports_not_self CHECK (reporter_id <> target_user_id),
    CONSTRAINT chk_reports_handled CHECK (status = 'RECEIVED' OR (action IS NOT NULL AND handled_by IS NOT NULL))
);
COMMENT ON TABLE reports IS '신고 (신고자는 관리자에게만 공개)';
COMMENT ON COLUMN reports.id IS '신고 ID';
COMMENT ON COLUMN reports.reporter_id IS '신고한 사람 ID';
COMMENT ON COLUMN reports.source_type IS '신고 위치 (채팅방/게시글)';
COMMENT ON COLUMN reports.source_id IS '채팅방 ID 또는 게시글 ID';
COMMENT ON COLUMN reports.target_type IS '신고 대상 종류';
COMMENT ON COLUMN reports.target_id IS '신고 대상 ID';
COMMENT ON COLUMN reports.target_user_id IS '신고당한 사람 ID';
COMMENT ON COLUMN reports.building_id IS '건물 ID';
COMMENT ON COLUMN reports.reason_type IS '신고 유형';
COMMENT ON COLUMN reports.reason_detail IS '신고 내용 (신고자 작성)';
COMMENT ON COLUMN reports.content_snapshot IS '신고 시점 내용 스냅샷 (수정·삭제돼도 증거 보존)';
COMMENT ON COLUMN reports.status IS '처리 상태 (접수/처리 완료/반려)';
COMMENT ON COLUMN reports.action IS '관리자 조치';
COMMENT ON COLUMN reports.admin_memo IS '관리자 처리 메모';
COMMENT ON COLUMN reports.handled_by IS '처리한 관리자 ID';
COMMENT ON COLUMN reports.handled_at IS '처리 일시';

CREATE INDEX idx_reports_status ON reports (status, created_at DESC);
CREATE INDEX idx_reports_target ON reports (target_type, target_id);          -- 같은 대상 신고 묶기
CREATE INDEX idx_reports_target_user ON reports (target_user_id);              -- 피신고자 이력
CREATE INDEX idx_reports_reporter ON reports (reporter_id);                    -- 반복 허위 신고 확인

-- =====================================================
-- 14. 제재 (User Sanction)
-- =====================================================
CREATE TABLE user_sanctions (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id             BIGINT        NOT NULL,
    building_id         BIGINT        NOT NULL,
    sanction_type       sanction_type NOT NULL,
    duration_days       SMALLINT,                                  -- 이용 제한 기간 (1/3/7/30일)
    reason              VARCHAR(500)  NOT NULL,                    -- 사용자에게 안내할 사유
    report_id           BIGINT,                                    -- 근거 신고 (관리자 직접 제재면 NULL)
    starts_at           TIMESTAMP     NOT NULL DEFAULT NOW(),
    ends_at             TIMESTAMP,                               -- 이용 제한 종료 (경고·정지는 NULL)
    created_by          BIGINT        NOT NULL,
    revoked_at          TIMESTAMP,                               -- 해제 일시
    revoked_by          BIGINT,
    revoke_reason       VARCHAR(500),
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_sanctions_report FOREIGN KEY (report_id) REFERENCES reports (id),
    CONSTRAINT chk_user_sanctions_duration CHECK (
        (sanction_type = 'RESTRICTION' AND duration_days IN (1, 3, 7, 30) AND ends_at IS NOT NULL)
        OR (sanction_type <> 'RESTRICTION' AND duration_days IS NULL AND ends_at IS NULL)
    )
);
COMMENT ON TABLE user_sanctions IS '커뮤니티 제재 (주거 계약과 분리 — 쪽지·FAQ는 제재 중에도 이용 가능)';
COMMENT ON COLUMN user_sanctions.id IS '제재 ID';
COMMENT ON COLUMN user_sanctions.user_id IS '제재 대상 ID';
COMMENT ON COLUMN user_sanctions.building_id IS '건물 ID';
COMMENT ON COLUMN user_sanctions.sanction_type IS '제재 종류 (경고/이용 제한/이용 정지)';
COMMENT ON COLUMN user_sanctions.duration_days IS '이용 제한 기간(일)';
COMMENT ON COLUMN user_sanctions.reason IS '제재 사유';
COMMENT ON COLUMN user_sanctions.report_id IS '근거 신고 ID';
COMMENT ON COLUMN user_sanctions.starts_at IS '시작 일시';
COMMENT ON COLUMN user_sanctions.ends_at IS '종료 일시';
COMMENT ON COLUMN user_sanctions.created_by IS '제재한 관리자 ID';
COMMENT ON COLUMN user_sanctions.revoked_at IS '해제 일시';
COMMENT ON COLUMN user_sanctions.revoked_by IS '해제한 관리자 ID';
COMMENT ON COLUMN user_sanctions.revoke_reason IS '해제 사유';

CREATE INDEX idx_user_sanctions_user ON user_sanctions (user_id, created_at DESC);
CREATE INDEX idx_user_sanctions_active ON user_sanctions (user_id, ends_at)
    WHERE revoked_at IS NULL AND sanction_type IN ('RESTRICTION', 'SUSPENSION');   -- 글쓰기 시 제재 확인

-- =====================================================
-- [참고] MongoDB — chat_messages 컬렉션
-- =====================================================
-- {
--   _id:      ObjectId,
--   roomId:   Long,      -- chat_rooms.id
--   senderId: Long,
--   content:  String,
--   status:   String,    -- ACTIVE / HIDDEN / DELETED
--   sentAt:   ISODate
-- }
-- db.chat_messages.createIndex({ roomId: 1, sentAt: -1 })

-- =====================================================
-- [참고] Redis 키
-- =====================================================
-- post:view:{postId}:{userId}            String  TTL 24h   조회수 중복 방지
-- unread:note:{receiverType}:{id}        String  -         안 읽은 쪽지 수
-- unread:chat:{userId}                   Hash    -         방별 안 읽은 채팅 수 (roomId → 수)
-- ratelimit:guest:{phoneHash 또는 IP}    String  TTL 10m   비회원 문의 반복 제출 방지
-- ratelimit:faq:{userId 또는 IP}         String  TTL 1m    FAQ 봇 질문 횟수 제한 (비회원 5회/분, 회원 10회/분)
-- faq:answer:{질문해시}:{buildingId}    String  TTL 10m   FAQ 답변 캐시 (같은 질문 반복 시 Gemini 호출 생략)
-- faq:daily-calls:{yyyyMMdd}            String  TTL 1d    하루 Gemini 호출 수 (상한 도달 시 fallback)
-- sanction:active:{userId}              String  TTL=종료시각  제재 중 여부 캐시 (글쓰기·채팅 전송 시 확인)
-- chat:room:{roomId}                     Pub/Sub           Pod 간 채팅 메시지 전달
-- notify:{userId}                        Pub/Sub           Pod 간 알림 전달
