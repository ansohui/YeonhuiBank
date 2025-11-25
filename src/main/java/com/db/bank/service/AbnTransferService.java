package com.db.bank.service;

import com.db.bank.apiPayload.exception.AccountException;
import com.db.bank.apiPayload.exception.TransactionException;
import com.db.bank.domain.entity.AbnTransfer;
import com.db.bank.domain.entity.Account;
import com.db.bank.domain.entity.Transaction;
import com.db.bank.domain.enums.abnTransfer.RuleCode;
import com.db.bank.repository.AbnTransferRepository;
import com.db.bank.repository.AccountRepository;
import com.db.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AbnTransferService {

    private final AbnTransferRepository abnTransferRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    //이상거래등록
    @Transactional
    public AbnTransfer createAbnTransfer(Long transactionId, String accountNum, RuleCode ruleCode, String detailMessage) {
        Transaction transaction = transactionRepository.findById(transactionId).
                orElseThrow(()->new TransactionException.TransactionNonExistsException("존재하지 않는 트랜잭션"));
        Account account = accountRepository.findByAccountNum(accountNum)
                .orElseThrow(()->new AccountException.AccountNonExistsException("존재하지 않는 계좌입니다"));

        AbnTransfer abnTransfer=AbnTransfer.builder()
                .transactionId(transaction)
                .accountNum(account)
                .ruleCode(ruleCode)
                .detailMessage(detailMessage)
                .createdAt(LocalDateTime.now())
                .build();

        return abnTransferRepository.save(abnTransfer);

    }

    //계좌번호로 이상거래 가져오기
    @Transactional(readOnly = true)
    public List<AbnTransfer> getAllAbnTransfersByAccount(String accountNum) {
        Account account = accountRepository.findByAccountNum(accountNum)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("계좌를 찾을 수 없습니다."));
        return abnTransferRepository.findAllByAccountNum(account);
    }

}
