/**
 * Configuração de produção (usada via fileReplacements no build).
 * Ajustar apiUrl/Keycloak para o endereço real do deploy.
 */
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8080',
  keycloak: {
    url: 'http://localhost:8081',
    realm: 'desafio',
    clientId: 'frontend',
    silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
  },
};
