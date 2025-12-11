-- ========================================
-- 최종 스키마 제약조건 및 인덱스
-- MySQL 8.0+ 호환 버전
-- ========================================

-- 1. 초기 데이터 삽입
-- 시스템 관리자 유저 생성
INSERT IGNORE INTO user (login_id, password, name, created_at)
VALUES ('system_admin', '$2a$10$jvlqtXrJLsa3f9jzi9.WGu1dIqbiiRTGc3TF8Iv83Si0sUvuKTThq', '시스템관리자', NOW());
-- 비번: admin123!

-- 실패 사유 코드
INSERT IGNORE INTO transfer_failure_reason (reason_code, description)
VALUES
    ('INSUFFICIENT_FUNDS', '잔액 부족'),
    ('ACCOUNT_LOCKED', '계좌 잠김'),
    ('DAILY_LIMIT_EXCEEDED', '일일 한도 초과'),
    ('RETRY_FAILED', '재시도 실패');

-- 시스템 입금 전용 계좌
INSERT IGNORE INTO account (account_num, balance, account_type, created_at, user_id)
VALUES ('999-000', 10000000, 'EXTERNAL_IN', NOW(), 1);

-- 시스템 출금 전용 계좌
INSERT IGNORE INTO account (account_num, balance, account_type, created_at, user_id)
VALUES ('999-111', 10000000, 'EXTERNAL_OUT', NOW(), 1);

-- ========================================
-- 2. 외래키 제약조건
-- ========================================

-- Account → User
ALTER TABLE `Account`
    ADD CONSTRAINT `FK_Account_User`
        FOREIGN KEY (`user_id`) REFERENCES `User`(`user_id`)
            ON DELETE RESTRICT ON UPDATE CASCADE;

-- transaction → Account (양방향 FK)
ALTER TABLE `transaction`
    ADD CONSTRAINT `FK_Account_TO_transaction_from`
        FOREIGN KEY (`from_account_num`) REFERENCES `Account`(`account_num`)
            ON DELETE RESTRICT ON UPDATE CASCADE,
ADD CONSTRAINT `FK_Account_TO_transaction_to`
    FOREIGN KEY (`to_account_num`) REFERENCES `Account`(`account_num`)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- log → transaction, Account, User(actor)
ALTER TABLE `log`
    ADD CONSTRAINT `FK_transaction_TO_log_1`
        FOREIGN KEY (`transaction_id`) REFERENCES `transaction`(`transaction_id`)
            ON DELETE RESTRICT ON UPDATE CASCADE,
