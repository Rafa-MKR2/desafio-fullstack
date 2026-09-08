package com.desafio.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Concorrência: a entidade foi modificada por outra transação após a leitura
 * (race de escrita). Vira 409 em vez de 500.
 */
@Provider
public class OptimisticLockExceptionMapper implements ExceptionMapper<OptimisticLockException> {

    @Override
    public Response toResponse(OptimisticLockException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("concurrent_modification",
                        "Registro alterado por outra requisição. Tente novamente."))
                .build();
    }
}