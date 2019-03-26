package com.jd.journalq.broker.kafka.coordinator.exception;

import com.jd.journalq.broker.kafka.exception.KafkaException;

/**
 * CoordinatorException
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/7
 */
public class CoordinatorException extends KafkaException {

    public CoordinatorException() {
    }

    public CoordinatorException(String message) {
        super(message);
    }

    public CoordinatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CoordinatorException(Throwable cause) {
        super(cause);
    }

    public CoordinatorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}