ADD CONSTRAINT `FK_Account_TO_log_1`
    FOREIGN KEY (`account_num`) REFERENCES `Account`(`account_num`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
ADD CONSTRAINT `FK_User_TO_log_actor`
    FOREIGN KEY (`actor_user_id`) REFERENCES `User`(`user_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- scheduled_transaction → Account(from/to), User(created_by)
ALTER TABLE `scheduled_transaction`
    ADD CONSTRAINT `FK_Account_TO_scheduled_transaction_from`
        FOREIGN KEY (`from_account_id`) REFERENCES `Account`(`account_id`)
            ON DELETE RESTRICT ON UPDATE CASCADE,
ADD CONSTRAINT `FK_Account_TO_scheduled_transaction_to`
    FOREIGN KEY (`to_account_id`) REFERENCES `Account`(`account_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
ADD CONSTRAINT `FK_User_TO_scheduled_transaction_creator`
    FOREIGN KEY (`created_by`) REFERENCES `User`(`user_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- SCHEDULED_TRANSFER_RUN → scheduled_transaction, transaction(출금/입금)
-- 거래 삭제 시 SET NULL로 실행 로그는 유지
ALTER TABLE `SCHEDULED_TRANSFER_RUN`
    ADD CONSTRAINT `FK_sched_TO_run`
        FOREIGN KEY (`schedule_id`) REFERENCES `scheduled_transaction`(`schedule_id`)
            ON DELETE RESTRICT ON UPDATE CASCADE,
ADD CONSTRAINT `FK_transaction_TO_SCHEDULED_TRANSFER_RUN_1`
    FOREIGN KEY (`txn_out_id`) REFERENCES `transaction`(`transaction_id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
ADD CONSTRAINT `FK_transaction_TO_SCHEDULED_TRANSFER_RUN_2`
    FOREIGN KEY (`txn_in_id`) REFERENCES `transaction`(`transaction_id`)
    ON DELETE SET NULL ON UPDATE CASCADE;

-- transfer_limit → Account
ALTER TABLE `transfer_limit`
    ADD CONSTRAINT `FK_Account_TO_transfer_limit_1`
        FOREIGN KEY (`account_num`) REFERENCES `Account`(`account_num`)
            ON DELETE RESTRICT ON UPDATE CASCADE;

-- SCHEDULED_TRANSFER_RUN → transfer_failure_reason (실패 사유 코드)
ALTER TABLE `SCHEDULED_TRANSFER_RUN`
    ADD CONSTRAINT `FK_failure_reason_TO_run`
        FOREIGN KEY (`failure_reason_code`) REFERENCES `transfer_failure_reason`(`reason_code`)
            ON DELETE SET NULL ON UPDATE CASCADE;

-- abntransfer → transaction, Account
ALTER TABLE `abntransfer`
    MODIFY `transaction_id` BIGINT NULL,
    ADD CONSTRAINT `FK_atl_txn`
    FOREIGN KEY (`transaction_id`) REFERENCES `transaction`(`transaction_id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
ADD CONSTRAINT `FK_atl_acc`
    FOREIGN KEY (`account_num`) REFERENCES `Account`(`account_num`)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- ========================================
-- 3. 유니크 제약조건
-- ========================================

-- user 테이블: login_id 유니크
ALTER TABLE `user`
    ADD CONSTRAINT `uk_user_login_id` UNIQUE (`login_id`);

-- transfer_limit: 계좌 + 시작일 조합 유니크
ALTER TABLE `transfer_limit`
    ADD CONSTRAINT `UQ_transfer_limit_account_start`
        UNIQUE (`account_num`, `start_date`);

-- SCHEDULED_TRANSFER_RUN: 거래 ID 유니크 (한 거래는 한 실행에만 연결)
ALTER TABLE `SCHEDULED_TRANSFER_RUN`
    ADD CONSTRAINT `UQ_run_txn_out` UNIQUE (`txn_out_id`),
ADD CONSTRAINT `UQ_run_txn_in` UNIQUE (`txn_in_id`);

-- ========================================
-- 4. CHECK 제약조건
-- ========================================

-- account: 계좌번호 형식 검증
ALTER TABLE `account`
    ADD CONSTRAINT `chk_account_num_format`
        CHECK (account_num REGEXP '^[0-9-]+$');

-- account: 잔액 음수 방지
ALTER TABLE `account`
    ADD CONSTRAINT `chk_account_balance_non_negative`
        CHECK (balance >= 0);

-- transaction: 금액, 타입, 상태 검증
ALTER TABLE `transaction`
    ADD CONSTRAINT `chk_tx_amount` CHECK (`amount` > 0),
ADD CONSTRAINT `chk_tx_type` CHECK (`type` IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'FEE')),
ADD CONSTRAINT `chk_tx_status` CHECK (`status` IN ('PENDING', 'SUCCESS', 'FAILED'));

-- scheduled_transaction: 금액, 출금/입금 계좌 다름, 날짜 범위
ALTER TABLE `scheduled_transaction`
    ADD CONSTRAINT `chk_st_amount` CHECK (`amount` > 0),
ADD CONSTRAINT `chk_st_date_range`
    CHECK (`end_date` IS NULL OR `end_date` >= `start_date`);

-- transfer_limit: 한도 양수, 날짜 범위, 건당 <= 일일
ALTER TABLE `transfer_limit`
    ADD CONSTRAINT `chk_tl_positive` CHECK (
        (daily_limit_amt IS NULL OR daily_limit_amt >= 0) AND
        (per_tx_limit_amt IS NULL OR per_tx_limit_amt >= 0)
        ),
ADD CONSTRAINT `chk_limit_date_range`
    CHECK (`end_date` IS NULL OR `end_date` >= `start_date`),
ADD CONSTRAINT `chk_per_tx_lte_daily`
    CHECK (per_tx_limit_amt IS NULL OR daily_limit_amt IS NULL
           OR per_tx_limit_amt <= daily_limit_amt);

-- SCHEDULED_TRANSFER_RUN: 재시도 범위, 재시도 시간 논리
ALTER TABLE `SCHEDULED_TRANSFER_RUN`
    ADD CONSTRAINT `chk_run_retry_range`
        CHECK (`retry_no` IS NULL OR `retry_no` <= `max_retries`),
ADD CONSTRAINT `chk_run_next_retry_after_executed`
    CHECK (next_retry_at IS NULL OR next_retry_at > executed_at);

-- log: 잔액 음수 방지
ALTER TABLE `log`
    ADD CONSTRAINT `chk_log_balance_non_negative`
        CHECK (before_balance >= 0 AND after_balance >= 0);

-- ========================================
-- 5. 인덱스 (성능 최적화)
-- ========================================

-- 기본 인덱스
CREATE INDEX ix_account_user ON `Account`(`user_id`);
CREATE INDEX ix_tx_from_time ON `transaction`(`from_account_num`, `created_at`);
CREATE INDEX ix_tx_to_time ON `transaction`(`to_account_num`, `created_at`);
CREATE INDEX ix_tx_status_time ON `transaction`(`status`, `created_at`);

CREATE INDEX ix_log_acc_time ON `log`(`account_num`, `created_at`);
CREATE INDEX ix_log_txn ON `log`(`transaction_id`);
CREATE INDEX ix_log_txn_time ON `log`(`transaction_id`, `created_at` DESC);

CREATE INDEX ix_sched_status_run ON `scheduled_transaction`(`scheduled_status`, `next_run_at`);
CREATE INDEX ix_sched_from ON `scheduled_transaction`(`from_account_id`);
CREATE INDEX ix_sched_to ON `scheduled_transaction`(`to_account_id`);
CREATE INDEX ix_sched_creator ON `scheduled_transaction`(`created_by`);
CREATE INDEX ix_sched_creator_status ON `scheduled_transaction`(`created_by`, `scheduled_status`, `created_at` DESC);

CREATE INDEX ix_run_sched_time ON `SCHEDULED_TRANSFER_RUN`(`schedule_id`, `executed_at`);
CREATE INDEX ix_run_result_time ON `SCHEDULED_TRANSFER_RUN`(`result`, `executed_at`);
CREATE INDEX ix_run_schedule_result ON `SCHEDULED_TRANSFER_RUN`(`schedule_id`, `result`, `executed_at` DESC);
CREATE INDEX ix_run_retry_target ON `SCHEDULED_TRANSFER_RUN`(`result`, `retry_no`, `next_retry_at`);

CREATE INDEX ix_tl_acc_range ON `transfer_limit`(`account_num`, `start_date`, `end_date`);
CREATE INDEX ix_tl_status ON `transfer_limit`(`status`);
CREATE INDEX ix_tl_account_status ON `transfer_limit`(`account_num`, `status`, `start_date`, `end_date`);

CREATE INDEX ix_atl_acc_time ON `abntransfer`(`account_num`, `created_at`);
CREATE INDEX ix_abn_rule_time ON `abntransfer`(`rule_code`, `created_at` DESC);

-- 이상거래 탐지 최적화 인덱스
CREATE INDEX ix_tx_abn_detect ON `transaction`(`from_account_num`, `to_account_num`, `amount`, `created_at`);
-- ========================================
-- 6. 트리거  (mysql에서 수동 구현)
-- ========================================

-- ========================================
-- 7. 파티셔닝 (선택적, 대용량 데이터 처리 시)
-- ========================================

-- transaction 테이블: 월별 파티셔닝
-- 주의: 기존 데이터가 있으면 백업 후 적용
/*
ALTER TABLE transaction
PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at)) (
    PARTITION p202401 VALUES LESS THAN (202402),
    PARTITION p202402 VALUES LESS THAN (202403),
    PARTITION p202403 VALUES LESS THAN (202404),
    PARTITION p202404 VALUES LESS THAN (202405),
    PARTITION p202405 VALUES LESS THAN (202406),
    PARTITION p202406 VALUES LESS THAN (202407),
    PARTITION p202407 VALUES LESS THAN (202408),
    PARTITION p202408 VALUES LESS THAN (202409),
    PARTITION p202409 VALUES LESS THAN (202410),
    PARTITION p202410 VALUES LESS THAN (202411),
    PARTITION p202411 VALUES LESS THAN (202412),
    PARTITION p202412 VALUES LESS THAN (202501),
    PARTITION p202501 VALUES LESS THAN (202502),
    PARTITION p202502 VALUES LESS THAN (202503),
    PARTITION p202503 VALUES LESS THAN (202504),
    PARTITION p202504 VALUES LESS THAN (202505),
    PARTITION p202505 VALUES LESS THAN (202506),
    PARTITION p202506 VALUES LESS THAN (202507),
    PARTITION p202507 VALUES LESS THAN (202508),
    PARTITION p202508 VALUES LESS THAN (202509),
    PARTITION p202509 VALUES LESS THAN (202510),
    PARTITION p202510 VALUES LESS THAN (202511),
    PARTITION p202511 VALUES LESS THAN (202512),
    PARTITION p202512 VALUES LESS THAN (202601),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- log 테이블도 동일하게 파티셔닝
ALTER TABLE log
PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at)) (
    PARTITION p202401 VALUES LESS THAN (202402),
    PARTITION p202402 VALUES LESS THAN (202403),
    PARTITION p202403 VALUES LESS THAN (202404),
    PARTITION p202404 VALUES LESS THAN (202405),
    PARTITION p202405 VALUES LESS THAN (202406),
    PARTITION p202406 VALUES LESS THAN (202407),
    PARTITION p202407 VALUES LESS THAN (202408),
    PARTITION p202408 VALUES LESS THAN (202409),
    PARTITION p202409 VALUES LESS THAN (202410),
    PARTITION p202410 VALUES LESS THAN (202411),
    PARTITION p202411 VALUES LESS THAN (202412),
    PARTITION p202412 VALUES LESS THAN (202501),
    PARTITION p202501 VALUES LESS THAN (202502),
    PARTITION p202502 VALUES LESS THAN (202503),
    PARTITION p202503 VALUES LESS THAN (202504),
    PARTITION p202504 VALUES LESS THAN (202505),
    PARTITION p202505 VALUES LESS THAN (202506),
    PARTITION p202506 VALUES LESS THAN (202507),
    PARTITION p202507 VALUES LESS THAN (202508),
    PARTITION p202508 VALUES LESS THAN (202509),
    PARTITION p202509 VALUES LESS THAN (202510),
    PARTITION p202510 VALUES LESS THAN (202511),
    PARTITION p202511 VALUES LESS THAN (202512),
    PARTITION p202512 VALUES LESS THAN (202601),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
*/

-- ========================================
-- 완료
-- ========================================