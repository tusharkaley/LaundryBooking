package com.laundrybooking.model;

/**
 * Model for responses from APIs
 */
public class Response {
    private final String payload;

    private final String errorMessage;

    private final int responseCode;

    public Response(final String payload, final String errorMessage, final int responseCode) {
        this.payload = payload;
        this.errorMessage = errorMessage;
        this.responseCode = responseCode;
    }

    public String getPayload() {
        return payload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getResponseCode() {
        return responseCode;
    }
}

