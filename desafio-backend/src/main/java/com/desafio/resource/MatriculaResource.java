package com.desafio.resource;

import com.desafio.dto.MatriculaRequest;
import com.desafio.dto.MatriculaResponse;
import com.desafio.entity.Aluno;
import com.desafio.entity.Matricula;
import com.desafio.exception.NotFoundException;
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
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;

@Tag(name = "Matrículas", description = "Matrícula de alunos em aulas")
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
    @Operation(summary = "Matricula o aluno autenticado em uma aula",
            description = "O aluno é resolvido pelo token JWT; aplica as regras de vagas, duplicidade e choque de horário")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Matrícula criada",
                    content = @Content(schema = @Schema(implementation = MatriculaResponse.class))),
            @APIResponse(responseCode = "400", description = "Requisição inválida (validação)"),
            @APIResponse(responseCode = "404", description = "Aluno sem cadastro ou aula inexistente"),
            @APIResponse(responseCode = "409", description = "Matrícula duplicada, vagas esgotadas ou choque de horário com aula já matriculada")
    })
    public Response matricular(@Valid MatriculaRequest request) {
        Aluno aluno = alunoAtual();
        Matricula matricula = matriculaService.matricular(aluno.getId(), request.getAulaId());
        MatriculaResponse response = new MatriculaResponse(
                matricula.getId(), matricula.getAluno().getId(), matricula.getAula().getId());
        return Response.created(URI.create("/matriculas/" + matricula.getId())).entity(response).build();
    }

    private Aluno alunoAtual() {
        String emailClaim = null;
        if (securityIdentity.getPrincipal() instanceof JsonWebToken jwt) {
            emailClaim = jwt.getClaim("email");
        }
        final String email = (emailClaim == null || emailClaim.isBlank())
                ? securityIdentity.getPrincipal().getName()
                : emailClaim;
        return alunoRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        "Aluno não encontrado para o usuário autenticado: " + email));
    }
}