# CRM de gestión de asociaciones — documento de planificación

> Origen: análisis y evolución del prototipo `erika.html` (mockup HTML/Tailwind de un solo archivo, sin backend ni persistencia). Este documento recoge la planificación acordada para construir la versión real como sistema cliente-servidor en red local.
>
> **Revisión 2**: corrige el modelo de dominio inicial. Lo que en el prototipo se llamaba "Usuario" son en realidad **trabajadores de la asociación** (admin/director, coordinador, recepción, rehabilitador...). Los verdaderos beneficiarios del servicio son **pacientes**, que siguen **planes de servicio** (ej. "rehabilitación 2 veces por semana durante 3 meses") con un calendario de sesiones que el personal va marcando como asistidas o no.
>
> **Revisión 3 (03/09/2026)**: se implementa el módulo de informes con datos reales, histórico persistente, regeneración, previsualización y exportación a PDF con plantilla HTML/CSS. También se documentan correcciones de navegación JavaFX, inicialización idempotente y compatibilidad de ejecución con JDK 26.
>
> **Revisión 4 (04/09/2026)**: se implementa la ficha de paciente del paso 11 en JavaFX,
> con consulta y gestión de planes, sesiones y sanciones mediante los endpoints REST existentes.
> `GET /pacientes/{id}` devuelve además una colección de sesiones agregada (y las sesiones
> siguen anidadas en cada plan para conservar su contexto).
>
> **Revisión 5 (04/09/2026)**: se fijan las decisiones funcionales del apartado 12:
> las faltas justificadas se marcan como AMARILLO sin reprogramación, las sanciones solo se
> registran en este sistema, se soportan inicialmente hasta 5 puestos concurrentes y la
> gestión de asociaciones y catálogo depende del permiso asignado por el Director.
>
> **Revisión 6 (04/09/2026)**: se implementa el módulo JavaFX de Agenda, con consulta por
> rango de fechas y estado, marcado de asistencia y visualización semafórica.
>
> **Revisión 7 (04/09/2026)**: se prepara el rollout de oficina con empaquetado del backend,
> servicio systemd opcional y checklist para el puesto piloto y hasta cinco clientes.
>
> **Revisión 8 (04/09/2026)**: se incorporan el Dashboard de KPIs y el módulo independiente
> de catálogo de tipos y subservicios.
>
> **Revisión 9 (04/09/2026)**: se traducen los días de la ficha del paciente, se ajusta el
> espaciado general de la interfaz y se aclara que el catálogo administra servicios; la
> asignación de servicios a pacientes se realiza desde Planes.
>
> **Revisión 10 (04/09/2026)**: se incorpora auditoría técnica de accesos y operaciones HTTP,
> consultable únicamente con `GESTIONAR_TRABAJADORES`; no se almacenan credenciales ni tokens.
>
> **Revisión 11 (04/09/2026)**: se añade la pantalla JavaFX de auditoría con filtrado por
> usuario, método y ruta.
>
> **Revisión 12 (04/09/2026)**: la auditoría incorpora eventos de negocio para altas,
> ediciones, cambios de estado y cancelaciones de pacientes, planes, sesiones y sanciones.
>
> **Revisión 13 (04/09/2026)**: se incorpora el módulo independiente de asociaciones con
> listado, alta y edición, protegido por `GESTIONAR_PACIENTES`.

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
- estado (activo/finalizado/cancelado), `creado_por` (trabajador_id — cualquier rol con permiso `CREAR_PLAN_SERVICIO`)
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
- `PUT /planes-servicio/{id}` (edita días/duración y regenera pendientes futuras)
- `PATCH /planes-servicio/{id}/estado` (cancela/finaliza conservando historial)

**Sesiones programadas**
- `GET /sesiones?planServicioId=&desde=&hasta=&estado=` — vista de agenda para recepción
- `PATCH /sesiones/{id}` — marcar estado (VERDE/NARANJA/ROJO/AMARILLO/CANCELADA)
- `DELETE /sesiones/{id}` — borrar una sesión puntual únicamente si sigue PENDIENTE

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
- Para el rollout multiplataforma se añade `client/package-windows.ps1`, que genera un instalador `.exe` desde Windows; la distribución y configuración por puesto se documentan en `docs/despliegue-oficina.md`.

## 11. Alta de servicios desde la ficha del paciente (confirmado)

Al dar de alta a un paciente, los servicios (`PlanServicio`) **no son obligatorios en el mismo paso** — se pueden añadir después, desde la propia ficha del paciente, de forma independiente unos de otros. Por cada servicio añadido:

- Se define el/los días de la semana y la duración (frecuencia + periodo), como ya recoge `PlanServicio`.
- El plan debe ser **editable después de creado**: cambiar días, ampliar/acortar el periodo.
- Debe poderse **eliminar una sesión suelta** (`SesionProgramada` puntual) sin afectar al resto del plan, o **cancelar el plan completo** mediante baja lógica, conservando sesiones e histórico.

Esto implica en el backend, además de `POST /planes-servicio`:
- `PUT /planes-servicio/{id}` para editar días/duración (regenerando las `SesionProgramada` futuras afectadas)
- `DELETE /sesiones/{id}` para borrar una sesión puntual
- `PATCH /planes-servicio/{id}/estado` con `CANCELADO` para cancelar el plan completo

El propio Cristian señala que el detalle fino de este comportamiento (cómo se regeneran sesiones al editar, si el borrado es lógico o físico, etc.) se irá afinando con las versiones — no se cierra aquí, solo se deja constancia de la necesidad.

Decisión aplicada: un plan cancelado conserva sesiones e histórico, y no puede editarse ni reactivarse.
Al editar un plan se regeneran las sesiones futuras PENDIENTES según la nueva frecuencia y
fechas; las sesiones ya registradas se conservan. El borrado de una sesión es lógico:
pasa a `CANCELADA`. Al cancelar un plan se cancelan sus sesiones futuras pendientes.

## 12. Decisiones confirmadas

- Una falta justificada se marca como `AMARILLO` y no genera reprogramación.
- La aplicación solo registra la sanción; la ejecución posterior queda fuera de alcance.
- El despliegue inicial debe soportar hasta 5 puestos concurrentes.
- Asociaciones y catálogo de servicios pueden gestionarse desde cualquier rol con el permiso asignado por el Director.
- Al editar un plan se regeneran las sesiones futuras pendientes y se conserva el histórico.
- El borrado de una sesión es lógico: pasa a `CANCELADA`.
- Al cancelar un plan se conserva el histórico y se cancelan sus sesiones futuras pendientes.

## 13. Catálogo inicial de servicios

El sistema inicializa de forma idempotente estos tipos de servicio, manteniendo su gestión
posterior desde el CRM: Ambulancia, Psicología, Rehabilitación, Transporte, Ayuda a domicilio
y Trabajo social. Cada tipo incluye un icono y un color visual propios.

## 14. Módulo global de planes

Además del alta contextual desde la ficha del paciente, el cliente JavaFX incluye
un módulo global de **Planes**. Permite listar y filtrar planes por paciente,
servicio y estado, crear un plan seleccionando el paciente y cancelar planes
manteniendo el histórico. El acceso requiere `CREAR_PLAN_SERVICIO`.
