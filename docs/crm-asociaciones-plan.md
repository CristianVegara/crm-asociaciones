# CRM de gestión de asociaciones — documento de planificación

> Origen: análisis y evolución del prototipo `erika.html` (mockup HTML/Tailwind de un solo archivo, sin backend ni persistencia). Este documento recoge la planificación acordada para construir la versión real como sistema cliente-servidor en red local.
>
> **Revisión 2**: corrige el modelo de dominio inicial. Lo que en el prototipo se llamaba "Usuario" son en realidad **trabajadores de la asociación** (admin/director, coordinador, recepción, rehabilitador...). Los verdaderos beneficiarios del servicio son **pacientes**, que siguen **planes de servicio** (ej. "rehabilitación 2 veces por semana durante 3 meses") con un calendario de sesiones que el personal va marcando como asistidas o no.
>
> **Revisión 3 (03/09/2026)**: se implementa el módulo de informes con datos reales, histórico persistente, regeneración, previsualización y exportación a PDF con plantilla HTML/CSS. También se documentan correcciones de navegación JavaFX, inicialización idempotente y compatibilidad de ejecución con JDK 26.

## 1. Qué era el prototipo original

`erika.html` es un mockup interactivo (Tailwind + FontAwesome vía CDN, un único `<script>` con estado en memoria `appState`). Sin backend, sin base de datos: los datos están hardcodeados y todo se pierde al recargar. Modelaba, en 9 pantallas, un flujo tipo wizard para registrar la prestación de servicios de una entidad social a sus beneficiarios, con un sistema de estados tipo semáforo y de sanciones acumulativas. Ese sistema de semáforo y las reglas de sanción se mantienen; lo que cambia es quién es quién y cómo se genera el trabajo diario.

## 2. Decisión de arquitectura

Sistema tipo **CRM en red local**, cliente-servidor:

```
OFICINA DE LA ASOCIACIÓN
[PC 1: JavaFX]  [PC 2: JavaFX]  [PC 3: JavaFX] ...
        │              │              │
        └──────────────┴──────────────┘
                    │ REST/HTTPS (red local)
                    ▼
        SERVIDOR FÍSICO (en la propia oficina)
        ├── Backend API — Spring Boot
        ├── Base de datos — PostgreSQL/MySQL
        └── Backups locales
```

- **Cliente**: nativo en **Java (JavaFX)**, sin motor de navegador ni Tailwind. Refuerza el perfil de Java backend usado en la búsqueda de empleo.
- **Backend**: **Spring Boot** (Spring Data JPA, Spring Security + JWT).
- **Servidor**: físico, en la propia oficina de la asociación (no en la Raspberry Pi personal ni en la nube) — datos sensibles (salud, discapacidad) sujetos a RGPD.
- **Backups**: automáticos (`pg_dump` + cron) en disco externo o NAS.

## 3. UX tipo CRM, no wizard

Navegación por **módulos independientes** accesibles en cualquier orden desde un sidebar, con listados filtrables, fichas de detalle y formularios reutilizables — no el flujo lineal del prototipo.

## 4. Modelo de datos (corregido)

### Personal y permisos

**Trabajador** *(antes mal llamado "Usuario" en el prototipo)*
- id, nombre, apellidos, usuario de acceso, contraseña (hash), activo/inactivo
- `rol_id` (FK)

**Rol** *(no es una lista fija — el director/admin crea los cargos que necesite: Recepción, Rehabilitador, Coordinador, etc.)*
- id, nombre, descripción

**Permiso** *(catálogo fijo de capacidades del sistema, definido en código)*
- ej: `GESTIONAR_TRABAJADORES`, `GESTIONAR_CATALOGO_SERVICIOS`, `VER_INFORMES`, `GESTIONAR_PACIENTES`, `CREAR_PLAN_SERVICIO`, `REGISTRAR_ASISTENCIA`, `APLICAR_SANCION`

**RolPermiso** *(tabla intermedia — aquí el director configura qué puede hacer cada rol, sin tocar código)*
- `rol_id`, `permiso_id`

**TipoServicioResponsable** *(permisos específicos por tipo de servicio — la pieza clave del ejemplo real)*
- `tipo_servicio_id`, `rol_id`, `capacidad` (`REGISTRAR_ASISTENCIA` / `APLICAR_SANCION` / `GESTIONAR_PLAN`)
- Permite, por ejemplo: el rol "Recepción" tiene `REGISTRAR_ASISTENCIA` en Rehabilitación; el rol "Rehabilitador" tiene `APLICAR_SANCION` en ese mismo servicio. Configurable por el director, servicio a servicio.

