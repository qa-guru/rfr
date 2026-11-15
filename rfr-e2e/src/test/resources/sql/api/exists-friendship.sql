SELECT COUNT(*)
FROM `friendship` f
INNER JOIN `user` requester ON requester.id = f.requester_id
INNER JOIN `user` addressee ON addressee.id = f.addressee_id
WHERE requester.username = :requester
  AND addressee.username = :addressee
  AND f.status = :status
