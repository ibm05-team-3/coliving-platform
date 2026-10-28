package com.coliving.dashboard.roomstatus.service;

import com.coliving.dashboard.kafka.dto.ContractStatusChangedEvent;
import com.coliving.dashboard.roomstatus.document.RoomStatusDocument;
import com.coliving.dashboard.roomstatus.dto.RoomStatusResponse;
import com.coliving.dashboard.roomstatus.mapper.RoomStatusMapper;
import com.coliving.dashboard.roomstatus.repository.RoomStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/** 계약 이벤트로 호실 현황을 갱신하고, 화면에 보여줄 호실 목록을 조회한다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomStatusService {

    private final RoomStatusRepository repository;

    /** 계약 상태 변경 이벤트를 호실 상태에 반영한다. */
    public void applyContractEvent(ContractStatusChangedEvent event) {
        String roomStatus = RoomStatusMapper.toRoomStatus(event.currentStatus()).orElse(null);
        if (roomStatus == null) {
            log.warn("매핑되지 않은 계약 상태라 무시합니다: {} (eventId={})",
                    event.currentStatus(), event.eventId());
            return;
        }

        String id = String.valueOf(event.roomId());
        Instant occurredAt = event.occurredAt().toInstant();

        RoomStatusDocument existing = repository.findById(id).orElse(null);

        // 늦게 도착한 오래된 이벤트가 최신 상태를 덮어쓰지 않도록 시간을 비교
        if (existing != null && existing.getUpdatedAt() != null
                && existing.getUpdatedAt().isAfter(occurredAt)) {
            log.info("더 최신 상태가 이미 있어 무시합니다: roomId={}", event.roomId());
            return;
        }

        repository.save(RoomStatusDocument.builder()
                .id(id)
                .roomId(event.roomId())
                .buildingId(event.buildingId())
                .buildingName(event.buildingName())
                .floor(existing != null ? existing.getFloor() : null)   // 층은 계약 이벤트에 없으므로 기존 값 유지
                .roomName(event.roomName())
                .status(roomStatus)
                .updatedAt(occurredAt)
                .build());
    }

    /** 건물별 호실 목록을 높은 층부터, 같은 층에서는 호실명 순으로 조회한다. */
    public List<RoomStatusResponse> getRooms(Long buildingId) {
        return repository.findByBuildingId(buildingId, PageRequest.of(0, 1000)).stream()
                .map(RoomStatusResponse::from)
                .sorted(Comparator
                        .comparing(RoomStatusResponse::floor,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(RoomStatusResponse::roomName,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }
}