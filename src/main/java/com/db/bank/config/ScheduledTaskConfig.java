package com.db.bank.config;

import com.db.bank.service.ScheduledTransactionService;
import com.db.bank.service.ScheduledTransferRunService;
import com.db.bank.service.TransactionService;

import com.db.bank.domain.entity.ScheduledTransferRun;
import com.db.bank.domain.enums.scheduledTransaction.RunResult;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduledTaskConfig {

    private final ScheduledTransactionService scheduledTransactionService;
    private final ScheduledTransferRunService scheduledTransferRunService;
    private final TransactionService transactionService;   // ⭐ 추가

    /**
     * 1) 예약이체 정상 실행
     */
    @Scheduled(cron = "0 */1 * * * *")
    public void runScheduledTransactions() {
        scheduledTransactionService.runDueSchedules(LocalDateTime.now());
    }

    /**
     * 2) 실패한 예약이체 재시도
     */
    @Scheduled(cron = "30 */5 * * * *")
    public void retryFailedScheduledTransfers() {

        LocalDateTime now = LocalDateTime.now();

        // 1. 재시도 대상 조회
        List<ScheduledTransferRun> retryTargets =
                scheduledTransferRunService.getRetryTargets(now, 3);

        for (ScheduledTransferRun run : retryTargets) {

            try {
                var schedule = run.getSchedule();


                var resultTx = transactionService.transfer(
                        schedule.getCreatedBy().getId(),
                        schedule.getFromAccount().getAccountNum(),
                        schedule.getToAccount().getAccountNum(),
                        schedule.getAmount(),
                        "[재시도] " + schedule.getMemo()
                );

                // 성공 로그 기록
                scheduledTransferRunService.recordSuccess(
                        schedule,
                        resultTx,
                        null,
                        "재시도 성공"
                );

            } catch (Exception e) {

                int nextRetry = run.getRetryNo() + 1;
                LocalDateTime nextRetryAt = now.plusMinutes(10);

                scheduledTransferRunService.recordFailure(
                        run.getSchedule(),
                        run.getTxnOut(),
                        run.getTxnIn(),
                        RunResult.ERROR,
                        "재시도 실패: " + e.getMessage(),
                        "RETRY_FAILED",
                        nextRetry,
                        run.getMaxRetries(),
                        nextRetryAt
                );
            }
        }
    }
}
