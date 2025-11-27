package com.db.bank.service;

import com.db.bank.apiPayload.exception.AccountException;
import com.db.bank.apiPayload.exception.UserException;
import com.db.bank.domain.entity.Account;
import com.db.bank.domain.entity.User;
import com.db.bank.domain.enums.account.AccountType;
import com.db.bank.repository.AccountRepository;
import com.db.bank.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    // 계좌 생성
    public Account createAccount(
            Long userId,
            String accountNum,
            AccountType accountType,
            BigDecimal initialBalance){
        if (userId == null) {
            throw new UserException.UserNonExistsException("userId가 null입니다. 요청 JSON에서 userId를 확인하세요.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException.UserNonExistsException("사용자를 찾을 수 없습니다. id="+userId));
        if (accountRepository.existsByAccountNum(accountNum)){
            throw new AccountException.AccountAlreadyExistsException("이미 존재하는 계좌번호 accountNum = "+accountNum);
        }
        if (!accountNum.matches("^[0-9-]+$")) {
            throw new AccountException.InvalidAccountNumException(
                    "계좌번호는 숫자와 '-'만 허용됩니다. 입력값=" + accountNum
            );
        }

        Account account = Account.builder()
                .accountNum(accountNum)
                .accountType(accountType != null ? accountType : AccountType.NORMAL)
                .balance(initialBalance != null ? initialBalance : BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        return accountRepository.save(account);
    }

    // 특정 유저 계좌 목록 조회
    @Transactional(readOnly = true)
    public Page<Account> getAccountsByUser(Long userId, Pageable pageable) {
        return accountRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    // 단일 계좌 조회, 소유자 검증
    @Transactional(readOnly = true)
    public Account getAccountForUser(String accountNum, Long userId){
        Account account = accountRepository.findByAccountNum(accountNum)
                .orElseThrow(()-> new AccountException.AccountNonExistsException("계좌 없음 accountNum ="+accountNum));
        if (!account.getUser().getId().equals(userId)){
            throw new AccountException.UnauthorizedAccountAccessException("해당 계좌에 접근할 권한이 없습니다.");

        }
        return account;

    }
    //계좌 번호로 계좌 조회 (락 걸고): 이체/입출금에서 사용할 내부용 메서드
    protected Account getAccountForUpdate(String accountNum) {
        return accountRepository.findByAccountNumForUpdate(accountNum)
                .orElseThrow(() -> new AccountException.AccountNonExistsException("계좌를 찾을 수 없습니다.accountNum ="+accountNum));
    }



}

