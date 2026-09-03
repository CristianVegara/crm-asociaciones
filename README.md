# CRM de gestión de asociaciones

Monorepo del sistema cliente-servidor descrito en [`docs/crm-asociaciones-plan.md`](./docs/crm-asociaciones-plan.md).
Ese documento es la fuente de verdad del proyecto: modelo de dominio, endpoints, reglas de negocio y orden de construcción (apartado 9).

## Estructura

```
crm-asociaciones/
├── docs/
│   └── crm-asociaciones-plan.md   ← planificación acordada, no modificar salvo decisión explícita
├── backend/                        ← Spring Boot + Spring Data JPA + MySQL
└── client/                         ← JavaFX (login + listado de pacientes, ver "Cliente JavaFX" abajo)
```

## Progreso (según apartado 9 del plan)

- [x] 1. Modelo de datos + backend con CRUD básico de Trabajador/Rol/Permiso y Paciente/Asociación
- [x] 2. Login + autorización basada en permisos (Spring Security/JWT)
- [x] 3. Cliente JavaFX: login + listado de pacientes conectado de verdad
- [x] 4. Módulo de Planes de servicio + generación automática de calendario de sesiones
- [x] 5. Módulo de Agenda/marcado de asistencia (recepción)
- [x] 6. Reglas de sanciones automáticas + módulo de aplicar sanción manual
- [x] 7. Módulo de Trabajadores y roles (gestión de permisos por el director)
- [x] 8. Módulo de informes con cálculos reales
- [x] 9. Empaquetado con `jpackage` + instalación piloto (DMG macOS Apple Silicon preparado)
- [ ] 10. Rollout al resto de puestos

### Empaquetado macOS Apple Silicon

El cliente se puede empaquetar como DMG desde un Mac Apple Silicon:

```bash
cd client
chmod +x package-macos.sh
./package-macos.sh
```

El resultado queda en `client/target/jpackage-output/`. El instalador no incluye el
backend: debe estar ejecutándose en el servidor de la oficina.

La URL del backend se configura externamente en:

```text
~/.crm-asociaciones/client.properties
```

Ejemplo:

```properties
api.base-url=http://localhost:8080
```

## Backend — cómo arrancar

1. Crear la base de datos en MySQL: `CREATE DATABASE crm_asociaciones;`
2. Ajustar credenciales en `backend/src/main/resources/application.properties`
3. `cd backend && mvn spring-boot:run`

La API arranca en `http://localhost:8080`.

### Autenticación

Todos los endpoints salvo `/auth/login` requieren un JWT válido en la cabecera `Authorization: Bearer <token>`.
El token dura 8 horas y lleva embebidos los permisos del trabajador (así el filtro no consulta la BD en cada petición).

La primera vez que arranca contra una base de datos vacía, se crea automáticamente un
trabajador inicial (usuario `admin`, contraseña `admin1234`, rol "Director" con todos los
permisos) — mira el log al arrancar, ahí se confirma. Cámbiale la contraseña en cuanto
entres con `PUT /trabajadores/{id}`.

```bash
# 1. Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usuario":"admin","password":"admin1234"}'

# 2. Usar el token devuelto en cualquier endpoint protegido
curl http://localhost:8080/pacientes \
  -H "Authorization: Bearer <token-devuelto>"
```

### Permisos por módulo

| Módulo | Permiso requerido |
|---|---|
| `/trabajadores`, `/roles` | `GESTIONAR_TRABAJADORES` |
| `/pacientes`, `/asociaciones` | `GESTIONAR_PACIENTES` |
| `/tipos-servicio`, `/subservicios` | `GESTIONAR_CATALOGO_SERVICIOS` |
| `/planes-servicio`, `DELETE /sesiones/{id}` | `CREAR_PLAN_SERVICIO` |
| `GET /sesiones`, `PATCH /sesiones/{id}` | `REGISTRAR_ASISTENCIA` |
| `/sanciones` | `APLICAR_SANCION` |
| `/informes` | `VER_INFORMES` |
| `/permisos` | cualquier trabajador autenticado |

El historial de informes se consulta con `GET /informes/historial`. Para regenerar
uno se usa `POST /informes/{id}/regenerar`; se recalculan sus fechas originales y
se crea un nuevo registro de historial, conservando el original.

## Cliente JavaFX — cómo arrancar

Con el backend ya corriendo (ver arriba):

```
cd client && mvn javafx:run
```

Login de prueba: `admin` / `admin1234` (el mismo trabajador que crea el seed del backend).
Tras el login verás el menú principal, con accesos a Pacientes, Sanciones y Trabajadores y roles
según los permisos del rol del trabajador (el "Director" del seed los tiene todos).

Por defecto apunta a `http://localhost:8080`. Para apuntar a otra IP (útil cuando el
backend esté en el servidor de la oficina y no en tu propio Mac) crea un archivo
`client.properties` junto al ejecutable con:

```properties
api.base-url=http://IP-DEL-SERVIDOR:8080
```

## Decisiones fijadas — Informes (paso 8)

