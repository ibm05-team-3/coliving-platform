package com.coliving.platform.contract.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * 본인인증·전자서명 기록. 서명 시점 값을 증빙으로 남기므로 수정하지 않는다.
 * - document_id: 문서 담당 쪽 문서 ID를 값으로만 저장 (FK 없음)
 * - (application_id, document_id) UNIQUE, identity_verification_id UNIQUE는 DB가 막는다
 */
@Entity
@Immutable      //기록용 테이블이라서 update를 보내지 않는 쿼리
@Table(name = "contract_signature")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContractSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;        //자동증가

    @Column(name = "application_id", nullable = false)
    private Long applicationId;     //어느 신청의 서명인지

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "identity_verification_id", nullable = false, unique = true, length = 100)
    private String identityVerificationId;              // 포트원 인증 거래 ID

    @Column(name = "verified_name", length = 255)
    private String verifiedName;

    // CHAR(64): String 기본 매핑은 VARCHAR라 validate에서 타입 불일치가 난다. CHAR로 명시
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "document_hash", nullable = false, length = 64)
    private String documentHash;                        // 서명 시점의 문서 SHA-256 (hex 64자)

    @Column(name = "signature_image_path", length = 500)
    private String signatureImagePath;      //서명 이미지 파일 경로


    @CreationTimestamp
    @Column(name = "signed_at", nullable = false, updatable = false)
    private Instant signedAt;       //서명한 시간
    //포트원 본인인증을 마치고 서명을 제출할 때 문서 해시를 검증. 값들은 수정없이 들어간다.
    public static ContractSignature create(Long applicationId, Long documentId, String identityVerificationId,
                                           String verifiedName, String documentHash,
                                           String signatureImagePath, String signerIp) {
        if (documentHash == null || documentHash.length() != 64)
            throw new IllegalArgumentException("문서 해시는 SHA-256 hex 64자여야 합니다");
        ContractSignature s = new ContractSignature();
        s.applicationId = applicationId;
        s.documentId = documentId;
        s.identityVerificationId = identityVerificationId;
        s.verifiedName = verifiedName;
        s.documentHash = documentHash;
        s.signatureImagePath = signatureImagePath;

        return s;
    }
}
