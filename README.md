# API - Spring Boot

Backend de red social con autenticacion JWT, perfiles, publicaciones y chat en tiempo real con WebSocket/STOMP.

## Tecnologias

- Java 21
- Spring Boot 4.0.3
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL
- Spring Mail
- WebSocket + STOMP
- Maven Wrapper (`./mvnw`)

## Requisitos

- JDK 21
- PostgreSQL accesible (local o cloud)
- Maven (opcional, ya que se incluye Maven Wrapper)

## Configuracion

El proyecto carga variables desde el archivo `.env` en la raiz.

1. Copia `.env.example` a `.env`
2. Completa los valores reales

Variables requeridas:

- `DATABASE_URL` (ejemplo: `jdbc:postgresql://host/database?sslmode=require`)
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET` (clave segura en Base64)
- `JWT_EXPIRATION` (opcional, por defecto `86400000`)
- `MAIL_USERNAME`
- `MAIL_PASSWORD`

## Ejecutar en desarrollo

```bash
./mvnw spring-boot:run
```

La API inicia por defecto en:

- `http://localhost:8080`

## Ejecutar tests

```bash
./mvnw test
```

## Autenticacion

La mayoria de endpoints requieren token JWT en header:

```http
Authorization: Bearer <access_token>
```

Endpoints publicos en seguridad:

- `POST /users/register`
- `POST /profiles/create`
- `POST /auth/**`
- `GET /`, `GET /index.html`, assets estaticos

## Endpoints principales

### Auth (`/auth`)

- `POST /auth/login?username={username}&password={password}`
- `POST /auth/refresh-token`
- `POST /auth/active-account`
- `POST /auth/reset-password`

### Users (`/users`)

- `POST /users/register`
- `GET /users/find/id?id={id}`
- `GET /users/find/username?username={username}`
- `PATCH /users/`

### Profiles (`/profiles`)

- `POST /profiles/create`
- `GET /profiles/me`
- `GET /profiles/search?username={username}`
- `POST /profiles/follow?username={username}`

### Posts (`/posts`)

- `POST /posts/create`
- `GET /posts/me`
- `GET /posts/feed`
- `POST /posts/like/{post_id}`
- `PATCH /posts/{id}`

## WebSocket (chat)

Configuracion STOMP:

- Endpoint WebSocket: `ws://localhost:8080/ws`
- Prefix cliente -> servidor: `/app`
- Broker topics: `/topic`
- Cola privada por usuario: `/user/queue`

Destinos usados:

- Enviar mensaje publico: `/app/chat`
- Suscripcion publica: `/topic/messages`
- Enviar mensaje privado: `/app/chat.private`
- Suscripcion privada: `/user/queue/messages`

Hay una pagina de prueba en `src/main/resources/static/index.html` para validar el chat STOMP rapidamente.

## Estructura

```text
src/main/java/com/spring/api/API
  Controllers/
  models/
  Repositories/
  security/
  services/
src/main/resources
  application.properties
  static/index.html
```

## Build de artefacto

```bash
./mvnw clean package
```

El JAR se genera en `target/`.
