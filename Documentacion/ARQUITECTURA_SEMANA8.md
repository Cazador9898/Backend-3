# Arquitectura Semana 8

La solución conserva la arquitectura de microservicios de las semanas anteriores e incorpora seguridad OAuth2 y despliegue con contenedores.

## Seguridad
`auth-server` funciona como servidor de autorización. Los consumidores solicitan un JWT utilizando Client Credentials. `account-service`, `transaction-service` y `customer-service` actúan como OAuth2 Resource Servers y validan el JWT mediante el JWK Set del servidor de autorización.

## Comunicación síncrona resiliente
`transaction-service` consulta a `account-service`. La llamada está protegida con un Circuit Breaker de Resilience4j. El Bearer Token recibido por `transaction-service` se reenvía a `account-service`, conservando la protección OAuth2 entre servicios.

## Comunicación asíncrona
`transaction-service` publica `transaction.created` en Apache Kafka y `account-service` consume el evento. Esto desacopla la creación de una transacción del procesamiento posterior.

## Docker
Cada servicio dispone de un Dockerfile. `docker-compose.yml` orquesta Auth Server, Config Server, Eureka, los tres microservicios y Kafka sobre la red `banco-network`.
