package com.coliving.platform.contract.entity;

import com.coliving.platform.contract.dto.internal.ApplicantInfo;
import com.coliving.platform.contract.dto.internal.RoomSnapshot;
import com.coliving.platform.contract.enums.ContractStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 신청(계약) 본체. 신청 = 계약 1:1, DRAFT부터 COMPLETED까지 한 행.
 *
 * - stay_period(GENERATED 컬럼)는 매핑하지 않는다. DB가 move_in_date·move_out_date로 계산한다.
 * - 스냅샷(건물명·호실명·금액·신청자 이름·휴대폰)은 서버가 gRPC로 받아 채운다. setter를 두지 않는다.
 * - 동시에 바뀔 수 있는 전이(APPROVED → PAID / EXPIRED 등)는 엔티티 메서드가 아니라
 *   Repository의 조건부 UPDATE(@Modifying)로 처리한다.
 * - 현재 시각(now)은 서비스에서 넘긴다. 만료 로직을 테스트에서 원하는 시각으로 검증하기 위해서다.
 */
@Entity
@Table(name = "contract_application")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA용 기본 생성자, 외부에서 new 금지
public class Contract {

    private static final Duration DRAFT_TTL = Duration.ofDays(2);
    private static final Duration PAYMENT_TTL = Duration.ofDays(1);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // BIGSERIAL
    private Long id; //insert 시 자동 채번, db 내부pk값, 다른 테이블이 참조할때, 서비스에서 부를때쓴다.

    @Column(name = "application_no", nullable = false, unique = true, length = 30)
    private String applicationNo;  //화면에 보여주는 신청번호

    @Column(name = "member_id", nullable = false)
    private Long memberId;   //회원의 id

    @Column(name = "room_id", nullable = false)
    private Long roomId;      //호실의 id

    @Column(name = "building_id", nullable = false)
    private Long buildingId;      //빌딩의 id

    // ---- 호실 스냅샷 ----
    @Column(name = "building_name", nullable = false, length = 100)
    private String buildingName;    //건물명

    @Column(name = "region", nullable = false, length = 100)
    private String region;   // 서울시 성동구]

    @Column(name = "adrress", nullable = false, length = 500)
    private String address;   //주소]


    @Column(name = "room_name", nullable = false, length = 100)
    private String roomName;   //호실명]

    @Column(name = "room_type", nullable = false)
    private String roomType;   //1인실 ]

    @Column(name = "room_structure", nullable = false, length = 100)
    private String roomStructure;   //호실구조]

    @Column(name = "floor_number", nullable = false)
    private int floorNumber;   //  층수]

    @Column(name = "area", nullable = false, precision = 10, scale = 2)
    private BigDecimal area;        //면적

    @Column(name = "room_facilities", nullable = false, columnDefinition = "TEXT")
    private String roomFacilities;

    @Column(name = "building_facilities", nullable = false, columnDefinition = "TEXT")
    private String buildingFacilities;

    // ---- 기간 ----
    @Column(name = "move_in_date", nullable = false)
    private LocalDate moveInDate;   //입주일

// stay_period가 시작일포함, 끝나는날 미포함이라서 이 날 다음 입주자가 들어올 수 있다
    @Column(name = "move_out_date", nullable = false)
    private LocalDate moveOutDate;        //퇴실예정일

    @Column(name = "contract_months", nullable = false)
    private Short contractMonths;                  //계약기간  // SMALLINT (DB타입은  smallint )

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ContractStatus status;          //지금 진행 단계. enum이름 그대로 저장

    // ---- 금액 스냅샷: DRAFT 동안 재계산, SUBMITTED에서 고정 ----
    @Column(name = "monthly_fee", nullable = false)
    private Long monthlyFee;        //월이용료

    @Column(name = "deposit", nullable = false)
    private Long deposit;       //보증금

    @NotNull
    @Column(name = "management_fee", nullable = false)
    private Long managementFee;       //관리비




