# Contrato REST S8

Base local de ejemplo: `http://localhost:8080`. Todas las rutas siguientes corresponden a controladores REST del código S8. No hay rutas SOAP documentadas ni implementadas por estos controladores.

## Convenciones

- `POST /auth/login` recibe credenciales y devuelve un token JWT. Configure `JWT_SECRET` antes de iniciar la aplicación para firmar y validar los tokens.
- Salvo `GET /public/hola`, `POST /auth/login` y la UI/especificación OpenAPI, las rutas requieren Bearer JWT mediante `Authorization: Bearer <token>`.
- Una operación restringida sin autenticar devuelve `401`; autenticada sin el rol requerido devuelve `403` cuando la protección por método aplica.
- Los controladores devuelven `EntityModel` y `CollectionModel` de Spring HATEOAS. La representación de productos declara explícitamente `application/hal+json`; las demás respuestas HATEOAS contienen enlaces generados por el framework.
- Las creaciones devuelven `201 Created`, un cuerpo del recurso y `Location` hacia su enlace `self`.
- Los `id` que están anotados con `@Positive` deben ser positivos; otros controladores aceptan el `Long` del path sin esa validación explícita.

## Inventario de endpoints

La columna **acceso** describe la regla implementada; los estados son las respuestas exitosas y de error declaradas o producidas por el controlador.

| Método | Ruta | Acceso | Resultado / estados |
| --- | --- | --- | --- |
| GET | `/public/hola` | Público | `200`, texto `¡Hola Mundo!` |
| POST | `/auth/login` | Público | `200` token JWT; `400`, `401` |
| GET | `/api/productos` | Autenticado | `200` colección HAL; `401` |
| GET | `/api/productos/{id}` | Autenticado | `200` HAL; `400`, `401`, `404` |
| POST | `/api/productos` | `ADMIN` | `201` HAL; `400`, `401`, `403`, `404` |
| PUT | `/api/productos/{id}` | `ADMIN` | `200` HAL; `400`, `401`, `403`, `404` |
| DELETE | `/api/productos/{id}` | `ADMIN` | `204`; `400`, `401`, `403`, `404` |
| GET | `/api/categorias` | Autenticado | `200` colección; `401` |
| GET | `/api/categorias/{id}` | Autenticado | `200`; `401`, `404` |
| POST | `/api/categorias` | Autenticado | `201`; `400`, `401` |
| PUT | `/api/categorias/{id}` | Autenticado | `200`; `400`, `401`, `404` |
| DELETE | `/api/categorias/{id}` | Autenticado | `204`; `401`, `404` |
| GET | `/api/inventario` | Autenticado | `200` colección; `401` |
| GET | `/api/inventario/{id}` | Autenticado | `200`; `400`, `401`, `404` |
| POST | `/api/inventario` | `ADMIN` | `201`; `400`, `401`, `403`, `404` |
| PUT | `/api/inventario/{id}` | `ADMIN` | `200`; `400`, `401`, `403`, `404` |
| DELETE | `/api/inventario/{id}` | `ADMIN` | `204`; `400`, `401`, `403`, `404` |
| GET | `/api/carrito` | Autenticado | `200` colección; `401` |
| GET | `/api/carrito/{id}` | Autenticado | `200`; `400`, `401`, `404` |
| POST | `/api/carrito` | Autenticado | `201`; `400`, `401`, `404` |
| PUT | `/api/carrito/{id}` | Autenticado | `200`; `400`, `401`, `404` |
| DELETE | `/api/carrito/{id}` | Autenticado | `204`; `400`, `401`, `404` |
| GET | `/api/ventas` | Autenticado | `200` colección; `401` |
| GET | `/api/ventas/{id}` | Autenticado | `200`; `401`, `404` |
| POST | `/api/ventas` | `CAJERO` o `ADMIN` | `201`; `400`, `401`, `403`, `404` |
| GET | `/api/detalle-ventas` | Autenticado | `200` colección; `401` |
| GET | `/api/detalle-ventas/{id}` | Autenticado | `200`; `401`, `404` |
| POST | `/api/detalle-ventas` | Autenticado | `201`; `400`, `401`, `404` |
| PUT | `/api/detalle-ventas/{id}` | Autenticado | `200`; `400`, `401`, `404` |
| DELETE | `/api/detalle-ventas/{id}` | Autenticado | `204`; `401`, `404` |
| GET | `/api/usuarios` | Autenticado | `200` colección; `401` |
| GET | `/api/usuarios/{id}` | Autenticado | `200`; `400`, `401`, `404` |
| POST | `/api/usuarios` | Autenticado | `201`; `400`, `401`, `409` |
| PUT | `/api/usuarios/{id}` | Autenticado | `200`; `400`, `401`, `404`, `409` |
| DELETE | `/api/usuarios/{id}` | Autenticado | `204`; `400`, `401`, `404` |

