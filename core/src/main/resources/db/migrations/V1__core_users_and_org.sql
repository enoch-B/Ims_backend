-- V1__core_schema.sql

CREATE TABLE department (
                            id              BIGSERIAL PRIMARY KEY,
                            name            VARCHAR(150) NOT NULL,
                            code            VARCHAR(20) UNIQUE NOT NULL,
                            hod_app_user_id     BIGINT,
                            created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE academic_batch (
                                id              BIGSERIAL PRIMARY KEY,
                                year_label      VARCHAR(20) NOT NULL,
                                start_date      DATE NOT NULL,
                                end_date        DATE NOT NULL
);

CREATE TABLE permissions (
                             id              BIGSERIAL PRIMARY KEY,
                             name            VARCHAR(100) UNIQUE NOT NULL,
                             description     VARCHAR(255),
                             created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE roles (
                       id                                  BIGSERIAL PRIMARY KEY,
                       name                                VARCHAR(50) UNIQUE NOT NULL,
                       permission_required_to_assign_role  VARCHAR(100),
                       created_at                          TIMESTAMP NOT NULL DEFAULT now(),
                       last_update                         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE role_permission (
                                 role_id         BIGINT NOT NULL REFERENCES roles(id),
                                 permission_id   BIGINT NOT NULL REFERENCES permissions(id),
                                 PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE app_user (
                          id                      BIGSERIAL PRIMARY KEY,
                          full_name               VARCHAR(150) NOT NULL,
                          user_name        VARCHAR(30) UNIQUE NOT NULL,
                          email                   VARCHAR(150),
                          password_hash           VARCHAR(255) NOT NULL,
                          role_id                 BIGINT NOT NULL REFERENCES roles(id),
                          department_id           BIGINT REFERENCES department(id),
                          must_change_password    BOOLEAN NOT NULL DEFAULT TRUE,
                          is_active               BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at              TIMESTAMP NOT NULL DEFAULT now()
);

ALTER TABLE department
    ADD CONSTRAINT fk_department_hod FOREIGN KEY (hod_app_user_id) REFERENCES app_user(id);

CREATE TABLE student (
                         app_user_id     BIGINT PRIMARY KEY REFERENCES app_user(id),
                         batch_id    BIGINT NOT NULL REFERENCES academic_batch(id),
                         advisor_id  BIGINT REFERENCES app_user(id)
);

CREATE TABLE refresh_token (
                               id              BIGSERIAL PRIMARY KEY,
                               app_user_id         BIGINT NOT NULL REFERENCES app_user(id),
                               token_hash      VARCHAR(255) UNIQUE NOT NULL,
                               expires_at      TIMESTAMP NOT NULL,
                               revoked         BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_app_user_department ON app_user(department_id);
CREATE INDEX idx_app_user_role ON app_user(role_id);
CREATE INDEX idx_app_user_user_name ON app_user(user_name);
CREATE INDEX idx_student_advisor ON student(advisor_id);
CREATE INDEX idx_student_batch ON student(batch_id);
CREATE INDEX idx_refresh_token_app_user ON refresh_token(app_user_id);
CREATE INDEX idx_refresh_token_hash ON refresh_token(token_hash);

-- seed data
INSERT INTO permissions (name, description) VALUES
                                                ('APPROVE_PLACEMENT', 'Approve or reject student placement requests'),
                                                ('ASSIGN_ADVISOR', 'Assign an advisor to a student'),
                                                ('SCORE_ADVISOR_EVAL', 'Submit advisor evaluation scores'),
                                                ('MANAGE_USERS', 'Create/deactivate app_user accounts'),
                                                ('MANAGE_DEPARTMENTS', 'Create/edit departments and batches'),
                                                ('VIEW_DEPARTMENT_DASHBOARD', 'View department-wide placement/grade dashboards'),
                                                ('SUBMIT_LOGBOOK', 'Submit own activity log entries'),
                                                ('REQUEST_PLACEMENT', 'Submit a placement request'),
                                                ('FINALIZE_GRADE', 'Publish final grade for a placement');

INSERT INTO roles (name) VALUES ('ADMIN'), ('HOD'), ('ADVISOR'), ('STUDENT');

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