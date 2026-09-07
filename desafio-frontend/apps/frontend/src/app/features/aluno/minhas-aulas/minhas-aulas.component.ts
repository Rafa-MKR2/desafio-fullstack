import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import type { Aula } from '../../../shared/models/aula';
import type { Disciplina, Professor } from '../../../shared/models/catalogos';
import { AulaService } from '../../../shared/services/aula.service';
import { CatalogoService } from '../../../shared/services/catalogo.service';
import { MatriculaService } from '../../../shared/services/matricula.service';

type TipoMensagem = 'sucesso' | 'erro';

interface Mensagem {
  tipo: TipoMensagem;
  texto: string;
}

/**
 * Tela do aluno: lista as aulas em que está matriculado e permite matricular-se
 * em aulas disponíveis (com filtros e feedback dos erros do backend).
 */
@Component({
  selector: 'app-minhas-aulas',
  imports: [FormsModule],
  templateUrl: './minhas-aulas.component.html',
  styleUrl: './minhas-aulas.component.css',
})
export class MinhasAulasComponent implements OnInit {
  private readonly aulaService = inject(AulaService);
  private readonly matriculaService = inject(MatriculaService);
  private readonly catalogoService = inject(CatalogoService);
  private readonly destroyRef = inject(DestroyRef);

  carregando = signal(true);
  erroLista = signal('');
  aulas = signal<Aula[]>([]);
  minhasAulas = signal<Aula[]>([]);
  disciplinas = signal<Disciplina[]>([]);
  professores = signal<Professor[]>([]);
  dias = signal<string[]>([]);
  filtroDisciplinaId = signal<number | null>(null);
  filtroProfessorId = signal<number | null>(null);
  filtroDia = signal<string | null>(null);
  matriculaPendenteId = signal<number | null>(null);
  mensagem = signal<Mensagem | null>(null);

  /** Ids das aulas em que o aluno já está matriculado. */
  readonly idsMatriculadas = computed(() => new Set(this.minhasAulas().map((a) => a.id)));

  /** Quantas aulas disponíveis (com filtros aplicados) ainda têm vagas. */
  readonly aulasComVaga = computed(
    () => this.aulas().filter((a) => a.vagasRestantes > 0).length,
  );

  ngOnInit(): void {
    this.carregarCatalogos();
    this.carregarMinhasAulas();
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
        next: (horarios) => this.dias.set([...new Set(horarios.map((h) => h.diaSemana))]),
      });
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

  carregarMinhasAulas(): void {
    this.matriculaService
      .listarAulas()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (aulas) => this.minhasAulas.set(aulas),
        error: () => this.minhasAulas.set([]),
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

  matricular(aula: Aula): void {
    this.mensagem.set(null);
    this.matriculaPendenteId.set(aula.id);
    this.matriculaService
      .matricular(aula.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.matriculaPendenteId.set(null);
          this.mensagem.set({
            tipo: 'sucesso',
            texto: `Matrícula realizada em ${aula.disciplinaNome} (${aula.horarioDiaSemana}, ${aula.horarioHoraInicio}).`,
          });
          this.carregarMinhasAulas();
          this.carregarAulas();
        },
        error: (erro: unknown) => {
          this.matriculaPendenteId.set(null);
          this.mensagem.set({ tipo: 'erro', texto: this.textoDeErro(erro) });
        },
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
      return `Não foi possível concluir a matrícula (erro ${erro.status}).`;
    }
    return 'Não foi possível concluir a matrícula.';
  }
}
