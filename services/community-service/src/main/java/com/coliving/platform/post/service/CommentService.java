package com.coliving.platform.post.service;

import com.coliving.platform.common.auth.ResidentGuard;
import com.coliving.platform.common.auth.ResidentGuard.Resident;
import com.coliving.platform.common.exception.CommunityException;
import com.coliving.platform.common.exception.ErrorCode;
import com.coliving.platform.notification.entity.NotificationTarget;
import com.coliving.platform.notification.entity.NotificationType;
import com.coliving.platform.notification.service.NotificationService;
import com.coliving.platform.post.dto.CommentCreateResponseDTO;
import com.coliving.platform.post.dto.CommentRequestDTO;
import com.coliving.platform.post.dto.CommentUpdateResponseDTO;
import com.coliving.platform.post.entity.CommentEntity;
import com.coliving.platform.post.entity.PostEntity;
import com.coliving.platform.post.repository.CommentRepository;
import com.coliving.platform.post.repository.PostRepository;
import com.coliving.platform.safety.service.BlockQueryService;
import com.coliving.platform.safety.service.SanctionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 커뮤니티 게시판 — 댓글 · 답글 (API 5-6 ~ 5-8) */
@Service
@RequiredArgsConstructor
public class CommentService {

	private final CommentRepository commentRepository;
	private final PostRepository postRepository;
	private final PostService postService;
	private final ResidentGuard residentGuard;
	private final SanctionGuard sanctionGuard;
	private final BlockQueryService blockQueryService;
	private final NotificationService notificationService;

	/** 5-6 댓글 · 답글 작성 (답글은 1단계까지) */
	@Transactional
	public CommentCreateResponseDTO create(Long userId, Long postId, CommentRequestDTO request) {
		Resident me = residentGuard.requireResident(userId);
		sanctionGuard.checkCanWrite(userId);
		PostEntity post = postService.findActivePost(postId);
		residentGuard.requireSameBuilding(me, post.getBuildingId());

		CommentEntity parent = null;
		if (request.parentCommentId() != null) {
			parent = commentRepository.findById(request.parentCommentId())
					.filter(comment -> comment.getPost().getId().equals(postId) && comment.isActive())
					.orElseThrow(() -> new CommunityException(ErrorCode.COMMENT_NOT_FOUND));
			if (parent.isReply()) {
				throw new CommunityException(ErrorCode.REPLY_DEPTH_EXCEEDED);
			}
		}

		CommentEntity saved = commentRepository.save(CommentEntity.create(post, parent, userId, request.content()));
		postRepository.addCommentCount(postId, 1);
		notifyAuthor(me, post, parent);

		return new CommentCreateResponseDTO(saved.getId(), parent != null ? parent.getId() : null, saved.getCreatedAt());
	}

	/** 5-7 댓글 수정 (작성자 본인) */
	@Transactional
	public CommentUpdateResponseDTO update(Long userId, Long commentId, String content) {
		CommentEntity comment = findOwnActiveComment(userId, commentId);
		sanctionGuard.checkCanWrite(userId);
		comment.updateContent(content);
		commentRepository.saveAndFlush(comment);
		return new CommentUpdateResponseDTO(comment.getId(), comment.getUpdatedAt());
	}

	/** 5-8 댓글 삭제 (소프트 삭제, 달린 답글은 유지) */
	@Transactional
	public void delete(Long userId, Long commentId) {
		CommentEntity comment = findOwnActiveComment(userId, commentId);
		comment.delete();
		postRepository.addCommentCount(comment.getPost().getId(), -1);
	}

	private CommentEntity findOwnActiveComment(Long userId, Long commentId) {
		CommentEntity comment = commentRepository.findById(commentId)
				.filter(CommentEntity::isActive)
				.orElseThrow(() -> new CommunityException(ErrorCode.COMMENT_NOT_FOUND));
		if (!comment.isWrittenBy(userId)) {
			throw new CommunityException(ErrorCode.NOT_OWNER, "본인이 작성한 댓글만 수정·삭제할 수 있어요.");
		}
		return comment;
	}

	/** 글쓴이(답글이면 원댓글 작성자)에게 알림 — 본인 글이거나 상대가 나를 차단했으면 생략 */
	private void notifyAuthor(Resident me, PostEntity post, CommentEntity parent) {
		Long receiverId = parent != null ? parent.getAuthorId() : post.getAuthorId();
		if (receiverId.equals(me.userId()) || blockQueryService.isBlockedBy(me.userId(), receiverId)) {
			return;
		}
		if (parent != null) {
			notificationService.notifyUser(receiverId, NotificationType.REPLY, "새 답글",
					me.name() + "님이 회원님의 댓글에 답글을 남겼어요", NotificationTarget.POST, post.getId());
		} else {
			notificationService.notifyUser(receiverId, NotificationType.COMMENT, "새 댓글",
					me.name() + "님이 회원님의 게시글에 댓글을 남겼어요", NotificationTarget.POST, post.getId());
		}
	}
}
