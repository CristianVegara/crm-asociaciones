-- Datos ficticios completos para pruebas manuales del CRM.
-- MySQL 8.0+. Ejecutar despues de crm-asociaciones-schema.sql y de arrancar
-- el backend al menos una vez, para que exista el usuario admin.
-- El script es idempotente: usa nombres y expedientes con prefijo TEST- y
-- no elimina ni modifica datos reales existentes.

USE crm_asociaciones;
SET NAMES utf8mb4;
SET @hace_un_ano = DATE_SUB(CURRENT_DATE, INTERVAL 1 YEAR);
SET @hoy = CURRENT_DATE;

START TRANSACTION;

-- Asociaciones de prueba.
INSERT INTO asociacion (nombre, direccion, contacto)
SELECT x.nombre, x.direccion, x.contacto
FROM (
    SELECT 'TEST - Asociacion Norte' nombre, 'Calle Norte 10' direccion, 'norte@test.invalid' contacto
    UNION ALL SELECT 'TEST - Asociacion Sur', 'Avenida Sur 20', 'sur@test.invalid'
    UNION ALL SELECT 'TEST - Asociacion Centro', 'Plaza Centro 30', 'centro@test.invalid'
) x
WHERE NOT EXISTS (SELECT 1 FROM asociacion a WHERE a.nombre = x.nombre);

-- Roles funcionales de prueba.
INSERT INTO rol (nombre, descripcion)
SELECT x.nombre, x.descripcion
FROM (
    SELECT 'TEST - Recepcion' nombre, 'Gestion de agenda y asistencia' descripcion
    UNION ALL SELECT 'TEST - Coordinacion', 'Gestion de pacientes, planes y agenda'
    UNION ALL SELECT 'TEST - Rehabilitacion', 'Asistencia y sanciones de rehabilitacion'
    UNION ALL SELECT 'TEST - Consulta', 'Acceso a informes'
) x
WHERE NOT EXISTS (SELECT 1 FROM rol r WHERE r.nombre = x.nombre);

INSERT IGNORE INTO rol_permiso (rol_id, permiso)
SELECT r.id, p.permiso
FROM rol r
JOIN (
    SELECT 'TEST - Recepcion' rol, 'REGISTRAR_ASISTENCIA' permiso
    UNION ALL SELECT 'TEST - Coordinacion', 'GESTIONAR_PACIENTES'
    UNION ALL SELECT 'TEST - Coordinacion', 'CREAR_PLAN_SERVICIO'
    UNION ALL SELECT 'TEST - Coordinacion', 'REGISTRAR_ASISTENCIA'
    UNION ALL SELECT 'TEST - Coordinacion', 'APLICAR_SANCION'
    UNION ALL SELECT 'TEST - Coordinacion', 'VER_INFORMES'
    UNION ALL SELECT 'TEST - Rehabilitacion', 'REGISTRAR_ASISTENCIA'
    UNION ALL SELECT 'TEST - Rehabilitacion', 'APLICAR_SANCION'
    UNION ALL SELECT 'TEST - Consulta', 'VER_INFORMES'
) p ON p.rol = r.nombre;

-- Trabajadores de prueba. Reutiliza el hash BCrypt del admin; contrasena:
-- admin1234. Cada cuenta tiene un rol distinto para probar la autorizacion.
SET @hash_admin = (SELECT password_hash FROM trabajador WHERE usuario = 'admin' LIMIT 1);
INSERT INTO trabajador (nombre, apellidos, usuario, password_hash, activo, rol_id)
SELECT x.nombre, x.apellidos, x.usuario, @hash_admin, x.activo, r.id
FROM (
    SELECT 'Lucia', 'Recepcion Test', 'test.recepcion', TRUE, 'TEST - Recepcion'
    UNION ALL SELECT 'Carlos', 'Coordinacion Test', 'test.coordinacion', TRUE, 'TEST - Coordinacion'
    UNION ALL SELECT 'Marta', 'Rehabilitacion Test', 'test.rehabilitacion', TRUE, 'TEST - Rehabilitacion'
    UNION ALL SELECT 'Pablo', 'Consulta Test', 'test.consulta', TRUE, 'TEST - Consulta'
    UNION ALL SELECT 'Nuria', 'Baja Test', 'test.baja', FALSE, 'TEST - Recepcion'
) x(nombre, apellidos, usuario, activo, rol_nombre)
JOIN rol r ON r.nombre = x.rol_nombre
WHERE @hash_admin IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM trabajador t WHERE t.usuario = x.usuario);

