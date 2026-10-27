import { blockList, chatMessages, chatRooms, communityPostDetails, communityPosts, faqItems, inactiveMySanction, unreadNotificationSummary, userNotes, userNotifications } from "../mocks"
import type { BlockListResponse, CommunityCommentRequest, CommunityPostDetail, CommunityPostRequest, CreateBlockRequest, CreateBlockResponse, CreateReportRequest, CreateReportResponse, GuestInquiryRequest, MySanction, PostCategory } from "../types"

const copy = <T,>(value: T): T => structuredClone(value)
const currentUserId = 5001
const error = (code: string, message: string) => new Error(`${code}: ${message}`)
let blocks = copy(blockList.blocks)
let nextPostId = 9
let nextCommentId = 99
let nextReportId = 81
let nextNoteId = 78
const unreadState = copy(unreadNotificationSummary)
const reportedTargets = new Set<string>(["COMMENT:13"])

export async function getCommunityPosts(params: { category?: PostCategory; keyword?: string; page?: number; size?: number } = {}) {
  const keyword = params.keyword?.trim().toLowerCase() ?? ""
  const blockedIds = new Set(blocks.map((item) => item.blockedUserId))
  const content = communityPosts.content.filter((post) => !post.authorId || !blockedIds.has(post.authorId)).filter((post) => (!params.category || post.category === params.category) && (!keyword || post.title.toLowerCase().includes(keyword)))
  return copy({ ...communityPosts, content, page: params.page ?? 0, size: params.size ?? communityPosts.size, totalElements: content.length, totalPages: content.length ? 1 : 0 })
}

export async function getCommunityPost(postId: number): Promise<CommunityPostDetail | undefined> {
  const post = communityPostDetails[postId]
  if (!post) return undefined
  const blockedIds = new Set(blocks.map((item) => item.blockedUserId))
  const redact = <T extends CommunityPostDetail["comments"][number] | CommunityPostDetail["comments"][number]["replies"][number]>(item: T): T => item.authorId !== null && blockedIds.has(item.authorId) ? { ...item, authorId: null, authorName: null, authorRoomNumber: null, content: null, blocked: true } : { ...item, blocked: false }
  return copy({ ...post, blocked: post.authorId !== null && blockedIds.has(post.authorId), viewCount: post.viewCount + 1, comments: post.comments.map((comment) => ({ ...redact(comment), replies: comment.replies.map(redact) })) })
}

export async function createCommunityPost(request: CommunityPostRequest) { const postId = nextPostId++; communityPostDetails[postId] = { postId, ...request, authorId: currentUserId, authorName: "김민서", authorRoomNumber: "302호", viewCount: 0, createdAt: new Date().toISOString(), updatedAt: null, isMine: true, blocked: false, comments: [] }; return { postId } }
export async function updateCommunityPost(postId: number, request: CommunityPostRequest) { const post = communityPostDetails[postId]; if (!post) return undefined; Object.assign(post, request, { updatedAt: new Date().toISOString() }); return copy(post) }
export async function deleteCommunityPost(postId: number): Promise<void> { delete communityPostDetails[postId] }
export async function createCommunityComment(_postId: number, _request: CommunityCommentRequest) { return { commentId: nextCommentId++, createdAt: new Date().toISOString() } }
export async function updateCommunityComment(commentId: number, request: { content: string }): Promise<void> { Object.values(communityPostDetails).forEach((post) => post.comments.forEach((comment) => { if (comment.commentId === commentId) comment.content = request.content; comment.replies.forEach((reply) => { if (reply.commentId === commentId) reply.content = request.content }) })) }
export async function deleteCommunityComment(commentId: number): Promise<void> { Object.values(communityPostDetails).forEach((post) => post.comments.forEach((comment) => { const target = comment.commentId === commentId ? comment : comment.replies.find((reply) => reply.commentId === commentId); if (target) Object.assign(target, { authorId: null, authorName: null, authorRoomNumber: null, content: null, status: "DELETED", isMine: false }) })) }

