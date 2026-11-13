DELETE f
FROM friendship f
JOIN `user` u ON u.id IN (f.requester_id, f.addressee_id)
WHERE u.username = :username
  AND f.status = 'ACCEPTED'