- **`periodo` es solo una etiqueta**: decisión tomada con Cristian — el cálculo siempre usa `desde`/`hasta` explícitos (ambos obligatorios), nunca se infiere un rango a partir de "mensual"/"trimestral"/etc. El combo del cliente con esas opciones no cambia el cálculo, solo se manda como texto informativo.
- **Dos campos nuevos en el modelo**: `Paciente.fechaAlta` y `PlanServicio.fechaCreacion`/`fechaFinalizacion` no existían — hacían falta para poder calcular "inscripciones nuevas" y "planes creados/finalizados en el periodo". `fechaFinalizacion` se rellena automáticamente en `PlanServicioService.cambiarEstado()` al pasar a `FINALIZADO`.
- **AMARILLO fuera del % de asistencia**: igual que en las reglas de sanción del paso 6, las faltas por baja médica no computan — el % de asistencia se calcula como `VERDE / (VERDE + NARANJA + ROJO)`, excluyendo tanto AMARILLO como PENDIENTE.
- **Sin permiso fino por servicio**: `VER_INFORMES` no está entre las tres capacidades de `TipoServicioResponsable` (`REGISTRAR_ASISTENCIA`/`APLICAR_SANCION`/`GESTIONAR_PLAN`), así que este módulo solo depende del permiso general — es un informe agregado de toda la asociación, no tiene sentido acotarlo a un servicio.

## Decisiones fijadas — rediseño de navegación y estilos (feedback tras el paso 7)

- **Barra de navegación persistente**: se sustituyó el "Menú principal" intermedio (una pantalla aparte a la que había que volver para cambiar de módulo) por un **shell** (`shell.fxml` / `ShellController`) con una barra superior siempre visible. Cambiar de módulo ahora es un clic, no dos, y no se pierde el contexto de en qué pantalla se está.
- **Una sola escena tras el login**: el shell se carga una vez; cambiar de módulo solo reemplaza el contenido central (`StackPane`), no la ventana entera. Las pantallas de Pacientes/Sanciones/Trabajadores ya no tienen su propio botón "Menú" ni gestionan su propia navegación.
- **Hoja de estilos común** (`styles.css`): antes no había ninguna, por eso se veía todo plano (tema por defecto de JavaFX sin tocar). Ahora hay clases reutilizables (`.card` para paneles de formulario, `.screen-header`/`.screen-title` para cabeceras, `.nav-bar`/`.nav-button-active` para la navegación, contraste en filas de tabla) aplicadas de forma consistente en todas las pantallas.
- **Activar/desactivar trabajador**: añadido en la pestaña Trabajadores (botón + selección en tabla, llama a `PATCH /trabajadores/{id}/estado`, que ya existía en el backend desde el paso 1 pero no tenía UI). **No hay borrado físico de trabajadores** — es una decisión de diseño deliberada, igual que con planes y pacientes: perder el registro de quién creó un plan, marcó una sesión o aplicó una sanción rompería la trazabilidad del sistema.

## Decisiones fijadas — Trabajadores y roles / autorización fina (paso 7)

- **Cliente JavaFX incluido**: a petición de Cristian, este paso también trae pantallas — y de paso las del paso 6 (Sanciones), que se había dejado solo en backend. Se añadió una pantalla de **Menú principal** tras el login (antes iba directo al listado de pacientes), con accesos ocultos según los permisos del trabajador logueado.
- **Pantalla de Trabajadores y roles**: tres pestañas — Trabajadores (tabla + alta), Roles (tabla + checklist de permisos generales, cargados dinámicamente desde `GET /permisos` en vez de hardcodearlos en el cliente), y Servicios (matriz rol × capacidad para `TipoServicioResponsable`, generada dinámicamente porque el número de roles no es fijo).
- **Pantalla de Sanciones**: búsqueda por id de paciente + formulario de sanción manual (tipo, motivo, plan opcional).
- **Dos capas de autorización, no una**: el permiso general (`REGISTRAR_ASISTENCIA`, `APLICAR_SANCION`, `CREAR_PLAN_SERVICIO` vía `@PreAuthorize`) sigue siendo la puerta de entrada al módulo; `TipoServicioResponsable` decide, dentro de ese módulo, para qué tipos de servicio concretos puede actuar el rol del trabajador. Las dos comprobaciones se aplican juntas.
- **Nomenclatura**: la capacidad fina se llama `GESTIONAR_PLAN` (no `CREAR_PLAN_SERVICIO`) porque así la nombra el propio apartado 4 del plan para las capacidades por servicio; el permiso general y la capacidad fina son conceptos relacionados pero con nombres distintos a propósito.
- **Sanciones automáticas no piden capacidad fina**: cuando el sistema crea una sanción automática (paso 6) no comprueba `APLICAR_SANCION` del trabajador que estaba marcando asistencia — es una consecuencia del sistema, no una acción manual suya. Solo `crearManual()` la comprueba (y únicamente si la sanción está ligada a un plan; si no, no hay servicio contra el que comprobar).
- **403 consistente**: `AutorizacionServicioService` lanza `AccessDeniedException` (la misma que usa Spring Security para `@PreAuthorize`), así que un fallo de capacidad fina da el mismo `403` que un fallo de permiso general, sin código adicional.

