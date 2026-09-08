package com.desafio.exception;

import jakarta.persistence.PessimisticLockException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Lock pessimista não adquirido (timeout) em operação concorrente de escrita.
 * Vira 409 em vez de 500.
 */
@Provider
public class PessimisticLockExceptionMapper implements ExceptionMapper<PessimisticLockException> {

    @Override
    public Response toResponse(PessimisticLockException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("lock_timeout",
                        "Recurso ocupado por outra transação. Tente novamente."))
                .build();
    }
}