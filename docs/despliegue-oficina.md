# Despliegue en los puestos de la oficina

## Arquitectura

- El backend y MySQL se ejecutan en un único servidor de la oficina.
- Cada puesto instala únicamente el cliente JavaFX.
- Todos los clientes apuntan a la misma URL del backend mediante configuración externa.

## Preparar el servidor

1. Arrancar MySQL y aplicar el esquema de `crm-asociaciones-schema.sql`.
2. Configurar las credenciales en `backend/src/main/resources/application.properties`.
3. Arrancar el backend con `cd backend && mvn spring-boot:run` o instalarlo como servicio.
4. Comprobar desde cada puesto que `http://IP-DEL-SERVIDOR:8080/auth/login` es accesible.

## Puestos macOS Apple Silicon

Generar en un Mac Apple Silicon:

```bash
cd client
./package-macos.sh
```

Instalar el DMG generado y crear `~/.crm-asociaciones/client.properties`:

```properties
api.base-url=http://IP-DEL-SERVIDOR:8080
```

## Puestos Windows

El instalador debe generarse en Windows con JDK 17 o superior:

```powershell
cd client
.\package-windows.ps1
```

El resultado queda en `client/target/jpackage-output/CRM Asociaciones-1.0.0.exe`.
Ejecutar el instalador en cada puesto.

Después de instalar, crear este archivo:

```text
%USERPROFILE%\.crm-asociaciones\client.properties
```

Con el contenido:

```properties
api.base-url=http://IP-DEL-SERVIDOR:8080
```

El instalador incluye el runtime de Java y no requiere instalar Java aparte.

## Comprobación por puesto

1. Abrir el cliente.
2. Iniciar sesión con un trabajador válido.
3. Confirmar que aparecen solo los módulos permitidos por su rol.
4. Probar una consulta de pacientes y una operación autorizada.
5. Confirmar que el cliente sigue funcionando tras reiniciarlo.

No copiar credenciales ni la base de datos a los puestos cliente.
