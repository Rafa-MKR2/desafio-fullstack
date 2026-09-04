package com.desafio.resource;

import com.desafio.dto.AulaRequest;
import com.desafio.dto.AulaResponse;
import com.desafio.entity.Aula;
import com.desafio.service.AulaService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/aulas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AulaResource {

    @Inject
    AulaService aulaService;

    @POST
    public Response criar(@Valid AulaRequest request) {
        Aula aula = aulaService.criar(request);
        return Response.created(URI.create("/aulas/" + aula.getId()))
                .entity(AulaResponse.from(aula))
                .build();
    }

    @GET
    public List<AulaResponse> listar(@QueryParam("disciplinaId") Long disciplinaId,
                                     @QueryParam("professorId") Long professorId,
                                     @QueryParam("diaSemana") String diaSemana) {
        return aulaService.listarComFiltros(disciplinaId, professorId, diaSemana)
                .stream()
                .map(AulaResponse::from)
                .toList();
    }

    @GET
    @Path("/{id}")
    public AulaResponse buscar(@PathParam("id") Long id) {
        return AulaResponse.from(aulaService.buscarPorId(id));
    }

    @PUT
    @Path("/{id}")
    public AulaResponse atualizar(@PathParam("id") Long id, @Valid AulaRequest request) {
        return AulaResponse.from(aulaService.atualizar(id, request));
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") Long id) {
        aulaService.excluir(id);
        return Response.noContent().build();
    }
}