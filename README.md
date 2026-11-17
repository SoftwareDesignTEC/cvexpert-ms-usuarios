# cvexpert-ms-usuarios

Microservicio de **Usuarios** de CVExpert (Fase II). Guarda el perfil profesional y
el rol (usuario o administrador), los documentos legales y los contactos de ayuda.

- Spring Boot 3.3 · Java 17 · MongoDB (Cosmos DB con API de MongoDB en Azure)
- Arquitectura en capas: `controller` → `service` → `repository`
- Responsable: Gabriel Cabrera

## Contrato

El contrato está en [`openapi.yaml`](openapi.yaml). Son las 7 operaciones que los
clientes ya usan desde la Fase I:

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/profiles/{id}` | Devuelve el perfil y lo crea si es la primera vez |
| PUT | `/profiles/{id}` | Actualización parcial del perfil |
| GET | `/profiles/{id}/sessions` | Dispositivos desde los que se usó la cuenta (paginado) |
| GET | `/profiles/{id}/export` | Descarga de los datos que guarda Usuarios |
| GET | `/legal/privacy` | Política de privacidad vigente (`?lang=es\|en`) |
| GET | `/legal/terms` | Términos y condiciones vigentes (`?lang=es\|en`) |
| GET | `/help` | Contactos del centro de ayuda (`?lang=es\|en`) |

Reglas que aplica este servicio:

- **Identidad.** No recibe el token de Google. APIM lo valida y agrega `X-User-Id`
  (`sub`) y `X-User-Email`. Sin esos encabezados las rutas de perfil responden 403.
- **`{id}`** puede ser el `sub` (lo usa la web) o el correo (lo usa Android desde la
  Fase I). Si no coincide con el usuario autenticado, responde 403.
- **Rol.** Los correos de `ADMIN_EMAILS` quedan como `admin`. El primero es
  gcabrerarojas.25@gmail.com.
- **Fechas** en epoch, milisegundos UTC.
- **Certificado.** En Azure solo se aceptan llamadas con el certificado de cliente de
  APIM. El App Service lo pide en el TLS (modo `Required`, salvo `/actuator/health`) y
  `ApimCertificateFilter` compara su huella con `APIM_CERT_THUMBPRINTS`. Sin esa
  variable no se exige nada, así que en local no hace falta.

## Cómo correrlo en local

Requisitos: Java 17 o superior, Maven 3.9 y un MongoDB local. La forma más corta de
levantar MongoDB es con Docker:

```bash
docker run -d --name mongo-usuarios -p 27017:27017 mongo:7
```

Luego, desde la raíz del repo:

```bash
mvn test                 # pruebas unitarias (no necesitan MongoDB)
mvn spring-boot:run      # levanta el servicio en http://localhost:8080
```

Si la ruta de la carpeta tiene tildes o eñes, `spring-boot:run` no encuentra la clase
principal en Windows. En ese caso: `mvn clean package -DskipTests` y
`java -jar target/app.jar`.

Para probarlo sin APIM hay que mandar los encabezados que APIM agregaría:

```bash
curl -H "X-User-Id: sub-prueba" -H "X-User-Email: prueba@example.com" \
     -H "X-Client: web" http://localhost:8080/profiles/sub-prueba

curl -X PUT -H "Content-Type: application/json" \
     -H "X-User-Id: sub-prueba" -H "X-User-Email: prueba@example.com" \
     -d '{"fullName":"Persona de Prueba","modalities":["remote"]}' \
     http://localhost:8080/profiles/sub-prueba

curl "http://localhost:8080/legal/terms?lang=en"
curl http://localhost:8080/help
curl http://localhost:8080/actuator/health
```

### Swagger UI

Con el servicio corriendo, la documentación está en
<http://localhost:8080/swagger-ui.html>. Muestra el contrato
[`openapi.yaml`](openapi.yaml) tal cual (Maven lo copia al jar), no uno generado desde
el código. Para probar desde ahí, elegí el servidor `http://localhost:8080` y llená
`X-User-Id` y `X-User-Email`. En Azure no se puede abrir desde el navegador, porque el App
Service exige el certificado de APIM.

