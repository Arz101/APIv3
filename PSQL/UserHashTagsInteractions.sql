

-- ENCONTRAR LOS 5 HASHTAGS CON LOS QUE EL USUARIO x MAS INTERACTUA

SELECT h.name, COUNT(x.post_id) AS hash
FROM post_hashtag ph
INNER JOIN (
	SELECT publication_id AS post_id, created_at 
	FROM likes l
	WHERE l.user_id = 8
	ORDER BY l.created_at DESC
	LIMIT 100
) x ON x.post_id = ph.post_id
INNER JOIN hashtags h
	ON h.id = ph.hashtag_id
GROUP BY h.name
ORDER BY hash DESC
LIMIT 5

-- Posts basados en Intereses Principales:

WITH top_hashtags AS (
    SELECT ph.hashtag_id
    FROM post_hashtag ph
    INNER JOIN (
        SELECT publication_id AS post_id
        FROM likes
        WHERE user_id = 8
        ORDER BY created_at DESC
        LIMIT 100
    ) recent_likes ON recent_likes.post_id = ph.post_id
    GROUP BY ph.hashtag_id
    ORDER BY COUNT(*) DESC
    LIMIT 5
)
SELECT 
    p.id,
    p.description,
    p.picture,
    p.datecreated,
    u.username,
    (SELECT COUNT(*) FROM likes WHERE publication_id = p.id) AS likes,
    (SELECT COUNT(*) FROM comments WHERE post_id = p.id) AS comments
FROM posts p
JOIN users u ON u.id = p.user_id
JOIN profiles pf ON pf.user_id = p.user_id
LEFT JOIN follows f ON f.followed_id = p.user_id AND f.follower_id = 8
WHERE EXISTS (
    SELECT 1 FROM post_hashtag ph
    WHERE ph.post_id = p.id
    AND ph.hashtag_id IN (SELECT hashtag_id FROM top_hashtags)
)
AND (pf.private = false OR f.follower_id IS NOT NULL)
AND p.datecreated > NOW() - INTERVAL '30 days'
ORDER BY likes DESC
LIMIT 100;