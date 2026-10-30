package com.coliving.platform.contract.service;

import com.coliving.platform.common.exception.ApplicationAccessDeniedException;
import com.coliving.platform.common.exception.ApplicationNotFoundException;
import com.coliving.platform.contract.dto.application.ApplicationDraftRequest;
import com.coliving.platform.contract.dto.application.ApplicationDraftResponse;
import com.coliving.platform.contract.dto.application.ApplicationSubmitRequest;
import com.coliving.platform.contract.dto.application.ApplicationSubmitResponse;
import com.coliving.platform.contract.dto.application.ApplyFormResponse;
import com.coliving.platform.contract.dto.internal.ApplicantInfo;
import com.coliving.platform.contract.dto.internal.RoomSnapshot;
import com.coliving.platform.contract.dto.internal.UserContractInfoDTO;
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
import java.util.Optional;
import java.util.Set;

import static com.coliving.platform.contract.enums.ContractStatus.DRAFT;
import static com.coliving.platform.contract.enums.ContractStatus.SUBMITTED;

/**
 * 신청 1단계 (1-1 호실 선택 → 1-2 신청자 정보 → 제출).
 *
 * memberId는 항상 컨트롤러가 X-User 헤더(gateway가 JWT에서 꺼내 넣어준 값)로 넘긴다.
 * URL이나 body로 받은 회원 번호는 쓰지 않는다.
 */
@Service
public class ApplicationService {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.BASIC_ISO_DATE; // 20261028
    private static final Set<Integer> ALLOWED_MONTHS = Set.of(3, 6, 12);                // 화면 선택지와 같게
    private static final Set<String> ALLOWED_LANGS = Set.of("ko", "en");

    // final + 생성자 주입. (이전 코드는 필드에 주입 표시가 없어서 null이었다)
    private final ContractRepository contractRepository;
    private final HistoryRepository historyRepository;
    private final RoomQueryService roomQueryService;
    private final UserQueryService userQueryService;

    public ApplicationService(ContractRepository contractRepository,
                              HistoryRepository historyRepository,
                              RoomQueryService roomQueryService,
                              UserQueryService userQueryService) {
        this.contractRepository = contractRepository;
        this.historyRepository = historyRepository;
        this.roomQueryService = roomQueryService;
        this.userQueryService = userQueryService;
    }

    private String newApplicationNo() {
        String date = LocalDate.now(SEOUL).format(NO_DATE);
        long seq = contractRepository.nextApplicationSeq();
        return "CT-" + date + "-" + String.format("%04d", seq);   // CT-20261028-0001
    }

    // =====================================================================
    // 1-1 → 다음 단계: DRAFT 생성
    // 같은 회원이 같은 호실로 작성 중인 DRAFT가 있으면 새로 만들지 않고 그걸 수정한다.
    // ("이전"으로 돌아와 기간만 바꾸고 다시 "다음"을 눌러도 신청서가 쌓이지 않게)
    // =====================================================================
    @Transactional
    public ApplicationDraftResponse createDraft(Long memberId, ApplicationDraftRequest req) {
        validateDraftRequest(req);
        RoomSnapshot snap = RoomSnapshot.from(roomQueryService.getRoomForContract(req.roomId())); // 서버가 조회한 금액만

        Optional<Contract> existing = contractRepository.findByMemberIdAndRoomIdAndStatus(memberId, req.roomId(), DRAFT);
        if (existing.isPresent()) {
            Contract c = existing.get();
            c.changeDraft(snap, req.moveInDate(), req.months());
            contractRepository.saveAndFlush(c);                    // 기간 겹침 에러를 여기서 바로 받기 위해 flush
            return ApplicationDraftResponse.from(c);
        }

        Contract c = Contract.createDraft(newApplicationNo(), memberId, snap,
                req.moveInDate(), req.months(), Instant.now());
        contractRepository.saveAndFlush(c);                        // flush해야 기간 겹침 에러가 여기서 터짐
        historyRepository.save(ContractStatusHistory.user(c.getId(), null, DRAFT, memberId));

        return new ApplicationDraftResponse(c.getApplicationNo(), c.getStatus(), c.getExpiresAt().atOffset(ZoneOffset.ofHours(9)));
    }

