import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import type { Aula, AulaPayload } from '../../../shared/models/aula';
import type {
  Curso,
  Disciplina,
  Horario,
  Professor,
} from '../../../shared/models/catalogos';
import { AulaService } from '../../../shared/services/aula.service';
import { CatalogoService } from '../../../shared/services/catalogo.service';

type TipoMensagem = 'sucesso' | 'erro';

interface Mensagem {
  tipo: TipoMensagem;
  texto: string;
}

/**
 * Tela do coordenador: CRUD de aulas (POST/PUT/DELETE /aulas) com filtros,
 * formulário de criação/edição e exclusão com confirmação em dois cliques.
 */
@Component({
  selector: 'app-gestao-aulas',
  imports: [FormsModule],
  templateUrl: './gestao-aulas.component.html',
  styleUrl: './gestao-aulas.component.css',
})
export class GestaoAulasComponent implements OnInit {
  private readonly aulaService = inject(AulaService);
  private readonly catalogoService = inject(CatalogoService);
  private readonly destroyRef = inject(DestroyRef);

  carregando = signal(true);
  erroLista = signal('');
  aulas = signal<Aula[]>([]);
  disciplinas = signal<Disciplina[]>([]);
  professores = signal<Professor[]>([]);
  horarios = signal<Horario[]>([]);
  cursos = signal<Curso[]>([]);
  dias = signal<string[]>([]);

  filtroDisciplinaId = signal<number | null>(null);
  filtroProfessorId = signal<number | null>(null);
  filtroDia = signal<string | null>(null);

  formAberto = signal(false);
  aulaEditando = signal<Aula | null>(null);
  salvando = signal(false);
  exclusaoPendenteId = signal<number | null>(null);
  mensagem = signal<Mensagem | null>(null);

  // Campos do formulário de criação/edição.
  formDisciplinaId = signal<number | null>(null);
  formProfessorId = signal<number | null>(null);
  formHorarioId = signal<number | null>(null);
  formVagas = signal<number | null>(null);
  formCursoIds = signal<number[]>([]);

  /** Total de vagas oferecidas nas aulas listadas. */
  readonly vagasTotais = computed(() =>
    this.aulas().reduce((soma, a) => soma + a.vagas, 0),
  );

  /** Total de vagas ocupadas nas aulas listadas. */
  readonly vagasOcupadasTotais = computed(() =>
    this.aulas().reduce((soma, a) => soma + a.vagasOcupadas, 0),
  );

  /** Professores que lecionam a disciplina selecionada no formulário. */
  readonly professoresDaDisciplina = computed(() => {
    const disciplinaId = this.formDisciplinaId();
    if (disciplinaId == null) {
      return [];
    }
    return this.professores().filter((p) =>
      p.disciplinaIds.includes(disciplinaId),
    );
  });

  ngOnInit(): void {
    this.carregarCatalogos();
    this.carregarAulas();
  }

