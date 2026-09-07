import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import Keycloak from 'keycloak-js';
import { App } from './app';

/** Instância mínima do Keycloak para renderizar o shell sem rede. */
const fakeKeycloak = {
  authenticated: false,
  tokenParsed: undefined,
} as unknown as Keycloak;

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        { provide: Keycloak, useValue: fakeKeycloak },
      ],
    }).compileComponents();
  });

  it('renderiza o shell com a marca do app', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.marca')?.textContent).toContain(
      'Sistema Acadêmico',
    );
  });
});
