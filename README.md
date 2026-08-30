# StockFlow API

API REST para gestionar productos, stock y el ciclo de vida de pedidos. Un pedido se crea con el precio actual de sus productos, descuenta stock al confirmarse y lo recupera si después se cancela.

## Demo

La API está desplegada en:

https://stockflowapi.duckdns.org/api/v1

## Stack

- Java 21
- Spring Boot 4.1
- Spring Web MVC, Spring Data JPA y Bean Validation
- PostgreSQL 17
- Flyway para las migraciones de base de datos
- Maven y JUnit/Mockito
- Docker Compose para la base de datos

## Alcance

La API permite:

- Crear, consultar, editar y eliminar productos.
- Crear y consultar pedidos de una o varias líneas.
- Confirmar un pedido y descontar el stock correspondiente.
- Cancelar un pedido confirmado y devolver sus unidades al stock.
- Validar las peticiones y devolver errores HTTP estructurados.
- Propagar o generar un identificador de correlación mediante la cabecera `X-Correlation-Id`.

### Fuera de alcance

Para mantener congelado el alcance de esta entrega, no se incluyen:

- Swagger UI ni otra interfaz gráfica para explorar la API.
- Autenticación, autorización o gestión de usuarios.
- Pagos, facturación y envíos.
- Reservas temporales de stock.
- Paginación, filtros o búsqueda avanzada.
- Despliegue en producción y observabilidad avanzada.

## Requisitos previos

- Java 21
- Maven 3.9 o posterior
- Docker con Docker Compose

## Puesta en marcha

1. Crea el archivo de variables de entorno a partir del ejemplo.

   En PowerShell:

   ```powershell
   Copy-Item .env.example .env
   ```

   En Linux o macOS:

   ```bash
   cp .env.example .env
   ```

2. Levanta PostgreSQL:

   ```bash
   docker compose up -d
   ```

3. Exporta las variables del archivo `.env` en la terminal desde la que ejecutarás la aplicación.

   En PowerShell:

   ```powershell
   Get-Content .env | ForEach-Object {
     $name, $value = $_ -split '=', 2
     Set-Item -Path "Env:$name" -Value $value
   }
   ```

   En Linux o macOS:

   ```bash
   set -a
   source .env
   set +a
   ```

4. Arranca la API.

   En PowerShell:

   ```powershell
   mvn spring-boot:run
   ```

   En Linux o macOS:

   ```bash
   mvn spring-boot:run
   ```

La API queda disponible en `http://localhost:8080`. Flyway crea y valida el esquema automáticamente durante el arranque.

Para detener y eliminar el contenedor:

```bash
docker compose down
```

Los datos permanecen en el volumen `stockflow_postgres_data`. Para eliminarlos también, ejecuta conscientemente `docker compose down -v`.

## Endpoints

| Método | Ruta | Descripción |
| --- | --- | --- |
| `POST` | `/api/v1/products` | Crear un producto |
| `GET` | `/api/v1/products` | Listar productos |
| `GET` | `/api/v1/products/{id}` | Consultar un producto |
| `PUT` | `/api/v1/products/{id}` | Actualizar un producto |
| `DELETE` | `/api/v1/products/{id}` | Eliminar un producto |
| `POST` | `/api/v1/orders` | Crear un pedido en estado `CREATED` |
| `GET` | `/api/v1/orders` | Listar pedidos |
| `GET` | `/api/v1/orders/{id}` | Consultar un pedido |
| `POST` | `/api/v1/orders/{id}/confirm` | Confirmar y descontar stock |
| `POST` | `/api/v1/orders/{id}/cancel` | Cancelar y recuperar stock |

## Ejemplo: producto → pedido → confirmación/cancelación

Los ejemplos usan `curl` y presuponen una base de datos vacía, por lo que el primer producto recibe el identificador `1`. Si ya existen datos, sustituye los identificadores por los devueltos por la API.

### 1. Crear un producto

```bash
curl -i -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: demo-stockflow" \
  -d '{"name":"Teclado","sku":"TEC-001","price":29.99,"stock":30}'
```

La API responde con `201 Created`. Comprueba el producto y su identificador:

```bash
curl -s http://localhost:8080/api/v1/products
```

### 2. Crear un pedido

```bash
curl -s -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"lines":[{"productId":1,"quantity":10}]}'
```

El pedido queda en estado `CREATED`, con un total de `299.90`; todavía no cambia el stock.

### 3A. Confirmar el pedido

Si el pedido creado tiene identificador `1`:

```bash
curl -s -X POST http://localhost:8080/api/v1/orders/1/confirm
```

El estado pasa a `CONFIRMED` y el stock del producto baja de `30` a `20`.

### 3B. Cancelar el pedido confirmado

```bash
curl -s -X POST http://localhost:8080/api/v1/orders/1/cancel
```

El estado pasa a `CANCELLED` y el stock vuelve de `20` a `30`. Solo se pueden cancelar pedidos confirmados.

## Validaciones y errores

- El nombre y el SKU son obligatorios.
- El precio debe ser mayor que cero y el stock no puede ser negativo.
- El SKU debe ser único.
- Un pedido requiere al menos una línea y cada cantidad debe ser como mínimo `1`.
- No se puede confirmar un pedido sin stock suficiente.
- No se puede confirmar dos veces ni cancelar un pedido que no esté confirmado.

Las respuestas de error incluyen estado, mensaje, ruta, fecha e identificador de correlación para facilitar el diagnóstico.

## Compilación y tests

Con PostgreSQL levantado y las variables de entorno cargadas:

```powershell
mvn verify
```

El workflow de GitHub Actions ejecuta automáticamente `verify` con Java 21 y un servicio PostgreSQL 17 en cada pull request y en los pushes a `main` o `master`.
