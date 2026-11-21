// Funciones compartidas por las pruebas de carga y estres (k6: https://k6.io)
import http from 'k6/http';
import { check, group } from 'k6';

export const DUENOS = __ENV.DUENOS_URL || 'http://localhost:8080';
export const MASCOTAS = __ENV.MASCOTAS_URL || 'http://localhost:8081';
export const NOTIFICACIONES = __ENV.NOTIFICACIONES_URL || 'http://localhost:8082';

const JSON_HDR = { headers: { 'Content-Type': 'application/json' } };

function post(url, body, tag) {
  return http.post(url, JSON.stringify(body), Object.assign({ tags: { api: tag } }, JSON_HDR));
}

function get(url, tag) {
  return http.get(url, { tags: { api: tag } });
}

/** Crea un dueno con saldo y una mascota para usar en la prueba. Se ejecuta UNA vez. */
export function prepararDatos() {
  const correo = `carga${Date.now()}@correo.com`;
  const dueno = post(`${DUENOS}/duenos`, {
    nombre: 'Dueno Carga', documento: '999', correo, clave: 'Clave123', telefono: '3001234567', direccion: 'k6',
  }, 'registrar-dueno').json();
  post(`${DUENOS}/duenos/saldo/recargar`, { duenoId: dueno.idDueno, monto: 2000000 }, 'recargar');
  const mascota = post(`${MASCOTAS}/mascotas`, {
    nombre: 'Toby', especie: 'Perro', raza: 'Labrador', edad: 4, peso: 25, duenoId: dueno.idDueno,
  }, 'registrar-mascota').json();
  return { duenoId: dueno.idDueno, correo, mascotaId: mascota.idMascota };
}

/**
 * Una iteracion = un usuario usando el sistema. Incluye las APIs core (las que van contra PostgreSQL).
 * No llama a Gemini ni envia correos/SMS reales para no gastar las cuotas gratis de esas APIs.
 */
export function flujoUsuario(datos) {
  group('ms-duenos-pagos', () => {
    const login = post(`${DUENOS}/duenos/login`, { correo: datos.correo, clave: 'Clave123' }, 'login');
    check(login, { 'login 200': (r) => r.status === 200 });

    check(get(`${DUENOS}/duenos/${datos.duenoId}`, 'consultar-dueno'), { 'dueno 200': (r) => r.status === 200 });

    const ref = `K6-${__VU}-${__ITER}-${Date.now()}`;
    const cobro = post(`${DUENOS}/duenos/saldo/descontar`,
      { duenoId: datos.duenoId, monto: 1000, referencia: ref, descripcion: 'Prueba de carga' }, 'descontar');
    check(cobro, { 'descontar 200': (r) => r.status === 200 });

    const reverso = post(`${DUENOS}/duenos/saldo/reversar`, { duenoId: datos.duenoId, referencia: ref }, 'reversar');
    check(reverso, { 'reversar 200': (r) => r.status === 200 });

    check(get(`${DUENOS}/duenos/${datos.duenoId}/saldo`, 'saldo'), { 'saldo 200': (r) => r.status === 200 });
  });

  group('ms-mascotas-atencion', () => {
    check(get(`${MASCOTAS}/tipos-atencion`, 'tipos-atencion'), { 'tipos 200': (r) => r.status === 200 });
    check(get(`${MASCOTAS}/mascotas/dueno/${datos.duenoId}`, 'mascotas-dueno'), { 'mascotas 200': (r) => r.status === 200 });
    const orden = post(`${MASCOTAS}/atenciones/${datos.mascotaId}/agregar-servicio`, { tipoAtencionId: 1 }, 'agregar-servicio');
    check(orden, { 'agregar-servicio 200': (r) => r.status === 200 });
    check(get(`${MASCOTAS}/atenciones/historial-mascota/${datos.mascotaId}`, 'historial-mascota'),
      { 'historial 200': (r) => r.status === 200 });
  });

  group('ms-notificaciones', () => {
    const n = post(`${NOTIFICACIONES}/notificaciones`, {
      canal: 'EMAIL', tipo: 'CONFIRMACION_CITA', destinatario: datos.correo, asunto: 'Cita', mensaje: 'Prueba de carga',
    }, 'crear-notificacion');
    check(n, { 'notificacion 201': (r) => r.status === 201 });
    check(get(`${NOTIFICACIONES}/notificaciones/historial/${datos.correo}`, 'historial-notificaciones'),
      { 'historial notif 200': (r) => r.status === 200 });
  });
}
