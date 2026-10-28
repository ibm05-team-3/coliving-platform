package com.coliving.platform.contract.repository;

import com.coliving.platform.contract.entity.Contract;
import com.coliving.platform.contract.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract,Long> {

    Optional<Contract> findByApplicationNo(String applicationNo);

    // 같은 회원이 같은 호실로 만든 작성 중 신청서 (이전으로 돌아왔을 때 재사용)
    Optional<Contract> findByMemberIdAndRoomIdAndStatus(Long memberId, Long roomId, ContractStatus status);
    @Query(value = "SELECT nextval('application_no_seq')", nativeQuery = true)
    long nextApplicationSeq();


}
