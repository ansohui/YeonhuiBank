package com.db.bank.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "transfer_failure_reason")
public class TransferFailureReason {

    // 사유 코드 (PK)
    @Id
    @Column(name = "reason_code", length = 30, nullable = false)
    private String reasonCode;

    // 사유 설명
    @Column(name = "description", length = 255, nullable = false)
    private String description;
}
