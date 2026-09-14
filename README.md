# BarrioDigital — BFF

Backend For Frontend del sistema BarrioDigital (gestión de trámites vecinales), parte de la Evaluación Parcial 1
de Desarrollo Cloud Native I. Construido con Spring Boot y Spring Security.

## Responsabilidad

- Valida el JWT emitido por Microsoft Entra ID (issuer, audiencia, firma y vigencia).
- Aplica autorización por rol (`Admin`, `Vecino`) sobre cada endpoint.
- Orquesta las llamadas hacia los microservicios de dominio (`ms-barriodigital-requests` y
  `ms-barriodigital-catalog`). No se conecta directamente a la base de datos.

## Endpoints principales

- `GET /api/public` — endpoint de prueba, sin autenticación.
- `GET/POST /api/requests`, `PUT /api/requests/{id}/estado` — gestión de trámites.
- `GET/POST/PUT/DELETE /api/catalog` — gestión del catálogo de tipos de trámite y cupos.

## Configuración

Corre en el puerto `8080` y necesita, como variables de entorno, el tenant y client ID de Entra ID, las URLs de
los microservicios y el origen permitido para CORS (ver `deploy/ms-barriodigital-bff.service`).

## Ejecutar localmente

```bash
./mvnw spring-boot:run
```

## Compilar

```bash
./mvnw clean package
```
