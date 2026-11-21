// PRUEBA DE ESTRES: sube los usuarios por encima de lo normal para encontrar el punto de quiebre.
//   k6 run k6/estres.js   (mismas variables -e que carga.js)
// Revise en el resumen en que etapa p(95) supera 500 ms o empiezan los errores.
import { sleep } from 'k6';
import { prepararDatos, flujoUsuario } from './comun.js';

export const options = {
  stages: [
    { duration: '1m', target: 20 },
    { duration: '1m', target: 50 },
    { duration: '1m', target: 100 },
    { duration: '1m', target: 150 },
    { duration: '1m', target: 0 },  // recuperacion
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.05'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'max'],
};

export function setup() {
  return prepararDatos();
}

export default function (datos) {
  flujoUsuario(datos);
  sleep(0.5);
}
