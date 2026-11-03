INSERT INTO `authority` (id, user_id, authority)
SELECT UUID_TO_BIN(UUID(), true), id, 'read'
FROM `user`
WHERE username = :username
UNION ALL
SELECT UUID_TO_BIN(UUID(), true), id, 'write'
FROM `user`
WHERE username = :username
