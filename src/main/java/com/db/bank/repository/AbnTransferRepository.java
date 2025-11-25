package com.db.bank.repository;

import com.db.bank.domain.entity.AbnTransfer;
import com.db.bank.domain.entity.Account;
import com.db.bank.domain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AbnTransferRepository extends JpaRepository<AbnTransfer, Long> {

    //특정계좌의 이상거래 모두 조회
    List <AbnTransfer> findAllByAccountNum (Account accountNum);

    //특정 트랜잭션으로 이상거래 조회
    List <AbnTransfer> findAllByTransactionId (Transaction transactionId);

}
