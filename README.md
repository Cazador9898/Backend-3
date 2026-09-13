# Banco XYZ - Backend for Frontend (BFF) - Semana 5

Proyecto académico de **Desarrollo Backend III (PBY2203)**. Esta versión continúa el trabajo anterior e implementa el patrón **Backend for Frontend (BFF)** para tres canales del Banco XYZ: Web, Móvil y Cajero Automático.

## 1. Objetivo

El objetivo es que cada frontend consuma un backend adaptado a sus necesidades, evitando entregar la misma respuesta a todos los dispositivos.

- **Web:** respuesta completa para un dashboard.
- **Móvil:** respuesta liviana con solo datos esenciales.
- **ATM:** operaciones críticas de consulta de saldo y retiro.

## 2. Estrategia de implementación

Se utiliza una estrategia **BFF por canal dentro de una aplicación Spring Boot**, manteniendo controladores, servicios BFF y DTO separados. Los BFF consumen servicios Backend internos especializados para agregar la información necesaria.

```text
Cliente Web ------> WebBffController ------> WebBffService ------\
Cliente Móvil ----> MobileBffController ---> MobileBffService ----+--> Servicios Backend --> MySQL
Cajero ATM -------> AtmBffController ------> AtmService ---------/

Servicios Backend:
- AccountBackendService
- MovementBackendService
- AnalyticsBackendService
```

Esta separación permite extender un canal sin modificar innecesariamente los otros.

## 3. Integración y agregación de información

El **BFF Web** agrega datos desde tres servicios Backend: cuenta, movimientos y analítica. El **BFF Móvil** utiliza esos servicios pero transforma la respuesta y limita los movimientos a 3. El **BFF ATM** trabaja con la información mínima necesaria y utiliza transacciones para proteger el retiro.

Los datos provienen del procesamiento legacy conservado de las semanas anteriores mediante Spring Batch.

## 4. Estructura principal

```text
src/main/java/cl/duoc/bancoxyz/
├── backend/
│   └── service/
│       ├── AccountBackendService.java
│       ├── MovementBackendService.java
│       └── AnalyticsBackendService.java
├── bff/
│   ├── config/
│   │   ├── ChannelAuthInterceptor.java
│   │   └── WebMvcConfig.java
│   ├── controller/
│   │   ├── WebBffController.java
│   │   ├── MobileBffController.java
│   │   └── AtmBffController.java
│   ├── dto/
│   ├── exception/
│   └── service/
│       ├── WebBffService.java
│       ├── MobileBffService.java
│       └── AtmService.java
└── ... código Batch anterior
```

## 5. BFF Web

```http
GET /api/bff/web/cuentas/{cuentaId}/dashboard
```

Entrega:
- datos de cuenta;
- saldo actual;
- interés calculado;
- último estado anual;
- hasta 10 movimientos con descripción;
- últimos 7 resúmenes diarios.

```powershell
curl.exe -i -H "X-CHANNEL-TOKEN: web-xyz-2026" http://localhost:8080/api/bff/web/cuentas/101/dashboard
```

## 6. BFF Móvil

```http
GET /api/bff/mobile/cuentas/{cuentaId}/resumen
```

La respuesta se reduce a los datos esenciales y solo 3 movimientos para disminuir el payload.

```powershell
curl.exe -i -H "X-CHANNEL-TOKEN: mobile-xyz-2026" http://localhost:8080/api/bff/mobile/cuentas/101/resumen
```

## 7. BFF ATM

### Consultar saldo

```http
GET /api/bff/atm/cuentas/{cuentaId}/saldo
```

```powershell
curl.exe -i -H "X-CHANNEL-TOKEN: atm-xyz-2026" -H "X-ATM-ID: ATM-001" http://localhost:8080/api/bff/atm/cuentas/101/saldo
```

### Realizar retiro

```http
POST /api/bff/atm/cuentas/{cuentaId}/retiros
```

