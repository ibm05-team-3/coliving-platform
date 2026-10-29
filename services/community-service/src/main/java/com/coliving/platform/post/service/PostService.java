package com.coliving.platform.post.service;

import com.coliving.platform.client.ContractClient;
import com.coliving.platform.client.ContractClient.ResidentRoom;
import com.coliving.platform.client.UserClient;
import com.coliving.platform.client.UserClient.UserSummary;
import com.coliving.platform.common.auth.ResidentGuard;
import com.coliving.platform.common.auth.ResidentGuard.Resident;
import com.coliving.platform.common.entity.ContentStatus;
import com.coliving.platform.common.entity.SenderType;
import com.coliving.platform.common.exception.CommunityException;
import com.coliving.platform.common.exception.ErrorCode;
import com.coliving.platform.post.dto.CommentDTO;
import com.coliving.platform.post.dto.MySummaryDTO;
import com.coliving.platform.post.dto.PinnedPostDTO;
import com.coliving.platform.post.dto.PopularPostDTO;
import com.coliving.platform.post.dto.PostCreateResponseDTO;
import com.coliving.platform.post.dto.PostDetailDTO;
import com.coliving.platform.post.dto.PostListResponseDTO;
import com.coliving.platform.post.dto.PostRequestDTO;
import com.coliving.platform.post.dto.PostSummaryDTO;
import com.coliving.platform.post.dto.PostUpdateResponseDTO;
import com.coliving.platform.post.dto.ReplyDTO;
import com.coliving.platform.post.entity.CommentEntity;
import com.coliving.platform.post.entity.PostCategory;
import com.coliving.platform.post.entity.PostEntity;
import com.coliving.platform.post.repository.CommentRepository;
import com.coliving.platform.post.repository.PostRepository;
import com.coliving.platform.post.repository.PostSpecs;
import com.coliving.platform.safety.service.BlockQueryService;
import com.coliving.platform.safety.service.SanctionGuard;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 커뮤니티 게시판 — 게시글 (API 5-1 ~ 5-5, 5-9) */
@Service
@RequiredArgsConstructor
public class PostService {

	private static final Duration VIEW_DEDUP_TTL = Duration.ofHours(24);
	private static final Duration NEW_POST_PERIOD = Duration.ofHours(24);

	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final ResidentGuard residentGuard;
	private final BlockQueryService blockQueryService;
	private final SanctionGuard sanctionGuard;
	private final UserClient userClient;
	private final ContractClient contractClient;
	private final StringRedisTemplate redisTemplate;

	/** 5-1 게시글 목록 (내 건물, 공지는 pinned 로 따로) */
	@Transactional(readOnly = true)
	public PostListResponseDTO getPosts(Long userId, PostCategory category, String keyword, int page, int size) {
		Resident me = residentGuard.requireResident(userId);
		Set<Long> blocked = blockQueryService.getBlockedUserIds(userId);

		List<PostEntity> notices = postRepository.findByBuildingIdAndCategoryAndStatusAndPinnedTrueOrderByCreatedAtDesc(
				me.buildingId(), PostCategory.NOTICE, ContentStatus.ACTIVE);
		String managerName = me.buildingName() + " 매니저";
		List<PinnedPostDTO> pinned = notices.stream()
				.map(post -> new PinnedPostDTO(post.getId(), post.getCategory(), post.getTitle(), managerName,
						post.getViewCount(), post.getCommentCount(), post.getCreatedAt()))
				.toList();

		Page<PostEntity> posts = postRepository.findAll(
				PostSpecs.list(me.buildingId(), category == PostCategory.NOTICE ? null : category, keyword, blocked),
				PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));

		AuthorLookup authors = lookupAuthors(posts.getContent().stream().map(PostEntity::getAuthorId).toList());
		LocalDateTime newSince = LocalDateTime.now().minus(NEW_POST_PERIOD);
		List<PostSummaryDTO> content = posts.getContent().stream()
				.map(post -> new PostSummaryDTO(
						post.getId(),
						post.getCategory(),
						post.getTitle(),
						post.getAuthorId(),
						authors.name(post.getAuthorId()),
						authors.room(post.getAuthorId()),
						post.getViewCount(),
						post.getCommentCount(),
						post.getCreatedAt(),
						post.getCreatedAt() != null && post.getCreatedAt().isAfter(newSince)))
				.toList();

