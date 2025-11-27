package com.db.bank.app.controller;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import com.db.bank.app.dto.ScheduledTransferRunDto;
import com.db.bank.domain.entity.ScheduledTransferRun;
import com.db.bank.domain.enums.scheduledTransaction.RunResult;
import com.db.bank.service.ScheduledTransferRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scheduled-transfer-runs")
@RequiredArgsConstructor
public class ScheduledTransferRunController {

    private final ScheduledTransferRunService scheduledTransferRunService;

    /**
     * 특정 예약이체의 실행 이력 조회
     * 전체 실행 내역 조회 - /api/scheduled-transfer-runs/schedule/{scheduleId}
     * 특정 결과만 보고 싶으면  - /api/scheduled-transfer-runs/schedule/{scheduleId}?result=SUCCESS
     */
    @GetMapping("/schedule/{scheduleId}")
    public ApiResponse<List<ScheduledTransferRunDto.Response>> getRunsBySchedule(
            @PathVariable Long scheduleId,
            @RequestParam(required = false) RunResult result,
            Pageable pageable
    ) {
        List<ScheduledTransferRun> runs;

        if (result == null) {
            // 전체 실행 이력
            runs = scheduledTransferRunService.getRunsBySchedule(scheduleId, pageable);
        } else {
            // 결과별 필터 (SUCCESS, ERROR, INSUFFICIENT_FUNDS 등)
            runs = scheduledTransferRunService.getRunsByScheduleAndResult(scheduleId, result, pageable);
        }

        List<ScheduledTransferRunDto.Response> body = runs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.onSuccess(Status.SCHEDULE_RUN_READ_SUCCESS, body);
    }

    // ==========================
    // Entity → DTO 변환
    // ==========================
    private ScheduledTransferRunDto.Response toResponse(ScheduledTransferRun run) {
        return ScheduledTransferRunDto.Response.builder()
                .runId(run.getId())
                .scheduleId(run.getSchedule().getId())
                .executedAt(run.getExecutedAt())
                .result(run.getResult())
                .message(run.getMessage())
                .txnOutId(run.getTxnOut() != null ? run.getTxnOut().getId() : null)
                .txnInId(run.getTxnIn() != null ? run.getTxnIn().getId() : null)
                .failureReasonCode(run.getFailureReasonCode())
                .retryNo(run.getRetryNo())
                .maxRetries(run.getMaxRetries())
                .nextRetryAt(run.getNextRetryAt())
                .build();
    }
}