## Decisiones fijadas — Sanciones (paso 6)

- **AMARILLO no es lo mismo que "tarjeta amarilla"**: el apartado 4 del plan tenía una contradicción (AMARILLO en el semáforo de asistencia = falta justificada que no cuenta, pero también hablaba de "2 tarjetas amarillas por agresión" como causa de suspensión). Se resolvió como dos conceptos distintos: `TipoSancion.TARJETA_AMARILLA_AGRESION` es una sanción disciplinaria manual, sin ninguna relación con marcar una sesión como AMARILLO.
- **Alcance de las reglas automáticas**: solo el primer nivel — al alcanzar exactamente 3 faltas ROJAS o 6 NARANJAS en el histórico de un plan, se crea una `Sancion` automática (`SUSPENSION_TEMPORAL`, `automatica: true`). No se cuentan "ciclos" ni se escala a suspensión definitiva todavía — el propio apartado 5 del plan deja explícitamente fuera de alcance qué pasa "después" de una sanción.
- **Dónde se evalúa**: en `SancionService.evaluarReglasAutomaticas()`, llamado desde `SesionProgramadaService.marcarEstado()` justo después de guardar. Se dispara solo en el momento exacto en que se alcanza el umbral (no en cada falta posterior), para no duplicar sanciones.
- **Limitación conocida**: si una sesión que disparó una sanción automática se vuelve a marcar con otro estado, la sanción ya creada no se revierte. Aceptable para el MVP, documentado aquí para no olvidarlo.

## Decisiones fijadas — Agenda / marcado de asistencia (paso 5)

- **Permiso general, no por servicio todavía**: `REGISTRAR_ASISTENCIA` es de momento un permiso general de rol (vía `PUT /roles/{id}/permisos`), no específico por tipo de servicio. `TipoServicioResponsable` (qué rol puede marcar QUÉ servicio, el ejemplo Recepción/Rehabilitador del apartado 4) se implementa en el paso 7, junto con el resto de gestión de roles.
- **No se puede volver a `PENDIENTE`**: `PATCH /sesiones/{id}` solo acepta VERDE/NARANJA/ROJO/AMARILLO. `PENDIENTE` es exclusivamente el estado inicial que pone el generador de calendario.
- **Sanciones automáticas, todavía no**: marcar una sesión no dispara aún ninguna regla de sanción (3 ROJAS → suspensión, etc.) — eso es el paso 6. Hay un `TODO` explícito en `SesionProgramadaService.marcarEstado()` señalando dónde enganchará.

## Decisiones fijadas — Planes de servicio (paso 4)

- **Fecha fin flexible** (pediste que quedara abierto): `POST /planes-servicio` acepta `fechaFin` explícita **o** `duracionSemanas`, nunca las dos a la vez. Si se manda duración, se calcula `fechaFin = fechaInicio + duracionSemanas semanas - 1 día`.
- **Edición con regeneración** (apartado 11, ya no pendiente): `PUT /planes-servicio/{id}` permite cambiar días de la semana y/o duración. Solo se tocan las sesiones futuras que siguen `PENDIENTE`; las ya marcadas (asistencia o falta) son historial y nunca se borran ni regeneran, sea cual sea su fecha.
- **Borrado de sesión suelta**: `DELETE /sesiones/{id}` solo funciona si la sesión sigue `PENDIENTE` — evita borrar accidentalmente un registro de asistencia o de falta ya aplicado.
- **Baja de plan completo**: se implementó como **baja lógica** (`PATCH /planes-servicio/{id}/estado` → `FINALIZADO`), no como `DELETE` físico. El apartado 12 del plan dejaba esto pendiente; se decidió así porque el sistema maneja datos de salud/discapacidad sujetos a RGPD, y conservar el historial es más seguro que un borrado irreversible. Al finalizar, se eliminan las sesiones futuras que seguían pendientes.
- **Catálogo de Tipos de servicio / Subservicio**: no es su propio paso en el plan, pero es una dependencia obligatoria de Plan de servicio (FK `tipo_servicio_id`), así que se construyó como prerrequisito de este mismo paso.

## Decisiones fijadas — backend

- Base de datos: **MySQL**
- Sin Lombok: getters/setters explícitos para que el código sea legible sin depender de generación de bytecode en tiempo de compilación (encaja con el perfil "Java backend junior" que se está construyendo en la búsqueda de empleo).
- DTOs separados de las entidades JPA en cada módulo (nunca se exponen entidades directamente en los controladores).

## Decisiones fijadas — cliente

- Vistas en **FXML**, sin Scene Builder usado todavía pero compatibles con él.
- Sin framework de navegación: con dos pantallas, `MainApp` cambiando la raíz de la escena es suficiente. Se revisará si el número de módulos crece (pasos 4+).
- `java.net.http.HttpClient` (incluido en el JDK) en vez de una librería HTTP externa — la única dependencia añadida es Jackson, para (de)serializar JSON.
- Las llamadas de red siempre corren en un `Task` en un hilo aparte, nunca en el hilo de JavaFX (si no, la ventana se congelaría mientras espera al backend).
