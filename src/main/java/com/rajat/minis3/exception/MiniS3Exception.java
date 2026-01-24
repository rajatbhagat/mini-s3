package com.rajat.minis3.exception;

public class MiniS3Exception extends Exception{
    public MiniS3Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public MiniS3Exception(String message) {
        super(message);
    }
}
