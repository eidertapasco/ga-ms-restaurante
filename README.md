# 🍽️ GastroSENA — Microservicio de Restaurante

> **`ga-ms-restaurante`** es el microservicio backend del módulo de restaurante del proyecto **GastroSENA**, un sistema integral de gestión para restaurantes de formación del SENA. Cubre el ciclo completo: asignación de mesas → toma de pedidos → comunicación con cocina/bar vía mensajería asíncrona → facturación y cierre de caja.

---

## 📑 Tabla de Contenidos

- [Descripción General](#-descripción-general)
- [Stack Tecnológico](#-stack-tecnológico)
- [Prerrequisitos](#-prerrequisitos)
- [Configuración Rápida (Local / Dev)](#-configuración-rápida-local--dev)
- [Configuración con Docker (Prod)](#-configuración-con-docker-prod)
- [Variables de Entorno](#-variables-de-entorno)
- [Módulos del Sistema](#-módulos-del-sistema)
- [Mensajería RabbitMQ](#-mensajería-rabbitmq)
- [Seguridad y Contexto de Usuario](#-seguridad-y-contexto-de-usuario)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [API y Documentación](#-api-y-documentación)
- [CI/CD — Pipeline de GitHub Actions](#-cicd--pipeline-de-github-actions)
- [Flujo Completo del Sistema](#-flujo-completo-del-sistema)
- [Colaboración y Ramas](#-colaboración-y-ramas)

---

## 📋 Descripción General

GastroSENA digitaliza la operación de un restaurante de formación. Este microservicio gestiona:

| Módulo | Responsabilidad |
|---|---|
| **Mesas** | CRUD de mesas, estados (Libre / Ocupada / Por Pagar / Inactiva), zonas |
| **Pedidos** | Creación, confirmación y ciclo de vida de comandas |
| **Caja** | Sesiones de turno, facturación, métodos de pago, generación de PDF |
| **Reportes** | Resumen de ventas por sesión, pedidos por mesa |

El servicio se comunica con los microservicios de **Cocina** y **Bar** de forma asíncrona mediante **RabbitMQ**. Cuando un pedido se confirma, el sistema separa automáticamente los ítems por categoría (COMIDA → Cocina, BEBIDA → Bar) y publica eventos independientes. Las respuestas de estado regresan por el mismo bus de mensajería.

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 (Eclipse Temurin) |
| Framework | Spring Boot 3.5.10 |
| Persistencia | Spring Data JPA + Hibernate 6 |
| Base de datos (dev) | H2 en memoria |
| Base de datos (prod) | MySQL 8.0 |
| Mensajería | RabbitMQ 3 (Spring AMQP) |
| Microservicios | Spring Cloud 2025.0.1 + OpenFeign |
| Seguridad | Spring Security (mock dev / headers prod) |
| Generación de PDF | OpenPDF 1.3.30 |
| Documentación API | SpringDoc OpenAPI 2.8.9 (Swagger UI) |
| Build | Maven 3.9.12 (wrapper incluido) |
| Contenedores | Docker (multi-stage build) + Docker Compose |
| CI/CD | GitHub Actions |

---

## ✅ Prerrequisitos

### Para desarrollo local (perfil `dev`)

| Herramienta | Versión mínima | Notas |
|---|---|---|
| Java JDK | 21 | Temurin recomendado |
| Maven | 3.9+ | O usar `./mvnw` incluido |
| RabbitMQ | 3.x | Puede levantarse con Docker |
| Git | cualquiera | |

> **RabbitMQ local rápido** (sin instalarlo en Windows):
> ```bash
> docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
> ```
> Panel de administración: [http://localhost:15672](http://localhost:15672) · usuario: `guest` / contraseña: `guest`

### Para despliegue con Docker (perfil `prod`)

| Herramienta | Versión mínima |
|---|---|
| Docker Engine | 24+ |
| Docker Compose | 2.x |

---

## 🚀 Configuración Rápida (Local / Dev)

El perfil `dev` usa **H2 en memoria** (no necesitas MySQL) y un filtro de seguridad simulado.

### 1. Clonar el repositorio

```bash
git clone https://github.com/TU_ORG/ga-ms-restaurante.git
cd ga-ms-restaurante
```

### 2. Levantar RabbitMQ

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### 3. Ejecutar la aplicación

```bash
# Con Maven Wrapper (recomendado)
./mvnw spring-boot:run

# O directamente
mvn spring-boot:run
```

El perfil `dev` se activa automáticamente (ver `application.properties`).

### 4. Verificar que funciona

- **API:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **BD H2 Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
    - JDBC URL: `jdbc:h2:mem:ga-ms-restaurante_db`
    - Usuario: `sa` · Contraseña: *(vacío)*

Al iniciar, el `DataInitializer` crea automáticamente **10 mesas** de ejemplo si la base de datos está vacía:

| Mesa | Capacidad | Zona |
|---|---|---|
| Mesa 01 – Mesa 04 | 4 / 6 | Salón Principal |
| Mesa 05 – Mesa 06 | 2 | Terraza |
| Mesa 07 – Mesa 08 | 8 | Salón VIP |
| Barra 01 – Barra 02 | 1 | Barra |

### 5. Simular usuario autenticado (perfil dev)

En dev, la identidad del usuario se lee de headers HTTP. Si no los envías, el sistema usa valores por defecto.

```http
X-Mock-User-Id: 00000000-0000-0000-0000-000000000001
X-Mock-User-Role: MESERO
```

**Roles disponibles:** `MESERO`, `CAJERO`, `INSTRUCTOR`, `ADMIN`

En Postman/Insomnia, agrégalos como headers en cada request.

---

## 🐳 Configuración con Docker (Prod)

### Opción A — Construir desde código fuente (docker-compose.yml)

Levanta MySQL + RabbitMQ + el backend compilado localmente:

```bash
docker compose up --build
```

> La primera vez descarga imágenes y compila el JAR dentro del contenedor. Puede tardar 3–5 minutos.

### Opción B — Usar imagen publicada en Docker Hub (docker-compose.prod.yml)

Descarga la imagen ya construida por el pipeline de CI/CD:

```bash
docker compose -f docker-compose.prod.yml up -d
```

### Verificar servicios levantados

```bash
docker compose ps
```

| Servicio | Puerto Host | Descripción |
|---|---|---|
| `gastrosena-backend` | 8080 | API REST |
| `gastrosena-mysql` | 3307 | MySQL (3307 para no chocar con instalación local) |
| `gastrosena-rabbitmq` | 5672 / 15672 | Mensajería / Panel web |

> **Nota importante:** MySQL expone el puerto `3307` en tu máquina host (no el 3306 estándar) para evitar conflictos si tienes MySQL instalado localmente. Dentro de la red Docker, los contenedores se comunican normalmente por el puerto `3306`.

### Detener servicios

```bash
docker compose down          # Detiene pero conserva datos (volumen mysql_data)
docker compose down -v       # Detiene Y borra todos los datos
```

---

## 🔧 Variables de Entorno

El perfil `prod` lee configuración desde variables de entorno. Si no se definen, usa los valores por defecto indicados.

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `DB_USERNAME` | `root` | Usuario de MySQL |
| `DB_PASSWORD` | `root` | Contraseña de MySQL |
| `RABBITMQ_HOST` | `rabbitmq` | Hostname del broker (nombre del contenedor en Docker) |
| `RABBITMQ_USER` | `guest` | Usuario de RabbitMQ |
| `RABBITMQ_PASS` | `guest` | Contraseña de RabbitMQ |

Para producción real, crea un archivo `.env` en la raíz del proyecto (ya está en `.gitignore`):

```env
DB_USERNAME=gastrosena_user
DB_PASSWORD=una_clave_segura
RABBITMQ_USER=gastrosena
RABBITMQ_PASS=otra_clave_segura
```

Y luego `docker compose -f docker-compose.prod.yml --env-file .env up -d`.

---

## 📦 Módulos del Sistema

### 🪑 Mesas (`/api/mesas`)

Gestión del salón. Endpoint principal de acceso a la operación.

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/mesas` | Listar todas las mesas activas |
| `GET` | `/api/mesas/inactivas` | Listar mesas desactivadas |
| `GET` | `/api/mesas/estado/{estado}` | Filtrar por estado (LIBRE, OCUPADA, POR_PAGAR) |
| `GET` | `/api/mesas/{id}` | Buscar mesa por ID |
| `POST` | `/api/mesas` | Crear nueva mesa |
| `PUT` | `/api/mesas/{id}` | Actualizar nombre, capacidad, zona u observaciones |
| `PATCH` | `/api/mesas/{id}/estado` | Cambiar estado manualmente |
| `PATCH` | `/api/mesas/{id}/activar` | Activar mesa inactiva |
| `PATCH` | `/api/mesas/{id}/desactivar` | Desactivar mesa (solo si está LIBRE) |

**Estados de mesa y transiciones válidas:**

```
LIBRE ──────► OCUPADA ──────► POR_PAGAR ──────► LIBRE
  │                                                 ▲
  └──────────────────► INACTIVA ◄──────────────────┘
                     (vía activar/desactivar)
```

---

### 📋 Pedidos (`/api/pedidos`)

Ciclo de vida completo de una comanda.

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/pedidos` | Crear pedido en estado BORRADOR |
| `GET` | `/api/pedidos/{id}` | Detalle completo con ítems |
| `GET` | `/api/pedidos` | Listar todos (rol instructor/admin) |
| `GET` | `/api/pedidos/mis-pedidos` | Listar pedidos del mesero autenticado |
| `GET` | `/api/pedidos/estado/{estado}` | Filtrar por estado |
| `GET` | `/api/pedidos/mesa/{mesaId}` | Pedidos de una mesa específica |
| `PATCH` | `/api/pedidos/{id}/confirmar` | BORRADOR → ENVIADO_COCINA + publica eventos RabbitMQ |
| `PATCH` | `/api/pedidos/{id}/entregar` | LISTO_PARA_SERVIR → ENTREGADO (mesa pasa a POR_PAGAR) |
| `PATCH` | `/api/pedidos/{id}/cancelar` | Cancelar pedido y liberar mesa |

**Ciclo de estados del pedido:**

```
BORRADOR ──► ENVIADO_COCINA ──► EN_PREPARACION ──► LISTO_PARA_SERVIR ──► ENTREGADO ──► FACTURADO
    │               │                  │                    │                              │
    └───────────────┴──────────────────┴────────────────────┴──────────────────────────► CANCELADO
```

> Los estados `EN_PREPARACION` y `LISTO_PARA_SERVIR` son actualizados por los microservicios de Cocina/Bar mediante eventos RabbitMQ — no por el frontend de restaurante.

**Estructura de un ítem de pedido (`DetallePedidoRequest`):**

```json
{
  "productoId": "uuid-o-string-del-catalogo",
  "nombreProducto": "Bandeja Paisa",
  "cantidad": 2,
  "precioUnitario": 25000.00,
  "categoria": "COMIDA",
  "observaciones": "Sin morcilla"
}
```

> `categoria` debe ser `"COMIDA"` o `"BEBIDA"`. Este campo determina a qué cola de RabbitMQ se publica el evento.

---

### 💰 Caja (`/api/caja` y `/api/facturas`)

Gestión de turnos y facturación.

**Sesiones de Caja:**

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/caja/sesion/abrir` | Abrir turno con base de efectivo |
| `PATCH` | `/api/caja/sesion/{id}/cerrar` | Cerrar turno con efectivo contado |
| `GET` | `/api/caja/sesion/activa` | Obtener sesión abierta actual |
| `GET` | `/api/caja/sesion/{id}` | Buscar sesión por ID |

> Solo puede haber **una sesión abierta a la vez**. Intentar abrir una segunda lanza error 422.

**Facturas:**

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/facturas` | Facturar pedido (requiere sesión abierta y pedido en ENTREGADO) |
| `PATCH` | `/api/facturas/{id}/anular` | Anular factura (revierte acumulados en sesión) |
| `GET` | `/api/facturas/{id}` | Buscar factura por ID |
| `GET` | `/api/facturas/numero/{numero}` | Buscar por número de factura (ej: `FAC-20260609-A1B2C3D4`) |
| `GET` | `/api/facturas/sesion/{sesionId}` | Listar todas las facturas de un turno |
| `GET` | `/api/facturas/{id}/pdf` | Descargar factura en PDF (formato tirilla) |

**Métodos de pago disponibles:** `EFECTIVO`, `TARJETA`, `TRANSFERENCIA`, `CORTESIA`

**Body de facturación:**

```json
{
  "pedidoId": "uuid-del-pedido",
  "metodoPago": "EFECTIVO",
  "propina": 5000.00
}
```

---

### 📊 Reportes (`/api/reportes`)

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/reportes/sesion/{sesionId}/resumen` | Totales de ventas por turno desglosados por método de pago |
| `GET` | `/api/reportes/mesas` | Pedidos facturados y total facturado por cada mesa activa |

**Ejemplo de respuesta de resumen por sesión:**

```json
{
  "totalPedidosFacturados": 15,
  "totalPedidosCancelados": 2,
  "totalVentasBrutas": 450000.00,
  "totalPropinas": 30000.00,
  "totalVentasConPropina": 480000.00,
  "ventasEfectivo": 200000.00,
  "ventasTarjeta": 250000.00,
  "ventasTransferencia": 30000.00,
  "ventasCortesia": 0.00
}
```

---

## 🐇 Mensajería RabbitMQ

El sistema usa un **Topic Exchange** llamado `gastrosena.pedidos`.

### Topología

```
                    ┌─────────────────────────────────┐
                    │  Exchange: gastrosena.pedidos    │
                    │  (Topic Exchange, durable)       │
                    └──────────────┬──────────────────┘
                                   │
          ┌────────────────────────┼────────────────────────┐
          │                        │                        │
   pedido.cocina            pedido.bar          pedido.estado.actualizado
          │                        │                        │
          ▼                        ▼                        ▼
 restaurante.pedido.cocina  restaurante.pedido.bar  restaurante.pedido.estado
   (consume: Cocina)          (consume: Bar)          (consume: Restaurante ←)
```

### Eventos publicados por este microservicio

| Routing Key | Queue destino | Cuándo se publica | Payload |
|---|---|---|---|
| `pedido.cocina` | `restaurante.pedido.cocina` | Al confirmar pedido (si hay ítems COMIDA) | `PedidoCocinaEvent` |
| `pedido.bar` | `restaurante.pedido.bar` | Al confirmar pedido (si hay ítems BEBIDA) | `PedidoBarEvent` |

### Eventos consumidos por este microservicio

| Queue | Routing Key | Quién publica | Qué hace al recibirlo |
|---|---|---|---|
| `restaurante.pedido.estado` | `pedido.estado.actualizado` | Cocina / Bar | Actualiza el estado del pedido en BD |

### Estados válidos que acepta el listener

| Estado recibido | Transición permitida desde |
|---|---|
| `EN_PREPARACION` | Solo desde `ENVIADO_COCINA` |
| `LISTO_PARA_SERVIR` | Solo desde `EN_PREPARACION` |
| `CANCELADO` | Cualquier estado excepto `FACTURADO` o `CANCELADO` |

> Si la transición no es válida, el evento se **ignora silenciosamente** (no se reencola) para evitar loops infinitos.

### Estructura de los eventos

**`PedidoCocinaEvent` / `PedidoBarEvent`:**
```json
{
  "idPedido": "uuid",
  "numeroMesa": "Mesa 03",
  "notas": "Para llevar",
  "items": [
    {
      "idProducto": "uuid-o-string",
      "nombreProducto": "Bandeja Paisa",
      "cantidad": 2,
      "observaciones": "Sin morcilla"
    }
  ]
}
```

**`EstadoPedidoEvent`** (recibido desde Cocina/Bar):
```json
{
  "idPedido": "uuid",
  "nuevoEstado": "LISTO_PARA_SERVIR",
  "modulo": "COCINA"
}
```

---

## 🔐 Seguridad y Contexto de Usuario

Este microservicio espera recibir la identidad del usuario ya autenticado desde un **API Gateway** o **microservicio de usuarios**. No valida tokens JWT directamente — eso ocurre aguas arriba.

### Perfil `dev` — MockSecurityFilter

Lee headers de prueba en cada request:

```http
X-Mock-User-Id: 00000000-0000-0000-0000-000000000001
X-Mock-User-Role: MESERO
```

Si los headers no se envían, usa valores por defecto (`00000000-...-0001` y `MESERO`).

### Perfil `prod` — ProdUserContextFilter

Lee headers reales enviados por el gateway:

```http
X-User-Id: <uuid-del-usuario-autenticado>
X-User-Role: <ROL>
```

### Reglas de propiedad en pedidos

Solo puede modificar un pedido (confirmar, cancelar, entregar):
- El mesero que lo creó (`meseroId == currentUserId`)
- Un usuario con rol `INSTRUCTOR` o `ADMIN`

---

## 📁 Estructura del Proyecto

```
ga-ms-restaurante/
├── src/
│   ├── main/
│   │   ├── java/co/edu/sena/ga_ms_restaurante/
│   │   │   ├── GaMsRestauranteApplication.java   # Punto de entrada
│   │   │   │
│   │   │   ├── amqp/                             # Mensajería RabbitMQ
│   │   │   │   ├── dto/                          # Eventos: PedidoCocinaEvent, PedidoBarEvent, EstadoPedidoEvent
│   │   │   │   ├── listener/                     # EstadoPedidoListener (consume respuestas de Cocina/Bar)
│   │   │   │   └── publisher/                    # PedidoCocinaPublisher, PedidoBarPublisher
│   │   │   │
│   │   │   ├── caja/                             # Módulo de facturación
│   │   │   │   ├── controller/                   # CajaController, FacturaController
│   │   │   │   ├── dto/request/ & response/      # DTOs de entrada y salida
│   │   │   │   ├── enums/                        # EstadoFactura, EstadoSesion, MetodoPago
│   │   │   │   ├── mapper/                       # CajaMapper, FacturaMapper
│   │   │   │   ├── model/                        # Factura, SesionCaja (entidades JPA)
│   │   │   │   ├── repository/                   # FacturaRepository, SesionCajaRepository
│   │   │   │   └── service/                      # CajaService, FacturaService (+ Impl)
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── amqp/                         # RabbitMQConfig (exchange, queues, bindings)
│   │   │   │   ├── security/                     # MockSecurityFilter (dev), ProdUserContextFilter (prod)
│   │   │   │   │                                 # SecurityConfig (dev), SecurityConfigProd (prod)
│   │   │   │   └── web/                          # CorsConfig, OpenApiConfig
│   │   │   │
│   │   │   ├── exception/                        # GlobalExceptionHandler, ApiError
│   │   │   │   └── custom/                       # BusinessRuleException, ResourceNotFoundException
│   │   │   │
│   │   │   ├── mesa/                             # Módulo de mesas
│   │   │   │   ├── controller/                   # MesaController
│   │   │   │   ├── dto/                          # MesaCreateRequest, MesaUpdateRequest, MesaResponse
│   │   │   │   ├── enums/                        # EstadoMesa
│   │   │   │   ├── mapper/                       # MesaMapper
│   │   │   │   ├── model/                        # Mesa (entidad JPA)
│   │   │   │   ├── repository/                   # MesaRepository
│   │   │   │   └── service/                      # MesaService, MesaServiceImpl
│   │   │   │
│   │   │   ├── pedido/                           # Módulo de pedidos
│   │   │   │   ├── controller/                   # PedidoController
│   │   │   │   ├── dto/                          # PedidoCreateRequest, DetallePedidoRequest, PedidoResponse...
│   │   │   │   ├── enums/                        # EstadoPedido (7 valores)
│   │   │   │   ├── mapper/                       # PedidoMapper
│   │   │   │   ├── model/                        # Pedido, DetallePedido (entidades JPA)
│   │   │   │   ├── repository/                   # PedidoRepository, DetallePedidoRepository
│   │   │   │   └── service/                      # PedidoService, PedidoServiceImpl
│   │   │   │
│   │   │   ├── reporte/                          # Módulo de reportes
│   │   │   │   ├── controller/                   # ReporteController
│   │   │   │   ├── dto/response/                 # ResumenVentasResponse, PedidoPorMesaResponse
│   │   │   │   └── service/                      # ReporteService, ReporteServiceImpl
│   │   │   │
│   │   │   ├── security/                         # UserContext, UserContextHolder, PermissionConstants
│   │   │   └── seed/                             # DataInitializer (dev), DataInitializerProd (prod)
│   │   │
│   │   └── resources/
│   │       ├── application.properties            # Perfil dev (H2, RabbitMQ local)
│   │       └── application-prod.properties       # Perfil prod (MySQL, RabbitMQ Docker)
│   │
│   └── test/
│       └── java/...GaMsRestauranteApplicationTests.java
│
├── .github/
│   └── workflows/
│       └── docker-publish.yml                    # Pipeline CI/CD
│
├── Dockerfile                                    # Multi-stage build
├── docker-compose.yml                            # Dev local (build desde código)
├── docker-compose.prod.yml                       # Prod (imagen Docker Hub)
├── pom.xml
├── mvnw / mvnw.cmd                               # Maven Wrapper
└── README.md                                     # Este archivo
```

---

## 📖 API y Documentación

Con la aplicación corriendo, accede a la documentación interactiva:

**Swagger UI:**
```
http://localhost:8080/swagger-ui/index.html
```

**OpenAPI JSON:**
```
http://localhost:8080/v3/api-docs
```

**H2 Console** *(solo perfil dev)*:
```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:ga-ms-restaurante_db
User: sa  |  Password: (vacío)
```

**RabbitMQ Management Panel:**
```
http://localhost:15672
User: guest  |  Password: guest
```

---

## ⚙️ CI/CD — Pipeline de GitHub Actions

El archivo `.github/workflows/docker-publish.yml` automatiza el proceso de build y publicación.

**Se ejecuta cuando se hace push a:**
- `feature/docker`
- `develop`

**Pasos del pipeline:**

```
1. Checkout del código
2. Configurar Java 21 (Temurin)
3. Restaurar caché de dependencias Maven (~/.m2)
4. mvn clean package -DskipTests
5. Login a Docker Hub
6. Build y Push de imagen Docker con tags:
   - latest
   - 1.0.0
```

**Secretos de GitHub requeridos** *(configurar en Settings → Secrets → Actions)*:

| Secreto | Descripción |
|---|---|
| `DOCKERHUB_USERNAME` | Tu usuario de Docker Hub |
| `DOCKERHUB_TOKEN` | Token de acceso de Docker Hub (no la contraseña) |

**Imagen publicada:**
```
docker.io/eidertapasco/ga-ms-restaurante:latest
docker.io/eidertapasco/ga-ms-restaurante:1.0.0
```

---

## 🔄 Flujo Completo del Sistema

```
👥 Comensales llegan al restaurante
          │
          ▼
🪑 Mesero abre el módulo de MESAS
   └── Selecciona mesa LIBRE
   └── Confirma número de comensales
   └── Mesa pasa a estado OCUPADA
          │
          ▼
📋 Mesero toma el pedido (módulo PEDIDOS)
   └── Agrega ítems del menú (COMIDA / BEBIDA)
   └── Agrega observaciones por ítem si es necesario
   └── Pedido en estado BORRADOR (editable)
          │
          ▼
✅ Mesero confirma el pedido
   └── Sistema separa ítems por categoría
   └── Publica evento → Cola COCINA (platos)
   └── Publica evento → Cola BAR (bebidas)
   └── Pedido pasa a ENVIADO_COCINA
          │
          ▼
👨‍🍳 Cocina/Bar procesan el pedido (microservicio externo)
   └── Publica evento → restaurante.pedido.estado: EN_PREPARACION
   └── Publica evento → restaurante.pedido.estado: LISTO_PARA_SERVIR
          │
          ▼
🍽️ Mesero recoge y lleva el pedido a la mesa
   └── Marca pedido como ENTREGADO
   └── Mesa pasa automáticamente a POR_PAGAR
          │
          ▼
💳 Cajero ve las mesas POR_PAGAR (módulo CAJA)
   └── Selecciona el pedido a cobrar
   └── Elige método de pago (Efectivo / Tarjeta / Transferencia)
   └── Confirma el pago
   └── Se genera factura con número único (FAC-YYYYMMDD-XXXXXXXX)
   └── Pedido pasa a FACTURADO
   └── Mesa queda LIBRE automáticamente
          │
          ▼
♻️ El ciclo se reinicia para la siguiente mesa
```

---

## 🤝 Colaboración y Ramas

### Estrategia de ramas

```
main          ← producción estable (solo merge desde develop con PR aprobado)
develop       ← integración continua, rama activa de desarrollo
feature/*     ← funcionalidades nuevas (ej: feature/reporte-pdf)
fix/*         ← correcciones de bugs (ej: fix/uuid-hibernate)
```

### Flujo de trabajo recomendado

```bash
# 1. Siempre partir desde develop actualizado
git checkout develop
git pull origin develop

# 2. Crear tu rama de trabajo
git checkout -b feature/nombre-descriptivo

# 3. Trabajar, hacer commits descriptivos
git commit -m "feat(pedidos): agregar validación de categoría en ítem"

# 4. Subir la rama y abrir Pull Request hacia develop
git push origin feature/nombre-descriptivo
```

### Convención de commits

```
feat(modulo):    nueva funcionalidad
fix(modulo):     corrección de bug
refactor(modulo): cambio de código sin cambio funcional
chore(modulo):   configuración, dependencias, CI/CD
docs(modulo):    documentación
```

**Ejemplos:**
```
feat(caja): agregar endpoint de descarga de PDF por factura
fix(pedido): corregir transición de estado EN_PREPARACION → LISTO_PARA_SERVIR
chore(docker): actualizar imagen base a eclipse-temurin:21-jre
docs(readme): agregar sección de mensajería RabbitMQ
```

---

## ⚠️ Errores Comunes y Soluciones

| Error | Causa probable | Solución |
|---|---|---|
| `Connection refused` al iniciar | RabbitMQ no está corriendo | Verificar `docker ps` y levantar el contenedor |
| `Ya existe una sesión de caja abierta` (HTTP 422) | Se intenta abrir una segunda sesión | Cerrar la sesión activa primero (`PATCH /api/caja/sesion/{id}/cerrar`) |
| `La mesa no está disponible` (HTTP 422) | La mesa ya está OCUPADA o POR_PAGAR | Verificar estado de la mesa antes de asignarla |
| `Solo se pueden facturar pedidos en estado ENTREGADO` | El pedido no ha completado el ciclo | El mesero debe marcar el pedido como entregado primero |
| UUID en formato binario en MySQL | Hibernate 6 por defecto usa BINARY | Ya configurado con `preferred_uuid_jdbc_type=CHAR` en `application-prod.properties` |
| H2 console no carga en prod | La consola H2 está deshabilitada en prod | Es intencional; usa MySQL Workbench o similar para prod |

---

## 📌 Notas para el Equipo

- **`productoId` en `DetallePedido` es `String`**, no UUID. Esto es intencional porque el catálogo de productos es responsabilidad del microservicio de Cocina, que define el tipo de su propio identificador. El módulo de restaurante no asume nada sobre esa estructura.

- **El precio del producto se congela en el pedido** (`precioUnitario` en `DetallePedido`). Aunque el precio cambie en el catálogo, la factura refleja el precio al momento de tomar el pedido.

- **La categoría `COMIDA`/`BEBIDA` debe venir del frontend**. El backend no consulta el catálogo de productos; el cliente es quien conoce la categoría de cada ítem y la debe incluir en el `DetallePedidoRequest`.

- **El módulo de autenticación JWT** es responsabilidad de otro microservicio del proyecto. Este servicio solo lee los headers `X-User-Id` y `X-User-Role` que el gateway ya validó.

---

*Última actualización: Junio 2026 · Equipo GastroSENA — Módulo Restaurante*