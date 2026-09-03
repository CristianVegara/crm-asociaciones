-- Datos ficticios para probar /informes/resumen.
-- MySQL 8.0+. Ejecutar despues de crm-asociaciones-schema.sql y con el backend
-- arrancado al menos una vez para que exista el trabajador admin.

USE crm_asociaciones;

START TRANSACTION;

INSERT INTO asociacion (nombre, direccion, contacto)
SELECT 'Asociacion Demo - Datos de prueba', 'Direccion de prueba', 'demo@example.test'
WHERE NOT EXISTS (
    SELECT 1 FROM asociacion
    WHERE nombre = 'Asociacion Demo - Datos de prueba'
);
SET @asociacion_id = (
    SELECT id FROM asociacion
    WHERE nombre = 'Asociacion Demo - Datos de prueba'
    LIMIT 1
);

INSERT INTO tipo_servicio (nombre, icono, color, activo)
SELECT 'Servicio Demo - Rehabilitacion', 'activity', '#2563eb', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM tipo_servicio
    WHERE nombre = 'Servicio Demo - Rehabilitacion'
);
SET @tipo_servicio_id = (
    SELECT id FROM tipo_servicio
    WHERE nombre = 'Servicio Demo - Rehabilitacion'
    LIMIT 1
);

INSERT INTO sub_servicio (nombre, activo, tipo_servicio_id)
SELECT 'Sesion individual', TRUE, @tipo_servicio_id
WHERE NOT EXISTS (
    SELECT 1 FROM sub_servicio
    WHERE nombre = 'Sesion individual'
      AND tipo_servicio_id = @tipo_servicio_id
);
SET @sub_servicio_id = (
    SELECT id FROM sub_servicio
    WHERE nombre = 'Sesion individual'
      AND tipo_servicio_id = @tipo_servicio_id
    LIMIT 1
);

INSERT INTO paciente (
    nombre, apellidos, numero_expediente, fecha_nacimiento, genero,
    activo, fecha_alta, asociacion_id
)
SELECT
    'Paciente', 'Demo', 'EXP-DEMO-001', '1990-05-15', 'NO_ESPECIFICADO',
    TRUE, CURRENT_DATE, @asociacion_id
WHERE NOT EXISTS (
    SELECT 1 FROM paciente
    WHERE numero_expediente = 'EXP-DEMO-001'
);
SET @paciente_id = (
    SELECT id FROM paciente
    WHERE numero_expediente = 'EXP-DEMO-001'
    LIMIT 1
);

SET @trabajador_id = (
    SELECT id FROM trabajador
    WHERE usuario = 'admin'
    LIMIT 1
);

INSERT INTO plan_servicio (
    paciente_id, tipo_servicio_id, sub_servicio_id,
    fecha_inicio, fecha_fin, estado, creado_por,
    fecha_creacion, fecha_finalizacion
)
SELECT
    @paciente_id, @tipo_servicio_id, @sub_servicio_id,
    DATE_FORMAT(CURRENT_DATE, '%Y-%m-01'),
    LAST_DAY(CURRENT_DATE),
    'ACTIVO', @trabajador_id, NOW(6), NULL
WHERE NOT EXISTS (
    SELECT 1 FROM plan_servicio
    WHERE paciente_id = @paciente_id
      AND tipo_servicio_id = @tipo_servicio_id
      AND fecha_inicio = DATE_FORMAT(CURRENT_DATE, '%Y-%m-01')
);
SET @plan_id = (
    SELECT id FROM plan_servicio
    WHERE paciente_id = @paciente_id
      AND tipo_servicio_id = @tipo_servicio_id
      AND fecha_inicio = DATE_FORMAT(CURRENT_DATE, '%Y-%m-01')
    ORDER BY id
    LIMIT 1
);

INSERT IGNORE INTO plan_servicio_dia_semana (plan_servicio_id, dia_semana)
VALUES (@plan_id, 'MONDAY'), (@plan_id, 'WEDNESDAY'), (@plan_id, 'FRIDAY');

INSERT INTO sesion_programada (
    plan_servicio_id, fecha_prevista, estado, registrado_por, fecha_registro
)
VALUES
    (@plan_id, DATE_FORMAT(CURRENT_DATE, '%Y-%m-01'), 'VERDE', @trabajador_id, NOW(6)),
    (@plan_id, DATE_ADD(DATE_FORMAT(CURRENT_DATE, '%Y-%m-01'), INTERVAL 2 DAY), 'VERDE', @trabajador_id, NOW(6)),
    (@plan_id, DATE_ADD(DATE_FORMAT(CURRENT_DATE, '%Y-%m-01'), INTERVAL 4 DAY), 'NARANJA', @trabajador_id, NOW(6)),
    (@plan_id, DATE_ADD(DATE_FORMAT(CURRENT_DATE, '%Y-%m-01'), INTERVAL 7 DAY), 'ROJO', @trabajador_id, NOW(6)),
    (@plan_id, DATE_ADD(DATE_FORMAT(CURRENT_DATE, '%Y-%m-01'), INTERVAL 9 DAY), 'AMARILLO', @trabajador_id, NOW(6)),
    (@plan_id, DATE_ADD(DATE_FORMAT(CURRENT_DATE, '%Y-%m-01'), INTERVAL 11 DAY), 'PENDIENTE', NULL, NULL)
ON DUPLICATE KEY UPDATE
    estado = VALUES(estado),
    registrado_por = VALUES(registrado_por),
    fecha_registro = VALUES(fecha_registro);

INSERT INTO sancion (
    paciente_id, plan_servicio_id, tipo, fecha, motivo, aplicada_por, automatica
)
SELECT
    @paciente_id, @plan_id, 'SUSPENSION_TEMPORAL', CURRENT_DATE,
    'Sancion ficticia para probar informes', @trabajador_id, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM sancion
    WHERE paciente_id = @paciente_id
      AND plan_servicio_id = @plan_id
      AND motivo = 'Sancion ficticia para probar informes'
);

COMMIT;

-- Consulta sugerida para comprobar el resultado del informe del mes actual:
-- GET /informes/resumen?desde=2026-09-01&hasta=2026-09-30&periodo=mensual
--
-- Comprobacion SQL:
-- SELECT estado, COUNT(*) AS total
-- FROM sesion_programada
-- WHERE plan_servicio_id = @plan_id
-- GROUP BY estado;
