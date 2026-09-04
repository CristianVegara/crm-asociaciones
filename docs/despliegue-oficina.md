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

### Empaquetar e instalar el backend

En el servidor, con Java 17 o superior:

```bash
cd backend
./package-backend.sh
```

Copiar `backend/target/distribution/crm-asociaciones-backend.jar` a
`/opt/crm-asociaciones/crm-asociaciones-backend.jar` y configurar las credenciales
de MySQL en `backend/src/main/resources/application.properties` (o mediante las
variables de entorno equivalentes). En Linux se puede instalar el servicio incluido:

```bash
sudo cp docs/crm-asociaciones-backend.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now crm-asociaciones-backend
sudo systemctl status crm-asociaciones-backend
```

No incluir credenciales en el repositorio ni en los instaladores de los clientes.

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

## Orden recomendado del rollout

1. Configurar el servidor y validar el login desde el propio servidor.
2. Instalar el cliente en un puesto piloto y probar pacientes, planes, agenda,
   sanciones e informes con los permisos correspondientes.
3. Instalar el cliente en los cuatro puestos restantes usando la misma URL.
4. Crear una cuenta individual por trabajador; no compartir el usuario `admin`.
5. Registrar la versión instalada y validar que cada puesto puede reconectarse
   después de reiniciar.

No copiar credenciales ni la base de datos a los puestos cliente.
