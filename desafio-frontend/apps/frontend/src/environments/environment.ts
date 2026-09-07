/**
 * Configuração de desenvolvimento.
 * Em produção, o arquivo é substituído por environment.prod.ts
 * (fileReplacements no project.json).
 */
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  keycloak: {
    url: 'http://localhost:8081',
    realm: 'desafio',
    clientId: 'frontend',
    silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
  },
};
