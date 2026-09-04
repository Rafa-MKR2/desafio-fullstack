package com.desafio.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class IllegalStateExceptionMapper implements ExceptionMapper<IllegalStateException> {

    @Override
    public Response toResponse(IllegalStateException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("illegal_state", exception.getMessage()))
                .build();
    }
}