### Beneficiarios y servicios

**Paciente** *(el verdadero beneficiario — antes confundido con "Usuario" en el prototipo)*
- id, nombre, apellidos, nº expediente, fecha nacimiento, género, DNI, teléfono y email opcional
- `asociacion_id` (FK, 1 paciente → 1 asociación)

**Asociación**
- id, nombre, dirección, contacto

**TipoServicio** *(catálogo configurable por admin)*
- id, nombre, icono/color, activo/inactivo

**SubServicio**
- id, `tipo_servicio_id` (FK), nombre, activo/inactivo

**PlanServicio** *(el elemento que faltaba en la v1 — ej. "Ana Valero, rehabilitación, 2 veces por semana, 3 meses")*
- id, `paciente_id`, `tipo_servicio_id`, `sub_servicio_id` (opcional)
- frecuencia (días de la semana), fecha inicio, fecha fin / duración
- estado (activo/finalizado), `creado_por` (trabajador_id — cualquier rol con permiso `CREAR_PLAN_SERVICIO`)
- **Al crearse, el backend genera automáticamente el calendario de `SesionProgramada`** según la frecuencia y duración indicadas.

**SesionProgramada** *(cada visita concreta esperada)*
- id, `plan_servicio_id`, fecha prevista
- estado (PENDIENTE / VERDE / NARANJA / ROJO / AMARILLO / CANCELADA)
- `registrado_por` (trabajador_id), fecha de registro

**Sanción**
- id, `paciente_id`, `plan_servicio_id` (opcional), tipo, fecha, motivo
- `aplicada_por` (trabajador_id — debe tener permiso `APLICAR_SANCION` para ese tipo de servicio)

**InformeGenerado**
- id, fecha de generación, fecha desde, fecha hasta, periodo, tipo de informe (`general` / `servicios`)
- `generado_por` (trabajador_id)
- Conserva el registro de cada informe generado para consulta y regeneración posterior.

### Relaciones

Asociación 1→N Paciente · Rol 1→N Trabajador · Rol N↔N Permiso (vía RolPermiso) · TipoServicio N↔N Rol (vía TipoServicioResponsable) · TipoServicio 1→N SubServicio · Paciente 1→N PlanServicio · PlanServicio 1→N SesionProgramada · Paciente 1→N Sanción

### Reglas de negocio a automatizar en backend

- 3 faltas ROJAS → suspensión 6 meses; 2 ciclos de ROJAS → suspensión definitiva
- 6 faltas NARANJAS → suspensión 6 meses; 2 ciclos de NARANJAS → suspensión definitiva
- Faltas por baja médica (AMARILLO) no computan para suspensión
- 2 tarjetas amarillas (agresión) → suspensión del servicio (prescribe al año)
- Estas reglas se evalúan al marcar una `SesionProgramada`, sobre el histórico del paciente en ese `PlanServicio`

## 5. Flujo real de ejemplo (caso base para diseñar todo lo demás)

1. Alguien con permiso `CREAR_PLAN_SERVICIO` da de alta el plan de Ana Valero: Rehabilitación, 2x/semana, 3 meses.
2. El backend genera automáticamente el calendario de sesiones (`SesionProgramada`) para esos 3 meses.
3. Juan, de Recepción (permiso `REGISTRAR_ASISTENCIA` en Rehabilitación), marca cada sesión como asistida o no según llegan los pacientes.
4. Cuando se acumulan faltas, el rehabilitador responsable (permiso `APLICAR_SANCION` en Rehabilitación) aplica la sanción, guardada ligada a Ana Valero.
5. *(Fuera de alcance por ahora)* De ahí en adelante el caso se gestiona con otro proceso/herramienta, pendiente de definir.

## 6. Endpoints REST

**Auth**
- `POST /auth/login`

**Trabajadores y roles** (admin/director)
- `GET /trabajadores` · `POST /trabajadores` · `PUT /trabajadores/{id}` · `PATCH /trabajadores/{id}/estado`
- `GET /roles` · `POST /roles` · `PUT /roles/{id}`
- `GET /permisos` (catálogo fijo, solo lectura)
- `PUT /roles/{id}/permisos` — asignar permisos generales a un rol
- `PUT /tipos-servicio/{id}/responsables` — asignar qué rol hace qué (`REGISTRAR_ASISTENCIA`/`APLICAR_SANCION`/`GESTIONAR_PLAN`) en ese servicio

