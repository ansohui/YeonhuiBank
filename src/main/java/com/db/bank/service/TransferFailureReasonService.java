package com.db.bank.service;


import com.db.bank.apiPayload.exception.TransferFailureReasonException;
import com.db.bank.domain.entity.TransferFailureReason;
import com.db.bank.repository.TransferFailureReasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TransferFailureReasonService {

    private final TransferFailureReasonRepository transferFailureReasonRepository;

    //사유등록
    public TransferFailureReason createReason(String reasonCode, String description){
        if(transferFailureReasonRepository.existsByReasonCode(reasonCode)){
            throw new TransferFailureReasonException.DuplicateReasonCodeException("이미 존재하는 사유 코드입니다:"+reasonCode);
        }

        TransferFailureReason reason = TransferFailureReason.builder()
                .reasonCode(reasonCode)
                .description(description)
                .build();
        return transferFailureReasonRepository.save(reason);
    }

    //사유코드로 단건 조회
    @Transactional(readOnly = true)
    public TransferFailureReason getReason(String reasonCode){
        return transferFailureReasonRepository.findById(reasonCode)
                .orElseThrow(() ->
                        new TransferFailureReasonException.ReasonCodeNonExistsException("존재하지 않는 실패 사유 코드입니다. code=" + reasonCode));
    }
}


