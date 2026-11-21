// PRUEBA DE CARGA: carga normal sostenida. Objetivo: el 95% de las peticiones en menos de 500 ms.
//   k6 run k6/carga.js                                  (local)
//   k6 run -e DUENOS_URL=https://... -e MASCOTAS_URL=https://... -e NOTIFICACIONES_URL=https://... k6/carga.js
import { sleep } from 'k6';
import { prepararDatos, flujoUsuario } from './comun.js';

export const options = {
  stages: [
    { duration: '30s', target: 20 }, // subida a 20 usuarios
    { duration: '2m', target: 20 },  // 20 usuarios constantes
    { duration: '30s', target: 0 },  // bajada
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // requisito: < 500 ms
    http_req_failed: ['rate<0.01'],   // menos del 1% de errores
    checks: ['rate>0.99'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'max'],
};

export function setup() {
  return prepararDatos();
}

export default function (datos) {
  flujoUsuario(datos);
  sleep(1);
}
