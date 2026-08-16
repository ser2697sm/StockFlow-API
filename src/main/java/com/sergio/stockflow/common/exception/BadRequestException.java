package com.sergio.stockflow.common.exception;

/*
  lo que envías está mal
*/

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