### Postman

En [`postman/`](postman) está la colección con las 7 operaciones y los errores que el
servicio tiene que rechazar (403 por perfil ajeno o sin encabezados, 400 por datos
inválidos), cada una con sus pruebas. Hay dos entornos:

- **Local** (`local.postman_environment.json`): contra `http://localhost:8080`, con los
  encabezados de APIM puestos a mano.
- **APIM** (`apim.postman_environment.json`): contra el gateway. Hay que llenar
  `googleToken` con un ID token de Google, y `userId` y `userEmail` con el `sub` y el
  correo de esa cuenta.

Se importan en Postman (Import → la carpeta `postman/`) o se corren desde la terminal:

```bash
npx newman run postman/cvexpert-usuarios.postman_collection.json -e postman/local.postman_environment.json
```

## Variables de entorno

Ningún valor va en el código. En Azure se definen en el App Service, con referencias
al Key Vault para los secretos. Hay un ejemplo en [`.env.example`](.env.example).

| Variable | Para qué | Valor por defecto (solo local) |
|---|---|---|
| `MONGODB_URI` | Cadena de conexión de Cosmos DB / MongoDB. **Secreta**: va al Key Vault | `mongodb://localhost:27017/cvexpert-usuarios` |
| `ADMIN_EMAILS` | Correos administradores, separados por coma | `gcabrerarojas.25@gmail.com` |
| `SEED_ENABLED` | Carga los textos de `src/main/resources/seed` si la colección está vacía | `true` |
| `PORT` | Puerto HTTP | `8080` |
| `APIM_CERT_THUMBPRINTS` | Huellas SHA-1 del certificado de cliente de APIM, separadas por coma. Vacía = no se exige | vacía |

## Despliegue

- **CI** (`.github/workflows/ci.yml`): `mvn verify` en cada pull request y en cada push a
  `develop` y `main`.
- **CD** (`.github/workflows/cd.yml`): en cada push a `main` que cambie `src/`, `pom.xml`
  o el workflow, vuelve a correr las pruebas y despliega `target/app.jar` al App Service
  `cvexpert-usuarios`. Se autentica con OIDC (federated credential de
  `gh-actions-cvexpert-ms-usuarios` para `refs/heads/main`), así que no hay contraseñas
  en el repo. Necesita los secrets `AZURE_CLIENT_ID`, `AZURE_TENANT_ID` y
  `AZURE_SUBSCRIPTION_ID`.
- En Azure, `MONGODB_URI` es una referencia al secreto `Usuarios--MongoDbUri` de
  `kv-cvexpert`, que el App Service lee con su identidad administrada.

## Estructura

```
src/main/java/cr/ac/tec/cvexpert/usuarios/
├── controller/   Endpoints REST (ProfileController, LegalController, HelpController)
├── service/      Reglas: dueño del perfil, rol, actualización parcial, sesiones
├── repository/   Acceso a MongoDB (Spring Data)
├── model/        Documentos de MongoDB (profiles, sessions, legal_documents, help_contacts)
├── dto/          Lo que entra y sale por la API
├── exception/    Errores y su traducción a ApiError
└── config/       Propiedades, reloj, carga inicial de datos y filtro del certificado de APIM
src/main/resources/seed/   Textos legales y contactos de ayuda iniciales
postman/                   Colección y entornos de Postman
```

## Cómo contribuir

La rama `main` está protegida: todo cambio entra por un pull request que revisa
otro integrante del equipo.

## Pendiente

- [x] Pipeline de GitHub Actions: pruebas en cada PR y despliegue desde `main`
- [x] Exigir el certificado de APIM en el App Service
- [x] Swagger UI y pruebas de Postman
- [x] Textos legales definitivos (términos 1.1 y privacidad 1.2, cargados también en producción)
- [x] Correo real de ayuda y privacidad (`seed/help.json`, también en producción)