		return new PostListResponseDTO(me.buildingId(), me.buildingName(), pinned, content,
				posts.getNumber(), posts.getSize(), posts.getTotalElements(), posts.getTotalPages());
	}

	/** 5-2 게시글 상세 (같은 사람은 24시간에 한 번만 조회수 +1) */
	@Transactional
	public PostDetailDTO getPost(Long userId, Long postId) {
		Resident me = residentGuard.requireResident(userId);
		PostEntity post = findActivePost(postId);
		residentGuard.requireSameBuilding(me, post.getBuildingId());

		int viewCount = post.getViewCount();
		Boolean firstView = redisTemplate.opsForValue()
				.setIfAbsent("post:view:" + postId + ":" + userId, "1", VIEW_DEDUP_TTL);
		if (Boolean.TRUE.equals(firstView)) {
			postRepository.increaseViewCount(postId);
			viewCount++;
		}

		Set<Long> blocked = blockQueryService.getBlockedUserIds(userId);
		List<CommentEntity> comments = commentRepository.findByPost_IdOrderByCreatedAtAscIdAsc(postId);

		Set<Long> authorIds = new HashSet<>();
		authorIds.add(post.getAuthorId());
		comments.forEach(comment -> authorIds.add(comment.getAuthorId()));
		AuthorLookup authors = lookupAuthors(authorIds);

		boolean notice = post.getAuthorType() == SenderType.ADMIN;
		return new PostDetailDTO(
				post.getId(),
				post.getCategory(),
				post.getTitle(),
				post.getContent(),
				post.getAuthorId(),
				notice ? me.buildingName() + " 매니저" : authors.name(post.getAuthorId()),
				notice ? null : authors.room(post.getAuthorId()),
				viewCount,
				post.getCreatedAt(),
				editedAt(post.getCreatedAt(), post.getUpdatedAt()),
				post.isWrittenBy(userId),
				buildCommentTree(comments, userId, blocked, authors));
	}

	/** 5-3 게시글 작성 (입주자: 내 건물 / 관리자: 공지만, buildingId 필수) */
	@Transactional
	public PostCreateResponseDTO create(Long userId, PostRequestDTO request) {
		PostEntity post;
		if (residentGuard.isAdmin(userId)) {
			if (request.category() != PostCategory.NOTICE || request.buildingId() == null) {
				throw new CommunityException(ErrorCode.INVALID_REQUEST, "관리자는 건물을 지정해 공지만 작성할 수 있어요.");
			}
			post = PostEntity.create(request.buildingId(), userId, SenderType.ADMIN, PostCategory.NOTICE,
					request.title(), request.content());
		} else {
			Resident me = residentGuard.requireResident(userId);
			sanctionGuard.checkCanWrite(userId);
			requireNotNotice(request.category());
			post = PostEntity.create(me.buildingId(), userId, SenderType.USER, request.category(),
					request.title(), request.content());
		}
		PostEntity saved = postRepository.save(post);
		return new PostCreateResponseDTO(saved.getId(), saved.getCreatedAt());
	}

	/** 5-4 게시글 수정 (작성자 본인) */
	@Transactional
	public PostUpdateResponseDTO update(Long userId, Long postId, PostRequestDTO request) {
		PostEntity post = findActivePost(postId);
		requireOwner(post, userId);
		if (post.getAuthorType() == SenderType.USER) {
			sanctionGuard.checkCanWrite(userId);
			requireNotNotice(request.category());
		}
		PostCategory category = post.getAuthorType() == SenderType.ADMIN ? PostCategory.NOTICE : request.category();
		post.update(category, request.title(), request.content());
		postRepository.saveAndFlush(post);
		return new PostUpdateResponseDTO(post.getId(), post.getUpdatedAt());
	}

	/** 5-5 게시글 삭제 (소프트 삭제) */
	@Transactional
	public void delete(Long userId, Long postId) {
		PostEntity post = findActivePost(postId);
		requireOwner(post, userId);
		post.delete();
	}

	/** 5-9 내 커뮤니티 정보 · 이번 주 인기글 */
	@Transactional(readOnly = true)
	public MySummaryDTO getMySummary(Long userId) {
		Resident me = residentGuard.requireResident(userId);
		long postCount = postRepository.countByAuthorIdAndBuildingIdAndStatus(userId, me.buildingId(), ContentStatus.ACTIVE);
		long receivedComments = commentRepository.countReceivedComments(userId, ContentStatus.ACTIVE);
		List<PopularPostDTO> popular = postRepository
				.findTop3ByBuildingIdAndStatusAndCategoryNotAndCreatedAtAfterOrderByViewCountDescIdDesc(
						me.buildingId(), ContentStatus.ACTIVE, PostCategory.NOTICE, LocalDateTime.now().minusDays(7))
				.stream()
				.map(post -> new PopularPostDTO(post.getId(), post.getTitle(), post.getViewCount(), post.getCommentCount()))
				.toList();
		return new MySummaryDTO(me.name(), me.buildingName(), me.roomNumber(), postCount, receivedComments, null, popular);
	}

	// ---------- 내부 ----------

	public PostEntity findActivePost(Long postId) {
		PostEntity post = postRepository.findById(postId)
				.orElseThrow(() -> new CommunityException(ErrorCode.POST_NOT_FOUND));
		if (!post.isActive()) {
			throw new CommunityException(ErrorCode.POST_NOT_FOUND);
		}
		return post;
	}

	private void requireOwner(PostEntity post, Long userId) {
		if (!post.isWrittenBy(userId)) {
			throw new CommunityException(ErrorCode.NOT_OWNER);
		}
	}

	private void requireNotNotice(PostCategory category) {
		if (category == PostCategory.NOTICE) {
			throw new CommunityException(ErrorCode.NOT_OWNER, "공지는 관리자만 작성할 수 있어요.");
		}
	}

	/** 수정한 적이 없으면 null (생성 직후 updated_at = created_at) */
	private LocalDateTime editedAt(LocalDateTime createdAt, LocalDateTime updatedAt) {
		if (createdAt == null || updatedAt == null) {
			return null;
		}
		return updatedAt.isAfter(createdAt.plusSeconds(1)) ? updatedAt : null;
	}

	private List<CommentDTO> buildCommentTree(List<CommentEntity> comments, Long userId, Set<Long> blocked,
			AuthorLookup authors) {
		Map<Long, List<ReplyDTO>> repliesByParent = new LinkedHashMap<>();
		for (CommentEntity comment : comments) {
			if (comment.isReply()) {
				repliesByParent.computeIfAbsent(comment.getParentComment().getId(), key -> new ArrayList<>())
						.add(toReply(comment, userId, blocked, authors));
			}
		}
		List<CommentDTO> result = new ArrayList<>();
		for (CommentEntity comment : comments) {
			if (comment.isReply()) {
				continue;
			}
			ReplyDTO view = toReply(comment, userId, blocked, authors);
			result.add(new CommentDTO(view.commentId(), view.authorId(), view.authorName(), view.authorRoomNumber(),
					view.content(), view.status(), view.blocked(), view.isMine(), view.createdAt(),
					repliesByParent.getOrDefault(comment.getId(), List.of())));
		}
		return result;
	}

	/** 삭제·숨김 → 내용·작성자 null / 차단한 사람 → blocked = true, 내용·작성자 null */
	private ReplyDTO toReply(CommentEntity comment, Long userId, Set<Long> blocked, AuthorLookup authors) {
		boolean visible = comment.isActive();
		boolean isBlocked = visible && blocked.contains(comment.getAuthorId());
		boolean show = visible && !isBlocked;
		return new ReplyDTO(
				comment.getId(),
				show ? comment.getAuthorId() : null,
				show ? authors.name(comment.getAuthorId()) : null,
				show ? authors.room(comment.getAuthorId()) : null,
				show ? comment.getContent() : null,
				comment.getStatus(),
				isBlocked,
				visible && comment.isWrittenBy(userId),
				comment.getCreatedAt());
	}

	/** 작성자 이름(유저 서비스) · 호실(계약 서비스) 일괄 조회 */
	private AuthorLookup lookupAuthors(Collection<Long> userIds) {
		List<Long> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();
		if (ids.isEmpty()) {
			return new AuthorLookup(Map.of(), Map.of());
		}
		return new AuthorLookup(userClient.getUsers(ids), contractClient.getResidentRooms(ids));
	}

	private record AuthorLookup(Map<Long, UserSummary> users, Map<Long, ResidentRoom> rooms) {

		String name(Long userId) {
			UserSummary user = users.get(userId);
			return user != null ? user.name() : "알 수 없는 사용자";
		}

		String room(Long userId) {
			ResidentRoom room = rooms.get(userId);
			return room != null ? room.roomNumber() : null;
		}
	}
}
