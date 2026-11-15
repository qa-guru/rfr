SELECT COUNT(*)
FROM `friendship` f
INNER JOIN `user` u ON u.username = :username
WHERE f.status = :status
  AND (f.requester_id = u.id OR f.addressee_id = u.id)