SET @trabajador_admin = (SELECT id FROM trabajador WHERE usuario = 'admin' LIMIT 1);
SET @trabajador_coordinacion = (SELECT id FROM trabajador WHERE usuario = 'test.coordinacion' LIMIT 1);
SET @trabajador_recepcion = (SELECT id FROM trabajador WHERE usuario = 'test.recepcion' LIMIT 1);
SET @trabajador_rehabilitacion = (SELECT id FROM trabajador WHERE usuario = 'test.rehabilitacion' LIMIT 1);

-- Catalogo: se crean tipos de prueba adicionales y subservicios.
INSERT INTO tipo_servicio (nombre, icono, color, activo)
SELECT x.nombre, x.icono, x.color, TRUE
FROM (
    SELECT 'TEST - Apoyo psicologico' nombre, 'PSI' icono, '#7c3aed' color
    UNION ALL SELECT 'TEST - Transporte adaptado', 'TRA', '#0891b2'
    UNION ALL SELECT 'TEST - Ayuda domiciliaria', 'DOM', '#059669'
) x
WHERE NOT EXISTS (SELECT 1 FROM tipo_servicio t WHERE t.nombre = x.nombre);

INSERT INTO sub_servicio (nombre, activo, tipo_servicio_id)
SELECT x.sub_nombre, TRUE, t.id
FROM (
    SELECT 'TEST - Apoyo psicologico' tipo_nombre, 'Terapia individual' sub_nombre
    UNION ALL SELECT 'TEST - Apoyo psicologico', 'Terapia familiar'
    UNION ALL SELECT 'TEST - Transporte adaptado', 'Ruta urbana'
    UNION ALL SELECT 'TEST - Transporte adaptado', 'Ruta interurbana'
    UNION ALL SELECT 'TEST - Ayuda domiciliaria', 'Acompanamiento'
    UNION ALL SELECT 'TEST - Ayuda domiciliaria', 'Apoyo en domicilio'
) x
JOIN tipo_servicio t ON t.nombre = x.tipo_nombre
WHERE NOT EXISTS (
    SELECT 1 FROM sub_servicio s
    WHERE s.tipo_servicio_id = t.id AND s.nombre = x.sub_nombre
);

-- Responsables de prueba para los servicios de prueba.
INSERT IGNORE INTO tipo_servicio_responsable (tipo_servicio_id, rol_id, capacidad)
SELECT t.id, r.id, c.capacidad
FROM tipo_servicio t
JOIN rol r ON r.nombre IN ('TEST - Recepcion', 'TEST - Coordinacion', 'TEST - Rehabilitacion')
JOIN (
    SELECT 'REGISTRAR_ASISTENCIA' capacidad
    UNION ALL SELECT 'APLICAR_SANCION'
    UNION ALL SELECT 'GESTIONAR_PLAN'
) c
WHERE t.nombre LIKE 'TEST - %';

-- 36 pacientes repartidos entre asociaciones, generos, fechas de nacimiento
-- y estados de actividad. Los tres ultimos quedan dados de baja.
INSERT INTO paciente (
    nombre, apellidos, numero_expediente, fecha_nacimiento, genero, dni,
    telefono, email, activo, fecha_alta, asociacion_id
)
WITH RECURSIVE n AS (
    SELECT 1 AS n
    UNION ALL SELECT n + 1 FROM n WHERE n < 36
)
SELECT
    ELT(MOD(n - 1, 12) + 1, 'Ana', 'Luis', 'Maria', 'Jorge', 'Elena', 'David',
        'Rocio', 'Sergio', 'Laura', 'Miguel', 'Irene', 'Raul'),
    CONCAT('Paciente Test ', LPAD(n, 2, '0')),
    CONCAT('TEST-', LPAD(n, 3, '0')),
    DATE_SUB(@hoy, INTERVAL (18 + MOD(n * 137, 60)) YEAR),
    ELT(MOD(n - 1, 4) + 1, 'FEMENINO', 'MASCULINO', 'NO_ESPECIFICADO', 'OTRO'),
    CONCAT('TEST-DNI-', LPAD(n, 3, '0')),
    CONCAT('600000', LPAD(n, 3, '0')),
    CONCAT('paciente', n, '@test.invalid'),
    n <= 33,
    DATE_SUB(@hoy, INTERVAL MOD(n * 11, 365) DAY),
    (SELECT id FROM asociacion WHERE nombre =
        ELT(MOD(n - 1, 3) + 1, 'TEST - Asociacion Norte',
            'TEST - Asociacion Sur', 'TEST - Asociacion Centro') LIMIT 1)