    @Column(name = "first_payment", nullable = false)
    private Long firstPayment;              // 첫 달 이용료 + 보증금 - 할인

    @Column(name = "amount_fixed_at")
    private Instant amountFixedAt;      //금액이 확정된 시간

    // ---- 신청자 스냅샷 (관리자 이름·전화번호 검색을 위해 평문 저장) ----
    @Column(name = "applicant_name", length = 255)
    private String applicantName;       //회원 이름

    @Column(name = "applicant_email", length = 255)
    private String applicantEmail;      //회원 이메일

    @Column(name = "applicant_phone", length = 255)
    private String applicantPhone;      //회원 폰번호

    @Column(name = "applicant_address", length = 500)
    private String applicantAddress;        //회원주소

    @Column(name = "contract_language", length = 10)
    private String contractLanguage;        //계약서 언어

    // ---- 약관 동의 ----
    @Column(name = "terms_version", length = 20)
    private String termsVersion;        //동의한 약관 버전

    @Column(name = "terms_agreed_at")
    private Instant termsAgreedAt;      //약관 동의한 시간

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;        //관리자가 입력한 계약서 반려사유

    @Column(name = "expires_at")
    private Instant expiresAt;                            // 작성 2일 / 결제 1일

    @Version
    @Column(name = "version", nullable = false)
    private Long version;       // 수정할때마다 1씩 올라가고 관리자2명이 동시에 승인/반려라면 늦게 보낸사람이 예외처리됨.

    // DB DEFAULT now()는 JPA가 컬럼에 null을 넣으면 적용되지 않으므로 Hibernate가 직접 채운다
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;      //생성시간

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;      //마지막 수정 시간

    // =====================================================================
    // 생성: ⑨ 신청 1단계 "다음" 버튼
    // =====================================================================
    public static Contract createDraft(String applicationNo, Long memberId, RoomSnapshot room,
                                       LocalDate moveInDate, int contractMonths, Instant now) {
        Contract c = new Contract();
        c.applicationNo = applicationNo;
        c.memberId = memberId;
        c.status = ContractStatus.DRAFT;
        c.applyRoomAndPeriod(room, moveInDate, contractMonths);
        c.expiresAt = now.plus(DRAFT_TTL); //expriestAt은 바뀌지 않는다.
        return c;
    }

    // ⑩ 이전 버튼: 호실·기간 변경 (DRAFT일 때만). 호실 서비스에서 다시 조회한 값을 넘긴다
    // TODO 정책 확인: 만료된 DRAFT도 수정을 허용할지 (막으려면 requireActiveDraft(now)로 교체)
    public void changeDraft(RoomSnapshot room, LocalDate moveInDate, int contractMonths) {
        //상태가 draft가 맞는지
        requireStatus(ContractStatus.DRAFT, "작성 중인 신청만 수정할 수 있습니다");
        //호실, 기간 , 금액을 다시 계산
        applyRoomAndPeriod(room, moveInDate, contractMonths);
    }

    // ⑩ 신청서 제출: 최신 금액으로 재계산 후 고정, 신청자 스냅샷 저장, 약관 동의
    public void submit(RoomSnapshot latestRoom, ApplicantInfo applicant,
                       String contractLanguage, String termsVersion, Instant now) {
        // 1) 검증을 먼저 끝내고
        requireActiveDraft(now);//(상태가 draft이고 기한이 안지났는지)
        //약관에 동의를 했는지
        if (termsVersion == null) throw new IllegalArgumentException("약관에 동의해야 제출할 수 있습니다");
        //새로 조회한 호실id가 신청한 호실 id와 일치하는지 확인
        if (!this.roomId.equals(latestRoom.roomId())) {
            throw new IllegalArgumentException("신청한 호실과 조회한 호실이 다릅니다");
        }

        // 2) 그다음에 값을 바꾼다
        //최신 호실정보로 금액을 한번더 계산.
        applyRoomAndPeriod(latestRoom, this.moveInDate, this.contractMonths);
        //신청자 정보 저장.
        this.applicantName = applicant.name();
        this.applicantPhone = applicant.phone();
        this.applicantEmail = applicant.email();
        this.applicantAddress = applicant.address();
        //계약서 언어, 약관버전, 동의 시각 저장.
        this.contractLanguage = contractLanguage;
        this.termsVersion = termsVersion;
        this.termsAgreedAt = now;
        //금액이 고정되는 부분
        this.amountFixedAt = now;
        //심사중에는 기한만료가 없음
        this.expiresAt = null;
        //상태를 submitted로 변경
        this.status = ContractStatus.SUBMITTED;
    }