```json
{
  "monto": 100
}
```

```powershell
curl.exe -i -X POST `
  -H "Content-Type: application/json" `
  -H "X-CHANNEL-TOKEN: atm-xyz-2026" `
  -H "X-ATM-ID: ATM-001" `
  -d '{"monto":100}' `
  http://localhost:8080/api/bff/atm/cuentas/101/retiros
```

El retiro valida existencia de la cuenta y saldo suficiente, actualiza el saldo dentro de una transacción y registra la operación en `atm_operaciones`.

## 8. Optimización por canal

Las respuestas no tienen el mismo tamaño ni estructura:

| Canal | Optimización |
|---|---|
| Web | Hasta 10 movimientos, analítica y estado anual |
| Móvil | Solo 3 movimientos y campos esenciales |
| ATM | Solo saldo/estado o resultado del retiro |

Además:
- Web utiliza caché privada corta de 30 segundos.
- Móvil utiliza caché privada de 15 segundos.
- ATM usa `no-store` porque trabaja con información crítica de saldo.
- Cada respuesta incluye `X-BFF-Channel` para identificar el canal.

## 9. Seguridad por canal

Cada frontend utiliza la cabecera:

```text
X-CHANNEL-TOKEN
```

Tokens académicos por defecto:

```text
Web:    web-xyz-2026
Móvil:  mobile-xyz-2026
ATM:    atm-xyz-2026
```

ATM también requiere:

```text
X-ATM-ID
```

## 10. Ejecución

### Levantar MySQL

```powershell
docker compose up -d
```

### Compilar

```powershell
mvn clean package
```

### Primera ejecución y carga de datos

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--app.batch.run-on-startup=true --app.job.name=all"
```

Luego puede ejecutarse normalmente:

```powershell
mvn spring-boot:run
```

API:

```text
http://localhost:8080
```

## 10.1 Requisitos para ejecutar el proyecto

- Java 17 o superior.
- Maven.
- Docker Desktop.
- Postman para realizar las pruebas de las APIs.

## 10.2 Colección de Postman

El proyecto incluye el archivo:

```text
Banco_XYZ_BFF.postman_collection.json
```

La colección contiene las solicitudes necesarias para probar Web, Móvil, ATM y el control de seguridad. La URL base utilizada es:

```text
http://localhost:8080
```

## 11. Evidencia de ejecución

Se realizaron pruebas de las APIs mediante Postman para comprobar el funcionamiento de cada BFF y la adaptación de las respuestas según el canal. Las capturas obtenidas se incluyen en la carpeta `Evidencias` del proyecto.

Pruebas realizadas:

1. BFF Web respondiendo el dashboard.
2. BFF Móvil mostrando una respuesta más pequeña.
3. BFF ATM consultando saldo.
4. Retiro ATM exitoso.
5. Consulta de saldo posterior al retiro.
6. Error `401` al usar token incorrecto.
Estas pruebas permiten verificar el funcionamiento independiente de los tres canales, la ejecución de una operación crítica de retiro y el control de acceso mediante token.

## 12. Componentes de la entrega

El proyecto considera los tres elementos solicitados para la entrega:

- **Código fuente:** implementación de los BFF Web, Móvil y ATM, junto con los servicios Backend y el procesamiento de datos.
- **Documentación:** este archivo `README.md`, con el objetivo, estrategia, estructura e instrucciones de ejecución.
- **Evidencia de ejecución:** capturas de las pruebas realizadas en Postman y almacenadas en la carpeta `Evidencias`.

## 13. Conclusión


La implementación separa los tres canales mediante BFF específicos. Cada uno transforma y limita la información según las necesidades del cliente. Web agrega datos de varios servicios para construir un dashboard completo, Móvil reduce la información para disminuir el consumo de red y ATM mantiene una interfaz simple y segura para operaciones críticas. La estructura modular permite agregar nuevos canales o servicios sin reescribir los BFF existentes.
