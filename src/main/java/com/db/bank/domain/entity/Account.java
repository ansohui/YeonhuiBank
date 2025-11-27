package com.db.bank.domain.entity;


import com.db.bank.domain.enums.account.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    // 예: 123-456-789012
    @Column(nullable = false, unique = true, length = 30)
    @Schema(description = "계좌번호 (숫자와 '-'만 사용 가능)", example = "123-456-789012")
    @Pattern(regexp = "^[0-9-]+$", message = "계좌번호는 숫자와 '-'만 사용할 수 있습니다.")
    private String accountNum;

    // 계좌 잔액 최초 0
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    // 기본값 NORMAL (일반 사용자 계좌)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType accountType = AccountType.NORMAL;


    // 계좌 생성일
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 사용자와 FK (User 하나가 여러 계좌) 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
