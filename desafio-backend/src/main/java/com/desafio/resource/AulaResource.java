package com.desafio.resource;

import com.desafio.dto.AulaRequest;
import com.desafio.dto.AulaResponse;
import com.desafio.entity.Aula;
import com.desafio.service.AulaService;
import jakarta.annotation.security.RolesAllowed;
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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;

@Tag(name = "Aulas", description = "Gestão de aulas pelo coordenador e consulta por alunos")
@Path("/aulas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"aluno", "coordenador"})
public class AulaResource {

    @Inject
    AulaService aulaService;

    @POST
    @RolesAllowed("coordenador")
    @Operation(summary = "Cria uma aula",
            description = "Valida as referências (disciplina, professor, horário) e o conflito de horário do professor")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Aula criada",
                    content = @Content(schema = @Schema(implementation = AulaResponse.class))),
            @APIResponse(responseCode = "400", description = "Requisição inválida (validação ou referência inexistente)"),
            @APIResponse(responseCode = "409", description = "Professor já possui aula no horário informado")
    })
    public Response criar(@Valid AulaRequest request) {
        Aula aula = aulaService.criar(request);
        return Response.created(URI.create("/aulas/" + aula.getId()))
                .entity(AulaResponse.from(aula))
                .build();
    }

    @GET
    @Operation(summary = "Lista aulas",
            description = "Retorna todas as aulas, opcionalmente filtradas por disciplina, professor e dia da semana")
    @APIResponse(responseCode = "200", description = "Lista de aulas",
            content = @Content(schema = @Schema(implementation = AulaResponse.class)))
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
    @Operation(summary = "Busca uma aula pelo id")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Aula encontrada",
                    content = @Content(schema = @Schema(implementation = AulaResponse.class))),
            @APIResponse(responseCode = "404", description = "Aula não encontrada")
    })
    public AulaResponse buscar(@PathParam("id") Long id) {
        return AulaResponse.from(aulaService.buscarPorId(id));
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("coordenador")
    @Operation(summary = "Atualiza uma aula",
            description = "Atualiza as referências e o número de vagas, respeitando os matriculados atuais")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Aula atualizada",
                    content = @Content(schema = @Schema(implementation = AulaResponse.class))),
            @APIResponse(responseCode = "400", description = "Requisição inválida ou vagas menores que o número de matriculados"),
            @APIResponse(responseCode = "404", description = "Aula não encontrada"),
            @APIResponse(responseCode = "409", description = "Professor já possui aula no horário informado")
    })
    public AulaResponse atualizar(@PathParam("id") Long id, @Valid AulaRequest request) {
        return AulaResponse.from(aulaService.atualizar(id, request));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("coordenador")
    @Operation(summary = "Exclui uma aula",
            description = "Exclui a aula apenas se não houver matrículas vinculadas")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Aula excluída"),
            @APIResponse(responseCode = "400", description = "Aula com matrículas vinculadas"),
            @APIResponse(responseCode = "404", description = "Aula não encontrada")
    })
    public Response excluir(@PathParam("id") Long id) {
        aulaService.excluir(id);
        return Response.noContent().build();
    }
}