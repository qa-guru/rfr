INSERT INTO friendship (id, requester_id, addressee_id, status, created_at, responded_at, version)
SELECT UUID_TO_BIN(UUID(), true),
       requester.id,
       addressee.id,
       'ACCEPTED',
       CURRENT_TIMESTAMP(6),
       CURRENT_TIMESTAMP(6),
       0
FROM `user` requester
CROSS JOIN `user` addressee
WHERE requester.username = :requester
  AND addressee.username = :addressee
