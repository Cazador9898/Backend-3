# Banco XYZ - Desarrollo Backend III - Semana 8
**Alumno:** Alonso Guzmán Ruz  
**Asignatura:** PBY2203 - Desarrollo Backend III

## Objetivo
Continuar el proyecto de microservicios del Banco XYZ y prepararlo para un entorno Cloud resiliente y seguro. La solución incorpora **OAuth 2.0 con JWT**, **Docker**, **Docker Compose**, **Resilience4j** y **Apache Kafka**.

## Arquitectura
Servicios incluidos:

| Componente | Puerto | Función |
|---|---:|---|
| Auth Server | 9000 | Entrega tokens OAuth2/JWT mediante Client Credentials |
| Config Server | 8888 | Centraliza la configuración de los microservicios |
| Eureka Discovery Server | 8761 | Registro y descubrimiento de servicios |
| Account Service | 8081 | Consulta de cuentas y consumidor Kafka |
| Transaction Service | 8082 | Gestión de transacciones, productor Kafka y Circuit Breaker |
| Customer Service | 8083 | Consulta de clientes |
| Apache Kafka | 9092 | Mensajería asíncrona orientada a eventos |

Flujo principal:

```text
Cliente/Postman
      |
      | 1) client_id + client_secret
      v
Auth Server :9000
      |
      | 2) JWT Bearer Token
      v
Microservicios protegidos
      |
      +--> Account Service :8081
      +--> Transaction Service :8082 ----> Kafka ----> Account Service
      +--> Customer Service :8083
                         |
                         +---- Resilience4j ----> fallback si Account Service falla
```

## OAuth 2.0
Se utiliza el flujo **Client Credentials**, apropiado para comunicación entre aplicaciones o servicios.

Credenciales académicas del cliente:

```text
client_id: banco-client
client_secret: duoc-secret
scopes: banco.read banco.write
```

Los endpoints de consulta requieren `banco.read`. La creación de transacciones requiere `banco.write`.

## Kafka
`transaction-service` publica el evento `transaction.created`. `account-service` lo consume de forma asíncrona.

Ejemplo:

```json
{
  "id": 4,
  "accountId": 101,
  "fecha": "2026-10-04",
  "monto": 25000,
  "tipo": "credito"
}
```

## Resilience4j
El endpoint:

```text
GET /api/transactions/account/{id}/summary
```

consulta `account-service`. Si el servicio no está disponible, el Circuit Breaker entrega una respuesta degradada con `estado: DEGRADADO`.

---

# Ejecución recomendada con Docker

## Requisitos
- Java 17
- Maven 3.9 o superior
- Docker Desktop
- Docker Compose

## 1. Compilar todos los microservicios
Desde la raíz del proyecto:

```powershell
mvn.cmd clean package -DskipTests
```

Si Maven está agregado al PATH como `mvn`, también sirve:

```powershell
mvn clean package -DskipTests
```

## 2. Construir las imágenes Docker

```powershell
docker compose build
```

## 3. Levantar toda la arquitectura

```powershell
docker compose up -d
```

## 4. Revisar contenedores

```powershell
docker compose ps
```

## 5. Ver logs generales

```powershell
docker compose logs -f
```

Para salir de los logs: `Ctrl + C`.

---

# Pruebas de OAuth2

## Obtener token
En PowerShell:

```powershell
$token = (curl.exe -s -u banco-client:duoc-secret -d "grant_type=client_credentials" --data-urlencode "scope=banco.read banco.write" http://localhost:9000/oauth2/token | ConvertFrom-Json).access_token
```

Comprobar que existe:

```powershell
$token
```

## Comprobar protección
Sin token debe responder `401 Unauthorized`:

```powershell
curl.exe -i http://localhost:8081/api/accounts
```

Con token debe responder correctamente:

```powershell
curl.exe -H "Authorization: Bearer $token" http://localhost:8081/api/accounts
```

Clientes:

```powershell
curl.exe -H "Authorization: Bearer $token" http://localhost:8083/api/customers
```

Transacciones:

```powershell
curl.exe -H "Authorization: Bearer $token" http://localhost:8082/api/transactions
```

---

# Prueba de Kafka
Crear una nueva transacción:

```powershell
curl.exe -X POST http://localhost:8082/api/transactions -H "Authorization: Bearer $token" -H "Content-Type: application/json" -d '{"accountId":101,"monto":25000,"tipo":"credito"}'
```

Revisar el consumidor:

```powershell
docker compose logs account-service --tail 100
```

Debe aparecer un mensaje similar a:

```text
[KAFKA] Evento procesado por account-service: ...
```

---

# Prueba de Resilience4j

Con todos los servicios levantados:

```powershell
curl.exe -H "Authorization: Bearer $token" http://localhost:8082/api/transactions/account/101/summary
```

Debe responder `estado: OK`.

Detener Account Service:

```powershell
docker compose stop account-service
```

Repetir la petición:

```powershell
curl.exe -H "Authorization: Bearer $token" http://localhost:8082/api/transactions/account/101/summary
```

Debe responder con:

```json
{
  "estado": "DEGRADADO",
  "mensaje": "Account Service no disponible; Resilience4j activa respuesta de respaldo"
}
```

Volver a iniciar Account Service:

```powershell
docker compose start account-service
```

---

# Eureka y Config Server

Eureka:

```text
http://localhost:8761
```

Config Server, ejemplo:

```text
http://localhost:8888/account-service/default
```

---

# Detener el proyecto

```powershell
docker compose down
```

Para eliminar además volúmenes y contenedores asociados:

```powershell
docker compose down -v
```

## Evidencias recomendadas para la entrega
1. `mvn clean package` finalizado correctamente.
2. `docker compose build` sin errores.
3. `docker compose ps` con todos los componentes activos.
4. Eureka mostrando los microservicios registrados.
5. Solicitud sin token devolviendo 401.
6. Obtención del token OAuth2.
7. Consulta protegida funcionando con Bearer Token.
8. POST de una transacción.
9. Log de `account-service` mostrando el evento recibido por Kafka.
10. Respuesta normal `estado: OK` de Resilience4j.
11. Respuesta `estado: DEGRADADO` con `account-service` detenido.

## Entrega
La carpeta final debe contener el código fuente, este `README.md`, documentación y evidencias de ejecución. El archivo comprimido se entrega con el nombre:

```text
Exp3_S8_Alonso_Guzman_Ruz.zip
```