    // =====================================================================
    // 1-2 신청자 정보 화면
    // =====================================================================
    public ApplyFormResponse getForm(Long memberId, String applicationNo) {
        Contract c = findOwned(memberId, applicationNo);
        if (c.getStatus() != DRAFT) {
            throw new IllegalStateException("이미 제출했거나 작성할 수 없는 신청서입니다.");
        }
        UserContractInfoDTO user = userQueryService.getUserForContract(memberId);
        return ApplyFormResponse.of(c, user);
    }

    // =====================================================================
    // 1-2 신청서 제출: DRAFT → SUBMITTED, 금액 고정
    // =====================================================================
    @Transactional
    public ApplicationSubmitResponse submit(Long memberId, String applicationNo, ApplicationSubmitRequest req) {
        Contract c = findOwned(memberId, applicationNo);

        if (req.contractLang() == null || !ALLOWED_LANGS.contains(req.contractLang())) {
            throw new IllegalArgumentException("계약서 언어를 선택해주세요.");
        }
        if (!Contract.CURRENT_TERMS_VERSION.equals(req.termsVersion())) {
            throw new IllegalArgumentException("약관이 변경되었습니다. 새로고침 후 다시 동의해주세요.");
        }

        // 금액은 제출 시점의 매물 서비스 값으로 다시 계산해서 고정한다
        RoomSnapshot latest = RoomSnapshot.from(roomQueryService.getRoomForContract(c.getRoomId()));

        // 이름·휴대폰은 멤버 서비스 값 그대로. 이메일·주소만 사용자가 고친 값
        UserContractInfoDTO user = userQueryService.getUserForContract(memberId);
        ApplicantInfo applicant = new ApplicantInfo(
                user.name(),
                user.phone(),
                blankToDefault(req.email(), user.email()),
                blankToDefault(req.address(), user.address())
        );
        if (applicant.email() == null || applicant.email().isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        Instant now = Instant.now();
        c.submit(latest, applicant, req.contractLang(), req.termsVersion(), now);   // DRAFT·기한 확인은 엔티티가 한다
        contractRepository.saveAndFlush(c);
        historyRepository.save(ContractStatusHistory.user(c.getId(), DRAFT, SUBMITTED, memberId));

        return new ApplicationSubmitResponse(c.getApplicationNo(), c.getStatus(), now.atOffset(ZoneOffset.ofHours(9)));
    }

    // ---------------------------------------------------------------------
    // 신청서 조회 + 본인 확인. URL의 신청번호는 사용자가 바꿀 수 있으므로 항상 주인을 확인한다
    private Contract findOwned(Long memberId, String applicationNo) {
        Contract c = contractRepository.findByApplicationNo(applicationNo)
                .orElseThrow(() -> new ApplicationNotFoundException("신청서를 찾을 수 없습니다."));
        if (!c.isOwnedBy(memberId)) {
            throw new ApplicationAccessDeniedException("본인의 신청서만 볼 수 있습니다.");
        }
        return c;
    }

    private void validateDraftRequest(ApplicationDraftRequest req) {
        if (req.roomId() == null) throw new IllegalArgumentException("호실을 선택해주세요.");
        if (req.moveInDate() == null) throw new IllegalArgumentException("입주일을 선택해주세요.");
        if (req.moveInDate().isBefore(LocalDate.now(SEOUL))) {
            throw new IllegalArgumentException("입주일은 오늘 이후로 선택해주세요.");
        }
        if (req.months() == null || !ALLOWED_MONTHS.contains(req.months())) {
            throw new IllegalArgumentException("계약 기간은 3, 6, 12개월 중에서 선택해주세요.");
        }
    }

    private static String blankToDefault(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }
}
