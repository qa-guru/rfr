DELETE f
FROM `friendship` f
INNER JOIN `user` u ON u.username = :username
WHERE f.status = 'PENDING'
  AND (f.requester_id = u.id OR f.addressee_id = u.id)
