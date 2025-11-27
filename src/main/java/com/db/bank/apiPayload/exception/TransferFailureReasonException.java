package com.db.bank.apiPayload.exception;

public class TransferFailureReasonException extends RuntimeException {
  public TransferFailureReasonException(String message) {
    super(message);
  }

  //이미 존재하는 사유코드
  public static class DuplicateReasonCodeException extends TransferFailureReasonException {
      public DuplicateReasonCodeException(String reasonCode) {
          super("이미 존재하는 사유코드입니다: " + reasonCode);
      }
  }
    public static class ReasonCodeNonExistsException extends TransferFailureReasonException {
        public ReasonCodeNonExistsException(String reasonCode) {
            super("이미 존재하는 사유코드입니다: " + reasonCode);
        }
    }

}
