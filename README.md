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
  APIM. Eso se configura en el App Service y se documenta en `cvexpert-apim`.

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

## Variables de entorno

Ningún valor va en el código. En Azure se definen en el App Service, con referencias
al Key Vault para los secretos. Hay un ejemplo en [`.env.example`](.env.example).

| Variable | Para qué | Valor por defecto (solo local) |
|---|---|---|
| `MONGODB_URI` | Cadena de conexión de Cosmos DB / MongoDB. **Secreta**: va al Key Vault | `mongodb://localhost:27017/cvexpert-usuarios` |
| `ADMIN_EMAILS` | Correos administradores, separados por coma | `gcabrerarojas.25@gmail.com` |
| `SEED_ENABLED` | Carga los textos de `src/main/resources/seed` si la colección está vacía | `true` |
| `PORT` | Puerto HTTP | `8080` |

## Estructura

```
src/main/java/cr/ac/tec/cvexpert/usuarios/
├── controller/   Endpoints REST (ProfileController, LegalController, HelpController)
├── service/      Reglas: dueño del perfil, rol, actualización parcial, sesiones
├── repository/   Acceso a MongoDB (Spring Data)
├── model/        Documentos de MongoDB (profiles, sessions, legal_documents, help_contacts)
├── dto/          Lo que entra y sale por la API
├── exception/    Errores y su traducción a ApiError
└── config/       Propiedades, reloj y carga inicial de datos
src/main/resources/seed/   Textos legales y contactos de ayuda iniciales (BORRADOR)
```

## Cómo contribuir

La rama `main` está protegida: todo cambio entra por un pull request que revisa
otro integrante del equipo.

## Pendiente

- [ ] Pipeline de GitHub Actions: pruebas en cada PR y despliegue desde `main` (dom 25 oct)
- [ ] Exigir el certificado de APIM en el App Service (sáb 31 oct)
- [ ] Swagger UI y pruebas de Postman (mar 3 nov)
- [ ] Reemplazar los textos legales y los correos de ayuda de `seed/` por los definitivos
