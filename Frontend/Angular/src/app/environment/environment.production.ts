// Ambiente di produzione.
// Il frontend viene servito direttamente dal backend Spring Boot sullo
// stesso dominio: "/api" evita problemi di CORS e funziona su qualsiasi host.
export const environment = {
  backendUrl: '/api',
  production: true,
};
