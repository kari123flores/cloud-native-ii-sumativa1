# Arquitectura - Sistema de Gestión de Usuarios y Roles

```mermaid
flowchart LR
    C[Cliente / Postman] --> BFF[BFF Usuarios y Roles<br/>Puerto 8080]

    BFF --> FU[Function Usuarios<br/>Puerto 8081]
    BFF --> FR[Function Roles<br/>Puerto 8082]

    FU --> DB[(Oracle Database<br/>USUARIOS)]
    FR --> DB2[(Oracle Database<br/>ROLES)]

    DC[Docker Compose] -. ejecuta .-> BFF
    DC -. ejecuta .-> FU
    DC -. ejecuta .-> FR