-- V__seed_manage_academic_batch_permission.sql

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'SYS_ADMIN' AND p.name = 'MANAGE_ACADEMIC_BATCH'
  AND NOT EXISTS (
    SELECT 1 FROM role_permission rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);