-- =====================================================================
-- CLINICA VETERINARIA - BASES DE DATOS (PostgreSQL / pgAdmin 4)
-- Cada microservicio tiene SU PROPIA base de datos. No hay claves foraneas entre ellas:
-- mascotas.dueno_id y ordenes_atencion.dueno_id son referencias LOGICAS a duenos.id_dueno.
-- Las tablas las crea Hibernate (ddl-auto=update) al arrancar cada servicio.
-- =====================================================================

-- PASO 1. Conectado a la BD "postgres": crear las 3 bases
CREATE DATABASE duenos_db;
CREATE DATABASE mascotas_db;
CREATE DATABASE notificaciones_db;

-- PASO 2. Arrancar los 3 microservicios UNA vez (crean las tablas y los 5 tipos de atencion).

-- PASO 3 (recomendado). Restricciones extra que JPA no genera por si solo.

-- >>> Conectado a duenos_db
ALTER TABLE duenos            ADD CONSTRAINT ck_dueno_saldo      CHECK (saldo >= 0);
ALTER TABLE movimientos_saldo ADD CONSTRAINT ck_movimiento_monto CHECK (monto > 0);

-- >>> Conectado a mascotas_db
ALTER TABLE tipos_atencion ADD CONSTRAINT ck_tipo_precio    CHECK (precio > 0);
ALTER TABLE tipos_atencion ADD CONSTRAINT ck_tipo_duracion  CHECK (duracion_minutos > 0);
ALTER TABLE mascotas       ADD CONSTRAINT ck_mascota_peso   CHECK (peso IS NULL OR peso > 0);
ALTER TABLE items_orden    ADD CONSTRAINT ck_item_cantidad  CHECK (cantidad > 0);
-- Una mascota solo puede tener UNA orden PENDIENTE a la vez (indice unico parcial)
CREATE UNIQUE INDEX uq_orden_pendiente_por_mascota ON ordenes_atencion (mascota_id) WHERE estado = 'PENDIENTE';

-- >>> Conectado a notificaciones_db
ALTER TABLE notificaciones ADD CONSTRAINT ck_notificacion_intentos CHECK (intentos >= 0);


-- =====================================================================
-- REFERENCIA: estructura que genera Hibernate (por si se quiere crear a mano)
-- =====================================================================
-- duenos_db
--   duenos(id_dueno varchar PK, nombre, documento, correo UNIQUE, clave (BCrypt), telefono, direccion, saldo numeric(15,2))
--   movimientos_saldo(id_movimiento varchar PK, id_dueno, tipo RECARGA|DESCUENTO|REVERSO, monto, saldo_resultante,
--                     referencia, descripcion, fecha, UNIQUE(referencia, tipo))
-- mascotas_db
--   mascotas(id_mascota bigserial PK, nombre, especie, raza, edad, peso numeric(6,2), dueno_id)
--   tipos_atencion(id_tipo_atencion bigserial PK, nombre UNIQUE, descripcion, precio, duracion_minutos)
--   ordenes_atencion(id_orden varchar PK, mascota_id, dueno_id, estado PENDIENTE|REALIZADA|CANCELADA,
--                    referencia_pago, fecha_creacion, fecha_atencion, total)
--   items_orden(id_item bigserial PK, id_orden FK -> ordenes_atencion, tipo_atencion_id, nombre_servicio,
--               cantidad, precio_unitario, subtotal, UNIQUE(id_orden, tipo_atencion_id))
-- notificaciones_db
--   notificaciones(id_notificacion bigserial PK, canal EMAIL|SMS, tipo, destinatario, asunto, mensaje,
--                  estado PENDIENTE|ENVIADA|FALLIDA, id_proveedor, intentos, detalle_error, fecha_creacion, fecha_envio)

-- Consultas utiles para las evidencias
-- >>> duenos_db
-- SELECT id_dueno, nombre, correo, saldo FROM duenos;
-- SELECT tipo, monto, saldo_resultante, referencia, descripcion, fecha FROM movimientos_saldo ORDER BY fecha;
-- >>> mascotas_db
-- SELECT * FROM tipos_atencion ORDER BY id_tipo_atencion;
-- SELECT id_orden, mascota_id, estado, total, fecha_atencion FROM ordenes_atencion;
-- >>> notificaciones_db
-- SELECT id_notificacion, canal, destinatario, estado, intentos, detalle_error FROM notificaciones ORDER BY fecha_creacion DESC;
