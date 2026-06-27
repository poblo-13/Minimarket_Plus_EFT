# Minimarket Plus - Backend II

Proyecto Spring Boot para gestión básica de productos, inventario, ventas y usuarios.

## Requisitos

- Java 21
- Maven Wrapper incluido (`mvnw`)

## Ejecutar pruebas

```bash
bash ./mvnw test
```

Con reporte de cobertura:

```bash
bash ./mvnw test jacoco:report
```

El reporte queda en:

```text
target/site/jacoco/index.html
```

## Variables de entorno

Se puede crear un archivo `.env` en la raíz:

```properties
APP_SEED_ENABLED=true
APP_ADMIN_USERNAME=admin
APP_ADMIN_PASSWORD=admin123
APP_CAJERO_USERNAME=cajero
APP_CAJERO_PASSWORD=cajero123
APP_CLIENTE_USERNAME=cliente
APP_CLIENTE_PASSWORD=cliente123
```

`.env` y `target/` están ignorados por Git.

## Roles

- `ADMIN`: administra productos e inventario.
- `CAJERO`: registra ventas.
- `CLIENTE`: consulta productos.

## Estado de pruebas

Última ejecución local:

```text
Tests run: 131, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
