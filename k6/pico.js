// PRUEBA DE PICO (spike): un salto brusco de usuarios y vuelta a la normalidad.
import { sleep } from 'k6';
import { prepararDatos, flujoUsuario } from './comun.js';

export const options = {
  stages: [
    { duration: '20s', target: 10 },
    { duration: '10s', target: 120 }, // pico
    { duration: '40s', target: 120 },
    { duration: '10s', target: 10 },
    { duration: '30s', target: 10 },
    { duration: '10s', target: 0 },
  ],
  thresholds: { http_req_failed: ['rate<0.05'] },
  summaryTrendStats: ['avg', 'med', 'p(95)', 'max'],
};

export function setup() {
  return prepararDatos();
}

export default function (datos) {
  flujoUsuario(datos);
  sleep(0.5);
}
