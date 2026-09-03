-- CRM de gestion de asociaciones
-- MySQL 8.0+
-- Ejecutar con un usuario con permisos para crear la base de datos y sus tablas.

CREATE DATABASE IF NOT EXISTS crm_asociaciones
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE crm_asociaciones;

CREATE TABLE IF NOT EXISTS asociacion (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    direccion VARCHAR(255) NULL,
    contacto VARCHAR(255) NULL,
    CONSTRAINT pk_asociacion PRIMARY KEY (id)
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS rol (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255) NULL,
    CONSTRAINT pk_rol PRIMARY KEY (id),
    CONSTRAINT uk_rol_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tipo_servicio (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    icono VARCHAR(255) NULL,
    color VARCHAR(255) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_tipo_servicio PRIMARY KEY (id),
    CONSTRAINT uk_tipo_servicio_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS trabajador (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    usuario VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    rol_id BIGINT NOT NULL,
    CONSTRAINT pk_trabajador PRIMARY KEY (id),
    CONSTRAINT uk_trabajador_usuario UNIQUE (usuario),
    CONSTRAINT fk_trabajador_rol FOREIGN KEY (rol_id)
        REFERENCES rol (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    INDEX idx_trabajador_rol (rol_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS informe_generado (
    id BIGINT NOT NULL AUTO_INCREMENT,
    desde DATE NOT NULL,
    hasta DATE NOT NULL,
    periodo VARCHAR(255) NULL,
    tipo_informe VARCHAR(50) NOT NULL DEFAULT 'general',
    fecha_generacion DATETIME NOT NULL,
    generado_por BIGINT NOT NULL,
    CONSTRAINT pk_informe_generado PRIMARY KEY (id),
    CONSTRAINT fk_informe_generado_trabajador FOREIGN KEY (generado_por)
        REFERENCES trabajador (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    INDEX idx_informe_generado_fecha (fecha_generacion)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS rol_permiso (
    rol_id BIGINT NOT NULL,
    permiso VARCHAR(255) NOT NULL,
    CONSTRAINT pk_rol_permiso PRIMARY KEY (rol_id, permiso),
    CONSTRAINT fk_rol_permiso_rol FOREIGN KEY (rol_id)
        REFERENCES rol (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sub_servicio (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    tipo_servicio_id BIGINT NOT NULL,
    CONSTRAINT pk_sub_servicio PRIMARY KEY (id),
    CONSTRAINT fk_sub_servicio_tipo FOREIGN KEY (tipo_servicio_id)
        REFERENCES tipo_servicio (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    INDEX idx_sub_servicio_tipo (tipo_servicio_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tipo_servicio_responsable (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tipo_servicio_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    capacidad VARCHAR(255) NOT NULL,
    CONSTRAINT pk_tipo_servicio_responsable PRIMARY KEY (id),
    CONSTRAINT uk_tipo_servicio_responsable UNIQUE
        (tipo_servicio_id, rol_id, capacidad),
    CONSTRAINT fk_responsable_tipo_servicio FOREIGN KEY (tipo_servicio_id)
        REFERENCES tipo_servicio (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT fk_responsable_rol FOREIGN KEY (rol_id)
        REFERENCES rol (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT chk_responsable_capacidad CHECK (
        capacidad IN ('REGISTRAR_ASISTENCIA', 'APLICAR_SANCION', 'GESTIONAR_PLAN')
    ),
    INDEX idx_responsable_tipo_servicio (tipo_servicio_id),
    INDEX idx_responsable_rol (rol_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS paciente (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    numero_expediente VARCHAR(255) NOT NULL,
    fecha_nacimiento DATE NULL,
    genero VARCHAR(255) NULL,
    dni VARCHAR(20) NULL,
    telefono VARCHAR(30) NULL,
    email VARCHAR(255) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_alta DATE NOT NULL,
    asociacion_id BIGINT NOT NULL,
    CONSTRAINT pk_paciente PRIMARY KEY (id),
    CONSTRAINT uk_paciente_numero_expediente UNIQUE (numero_expediente),
    CONSTRAINT fk_paciente_asociacion FOREIGN KEY (asociacion_id)
        REFERENCES asociacion (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    INDEX idx_paciente_asociacion (asociacion_id)
) ENGINE=InnoDB;

ALTER TABLE paciente ADD COLUMN IF NOT EXISTS dni VARCHAR(20) NULL,
    ADD COLUMN IF NOT EXISTS telefono VARCHAR(30) NULL,
    ADD COLUMN IF NOT EXISTS email VARCHAR(255) NULL;

CREATE TABLE IF NOT EXISTS plan_servicio (
    id BIGINT NOT NULL AUTO_INCREMENT,
    paciente_id BIGINT NOT NULL,
    tipo_servicio_id BIGINT NOT NULL,
    sub_servicio_id BIGINT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(255) NOT NULL DEFAULT 'ACTIVO',
    creado_por BIGINT NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_finalizacion DATETIME(6) NULL,
    CONSTRAINT pk_plan_servicio PRIMARY KEY (id),
    CONSTRAINT fk_plan_paciente FOREIGN KEY (paciente_id)
        REFERENCES paciente (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT fk_plan_tipo_servicio FOREIGN KEY (tipo_servicio_id)
        REFERENCES tipo_servicio (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT fk_plan_sub_servicio FOREIGN KEY (sub_servicio_id)
        REFERENCES sub_servicio (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT fk_plan_creado_por FOREIGN KEY (creado_por)
        REFERENCES trabajador (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT chk_plan_estado CHECK (estado IN ('ACTIVO', 'FINALIZADO')),
    CONSTRAINT chk_plan_fechas CHECK (fecha_fin >= fecha_inicio),
    INDEX idx_plan_paciente_estado (paciente_id, estado),
    INDEX idx_plan_tipo_servicio (tipo_servicio_id),
    INDEX idx_plan_sub_servicio (sub_servicio_id),
    INDEX idx_plan_creado_por (creado_por)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS plan_servicio_dia_semana (
    plan_servicio_id BIGINT NOT NULL,
    dia_semana VARCHAR(255) NOT NULL,
    CONSTRAINT pk_plan_dia_semana PRIMARY KEY (plan_servicio_id, dia_semana),
    CONSTRAINT fk_plan_dia_semana_plan FOREIGN KEY (plan_servicio_id)
        REFERENCES plan_servicio (id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT,
    CONSTRAINT chk_plan_dia_semana CHECK (
        dia_semana IN (
            'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY',
            'FRIDAY', 'SATURDAY', 'SUNDAY'
        )
    )
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sesion_programada (
    id BIGINT NOT NULL AUTO_INCREMENT,
    plan_servicio_id BIGINT NOT NULL,
    fecha_prevista DATE NOT NULL,
    estado VARCHAR(255) NOT NULL DEFAULT 'PENDIENTE',
    registrado_por BIGINT NULL,
    fecha_registro DATETIME(6) NULL,
    CONSTRAINT pk_sesion_programada PRIMARY KEY (id),
    CONSTRAINT uk_sesion_plan_fecha UNIQUE (plan_servicio_id, fecha_prevista),
    CONSTRAINT fk_sesion_plan FOREIGN KEY (plan_servicio_id)
        REFERENCES plan_servicio (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT fk_sesion_registrado_por FOREIGN KEY (registrado_por)
        REFERENCES trabajador (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT chk_sesion_estado CHECK (
        estado IN ('PENDIENTE', 'VERDE', 'NARANJA', 'ROJO', 'AMARILLO')
    ),
    INDEX idx_sesion_fecha_estado (fecha_prevista, estado),
    INDEX idx_sesion_registrado_por (registrado_por)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sancion (
    id BIGINT NOT NULL AUTO_INCREMENT,
    paciente_id BIGINT NOT NULL,
    plan_servicio_id BIGINT NULL,
    tipo VARCHAR(255) NOT NULL,
    fecha DATE NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    aplicada_por BIGINT NOT NULL,
    automatica BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_sancion PRIMARY KEY (id),
    CONSTRAINT fk_sancion_paciente FOREIGN KEY (paciente_id)
        REFERENCES paciente (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT fk_sancion_plan FOREIGN KEY (plan_servicio_id)
        REFERENCES plan_servicio (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT fk_sancion_aplicada_por FOREIGN KEY (aplicada_por)
        REFERENCES trabajador (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,
    CONSTRAINT chk_sancion_tipo CHECK (
        tipo IN ('SUSPENSION_TEMPORAL', 'TARJETA_AMARILLA_AGRESION', 'OTRA')
    ),
    INDEX idx_sancion_paciente_fecha (paciente_id, fecha),
    INDEX idx_sancion_plan (plan_servicio_id),
    INDEX idx_sancion_fecha (fecha),
    INDEX idx_sancion_aplicada_por (aplicada_por)
) ENGINE=InnoDB;

-- Semilla opcional e idempotente. No se inserta el trabajador admin:
-- InicializacionDatos lo crea con un hash BCrypt al arrancar la aplicacion.
INSERT INTO rol (nombre, descripcion)
VALUES ('Director', 'Rol inicial con todos los permisos del sistema')
ON DUPLICATE KEY UPDATE
    descripcion = VALUES(descripcion);

INSERT INTO rol_permiso (rol_id, permiso)
SELECT id, 'GESTIONAR_TRABAJADORES' FROM rol WHERE nombre = 'Director'
UNION ALL
SELECT id, 'GESTIONAR_CATALOGO_SERVICIOS' FROM rol WHERE nombre = 'Director'
UNION ALL
SELECT id, 'VER_INFORMES' FROM rol WHERE nombre = 'Director'
UNION ALL
SELECT id, 'GESTIONAR_PACIENTES' FROM rol WHERE nombre = 'Director'
UNION ALL
SELECT id, 'CREAR_PLAN_SERVICIO' FROM rol WHERE nombre = 'Director'
UNION ALL
SELECT id, 'REGISTRAR_ASISTENCIA' FROM rol WHERE nombre = 'Director'
UNION ALL
SELECT id, 'APLICAR_SANCION' FROM rol WHERE nombre = 'Director'
ON DUPLICATE KEY UPDATE
    permiso = VALUES(permiso);

-- Consultas de verificacion (ejecutar despues del script).
-- SHOW TABLES;
-- SELECT TABLE_NAME, ENGINE, TABLE_COLLATION
-- FROM information_schema.TABLES
-- WHERE TABLE_SCHEMA = 'crm_asociaciones';
-- SELECT TABLE_NAME, CONSTRAINT_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME,
--        REFERENCED_COLUMN_NAME
-- FROM information_schema.KEY_COLUMN_USAGE
-- WHERE TABLE_SCHEMA = 'crm_asociaciones'
--   AND REFERENCED_TABLE_NAME IS NOT NULL
-- ORDER BY TABLE_NAME, CONSTRAINT_NAME;
-- SELECT TABLE_NAME, INDEX_NAME, COLUMN_NAME, NON_UNIQUE
-- FROM information_schema.STATISTICS
-- WHERE TABLE_SCHEMA = 'crm_asociaciones'
-- ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;
-- SELECT r.nombre, rp.permiso
-- FROM rol r JOIN rol_permiso rp ON rp.rol_id = r.id
-- WHERE r.nombre = 'Director'
-- ORDER BY rp.permiso;
