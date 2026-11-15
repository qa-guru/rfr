SELECT COUNT(*)
FROM `friendship`
WHERE requester_id = UUID_TO_BIN(:id, true)
   OR addressee_id = UUID_TO_BIN(:id, true)
