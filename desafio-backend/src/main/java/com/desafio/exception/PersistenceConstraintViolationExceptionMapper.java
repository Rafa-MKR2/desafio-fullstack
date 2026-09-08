package com.desafio.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.hibernate.exception.ConstraintViolationException;

/**
 * Violação de constraint de banco (ex.: unicidade de matrícula) em corrida.
 * Diferente do Bean Validation (jakarta.validation.ConstraintViolationException).
 * Vira 409 em vez de 500.
 */
@Provider
public class PersistenceConstraintViolationExceptionMapper
        implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("constraint_violation",
                        "Operação conflita com uma restrição de dados. Tente novamente."))
                .build();
    }
}