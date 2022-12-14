package com.laundrybooking.builder;

import com.laundrybooking.model.Response;

public class ResponseBuilder {

    public Response buildSuccessResponse(final String payload){
        return new Response(payload, "", 200);
    }

    public Response buildErrorResponse(final String errorMessage, final int errorCode){
        return new Response("", errorMessage, errorCode);
    }
}
