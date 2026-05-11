package com.zhm.springboot.userservice.blockchain.exception;

/**
 * IPFS 操作异常
 */
public class IpfsException extends RuntimeException {

    private final ErrorCode errorCode;

    public enum ErrorCode {
        UPLOAD_FAILED,
        DOWNLOAD_FAILED,
        PIN_FAILED,
        UNPIN_FAILED,
        FILE_TOO_LARGE,
        INVALID_FILE_TYPE,
        CID_NOT_FOUND,
        GATEWAY_UNAVAILABLE,
        NETWORK_ERROR,
        TIMEOUT,
        UNKNOWN_ERROR
    }

    public IpfsException(String message) {
        super(message);
        this.errorCode = ErrorCode.UNKNOWN_ERROR;
    }

    public IpfsException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public IpfsException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.UNKNOWN_ERROR;
    }

    public IpfsException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