    // ⑪ 관리자 승인: 결제 기한 1일 시작
    public void approve(Instant now) {
        requireStatus(ContractStatus.SUBMITTED, "심사 중인 신청만 승인할 수 있습니다");
        this.status = ContractStatus.APPROVED;
        this.expiresAt = now.plus(PAYMENT_TTL);         //승인하면 만료일 1일 안에 결제를 진행 해야한다.
    }

    // ⑪ 관리자 반려: 최종 상태. 재신청은 새 신청(INSERT)으로 처음부터
    public void reject(String reason) {
        requireStatus(ContractStatus.SUBMITTED, "심사 중인 신청만 반려할 수 있습니다");
        this.status = ContractStatus.REJECTED; //거절 상태로 바꾸고
        this.rejectReason = reason; //이유를 붙인다.
    }

    // ⑪ 사용자 취소: SUBMITTED에서만
    public void cancelByUser() {
        requireStatus(ContractStatus.SUBMITTED, "심사 대기 중에만 취소할 수 있습니다");
        this.status = ContractStatus.CANCELED;
    }
    //조회, 수정요청이 들어왓을때 이 신청이 로그인한 사람 것인지 확인
    //반환 : true/ false (15번 회원이 20번 회원의 신청을 열어보려고 하면 서비스에서 이 메서드로 막는다.)
    public boolean isOwnedBy(Long memberId) {
        return this.memberId.equals(memberId);
    }

    // ---------------------------------------------------------------------
    // createDraft, changeDraft, submit가 이 메서드를 쓴다.
    private void applyRoomAndPeriod(RoomSnapshot room, LocalDate moveInDate, int contractMonths) {
        //계약기간 1개월 이상
        if (contractMonths <= 0) throw new IllegalArgumentException("계약 기간은 1개월 이상이어야 합니다");
        //월이용료 0보다 큼
        if (room.monthlyFee() <= 0) throw new IllegalArgumentException("이용료 정보가 올바르지 않습니다");
        //보증금이 음수가 아님
        if (room.deposit() < 0 ) throw new IllegalArgumentException("금액 정보가 올바르지 않습니다");
        //첫결제 금액이 음수가 아님
        long first = room.monthlyFee() + room.deposit();

        //호실정보 복사, 퇴실일 계산 , 금액복사, 첫결제 금액 계산
        this.roomId = room.roomId();
        this.buildingName = room.buildingName();
        this.roomName = room.roomName();
        this.moveInDate = moveInDate;
        this.moveOutDate = moveInDate.plusMonths(contractMonths);   // 퇴실 예정일은 서버가 계산
        this.contractMonths = (short) contractMonths;
        this.monthlyFee = room.monthlyFee();
        this.deposit = room.deposit();
        this.firstPayment = first;
    }
    //현재 상태가 expected가 아니면 예외(상태변경 메서드들이 부르는것)
    private void requireStatus(ContractStatus expected, String message) {
        if (this.status != expected) throw new IllegalStateException(message);
    }
    // 지금 시각이 만료시간을 넘으면 예외처리
    private void requireActiveDraft(Instant now) {
        requireStatus(ContractStatus.DRAFT, "작성 중인 신청만 처리할 수 있습니다");
        if (expiresAt != null && now.isAfter(expiresAt)) {
            throw new IllegalStateException("작성 기한이 지난 신청입니다");
        }
    }
}
