# Taller 09 — Comunicación síncrona y asíncrona de microservicios

**Banco de Preguntas Saber PRO** · Laboratorio de Ingeniería de Software II · Universidad del Cauca
Profesores: Paola Andrea Bedoya Toro, Pablo Magé · **Grupo 3** · Octubre de 2026

**Integrantes:** Johan Santiago López · Eddy Emmanuel Sánchez

Monorepo con seis microservicios Spring Boot que se comunican de forma **síncrona** (REST sobre HTTP)
y **asíncrona** (comandos y eventos con RabbitMQ / AMQP), más la colección de pruebas de Postman.

## Microservicios

| Microservicio | Puerto | Responsabilidad | Comunicación |
|---|---|---|---|
| `user-service` | 8081 | Autenticar usuarios y verificar roles | REST (`POST /auth/login`, `GET /auth/validate`, `GET /users/{id}`) |
| `validation-service` | 8082 | Validar estructura: enunciado, 4 opciones distintas y clave | REST (`POST /validate`) |
| `question-service` | 8083 | Contenido de la pregunta y coordinación del flujo | REST + publica `SubmitQuestionForReview` y `ValidationFailed`; consume `OperationResult` |
| `lifecycle-service` | 8084 | **Única autoridad** sobre estados y transiciones | Consume `SubmitQuestionForReview`; REST para transiciones; publica `OperationResult` y `QuestionSubmittedForReview` |
| `review-service` | 8085 | Asignaciones de revisores y resultados de revisión | REST hacia User y Lifecycle; publica `ReviewerAssigned` y `QuestionReviewed` |
| `notification-service` | 8086 | Notificaciones sin bloquear el flujo principal | Consume los eventos; `GET /notifications` para verificar |

Cada servicio tiene su propia base de datos H2 en archivo (`./data/<servicio>db`): *Database per Service*.

Máquina de estados (en `lifecycle-service`):
`DRAFT → PENDING_REVIEW → IN_REVIEW → APPROVED | REJECTED`, `REJECTED → DRAFT` (corrección),
`APPROVED → PUBLISHED → ARCHIVED`. Cualquier otra transición se rechaza con `409 Conflict`.

## Mensajería (RabbitMQ)

Exchange `saberpro.direct.exchange` (direct). Las colas son durables y envían los mensajes que fallan
tres veces a `saberpro.dlx.exchange` (colas `*.dlq`).

| Mensaje | Tipo | Routing key | Publica | Cola | Consume |
|---|---|---|---|---|---|
| SubmitQuestionForReview | Comando | `question.submit` | Question | `question.submit.review.queue` | Lifecycle |
| OperationResult | Evento | `question.operation.result` | Lifecycle | `question.operation.result.queue` | Question |
| QuestionSubmittedForReview | Evento | `question.submitted` | Lifecycle | `notification.question.submitted.queue` | Notification |
| ReviewerAssigned | Evento | `reviewer.assigned` | Review | `notification.reviewer.assigned.queue` | Notification |
| QuestionReviewed | Evento | `question.reviewed` | Review | `notification.question.reviewed.queue` | Notification |
| ValidationFailed | Evento | `question.validation.failed` | Question | `notification.validation.failed.queue` | Notification |

## Cómo ejecutarlo

Requisitos: **Java 17 o superior** y **Docker** (solo para RabbitMQ). Maven no hace falta: cada
proyecto incluye Maven Wrapper (`mvnw` / `mvnw.cmd`).

```bash
# 1. RabbitMQ (AMQP 5672, panel http://localhost:15672 con guest / guest)
docker compose up -d

# 2. Compilar y levantar los 6 microservicios (logs en ./logs)
./start-all.sh

# 3. Detenerlos
./stop-all.sh
```

También se puede levantar cada servicio por separado (por ejemplo desde el IDE o en Windows):

```bash
cd question-service
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Variables de configuración por servicio (con valores por defecto para ejecución local):
`SERVER_PORT`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASS`,
`USER_SERVICE_URL`, `VALIDATION_SERVICE_URL`, `LIFECYCLE_SERVICE_URL`,
`LIFECYCLE_PROCESSING_DELAY_MS` (2000) y `NOTIFICATION_SEND_DELAY_MS` (1500).

## Pruebas con Postman

1. Importe `postman/SaberPro_Microservices.postman_collection.json` y
   `postman/SaberPro_Local.postman_environment.json`.
2. Seleccione el entorno **SaberPro Local**.
3. Ejecute la colección completa con el *Collection Runner*, en orden. Las carpetas están organizadas
   por microservicio (01 a 06) y la 07 recorre el escenario de corrección y aprobación. Los valores
   (`token`, `question_id`, `operation_id`...) se encadenan entre peticiones.

La colección tiene 41 peticiones y 77 aserciones (casos exitosos y fallidos). También puede ejecutarse
por consola con Newman:

```bash
npx newman run postman/SaberPro_Microservices.postman_collection.json -e postman/SaberPro_Local.postman_environment.json
```

Usuarios de prueba:

| Usuario | Contraseña | Id | Roles |
|---|---|---|---|
| `docente1` | `docente123` | 1 | TEACHER, AUTHOR |
| `revisor1` | `revisor123` | 2 | REVIEWER |
| `revisor2` | `revisor123` | 3 | REVIEWER, TEACHER |
| `admin` | `admin123` | 4 | ADMIN |
| `estudiante1` | `estudiante123` | 5 | STUDENT |
| `revisor3` | `revisor123` | 6 | REVIEWER (inactivo) |

> El token que devuelve `POST /auth/login` es un identificador opaco de sesión que se envía como
> `Authorization: Bearer <token>`. No es JWT: los tokens firmados se implementan en el corte 3.

## Pruebas unitarias

```bash
./mvnw test
```

- `ValidationServiceTest`: reglas de validación estructural.
- `QuestionStatusTest`: transiciones permitidas y rechazadas de la máquina de estados.
- `ReviewServiceTest`: asignación confirmada y compensación cuando Lifecycle Service falla.

## Estructura

```
lisw2_taller09_microservicios_g3/
├── docker-compose.yml          # RabbitMQ con el plugin de administración
├── pom.xml                     # POM agregador (compila todos los servicios)
├── start-all.sh / stop-all.sh
├── postman/                    # Colección y entorno de pruebas
├── user-service/               # :8081  config, controller, dto, model, repository, service
├── validation-service/         # :8082  controller, dto, service/rules
├── question-service/           # :8083  client, config, controller, messaging, model, service
├── lifecycle-service/          # :8084  config, controller, messaging, model, service
├── review-service/             # :8085  client, config, controller, messaging, model, service
└── notification-service/       # :8086  config, controller, messaging, model, service
```
