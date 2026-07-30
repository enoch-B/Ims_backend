CREATE TYPE user_role AS ENUM ('ADMIN', 'HOD', 'ADVISOR', 'STUDENT');

CREATE TABLE app_user (
    id              BIGSERIAL PRIMARY KEY,
    full_name       VARCHAR(150) NOT NULL,
    email           VARCHAR(150) UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role            user_role NOT NULL,
    phone           VARCHAR(30),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE department (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    code            VARCHAR(20) UNIQUE NOT NULL,
    hod_user_id     BIGINT REFERENCES app_user(id),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE academic_batch (
    id              BIGSERIAL PRIMARY KEY,
    year_label      VARCHAR(20) NOT NULL,     -- e.g. "2025/26"
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL
);

CREATE TABLE student (
    user_id             BIGINT PRIMARY KEY REFERENCES app_user(id),
    student_id_number   VARCHAR(30) UNIQUE NOT NULL,
    department_id       BIGINT NOT NULL REFERENCES department(id),
    batch_id            BIGINT NOT NULL REFERENCES academic_batch(id),
    advisor_id          BIGINT REFERENCES app_user(id)   -- must have role=ADVISOR, enforce in service
);

CREATE TABLE company (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    sector          VARCHAR(100),
    address         VARCHAR(255),
    contact_person  VARCHAR(150),
    contact_phone   VARCHAR(30),
    contact_email   VARCHAR(150),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);