  private carregarCatalogos(): void {
    this.catalogoService
      .listarDisciplinas()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (d) => this.disciplinas.set(d) });

    this.catalogoService
      .listarProfessores()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (p) => this.professores.set(p) });

    this.catalogoService
      .listarHorarios()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (horarios) => {
          this.horarios.set(horarios);
          this.dias.set([...new Set(horarios.map((h) => h.diaSemana))]);
        },
      });

    this.catalogoService
      .listarCursos()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: (cursos) => this.cursos.set(cursos) });
  }

  carregarAulas(): void {
    this.carregando.set(true);
    this.erroLista.set('');
    this.aulaService
      .listar({
        disciplinaId: this.filtroDisciplinaId(),
        professorId: this.filtroProfessorId(),
        diaSemana: this.filtroDia(),
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (aulas) => {
          this.aulas.set(aulas);
          this.carregando.set(false);
        },
        error: () => {
          this.erroLista.set(
            'Não foi possível carregar as aulas. Verifique se o backend está no ar.',
          );
          this.carregando.set(false);
        },
      });
  }

  mudarFiltroDisciplina(valor: number | null): void {
    this.filtroDisciplinaId.set(valor);
    this.carregarAulas();
  }

  mudarFiltroProfessor(valor: number | null): void {
    this.filtroProfessorId.set(valor);
    this.carregarAulas();
  }

  mudarFiltroDia(valor: string | null): void {
    this.filtroDia.set(valor);
    this.carregarAulas();
  }

  limparFiltros(): void {
    this.filtroDisciplinaId.set(null);
    this.filtroProfessorId.set(null);
    this.filtroDia.set(null);
    this.carregarAulas();
  }

  abrirNovo(): void {
    this.aulaEditando.set(null);
    this.formDisciplinaId.set(null);
    this.formProfessorId.set(null);
    this.formHorarioId.set(null);
    this.formVagas.set(null);
    this.formCursoIds.set([]);
    this.formAberto.set(true);
  }

  editar(aula: Aula): void {
    this.aulaEditando.set(aula);
    this.formDisciplinaId.set(aula.disciplinaId);
    this.formProfessorId.set(aula.professorId);
    this.formHorarioId.set(aula.horarioId);
    this.formVagas.set(aula.vagas);
    this.formCursoIds.set(aula.cursoIds ?? []);
    this.formAberto.set(true);
  }

  cancelar(): void {
    this.formAberto.set(false);
    this.aulaEditando.set(null);
  }

  mudarFormDisciplina(valor: number | null): void {
    this.formDisciplinaId.set(valor);
    // Professor deve lecionar a disciplina escolhida.
    this.formProfessorId.set(null);
  }

  alternarCurso(id: number, selecionado: boolean): void {
    const atuais = this.formCursoIds();
    this.formCursoIds.set(
      selecionado
        ? [...new Set([...atuais, id])]
        : atuais.filter((c) => c !== id),
    );
  }

  salvar(): void {
    const disciplinaId = this.formDisciplinaId();
    const professorId = this.formProfessorId();
    const horarioId = this.formHorarioId();
    const vagas = this.formVagas();

    if (disciplinaId == null || professorId == null || horarioId == null) {
      this.mensagem.set({
        tipo: 'erro',
        texto: 'Preencha disciplina, professor e horário antes de salvar.',
      });
      return;
    }
    if (vagas == null || !Number.isInteger(vagas) || vagas < 1) {
      this.mensagem.set({
        tipo: 'erro',
        texto: 'Informe um número de vagas inteiro, no mínimo 1.',
      });
      return;
    }
    const editando = this.aulaEditando();
    if (editando != null && vagas < editando.vagasOcupadas) {
      this.mensagem.set({
        tipo: 'erro',
        texto: `O número de vagas não pode ser menor que o de matriculados (${editando.vagasOcupadas}).`,
      });
      return;
    }

    this.salvando.set(true);
    const cursoIds = this.formCursoIds();
    const payload: AulaPayload = {
      disciplinaId,
      professorId,
      horarioId,
      vagas,
      ...(cursoIds.length > 0 ? { cursoIds } : {}),
    };
    const operacao =
      editando != null
        ? this.aulaService.atualizar(editando.id, payload)
        : this.aulaService.criar(payload);

    operacao.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (aula) => {
        this.salvando.set(false);
        this.formAberto.set(false);
        this.aulaEditando.set(null);
        this.mensagem.set({
          tipo: 'sucesso',
          texto: `Aula ${editando != null ? 'atualizada' : 'criada'}: ${aula.disciplinaNome} (${aula.horarioDiaSemana}, ${aula.horarioHoraInicio}).`,
        });
        this.carregarAulas();
      },
      error: (erro: unknown) => {
        this.salvando.set(false);
        this.mensagem.set({ tipo: 'erro', texto: this.textoDeErro(erro) });
      },
    });
  }

  excluir(aula: Aula): void {
    if (this.exclusaoPendenteId() !== aula.id) {
      // Primeiro clique: pede confirmação.
      this.exclusaoPendenteId.set(aula.id);
      return;
    }
    this.exclusaoPendenteId.set(null);
    this.aulaService
      .excluir(aula.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.mensagem.set({
            tipo: 'sucesso',
            texto: `Aula de ${aula.disciplinaNome} excluída.`,
          });
          this.carregarAulas();
        },
        error: (erro: unknown) =>
          this.mensagem.set({ tipo: 'erro', texto: this.textoDeErro(erro) }),
      });
  }

  fecharMensagem(): void {
    this.mensagem.set(null);
  }

  /** Porcentagem de ocupação de uma aula (0–100), para a barra de vagas. */
  porcentagemOcupada(aula: Aula): number {
    if (aula.vagas <= 0) {
      return 0;
    }
    return Math.round((aula.vagasOcupadas / aula.vagas) * 100);
  }

  private textoDeErro(erro: unknown): string {
    if (erro instanceof HttpErrorResponse) {
      const corpo = erro.error as { message?: string } | null;
      if (corpo?.message) {
        return corpo.message;
      }
      if (erro.status === 0) {
        return 'Não foi possível conectar ao backend. Verifique se ele está no ar.';
      }
      return `Não foi possível concluir a operação (erro ${erro.status}).`;
    }
    return 'Não foi possível concluir a operação.';
  }
}