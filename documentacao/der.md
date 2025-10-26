erDiagram
    CUSTOMERS {
        varchar(36) id PK
        varchar(70) name
        varchar(11) tell UK
        varchar(70) email UK
        varchar(11) document_cpf UK
        varchar(255) password
    }

    BARBERSHOPS {
        varchar(36) id PK
        varchar(255) name
        varchar(14) cnpj UK
        varchar(255) address
    }

    BARBERS {
        varchar(36) id PK
        varchar(70) name
        varchar(11) tell UK
        varchar(70) email UK
        varchar(11) document_cpf UK
        varchar(255) password
        boolean is_owner
        time work_start_time
        time work_end_time
        varchar(36) barbershop_id FK
    }

    ACTIVITIES {
        varchar(36) id PK
        varchar(255) activity_name
        decimal price
        int duration_minutes
        varchar(36) barbershop_id FK
    }

    APPOINTMENTS {
        bigint id PK
        datetime start_time
        datetime end_time
        varchar(50) status
        varchar(36) customer_id FK
        varchar(36) barber_id FK
        varchar(36) barbershop_id FK
    }

    BARBERSHOP_JOIN_REQUESTS {
        bigint id PK
        varchar(50) status
        varchar(36) barber_id FK
        varchar(36) barbershop_id FK
    }

    BARBER_ACTIVITIES {
        varchar(36) barber_id PK, FK
        varchar(36) activity_id PK, FK
    }

    APPOINTMENT_ACTIVITIES {
        bigint appointment_id PK, FK
        varchar(36) activity_id PK, FK
    }

    CUSTOMERS ||--o{ APPOINTMENTS : "realiza"
    BARBERS ||--o{ APPOINTMENTS : "atende"
    BARBERSHOPS ||--o{ APPOINTMENTS : "ocorre em"
    BARBERSHOPS ||--o{ BARBERS : "emprega"
    BARBERSHOPS ||--o{ ACTIVITIES : "oferece"
    BARBERSHOPS ||--o{ BARBERSHOP_JOIN_REQUESTS : "recebe"
    BARBERS ||--o{ BARBERSHOP_JOIN_REQUESTS : "solicita"
    
    BARBERS }|--o{ BARBER_ACTIVITIES : "realiza"
    ACTIVITIES }|--o{ BARBER_ACTIVITIES : "é realizada por"
    
    APPOINTMENTS }|--o{ APPOINTMENT_ACTIVITIES : "inclui"
    ACTIVITIES }|--o{ APPOINTMENT_ACTIVITIES : "está em"