FROM n
WHERE NOT EXISTS (
    SELECT 1 FROM paciente p
    WHERE p.numero_expediente = CONCAT('TEST-', LPAD(n, 3, '0'))
);

-- Planes: dos por paciente, con fechas dentro del ultimo ano. Se alternan
-- planes activos y finalizados para cubrir filtros y estados.
INSERT INTO plan_servicio (
    paciente_id, tipo_servicio_id, sub_servicio_id, fecha_inicio, fecha_fin,
    estado, creado_por, fecha_creacion, fecha_finalizacion
)
SELECT p.id, t.id, s.id,
       DATE_ADD(@hace_un_ano, INTERVAL MOD(p.id * 9 + t.id, 250) DAY),
       DATE_ADD(@hace_un_ano, INTERVAL MOD(p.id * 9 + t.id, 250) + 35 + MOD(p.id, 80) DAY),
       CASE WHEN MOD(p.id + t.id, 4) = 0 THEN 'FINALIZADO' ELSE 'ACTIVO' END,
       COALESCE(@trabajador_coordinacion, @trabajador_admin),
       DATE_SUB(NOW(), INTERVAL MOD(p.id * 3, 365) DAY),
       CASE WHEN MOD(p.id + t.id, 4) = 0
            THEN DATE_SUB(NOW(), INTERVAL MOD(p.id, 120) DAY) ELSE NULL END
FROM paciente p
JOIN tipo_servicio t ON t.nombre IN ('TEST - Apoyo psicologico', 'TEST - Transporte adaptado')
LEFT JOIN sub_servicio s ON s.tipo_servicio_id = t.id
WHERE p.numero_expediente LIKE 'TEST-%'
  AND MOD(p.id + t.id, 3) <> 0
  AND NOT EXISTS (
      SELECT 1 FROM plan_servicio ps
      WHERE ps.paciente_id = p.id AND ps.tipo_servicio_id = t.id
        AND ps.fecha_inicio = DATE_ADD(@hace_un_ano, INTERVAL MOD(p.id * 9 + t.id, 250) DAY)
  );

-- Frecuencias variadas.
INSERT IGNORE INTO plan_servicio_dia_semana (plan_servicio_id, dia_semana)
SELECT ps.id, d.dia
FROM plan_servicio ps
JOIN (
    SELECT 'MONDAY' dia
    UNION ALL SELECT 'WEDNESDAY'
    UNION ALL SELECT 'FRIDAY'
) d
WHERE ps.creado_por IN (@trabajador_coordinacion, @trabajador_admin)
  AND ps.paciente_id IN (SELECT id FROM paciente WHERE numero_expediente LIKE 'TEST-%');

-- Calendario con sesiones en todos los estados permitidos por el esquema.
INSERT IGNORE INTO sesion_programada (
    plan_servicio_id, fecha_prevista, estado, registrado_por, fecha_registro
)
WITH RECURSIVE fechas AS (
    SELECT @hace_un_ano AS fecha
    UNION ALL SELECT DATE_ADD(fecha, INTERVAL 1 DAY)
    FROM fechas WHERE fecha < @hoy
)
SELECT ps.id, f.fecha,
       CASE MOD(DATEDIFF(f.fecha, ps.fecha_inicio) + ps.id, 11)
           WHEN 0 THEN 'ROJO'
           WHEN 1 THEN 'NARANJA'
           WHEN 2 THEN 'AMARILLO'
           WHEN 3 THEN 'VERDE'
           WHEN 4 THEN 'VERDE'
           WHEN 5 THEN 'PENDIENTE'
           ELSE 'VERDE'
       END,
       CASE WHEN MOD(DATEDIFF(f.fecha, ps.fecha_inicio) + ps.id, 11) = 5
            THEN NULL ELSE COALESCE(@trabajador_recepcion, @trabajador_admin) END,
       CASE WHEN MOD(DATEDIFF(f.fecha, ps.fecha_inicio) + ps.id, 11) = 5
            THEN NULL ELSE TIMESTAMP(f.fecha, '10:00:00') END
