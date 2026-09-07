import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import type { Aula } from '../../../shared/models/aula';
import type {
  Disciplina,
  Horario,
  Professor,
} from '../../../shared/models/catalogos';
import { GestaoAulasComponent } from './gestao-aulas.component';

const disciplina: Disciplina = { id: 10, nome: 'Matemática', cargaHoraria: 60 };
const professor: Professor = { id: 20, nome: 'Ana Paula', disciplinaIds: [10] };
const horario: Horario = {
  id: 30,
  diaSemana: 'Segunda',
  horaInicio: '08:00',
  horaFim: '10:00',
};

const aula: Aula = {
  id: 1,
  disciplinaId: 10,
  disciplinaNome: 'Matemática',
  professorId: 20,
  professorNome: 'Ana Paula',
  horarioId: 30,
  horarioDiaSemana: 'Segunda',
  horarioHoraInicio: '08:00',
  horarioHoraFim: '10:00',
  vagas: 30,
  vagasOcupadas: 5,
  vagasRestantes: 25,
};

describe('GestaoAulasComponent', () => {
  let fixture: ComponentFixture<GestaoAulasComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestaoAulasComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    fixture = TestBed.createComponent(GestaoAulasComponent);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  /** Descarrega as chamadas iniciais (catálogos + listagem de aulas). */
  function liberarCargaInicial(): void {
    http.expectOne((r) => r.method === 'GET' && r.url.endsWith('/disciplinas')).flush([disciplina]);
    http.expectOne((r) => r.method === 'GET' && r.url.endsWith('/professores')).flush([professor]);
    http.expectOne((r) => r.method === 'GET' && r.url.endsWith('/horarios')).flush([horario]);
    http.expectOne((r) => r.method === 'GET' && r.url.endsWith('/aulas')).flush([aula]);
  }

  /** Encontra um botão pelo texto visível. */
  function botaoPorTexto(texto: string): HTMLButtonElement {
    const botao = [...fixture.nativeElement.querySelectorAll('button')].find(
      (b) => (b as HTMLButtonElement).textContent?.includes(texto),
    ) as HTMLButtonElement | undefined;
    expect(botao).toBeDefined();
    return botao as HTMLButtonElement;
  }

  it('cria uma aula e exibe aviso de sucesso', () => {
    fixture.detectChanges();
    liberarCargaInicial();
    fixture.detectChanges();

    // Abre o formulário de nova aula.
    botaoPorTexto('Nova aula').click();
    fixture.detectChanges();

    // Preenche o formulário (professor filtrado pela disciplina escolhida).
    fixture.componentInstance.mudarFormDisciplina(10);
    fixture.componentInstance.formProfessorId.set(20);
    fixture.componentInstance.formHorarioId.set(30);
    fixture.componentInstance.formVagas.set(25);
    fixture.detectChanges();

    botaoPorTexto('Criar aula').click();
    fixture.detectChanges();

    const post = http.expectOne((r) => r.method === 'POST' && r.url.endsWith('/aulas'));
    expect(post.request.body).toEqual({
      disciplinaId: 10,
      professorId: 20,
      horarioId: 30,
      vagas: 25,
    });
    post.flush({ ...aula, vagas: 25, vagasOcupadas: 0, vagasRestantes: 25 }, {
      status: 201,
      statusText: 'Created',
    });

    // O sucesso recarrega a listagem.
    http.expectOne((r) => r.method === 'GET' && r.url.endsWith('/aulas')).flush([aula]);
    fixture.detectChanges();

    const aviso = fixture.nativeElement.querySelector('.aviso-sucesso') as HTMLElement | null;
    expect(aviso?.textContent).toContain('Aula criada');
  });

  it('edita uma aula com o formulário pré-preenchido', () => {
    fixture.detectChanges();
    liberarCargaInicial();
    fixture.detectChanges();

    botaoPorTexto('Editar').click();
    fixture.detectChanges();

    // O formulário vem pré-preenchido com os dados da aula.
    const comp = fixture.componentInstance;
    expect(comp.formDisciplinaId()).toBe(10);
    expect(comp.formProfessorId()).toBe(20);
    expect(comp.formHorarioId()).toBe(30);
    expect(comp.formVagas()).toBe(30);

    comp.formVagas.set(35);
    fixture.detectChanges();

    botaoPorTexto('Salvar alterações').click();
    fixture.detectChanges();

    const put = http.expectOne((r) => r.method === 'PUT' && r.url.endsWith('/aulas/1'));
    expect(put.request.body).toEqual({
      disciplinaId: 10,
      professorId: 20,
      horarioId: 30,
      vagas: 35,
    });
    put.flush({ ...aula, vagas: 35, vagasRestantes: 30 });

    http.expectOne((r) => r.method === 'GET' && r.url.endsWith('/aulas')).flush([aula]);
    fixture.detectChanges();

    const aviso = fixture.nativeElement.querySelector('.aviso-sucesso') as HTMLElement | null;
    expect(aviso?.textContent).toContain('Aula atualizada');
  });

  it('exclui uma aula somente após confirmação', () => {
    fixture.detectChanges();
    liberarCargaInicial();
    fixture.detectChanges();

    // Primeiro clique: vira pedido de confirmação, sem chamada HTTP.
    botaoPorTexto('Excluir').click();
    fixture.detectChanges();
    expect(botaoPorTexto('Confirmar exclusão')).toBeDefined();

    // Segundo clique: executa o DELETE.
    botaoPorTexto('Confirmar exclusão').click();
    fixture.detectChanges();

    const del = http.expectOne((r) => r.method === 'DELETE' && r.url.endsWith('/aulas/1'));
    del.flush(null, { status: 204, statusText: 'No Content' });

    http.expectOne((r) => r.method === 'GET' && r.url.endsWith('/aulas')).flush([]);
    fixture.detectChanges();

    const aviso = fixture.nativeElement.querySelector('.aviso-sucesso') as HTMLElement | null;
    expect(aviso?.textContent).toContain('Aula de Matemática excluída');
  });

  it('exibe a mensagem do backend quando o professor já tem aula no horário (409)', () => {
    fixture.detectChanges();
    liberarCargaInicial();
    fixture.detectChanges();

    botaoPorTexto('Nova aula').click();
    fixture.detectChanges();

    fixture.componentInstance.mudarFormDisciplina(10);
    fixture.componentInstance.formProfessorId.set(20);
    fixture.componentInstance.formHorarioId.set(30);
    fixture.componentInstance.formVagas.set(25);
    fixture.detectChanges();

    botaoPorTexto('Criar aula').click();
    fixture.detectChanges();

    const post = http.expectOne((r) => r.method === 'POST' && r.url.endsWith('/aulas'));
    post.flush(
      { code: 'business_error', message: 'Professor já possui aula neste horário (aula: 2)' },
      { status: 409, statusText: 'Conflict' },
    );
    fixture.detectChanges();

    const aviso = fixture.nativeElement.querySelector('.aviso-erro') as HTMLElement | null;
    expect(aviso?.textContent).toContain('Professor já possui aula neste horário');
  });
});