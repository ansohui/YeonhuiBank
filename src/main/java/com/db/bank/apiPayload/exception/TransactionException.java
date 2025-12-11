package com.db.bank.apiPayload.exception;

public class TransactionException extends RuntimeException {
  public TransactionException(String message) {
    super(message);
  }

  public static class TransactionNonExistsException extends TransactionException {
    public TransactionNonExistsException(String message) {
      super(message);
    }
  }
  public static class InsufficientFundsException extends TransactionException {
    public InsufficientFundsException(String message) {
      super(message);
    }
  }
  public static class AccountLockedException extends TransactionException {
    public AccountLockedException(String message) {
      super(message);
    }
  }
  public static class DailyLimitExceededException extends TransactionException {
    public DailyLimitExceededException(String message) {
      super(message);
    }
  }
  public static class IllegalTransferException extends TransactionException {
    public IllegalTransferException(String message) {
      super(message);
    }
  }

}