## DTO de solicitud y respuesta

| Recurso | Solicitud | Respuesta |
| --- | --- | --- |
| Producto | `nombre` (texto no vacío, máx. 150), `precio` (>= 0.01), `stock` (>= 0), `categoriaId` (positivo) | `id`, `nombre`, `precio`, `stock`, `categoriaId` |
| Categoría | `nombre` (texto no vacío, máx. 100) | `id`, `nombre` |
| Inventario | `productoId` (positivo), `cantidad` (>= 1), `tipoMovimiento` (`Entrada` o `Salida`), `fechaMovimiento` (fecha-hora) | `id`, `productoId`, `cantidad`, `tipoMovimiento`, `fechaMovimiento` |
| Carrito | `usuarioId`, `productoId` (positivos), `cantidad` (>= 1) | `id`, `usuarioId`, `productoId`, `cantidad` |
| Venta | `usuarioId` (positivo), `fecha` (fecha-hora presente o pasada) | `id`, `usuarioId`, `fecha`, `detalleIds`, `total` |
| Detalle de venta | `ventaId`, `productoId` (positivos), `cantidad` (>= 1), `precio` (>= 0.01) | `id`, `ventaId`, `productoId`, `cantidad`, `precio` |
| Usuario — crear | `username` (3–100), `password` (8–128, minúscula, mayúscula y dígito), `rolIds` (conjunto no vacío de positivos) | `id`, `username`, `rolIds` |
| Usuario — actualizar | `username` (3–100), `password` opcional con las mismas reglas, `rolIds` opcional (si se envía, no vacío) | `id`, `username`, `rolIds` |

Las fechas usan `LocalDateTime`; use JSON ISO-8601 sin zona, por ejemplo `2026-07-11T14:30:00`. Los DTO de respuesta no incluyen la contraseña ni grafos JPA.

## HAL y navegación

Un recurso HATEOAS se serializa con sus campos y `_links`. Ejemplo conceptual de producto:

```json
{
  "id": 10,
  "nombre": "Leche",
  "precio": 1290.00,
  "stock": 8,
  "categoriaId": 2,
  "_links": {
    "self": { "href": "http://localhost:8080/api/productos/10" },
    "collection": { "href": "http://localhost:8080/api/productos" },
    "categoria": { "href": "http://localhost:8080/api/categorias/2" }
  }
}
```

Enlaces implementados: producto (`self`, `collection`, `categoria`); categoría (`self`, `categorias`, `productos`); inventario (`self`, `producto`); carrito (`self`, `usuario`, `producto`); venta (`self`, `ventas`, `detalles`, y `usuario` si existe); detalle (`self`, `detalle-ventas`, y relaciones disponibles); usuario individual (`self`). Las colecciones incluyen `self`.

## Errores RFC 9457

El manejador global devuelve `ProblemDetail` para validación, JSON malformado, tipo de parámetro inválido, ausencia de recurso, conflicto de datos y errores no controlados. Seguridad genera `401` y `403` también como `application/problem+json`.

La forma efectiva incluye los miembros estándar `type`, `title`, `status`, `detail` e `instance` que serializa Spring, y extensiones implementadas por la aplicación: `code`, `path` y, para validación, `errors` (`field`, `message`). Los tipos son URN, por ejemplo `urn:minimarket:error:validation_error`, `...:not_found`, `...:conflict`, `...:unauthorized` y `...:forbidden`.

Ejemplo de la forma de respuesta de validación:

```json
{
  "type": "urn:minimarket:error:validation_error",
  "title": "Bad Request",
  "status": 400,
  "detail": "Request validation failed.",
  "code": "VALIDATION_ERROR",
  "path": "/api/productos",
  "errors": [{ "field": "nombre", "message": "must not be blank" }]
}
```
