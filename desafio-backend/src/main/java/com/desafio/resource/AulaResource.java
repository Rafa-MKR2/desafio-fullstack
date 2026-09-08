package com.desafio.resource;

import com.desafio.dto.AulaRequest;
import com.desafio.dto.AulaResponse;
import com.desafio.entity.Aula;
import com.desafio.entity.Coordenador;
import com.desafio.exception.NotFoundException;
import com.desafio.repository.CoordenadorRepository;
import com.desafio.service.AulaService;
import io.quarkus.security.identity.SecurityIdentity;
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
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Tag(name = "Aulas", description = "Gestão de aulas pelo coordenador e consulta por alunos")
@Path("/aulas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"aluno", "coordenador"})
public class AulaResource {

    @Inject
    AulaService aulaService;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    CoordenadorRepository coordenadorRepository;

    @POST
    @RolesAllowed("coordenador")
    @Operation(summary = "Cria uma aula",
            description = "Valida as referências (disciplina, professor, horário) e o conflito de horário do professor. A aula fica vinculada ao coordenador autenticado")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Aula criada",
                    content = @Content(schema = @Schema(implementation = AulaResponse.class))),
            @APIResponse(responseCode = "400", description = "Requisição inválida (validação ou referência inexistente)"),
            @APIResponse(responseCode = "409", description = "Professor já possui aula no horário informado")
    })
    public Response criar(@Valid AulaRequest request) {
        Aula aula = aulaService.criar(request, coordenadorAtual().getId());
        return Response.created(URI.create("/aulas/" + aula.getId()))
                .entity(AulaResponse.from(aula))
                .build();
    }

    @GET
    @Operation(summary = "Lista aulas",
            description = "Para o coordenador, retorna apenas as suas aulas; para o aluno, todo o catálogo ativo. Filtros: disciplina, professor, dia, curso, horário e disponibilidade de vagas")
    @APIResponse(responseCode = "200", description = "Lista de aulas",
            content = @Content(schema = @Schema(implementation = AulaResponse.class)))
    public List<AulaResponse> listar(@QueryParam("disciplinaId") Long disciplinaId,
                                     @QueryParam("professorId") Long professorId,
                                     @QueryParam("diaSemana") String diaSemana,
                                     @QueryParam("cursoId") Long cursoId,
                                     @QueryParam("horarioId") Long horarioId,
                                     @QueryParam("vagasDisponiveis") Boolean vagasDisponiveis) {
        List<Aula> aulas = aulaService.listarComFiltros(
                disciplinaId, professorId, diaSemana, cursoId, horarioId, vagasDisponiveis,
                coordenadorIdSeCoordenador());
        Map<Long, Long> matriculados = aulaService.contarMatriculadosPorAula(
                aulas.stream().map(Aula::getId).toList());
        return aulas.stream()
                .map(aula -> AulaResponse.from(aula,
                        matriculados.getOrDefault(aula.getId(), 0L)))
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
        Aula aula = aulaService.buscarPorId(id, coordenadorIdSeCoordenador());
        return AulaResponse.from(aula, aulaService.contarMatriculados(aula.getId()));
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("coordenador")
    @Operation(summary = "Atualiza uma aula",
            description = "Atualiza as referências e o número de vagas, respeitando os matriculados atuais. Apenas a própria aula do coordenador")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Aula atualizada",
                    content = @Content(schema = @Schema(implementation = AulaResponse.class))),
            @APIResponse(responseCode = "400", description = "Requisição inválida ou vagas menores que o número de matriculados"),
            @APIResponse(responseCode = "404", description = "Aula não encontrada"),
            @APIResponse(responseCode = "409", description = "Professor já possui aula no horário informado")
    })
    public AulaResponse atualizar(@PathParam("id") Long id, @Valid AulaRequest request) {
        Aula aula = aulaService.atualizar(id, request, coordenadorAtual().getId());
        return AulaResponse.from(aula, aulaService.contarMatriculados(aula.getId()));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("coordenador")
    @Operation(summary = "Exclui uma aula",
            description = "Exclui logicamente a aula (apenas se não houver matrículas vinculadas). Apenas a própria aula do coordenador")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Aula excluída"),
            @APIResponse(responseCode = "400", description = "Aula com matrículas vinculadas"),
            @APIResponse(responseCode = "404", description = "Aula não encontrada")
    })
    public Response excluir(@PathParam("id") Long id) {
        aulaService.excluir(id, coordenadorAtual().getId());
        return Response.noContent().build();
    }

    private Long coordenadorIdSeCoordenador() {
        if (!securityIdentity.hasRole("coordenador")) {
            return null;
        }
        return coordenadorAtual().getId();
    }

    private Coordenador coordenadorAtual() {
        String email = null;
        if (securityIdentity.getPrincipal() instanceof JsonWebToken jwt) {
            email = jwt.getClaim("email");
        }
        final String resolved = (email == null || email.isBlank())
                ? securityIdentity.getPrincipal().getName()
                : email;
        return coordenadorRepository.findByEmail(resolved)
                .orElseThrow(() -> new NotFoundException(
                        "Coordenador não encontrado para o usuário autenticado: " + resolved));
    }
}