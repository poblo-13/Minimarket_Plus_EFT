# Evidencia verificable S8

Esta evidencia fue observada en la ejecución clean-room S8. Los resultados siguientes corresponden a los comandos y comprobaciones indicados, no a una inferencia desde el código.

## Evidencia estática revisada

- [x] `pom.xml` declara Java 21, Spring HATEOAS y `springdoc-openapi-starter-webmvc-ui` 2.7.0.
- [x] `SecurityConfig` permite `/public/**`, `/auth/**`, `/swagger-ui/**`, `/swagger-ui.html` y `/v3/api-docs/**`; las rutas protegidas usan Bearer JWT y no hay inicio de sesión por formulario.
- [x] `OpenApiConfig` registra el esquema `bearerAuth` de tipo HTTP con esquema `bearer` y formato JWT.
- [x] `POST /auth/login` autentica credenciales y entrega el token JWT que se envía como `Authorization: Bearer <token>`; `JWT_SECRET` debe estar configurado para firmarlo y validarlo.
- [x] Los ocho controladores REST bajo `src/main/java/com/minimarket/controller` sustentan el inventario de `docs/API.md`.
- [x] Los DTO, enlaces HATEOAS y el manejador `ApiExceptionHandler` sustentan los contratos descritos.

## Evidencia de ejecución

- [x] Java 21 utilizado por la compilación.
- [x] `sh ./mvnw --batch-mode test` terminó correctamente con **142 pruebas**.
- [x] `sh ./mvnw --batch-mode verify` terminó correctamente con **142 pruebas**.
- [x] La ejecución de `verify` generó el reporte JaCoCo y el JAR de la aplicación.
- [x] `/v3/api-docs` fue accesible públicamente y entregó JSON OpenAPI con título `Minimarket API` y versión `v1`.
- [x] `/swagger-ui/index.html` fue accesible públicamente y renderizó el título `Minimarket API v1 OAS 3.0`.
- [x] Swagger UI mostró las 8 etiquetas: `Carrito`, `Inventario`, `Usuarios`, `Productos`, `Detalles de venta`, `Categorias`, `Ventas` y `Público`.
- [x] Swagger UI mostró **34 operaciones**, consistente con el inventario REST de `docs/API.md`.
- [x] Captura de la comprobación visual: [`docs/evidence/swagger-ui.png`](evidence/swagger-ui.png).

## Comandos reproducibles

```bash
sh ./mvnw --batch-mode test
sh ./mvnw --batch-mode verify
```

Con la aplicación activa, las rutas verificadas de OpenAPI son:

```bash
curl -fsS http://localhost:8080/v3/api-docs
```

Abra `http://localhost:8080/swagger-ui/index.html` para inspeccionar la UI. No incluya credenciales reales en capturas, documentación ni registros.

## Próximas mejoras priorizadas

Estas acciones son futuras y no deben confundirse con la evidencia S8 completada de las secciones anteriores.

1. **Prioridad 1 — contrato y seguridad:** automatizar la comprobación de Problem Details, enlaces HAL y autorización por recurso bajo mínimo privilegio.
2. **Prioridad 2 — cobertura y calidad:** ampliar la cobertura de errores, validación y roles, preservando pruebas de integración del contrato OpenAPI.
3. **Prioridad 3 — mantenibilidad y refinamiento:** unificar anotaciones OpenAPI, `produces` y relaciones HAL; versionar y registrar cualquier cambio de contrato.
