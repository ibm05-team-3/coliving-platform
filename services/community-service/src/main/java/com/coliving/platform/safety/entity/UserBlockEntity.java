package com.coliving.platform.safety.entity;

import com.coliving.platform.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자 차단 — 한쪽 방향, 상대에게 알리지 않음 */
@Entity
@Table(name = "user_blocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBlockEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long blockerId;

	@Column(nullable = false)
	private Long blockedId;

	public static UserBlockEntity create(Long blockerId, Long blockedId) {
		UserBlockEntity block = new UserBlockEntity();
		block.blockerId = blockerId;
		block.blockedId = blockedId;
		return block;
	}
}