const isOwnTarget = (request: CreateReportRequest) => {
  if (request.targetType === "USER") return Number(request.targetId) === currentUserId
  if (request.targetType === "POST") return communityPostDetails[Number(request.targetId)]?.authorId === currentUserId
  if (request.targetType === "COMMENT") return Object.values(communityPostDetails).some((post) => post.comments.some((comment) => (comment.commentId === Number(request.targetId) && comment.authorId === currentUserId) || comment.replies.some((reply) => reply.commentId === Number(request.targetId) && reply.authorId === currentUserId)))
  return false
}
export async function createReport(request: CreateReportRequest): Promise<CreateReportResponse> { const key = `${request.targetType}:${request.targetId}`; if (isOwnTarget(request)) throw error("CANNOT_REPORT_SELF", "본인은 신고할 수 없습니다."); if (reportedTargets.has(key)) throw error("ALREADY_REPORTED", "이미 신고한 대상입니다."); reportedTargets.add(key); return { reportId: nextReportId++, status: "RECEIVED", createdAt: new Date().toISOString() } }
export async function getBlocks(): Promise<BlockListResponse> { return copy({ blocks }) }
export async function createBlock(request: CreateBlockRequest): Promise<CreateBlockResponse> { if (request.blockedUserId === currentUserId) throw error("CANNOT_BLOCK_SELF", "본인은 차단할 수 없습니다."); if (blocks.some((item) => item.blockedUserId === request.blockedUserId)) throw error("ALREADY_BLOCKED", "이미 차단한 사용자입니다."); const users: Record<number, { name: string; room: string | null }> = { 5002: { name: "박준영", room: "201호" }, 5003: { name: "정하은", room: "304호" }, 5004: { name: "윤가영", room: null } }; const blockedAt = new Date().toISOString(); blocks.push({ blockedUserId: request.blockedUserId, name: users[request.blockedUserId]?.name ?? "알 수 없는 사용자", roomNumber: users[request.blockedUserId]?.room ?? null, profileImageUrl: null, blockedAt }); return { blockedUserId: request.blockedUserId, blockedAt } }
export async function deleteBlock(blockedUserId: number): Promise<void> { if (!blocks.some((item) => item.blockedUserId === blockedUserId)) throw error("BLOCK_NOT_FOUND", "차단 내역을 찾을 수 없습니다."); blocks = blocks.filter((item) => item.blockedUserId !== blockedUserId) }
export async function getMySanction(): Promise<MySanction> { return copy(inactiveMySanction) }
export async function openChatRoom(request: { partnerUserId: number; sourceType: "POST" | "PROFILE"; sourceId: number }) { if (blocks.some((item) => item.blockedUserId === request.partnerUserId) || request.partnerUserId === 5002) throw error("CHAT_NOT_AVAILABLE", "지금은 이 사용자와 채팅할 수 없습니다."); const existing = chatRooms.find((room) => room.partner.userId === request.partnerUserId); return existing ? { roomId: existing.roomId, created: false } : { roomId: `r_${request.partnerUserId}`, created: true } }

export async function getFaqItems() { return copy(faqItems) }
export async function getUserNotes() { return copy(userNotes) }
export async function createUserNote(request: { content: string }) { const createdAt = new Date().toISOString(); const noteId = nextNoteId++; userNotes.content.push({ noteId, senderType: "USER", senderName: "김민서", content: request.content, createdAt, readAt: null }); return { noteId, createdAt } }
export async function readAllUserNotes(): Promise<void> { userNotes.content.forEach((note) => { if (note.senderType === "ADMIN" && !note.readAt) note.readAt = new Date().toISOString() }); userNotes.unreadCount = 0; unreadState.note = 0; unreadState.total = unreadState.chat; userNotifications.content.forEach((item) => { if (item.targetType === "NOTE") item.read = true }) }
export async function createGuestInquiry(request: GuestInquiryRequest) { if (!request.privacyAgreed) throw new Error("개인정보 수집·이용 동의가 필요합니다."); return { inquiryId: 15, createdAt: new Date().toISOString() } }
export async function getChatRooms() { return copy(chatRooms.filter((room) => !blocks.some((block) => block.blockedUserId === room.partner.userId))) }
export async function getChatRoomMessages(roomId: string) { return copy(chatMessages[roomId] ?? { messages: [], hasMore: false }) }
export async function readChatRoom(roomId: string): Promise<void> { const room = chatRooms.find((item) => item.roomId === roomId); if (!room) return; unreadState.chat = Math.max(0, unreadState.chat - room.unreadCount); room.unreadCount = 0; unreadState.total = unreadState.note + unreadState.chat; userNotifications.content.forEach((item) => { if (item.targetType === "CHAT_ROOM" && item.targetId === roomId) item.read = true }) }
export async function getUserNotifications() { return copy(userNotifications) }
export async function getUnreadNotificationSummary() { return copy(unreadState) }
export async function readNotification(notificationId: number): Promise<void> { const item = userNotifications.content.find((notification) => notification.notificationId === notificationId); if (item) item.read = true }
export async function readAllNotifications(): Promise<void> { userNotifications.content.forEach((item) => { item.read = true }); unreadState.note = 0; unreadState.chat = 0; unreadState.total = 0 }
