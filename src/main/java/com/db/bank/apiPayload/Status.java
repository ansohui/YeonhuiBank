package com.db.bank.apiPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@RequiredArgsConstructor(access = PRIVATE)
public enum Status {
    TEMP_SUCCESS("200", "SUCCESS", "임시 API 접근에 성공했습니다."),
    //계좌
    ACCOUNT_FORBIDDEN("403", "FAILURE", "접근 불가 계좌입니다."),
    ACCOUNT_ALREADY_PRESENT("409", "FAILURE", "이미 존재하는 계좌입니다."),
    ACCOUNT_NON_PRESENT("404", "FAILURE", "존재하지 않는 계좌입니다."),
    //로그
    INVALID_LOG_ARGUMENT("404", "FAILURE", "로그 기록을 위한 transaction/account/actorUser는 null일 수 없습니다."),
    //사용자
    USER_NON_PRESENT("404", "FAILURE", "존재하지 않는 사용자입니다.");
    private final String code;
    private final String result;
    private final String message;
}
