import { TestBed } from '@angular/core/testing';
import Keycloak from 'keycloak-js';
import { AuthService } from './auth.service';

function keycloakComToken(claims: unknown): Keycloak {
  return {
    authenticated: true,
    tokenParsed: claims,
  } as unknown as Keycloak;
}

describe('AuthService', () => {
  it('expõe nome, e-mail e roles do realm a partir do token', () => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: Keycloak,
          useValue: keycloakComToken({
            name: 'João Silva',
            preferred_username: 'joao',
            email: 'joao@email.com',
            realm_access: { roles: ['aluno', 'default-roles-desafio'] },
          }),
        },
      ],
    });
    const auth = TestBed.inject(AuthService);

    expect(auth.authenticated).toBeTruthy();
    expect(auth.displayName).toBe('João Silva');
    expect(auth.email).toBe('joao@email.com');
    expect(auth.roles).toEqual(['aluno', 'default-roles-desafio']);
    expect(auth.isAluno).toBeTruthy();
    expect(auth.isCoordenador).toBeFalsy();
  });

  it('considera usuário não autenticado quando não há sessão', () => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: Keycloak,
          useValue: { authenticated: false, tokenParsed: undefined } as unknown as Keycloak,
        },
      ],
    });
    const auth = TestBed.inject(AuthService);

    expect(auth.authenticated).toBeFalsy();
    expect(auth.roles).toEqual([]);
    expect(auth.isAluno).toBeFalsy();
    expect(auth.displayName).toBe('');
  });
});
