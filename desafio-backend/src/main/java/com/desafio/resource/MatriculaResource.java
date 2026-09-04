package com.desafio.resource;

import com.desafio.dto.MatriculaRequest;
import com.desafio.dto.MatriculaResponse;
import com.desafio.entity.Aluno;
import com.desafio.entity.Matricula;
import com.desafio.repository.AlunoRepository;
import com.desafio.service.MatriculaService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
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
@RolesAllowed("aluno")
public class MatriculaResource {

    @Inject
    MatriculaService matriculaService;

    @Inject
    AlunoRepository alunoRepository;

    @Inject
    SecurityIdentity securityIdentity;

    @POST
    public Response matricular(@Valid MatriculaRequest request) {
        Aluno aluno = alunoAtual();
        Matricula matricula = matriculaService.matricular(aluno.getId(), request.getAulaId());
        MatriculaResponse response = new MatriculaResponse(
                matricula.getId(), matricula.getAluno().getId(), matricula.getAula().getId());
        return Response.created(URI.create("/matriculas/" + matricula.getId())).entity(response).build();
    }

    private Aluno alunoAtual() {
        String email = securityIdentity.getPrincipal().getName();
        return alunoRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aluno não encontrado para o usuário autenticado: " + email));
    }
}