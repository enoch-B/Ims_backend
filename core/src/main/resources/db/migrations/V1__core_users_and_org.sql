-- V1a__permissions_and_roles.sql
CREATE TABLE permissions (
                             id              BIGSERIAL PRIMARY KEY,
                             name            VARCHAR(100) UNIQUE NOT NULL,
                             description     VARCHAR(255),
                             created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE roles (
                       id                              BIGSERIAL PRIMARY KEY,
                       name                            VARCHAR(50) UNIQUE NOT NULL,
                       permission_required_to_assign_role VARCHAR(100),
                       created_at                      TIMESTAMP NOT NULL DEFAULT now(),
                       last_update                     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE role_permission (
                                 role_id         BIGINT NOT NULL REFERENCES roles(id),
                                 permission_id   BIGINT NOT NULL REFERENCES permissions(id),
                                 PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user (
                          id              BIGSERIAL PRIMARY KEY,
                          full_name       VARCHAR(150) NOT NULL,
                          email           VARCHAR(150) UNIQUE,
                          password   VARCHAR(255) NOT NULL,
                          role_id         BIGINT NOT NULL REFERENCES roles(id),
                          is_active       BOOLEAN NOT NULL DEFAULT TRUE,
                          user_name       VARCHAR(255) UNIQUE NOT NULL,
                          must_change_password BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at      TIMESTAMP NOT NULL DEFAULT now()
);

-- seed data
INSERT INTO permissions (name, description) VALUES
                                                ('APPROVE_PLACEMENT', 'Approve or reject student placement requests'),
                                                ('ASSIGN_ADVISOR', 'Assign an advisor to a student'),
                                                ('SCORE_ADVISOR_EVAL', 'Submit advisor evaluation scores'),
                                                ('MANAGE_USERS', 'Create/deactivate user accounts'),
                                                ('MANAGE_DEPARTMENTS', 'Create/edit departments and batches'),
                                                ('VIEW_DEPARTMENT_DASHBOARD', 'View department-wide placement/grade dashboards'),
                                                ('SUBMIT_LOGBOOK', 'Submit own activity log entries'),
                                                ('REQUEST_PLACEMENT', 'Submit a placement request'),
                                                ('FINALIZE_GRADE', 'Publish final grade for a placement');

INSERT INTO roles (name) VALUES ('ADMIN'), ('HOD'), ('ADVISOR'), ('STUDENT');

CREATE TABLE department (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    code            VARCHAR(20) UNIQUE NOT NULL,
    hod_user_id     BIGINT REFERENCES user(id),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE academic_batch (
    id              BIGSERIAL PRIMARY KEY,
    year_label      VARCHAR(20) NOT NULL,     -- e.g. "2025/26"
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL
);

CREATE TABLE student (
    user_id             BIGINT PRIMARY KEY REFERENCES user(id),
    student_id_number   VARCHAR(30) UNIQUE NOT NULL,
    department_id       BIGINT NOT NULL REFERENCES department(id),
    batch_id            BIGINT NOT NULL REFERENCES academic_batch(id),
    advisor_id          BIGINT REFERENCES user(id)   -- must have role=ADVISOR, enforce in service
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