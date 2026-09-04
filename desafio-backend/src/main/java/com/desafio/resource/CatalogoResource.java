package com.desafio.resource;

import com.desafio.dto.CursoResponse;
import com.desafio.dto.DisciplinaResponse;
import com.desafio.dto.HorarioResponse;
import com.desafio.dto.ProfessorResponse;
import com.desafio.repository.CursoRepository;
import com.desafio.repository.DisciplinaRepository;
import com.desafio.repository.HorarioRepository;
import com.desafio.repository.ProfessorRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Catálogos", description = "Leitura de disciplinas, professores, horários e cursos")
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"aluno", "coordenador"})
public class CatalogoResource {

    @Inject
    DisciplinaRepository disciplinaRepository;

    @Inject
    ProfessorRepository professorRepository;

    @Inject
    HorarioRepository horarioRepository;

    @Inject
    CursoRepository cursoRepository;

    @GET
    @Path("/disciplinas")
    @Operation(summary = "Lista disciplinas")
    @APIResponse(responseCode = "200", description = "Lista de disciplinas",
            content = @Content(schema = @Schema(implementation = DisciplinaResponse.class)))
    public List<DisciplinaResponse> listarDisciplinas() {
        return disciplinaRepository.listAll().stream()
                .map(DisciplinaResponse::from)
                .toList();
    }

    @GET
    @Path("/professores")
    @Operation(summary = "Lista professores")
    @APIResponse(responseCode = "200", description = "Lista de professores",
            content = @Content(schema = @Schema(implementation = ProfessorResponse.class)))
    public List<ProfessorResponse> listarProfessores() {
        return professorRepository.listAll().stream()
                .map(ProfessorResponse::from)
                .toList();
    }

    @GET
    @Path("/horarios")
    @Operation(summary = "Lista horários")
    @APIResponse(responseCode = "200", description = "Lista de horários",
            content = @Content(schema = @Schema(implementation = HorarioResponse.class)))
    public List<HorarioResponse> listarHorarios() {
        return horarioRepository.listAll().stream()
                .map(HorarioResponse::from)
                .toList();
    }

    @GET
    @Path("/cursos")
    @Operation(summary = "Lista cursos")
    @APIResponse(responseCode = "200", description = "Lista de cursos",
            content = @Content(schema = @Schema(implementation = CursoResponse.class)))
    public List<CursoResponse> listarCursos() {
        return cursoRepository.listAll().stream()
                .map(CursoResponse::from)
                .toList();
    }
}