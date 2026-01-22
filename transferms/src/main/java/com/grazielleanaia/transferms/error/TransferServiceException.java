package com.grazielleanaia.transferms.error;

public class TransferServiceException extends RuntimeException {
    public TransferServiceException(String message) {
        super(message);
    }

    public TransferServiceException(Throwable cause) {
        super(cause);
    }
}
