

WITH following AS (
	SELECT follower_id AS id FROM follows
	WHERE followed_id = 994 AND status = 'active'
), followers AS (
	SELECT followed_id AS id FROM follows
	WHERE follower_id = 994 AND status = 'active'
), mutual AS (
	SELECT fw.id
	FROM following fw
	INNER JOIN followers fr ON fw.id = fr.id
)
SELECT DISTINCT u.username, u.id, COUNT(m.id) AS relevant
FROM mutual m
INNER JOIN follows f ON f.follower_id = m.id AND f.status = 'active'
INNER JOIN users u ON u.id = f.followed_id
WHERE u.id != 994 AND u.id NOT IN (
	SELECT followed_id 
	FROM follows 
	WHERE follower_id = 994 AND status = 'active'
)
GROUP BY u.username, u.id
ORDER BY Relevant DESC
LIMIT 50

