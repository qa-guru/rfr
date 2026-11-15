SELECT BIN_TO_UUID(id, true)
FROM `user`
WHERE username = :username
