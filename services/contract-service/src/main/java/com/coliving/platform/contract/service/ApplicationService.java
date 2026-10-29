package com.coliving.platform.contract.service;

import com.coliving.platform.contract.dto.application.ApplicationDraftRequest;
import com.coliving.platform.contract.dto.application.ApplicationDraftResponse;
import com.coliving.platform.contract.dto.internal.RoomContractInfoDTO;
import com.coliving.platform.contract.dto.internal.RoomSnapshot;
import com.coliving.platform.contract.entity.Contract;
import com.coliving.platform.contract.entity.ContractStatusHistory;
import com.coliving.platform.contract.repository.ContractRepository;
import com.coliving.platform.contract.repository.HistoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static com.coliving.platform.contract.enums.ContractStatus.DRAFT;

@Service
public class ApplicationService {
    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.BASIC_ISO_DATE; // 20261028
    ContractRepository contractRepository;
    HistoryRepository historyRepository;
    RoomQueryService roomQueryService;


    private String newApplicationNo() {
        String date = LocalDate.now(ZoneId.of("Asia/Seoul")).format(NO_DATE);
        long seq = contractRepository.nextApplicationSeq();
        return "CT-" + date + "-" + String.format("%04d", seq);   // CT-20261028-0001
    }
    @Transactional
    public ApplicationDraftResponse createDraft(Long memberId, ApplicationDraftRequest req) {
        RoomContractInfoDTO r = roomQueryService.getRoomForContract(req.roomId());
        RoomSnapshot snap = toSnapshot(r);                         // 서버가 조회한 금액만

        Contract c = Contract.createDraft(newApplicationNo(), memberId, snap,
                req.moveInDate(), req.months(), Instant.now());
        contractRepository.saveAndFlush(c);                        // flush해야 기간 겹침 에러가 여기서 터짐
        historyRepository.save(ContractStatusHistory.user(c.getId(), null, DRAFT, memberId));

        return new ApplicationDraftResponse(c.getApplicationNo(), c.getStatus(), c.getExpiresAt().atOffset(ZoneOffset.ofHours(9)));
    }
    private RoomSnapshot toSnapshot(RoomContractInfoDTO r) {
        return new RoomSnapshot(
                r.roomId(),
                r.buildingName(),
                r.roomName(),
                r.monthlyFee(),
                r.deposit()
        );
    }
}
