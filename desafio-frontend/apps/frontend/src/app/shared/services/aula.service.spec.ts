import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import type { Aula, AulaPayload } from '../models/aula';
import { AulaService } from './aula.service';

describe('AulaService', () => {
  let service: AulaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AulaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('listar omite filtros nulos ou em branco da query string', () => {
    let resultado: Aula[] | undefined;
    service
      .listar({ disciplinaId: 1, professorId: null, diaSemana: '  ' })
      .subscribe((aulas) => (resultado = aulas));

    const req = http.expectOne((r) => r.method === 'GET' && r.url === `${environment.apiUrl}/aulas`);
    expect(req.request.params.get('disciplinaId')).toBe('1');
    expect(req.request.params.has('professorId')).toBeFalsy();
    expect(req.request.params.has('diaSemana')).toBeFalsy();
    req.flush([]);

    expect(resultado).toEqual([]);
  });

  it('criar envia POST /aulas com o payload de criação', () => {
    const payload: AulaPayload = {
      disciplinaId: 2,
      professorId: 3,
      horarioId: 4,
      vagas: 30,
    };
    let resultado: Aula | undefined;
    service.criar(payload).subscribe((aula) => (resultado = aula));

    const req = http.expectOne((r) => r.method === 'POST' && r.url === `${environment.apiUrl}/aulas`);
    expect(req.request.body).toEqual(payload);
    req.flush({ id: 9, ...payload } as Aula);

    expect(resultado?.id).toBe(9);
  });
});
