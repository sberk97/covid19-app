package com.example.covid19countryinfo.misc;

public class NoCasesException extends Exception {

    public NoCasesException(String message) {
        super(message);
    }

    public NoCasesException(String message, Throwable cause) {
        super(message, cause);
    }

}
