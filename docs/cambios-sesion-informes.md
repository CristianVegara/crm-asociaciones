# Cambios realizados en la sesión — CRM Asociaciones

**Fecha:** 03/09/2026

## Base de datos y datos de prueba

- Se definió `prompt-sql-bbdd.md` y se generó `crm-asociaciones-schema.sql`.
- El esquema incluye tablas, relaciones, restricciones, índices, la semilla del rol `Director` y la tabla `informe_generado`.
- Se creó `seed-informe-prueba.sql` con datos ficticios para pacientes, planes, sesiones y sanciones.
- La inicialización del rol `Director` reutiliza el rol existente y evita duplicidades.
- `RolRepository` incorpora la búsqueda por nombre.

## Diagnóstico de MySQL y permisos

- Se identificó que MySQL se estaba ejecutando con `--skip-networking`, impidiendo conexiones TCP.
- Se revisó la diferencia entre `crm_app@localhost` y `crm_app@127.0.0.1`.
- Se indicó que la contraseña de `application.properties` debe coincidir con la de MySQL y que `crm_app` debe tener permisos sobre `crm_asociaciones`.
- Tras cambiar permisos, es necesario cerrar sesión y volver a autenticarse porque los permisos forman parte del JWT.

## Navegación al módulo de informes

- Se revisó el flujo del botón **Informes** en `ShellController`.
- Se añadieron mensajes visibles cuando no se puede cargar una pantalla.
- Se corrigió `informe-screen.fxml`, cuyo atributo de texto inicial con `%` provocaba `No resources specified`.
- El módulo pasó a cargarse correctamente desde la barra superior.

## Backend de informes

- Se implementó el resumen con datos reales de pacientes, sesiones, sanciones y planes.
- Se añadieron `InformeGenerado`, `InformeGeneradoRepository` y `InformeHistorialResponse`.
- Se añadieron estos endpoints:

```text
GET  /informes/resumen
GET  /informes/historial
POST /informes/{id}/regenerar
```

- Cada generación se registra con rango de fechas, periodo, usuario y fecha.
- Regenerar conserva el registro original y crea otro con datos recalculados.
- La asistencia se calcula con sesiones `VERDE`, `NARANJA` y `ROJO`; `AMARILLO` y `PENDIENTE` quedan fuera del denominador.

## Cliente JavaFX y PDF

- La pantalla de informes incluye fechas, periodo, KPI, desglose de sesiones, sanciones, planes, histórico y regeneración.
- `InformeHtmlBuilder` genera la plantilla HTML/CSS con cabecera azul marino, secciones con barra azul, tarjetas KPI, tablas, badges, resumen operativo y pie.
- `InformePdfExporter` convierte la plantilla a PDF mediante OpenHTMLToPDF/PDFBox.
- La previsualización genera el PDF real en un archivo temporal y lo abre con el visor predeterminado del sistema.
- Se corrigió el `%` del CSS que interfería con `String.formatted(...)`.

## Compatibilidad y validación

- `javafx-web` requería el módulo `jdk.jsobject`, no disponible al ejecutar con JDK 26.
- Se retiró `javafx-web`; la previsualización ya no depende de `WebView`.
- Se validó correctamente:

```bash
cd client
mvn clean test
mvn javafx:run
```

Los avisos de `restricted method` y `Unsafe` son advertencias de JavaFX/JDK y no impiden la ejecución.

## 8. Empaquetado piloto

- Se preparó `client/package-macos.sh` para generar un DMG de macOS Apple Silicon.
- El script compila el cliente, copia sus dependencias runtime y ejecuta `jpackage`.
- El backend no se incluye en el instalador.
- La configuración externa se busca primero en `~/.crm-asociaciones/client.properties` y, como alternativa de desarrollo, en `client.properties` del directorio actual.
- El DMG se generó correctamente en `client/target/jpackage-output/`.
- Tras probarlo, se corrigió el arranque del `.app` añadiendo `Launcher`, una entrada Java estándar que invoca `Application.launch`.
- El DMG regenerado se probó ejecutando directamente su binario macOS y JavaFX inició correctamente.

## 7. Ampliación solicitada por el cliente

- Se añadió el selector de `Informe general` e `Informe de servicios`.
- El informe de servicios incluye nombre y número de servicios, distribución por sexo y asociación, número de cancelaciones y porcentaje de cancelaciones.
- Se añadió `CANCELADA` como estado de sesión para que las cancelaciones sean datos reales y no una inferencia de otros estados.
- El tipo de informe queda guardado en `informe_generado` y se conserva al regenerar.
- La selección se realiza antes de generar entre `Informe general` e `Informe de servicios`.
- El estado `CANCELADA` permite calcular cancelaciones reales y su porcentaje.
- El informe de servicios muestra nombre y número de servicios, distribución por sexo y asociación.

## Archivos principales

- `backend/src/main/java/com/aitsolutions/crm/informe/InformeController.java`
- `backend/src/main/java/com/aitsolutions/crm/informe/InformeService.java`
- `backend/src/main/java/com/aitsolutions/crm/informe/InformeGenerado.java`
- `backend/src/main/java/com/aitsolutions/crm/informe/InformeGeneradoRepository.java`
- `client/src/main/java/com/aitsolutions/crmclient/informe/InformeScreenController.java`
- `client/src/main/java/com/aitsolutions/crmclient/informe/InformeHtmlBuilder.java`
- `client/src/main/java/com/aitsolutions/crmclient/informe/InformePdfExporter.java`
- `client/src/main/resources/com/aitsolutions/crmclient/informe-screen.fxml`
- `client/pom.xml`
- `docs/crm-asociaciones-schema.sql`
### Mejoras de pacientes y visualización

- El formulario de Pacientes permite registrar nombre, apellidos, expediente, asociación,
  fecha de nacimiento, género, DNI, teléfono y email opcional.
- La entidad, DTOs y esquema SQL incorporan los nuevos datos de contacto (JPA actualiza
  automáticamente la base de datos en desarrollo).
- El listado de pacientes carga las asociaciones disponibles y muestra un formulario de alta.
- La pantalla de Informes usa ahora un `ScrollPane` y columnas con ancho flexible para evitar
  que los indicadores y el historial queden ocultos en ventanas pequeñas.
- Sanciones permite seleccionar al paciente por nombre, apellidos y expediente mediante un
  selector cargado desde el backend, eliminando la introducción manual del ID.
- El historial ocupa ahora todo el cuerpo de Informes; se retiró el panel de indicadores
  duplicado de la pantalla y se mantiene la exportación/regeneración.
- El alta de pacientes se abre desde un botón en un diálogo independiente.
- Los informes PDF adoptan la plantilla ejecutiva solicitada (cabecera azul marino,
  métricas destacadas, tablas, badges de estado y resumen operativo).
- La pantalla vuelve a ofrecer «Previsualizar PDF» después de generar o regenerar un informe,
  abriendo el PDF temporal final con el visor predeterminado del sistema.
