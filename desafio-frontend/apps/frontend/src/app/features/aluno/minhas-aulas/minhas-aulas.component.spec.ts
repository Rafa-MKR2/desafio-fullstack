import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import type { Aula } from '../../../shared/models/aula';
import { MinhasAulasComponent } from './minhas-aulas.component';

const aulaDisponivel: Aula = {
  id: 1,
  disciplinaId: 10,
  disciplinaNome: 'Matemática',
  professorId: 20,
  professorNome: 'Prof. Ana',
  horarioId: 30,
  horarioDiaSemana: 'Segunda',
  horarioHoraInicio: '08:00',
  horarioHoraFim: '10:00',
  vagas: 30,
  vagasOcupadas: 10,
  vagasRestantes: 20,
};

const aulaMatriculada: Aula = {
  ...aulaDisponivel,
  id: 2,
  disciplinaNome: 'Física',
  horarioDiaSemana: 'Terça',
  vagas: 30,
  vagasOcupadas: 25,
  vagasRestantes: 5,
};

describe('MinhasAulasComponent', () => {
  let fixture: ComponentFixture<MinhasAulasComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MinhasAulasComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    fixture = TestBed.createComponent(MinhasAulasComponent);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  /** Descarrega as chamadas iniciais (catálogos + listas). */
  function liberarCargaInicial(): void {
    http.expectOne((r) => r.method === 'GET' && r.url.endsWith('/disciplinas')).flush([]);
    http.expectOne((r) => r.method === 'GET' && r.url.endsWith('/professores')).flush([]);
    http.expectOne((r) => r.method === 'GET' && r.url.endsWith('/horarios')).flush([]);
    http.expectOne((r) => r.method === 'GET' && r.url.endsWith('/matriculas/aulas')).flush([aulaMatriculada]);
    http.expectOne((r) => r.method === 'GET' && r.url.endsWith('/aulas')).flush([aulaDisponivel, aulaMatriculada]);
  }

  it('lista aulas e realiza matrícula com aviso de sucesso', () => {
    fixture.detectChanges();
    liberarCargaInicial();
    fixture.detectChanges();

    const botao = fixture.nativeElement.querySelector(
      'button.botao-primario',
    ) as HTMLButtonElement | null;
    expect(botao).not.toBeNull();
    botao?.click();
    fixture.detectChanges();

    const post = http.expectOne((r) => r.method === 'POST' && r.url.endsWith('/matriculas'));
    expect(post.request.body).toEqual({ aulaId: 1 });
    post.flush({ id: 99, alunoId: 1, aulaId: 1 }, { status: 201, statusText: 'Created' });

    // O sucesso recarrega as listas.
    http.expectOne((r) => r.method === 'GET' && r.url.endsWith('/matriculas/aulas')).flush([
      aulaMatriculada,
      aulaDisponivel,
    ]);
    http.expectOne((r) => r.method === 'GET' && r.url.endsWith('/aulas')).flush([
      aulaDisponivel,
      aulaMatriculada,
    ]);
    fixture.detectChanges();

    const aviso = fixture.nativeElement.querySelector('.aviso-sucesso') as HTMLElement | null;
    expect(aviso?.textContent).toContain('Matrícula realizada em Matemática');
  });

  it('exibe a mensagem do backend quando a matrícula falha (409)', () => {
    fixture.detectChanges();
    liberarCargaInicial();
    fixture.detectChanges();

    const botao = fixture.nativeElement.querySelector(
      'button.botao-primario',
    ) as HTMLButtonElement | null;
    botao?.click();
    fixture.detectChanges();

    const post = http.expectOne((r) => r.method === 'POST' && r.url.endsWith('/matriculas'));
    post.flush(
      { code: 'business_error', message: 'Aluno já matriculado nesta aula' },
      { status: 409, statusText: 'Conflict' },
    );
    fixture.detectChanges();

    const aviso = fixture.nativeElement.querySelector('.aviso-erro') as HTMLElement | null;
    expect(aviso?.textContent).toContain('Aluno já matriculado nesta aula');
  });
});
