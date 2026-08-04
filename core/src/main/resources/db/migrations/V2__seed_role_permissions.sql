-- V__seed_role_permissions.sql

-- SYS_ADMIN gets everything
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN';

-- HOD
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'HOD'
  AND p.name IN (
                 'APPROVE_PLACEMENT',
                 'ASSIGN_ADVISOR',
                 'VIEW_DEPARTMENT_DASHBOARD',
                 'FINALIZE_GRADE'
    );

-- ADVISOR
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADVISOR'
  AND p.name IN (
                 'SCORE_ADVISOR_EVAL',
                 'VIEW_DEPARTMENT_DASHBOARD'
    );

-- STUDENT
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'STUDENT'
  AND p.name IN (
                 'REQUEST_PLACEMENT',
                 'SUBMIT_LOGBOOK'
    );