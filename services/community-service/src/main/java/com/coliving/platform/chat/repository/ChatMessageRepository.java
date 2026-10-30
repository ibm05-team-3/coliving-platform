package com.coliving.platform.chat.repository;

import com.coliving.platform.chat.document.ChatMessageDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * ObjectId 는 만들어진 순서대로 커지므로 id 로 시간순 정렬·비교
 * HiddenForNot(viewer) = 보는 사람에게 숨긴 메시지 제외 (hiddenFor 가 없는 메시지는 포함)
 */
public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {

	/** 최신 메시지부터 */
	List<ChatMessageDocument> findByRoomIdAndHiddenForNotOrderByIdDesc(Long roomId, Long viewerId, Pageable pageable);

	/** before 보다 이전 메시지 (위로 스크롤) */
	List<ChatMessageDocument> findByRoomIdAndIdLessThanAndHiddenForNotOrderByIdDesc(Long roomId, String before,
			Long viewerId, Pageable pageable);

	/** 보는 사람 기준 마지막 메시지 (4-1 미리보기) */
	Optional<ChatMessageDocument> findFirstByRoomIdAndHiddenForNotOrderByIdDesc(Long roomId, Long viewerId);

	/** 상대가 보낸 메시지 중 읽음 위치 이후 개수 (나에게 숨긴 메시지 제외) */
	long countByRoomIdAndSenderIdNotAndIdGreaterThanAndHiddenForNot(Long roomId, Long senderId, String afterId,
			Long viewerId);

	long countByRoomIdAndSenderIdNotAndHiddenForNot(Long roomId, Long senderId, Long viewerId);
}
