# Arquitectura - Sistema de Gestión de Usuarios y Roles

```mermaid
flowchart LR
    C[Cliente / Postman] --> BFF[BFF Usuarios y Roles<br/>Spring Boot<br/>Puerto 8080]

    BFF --> FU[Azure Function<br/>Usuarios<br/>HTTP Trigger]
    BFF --> FR[Azure Function<br/>Roles<br/>HTTP Trigger]

    FU --> DB[(Oracle Database<br/>Tablas USUARIOS y ROLES)]
    FR --> DB

    AF[Azure Functions<br/>Puerto local 7071] -. aloja .-> FU
    AF -. aloja .-> FR
```

## Flujo de comunicación

1. El cliente realiza solicitudes HTTP al BFF.
2. El BFF recibe y orquesta las solicitudes.
3. El BFF consume las Azure Functions de Usuarios y Roles.
4. Las Azure Functions ejecutan las operaciones CRUD.
5. Las funciones se conectan mediante JDBC a Oracle Database.
6. Oracle almacena la información de usuarios y roles.

## Endpoints

### BFF
- `/api/usuarios`
- `/api/usuarios/{id}`
- `/api/roles`
- `/api/roles/{id}`

### Azure Functions
- `/api/usuarios`
- `/api/usuarios/{id}`
- `/api/roles`
- `/api/roles/{id}`