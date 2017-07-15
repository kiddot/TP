package com.android.server.common.exception;

/**
 * Created by kiddo on 17-7-14.
 */

public class ServiceException extends RuntimeException{
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
