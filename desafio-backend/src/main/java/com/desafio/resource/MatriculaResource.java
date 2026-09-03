package com.desafio.resource;

import com.desafio.dto.MatriculaRequest;
import com.desafio.dto.MatriculaResponse;
import com.desafio.entity.Matricula;
import com.desafio.service.MatriculaService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("/matriculas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MatriculaResource {

    @Inject
    MatriculaService matriculaService;

    @POST
    public Response matricular(@Valid MatriculaRequest request) {
        Matricula matricula = matriculaService.matricular(request.getAlunoId(), request.getAulaId());
        MatriculaResponse response = new MatriculaResponse(
                matricula.getId(), matricula.getAluno().getId(), matricula.getAula().getId());
        return Response.created(URI.create("/matriculas/" + matricula.getId())).entity(response).build();
    }
}