FROM plan_servicio ps
JOIN fechas f ON f.fecha BETWEEN ps.fecha_inicio AND LEAST(ps.fecha_fin, @hoy)
JOIN plan_servicio_dia_semana d
  ON d.plan_servicio_id = ps.id
 AND d.dia_semana = CASE DAYOFWEEK(f.fecha)
     WHEN 1 THEN 'SUNDAY'
     WHEN 2 THEN 'MONDAY'
     WHEN 3 THEN 'TUESDAY'
     WHEN 4 THEN 'WEDNESDAY'
     WHEN 5 THEN 'THURSDAY'
     WHEN 6 THEN 'FRIDAY'
     WHEN 7 THEN 'SATURDAY'
 END
WHERE ps.paciente_id IN (SELECT id FROM paciente WHERE numero_expediente LIKE 'TEST-%');

-- Sanciones manuales y automaticas, incluyendo sanciones sin plan.
INSERT INTO sancion (
    paciente_id, plan_servicio_id, tipo, fecha, motivo, aplicada_por, automatica
)
SELECT p.id, NULL, 'TARJETA_AMARILLA_AGRESION',
       DATE_SUB(@hoy, INTERVAL MOD(p.id, 300) DAY),
       'Sancion manual de prueba sin plan asociado',
       COALESCE(@trabajador_rehabilitacion, @trabajador_admin), FALSE
FROM paciente p
WHERE p.numero_expediente LIKE 'TEST-%'
  AND MOD(p.id, 5) = 0
  AND NOT EXISTS (
      SELECT 1 FROM sancion s
      WHERE s.paciente_id = p.id AND s.motivo = 'Sancion manual de prueba sin plan asociado'
  );

INSERT INTO sancion (
    paciente_id, plan_servicio_id, tipo, fecha, motivo, aplicada_por, automatica
)
SELECT ps.paciente_id, ps.id, 'SUSPENSION_TEMPORAL',
       DATE_ADD(ps.fecha_inicio, INTERVAL 20 DAY),
       'Sancion automatica de prueba por acumulacion de faltas',
       COALESCE(@trabajador_rehabilitacion, @trabajador_admin), TRUE
FROM plan_servicio ps
WHERE ps.paciente_id IN (SELECT id FROM paciente WHERE numero_expediente LIKE 'TEST-%')
  AND MOD(ps.id, 4) = 0
  AND NOT EXISTS (
      SELECT 1 FROM sancion s
      WHERE s.plan_servicio_id = ps.id
        AND s.motivo = 'Sancion automatica de prueba por acumulacion de faltas'
  );

-- Informes historicos en periodos mensuales, trimestrales, semestrales y anual.
INSERT INTO informe_generado (desde, hasta, periodo, tipo_informe, fecha_generacion, generado_por)
SELECT x.desde, x.hasta, x.periodo, x.tipo_informe,
       TIMESTAMP(x.hasta, '18:00:00'), COALESCE(@trabajador_admin, @trabajador_coordinacion)
FROM (
    SELECT @hace_un_ano desde, DATE_SUB(@hoy, INTERVAL 10 MONTH) hasta, 'trimestral' periodo, 'general' tipo_informe
    UNION ALL SELECT DATE_SUB(@hoy, INTERVAL 6 MONTH), DATE_SUB(@hoy, INTERVAL 3 MONTH), 'trimestral', 'servicios'
    UNION ALL SELECT DATE_SUB(@hoy, INTERVAL 3 MONTH), @hoy, 'trimestral', 'general'
    UNION ALL SELECT DATE_SUB(@hoy, INTERVAL 6 MONTH), @hoy, 'semestral', 'servicios'
    UNION ALL SELECT @hace_un_ano, @hoy, 'anual', 'general'
    UNION ALL SELECT @hace_un_ano, @hoy, 'anual', 'servicios'
) x
WHERE NOT EXISTS (
    SELECT 1 FROM informe_generado i
    WHERE i.desde = x.desde AND i.hasta = x.hasta
      AND i.periodo = x.periodo AND i.tipo_informe = x.tipo_informe
      AND i.generado_por = COALESCE(@trabajador_admin, @trabajador_coordinacion)
);

COMMIT;

-- Comprobacion rapida:
-- SELECT COUNT(*) FROM paciente WHERE numero_expediente LIKE 'TEST-%';
-- SELECT estado, COUNT(*) FROM sesion_programada GROUP BY estado;
-- SELECT tipo, automatica, COUNT(*) FROM sancion GROUP BY tipo, automatica;
-- SELECT periodo, tipo_informe, desde, hasta FROM informe_generado ORDER BY desde;
-- Cuentas: test.recepcion, test.coordinacion, test.rehabilitacion,
-- test.consulta y test.baja. Contrasena de las cuentas activas: admin1234.