**Pacientes**
- `GET /pacientes?asociacionId=&nombre=&page=`
- `GET /pacientes/{id}` (incluye planes, sesiones y sanciones)
- `POST /pacientes` · `PUT /pacientes/{id}` · `PATCH /pacientes/{id}/estado`

**Asociaciones**
- `GET /asociaciones` · `POST /asociaciones` · `PUT /asociaciones/{id}`

**Catálogo de servicios**
- `GET /tipos-servicio` (con subservicios anidados) · `POST /tipos-servicio` · `PUT /tipos-servicio/{id}`
- `POST /tipos-servicio/{id}/subservicios` · `PUT /subservicios/{id}`

**Planes de servicio**
- `GET /planes-servicio?pacienteId=&tipoServicioId=&estado=`
- `POST /planes-servicio` (genera automáticamente las `SesionProgramada`)
- `PUT /planes-servicio/{id}` (ej. finalizar plan)

**Sesiones programadas**
- `GET /sesiones?planServicioId=&desde=&hasta=&estado=` — vista de agenda para recepción
- `PATCH /sesiones/{id}` — marcar estado (VERDE/NARANJA/ROJO/AMARILLO/CANCELADA)

**Sanciones**
- `GET /sanciones?pacienteId=` · `POST /sanciones`

**Informes** (admin)
- `GET /informes/resumen?periodo=mensual|trimestral|semestral|anual&desde=&hasta=`
- `GET /informes/resumen?...&tipoInforme=general|servicios`
- `GET /informes/historial` — listado de informes generados
- `POST /informes/{id}/regenerar` — recalcula un informe histórico con su rango original y crea un nuevo registro

Cada generación se guarda en `informe_generado`, incluyendo rango de fechas, periodo, tipo de informe, trabajador y fecha de generación. El resumen calcula pacientes, sesiones por estado, porcentaje de asistencia, sanciones y planes de servicio. El informe de servicios añade nombre y número de servicios, desglose por sexo y asociación, cancelaciones y porcentaje de cancelaciones.

## 7. Estructura de módulos del cliente JavaFX

Ventana principal con sidebar tras el login. Los módulos visibles dependen de los **permisos del trabajador**, no de un simple flag admin/usuario:

- **Dashboard** — KPIs generales
- **Agenda de sesiones** — vista de recepción: sesiones programadas del día/semana, marcado de asistencia (requiere `REGISTRAR_ASISTENCIA`)
- **Pacientes** — listado filtrable + ficha de detalle con planes, historial de sesiones y sanciones
- **Planes de servicio** — alta de nuevos planes (requiere `CREAR_PLAN_SERVICIO`), genera el calendario automáticamente
- **Sanciones** — aplicar y consultar sanciones (requiere `APLICAR_SANCION` en el servicio correspondiente)
- **Asociaciones** *(requiere permiso de gestión)*
- **Catálogo de servicios** *(requiere `GESTIONAR_CATALOGO_SERVICIOS`)*
- **Trabajadores y roles** *(requiere `GESTIONAR_TRABAJADORES`)* — alta de trabajadores, creación de roles, asignación de permisos generales y por tipo de servicio
- **Informes** *(requiere `VER_INFORMES`)*

El control de acceso se hace en el **backend** (Spring Security evaluando permisos reales), no ocultando botones en el cliente.

## 8. Formularios clave

- **Login**: usuario + contraseña. Los módulos visibles se determinan por los permisos del rol asignado al trabajador.
- **Alta de Trabajador**: nombre, apellidos, usuario, contraseña inicial, rol (combo, con opción de crear uno nuevo).
- **Alta/edición de Rol**: nombre, descripción, checklist de permisos generales, y matriz de responsabilidades por tipo de servicio (qué puede hacer este rol en cada servicio).
- **Alta de Paciente**: nombre, apellidos, nº expediente, fecha nacimiento, género, asociación (combo obligatorio).
- **Alta de Plan de servicio**: paciente (buscador), tipo de servicio, subservicio, frecuencia (días de la semana), fecha inicio, duración/fecha fin → al guardar, genera el calendario de sesiones.
- **Agenda/marcado de sesión**: vista de lista de sesiones del día/semana, con botones de estado tipo semáforo por sesión (equivalente al `ToggleButton` del diseño original).
- **Aplicar sanción**: paciente, plan de servicio (opcional), tipo, motivo.
- **Catálogo de servicios**: nombre del tipo, subservicios editables.
- **Informes**: selector de tipo (`general` / `servicios`), periodo y rango de fechas → tarjetas de KPIs con datos reales, previsualización del PDF, exportación, histórico y regeneración de informes anteriores.

