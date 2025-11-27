package com.db.bank.repository;


import com.db.bank.domain.entity.TransferFailureReason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferFailureReasonRepository extends JpaRepository<TransferFailureReason, String> {

    //사유코드 중복조회
    boolean existsByReasonCode(String reasonCode);

    //사유코드로 설명조회
    TransferFailureReason findByReasonCode(String reasonCode);


}
