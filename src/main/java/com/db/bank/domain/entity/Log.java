package com.db.bank.domain.entity;
import com.db.bank.domain.enums.log.Action;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "log")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    //==Fk==

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id",nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_num", referencedColumnName = "account_num", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id", nullable = false)
    private User actorUser;

    // ===== 잔액 정보 =====

    @Column(name = "before_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal beforeBalance;

    @Column(name = "after_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal afterBalance;

    // 입금 / 출금 / 불법 / 수정
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 10)
    private Action action;

    // 로그 생성 시간
    @Column(name = "created_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}