La plantilla del informe se genera como HTML/CSS con cabecera azul marino, tarjetas KPI, tablas, estados tipo badge, resumen operativo y pie de página. La previsualización exporta temporalmente el mismo PDF final y lo abre con el visor predeterminado del sistema, garantizando que la vista coincide con el archivo guardado.

## 9. Orden de construcción recomendado

1. Modelo de datos + backend con CRUD básico de Trabajador/Rol/Permiso y Paciente/Asociación
2. Login + autorización basada en permisos (Spring Security/JWT)
3. Cliente JavaFX: login + listado de pacientes conectado de verdad
4. Módulo de Planes de servicio + generación automática de calendario de sesiones
5. Módulo de Agenda/marcado de asistencia (recepción)
6. Reglas de sanciones automáticas + módulo de aplicar sanción manual
7. Módulo de Trabajadores y roles (gestión de permisos por el director)
8. Módulo de informes con cálculos reales, histórico y exportación PDF
9. Empaquetado con `jpackage` + instalación piloto en un puesto (DMG macOS Apple Silicon preparado)
10. Rollout al resto de puestos de la oficina

## 10. Despliegue

- Servidor: JDK + PostgreSQL/MySQL + backend como servicio del sistema (systemd/servicio Windows) con arranque automático
- Cliente: empaquetado nativo con `jpackage` (incluye el runtime de Java, no requiere instalación previa en cada PC)
- IP del servidor configurable desde el cliente (`.properties` o pantalla de configuración inicial), sin necesidad de recompilar si cambia
- Para macOS Apple Silicon, el cliente se empaqueta como DMG mediante `client/package-macos.sh`.
- La URL externa se lee desde `~/.crm-asociaciones/client.properties`; el backend no se incluye en el DMG.
- El DMG se ha generado y validado en `client/target/jpackage-output/` con versión de paquete macOS `1.0.0`.
- El launcher usa una clase de entrada independiente (`com.aitsolutions.crmclient.Launcher`) para iniciar JavaFX correctamente desde el `.app`.

## 11. Alta de servicios desde la ficha del paciente (confirmado)

Al dar de alta a un paciente, los servicios (`PlanServicio`) **no son obligatorios en el mismo paso** — se pueden añadir después, desde la propia ficha del paciente, de forma independiente unos de otros. Por cada servicio añadido:

- Se define el/los días de la semana y la duración (frecuencia + periodo), como ya recoge `PlanServicio`.
- El plan debe ser **editable después de creado**: cambiar días, ampliar/acortar el periodo.
- Debe poderse **eliminar una sesión suelta** (`SesionProgramada` puntual) sin afectar al resto del plan, o **eliminar el plan completo** (todas las sesiones futuras, o todas incluyendo el histórico — a definir).

Esto implica en el backend, además de `POST /planes-servicio`:
- `PUT /planes-servicio/{id}` para editar días/duración (regenerando las `SesionProgramada` futuras afectadas)
- `DELETE /sesiones/{id}` para borrar una sesión puntual
- `DELETE /planes-servicio/{id}` (o `PATCH .../estado` si se prefiere baja lógica) para borrar/cancelar el plan completo

El propio Cristian señala que el detalle fino de este comportamiento (cómo se regeneran sesiones al editar, si el borrado es lógico o físico, etc.) se irá afinando con las versiones — no se cierra aquí, solo se deja constancia de la necesidad.

## 12. Pendiente de decidir más adelante

- Qué ocurre con las `SesionProgramada` cuando un paciente falta justificadamente de forma puntual: ¿se reprograma la sesión o simplemente queda marcada como NARANJA sin reposición?
- Qué pasa exactamente "después" de aplicar una sanción — el propio Cristian indica que se gestiona con otra herramienta/proceso; queda fuera de alcance de este sistema por ahora.
- Número final de puestos concurrentes en la oficina.
- Si Asociaciones y el catálogo de servicios deben ser editables por más roles que el director, o quedan centralizados en un permiso único de gestión.
- Detalle exacto de edición/borrado de planes y sesiones (ver apartado 11) — a definir en versiones posteriores.
