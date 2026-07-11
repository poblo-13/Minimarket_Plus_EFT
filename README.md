# Minimarket API — S8

Backend REST para la gestión de un minimarket. Esta entrega documenta la implementación S8 actualmente presente en el código: no expone servicios SOAP.

## Tecnología y ejecución

- Java 21
- Spring Boot 3.4.1
- Maven Wrapper (`mvnw`)
- Spring Security con HTTP Basic
- Spring HATEOAS y springdoc OpenAPI

Ejecutar la aplicación:

```bash
bash ./mvnw spring-boot:run
```

La URL base local es `http://localhost:8080` salvo que la configuración de Spring la cambie.

## Documentación OpenAPI

Con la aplicación en ejecución, Springdoc publica:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Especificación OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Ambas rutas son públicas según la configuración de seguridad y fueron verificadas en ejecución. Swagger UI renderizó **Minimarket API v1 OAS 3.0**, con 8 etiquetas y 34 operaciones; `/v3/api-docs` entregó JSON con título `Minimarket API` y versión `v1`. Consulte la evidencia en [`docs/EVIDENCE.md`](docs/EVIDENCE.md).

## Seguridad

`GET /public/hola` es el único endpoint funcional público. El resto requiere autenticación HTTP Basic; las credenciales inválidas o ausentes reciben un problema `401` y el reto `WWW-Authenticate: Basic realm="minimarket"`.

Las restricciones por rol implementadas son:

| Operación | Rol requerido |
| --- | --- |
| Crear, actualizar o eliminar productos | `ADMIN` |
| Crear, actualizar o eliminar movimientos de inventario | `ADMIN` |
| Crear ventas | `CAJERO` o `ADMIN` |
| Demás operaciones documentadas | Usuario autenticado |

La documentación no incluye credenciales. Configure usuarios y roles válidos de acuerdo con la instancia que ejecute.

## API REST

El inventario completo de rutas, DTO, navegación HAL y errores RFC 9457 está en [`docs/API.md`](docs/API.md).

## Pruebas y evidencia

La evidencia observada y los comandos reproducibles están en [`docs/EVIDENCE.md`](docs/EVIDENCE.md). En la evidencia clean-room se ejecutaron correctamente:

```bash
sh ./mvnw --batch-mode test
sh ./mvnw --batch-mode verify
```

Ambos comandos finalizaron correctamente con 135 pruebas; `verify` generó el reporte JaCoCo y el JAR.

## Reflexión S8

**Calidad.** La separación entre DTO de entrada y respuesta evita exponer entidades y, en usuarios, evita serializar contraseña. La validación Bean Validation y las respuestas Problem Details reducen ambigüedad de entrada. La evidencia clean-room confirma 135 pruebas correctas, generación de JaCoCo y del JAR, además de la publicación de OpenAPI; las pruebas de regresión deben mantenerse al evolucionar el contrato.

**Navegación.** Las respuestas de recursos emplean enlaces HAL para `self` y para relaciones relevantes (por ejemplo, producto–categoría o detalle–venta). Esto permite que un cliente descubra recursos relacionados sin construir todas las URL manualmente. La cobertura de relaciones no es uniforme: por ejemplo, la colección de usuarios solo declara enlace `self`; por ello el cliente no debe asumir enlaces no documentados.

**Mantenibilidad.** Los records DTO, el mapeador y el manejador central de excepciones concentran responsabilidades y hacen más local un cambio de contrato. La convivencia de controladores con distintos niveles de detalle en sus anotaciones OpenAPI es una oportunidad de mejora: conviene mantener respuestas, `produces` y restricciones de rol uniformes y respaldadas por pruebas de integración.
