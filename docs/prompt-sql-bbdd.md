# Prompt para generar el esquema SQL del CRM

El siguiente prompt está preparado para copiarlo en una herramienta de IA que genere
el script de creación de la base de datos.

```text
Actúa como arquitecto de bases de datos especializado en MySQL 8.0 y Spring Boot 3.3
con Hibernate/JPA. Genera un único script SQL ejecutable de forma segura y repetible
para crear el esquema inicial del proyecto "CRM de gestión de asociaciones".

## Contexto técnico

- Motor obligatorio: MySQL 8.0 o superior.
- Base de datos: crm_asociaciones.
- Charset y collation: utf8mb4 y utf8mb4_unicode_ci.
- El backend usa Hibernate con GenerationType.IDENTITY y campos Java Long, por lo que
  las PK y FK deben ser BIGINT AUTO_INCREMENT (sin UNSIGNED, para que ddl-auto=validate
  pueda comparar los tipos sin sorpresas).
- Los nombres de tablas y columnas deben coincidir exactamente con los indicados.
- No crees una tabla permiso: los permisos son el enum Java
  com.aitsolutions.crm.permiso.Permiso y se guardan como texto en rol_permiso.
- Los enums Java se almacenan con sus nombres (EnumType.STRING), nunca como números.
- No guardes contraseñas en texto plano: trabajador.password_hash contiene un hash BCrypt.

## Tablas y columnas obligatorias

1. asociacion
   - id BIGINT PK AUTO_INCREMENT
   - nombre VARCHAR(255) NOT NULL
   - direccion VARCHAR(255) NULL
   - contacto VARCHAR(255) NULL

2. rol
   - id BIGINT PK AUTO_INCREMENT
   - nombre VARCHAR(255) NOT NULL UNIQUE
   - descripcion VARCHAR(255) NULL

3. trabajador
   - id BIGINT PK AUTO_INCREMENT
   - nombre VARCHAR(255) NOT NULL
   - apellidos VARCHAR(255) NOT NULL
   - usuario VARCHAR(255) NOT NULL UNIQUE
   - password_hash VARCHAR(255) NOT NULL
   - activo BOOLEAN NOT NULL DEFAULT TRUE
   - rol_id BIGINT NOT NULL FK a rol(id)

4. rol_permiso (colección JPA de Rol.permisos)
   - rol_id BIGINT NOT NULL FK a rol(id)
   - permiso VARCHAR(255) NOT NULL
   - PK compuesta (rol_id, permiso)
   - valores permitidos: GESTIONAR_TRABAJADORES, GESTIONAR_CATALOGO_SERVICIOS,
     VER_INFORMES, GESTIONAR_PACIENTES, CREAR_PLAN_SERVICIO,
     REGISTRAR_ASISTENCIA, APLICAR_SANCION

5. tipo_servicio
   - id BIGINT PK AUTO_INCREMENT
   - nombre VARCHAR(255) NOT NULL UNIQUE
   - icono VARCHAR(255) NULL
   - color VARCHAR(255) NULL
   - activo BOOLEAN NOT NULL DEFAULT TRUE

6. sub_servicio
   - id BIGINT PK AUTO_INCREMENT
   - nombre VARCHAR(255) NOT NULL
   - activo BOOLEAN NOT NULL DEFAULT TRUE
   - tipo_servicio_id BIGINT NOT NULL FK a tipo_servicio(id)

7. tipo_servicio_responsable
   - id BIGINT PK AUTO_INCREMENT
   - tipo_servicio_id BIGINT NOT NULL FK a tipo_servicio(id)
   - rol_id BIGINT NOT NULL FK a rol(id)
   - capacidad VARCHAR(255) NOT NULL
   - UNIQUE (tipo_servicio_id, rol_id, capacidad)
   - capacidad solo puede ser REGISTRAR_ASISTENCIA, APLICAR_SANCION o GESTIONAR_PLAN

8. paciente
   - id BIGINT PK AUTO_INCREMENT
   - nombre VARCHAR(255) NOT NULL
   - apellidos VARCHAR(255) NOT NULL
   - numero_expediente VARCHAR(255) NOT NULL UNIQUE
   - fecha_nacimiento DATE NULL
   - genero VARCHAR(255) NULL
   - activo BOOLEAN NOT NULL DEFAULT TRUE
   - fecha_alta DATE NOT NULL
   - asociacion_id BIGINT NOT NULL FK a asociacion(id)

9. plan_servicio
   - id BIGINT PK AUTO_INCREMENT
   - paciente_id BIGINT NOT NULL FK a paciente(id)
   - tipo_servicio_id BIGINT NOT NULL FK a tipo_servicio(id)
   - sub_servicio_id BIGINT NULL FK a sub_servicio(id)
   - fecha_inicio DATE NOT NULL
   - fecha_fin DATE NOT NULL
   - estado VARCHAR(255) NOT NULL DEFAULT 'ACTIVO'
   - creado_por BIGINT NOT NULL FK a trabajador(id)
   - fecha_creacion DATETIME(6) NOT NULL
   - fecha_finalizacion DATETIME(6) NULL

10. plan_servicio_dia_semana (colección JPA de PlanServicio.diasSemana)
    - plan_servicio_id BIGINT NOT NULL FK a plan_servicio(id)
    - dia_semana VARCHAR(255) NOT NULL
    - PK compuesta (plan_servicio_id, dia_semana)
    - valores Java esperados: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY,
      SATURDAY, SUNDAY

11. sesion_programada
    - id BIGINT PK AUTO_INCREMENT
    - plan_servicio_id BIGINT NOT NULL FK a plan_servicio(id)
    - fecha_prevista DATE NOT NULL
    - estado VARCHAR(255) NOT NULL DEFAULT 'PENDIENTE'
    - registrado_por BIGINT NULL FK a trabajador(id)
    - fecha_registro DATETIME(6) NULL
    - UNIQUE (plan_servicio_id, fecha_prevista)
    - estados permitidos: PENDIENTE, VERDE, NARANJA, ROJO, AMARILLO, CANCELADA

12. sancion
    - id BIGINT PK AUTO_INCREMENT
    - paciente_id BIGINT NOT NULL FK a paciente(id)
    - plan_servicio_id BIGINT NULL FK a plan_servicio(id)
    - tipo VARCHAR(255) NOT NULL
    - fecha DATE NOT NULL
    - motivo VARCHAR(255) NOT NULL
    - aplicada_por BIGINT NOT NULL FK a trabajador(id)
    - automatica BOOLEAN NOT NULL DEFAULT FALSE
    - tipos permitidos: SUSPENSION_TEMPORAL, TARJETA_AMARILLA_AGRESION, OTRA

13. informe_generado
    - id BIGINT PK AUTO_INCREMENT
    - desde DATE NOT NULL
    - hasta DATE NOT NULL
    - periodo VARCHAR(255) NULL
    - tipo_informe VARCHAR(50) NOT NULL DEFAULT 'general'
    - fecha_generacion DATETIME(6) NOT NULL
    - generado_por BIGINT NOT NULL FK a trabajador(id)
    - índice por fecha_generacion

## Reglas de diseño

1. Usa InnoDB, restricciones FOREIGN KEY explícitas y nombres de constraints legibles.
2. Usa ON DELETE RESTRICT para entidades históricas y referencias de negocio
   (pacientes, planes, sesiones, sanciones y trabajadores).
3. Para las tablas de colección rol_permiso y plan_servicio_dia_semana usa
   ON DELETE CASCADE desde la entidad propietaria.
4. Añade índices para todas las FK y para las consultas habituales:
   paciente(asociacion_id), plan_servicio(paciente_id, estado),
   plan_servicio(tipo_servicio_id), sesion_programada(fecha_prevista, estado),
   sesion_programada(plan_servicio_id, fecha_prevista), sancion(fecha),
   sancion(paciente_id, fecha), informe_generado(fecha_generacion), informe_generado(generado_por).
5. Añade CHECK constraints para los valores de enums cuando sean compatibles con
   MySQL 8.0. Si Hibernate pudiera generar valores futuros, documenta el riesgo y
   prioriza compatibilidad sin romper lecturas existentes.
6. No inventes columnas de auditoría, dirección, teléfono, email, tablas de permisos
   ni tablas de asociación-paciente que no estén especificadas.
7. Mantén el orden de creación según dependencias y el orden inverso para DROP.
8. Incluye CREATE DATABASE IF NOT EXISTS y USE crm_asociaciones.
9. Incluye INSERT opcionales, claramente separados del DDL:
   - un rol "Director" con descripción "Rol inicial con todos los permisos del sistema";
   - sus filas en rol_permiso para los siete permisos.
   No insertes el trabajador admin por SQL porque su password debe ser generado por
   PasswordEncoder de Spring y la aplicación ya lo crea al arrancar si no hay trabajadores.
10. No incluyas credenciales, contraseñas reales ni secretos.

## Formato de salida

Entrega:

A. El script SQL completo, en un único bloque.
B. Una tabla breve con cada FK, su columna y la política ON DELETE.
C. Una lista de supuestos y cualquier diferencia relevante entre este DDL y el
   mapeo JPA.
D. Consultas de verificación que comprueben tablas, FK, índices, enums y unicidad.

El resultado debe poder ejecutarse dos veces sin errores gracias a
CREATE DATABASE IF NOT EXISTS y a un mecanismo explícito para no duplicar las
semillas opcionales.
```

## Nota de integración

El backend actual usa `spring.jpa.hibernate.ddl-auto=update` en desarrollo. Tras
validar el script generado contra las entidades JPA, conviene cambiar esa propiedad
a `validate` y gestionar cambios posteriores mediante migraciones versionadas
(Flyway o Liquibase).
